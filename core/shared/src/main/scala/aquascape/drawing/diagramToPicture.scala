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

object DiagramToPicture {
  // Draw an arrow in the Y direction, with the origin at the base of the arrow
  private def arrow(config: Config)(length: Int): Picture[Unit] = {
    val l = length.abs
    val arrowheadHalfHeight = config.arrowBaseHalfWidth
    val arrowheadHeight = arrowheadHalfHeight * 2
    val arrowHead = Picture
      .triangle(config.arrowBaseHalfWidth * 2, arrowheadHeight)
      .originAt(0.0, arrowheadHalfHeight)
      .at(0.0, l)
    OpenPath.empty.lineTo(0, l).path.on(arrowHead)
  }

  private[drawing] def start(config: Config): Picture[Unit] = Picture
    .circle(config.arrowBaseWidth)
    .strokeColor(config.startDotColor)
    .fillColor(config.startDotColor)
    .at(
      config.progressPaddingLeft + config.arrowBaseHalfWidth,
      0
    )

  private[drawing] def pull(
      config: Config,
      progressOffset: Int,
      i: Item.Pull
  ): Picture[Unit] = {
    arrow(config)((i.from - i.to).abs * config.stageHeight - 5)
      // Position the base of the arrow
      .at(
        progressOffset + config.arrowBaseHalfWidth,
        i.from * config.stageHeight
      )
      .strokeColor(config.pullColor)
      .fillColor(config.pullColor)
  }

  private[drawing] def done(
      config: Config,
      progressOffset: Int,
      i: Item.Done
  ): Picture[Unit] = {
    val text = Picture
      // Draw text centered around (0.0, 0.0)
      .text("✔")
      .font(config.font)
    val length = i.from - i.to
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
  }

  private[drawing] def pullToOutput(
      config: Config,
      progressOffset: Int,
      offsets: Map[Int, Int],
      from: Int,
      pullProgress: Int
  ): Picture[Unit] = {
    val pullOffset = offsets(pullProgress)
    OpenPath.empty
      .moveTo(
        pullOffset + config.arrowBaseHalfWidth,
        from * config.stageHeight - 5
      )
      .lineTo(
        progressOffset + config.arrowBaseHalfWidth,
        from * config.stageHeight - 5
      )
      .path
  }

