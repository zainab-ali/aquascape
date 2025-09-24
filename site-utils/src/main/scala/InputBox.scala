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

/** Describes how a value encodes to and from an <input> element.
  *
  * In practice, this only works for numbers. More work is needed to support
  * coproducts (e.g. with radio buttons).
  */
trait InputBox[A] {

  def attributes: Map[String, String]
  def inputType: String
  def label: String
  def default: A

  def decode(text: String): Option[A]
  def encode(a: A): String
}

object InputBox {
  def int(
      labelText: String,
      min: Int,
      max: Int,
      defaultValue: Int
  ): InputBox[Int] = new InputBox[Int] {
    def attributes: Map[String, String] = Map(
      "min" -> min.toString,
      "max" -> max.toString
    )
    def default: Int = defaultValue
    def label: String = labelText
    def inputType: String = "number"
    def encode(i: Int): String = i.toString
    def decode(text: String): Option[Int] =
      scala.util.Try(Integer.parseInt(text)).toOption
  }
}
