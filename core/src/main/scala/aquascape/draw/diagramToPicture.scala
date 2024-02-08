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

import cats.*
import cats.syntax.all.*
import doodle.algebra.*
import doodle.core.*
import doodle.syntax.all.*

object Picture
    extends BaseConstructor
    with ShapeConstructor
    with TextConstructor {
  type Algebra = Path & Layout & Size & Style & Text &
    doodle.algebra.Transform & Shape & Debug
}
type Picture[A] = doodle.algebra.Picture[Picture.Algebra, A]

// TODO: Raise a bug in doodle for tailRecM
private def foldLeftM[A, B, F[_]: Monad](
    as: List[A]
)(b: B)(f: (B, A) => F[B]): F[B] =
  as match {
    case h :: t => foldLeftM(t)(b)(f).flatMap(f(_, h))
    case Nil    => b.pure[F]
  }
def diagramToPicture(config: Config)(diagram: Diagram): Picture[Unit] = {

  def picture(config: Config)(
      item: Item,
      progressOffset: Int,
      currentMaxWidth: Int,
      offsets: Map[Int, Int]
  ): Picture[(Picture[Unit], Int, Int, Map[Int, Int])] = {
    // Draw an arrow in the Y direction, with the origin at the base of the arrow
    def arrow(config: Config)(length: Int): Picture[Unit] = {
      val l = length.abs
      val arrowheadHalfHeight = config.arrowBaseHalfWidth
      val arrowheadHeight = arrowheadHalfHeight * 2
      val arrowHead = Picture
        .triangle(config.arrowBaseHalfWidth * 2, arrowheadHeight)
        .originAt(0.0, arrowheadHalfHeight)
        .at(0.0, l)
      OpenPath.empty.lineTo(0, l).path.on(arrowHead)
    }

    item match {
      case i: Item.Pull =>
        // Draw an arrow upwards
        val picture =
          arrow(config)((i.from - i.to).abs * config.stageHeight - 5)
            // Position the base of the arrow
            .at(
              progressOffset + config.arrowBaseHalfWidth,
              i.from * config.stageHeight
            )
            .strokeColor(config.pullColor)
            .fillColor(config.pullColor)
        picture.width.map(w =>
          (picture, progressOffset, w.toInt.max(currentMaxWidth), offsets)
        )
      case i: Item.Done =>
        val text = Picture
          // Draw text centered around (0.0, 0.0)
          .text("✔")
          .font(config.font)
        val length = i.from - i.to
        val picture =
          text.width.flatMap { width =>
            // Draw an arrow upwards
            arrow(config)(length * config.stageHeight - 5)
              // Reflect in the X axis such that the arrow head is in negative Y
              .verticalReflection
              // Move the origin to the centre of the arrow line.
              .originAt(0.0, -(length * config.stageHeight - 5).abs * 0.5)
              .beside(text)
              // Position the origin at the top
              .originAt(
                -((width / 2.0)),
                (length * config.stageHeight + 5).abs * 0.5
              )
              // Position the base of the arrow at the "from" stage
              .at(
                progressOffset + config.arrowBaseHalfWidth,
                i.from * config.stageHeight
              )
              .strokeColor(config.doneColor)
              .fillColor(config.doneColor)
          }
        val pullOffset = offsets(i.pullProgress)
        val pullToOutput = OpenPath.empty
          .moveTo(
            pullOffset + config.arrowBaseHalfWidth,
            i.from * config.stageHeight - 5
          )
          .lineTo(
            progressOffset + config.arrowBaseHalfWidth,
            i.from * config.stageHeight - 5
          )
          .path
        picture.width.map(w =>
          (
            picture.on(pullToOutput),
            progressOffset,
            w.toInt.max(currentMaxWidth),
            offsets
          )
        )
      case i: Item.Output =>
        val length = (i.from - i.to).abs
        val text = Picture
          // Draw text centered around (0.0, 0.0)
          .text(i.value)
          .font(config.font)
          .strokeColor(config.outputColor)
        val picture = text.width.flatMap { width =>
          // Draw an arrow upwards
          arrow(config)(length * config.stageHeight - 5)
            // Reflect in the X axis such that the arrow head is in negative Y
            .verticalReflection
            // Move the origin to the centre of the arrow line.
            .originAt(0.0, -(length * config.stageHeight - 5).abs * 0.5)
            // Position "beside" the text. The origin is at the centre X and 0Y.
            .beside(text)
            // Position the origin at the top left
            .originAt(
              -(config.arrowBaseHalfWidth + (width / 2.0)),
              (length * config.stageHeight + 5).abs * 0.5
            )
            .at(progressOffset, i.from * config.stageHeight)
            .strokeColor(config.outputColor)
            .fillColor(config.outputColor)
        }
        val pullOffset = offsets(i.pullProgress)
        val pullToOutput = OpenPath.empty
          .moveTo(
            pullOffset + config.arrowBaseHalfWidth,
            i.from * config.stageHeight - 5
          )
          .lineTo(
            progressOffset + config.arrowBaseHalfWidth,
            i.from * config.stageHeight - 5
          )
          .path
        picture.width.map(w =>
          (
            picture.on(pullToOutput),
            progressOffset,
            w.toInt.max(currentMaxWidth),
            offsets
          )
        )
      case i: Item.Eval =>
        val box = Picture.text(i.value).font(config.font)
        val picture = (box.width, box.height).flatMapN { (width, height) =>
          box
            .on(
              Picture.rectangle(
                width = width + config.textBoxPaddingWidth,
                height = height + config.textBoxPaddingHeight
              )
            )
            .originAt(-width / 2.0, 0.0)
            .at(
              progressOffset + config.arrowBaseWidth + config.textBoxPaddingWidth,
              i.at * config.stageHeight
            )
            .font(config.font)
            .strokeColor(config.evalColor)
            .fillColor(Color.white)
        }
        picture.width.map(w =>
          (picture, progressOffset, w.toInt.max(currentMaxWidth), offsets)
        )
      case i: Item.Error =>
        val length = (i.from - i.to).abs
        val text = Picture
          // Draw text centered around (0.0, 0.0)
          .text(i.value)
          .font(config.font)
          .strokeColor(config.errorColor)
        val picture = text.width.flatMap { width =>
          // Draw an arrow upwards
          arrow(config)(length * config.stageHeight - 5)
            // Reflect in the X axis such that the arrow head is in negative Y
            .verticalReflection
            // Move the origin to the centre of the arrow line.
            .originAt(0.0, -(length * config.stageHeight - 5).abs * 0.5)
            // Position "beside" the text. The origin is at the centre X and 0Y.
            .beside(text)
            // Position the origin at the top left
            .originAt(
              -(config.arrowBaseHalfWidth + (width / 2.0)),
              (length * config.stageHeight + 5).abs * 0.5
            )
            .at(progressOffset, i.from * config.stageHeight)
            .strokeColor(config.errorColor)
            .fillColor(config.errorColor)
        }
        val pullOffset = offsets(i.pullProgress)
        val pullToOutput = OpenPath.empty
          .moveTo(
            pullOffset + config.arrowBaseHalfWidth,
            i.from * config.stageHeight - 5
          )
          .lineTo(
            progressOffset + config.arrowBaseHalfWidth,
            i.from * config.stageHeight - 5
          )
          .path
        picture.width.map(w =>
          (
            picture.on(pullToOutput),
            progressOffset,
            w.toInt.max(currentMaxWidth),
            offsets
          )
        )
      // case i: Item.Error =>
      //   // TODO: Should we draw a line to the next item after the error is thrown?
      //   val box = Picture.text(i.value).font(config.font)
      //   val picture = (box.width, box.height).flatMapN { (width, height) =>
      //     box
      //       .on(
      //         Picture.rectangle(
      //           width = width + config.textBoxPaddingWidth,
      //           height = height + config.textBoxPaddingHeight
      //         )
      //       )
      //       .originAt(-width / 2.0, 0.0)
      //       .at(
      //         progressOffset + config.arrowBaseWidth + config.textBoxPaddingWidth,
      //         i.at * config.stageHeight
      //       )
      //       .font(config.font)
      //       .strokeColor(config.errorColor)
      //       .fillColor(Color.white)
      //   }
      //   picture.width.map(w =>
      //     (picture, progressOffset, w.toInt.max(currentMaxWidth), offsets)
      //   )
      case i: Item.Finished =>
        val color = if (i.errored) config.errorColor else config.startDotColor
        val innerCircle = Picture
          .circle(config.arrowBaseWidth * 0.6)
          .strokeColor(color)
          .fillColor(color)
        val outerCircle =
          Picture
            .circle(config.arrowBaseWidth)
            .strokeColor(color)
            .fillColor(Color.white)
        val text = Picture
          .text(i.value)
          .font(config.font)
          .strokeColor(color)
        text.width.map { width =>
          val picture = innerCircle
            .on(outerCircle)
            .beside(text.originAt(-width / 2 - config.arrowBaseHalfWidth, 0))
            .originAt(-width / 2 - config.arrowBaseHalfWidth, 0)
            .at(progressOffset + config.arrowBaseHalfWidth, 0)
          (
            picture,
            progressOffset,
            (config.arrowBaseWidth + progressOffset + width.toInt)
              .max(currentMaxWidth),
            offsets
          )
        }
      case i: Item.IncProgress =>
        val width =
          (currentMaxWidth - progressOffset + config.progressPaddingLeft).max(
            config.minProgressWidth
          )
        val nextOffsets = offsets + ((i.progress + 1, progressOffset + width))
        (Picture.empty, progressOffset + width, 0, nextOffsets).pure
    }
  }

  // TODO: Draw different lines when a stage is done. E.g on "repeat"
  def stageLine(diagramWidth: Int, index: Int): Picture[Unit] = {
    OpenPath.empty
      .lineTo(diagramWidth, 0)
      .path
      .strokeColor(config.stageLineColor)
      .strokeDash(config.stageLineStrokeDash.map(_.toDouble))
      .at(0, index * config.stageHeight)
  }

  val dot = Picture
    .circle(config.arrowBaseWidth)
    .strokeColor(config.startDotColor)
    .fillColor(config.startDotColor)
    .at(
      config.progressPaddingLeft + config.arrowBaseHalfWidth,
      0
    )
  def label(config: Config)(
      diagramWidth: Int
  )(text: String, index: Int): Picture[Unit] = {
    def labelWidth(config: Config, text: String): Picture[(Int, Int)] = {
      val box = Picture
        .text(text)
        .font(config.font)
      (box.width.map(_.toInt), box.height.map(_.toInt)).tupled
    }

    labelWidth(config, text).flatMap { (width, height) =>
      val labelBoxWidth = width + config.labelPaddingWidth
      Picture
        .text(text)
        .on(
          Picture.rectangle(
            width = labelBoxWidth,
            height = height + config.labelPaddingHeight
          )
        )
        .at(-labelBoxWidth / 2, index * config.stageHeight)
        .font(config.font)
        .strokeColor(config.stageColor)
        .on(stageLine(diagramWidth, index))
    }
  }

  foldLeftM(diagram.items.reverse)(
    (
      config.progressPaddingLeft,
      0,
      Map.empty[Int, Int],
      List.empty[Picture[Unit]]
    )
  )((b, item) =>
    b match {
      case (progressOffset, maxWidth, offsets, pics) =>
        picture(config)(item, progressOffset, maxWidth, offsets).map {
          case (pic, nextOffset, nextWidth, nextOffsets) =>
            (nextOffset, nextWidth, nextOffsets, pic :: pics)
        }
    }
  )
    .flatMap {
      case (progressOffset, maxWidth, _, pictures: List[Picture[Unit]]) =>
        ((pictures ++ List(
          dot
        ) ++ diagram.labels.zipWithIndex.map(
          label(config)(maxWidth)
        ))).reduce(_ on _)
    }
}
