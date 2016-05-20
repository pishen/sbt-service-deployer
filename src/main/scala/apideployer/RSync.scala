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

import java.io.File
import org.slf4s.Logging
import sys.process._

case class RSync(
  sourceFile: File,
  user: String,
  address: String,
  targetPath: String,
  keyFile: String = null
) extends Logging {
  def sync() = {
    val sshCommand = Seq(
      "ssh",
      "-o", "UserKnownHostsFile=/dev/null",
      "-o", "StrictHostKeyChecking=no"
    ).++(if (keyFile == null) Seq.empty else Seq("-i", keyFile))

    val syncCommand = Seq(
      "rsync",
      "--progress",
      "-ve", sshCommand.mkString(" "),
      sourceFile.getAbsolutePath,
      s"${user}@${address}:${targetPath}"
    )
    
    log.info("[RSync] " + syncCommand.mkString(" "))
    
    val exitValue = syncCommand.!
    if(exitValue != 0){
      throw new RuntimeException(s"exitValue = $exitValue")
    }
  }
}
