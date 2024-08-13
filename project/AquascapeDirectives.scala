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
    BlockDirectives.create("example") {
      import BlockDirectives.dsl.*
      (attribute(0).as[String], attribute("drawChunked").as[Boolean].optional)
        .mapN { case (example, drawChunked) =>
          val codeId = s"${example}Code"

          val (svgEls, frameIds) = {
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
                  s"""ExampleFrameIds.chunked("$chunkedFrameId")"""
                )
            }
          }
          val codeEl = html(
            s"""<pre class="example-code"><code id="$codeId" class="language-scala"></code></pre>"""
          )

          val scriptEl = html(
            s"""<script>
                   | hljsWrapper.highlightExampleCode("$codeId");
                   | ${example}.draw("$codeId", $frameIds);
                   |</script>""".stripMargin
          )
          BlockSequence(codeEl +: svgEls :+ scriptEl)
        }
    }

  val spanDirectives = Seq.empty
  val blockDirectives = Seq(example)
  val templateDirectives = Seq.empty
  val linkDirectives = Seq.empty
}
