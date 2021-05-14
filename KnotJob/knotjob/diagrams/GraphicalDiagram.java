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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import javax.swing.JComponent;
import knotjob.links.Link;

/**
 *
 * @author Dirk
 */
public class GraphicalDiagram extends JComponent {
    
    protected final SComplex theComplex;
    protected final int originx;
    protected final int originy;
    private final int drawnComp;
    public double factorx;
    public double factory;
    private final Link theLink;
    private ArrayList<Color> colors;
    private ArrayList<Boolean> showComps;
    private ArrayList<Boolean> orientComps;
    private boolean circlePacked;
    private final boolean useoCs;
    private final boolean usesCs; 
    public boolean highLight;
    public int highLighted;
    
    public GraphicalDiagram(SComplex complex, double facx, double facy, Link link, int dc, boolean cp, ArrayList<Color> clrs,
            ArrayList<Boolean> oCs, ArrayList<Boolean> sCs) {
        circlePacked = cp;
        drawnComp = dc;
        theComplex = complex;
        factorx = facx;
        factory = facy;
        originx = 50;
        originy = 30;
        theLink = link;
        highLight = false;
        useoCs = (oCs != null);
        usesCs = (sCs != null);
        if (clrs == null) {
            colors = new ArrayList<Color>();
            int un = link.unComponents();
            colors.add(Color.BLACK);
            for (int j = 1; j <= dc+un-1; j++) {
                int factor = 50 + (j * 150)/(dc+un-1) ;
                colors.add(new Color(factor+ 256 * factor + 256 * 256 * factor));
            }
        }
        else colors = clrs;
        showComps = new ArrayList<Boolean>();
        orientComps = new ArrayList<Boolean>();
        for (int i = 0; i < colors.size(); i++) {
            if (sCs != null) showComps.add(sCs.get(i));
            else showComps.add(true);
            if (oCs != null) orientComps.add(oCs.get(i));
            else orientComps.add(false);
        }
    }
    
    public ArrayList<Color> getColors() {
        return colors;
    }
    
    public ArrayList<Boolean> getShownComponents() {
        return showComps;
    }
    
    public ArrayList<Boolean> getOrientComponents() {
        return orientComps;
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setBackground(Color.WHITE);
        if (circlePacked) drawCirclePacking(g2);
        if (highLight) drawHighLightedCircle(g2);
        for (Vertex vert : theComplex.vertices) {
            if (vert.type == 3) {
                int i = drawnComp+vert.label;
                Color col = colors.get(i);
                boolean draw = showComps.get(i);
                boolean orie = orientComps.get(i);
                if (draw) drawCircle(vert,g2,col,orie);
            }
            if (vert.type == 1) {
                Edge edge0 = vert.comb.get(0);
                Edge edge1 = vert.comb.get(1);
                int m = theLink.compOf(vert.label);
                if (m < drawnComp && showComps.get(m)) {
                    Color col = colors.get(m);
                    drawInCircle(edge0.fvert,edge1.fvert,vert,g2,false,col);
                }
            }
            if (vert.type == 0) {
                Edge edge0 = vert.comb.get(0);
                Edge edge1 = vert.comb.get(1);
                Edge edge2 = vert.comb.get(2);
                Edge edge3 = vert.comb.get(3);
                int cross = theLink.getCross(vert.label);
                if (cross == -1) {
                    int m = theLink.compOf(edge0.svert.label);
                    if (m < drawnComp && showComps.get(m)) {
                        Color col = colors.get(m);
                        drawInCircle(edge0.svert,edge2.svert,vert,g2,false,col);
                    }
                    m = theLink.compOf(edge1.svert.label);
                    if (m < drawnComp && showComps.get(m)) {
                        Color col = colors.get(m);
                        drawInCircle(edge1.svert,edge3.svert,vert,g2,true,col);
                    }
                }
                if (cross == 1) {
                    int m = theLink.compOf(edge1.svert.label);
                    if (m < drawnComp && showComps.get(m)) {
                        Color col = colors.get(m);
                        drawInCircle(edge1.svert,edge3.svert,vert,g2,false,col);
                    }
                    m = theLink.compOf(edge0.svert.label);
                    if (m < drawnComp && showComps.get(m)) {
                        Color col = colors.get(m);
                        drawInCircle(edge0.svert,edge2.svert,vert,g2,true,col);
                    }
                }
            }
        }
        for (int i = 0; i < drawnComp; i++) if (orientComps.get(i) && showComps.get(i)) drawOrientation(i,g2);  
    }
    
    public void setCirclePacked(boolean setter) {
        circlePacked = setter;
    }
    
    private void drawCirclePacking(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        for (Vertex vert : theComplex.vertices) {
            if (vert.type == 0) g.setColor(Color.BLUE);
            if (vert.type == 1) g.setColor(Color.RED);
            if (vert.type == 2) g.setColor(Color.YELLOW);
            int x = originx + (int) (factorx * (vert.x-vert.r));
            int y = originy + (int) (factory * (vert.y-vert.r));
            g.drawOval(x,y,(int) (2 * factorx * vert.r),(int) (2 * factory * vert.r));
        }
    }

