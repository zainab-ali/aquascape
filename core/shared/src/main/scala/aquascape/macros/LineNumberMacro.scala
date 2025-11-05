package aquascape.macros

import scala.quoted.*

object macros {
  def fromContextImpl(using ctx: Quotes)= {
    import ctx.reflect._
    val position = Position.ofMacroExpansion
    Expr(position.startLine + 1)
  }
}
case class LineNumber(lineNumber: Int)

object LineNumber {
  inline def here: Int = ${ macros.fromContextImpl }

  inline given derived: LineNumber = LineNumber(here)
}
