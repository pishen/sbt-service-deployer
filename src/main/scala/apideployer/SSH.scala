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

import java.lang.RuntimeException
import org.slf4s.Logging
import sys.process._

case class SSH(
  user: String,
  address: String,
  command: String = null,
  keyFile: String = null,
  pty: Boolean = false
) extends Logging {
  val fullCommand = Seq(
    "ssh",
    "-o", "UserKnownHostsFile=/dev/null",
    "-o", "StrictHostKeyChecking=no"
  )
    .++(if (keyFile == null) Seq.empty else Seq("-i", keyFile))
    .++(if (pty) Seq("-tt") else Seq.empty)
    .:+(user + "@" + address)
    .:+(command)

  def run(): String = {
    log.info("[SSH] " + fullCommand.mkString(" "))
    
    val buffer = new StringBuffer()
    val exitValue = fullCommand.run(BasicIO(false, buffer, None)).exitValue
    
    if (exitValue != 0) {
      throw new RuntimeException(s"exitValue = $exitValue")
    } else {
      buffer.toString()
    }
  }
}
