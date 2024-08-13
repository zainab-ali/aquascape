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

package aquascape.examples

import cats.effect.IO
import org.scalafmt.Scalafmt
import org.scalafmt.config.ScalafmtConfig

import scala.meta.*
import scala.quoted.*

final case class StreamCode(code: String, stream: IO[Any])

inline def code(stream: IO[Any]): StreamCode = ${ codeImpl('stream) }

private def codeImpl(
    stream: Expr[IO[Any]]
)(using q: Quotes): Expr[StreamCode] = {
  import q.reflect.*
  val txt = Expr(clean(stream.asTerm.pos.sourceCode.getOrElse {
    throw new Error("Cannot get source code of code example.")
  }))
  '{
    StreamCode(
      code = $txt,
      stream = $stream
    )
  }
}

private def clean(source: String): String = {
  val tree =
    source
      .parse[Stat]
      .get // throw an error at compile time if we cannot parse the source code.
  format(stripStageCalls(tree))
}
private def stripStageCalls(tree: Tree): Tree = {
  tree.transform {
    case Term.Apply.After_4_6_0(Term.Select(t, Term.Name("fork")), _) =>
      stripStageCalls(t)
    case Term.Apply.After_4_6_0(Term.Select(t, Term.Name("stage")), _) => t
    case Term.Apply.After_4_6_0(Term.Select(t, Term.Name("trace")), _) => t
    case Term.Apply
          .After_4_6_0(Term.Select(t, Term.Name("compileStage")), _) =>
      t
  }
}

private def format(code: Tree): String = {
  import scala.meta.dialects.Scala3
  Scalafmt
    .format(code.syntax, style = ScalafmtConfig.default.withDialect(Scala3))
    .get // Throw an error at compile time if we cannot format the code
}
