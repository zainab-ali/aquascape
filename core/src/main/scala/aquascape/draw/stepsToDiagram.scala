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

import aquascape.*
import cats.effect.Unique
import cats.syntax.all.*

def stepsToDiagram(steps: List[Step]): Diagram = {

  case class PullCoord(progress: Int, from: Int, to: Int)
  type TokenMapEntry = (Unique.Token, PullCoord)
  type TokenMap = Map[Unique.Token, PullCoord]

  def op(
      acc: (Diagram, Progress, TokenMap),
      step: Step
  ): (Diagram, TokenMap) = {
    val (diagram, prevProgress, tokens) = acc

    def labelIndices(
        stepLabels: List[Label],
        diagramLabels: List[Label]
    ): (Int, Int) = {
      val head: Int = diagramLabels.indexOf(stepLabels.head)
      val tail: Int =
        stepLabels.tail.headOption.fold(rootIndex)(diagramLabels.indexOf)
      (head, tail)
    }
    val labels = step.labels.headOption
      .filterNot(diagram.labels.contains)
      .fold(diagram.labels)(diagram.labels :+ _)
    def maybeToken: Event => Option[TokenMapEntry] = {
      case Event.Pull(tok) =>
        val (to, from) = labelIndices(step.labels, labels)
        val (progress) = prevProgress + 1
        Some((tok, PullCoord(progress = progress, to = to, from = from)))
      case _ => None
    }
    val item: PartialFunction[
      Event,
      Item
    ] = {
      case Event.Pull(tok) =>
        // Draw a line from this "to" to the next from that has a token.
        val (to, from) = labelIndices(step.labels, labels)
        val (progress) = prevProgress + 1
        Item.Pull(from = from, to = to, progress = progress)
      case Event.Output(value, tok) =>
        val (progress) =
          prevProgress + 1
        val pullCoord =
          tokens.get(tok).getOrElse(sys.error("Token not found."))
        Item.Output(
          value = value,
          from = pullCoord.to,
          to = pullCoord.from,
          progress = progress,
          pullProgress = pullCoord.progress
        )
      case e: Event.OutputChunk =>
        val pullCoord =
          tokens.get(e.token).getOrElse(sys.error("Token not found."))
        Item.Output(
          value = e.value.toList.mkString("[", ",", "]"),
          from = pullCoord.to,
          to = pullCoord.from,
          progress = prevProgress + 1,
          pullProgress = pullCoord.progress
        )
      case Event.Done(tok) =>
        val pullCoord =
          tokens.get(tok).getOrElse(sys.error("Token not found."))
        Item.Done(
          from = pullCoord.to,
          to = pullCoord.from,
          progress = prevProgress + 1,
          pullProgress = pullCoord.progress
        )
      case e: Event.Eval =>
        // In the case of an error, there may be no steps listed.
        val at = step.labels.headOption.map(labels.indexOf).getOrElse(-1)
        Item.Eval(at = at, value = e.value, progress = prevProgress + 1)
      case e: Event.Error =>
        val pullCoord =
          tokens.get(e.token).getOrElse(sys.error("Token not found."))
        Item.Error(
          from = pullCoord.to,
          to = pullCoord.from,
          value = e.value,
          progress = prevProgress + 1,
          pullProgress = pullCoord.progress
        )
      case Event.Finished(errored, value) =>
        Item.Finished(
          value = value,
          errored = errored,
          progress = prevProgress + 1
        )
    }
    val nextTokens = maybeToken(step.e).fold(tokens)(tokens + _)
    val items = item.lift(step.e) match {
      case None => diagram.items
      case Some(i) => (
        i :: Item.IncProgress(prevProgress) :: diagram.items
      )
    }
    (diagram.copy(labels = labels, items = items), nextTokens)
  }

  val empty =
    (Diagram(labels = Nil, items = Nil), 0, Map.empty[Unique.Token, PullCoord])
  def foldOp(
      acc: (Diagram, Progress, TokenMap),
      step: Step
  ): (Diagram, Progress, TokenMap) = {
    val (_, prevProgress, _) = acc
    val (diagram, tokens) = op(acc, step)
    (diagram, prevProgress + 1, tokens)
  }
  val (diagram, _, _) = steps.foldLeft(empty)(foldOp)
  diagram.copy(items = diagram.items.reverse)
}
