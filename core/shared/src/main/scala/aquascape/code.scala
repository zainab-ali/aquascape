package aquascape

import cats.effect.IO

final case class StreamCode(code: Option[String], stream: IO[Any])

inline def code(stream: IO[Any]): StreamCode = ${
  aquascape.codemacro.codeImpl('stream)
}
