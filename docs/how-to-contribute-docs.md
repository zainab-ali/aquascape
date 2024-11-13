# How to contribute docs

Contributions to the reference guide are very welcome!

## Getting started

1. First experiment with [how to write aquascapes](how-to-write-the-diagrams.md).
2. The aquascapes for the reference guide are stored in the [examples module](https://github.com/zainab-ali/aquascape/tree/main/examples/src/main/scala/docs/reference). By convention, each file has the same name as its corresponding section in the guide. Find the file you want to contribute to. 

3. In the object in the file, create a new `Example` value and use `JSExport` to export it:

```scala
  @JSExport
  val myNewExample = new Example {
    def apply(using Scape[IO]): StreamCode = code(
	  ??? // Put your code here
	)
  }
```

Run `sbt examples/fastLinkJS` to check that the code compiles and links.

4. Go to the corresponding markdown file, and add the following snippet:

```md
@:example(basic) {
  drawChunked = false
}
```

5. Run `sbt docs/tlSitePreview` to view the new aquascape.

### Understanding the process

The `examples` module compiles to JavaScript using [ScalaJS](https://www.scala-js.org/). The JavaScript is bundled up and referenced in the aquascape docs.
The `docs` are built using [Laika](https://typelevel.org/Laika/). The `@:example` directive is a custom directive that embeds a `<script>` in the generated html page that runs the JS code. 

Aquascape generation happens in the user's browser, so we can generate aquascapes based on user input. Have a look at `@:exampleWithInput` to see how to do this.

## Writing a good reference

The reference guide is heavily influenced by [diataxis](https://diataxis.fr/reference). Try and make reference docs as concise and comprehensive as possible. You should cover the nitty-gritty details of how an operator works.

For each operator, you might want to draw aquascapes for:

 - chunking
 - error handling
 - resource management
 - special termination cases

Have a read of the [best practices](how-to-write-the-diagrams.md#best-practices) to keep your aquascapes clean.
