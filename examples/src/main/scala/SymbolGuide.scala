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

import aquascape.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel
import aquascape.drawing.{Config, Symbols}

@JSExportTopLevel("SymbolGuide")
object SymbolGuide {

  val config = Config.default

  @JSExport
  val start = new Symbol(Symbols.start(config))

  @JSExport
  val finished = new Symbol(Symbols.finished(config))

  @JSExport
  val finishedErrored = new Symbol(Symbols.finishedErrored(config))

  @JSExport
  val label = new Symbol(Symbols.label(config))

  @JSExport
  val pull = new Symbol(Symbols.pull(config))

  @JSExport
  val output = new Symbol(Symbols.output(config))

  @JSExport
  val outputChunk = new Symbol(Symbols.outputChunk(config))

  @JSExport
  val error = new Symbol(Symbols.error(config))

  @JSExport
  val done = new Symbol(Symbols.done(config))

  @JSExport
  val eval = new Symbol(Symbols.eval(config))

  @JSExport
  val time = new Symbol(Symbols.time(config))
}
