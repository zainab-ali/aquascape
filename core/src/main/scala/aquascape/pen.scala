package aquascape

import cats.*
import cats.effect.Ref
import cats.syntax.all.*
import fs2.*
import fs2.concurrent.Channel

trait Pen[F[_], E] {
  def bracket[A](branch: Branch, child: Label)(fa: F[A]): F[A]
  def bracket[O, A](branch: Branch, child: Label)(
      fa: Pull[F, O, A]
  ): Pull[F, O, A]
  def writeWith(branch: Branch, f: List[Label] => E): F[Unit]
  def write(branch: Branch, e: E): F[Unit] = writeWith(branch, _ => e)
  def fork(parent: Branch, child: Branch): F[Unit]
  def events: Stream[F, E]
  def close: F[Unit]
}
import cats.effect.*

val root = "root"

object Pen {
  def apply[F[_]: Async, E]: F[Pen[F, E]] =
    (
      Ref.of[F, Map[Branch, (List[Label])]](Map(root -> (Nil))),
      Channel.synchronous[F, E]
    ).mapN { case (stack, chan) =>
      new {
        def bracket[A](branch: Branch, child: Label)(fa: F[A]): F[A] =
          stack.bracketF(branch, child)(fa)
        def bracket[O, A](branch: Branch, child: Label)(
            fa: Pull[F, O, A]
        ): Pull[F, O, A] = stack.bracket(branch, child)(fa)
        def writeWith(branch: Branch, f: List[Label] => E): F[Unit] =
          stack.peek(branch).map(f) >>= (s => chan.send(s).void)

        def fork(parent: Branch, child: Branch): F[Unit] =
          stack.forkTS(parent, child)
        def events: Stream[F, E] = chan.stream
        def close: F[Unit] = chan.close.void
      }

    }
}
