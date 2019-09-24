/*
 * Copyright 2019 Matthew Denton
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

package me.masstrix.eternalnature.core.world;

import org.bukkit.util.Vector;

public class VectorLattice {

    private Vector[][][] points;

    public VectorLattice(int size) {
        points = new Vector[size][size][size];
    }

    public void set(int x, int y, int z, Vector vec) {
        validateValues(x, y, z);
        this.points[x][y][z] = vec;
    }

    public Vector get(int x, int y, int z) {
        validateValues(x, y, z);
        return points[x][y][z];
    }

    private void validateValues(int x, int y, int z) {
        if (x < 0) throw new IllegalArgumentException("x value is negative.");
        else if (y < 0) throw new IllegalArgumentException("y value is negative.");
        else if (z < 0) throw new IllegalArgumentException("z value is negative.");
    }
}
