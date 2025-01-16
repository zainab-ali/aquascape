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

package aquascape.codemacro
import aquascape.StreamCode
import cats.effect.IO

import scala.meta.*
import scala.meta.given
import scala.quoted.*

def codeImpl(
    stream: Expr[IO[Any]]
)(using q: Quotes): Expr[StreamCode] = {
  import q.reflect.*
  val txt = Expr(clean(stream.asTerm.pos.sourceCode.getOrElse {
    throw new Error("Cannot get source code of code example.")
  }))
  '{
    StreamCode(
      code = Some($txt),
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
  ???
  // scalameta transformers don't compile for Scala 3 yet.
  // See
  //  - https://github.com/scalameta/scalameta/issues/4146
  //  - https://github.com/scalameta/scalameta/blob/2e71839da10d6bbb20a19a09407778d8f1f179f1/scalameta/common/shared/src/main/scala/scala/meta/internal/transversers/traverser.scala#L6
  // tree.transform {
  //   case Term.Apply.After_4_6_0(Term.Select(t, Term.Name("fork")), _) =>
  //     stripStageCalls(t)
  //   case Term.Apply.After_4_6_0(Term.Select(t, Term.Name("stage")), _)  => t
  //   case Term.Apply.After_4_6_0(Term.Select(t, Term.Name("trace")), _)  => t
  //   case Term.Apply.After_4_6_0(Term.Select(t, Term.Name("trace_")), _) => t
  //   case Term.Apply
  //         .After_4_6_0(Term.Select(t, Term.Name("compileStage")), _) =>
  //     t
  // }
}

private def prettySyntax(tree: Tree): String = tree match {
  case Term.Block(stats @ (_ :: _ :: _)) =>
    // Blocks should be treated as separate statements to avoid printing {}.
    // The last statement should be given an extra line spacing to stand out.
    val lines = stats.map(_.syntax)
    (lines.init :+ "\n" :+ lines.last).mkString("\n")
  case Term.Block(stat :: Nil) => stat.syntax
  case singleStat              => singleStat.syntax
}

private def format(code: Tree): String = {
  import scala.meta.dialects.Scala3
  val text = prettySyntax(code)
  text.trim
}
