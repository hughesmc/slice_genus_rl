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
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 *
 * @author Dirk
 */
public class GirthMinimizer extends Thread {
    
    private boolean cancelled;
    private boolean aborted;
    private boolean skipped;
    private int extra;
    private ArrayList<LinkData> theLinks;
    private JLabel knotLabel;
    private JLabel posLabel;
    private JLabel optLabel;
    private JDialog frame;
    private Diagram currentBest;
    private Diagram original;
    private int orgGirth;
    private int orgtGirth;
    private int maxGirth;
    private int totalGirth;

    public GirthMinimizer(ArrayList<LinkData> tLinks,JLabel knLabel,JLabel poLabel,JLabel opnLabel,boolean tot, JDialog fram) {
        theLinks = tLinks;
        knotLabel = knLabel;
        posLabel = poLabel;
        optLabel = opnLabel;
        frame = fram;
        if (tot) extra = 0;
        else extra = 1;
    }
    
    public GirthMinimizer(Link theLink) {
        currentBest = new Diagram(theLink);
        original = new Diagram(theLink);
        maxGirth = maxGirth(currentBest.paths);
        totalGirth = totalGirth(currentBest.paths);
    }

    @Override
    public void run() {
        cancelled = false;
        aborted = false;
        skipped = false;
        int counter = 0;
        while (!cancelled & counter < theLinks.size() ) {
            int starter = 0;
            LinkData lData = theLinks.get(counter);
            knotLabel.setText(lData.name);
            original = new Diagram(lData.links.get(lData.chosen));
            orgGirth = maxGirth(original.paths);
            orgtGirth = totalGirth(original.paths);
            maxGirth = orgGirth;
            totalGirth = orgtGirth;
            optLabel.setText(""+maxGirth+"/"+totalGirth);
            while (!aborted & starter < original.crossings.length) {
                Diagram startDiag = new Diagram(original,starter);
                posLabel.setText(String.valueOf(starter));
                tryDiag(startDiag, startDiag.notUsed);
                skipped = false;
                starter++;
            }
            counter++;
            if (!cancelled) {
                if (maxGirth < orgGirth | totalGirth < orgtGirth) {
                    Link newLink = new Link(currentBest.crossings, currentBest.paths);
                    lData.links.add(newLink);
                }
            }
            aborted = false;
        }
        delay(200);
        frame.dispose();
    }

    void delay(int k) {
        try {
            Thread.sleep(k);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    String thePaths(int[][] paths) {
        String thePths = "";
        for (int i=0; i < paths.length; i++) {
            for (int j = 0; j < 4; j++) {
                thePths = thePths+paths[i][j];
                if (j!=3) thePths = thePths+",";
            }
            if (i!= paths.length-1) thePths = thePths+",";
        }
        return thePths;
    }
    
    String theCrosss(int[] crossings) {
        String theCrs = "";
        for (int i=0; i < crossings.length; i++) {
            theCrs = theCrs +crossings[i];
            if (i != crossings.length-1) theCrs = theCrs+",";
        }
        return theCrs;
    }
    
    void tryDiag(Diagram Start, ArrayList<Integer> nUsed) {
        if (skipped) return;
        if (nUsed.isEmpty()) {
            if (maxGirth(Start.paths) <= maxGirth) {
                if (totalGirth(Start.paths) < totalGirth & maxGirth(Start.paths) <= maxGirth) {
                    maxGirth = maxGirth(Start.paths);
                    totalGirth = totalGirth(Start.paths);
                    currentBest = Start;
                    optLabel.setText(""+maxGirth+"/"+totalGirth);
                }
            }
            return;
        }
        int connecter = getConnections(Start,nUsed);
        ArrayList<Integer> ends = getEnds(Start.paths);
        for (int u : nUsed) {
            if (skipped) return;
            if (overlap(ends,original.paths[u]) == connecter) {
                Diagram newDiag = new Diagram(Start,original.paths[u],original.crossings[u],u);
                if (maxGirth(newDiag.paths) <= maxGirth-extra) tryDiag(newDiag, newDiag.notUsed);
            }
        }
    }

    int getConnections(Diagram Start, ArrayList<Integer> nUsed) {
        int connect = 0;
        ArrayList<Integer> ends = getEnds(Start.paths);
        for (int u : nUsed) {
            int t = overlap(ends,original.paths[u]);
            if (t > connect) connect = t; 
        }
        return connect;
    }

    int overlap(ArrayList<Integer> ends, int[] path) {
        int ov = 0;
        for (int t : path) {
            if (ends.contains(t)) ov++;
        }
        return ov;
    }
    
    ArrayList<Integer> getEnds(int[][] paths) {
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
        }
        return ends;
    }
    
    /*final static String girth(int[][] paths) {
        String grth = "(";
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Object) i)) ends.remove((Object) i);
                else ends.add(i);
            }
            grth = grth+" "+ends.size();
        }
        grth = grth+")";
        return grth;
    }// */
    
    public final int maxGirth(int[][] paths) {
        int max = 0;
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Object) i)) ends.remove((Object) i);
                else ends.add(i);
            }
            if (ends.size()> max) max = ends.size();
        }
        return max;
    }

    public final int totalGirth(int[][] paths) {
        int tot = 0;
        ArrayList<Integer> ends = new ArrayList<Integer>();
        for (int j = 0; j < paths.length; j++) {
            for (int i : paths[j]) {
                if (ends.contains((Integer) i)) ends.remove((Integer) i);
                else ends.add(i);
            }
            tot = tot + ends.size();
        }
        return tot;
    }
    
    public void setSkipped(boolean b) {
        skipped = b;
    }

    public void setAborted(boolean b) {
        aborted = b;
    }

    public void setCancelled(boolean b) {
        cancelled = b;
    }
    
}
