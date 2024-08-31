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
private[drawing] def diagramToPicture(
    config: Config
)(diagram: Diagram): Picture[Unit] = {

  def picture(config: Config)(
      item: Item,
      progressOffset: Int,
      offsets: Map[Int, Int]
  ): Picture[(Picture[Unit], Int)] = {
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
        picture.width.map(w => (picture, w.toInt))
      case i: Item.Done =>
        val text = Picture
          // Draw text centered around (0.0, 0.0)
          .text("✔")
          .font(config.font)
        val length = i.from - i.to
        val picture =
          (text.width, text.height).flatMapN { case (width, height) =>
            // Draw an arrow upwards
            arrow(config)(length * config.stageHeight - 5)
              // Reflect in the X axis such that the arrow head is in negative Y
              .verticalReflection
              // Move the origin to just beyond the start of the arrow line.
              .originAt(0.0, -(height * 0.5))
              .beside(text)
              // Position the origin at the top left
              .originAt(
                -((width / 2.0)),
                height * 0.5
              )
              // Position the base of the arrow at the "from" stage
              .at(
                progressOffset + config.arrowBaseHalfWidth,
                i.from * config.stageHeight - 5
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
            w.toInt + config.outputPaddingRight
          )
        )
      case i: Item.Output =>
        val length = (i.from - i.to).abs
        val text = Picture
          // Draw text centered around (0.0, 0.0)
          .text(i.value)
          .font(config.font)
          .strokeColor(config.outputColor)
        val picture = (text.width, text.height).flatMapN {
          case (width, height) =>
            // Draw an arrow upwards
            arrow(config)(length * config.stageHeight - 5)
              // Reflect in the X axis such that the arrow head is in negative Y
              .verticalReflection
              // Move the origin to just beyond the start of the arrow line.
              .originAt(0.0, -(height * 0.5))
              // Position "beside" the text. The origin is at the centre X and 0Y.
              .beside(text)
              // Position the origin at the top left
              .originAt(
                -(config.arrowBaseHalfWidth + (width / 2.0)),
                (height * 0.5)
              )
              .at(progressOffset, i.from * config.stageHeight - 5)
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
            w.toInt + config.outputPaddingRight
          )
        )
      case i: Item.Eval =>
        val box = Picture
          .text(i.value)
          .font(config.font)
          .strokeColor(config.evalColor)
          .fillColor(config.evalColor)
        val picture = (box.width, box.height).flatMapN { (width, height) =>
          box
            .on(
              Picture
                .rectangle(
                  width = width + config.textBoxPaddingWidth,
                  height = height + config.textBoxPaddingHeight
                )
                .fillColor(Color.white)
            )
            .originAt(-width / 2.0, 0.0)
            .at(
              progressOffset + config.arrowBaseWidth + config.textBoxPaddingWidth,
              i.at * config.stageHeight
            )
            .strokeColor(config.evalColor)
        }
        picture.width.map(w => (picture, w.toInt))
      case i: Item.Time =>
        val box = Picture
          .text(s"${i.value}s")
          .font(config.font)
          .strokeColor(config.timeColor)
          .fillColor(config.timeColor)

        val picture = (box.width, box.height).flatMapN { (width, height) =>
          box
            .on(
              Picture
                .circle(
                  diameter = width + config.textBoxPaddingWidth
                )
                .strokeColor(config.timeColor)
                .fillColor(Color.white)
            )
            .originAt(-width / 2.0, 0.0)
            .at(
              progressOffset + config.arrowBaseWidth + config.textBoxPaddingWidth,
              0
            )
        }
        picture.width.map(w => (picture, w.toInt))
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
            w.toInt
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
          .fillColor(color)
        text.width.map { width =>
          val picture = innerCircle
            .on(outerCircle)
            .beside(text.originAt(-width / 2 - config.arrowBaseHalfWidth, 0))
            .originAt(-width / 2 - config.arrowBaseHalfWidth, 0)
            .at(progressOffset + config.arrowBaseHalfWidth, 0)
          (
            picture,
            (config.arrowBaseWidth + progressOffset + width.toInt)
          )
        }
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
        .fillColor(config.labelColor)
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

  foldLeftM(diagram.items.zipWithIndex.reverse)(
    (
      config.progressPaddingLeft,
      Map.empty[Int, Int],
      List.empty[Picture[Unit]]
    )
  )((b, item) =>
    (b, item) match {
      case ((progressOffset, offsets, pics), (i, idx)) =>
        picture(config)(i, progressOffset, offsets).map {
          case (pic, nextOffset) =>
            val nextOffsets = offsets + ((idx, progressOffset))
            (nextOffset, nextOffsets, pic :: pics)
        }
    }
  )
    .flatMap { case (progressOffset, _, pictures: List[Picture[Unit]]) =>
      ((pictures ++ List(
        dot
      ) ++ diagram.labels.zipWithIndex.map(
        label(config)(progressOffset)
      ))).reduce(_ on _)
    }
}
