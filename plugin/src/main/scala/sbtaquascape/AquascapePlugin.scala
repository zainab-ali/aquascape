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

package sbtaquascape

import mdoc.MdocPlugin
import mdoc.MdocPlugin.autoImport.*
import sbt.*

import Keys.*

object AquascapePlugin extends AutoPlugin {
  override def requires = MdocPlugin

  object autoImport {
    val aquascapeVersion = BuildInfo.version
    val aquascape = taskKey[Unit]("Write aquascapes to mdoc output directory")
    val aquascapeProject =
      settingKey[Project]("SBT project containing aquascapes")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    aquascape := (Def.taskDyn {
      val project = aquascapeProject.value
      val targetDir = mdocOut.value
      Def.task {
        (project / Compile / run).toTask(s" --output $targetDir").value
      }
    }).value
  )
}
