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

private def eventsToDiagram[F[_]: Foldable](
    events: F[(Event, Time)]
): Diagram = {

  case class PullCoord(progress: Int, from: Int, to: Int)
  type TokenMapEntry = (Unique.Token, PullCoord)
  type TokenMap = Map[Unique.Token, PullCoord]

  def op(
      acc: (Diagram, TokenMap),
      event: Event
  ): (Diagram, TokenMap) = {
    val (diagram, tokens) = acc

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
        Some(
          (
            e.token,
            PullCoord(progress = diagram.items.length, to = to, from = from)
          )
        )
      case _ => None
    }

    val item: PartialFunction[
      Event,
      Item
    ] = {
      case e: Event.Pull =>
        val to = labelIndex(e.to)
        val from = labelIndex(e.from)
        Item.Pull(from = from, to = to)
      case Event.Output(value, tok) =>
        val pullCoord = token(tok)
        Item.Output(
          value = value,
          from = pullCoord.to,
          to = pullCoord.from,
          pullProgress = pullCoord.progress
        )
      case e: Event.OutputChunk =>
        val pullCoord = token(e.token)
        Item.Output(
          value = e.value.toList.mkString("[", ",", "]"),
          from = pullCoord.to,
          to = pullCoord.from,
          pullProgress = pullCoord.progress
        )
      case Event.Done(tok) =>
        val pullCoord = token(tok)
        Item.Done(
          from = pullCoord.to,
          to = pullCoord.from,
          pullProgress = pullCoord.progress
        )
      case e: Event.Eval =>
        val at = labelIndex(e.at)
        Item.Eval(at = at, value = e.value)
      case e: Event.Error =>
        val pullCoord = token(e.token)
        Item.Error(
          from = pullCoord.to,
          to = pullCoord.from,
          value = e.value,
          pullProgress = pullCoord.progress
        )
      case Event.Finished(errored, value) =>
        Item.Finished(
          value = value,
          errored = errored
        )
    }
    val nextTokens = maybeToken(event).fold(tokens)(tokens + _)
    val items = item.lift(event).fold(diagram.items)(_ :: diagram.items)
    (diagram.copy(labels = labels, items = items), nextTokens)
  }

  def time(prev: Time, cur: Time): Option[Item] = {
    val diff = cur.seconds - prev.seconds
    // TODO: This should be its own item. Render with a different symbol.
    Option.when(diff > 0)(Item.Time(diff))
  }

  val empty =
    (
      Diagram(labels = Nil, items = Nil),
      Map.empty[Unique.Token, PullCoord],
      Option.empty[Time]
    )
  def foldOp(
      acc: (Diagram, TokenMap, Option[Time]),
      te: (Event, Time)
  ): (Diagram, TokenMap, Option[Time]) = {
    val (event, curTime) = te
    val (prevDiagram, tokenMap, prevTime) = acc
    val opAcc = (prevDiagram, tokenMap)
    val (diagram, tokens) = op(opAcc, event)
    val nextItems =
      prevTime.flatMap(time(_, curTime)).fold(diagram.items)(_ :: diagram.items)
    val nextDiagram = diagram.copy(items = nextItems)
    (nextDiagram, tokens, Some(curTime))
  }
  val (diagram, _, _) = events.foldLeft(empty)(foldOp)
  diagram.copy(items = diagram.items.reverse)
}
