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
import cats.Foldable
import cats.effect.Unique
import cats.syntax.all.*

def eventsToDiagram[F[_]: Foldable](events: F[Event]): Diagram = {

  case class PullCoord(progress: Int, from: Int, to: Int)
  type TokenMapEntry = (Unique.Token, PullCoord)
  type TokenMap = Map[Unique.Token, PullCoord]

  def op(
      acc: (Diagram, Progress, TokenMap),
      event: Event
  ): (Diagram, TokenMap) = {
    val (diagram, prevProgress, tokens) = acc

    val labels: List[Label] = {
      val newLabels: List[Label] = event match {
        case e: Event.Pull => List(e.from, e.to)
        case e: Event.Eval => List(e.at)
        case _             => Nil
      }
      newLabels
        .filterNot(diagram.labels.contains)
        .foldLeft(diagram.labels)((ls, l) => ls :+ l)
    }
    def labelIndex(l: String): Int =
      val idx = labels.indexOf(l)
      if (idx == -1) {
        throw sys.error(s"Label is not present. Label $l in $labels")
      } else idx
    def token(t: Unique.Token) =
      tokens.getOrElse(t, sys.error("Token is not present."))

    def maybeToken: Event => Option[TokenMapEntry] = {
      case e: Event.Pull =>
        val to = labelIndex(e.to)
        val from = labelIndex(e.from)
        val (progress) = prevProgress + 1
        Some((e.token, PullCoord(progress = progress, to = to, from = from)))
      case _ => None
    }
    val item: PartialFunction[
      Event,
      Item
    ] = {
      case e: Event.Pull =>
        val to = labelIndex(e.to)
        val from = labelIndex(e.from)
        val (progress) = prevProgress + 1
        Item.Pull(from = from, to = to, progress = progress)
      case Event.Output(value, tok) =>
        val (progress) = prevProgress + 1
        val pullCoord = token(tok)
        Item.Output(
          value = value,
          from = pullCoord.to,
          to = pullCoord.from,
          progress = progress,
          pullProgress = pullCoord.progress
        )
      case e: Event.OutputChunk =>
        val pullCoord = token(e.token)
        Item.Output(
          value = e.value.toList.mkString("[", ",", "]"),
          from = pullCoord.to,
          to = pullCoord.from,
          progress = prevProgress + 1,
          pullProgress = pullCoord.progress
        )
      case Event.Done(tok) =>
        val pullCoord = token(tok)
        Item.Done(
          from = pullCoord.to,
          to = pullCoord.from,
          progress = prevProgress + 1,
          pullProgress = pullCoord.progress
        )
      case e: Event.Eval =>
        val at = labelIndex(e.at)
        Item.Eval(at = at, value = e.value, progress = prevProgress + 1)
      case e: Event.Error =>
        val pullCoord = token(e.token)
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
    val nextTokens = maybeToken(event).fold(tokens)(tokens + _)
    val items = item.lift(event) match {
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
      event: Event
  ): (Diagram, Progress, TokenMap) = {
    val (_, prevProgress, _) = acc
    val (diagram, tokens) = op(acc, event)
    (diagram, prevProgress + 1, tokens)
  }
  val (diagram, _, _) = events.foldLeft(empty)(foldOp)
  diagram.copy(items = diagram.items.reverse)
}
