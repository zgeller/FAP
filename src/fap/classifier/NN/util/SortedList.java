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

package fap.classifier.NN.util;

/**
 * A simple sorted list of {@link LinkedDistanceNode} objects. It can contain at
 * most {@link #len} objects sorted according to their
 * {@link LinkedDistanceNode#distance distance} in ascending order.
 * 
 * @author Zoltán Gellér
 * @version 2024.10.01
 * @see LinkedDistanceNode
 * @see DistanceNode
 */
public class SortedList<T> {

    /**
     * Capacity of the list.
     */
    private int len;

    /**
     * Number of nodes in the list.
     */
    private int count;

    /**
     * The first node of the list.
     */
    private LinkedDistanceNode<T> first;

    /**
     * The last node of the list.
     */
    private LinkedDistanceNode<T> last;

    /**
     * Constructs a new sorted list of the specified capacity.
     * 
     * @param len the capacity of the list
     */
    public SortedList(int len) {
        this.len = len;
        this.count = 0;
        this.first = null;
        this.last = null;
    }

    /**
     * Returns the first element of the list.
     * 
     * @return the first element of the list
     */
    public LinkedDistanceNode<T> getFirst() {
        return first;
    }

    /**
     * Returns the last element of the list.
     * 
     * @return the last element of the list
     */
    public LinkedDistanceNode<T> getLast() {
        return last;
    }

    /**
     * Returns the number of nodes in the list.
     * 
     * @return the number of nodes in the list
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the capacity of the list.
     * 
     * @return the capacity of the list
     */
    public int getLen() {
        return len;
    }

    /**
     * Adds the specified {@link DistanceNode} to the list.
     * 
     * @param node the {@code DistanceNode} to add
     */
    public void add(DistanceNode<T> node) {
        this.add(node.obj, node.distance);
    }

    /**
     * Adds the given object and distance to the list.
     * 
     * @param series   the object
     * @param distance the distance
     */
    public void add(T obj, double distance) {

        // if the length of the list is less than one, do nothing
        if (len < 1)
            return;

        // if the list is empty
        if (count == 0) {
            first = new LinkedDistanceNode<>(obj, distance, null, null);
            last = first;
            count++;
        }

        // if the list isn't empty
        else {

            // if the distance is bigger or equal than the distance of the biggest distance
            if (distance >= last.distance) {

                // if the list is full, do nothing
                if (count == len)
                    return;

                // if the list isn't full, append the new node to the end of the list
                LinkedDistanceNode<T> node = new LinkedDistanceNode<>(obj, distance, last, null);
                last.next = node;
                last = node;
                count++;
                
            }

            // if the distance is smaller than the biggest distance
            // and the length is 1, just replace the node
            else if (len == 1) {
                first = new LinkedDistanceNode<>(obj, distance, null, null);
                last = first;
                count = 1;
            }

            // if the distance is smaller than the biggest distance
            // and the length is greater than 1, insert the new node
            // and remove the last one if the list is full
            else {

                // find the first node whose distance is greater than the given distance
                LinkedDistanceNode<T> node = first;
                while (distance > node.distance)
                    node = node.next;

                LinkedDistanceNode<T> newNode = new LinkedDistanceNode<>(obj, distance, node.prev, node);

                if (node != first)
                    node.prev.next = newNode;
                node.prev = newNode;

                // if the list is full, remove the last node
                if (count == len) {
                    LinkedDistanceNode<T> tmp = last;
                    last = tmp.prev;
                    last.next = null;
                    tmp.prev = null;
                } else
                    count++;

                if (first == node)
                    first = newNode;
                
            }
            
        }
        
    }
    
    /**
     * Removes the first element from this list and returns it.
     * 
     * @return the first element of the list
     */
    public DistanceNode<T> remove() {
        
        DistanceNode<T> node = null;
        
        if (first != null) {
            
            LinkedDistanceNode<T> tmp = first;
            node = new DistanceNode<T>(first.obj, first.distance);
            
            first = first.next;
            if (last == tmp)
                last = first;
            
        }
        
        return node;
        
    }
    
    /**
     * Removes the first {@code k} elements of the this list and returns them as a
     * new list.
     * 
     * @param k the number of elements to be removed from the beginning of the list
     * @return a {@code SortedList} containing the first {@code k} elements of this
     *         list
     */
    public SortedList<T> remove(int k) {
        
        SortedList<T> list = new SortedList<>(k);
        
        if (k > 0) {
            
            DistanceNode<T> node = remove();
            while (node != null && k > 0) {
                count--;
                list.add(node);
                k--;
                if (k > 0)
                    node = remove();
            }
            
        }
        
        return list;
        
    }

}
