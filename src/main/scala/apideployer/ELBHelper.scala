/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package apideployer

import com.amazonaws.regions.Regions
import scala.collection.JavaConverters._

import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest

object ELBHelper {
  def getServersUnderELB(elbName: String, elbRegion: String, privateIp: Boolean = true) = {
    val region = Regions.fromName(elbRegion)
    val elb = new AmazonElasticLoadBalancingClient().withRegion[AmazonElasticLoadBalancingClient](region)
    elb.describeLoadBalancers(new DescribeLoadBalancersRequest().withLoadBalancerNames(elbName))
      .getLoadBalancerDescriptions.asScala.headOption
      .map { desc =>
        val instanceIds = desc.getInstances.asScala.map(_.getInstanceId)
        val ec2 = new AmazonEC2Client().withRegion[AmazonEC2Client](region)
        ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceIds.asJava))
          .getReservations.asScala.flatMap(_.getInstances.asScala)
          .map(i => if (privateIp) i.getPrivateIpAddress else i.getPublicIpAddress)
      }
      .getOrElse(Seq.empty)
  }
}
