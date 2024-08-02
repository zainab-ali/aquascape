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
      attribute(0).as[String]
        .map { example =>
          val frameId = example
          val codeId = s"${example}Code"
          BlockSequence(
            Seq(
              RawContent(
                NonEmptySet.one("html"),
                s"""<div id="$frameId" class="example-frame"></div>"""
              ),
              RawContent(
                NonEmptySet.one("html"),
                s"""<code id="$codeId"></code>"""
              ),
              RawContent(
                NonEmptySet.one("html"),
                s"""<script>${example}.draw("$frameId", "$codeId")</script>"""
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
