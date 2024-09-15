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

package aquascape.drawing

object Symbols {

  def start(config: Config): Picture[Unit] =
    DiagramToPicture.start(config)

  def pull(config: Config): Picture[Unit] =
    DiagramToPicture.pull(config, 0, Item.Pull(from = 0, to = 1))

  def output(config: Config): Picture[Unit] =
    DiagramToPicture.output(
      config,
      0,
      Item.Output(value = "a", from = 1, to = 0, pullProgress = 0)
    )

  def outputChunk(config: Config): Picture[Unit] =
    DiagramToPicture.output(
      config,
      0,
      Item.Output(value = "[a, b]", from = 1, to = 0, pullProgress = 0)
    )

  def error(config: Config): Picture[Unit] =
    DiagramToPicture.error(
      config,
      0,
      Item.Error(value = "Err", from = 1, to = 0, pullProgress = 0)
    )

  def done(config: Config): Picture[Unit] =
    DiagramToPicture.done(
      config,
      0,
      Item.Done(from = 1, to = 0, pullProgress = 0)
    )

  def finished(config: Config): Picture[Unit] =
    DiagramToPicture.finished(
      config,
      0,
      Item.Finished(value = "Some(a)", errored = false)
    )

  def finishedErrored(config: Config): Picture[Unit] =
    DiagramToPicture.finished(
      config,
      0,
      Item.Finished(value = "Err", errored = true)
    )

  def time(config: Config): Picture[Unit] =
    DiagramToPicture.time(config, 0, Item.Time(value = 1))

  def eval(config: Config): Picture[Unit] =
    DiagramToPicture.eval(config, 0, Item.Eval(value = "a", at = 0))

  def label(config: Config): Picture[Unit] =
    DiagramToPicture.label(config, 0)("take(2)", 0)
}