    private void drawHighLightedCircle(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.YELLOW);
        Vertex vert = theComplex.vertices.get(highLighted);
        int x = originx + (int) (factorx * (vert.x-vert.r));
        int y = originy + (int) (factory * (vert.y-vert.r));
        g.fillOval(x,y,(int) (2 * factorx * vert.r),(int) (2 * factory * vert.r));
    }
    
    private void drawCircle(Vertex vert, Graphics2D g, Color col, boolean orient) {
        g.setStroke(new BasicStroke(3));
        g.setColor(col);
        int x = originx + (int) (factorx * (vert.x-vert.r));
        int y = originy + (int) (factory * (vert.y-vert.r));
        g.drawOval(x,y,(int) (2 * factorx * vert.r),(int) (2 * factory * vert.r));
        if (!orient) return;
        int[] xPoints = new int[3];
        int[] yPoints = new int[3];
        xPoints[1] = originx + (int) (factorx * (vert.x+vert.r));
        yPoints[1] = originy + (int) (factory * vert.y);
        xPoints[0] = xPoints[1] - (int) (factorx * 0.35);
        yPoints[0] = yPoints[1] + (int) (factory * 0.5);
        xPoints[2] = xPoints[0] + (int) (factorx * 0.7);
        yPoints[2] = yPoints[0];
        g.drawPolyline(xPoints, yPoints, 3);
    }

    private void drawInCircle(Vertex fvert, Vertex svert, Vertex cvert, Graphics2D g, boolean shadow, Color col) {
        double tau0 = fvert.r/(fvert.r+cvert.r);
        double tau1 = svert.r/(svert.r+cvert.r);
        double x0 = (1-tau0) * fvert.x + tau0 * cvert.x;
        double y0 = (1-tau0) * fvert.y + tau0 * cvert.y;
        double x1 = (1-tau1) * svert.x + tau1 * cvert.x;
        double y1 = (1-tau1) * svert.y + tau1 * cvert.y;
        if (shadow) {
            g.setColor(g.getBackground());
            g.setStroke(new BasicStroke(11));
            drawBezier(x0,y0,cvert.x,cvert.y,x1,y1,g);
        }
        g.setColor(col);
        g.setStroke(new BasicStroke(3));
        drawBezier(x0,y0,cvert.x,cvert.y,x1,y1,g);
    }
    
    private void drawBezier(double x0, double y0, double x1, double y1, double x2, double y2,Graphics2D g) {
        x0 = originx + factorx * x0;
        y0 = originy + factory * y0;
        x1 = originx + factorx * x1;
        y1 = originy + factory * y1;
        x2 = originx + factorx * x2;
        y2 = originy + factory * y2;
        QuadCurve2D curve = new QuadCurve2D.Double(x0,y0,x1,y1,x2,y2);
        g.draw(curve);
    }

    private void drawOrientation(int i, Graphics2D g) {
        int[] orient = theLink.orientation(i);
        g.setColor(colors.get(i));
        g.setStroke(new BasicStroke(3));
        Vertex vert0 = getVertex(orient[0],0);
        Vertex vert1 = getVertex(theLink.getPath(orient[0], orient[1]),1);
        double[] xx = new double[3];
        double[] yy = new double[3];
        xx[1] = vert1.x + (vert0.x - vert1.x) * vert1.r / (vert0.r+vert1.r);
        yy[1] = vert1.y + (vert0.y - vert1.y) * vert1.r / (vert0.r+vert1.r);
        xx[0] = xx[1] - 0.5;
        yy[0] = yy[1] - 0.35;
        xx[2] = xx[1] - 0.5;
        yy[2] = yy[0] + 0.7;
        rotate(xx, yy, vert0.x - vert1.x, vert0.y - vert1.y);
        int[] x = integerArray(originx, factorx, xx);
        int[] y = integerArray(originy, factory, yy);
        g.drawPolyline(x, y, 3);
    }
    
    private void rotate(double[] x, double[] y, double cs, double si) {
        double l = Math.sqrt(cs*cs+si*si);
        double c = cs / l;
        double s = si / l;
        double nx = c * (x[0]-x[1]) - s * (y[0]-y[1]);
        double ny = s * (x[0]-x[1]) + c * (y[0]-y[1]);
        x[0] = nx + x[1];
        y[0] = ny + y[1];
        nx = c * (x[2]-x[1]) - s * (y[2]-y[1]);
        ny = s * (x[2]-x[1]) + c * (y[2]-y[1]);
        x[2] = nx + x[1];
        y[2] = ny + y[1];
    }
    
    private int[] integerArray(int orig, double fac, double[] ar) {
        int[] in = new int[ar.length];
        for (int i = 0; i < ar.length; i++) {
            in[i] = orig + (int) (fac * ar[i]);
        }
        return in;
    }
    
    private Vertex getVertex(int la, int ty) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (theComplex.vertices.get(i).type == ty && theComplex.vertices.get(i).label == la) found = true;
            else i++;
        }
        return theComplex.vertices.get(i);
    }
    
    public void setColors(ArrayList<Color> setColors) {
        colors = setColors;
    }
    
    public void setShownComponents(ArrayList<Boolean> setShows) {
        showComps = setShows;
    }
    
    public void setOrientComponents(ArrayList<Boolean> setOrs) {
        orientComps = setOrs;
    }
    
}
