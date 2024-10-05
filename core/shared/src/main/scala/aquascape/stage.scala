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

package aquascape

import cats.*
import cats.effect.*
import cats.syntax.all.*
import fs2.*

trait Scape[F[_]] {

  /** Record a stage of pulls and outputs surrounding this stream. */
  private[aquascape] def stage[O: Show](label: Label, branch: Branch)(
      s: Stream[F, O]
  ): Stream[F, O]

  /** Record an eval or error event after running this effect. */
  private[aquascape] def trace[O: Show](fo: F[O], branch: Branch): F[O]

  /** Connects a parent and child branch. */
  private[aquascape] def fork[O](parent: Branch, child: Branch)(
      s: Stream[F, O]
  ): Stream[F, O]

  /** Record a stage of the compile event surrounding this stream. */
  private[aquascape] def compileStage[O: Show](
      fo: F[O],
      label: Label,
      root: Branch
  ): F[O]

  /** Given a compiled stream `fo` which has been staged, output a stream of
    * events.
    */
  private[aquascape] def events[O](fo: F[O]): F[(Vector[(Event, Time)], O)]
}

object Scape {

  extension [F[_], A](fa: F[A]) {
    private[Scape] def lift: Pull[F, Nothing, A] = Pull.eval(fa)
  }

  def unchunked[F[_]: Async: NonEmptyParallel]: F[Scape[F]] =
    Pen[F, (Event, Time)].map { pen =>
      new {
        def stage[O: Show](label: Label, branch: Branch)(
            s: Stream[F, O]
        ): Stream[F, O] =
          stage_[F, O, O](
            _.uncons1,
            (o, tok) => Event.Output(o.show, tok),
            Pull.output1(_),
            pen
          )(label, branch)(s)

        def trace[O: Show](fo: F[O], branch: Branch): F[O] =
          trace_(pen, fo, branch)
        def fork[O](parent: Branch, child: Branch)(
            s: Stream[F, O]
        ): Stream[F, O] = fork_(pen)(parent, child)(s)
        def compileStage[O: Show](fo: F[O], label: Label, root: Branch): F[O] =
          compileStage_(pen, fo, label, root)

        def events[O](fo: F[O]): F[(Vector[(Event, Time)], O)] =
          events_(pen, fo)
      }
    }

  def chunked[F[_]: Async: NonEmptyParallel]: F[Scape[F]] =
    Pen[F, (Event, Time)].map { pen =>
      new {
        def stage[O: Show](label: Label, branch: Branch)(
            s: Stream[F, O]
        ): Stream[F, O] =
          stage_[F, O, Chunk[O]](
            _.uncons,
            (chk, tok) => Event.OutputChunk(chk.map(_.show).toList, tok),
            Pull.output(_),
            pen
          )(label, branch)(s)

        def trace[O: Show](fo: F[O], branch: Branch): F[O] =
          trace_(pen, fo, branch)
        def fork[O](parent: Branch, child: Branch)(
            s: Stream[F, O]
        ): Stream[F, O] = fork_(pen)(parent, child)(s)

        def compileStage[O: Show](fo: F[O], label: Label, root: Branch): F[O] =
          compileStage_(pen, fo, label, root)
        def events[O](fo: F[O]): F[(Vector[(Event, Time)], O)] =
          events_(pen, fo)
      }
    }

  private def time[F[_]: Temporal]: F[Time] =
    summon[Temporal[F]].realTime.map(t => Time(t.toSeconds.toInt))

  private def stage_[F[_]: Temporal: Unique, O, A](
      uncons: Stream.ToPull[F, O] => Pull[F, O, Option[(A, Stream[F, O])]],
      event: (A, Token.Token) => Event,
      output: A => Pull[F, O, Unit],
      pen: Pen[F, (Event, Time)]
  )(label: Label, branch: Branch)(s: Stream[F, O]): Stream[F, O] = {

    def go(in: Stream[F, O]): Pull[F, O, Unit] =
      pen
        .bracket(branch, label)(token =>
          time.lift.flatMap { t =>
            pen
              .writeWithLastTwo(
                branch,
                (to, from) =>
                  (
                    Event
                      .Pull(
                        to = to,
                        from = from,
                        token = token
                      ),
                    t
                  )
              )
              .lift >> uncons(in.pull)
              .flatTap {
                case Some((h, _)) =>
                  (time >>= (t => pen.write(branch, (event(h, token), t)))).lift
                case None =>
                  (time >>= (t =>
                    pen.write(branch, (Event.Done(token), t))
                  )).lift
              }
              .handleErrorWith(err =>
                time.lift >>= (t => onError(err, t, token, pen, branch))
              )
          }
        )
        .flatMap {
          case Some((h, t)) => output(h) >> go(t)
          case None         => Pull.done
        }

    Stream.bracket(
      time >>= (t => pen.write(branch, (Event.OpenScope(label), t)))
    )(_ =>
      time >>= (t => pen.write(branch: Branch, (Event.CloseScope(label), t)))
    ) >> go(s).stream

  }

  private[aquascape] final case class Caught(e: Throwable) extends Throwable
  private def onError[F[_]: Monad](
      e: Throwable,
      time: Time,
      token: Token.Token,
      pen: Pen[F, (Event, Time)],
      branch: Branch
  ): Pull[F, Nothing, Nothing] = {
    e match {
      case Caught(e) =>
        pen
          .write(
            branch,
            (Event.Error(e.getMessage, token, raisedHere = false), time)
          )
          .lift >> e.raiseError
      case _ =>
        pen
          .write(
            branch,
            (Event.Error(e.getMessage, token, raisedHere = true), time)
          )
          .lift >> Caught(
          e
        ).raiseError
    }
  }

  private def trace_[F[_]: MonadThrow: Temporal, O: Show](
      pen: Pen[F, (Event, Time)],
      fo: F[O],
      branch: Branch
  ): F[O] =
    fo.attempt.flatTap {
      case Right(o) =>
        time >>= (t => pen.write(branch, (Event.Eval(o.show), t)))
      case Left(err) =>
        time >>= (t => pen.write(branch, (Event.EvalError(err.getMessage), t)))
    }.rethrow

  private def fork_[F[_]: Monad, O, E](
      pen: Pen[F, E]
  )(parent: Branch, child: Branch)(
      s: Stream[F, O]
  ): Stream[F, O] =
    Stream.exec(pen.fork(parent, child)) ++ s

  private def compileStage_[F[_]: MonadCancelThrow: Temporal, O: Show](
      pen: Pen[F, (Event, Time)],
      fo: F[O],
      label: Label,
      root: Branch
  ): F[O] =
    pen.newRoot(root) >> pen.bracket(root, label)((fo.attempt, time).flatMapN {
      case (Right(o), t) =>
        pen.write(root, (Event.Finished(label, false, o.show), t)).as(o)
      case (Left(Caught(err)), t) =>
        pen.write(
          root,
          (
            Event.Finished(label, true, Option(err.getMessage).getOrElse("!")),
            t
          )
        ) >> err.raiseError
      case (Left(err), t) =>
        pen.write(
          root,
          (
            Event.Finished(label, true, Option(err.getMessage).getOrElse("!")),
            t
          )
        ) >> err.raiseError
    })

  private def events_[F[_]: Concurrent: NonEmptyParallel, O](
      pen: Pen[F, (Event, Time)],
      fo: F[O]
  ): F[(Vector[(Event, Time)], O)] =
    (pen.events.compile.toVector, fo.flatTap(_ => pen.close)).parTupled

}
