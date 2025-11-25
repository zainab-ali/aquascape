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
import cats.data.Chain
import cats.data.NonEmptyChain

private def eventsToDiagram[F[_]: Foldable](
    events: F[(Event, Time)]
): Diagram = {

  case class PullCoord(progress: Int, from: Int, to: Int)
  type TokenMapEntry = (Token.Token, PullCoord)
  type TokenMap = Map[Token.Token, PullCoord]

  def op(labels: List[String])(
      acc: (Diagram, TokenMap),
      event: Event
  ): (Diagram, TokenMap) = {
    val (diagram, tokens) = acc

    def labelIndex(l: String): Int =
      val idx = labels.indexOf(l)
      if (idx == -1) {
        throw sys.error(s"Label is not present. Label $l in $labels")
      } else idx
    def token(t: Token.Token) =
      tokens.getOrElse(t, sys.error("Token is not present."))

    def maybeToken: Event => Option[TokenMapEntry] = {
      case e: Event.Pull =>
        val to = labelIndex(e.to._1)
        val from = labelIndex(e.from._1)
        Some(
          (
            e.token,
            PullCoord(progress = diagram.items.length, to = to, from = from)
          )
        )
      case _ => None
    }

    val item: Event => Item = {
      case e: Event.Pull =>
        val to = labelIndex(e.to._1)
        val from = labelIndex(e.from._1)
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
          value = e.value.mkString("[", ",", "]"),
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
        Item.Eval(value = e.value)
      case e: Event.Error =>
        val pullCoord = token(e.token)
        Item.Error(
          from = pullCoord.to,
          to = pullCoord.from,
          value = e.value,
          pullProgress = pullCoord.progress
        )
      case Event.Finished(at, errored, value) =>
        val atIndex = labelIndex(at._1)
        Item.Finished(
          value = value,
          errored = errored,
          at = atIndex
        )
    }
    val nextTokens = maybeToken(event).fold(tokens)(tokens + _)
    val items = item(event) :: diagram.items
    (diagram.copy(items = items), nextTokens)
  }

  def time(prev: Time, cur: Time): Option[Item] = {
    val diff = cur.seconds - prev.seconds
    // TODO: This should be its own item. Render with a different symbol.
    Option.when(diff > 0)(Item.Time(diff))
  }

  def foldOp(labels: List[String])(
      acc: (Diagram, TokenMap, Option[Time]),
      te: (Event, Time)
  ): (Diagram, TokenMap, Option[Time]) = {
    val (event, curTime) = te
    val (prevDiagram, tokenMap, prevTime) = acc
    val itemsWithTime =
      prevTime
        .flatMap(time(_, curTime))
        .fold(prevDiagram.items)(_ :: prevDiagram.items)
    val diagramWithTime = prevDiagram.copy(items = itemsWithTime)
    val opAcc = (diagramWithTime, tokenMap)
    val (nextDiagram, tokens) = op(labels)(opAcc, event)
    (nextDiagram, tokens, Some(curTime))
  }

  val labelPairs: List[(Label, Label)] = events.toList.mapFilter {
    case (Event.Pull(to, from, _), _) => Some((from, to))
    case _                            => None
  }
  val labels = sortLabels(labelPairs)
  val labelsSortedLN =
    labels.sortBy(a => a._2).foldLeft(List.empty[String])((a, b) => b._1 :: a)
  val empty =
    (
      Diagram(labels = labelsSortedLN, items = Nil),
      Map.empty[Token.Token, PullCoord],
      Option.empty[Time]
    )

  val (diagram, _, _) = events.foldLeft(empty)(foldOp(labelsSortedLN))
  diagram.copy(items = diagram.items.reverse)
}

import cats.syntax.all.*

/** Sort labels in a collection of pairs of `from` and `to` label pairs. Attempt
  * to position adjacent next to each other.
  */
private def sortLabels[F[_]: Foldable](
    pairs: F[(Label, Label)]
): List[Label] = {
  val groups: Chain[NonEmptyChain[Label]] =
    pairs.foldLeft(Chain.empty[NonEmptyChain[Label]]) {
      case (groups, (from, to)) =>
        if (groups.exists(_.contains(to))) {
          // The `to` label is present. Do not add it again.
          groups
        } else {

          groups.find(_.contains(from)) match {
            case None =>
              // Neither `from` nor `to` are present in the accumulated labels
              groups :+ NonEmptyChain(from, to)
            case Some(group) =>
              // `from` is present
              if (from === group.last) {
                // `from` is the last element. Add `to` at the end
                val nextGroup = group :+ to
                groups.map { g => if (group === g) nextGroup else g }
              } else {
                // `from` is somewhwere in the middle. Create a new group with `to`.
                groups :+ NonEmptyChain(to)
              }
          }
        }
    }
  groups.flatMap(_.toChain).toList
}
