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

package knotjob.filters;

import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class SInvFilter implements Filter {

    private final boolean nonConstS;
    private final boolean nonConstSqOne;
    private final boolean even;
    private final boolean bddBelow;
    private final boolean bddAbove;
    private final int lowerBound;
    private final int upperBound;
    private String name;
    
    public SInvFilter(String nm, boolean ncS, boolean ncSq, boolean ev, boolean bddb, boolean bdda, 
            int lb, int ub) {
        nonConstS = ncS;
        nonConstSqOne = ncSq;
        even = ev;
        bddBelow = bddb;
        bddAbove = bdda;
        lowerBound = lb;
        upperBound = ub;
        name = nm;
    }
    
    @Override
    public boolean linkIsFiltered(LinkData link) {
        boolean okay = false;
        if (nonConstS) okay = linkHasNonConstS(link);
        if (nonConstSqOne) okay = linkHasNonConstSqOne(link);
        if (!nonConstS && !nonConstSqOne ) okay = sInvInBounds(link);
        return okay;
    }
    
    private boolean sInvInBounds(LinkData link) {
        boolean okay = false;
        int[][] sinvs = link.sInvariants();
        int i = 0;
        while (!okay & i < sinvs.length) {
            int s = sinvs[i][1];
            okay = true;
            if (bddBelow && s < lowerBound) okay = false;
            if (bddAbove && s > upperBound) okay = false;
            i++;
        }
        return okay;
    }
    
    private boolean linkHasNonConstSqOne(LinkData link) {
        boolean okay = false;
        int[] sqOne = link.getSqOne(even);
        if (sqOne != null) {
            int c = sqOne[0];
            okay = false;
            if (sqOne[1] != c | sqOne[2] != c | sqOne[3] != c) okay = true;
        }
        return okay;
    }
    
    private boolean linkHasNonConstS(LinkData link) {
        boolean okay = false;
        int[][] sinv = link.sInvariants();
        if (sinv.length > 0) {
            int i = 1;
            okay = false;
            int c = sinv[0][1];
            while (i < sinv.length && !okay) {
                if (sinv[i][1] != c) okay = true;
                else i++;
            }
        }
        return okay;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String nm) {
        name = nm;
    }
    
}
