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

trait Trace[F[_]] {

  /** Record a trace of pulls and outputs surrounding this stream. */
  def trace[O: Show](label: Label, branch: Branch)(
      s: Stream[F, O]
  ): Stream[F, O]

  /** Record an eval or error event after running this effect. */
  def trace[O: Show](fo: F[O], branch: Branch): F[O]

  /** Connects a parent and child branch. */
  def fork[O](parent: Branch, child: Branch)(s: Stream[F, O]): Stream[F, O]

  /** Record a trace of the compile event surrounding this stream. */
  def traceCompile[O: Show](fo: F[O], label: Label): F[Unit]

  /** Given a compiled stream `fo` which has been traced, output a stream of
    * events.
    */
  def events[O](fo: F[O]): Stream[F, (Event, Time)]
}

object Trace {

  extension [F[_], A](fa: F[A]) {
    def lift: Pull[F, Nothing, A] = Pull.eval(fa)
  }

  def unchunked[F[_]: Async]: F[Trace[F]] = Pen[F, (Event, Time)].map { pen =>
    new {
      def trace[O: Show](label: Label, branch: Branch)(
          s: Stream[F, O]
      ): Stream[F, O] =
        trace_[F, O, O](
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
      def traceCompile[O: Show](fo: F[O], branch: Branch): F[Unit] =
        traceCompile_(pen, fo, branch)
      def events[O](fo: F[O]): Stream[F, (Event, Time)] = events_(pen, fo)
    }
  }

  def chunked[F[_]: Async]: F[Trace[F]] = Pen[F, (Event, Time)].map { pen =>
    new {
      def trace[O: Show](label: Label, branch: Branch)(
          s: Stream[F, O]
      ): Stream[F, O] =
        trace_[F, O, Chunk[O]](
          _.uncons,
          (chk, tok) => Event.OutputChunk(chk.map(_.show), tok),
          Pull.output(_),
          pen
        )(label, branch)(s)

      def trace[O: Show](fo: F[O], branch: Branch): F[O] =
        trace_(pen, fo, branch)
      def fork[O](parent: Branch, child: Branch)(
          s: Stream[F, O]
      ): Stream[F, O] = fork_(pen)(parent, child)(s)

      def traceCompile[O: Show](fo: F[O], branch: Branch): F[Unit] =
        traceCompile_(pen, fo, branch)
      def events[O](fo: F[O]): Stream[F, (Event, Time)] = events_(pen, fo)
    }
  }

  private def time[F[_]: Temporal]: F[Time] =
    summon[Temporal[F]].realTime.map(t => Time(t.toSeconds.toInt))

  private def trace_[F[_]: Temporal: Unique, O, A](
      uncons: Stream.ToPull[F, O] => Pull[F, O, Option[(A, Stream[F, O])]],
      event: (A, Unique.Token) => Event,
      output: A => Pull[F, O, Unit],
      pen: Pen[F, (Event, Time)]
  )(label: Label, branch: Branch)(s: Stream[F, O]): Stream[F, O] = {

    def go(in: Stream[F, O]): Pull[F, O, Unit] =
      pen
        .bracket(branch, label)(
          (Unique[F].unique.lift, time[F].lift)
            .flatMapN { (token, t) =>
              pen
                .writeWithLastTwo(
                  branch,
                  (to, from) =>
                    (
                      Event
                        .Pull(
                          to = to,
                          from = from,
                          token
                        ),
                      t
                    )
                )
                .lift >> uncons(in.pull)
                .flatTap {
                  case Some((h, _)) =>
                    (time >>= (t =>
                      pen.write(branch, (event(h, token), t))
                    )).lift
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
      token: Unique.Token,
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
        time >>= (t =>
          pen.writeWithLast(branch, label => (Event.Eval(label, o.show), t))
        )
      case Left(err) =>
        time >>= (t => pen.write(branch, (Event.EvalError(err.getMessage), t)))
    }.rethrow

  private def fork_[F[_]: Monad, O, E](
      pen: Pen[F, E]
  )(parent: Branch, child: Branch)(
      s: Stream[F, O]
  ): Stream[F, O] =
    Stream.exec(pen.fork(parent, child)) ++ s

  private def traceCompile_[F[_]: MonadCancelThrow: Temporal, O: Show](
      pen: Pen[F, (Event, Time)],
      fo: F[O],
      label: Label
  ): F[Unit] =
    pen.bracket(root, label)((fo.attempt, time).flatMapN {
      case (Right(o), t) => pen.write(root, (Event.Finished(false, o.show), t))
      case (Left(Caught(err)), t) =>
        pen.write(
          root,
          (Event.Finished(true, Option(err.getMessage).getOrElse("!")), t)
        )
      case (Left(err), t) =>
        pen.write(
          root,
          (Event.Finished(true, Option(err.getMessage).getOrElse("!")), t)
        )
    })

  private def events_[F[_]: Concurrent, O](
      pen: Pen[F, (Event, Time)],
      fo: F[O]
  ): Stream[F, (Event, Time)] =
    pen.events
      .concurrently(
        Stream.exec(fo >> pen.close)
      )

}
