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

import sbt._
import sbt.Keys._
import sbtassembly.AssemblyKeys._
import com.vast.sbtlogger.SbtLogger._
import sbtassembly.AssemblyPlugin
import scala.util.{ Failure, Success, Try }

object APIDeployerPlugin extends AutoPlugin {
  object autoImport {
    lazy val Staging = config("staging") extend (Compile)
    lazy val Production = config("production") extend (Compile)

    lazy val servers = settingKey[Seq[String]]("servers' ip")
    lazy val user = settingKey[String]("user name")
    lazy val elbName = settingKey[String]("elb's name")
    lazy val elbRegion = settingKey[String]("elb's region")

    lazy val deploy = taskKey[Unit]("Deploy the code.")
  }
  import autoImport._
  override def trigger = allRequirements
  override def requires = AssemblyPlugin

  lazy val baseAPIDeployerSettings = Seq(
    deploy := {
      val log = streams.value.log
      withLogger(log) {
        val jar = assembly.value
        val fallbackedServers = servers.?.value.getOrElse(ELBHelper.getServersUnderELB(elbName.value, elbRegion.value))
        fallbackedServers.foreach { server =>
          log.info(s"Deploying to server $server")
          val ssh = SSH(user.value, server)
          if(ssh.copy(command = "ls").run.contains("RUNNING_PID")){
            val pid = ssh.copy(command = "cat RUNNING_PID").run
            log.info(s"killing old process $pid")
            //kill the process
            ssh.copy(command = "kill `cat RUNNING_PID`").run
            //check if the process is stopped, and clean up
            Retry(){ _ =>
              Try(ssh.copy(command = s"kill -0 $pid").run) match {
                case Success(_) =>
                  throw new RuntimeException("process still exists")
                case Failure(_) =>
                  log.info("process killed")
                  ssh.copy(command = "rm RUNNING_PID").run
              }
            }
          }
          //upload jar
          RSync(jar, user.value, server, s"~/${jar.getName}").sync()
          //start the jar
          ssh.copy(command = s"nohup java -jar ${jar.getName} > logfile 2>&1 & echo $$! > RUNNING_PID").run
        }
      }
    }
  )

  override lazy val projectSettings =
    inConfig(Production) {
      AssemblyPlugin.baseAssemblySettings ++ baseAPIDeployerSettings ++ Seq(
        fullClasspath in assembly += baseDirectory.value / "production",
        assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false)
      )
    } ++ inConfig(Staging) {
      AssemblyPlugin.baseAssemblySettings ++ baseAPIDeployerSettings ++ Seq(
        fullClasspath in assembly += baseDirectory.value / "staging",
        assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false)
      )
    } :+ (assemblyOption in assembly := (assemblyOption in assembly).value.copy(cacheOutput = false))
}
