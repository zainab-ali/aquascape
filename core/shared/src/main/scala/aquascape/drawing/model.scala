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

package aquascape.drawing
import doodle.core.Color
import doodle.core.font.Font
import doodle.core.font.FontFamily

private final case class Diagram(labels: List[String], items: List[Item])
private enum Item {
  case Pull(from: Int, to: Int)
  case Done(from: Int, to: Int, pullProgress: Int)
  // Do we care that this is anything more than a text box placed along a connector?
  case Output(
      value: String,
      from: Int,
      to: Int,
      pullProgress: Int
  )
  // TODO: Having an "at" label is misleading.
  // While there is an order of events, there is no label to evaluate the effect "at".
  // This is especially confusing for effects in resource finalizers. The effect is evaluated in a given order, but is marked with whatever label the scope closer has, and not the resource label.
  case Eval(value: String, at: Int)
  case Time(value: Int)
  case Error(
      value: String,
      from: Int,
      to: Int,
      pullProgress: Int
  )
  case Finished(value: String, errored: Boolean)
}

final case class Config(
    // The dash style used to mark the horizontal stage line
    stageLineStrokeDash: Array[Int],
    // The color of the stage line
    stageLineColor: Color,
    // The height between each stage line
    stageHeight: Int,
    // The halfwidth of the base of the arrowheads
    arrowBaseHalfWidth: Int,
    progressPaddingLeft: Int,
    // The space to the right of each output element.
    outputPaddingRight: Int,
    // The color of the label font and boxes
    labelColor: Color,
    // Padding around the label text box
    labelPaddingHeight: Int,
    labelPaddingWidth: Int,
    // Padding around the eval and error boxes
    textBoxPaddingWidth: Int,
    textBoxPaddingHeight: Int,
    stageColor: Color,
    evalColor: Color,
    timeColor: Color,
    errorColor: Color,
    progressColor: Color,
    outputColor: Color,
    pullColor: Color,
    doneColor: Color,
    startDotColor: Color,
    font: Font
) {
  def arrowBaseWidth: Int = arrowBaseHalfWidth * 2

  def scale(factor: Int) = copy(
    stageHeight = stageHeight * factor,
    arrowBaseHalfWidth = arrowBaseHalfWidth * factor,
    progressPaddingLeft = progressPaddingLeft * factor,
    outputPaddingRight = outputPaddingRight * factor,
    labelPaddingWidth = labelPaddingWidth * factor,
    labelPaddingHeight = labelPaddingHeight * factor,
    textBoxPaddingWidth = textBoxPaddingWidth * factor,
    textBoxPaddingHeight = textBoxPaddingHeight * factor,
    font = font.size((font.size match {
      case doodle.core.font.FontSize.Points(p) => p
    }) * factor)
  )
}
object Config {
  def default: Config = Config(
    stageLineStrokeDash = Array(4, 4),
    stageLineColor = Color.grey,
    stageColor = Color.black,
    stageHeight = 80,
    arrowBaseHalfWidth = 4,
    progressPaddingLeft = 8,
    outputPaddingRight = 24,
    labelColor = Color.black,
    labelPaddingWidth = 8,
    labelPaddingHeight = 8,
    textBoxPaddingWidth = 8,
    textBoxPaddingHeight = 8,
    evalColor = Color.blue,
    timeColor = Color.purple,
    errorColor = Color.red,
    progressColor = Color.black,
    pullColor = Color.black,
    outputColor = Color.rgb(6, 68, 88),
    doneColor = Color.green,
    startDotColor = Color.black,
    font = Font.defaultSansSerif.family(FontFamily.named("monospace")).size(12)
  )
}
