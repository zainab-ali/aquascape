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

package aquascape.golden

import cats.effect.IO
import fs2.io.file.Path

final case class GroupName(value: Path) extends AnyVal
final case class TestName(name: String, value: Path) {
  val imageDirName: String = name.replace(" ", "-")
  def imageDir: Path = value / imageDirName
  def markdownFile: Path = value / s"${imageDirName}.md"
}
final case class ExampleName(value: String, parent: TestName) {
  private val dashedName: String = value.replaceAll("\\s|\\(|\\)", "-")
  def imageFile: Path = parent.imageDir / s"${dashedName}.png"
  def relativeImage: String = s"${parent.imageDirName}/${dashedName}.png"
}

final case class RangePos(source: String, startAt: Int, endAt: Int)
final case class StreamCode(pos: RangePos, code: IO[Any])