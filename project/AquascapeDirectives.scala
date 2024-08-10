package aquascapebuild

import cats.data.*
import cats.implicits.*
import laika.ast.*
import laika.api.bundle.*

object AquascapeDirectives extends DirectiveRegistry {

  override val description: String =
    "Directives to work with generated aquascape SVG images."

  // Parameter is the name of the JS instance of aquascape.example.Example
  val example: BlockDirectives.Directive =
    BlockDirectives.create("example") {
      import BlockDirectives.dsl.*
      (attribute(0).as[String], attribute("drawChunked").as[Boolean].optional)
        .mapN { case (example, drawChunked) =>
          val codeId = s"${example}Code"
          val unchunkedFrameId = example
          val chunkedFrameId = s"${example}Chunked"
          val unchunkedSnippet = Seq(
            RawContent(
              NonEmptySet.one("html"),
              s"""<div id="$unchunkedFrameId" class="example-frame"></div>"""
            )
          )
          val chunkedSnippet = Seq(
            RawContent(NonEmptySet.one("html"), "<h3>chunked</h3>"),
            RawContent(
              NonEmptySet.one("html"),
              s"""<div id="$chunkedFrameId" class="example-frame"></div>"""
            )
          )

          val (snippet, frameIds) = drawChunked match {
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
          BlockSequence(
            Seq(
              RawContent(
                NonEmptySet.one("html"),
                // TODO: Zainab - Laika's in-built syntax highlighting is not dynamic, so doesn't work with these code blocks.
                s"""<pre><code id="$codeId"></code></pre>"""
              )
            ) ++ snippet ++ Seq(
              RawContent(
                NonEmptySet.one("html"),
                s"""<script>${example}.draw("$codeId", $frameIds)</script>"""
              )
            )
          )
        }
    }

  val spanDirectives = Seq.empty
  val blockDirectives = Seq(example)
  val templateDirectives = Seq.empty
  val linkDirectives = Seq.empty
}
