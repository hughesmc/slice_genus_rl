/*

Copyright (C) 2019-20 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

This file is part of KnotJob.

KnotJob is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

KnotJob is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTIBILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org.licenses/>.

 */

package knotjob.links;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class Diagram {

    int[] crossings;
    int[][] paths;
    ArrayList<Integer> notUsed;

    public Diagram(Link link) {
        crossings = link.getCrossings();
        paths = link.getPaths();
        notUsed = new ArrayList<Integer>();
    }

    public Diagram(Diagram current, int pos) {
        crossings = new int[1];
        paths = new int[1][4];
        notUsed = new ArrayList<Integer>();
        crossings[0] = current.crossings[pos];
        paths[0] = current.paths[pos];
        for (int i = 0; i < current.crossings.length; i++) {
            if (i!=pos) notUsed.add(i);
        }
    }

    public Diagram(Diagram Start, int[] path, int cross, int u) {
        crossings = new int[Start.crossings.length+1];
        paths = new int[Start.paths.length+1][4];
        notUsed = new ArrayList<Integer>();
        for (int t = 0; t < Start.crossings.length; t++) {
            crossings[t] = Start.crossings[t];
            paths[t] = Start.paths[t];
        }
        crossings[crossings.length-1] = cross;
        paths[paths.length-1] = path;
        for (int t : Start.notUsed) {
            if (u!=t) notUsed.add(t);
        }
    }
    
}
