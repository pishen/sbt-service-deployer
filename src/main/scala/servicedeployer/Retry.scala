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

import scala.util.{ Failure, Success, Try }

case class Retry(attempts: Int = 20, sleep: Int = 10) {
  def apply[T](op: Int => T): T = {
    @annotation.tailrec
    def singleRun(attempt: Int): T = {
      Try { op(attempt) } match {
        case Success(x) => x
        case Failure(e) if attempt < attempts =>
          Thread.sleep(sleep.toLong * 1000)
          singleRun(attempt + 1)
        case Failure(e) => throw e
      }
    }
    singleRun(1)
  }
}
