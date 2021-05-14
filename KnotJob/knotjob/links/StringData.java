/*

Copyright (C) 2019-21 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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
public class StringData {
    
    public String name;
    public String comment;
    public String sinvariant;
    public String sqeven;
    public String sqodd;
    public ArrayList<String> crossings;
    public ArrayList<String> paths;
    public ArrayList<String> orientations;
    public ArrayList<String> unredKhovHom;
    public ArrayList<String> redKhovHom;
    public ArrayList<String> khovInfo;
    public ArrayList<String> oddKhovHom;
    public ArrayList<String> okhovInfo;
    
    public StringData(String title) {
        name = title;
        comment = null;
        sinvariant = null;
        sqeven = null;
        sqodd = null;
        crossings = new ArrayList<String>();
        paths = new ArrayList<String>();
        orientations = new ArrayList<String>();
        unredKhovHom = new ArrayList<String>();
        redKhovHom = new ArrayList<String>();
        oddKhovHom = new ArrayList<String>();
        khovInfo = new ArrayList<String>();
        okhovInfo = new ArrayList<String>();
    }
}
