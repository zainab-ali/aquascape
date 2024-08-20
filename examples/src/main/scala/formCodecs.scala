/*
 * Copyright 2023 Zainab Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aquascape.examples

object formCodecs {
  def intCodec(min: Int, max: Int): FormCodec[Int] = new FormCodec[Int] {
    def attributes: Map[String, String] = Map(
      "min" -> min.toString,
      "max" -> max.toString
    )
    def inputType: String = "number"
    def encode(i: Int): String = i.toString
    def decode(text: String): Option[Int] =
      scala.util.Try(Integer.parseInt(text)).toOption
  }
}
