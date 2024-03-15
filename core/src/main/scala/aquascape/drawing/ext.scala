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
import aquascape.Event
import aquascape.Time
import cats.Foldable

extension [F[_]: Foldable](events: F[(Event, Time)]) {
  private[aquascape] def toPicture(config: Config): Picture[Unit] =
    diagramToPicture(config)(eventsToDiagram(events))
}