  private[drawing] def output(
      config: Config,
      progressOffset: Int,
      i: Item.Output
  ): Picture[Unit] = {
    val length = (i.from - i.to).abs
    val text = Picture
      // Draw text centered around (0.0, 0.0)
      .text(i.value)
      .font(config.font)
      .strokeColor(config.outputColor)
    text.width.flatMap { width =>
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
  }

  private[drawing] def eval(
      config: Config,
      progressOffset: Int,
      totalNumStages: Int,
      i: Item.Eval
  ): Picture[Unit] = {
    val box = Picture
      .text(i.value)
      .font(config.font)
      .strokeColor(config.evalColor)
      .fillColor(config.evalColor)
    (box.width, box.height).flatMapN { (width, height) =>
      val effectBox = box
        .on(
          Picture
            .rectangle(
              width = width + config.textBoxPaddingWidth,
              height = height + config.textBoxPaddingHeight
            )
            .fillColor(Color.white)
        )
        .strokeColor(config.evalColor)
        .originAt(-width / 2.0, 0.0)
      val margin = Picture
        .circle(
          diameter = width + 4 * config.textBoxPaddingWidth
        )
        .noStroke
        .noFill
      effectGridLine(config, totalNumStages)
        .beside(effectBox)
        .beside(effectGridLine(config, totalNumStages))
        .on(margin)
        .at(
          progressOffset + config.arrowBaseWidth + config.textBoxPaddingWidth,
          -1 * config.stageHeight
        )
    }
  }
  private[drawing] def error(
      config: Config,
      progressOffset: Int,
      i: Item.Error
  ): Picture[Unit] = {
    val length = (i.from - i.to).abs
    val text = Picture
      // Draw text centered around (0.0, 0.0)
      .text(i.value)
      .font(config.font)
      .strokeColor(config.errorColor)
    text.width.flatMap { width =>
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
  }

  private[drawing] def time(
      config: Config,
      progressOffset: Int,
      totalNumStages: Int,
      i: Item.Time
  ): Picture[Unit] = {
    val box = Picture
      .text(s"${i.value}s")
      .font(config.font)
      .strokeColor(config.timeColor)
      .fillColor(config.timeColor)
    (box.width, box.height).flatMapN { (width, height) =>
      val circle = box.on(
        Picture
          .circle(
            diameter = width + 2 * config.textBoxPaddingWidth
          )
          .strokeColor(config.timeColor)
          .fillColor(Color.white)
      )
      val margin = Picture
        .circle(
          diameter = width + 4 * config.textBoxPaddingWidth
        )
        .noStroke
        .noFill
      effectGridLine(config, totalNumStages)
        .beside(circle)
        .beside(effectGridLine(config, totalNumStages))
        .on(margin)
        .originAt(-(width + 4 * config.textBoxPaddingWidth) / 2.0, 0.0)
        .at(
          progressOffset,
          -1 * config.stageHeight
        )
    }
  }

  private def effectGridLine(
      config: Config,
      totalNumStages: Int
  ): Picture[Unit] = {
    // val line =
    OpenPath.empty
      .lineTo(0, (1 + totalNumStages) * config.stageHeight)
      .path
      .strokeColor(config.gridLineColor)
      .strokeDash(config.gridLineStrokeDash.map(_.toDouble))
  }

  private[drawing] def finished(
      config: Config,
      progressOffset: Int,
      i: Item.Finished
  ): Picture[Unit] = {
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
    text.width.flatMap { width =>
      innerCircle
        .on(outerCircle)
        .beside(text.originAt(-width / 2 - config.arrowBaseHalfWidth, 0))
        .originAt(-width / 2 - config.arrowBaseHalfWidth, 0)
        .at(
          progressOffset + config.arrowBaseHalfWidth,
          i.at * config.stageHeight
        )
    }
  }

  // TODO: Draw different lines when a stage is done. E.g on "repeat"
  private def stageLine(
      config: Config,
      diagramWidth: Int,
      index: Int
  ): Picture[Unit] = {
    OpenPath.empty
      .lineTo(diagramWidth, 0)
      .path
      .strokeColor(config.gridLineColor)
      .strokeDash(config.gridLineStrokeDash.map(_.toDouble))
      .at(0, index * config.stageHeight)
  }

  private[drawing] def label(
      config: Config,
      diagramWidth: Int
  )(text: String, index: Int): Picture[Unit] = {
    def labelDimensions(config: Config, text: String): Picture[(Int, Int)] = {
      val box = Picture
        .text(text)
        .font(config.font)
      (box.width.map(_.toInt), box.height.map(_.toInt)).tupled
    }

    labelDimensions(config, text).flatMap { (width, height) =>
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
        .on(stageLine(config, diagramWidth, index))
    }
  }
}
private[drawing] def diagramToPicture(
    config: Config
)(diagram: Diagram): Picture[Unit] = {
  val totalNumStages: Int = diagram.items.collect { case i: Item.Pull =>
    i.to
  }.max

  def picture(
      item: Item,
      progressOffset: Int,
      offsets: Map[Int, Int]
  ): Picture[Unit] = {
    item match {
      case i: Item.Pull =>
        // Draw an arrow upwards
        DiagramToPicture.pull(config, progressOffset, i)
      case i: Item.Done =>
        val done = DiagramToPicture.done(config, progressOffset, i)
        val pullToOutput = DiagramToPicture.pullToOutput(
          config,
          progressOffset,
          offsets,
          i.from,
          i.pullProgress
        )
        done.on(pullToOutput)
      case i: Item.Output =>
        val output = DiagramToPicture.output(config, progressOffset, i)
        val pullToOutput = DiagramToPicture.pullToOutput(
          config,
          progressOffset,
          offsets,
          i.from,
          i.pullProgress
        )
        output.on(pullToOutput)
      case i: Item.Eval =>
        DiagramToPicture.eval(config, progressOffset, totalNumStages, i)
      case i: Item.Time =>
        DiagramToPicture.time(config, progressOffset, totalNumStages, i)
      case i: Item.Error =>
        val error = DiagramToPicture.error(config, progressOffset, i)
        val pullToOutput = DiagramToPicture.pullToOutput(
          config,
          progressOffset,
          offsets,
          i.from,
          i.pullProgress
        )
        error.on(pullToOutput)
      case i: Item.Finished =>
        DiagramToPicture.finished(config, progressOffset, i)
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
        val pic = picture(i, progressOffset, offsets)
        pic.width.map { nextOffset =>
          val nextOffsets = offsets + ((idx, progressOffset))
          (nextOffset.toInt, nextOffsets, pic :: pics)
        }
    }
  )
    .flatMap { case (progressOffset, offsets, pictures: List[Picture[Unit]]) =>
      (pictures ++ List(
        DiagramToPicture.start(config)
      ) ++ diagram.labels.zipWithIndex.map(
        DiagramToPicture.label(config, progressOffset)
      )).reduce(_ on _)
    }
}
