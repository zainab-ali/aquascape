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

package aquascape.golden

import aquascape.*
import cats.effect.*
import cats.syntax.all.*
import fs2.*
import fs2.io.file.Path
import fs2.io.file.*
import org.scalafmt.Scalafmt
import org.scalafmt.config.ScalafmtConfig

import scala.meta.*

private def writeSource(
    pos: RangePos,
    exampleName: ExampleName
): IO[Unit] = {
  def stripStageCalls(tree: Tree): Tree = {
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

  def format(code: Tree): IO[String] = {
    import scala.meta.dialects.Scala3
    Scalafmt
      .format(code.syntax, style = ScalafmtConfig.default.withDialect(Scala3))
      .toEither
      .liftTo[IO]
  }

  def template(exampleName: ExampleName, code: String): String = {
    s"""
## ${exampleName.value}

```scala
$code
```
![diagram](${exampleName.relativeImage})
"""
  }
  val target = exampleName.parent.markdownFile
  val source = Files[IO]
    .readUtf8(Path(pos.source))
    .flatMap(str => Stream.emits(str))
    .drop(pos.startAt)
    .take(pos.endAt - pos.startAt)
    .compile
    .toVector
    .map(_.mkString)
  source.flatMap { source =>
    val cleaned =
      source.parse[Stat].toEither.leftMap(_.details).liftTo[IO].flatMap {
        tree =>
          format(stripStageCalls(tree))
      }
    cleaned.flatMap { text =>
      Files[IO].createDirectories(exampleName.parent.value) >> Files[IO]
        .writeUtf8(target, Flags.Append)(
          Stream(template(exampleName, text))
        )
        .compile
        .drain
    }
  }
}

private def writeHeader(testName: TestName): IO[Unit] = {
  val header = s"#${testName.name}"
  Files[IO].createDirectories(testName.value) >> Files[IO]
    .writeUtf8(testName.markdownFile, Flags.Append)(
      Stream(header)
    )
    .compile
    .drain
}
