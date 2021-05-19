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

package knotjob.diagrams;

import java.util.ArrayList;

/**
 *
 * @author Dirk
 */
public class Vertex {
    
    double x,y,r;
    int type; // 0 means crossing, 1 means arc, 2 means disc
    int label;
    boolean fixed;
    ArrayList<Triangle> rose;
    ArrayList<Edge> comb;

    Vertex(double rad, double a, int typ, int lab, boolean fix) {
        x = rad;
        y = a;
        type = typ;
        label = lab;
        fixed = fix;
        rose = new ArrayList<Triangle>();
        comb = new ArrayList<Edge>();
    }

    Vertex(double rad, int typ, int lab, boolean fix) {
        x = 0;
        y = 0;
        r = rad;
        type = typ;
        label = lab;
        fixed = fix;
        rose = new ArrayList<Triangle>();
        comb = new ArrayList<Edge>();
    }
    
}
