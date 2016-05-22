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

package servicedeployer

import sbt._
import sbt.Keys._
import com.vast.sbtlogger.SbtLogger._
import scala.util.{ Failure, Success, Try }

object ServiceDeployerPlugin extends AutoPlugin {
  object autoImport {
    lazy val Staging = config("staging") extend (Compile)
    lazy val Production = config("production") extend (Compile)

    lazy val deployServers = settingKey[Seq[String]]("servers' ip")
    lazy val deployUser = settingKey[String]("user name")
    lazy val deployFile = taskKey[File]("deploy file")
    lazy val deployCommand = taskKey[String]("command to start the service")

    lazy val deploy = taskKey[Unit]("Deploy the code.")
  }
  import autoImport._
  override def trigger = allRequirements

  lazy val baseServiceDeployerSettings = Seq(
    deploy := {
      val log = streams.value.log
      withLogger(log) {
        val file = deployFile.value
        deployServers.value.foreach { server =>
          log.info(s"Deploying to server $server")
          val ssh = SSH(deployUser.value, server)
          if(ssh.withCommand("ls").run.contains("running.pid")){
            val pid = ssh.withCommand("cat running.pid").run
            log.info(s"killing old process $pid")
            //kill the process
            ssh.withCommand("kill `cat running.pid`").run
            //check if the process is stopped, and clean up
            Retry(){ _ =>
              Try(ssh.withCommand(s"kill -0 $pid").run) match {
                case Success(_) =>
                  throw new RuntimeException("process still exists")
                case Failure(_) =>
                  log.info("process killed")
                  ssh.withCommand("rm running.pid").run
              }
            }
          }
          //upload jar
          RSync(file, deployUser.value, server, s"~/${file.getName}").run()
          //start the jar
          ssh.withCommand(s"nohup ${deployCommand.value} > running.log 2>&1 & echo $$! > running.pid").run
        }
      }
    }
  )

  override lazy val projectSettings =
    inConfig(Production)(baseServiceDeployerSettings) ++
      inConfig(Staging)(baseServiceDeployerSettings)
}
