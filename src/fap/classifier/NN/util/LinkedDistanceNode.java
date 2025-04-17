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

package fap.classifier.nn.util;

/**
 * Each node contains an {@link #obj object} of type {@code T} and an associated
 * {@code double} value (i.e. the {@link #distance} of the object), as well as
 * references to the {@link #prev previous} and {@link #next} nodes of the list
 * to which the given node belongs.
 * 
 * @author Zoltán Gellér
 * @version 2024.08.20.
 */
public class LinkedDistanceNode<T> {

    /**
     * The object.
     */
    public T obj;

    /**
     * The distance.
     */
    public double distance;

    /**
     * Reference to the previous node of the list to which this node belongs.
     */
    public LinkedDistanceNode<T> prev;

    /**
     * Reference to the next node of the list to which this node belongs.
     */
    public LinkedDistanceNode<T> next;

    /**
     * Constructs a new {@code LinkedDistanceNode} object.
     * 
     * @param obj      the object
     * @param distance the distance
     * @param prev     reference to the previous node
     * @param next     reference to the next node
     */
    public LinkedDistanceNode(T obj, double distance, LinkedDistanceNode<T> prev, LinkedDistanceNode<T> next) {
        this.obj = obj;
        this.distance = distance;
        this.prev = prev;
        this.next = next;
    }

}
