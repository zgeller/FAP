/*   
 * Copyright 2024 Zoltán Gellér
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

package fap.distance.elastic;

import fap.distance.ThresholdDistance;

/**
 * Interface for constrained elastic distance measures that rely on a matching
 * threshold.
 * 
 * @author Zoltán Gellér
 * @version 2024.08.22.
 * @see ConstrainedDistance
 * @see ThresholdDistance
 */
public interface ConstrainedThresholdDistance extends ConstrainedDistance, ThresholdDistance {

}
