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

package aquascapebuild

import cats.data.*
import cats.implicits.*
import laika.ast.*
import laika.api.bundle.*

object AquascapeDirectives extends DirectiveRegistry {

  override val description: String =
    "Directives to generate aquascape SVG images."

  private def html(element: String): RawContent =
    RawContent(NonEmptySet.one("html"), element)

  // Parameter is the name of the JS instance of aquascape.example.Example
  val example: BlockDirectives.Directive =
    // TODO: Access the cursor and 
    BlockDirectives.create("example") {
      exampleAttributes
        .mapN { case (cursor, exampleName, drawChunked) =>
          val example = cursor.config.get[String]("pageid").fold(_ => exampleName, id => s"$id.$exampleName")
          val codeId = s"${example}Code"

          val (svgEls, frameIds) = frameElsAndIds(example, drawChunked)

          val scriptEl = html(
            s"""<script>
                   | aquascape.example($example, "$codeId", $frameIds);
                   |</script>""".stripMargin
          )
          BlockSequence(codeEl(codeId) +: svgEls :+ scriptEl)
        }
    }
  val exampleWithInput: BlockDirectives.Directive =
    BlockDirectives.create("exampleWithInput") {
      exampleAttributes
        .mapN { case (cursor, exampleName, drawChunked) =>
          val example = cursor.config.get[String]("pageid").fold(_ => exampleName, id => s"$id.$exampleName")
          val codeId = s"${example}Code"
          val (svgEls, frameIds) = frameElsAndIds(example, drawChunked)

          val labelId = s"${example}Label"
          val inputId = s"${example}Input"
          val inputEls = BlockSequence(
            Seq(
              html(s"""<label id="$labelId" for="$inputId"></label>"""),
              html(s"""<input id="$inputId"></input>""")
            ),
            Options(styles = Set("example-input"))
          )

          val scriptEl = html(
            s"""<script>
                   | aquascape.exampleWithInput($example, "$codeId", $frameIds, "$labelId", "$inputId");
                   |</script>""".stripMargin
          )
          BlockSequence(inputEls +: (codeEl(codeId) +: svgEls :+ scriptEl))
        }
    }

  private def exampleAttributes: (
      BlockDirectives.DirectivePart[DocumentCursor],
      BlockDirectives.DirectivePart[String],
      BlockDirectives.DirectivePart[Option[Boolean]]
  ) = {
    import BlockDirectives.dsl.*
    (cursor, attribute(0).as[String], attribute("drawChunked").as[Boolean].optional)
  }
  private def codeEl(codeId: String): RawContent = {
    html(
      s"""<pre class="example-code"><code id="$codeId" class="language-scala"></code></pre>"""
    )
  }

  private def frameElsAndIds(
      example: String,
      drawChunked: Option[Boolean]
  ): (Seq[RawContent], String) = {
    val unchunkedFrameId = example
    val chunkedFrameId = s"${example}Chunked"
    val unchunkedSnippet = Seq(
      html(
        s"""<div id="$unchunkedFrameId" class="example-frame"></div>"""
      )
    )
    val chunkedSnippet = Seq(
      html("<h3>chunked</h3>"),
      html(
        s"""<div id="$chunkedFrameId" class="example-frame"></div>"""
      )
    )
    drawChunked match {
      case None =>
        (
          unchunkedSnippet ++ chunkedSnippet,
          s"""ExampleFrameIds.both("$unchunkedFrameId", "$chunkedFrameId")"""
        )
      case Some(true) =>
        (
          chunkedSnippet,
          s"""ExampleFrameIds.chunked("$chunkedFrameId")"""
        )
      case Some(false) =>
        (
          unchunkedSnippet,
          s"""ExampleFrameIds.unchunked("$unchunkedFrameId")"""
        )
    }
  }
  val spanDirectives = Seq.empty
  val blockDirectives = Seq(example, exampleWithInput)
  val templateDirectives = Seq.empty
  val linkDirectives = Seq.empty
}
