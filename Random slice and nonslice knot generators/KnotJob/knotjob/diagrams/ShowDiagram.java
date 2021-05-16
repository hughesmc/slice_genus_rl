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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import knotjob.AbortInfo;
import knotjob.dialogs.CompDialog;
import knotjob.dialogs.DiagramFrame;
import knotjob.dialogs.RotateDialog;
import knotjob.links.Link;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class ShowDiagram extends Thread implements MouseListener, MouseMotionListener {
    
    private final double tau;
    private final double error;
    private final int precision;
    private final DiagramFrame frame;
    private final AbortInfo abort;
    private final Link link;
    private final double radius0;
    private final double radius1;
    private final double factor;
    private final int crs;
    private final int cmps;
    private int highLight;
    private int highLabel;
    private final ArrayList<Integer> ignorers;
    private final ArrayList<Integer> counters;
    private JScrollPane scroller;
    private ArrayList<Color> colors;
    private ArrayList<Boolean> orients;
    private ArrayList<Boolean> shows;
    private boolean recalc;
    private SComplex[] theComplexes;
    private final ArrayList<Integer> illegalPaths;
    private GraphicDiagram gDiag;
    
    public ShowDiagram(LinkData theLink, int prec, DiagramFrame fram, double fc) {
        radius0 = 1;
        radius1 = 1;
        factor = fc;
        Link nlink = theLink.chosenLink().breakUp();
        crs = nlink.crossingNumber();
        cmps = nlink.relComponents();
        illegalPaths = new ArrayList<Integer>();
        if (nlink.isReduced()) link = nlink;
        else link = nlink.graphicalReduced();
        if (crs < link.crossingNumber()) {
            int k = (link.crossingNumber()-crs)/4;
            for (int i = 0; i < k; i++) {
                for (int j = 5; j < 9; j++) illegalPaths.add(2*crs + 8*i + j);
            }
        }
        frame = fram;
        abort = new AbortInfo();
        frame.closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                frame.dispose();
                gDiag.stopMoving();
                abort.cancel();
            }
        });
        precision = prec;
        error = Math.pow(10, -prec);
        tau = 2 * Math.PI;
        scroller = null;
        ignorers = new ArrayList<Integer>();
        counters = new ArrayList<Integer>();
    }

    @Override
    public void run() {
        highLight = -1;
        drawAll(null,null,null);
        setStuff(20);
        if (scroller != null) {
            frame.zoomSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int factor = 19+frame.zoomSlider.getValue();
                    setStuff(factor);
                    
                }
            });
            frame.minimizeEng.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox theBox = (JCheckBox) ae.getSource();
                    gDiag.minimizeEng(theBox.isSelected());
                    gDiag.repaint();
                }
            });
            frame.rotateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RotateDialog fram = new RotateDialog(frame, "Rotate Link", true);
                    fram.setUpStuff();
                    if (fram.angle != 0) gDiag.rotateDiagram(fram.angle);
                    gDiag.setPreferredSize(new Dimension(100 + (int) (gDiag.factorx * gDiag.maxx),60 + 
                            (int) (gDiag.factory * gDiag.maxy)));
                    gDiag.repaint();
                    scroller.getViewport().revalidate();
                }
            });
            frame.compButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CompDialog fram = new CompDialog(frame, "Components", true, gDiag);
                    fram.setUpStuff();
                    if (fram.isOkay()) {
                        gDiag.setColors(fram.setColors());
                        gDiag.setShownComponents(fram.setShownComponents());
                        gDiag.setOrientComponents(fram.setOrientComponents());
                    }
                    gDiag.repaint();
                    scroller.getViewport().revalidate(); // */
                }
            });
        }
        boolean keepgoing = (scroller != null);
        while (keepgoing) {
            recalc = false;
            while (keepgoing && !recalc) {
                keepgoing = frame.isVisible();
                sleepFor(10);
                if (recalc) {
                    drawAll(colors, orients, shows);
                    frame.zoomSlider.getChangeListeners()[0].stateChanged(new ChangeEvent(this));
                }
            }
        }
    }
    
    private void setStuff(int factor) {
        gDiag.factorx = (double) (factor);
        gDiag.factory = (double) (factor);
        gDiag.setPreferredSize(new Dimension(100 + (int) (factor * gDiag.maxx),
                60 + (int) (factor * gDiag.maxy)));
        gDiag.repaint();
        scroller.getViewport().revalidate();
    }
    
    private void sleepFor(long i) {
        try {
            Thread.sleep(i);
        }
        catch (InterruptedException ex) {

        }
    }
    
    private void drawAll(ArrayList<Color> clrs, ArrayList<Boolean> oCs, ArrayList<Boolean> sCs) {
        ArrayList<ArrayList<Integer[]>> discs = link.getDiscs();
        ArrayList<ArrayList<Integer>> splitComps = link.splitComponents();
        int scomp = splitComps.size();
        DiagramInfo[] theDiagrams = new DiagramInfo[scomp];
        theComplexes = new SComplex[scomp];
        for (int i = 0; i < scomp; i++) theDiagrams[i] = new DiagramInfo();
        for (ArrayList<Integer[]> disc : discs) {
            int u = getComp(disc.get(0)[0], splitComps);
            theDiagrams[u].add(disc);
        }
        int counter = 0;
        for (int i = 0; i < scomp; i++) {
            theDiagrams[i].setIgnore();
            if (highLight<0) {
                theDiagrams[i].setIgnore();
                ignorers.add(theDiagrams[i].ignore);
            }
            else {
                theDiagrams[i].ignore = ignorers.get(i);
                int j = 0;
                if (i > 0) j = counters.get(i-1);
                if (j <= highLight && highLight < counters.get(i)) {
                    theDiagrams[i].ignore = highLabel;
                    ignorers.set(i,highLabel);
                }
            }
            theComplexes[i] = getComplex(theDiagrams[i],splitComps.get(i),counter);
            improve(theComplexes[i]);
            if (abort.isAborted()) break;
            for (Vertex vert : theComplexes[i].vertices) {
                vert.fixed = false;
            }
            position(theComplexes[i]);
            counter = counter + theComplexes[i].vertices.size();
            if (highLight<0) counters.add(counter);
        }
        if (abort.isAborted()) {
            return;
        }
        drawDiagram(clrs,oCs,sCs);
    }
    
    private void drawDiagram(ArrayList<Color> clrs, ArrayList<Boolean> oCs, ArrayList<Boolean> sCs) {
        gDiag = new GraphicDiagram(theComplexes,19+frame.zoomSlider.getValue(), 
                19+frame.zoomSlider.getValue(), link, cmps, factor,
                frame.minimizeEng.isSelected(),clrs,oCs, sCs);
        gDiag.setPreferredSize(new Dimension(100 + (int) (20 * gDiag.maxx),
                60 + (int) (20 * gDiag.maxy)));
        Container c = frame.getContentPane();
        c.remove(frame.infoPanel);
        scroller = new JScrollPane(gDiag);
        scroller.addMouseListener(this);
        scroller.addMouseMotionListener(this);
        scroller.getViewport().setBackground(Color.WHITE);
        c.add(scroller, BorderLayout.CENTER);
        frame.revalidate();
    }
    
    /*private void translate(SComplex complex, boolean keep) {
        double maxx = -10000;
        double maxy = -10000;
        double minx = 10000;
        double miny = 10000;
        double minradx = -10000;
        double maxradx = 1;
        double minrady = -10000;
        double maxrady = 1;
        for (Vertex vert : complex.vertices) {
            if (vert.fixed) {
                if (maxx + maxradx < vert.x+vert.r) {
                    maxx = vert.x;
                    maxradx = vert.r;
                }
                if (maxy + maxrady < vert.y + vert.r) {
                    maxy = vert.y;
                    maxrady = vert.r;
                }
                if (minx - minradx > vert.x - vert.r) {
                    minx = vert.x;
                    minradx = vert.r;
                }
                if (miny - minrady > vert.y - vert.r) {
                    miny = vert.y;
                    minrady = vert.r;
                }
            }
        }
        complex.maxx = maxx + maxradx + minradx - minx;
        complex.maxy = maxy + maxrady + minrady - miny;
        double fac = 20/complex.maxx;
        if (keep) fac = 1;
        for (Vertex vert : complex.vertices) {
            vert.x = fac * (vert.x - minx + minradx);
            vert.y = fac * (vert.y - miny + minrady);
            vert.r = fac * vert.r;
        }
        complex.maxx = complex.maxx * fac;
        complex.maxy = complex.maxy * fac;
    }// */
    
    private void position(SComplex complex) {
        Triangle trian = complex.triangles.get(0);
        trian.fvert.fixed = true;
        trian.svert.fixed = true;
        trian.tvert.fixed = true;
        setVertex(trian.fvert,0,0);
        setVertex(trian.svert,trian.fvert.r + trian.svert.r,0);
        double alpha = angleValue(trian.fvert, trian.svert, trian.tvert);
        double rad = trian.fvert.r + trian.tvert.r;
        setVertex(trian.tvert,Math.cos(alpha) * rad, Math.sin(alpha) * rad);
        complex.triangles.remove(trian);
        while (!complex.triangles.isEmpty()) {
            int i = 0;
            boolean found = false;
            while (!found) {
                trian = complex.triangles.get(i);
                if ((trian.fvert.fixed & trian.svert.fixed) | (trian.fvert.fixed & trian.tvert.fixed) | 
                        (trian.tvert.fixed & trian.svert.fixed)) found = true;
                else i++;
            }
            if (!trian.fvert.fixed) positionVertex(trian.fvert,trian.svert,trian.tvert,trian.rot);
            if (!trian.svert.fixed) positionVertex(trian.svert,trian.tvert,trian.fvert,trian.rot);
            if (!trian.tvert.fixed) positionVertex(trian.tvert,trian.fvert,trian.svert,trian.rot);
            complex.triangles.remove(i);
        }
    }
    
    private void positionVertex(Vertex fvert, Vertex svert, Vertex tvert,boolean ro) {
        double angle = angleValue(svert,tvert,fvert);
        double rad = fvert.r+svert.r;
        double entry = (tvert.x-svert.x)/(tvert.r+svert.r);
        if (entry > 1) entry = 1;
        if (entry < -1) entry = -1;
        double sangl = Math.acos(entry);
        if (tvert.y-svert.y < 0) sangl = -sangl;
        double ang = sangl-angle;
        if (ro) ang = angle+sangl;
        fvert.x = svert.x + Math.cos(ang) * rad;
        fvert.y = svert.y + Math.sin(ang) * rad;
        fvert.fixed = true;
    }
    
    private void setVertex(Vertex vert, double x, double y) {
        vert.x = x;
        vert.y = y;
    }
    
    private SComplex getComplex(DiagramInfo theDiagram, ArrayList<Integer> crossings, int counter) {
        SComplex complex = new SComplex();
        ArrayList<Integer> paths = new ArrayList<Integer>();
        for (int i : crossings) {
            Vertex vert = new Vertex(radius0,0,i,false);
            complex.vertices.add(vert);
            for (int j : link.getPath(i)) if (!paths.contains(j)) paths.add(j);
        }
        for (int i : paths) {
            Vertex vert = new Vertex(radius1,1,i,false);
            complex.vertices.add(vert);
        }
        /*if (counter <= highLight && highLight < counter+crossings.size()+paths.size()+theDiagram.discNumber()-1) {
            theDiagram.ignore = highLabel;
        }// */
        for (int i = 0; i < theDiagram.discNumber(); i++) {
            if (i != theDiagram.getIgnore()) {
                Vertex vert = new Vertex(1,2,i,false);
                complex.vertices.add(vert);
            }
        }
        for (int i : crossings) {
            for (int j = 0; j < 4; j++) {
                Edge edge = createEdge(i,link.getPath(i, j),0,1,complex.vertices);
                complex.edges.add(edge);
                edge.fvert.comb.add(edge);
                edge.svert.comb.add(edge);
            }
        }
        for (int i = 0; i < theDiagram.discNumber(); i++) {
            if (i!= theDiagram.getIgnore()) {
                for (int u = 0; u < theDiagram.getDisc(i).size(); u++) {
                    Integer[] dentry = theDiagram.getDisc(i).get(u);
                    Edge edge = createEdge(dentry[0],i,0,2,complex.vertices);
                    complex.edges.add(edge);
                    edge = createEdge(link.getPath(dentry[0],dentry[1]),i,1,2,complex.vertices);
                    complex.edges.add(edge);
                    Triangle triang = createTriangle(dentry[0],link.getPath(dentry[0],dentry[1]),i,complex.vertices,true);
                    complex.triangles.add(triang);
                    triang.fvert.rose.add(triang);
                    triang.svert.rose.add(triang);
                    triang.tvert.rose.add(triang);
                    if (u < theDiagram.getDisc(i).size()-1) {
                        Integer[] nentry = theDiagram.getDisc(i).get(u+1);
                        triang = createTriangle(nentry[0],link.getPath(dentry[0],dentry[1]),i,complex.vertices,false);
                        complex.triangles.add(triang);
                        triang.fvert.rose.add(triang);
                        triang.svert.rose.add(triang);
                        triang.tvert.rose.add(triang);
                    }
                }
                Integer[] fentry = theDiagram.getDisc(i).get(0);
                Integer[] lentry = theDiagram.getDisc(i).get(theDiagram.getDisc(i).size()-1);
                Triangle triang = createTriangle(fentry[0],link.getPath(lentry[0],lentry[1]),i,complex.vertices,false);
                complex.triangles.add(triang);
                triang.fvert.rose.add(triang);
                triang.svert.rose.add(triang);
                triang.tvert.rose.add(triang);
            }
        }// */
        fixVertices(complex,theDiagram.getDisc(theDiagram.getIgnore()));
        return complex;
    }
    
    private void fixVertices(SComplex complex, ArrayList<Integer[]> disc) {
        ArrayList<Integer> pathlabels = edgesOf(disc);
        ArrayList<Integer> dotlabels = dotsOf(disc);
        for (Vertex vert : complex.vertices) {
            if (vert.type == 0 && dotlabels.contains(vert.label)) {
                vert.fixed = true;
            }
            if (vert.type == 1 && pathlabels.contains(vert.label)) {
                vert.fixed = true;
            }
        }
    }
    
    private ArrayList<Integer> edgesOf(ArrayList<Integer[]> disc) {
        ArrayList<Integer> edges = new ArrayList<Integer>(disc.size());
        for (Integer[] dentry : disc) {
            edges.add(link.getPath(dentry[0],dentry[1]));
        }
        return edges;
    }
    
    private ArrayList<Integer> dotsOf(ArrayList<Integer[]> disc) {
        ArrayList<Integer> edges = new ArrayList<Integer>(disc.size());
        for (Integer[] dentry : disc) {
            edges.add(dentry[0]);
        }
        return edges;
    }
    
    private Triangle createTriangle(int lab1, int lab2, int lab3, ArrayList<Vertex> vertices, boolean ro) {
        boolean found = false;
        int i = 0;
        while (!found) {
            Vertex vert = vertices.get(i);
            if (vert.label == lab1 && vert.type == 0) found = true;
            else i++;
        }
        found = false;
        int j = 0;
        while (!found) {
            Vertex vert = vertices.get(j);
            if (vert.label == lab2 && vert.type == 1) found = true;
            else j++;
        }
        found = false;
        int k = 0;
        while (!found) {
            Vertex vert = vertices.get(k);
            if (vert.label == lab3 && vert.type == 2) found = true;
            else k++;
        }
        Triangle triang = new Triangle(vertices.get(i),vertices.get(j),vertices.get(k),ro);
        return triang;
    }
    
    private Edge createEdge(int lab1, int lab2, int typ1, int typ2, ArrayList<Vertex> vertices) {
        boolean found = false;
        int i = 0;
        while (!found) {
            Vertex vert = vertices.get(i);
            if (vert.label == lab1 && vert.type == typ1) found = true;
            else i++;
        }
        found = false;
        int j = 0;
        while (!found) {
            Vertex vert = vertices.get(j);
            if (vert.label == lab2 && vert.type == typ2) found = true;
            else j++;
        }
        Edge edge = new Edge(vertices.get(i),vertices.get(j));
        return edge;
    }
    
    private int getComp(int u, ArrayList<ArrayList<Integer>> splitComps) {
        int v = 0;
        boolean found = false;
        while (!found) {
            if (splitComps.get(v).contains(u)) found = true;
            else v++;
        }
        return v;
    }
    
    private void improve(SComplex complex) {
        double complexError = error(complex);
        while (Math.abs(complexError) > error) {
            change(complex);
            complexError = error(complex);
            int precis = precisionOf(Math.abs(complexError)); 
            frame.infoLabel.setText(" "+precis+" %");
        }
    }
    
    private int precisionOf(double err) {
        int i = 0;
        double comp = error;
        while (err > comp) {
            comp = comp * 10;
            i++;
        }
        int pre = 100 - (100/precision) * i;
        if (pre < 0) pre = 0;
        return pre;
    }
    
    private void change(SComplex complex) {
        for (Vertex vert : complex.vertices) {
            if (!vert.fixed) {
                double fact = (double) 2* vert.rose.size();
                double beta = Math.sin(angleSum(vert)/fact);
                double delta = Math.sin(tau/fact);
                double hatv = vert.r*beta/(1-beta);
                vert.r = hatv*(1-delta)/delta;
            }
        }
    }
    
    private double error(SComplex complex) {
        double err = 0;
        for (Vertex vert : complex.vertices) {
            if (!vert.fixed) err = err + Math.abs(angleSum(vert) - tau);
        }
        return err;
    }
    
    private double angleValue(Vertex first, Vertex sec, Vertex thi) {
        double root = Math.sqrt(sec.r*thi.r/((first.r+sec.r)*(first.r+thi.r)));
        return 2*Math.asin(root);
    }
    
    private double angleSum(Vertex vert) {
        double sum = 0;
        int theCase = vert.type;
        for (Triangle triang : vert.rose) {
            if (theCase == 0) sum = sum + angleValue(vert,triang.svert,triang.tvert);
            if (theCase == 1) sum = sum + angleValue(vert,triang.fvert,triang.tvert);
            if (theCase == 2) sum = sum + angleValue(vert,triang.fvert,triang.svert);
        }
        return sum;
    }

    /*private SComplex combineComplexes(SComplex[] theComplexes) {
        int i = theComplexes.length+link.unComponents();
        int a = (int) Math.sqrt(i);
        int b = a;
        while (a * b < i) b++;
        double maxy = 5;
        for (SComplex complex : theComplexes) if (complex.maxy > maxy) maxy = complex.maxy;
        SComplex complex = new SComplex();
        int c = 0;
        int d = 0;
        for (SComplex aComplex : theComplexes) {
            for (Vertex vert : aComplex.vertices) {
                vert.x = vert.x + 20 * d;
                vert.y = vert.y + maxy * c;
                complex.vertices.add(vert);
            }
            for (Edge edge : aComplex.edges) complex.edges.add(edge);
            for (Triangle tria : aComplex.triangles) complex.triangles.add(tria);
            d++;
            if (d == b) {
                d = 0;
                c++;
            }
        }
        if (maxy <= 10) maxy = 20;
        for (int j = 0; j < link.unComponents(); j++) {
            Vertex vert = new Vertex(20 * d + 10, maxy *c + maxy/2, 3, j, true);
            vert.r = 7;
            d++;
            if (d == b) {
                d = 0;
                c++;
            }
            complex.vertices.add(vert);
        }
        complex.maxx = 20 * b;
        complex.maxy = maxy * a;
        return complex;
    }// */

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        /*if (true) return;
        if (gDiag.highLight) {
            highLight = gDiag.highLighted;
            highLabel = gDiag.theComplex.vertices.get(gDiag.highLighted).label;
            colors = gDiag.getColors();
            orients = gDiag.getOrientComponents();
            shows = gDiag.getShownComponents();
            //Container c = frame.getContentPane();
            //c.remove(2);
            //c.add(frame.infoPanel);
            frame.repaint();
            recalc = true;
        }// */
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        /*
        if (true) return;
        
        int posX = e.getX();
        int posY = e.getY();
        int shiftX = scroller.getHorizontalScrollBar().getValue();
        int shiftY = scroller.getVerticalScrollBar().getValue();
        boolean found = false;
        for (Vertex vert : gDiag.theComplex.vertices) {
            if (vert.type == 2) {
                int x = gDiag.originx - shiftX + (int) (gDiag.factorx * vert.x);
                int y = gDiag.originy -shiftY + (int) (gDiag.factory * vert.y);
                int distance = Math.abs(posX-x)+Math.abs(posY-y);
                if (distance < (gDiag.factory * vert.r)) {
                    gDiag.highLight = true;
                    found = true;
                    gDiag.highLighted = gDiag.theComplex.vertices.indexOf(vert);
                    gDiag.repaint();
                }
            }
        }
        if (!found && gDiag.highLight) {
            gDiag.highLight = false;
            gDiag.repaint();
        }// */
    }
}
