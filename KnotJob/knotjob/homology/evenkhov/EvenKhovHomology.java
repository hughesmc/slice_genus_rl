/*

Copyright (C) 2020 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob.homology.evenkhov;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.ChainComplex;
import knotjob.homology.HomologyInfo;
import knotjob.homology.QuantumCohomology;
import knotjob.links.Link;
import knotjob.links.LinkData;
import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class EvenKhovHomology <R extends Ring<R>> {
    
    private final Link theLink;
    private final long coeff;
    private final DialogWrap frame;
    private final AbortInfo abInf;
    private final Options options;
    private final boolean unred;
    private final boolean red;
    private final int[] girth;
    private final R unit;
    private final R thePrime;
    private final ArrayList<String> endunredHom;
    private final ArrayList<String> endredHom;
    private final boolean highDetail;
    
    public EvenKhovHomology(LinkData link, long cff, DialogWrap frm, boolean unrd, boolean rd,
            Options optns, R unt, R prime) {
        theLink = link.chosenLink();
        girth = theLink.totalGirthArray();
        coeff = cff;
        frame = frm;
        unred = unrd;
        red = rd;
        abInf = frame.getAbortInfo();
        options = optns;
        unit = unt;
        thePrime = prime;
        endunredHom = new ArrayList<String>();
        endredHom = new ArrayList<String>();
        highDetail = optns.getGirthInfo() == 2;
    }

    public void calculate() {
        int[] wrt = theLink.crossingSigns();
        int hstart = -wrt[1];
        int qstart = wrt[0]+2*hstart;
        if (coeff == 0) calculateIntegral(hstart,qstart);
        if (coeff == 1) calculateRational(hstart,qstart);
        if (coeff >  1) calculateModular(hstart,qstart);
        if (coeff <  0) calculateLocalized(hstart,qstart);
    }

    public ArrayList<String> getReduced() {
        return endredHom;
    }
    
    public ArrayList<String> getUnreduced() {
        return endunredHom;
    }

    private void calculateIntegral(int hstart, int qstart) {
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = smithNormalize(theComplex, new int[0]);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateRational(int hstart, int qstart) {
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = finishUp(theComplex);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateModular(int hstart, int qstart) {
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = modNormalize(theComplex);
        if (finalInfo != null) finishOff(finalInfo);
    }

    private void calculateLocalized(int hstart, int qstart) {
        ArrayList<Integer> prms = EvenKhovCalculator.getPrimes(coeff, options.getPrimes());
        int[] primes = new int[prms.size()];
        for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
        EvenComplex<R> theComplex = getComplex(hstart, qstart);
        ArrayList<String> finalInfo = smithNormalize(theComplex, primes);
        if (finalInfo != null) finishOff(finalInfo);
    }
    
    private EvenComplex<R> getComplex(int hstart, int qstart) {
        if (theLink.crossingLength() == 0) return new EvenComplex<R>(0, unit, unred, red, abInf, null);
        EvenComplex<R> theComplex;
        if (theLink.crossingLength() == 1) theComplex = oneCrossingComplex(hstart ,qstart);
        else {
            theComplex = firstComplex(hstart, qstart);
            int u = 1;
            int d = 0;
            if (red) d = 1;
            while (u < theLink.crossingLength() - d && !abInf.isAborted()) {
                boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
                    theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
                EvenComplex<R> nextComplex = new EvenComplex<R>(theLink.getCross(u),theLink.getPath(u),0,0,orient,false,unred,
                        false, unit,null,null);
                frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0);
                theComplex.modifyComplex(nextComplex,0,girthInfo(u), highDetail);
                u++;
            }
            if (red && !abInf.isAborted()) theComplex = lastComplex(theComplex, u);
        }
        return theComplex;
    }
    
    private ArrayList<String> finishUp(EvenComplex<R> theComplex) { // this is only okay for fields.
        if (abInf.isAborted()) return null;
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        frame.setLabelLeft("Quantum degree : ", 0);
        frame.setLabelLeft("Homological degree : ", 1);
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0);
            ChainComplex<R> qComplex = theComplex.getQComplex(q);
            if (!qComplex.boundaryCheck()) System.out.println("Boundary Error");
            QuantumCohomology qCoh = new QuantumCohomology(q, qComplex.obtainBettis());
            if (abInf.isCancelled()) return null;
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms);
    }
    
    private ArrayList<String> modNormalize(EvenComplex<R> theComplex) {
        if (abInf.isAborted()) return null;
        if (highDetail) {
            frame.setLabelLeft(" ", 3);
            frame.setLabelRight(" ", 3);
        }
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        frame.setLabelLeft("Quantum degree : ", 0);
        frame.setLabelLeft("Homological degree : ", 1);
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0);
            ChainComplex<R> qComplex = theComplex.getQComplex(q);
            if (!qComplex.boundaryCheck()) System.out.println("Boundary Error "+q);
            if (abInf.isCancelled()) return null;
            QuantumCohomology qCoh = new QuantumCohomology(q, qComplex.modNormalize(thePrime));
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms);
    }
    
    private ArrayList<String> smithNormalize(EvenComplex<R> theComplex, int[] primes) {
        if (abInf.isAborted()) return null;
        if (highDetail) {
            frame.setLabelLeft(" ", 3);
            frame.setLabelRight(" ", 3);
        }
        ArrayList<Integer> relevantQs = theComplex.getQs();
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        frame.setLabelLeft("Quantum degree : ", 0);
        frame.setLabelLeft("Homological degree : ", 1);
        for (int q : relevantQs) {
            frame.setLabelRight("" + q, 0);
            ChainComplex<R> qComplex = theComplex.getQComplex(q);
            if (!qComplex.boundaryCheck()) System.out.println("Boundary Error "+q);
            if (abInf.isCancelled()) return null;
            QuantumCohomology qCoh = new QuantumCohomology(q, qComplex.smithNormalize(primes));
            cohoms.add(qCoh);
        }
        return reduceInformation(cohoms);
    }

    private ArrayList<String> reduceInformation(ArrayList<QuantumCohomology> cohoms) {
        if (theLink.unComponents() > 0) {
            HomologyInfo homInfo = new HomologyInfo(0l, 1, 1, cohoms);
            int u = theLink.unComponents();
            while (u > 0) {
                homInfo = homInfo.doubleHom();
                u--;
            }
            cohoms = homInfo.getHomologies();
        }
        ArrayList<String> theStrings = new ArrayList<String>();
        for (QuantumCohomology currCoh : cohoms) theStrings.add(currCoh.toString());
        return theStrings;
    }
    
    private void finishOff(ArrayList<String> finalInfo) {
        int add = 0;
        if (theLink.components() % 2 == 0) add++;
        for (String info : finalInfo) {
            if (!info.contains("x")) {
                if (Math.abs(quantum(info)) % 2 == add) endredHom.add(info);
                else endunredHom.add(info);
            }
        }
        if (!endredHom.isEmpty()) lastLine(true,endredHom);
        if (!endunredHom.isEmpty()) lastLine(false,endunredHom);
    }
    
    private void lastLine(boolean reduced, ArrayList<String> endHom) {
        String last = "u"+coeff+".";
        if (reduced) last = "r"+coeff+".";
        for (String info : endHom) {
            if (info.contains("aborted")) last = last+"a"+quantum(info)+".";
        }
        if (reduced) last = last+theLink.basecomponent()+"c.";
        endHom.add(0,last);
    }
    
    private int quantum(String info) {
        int e = info.indexOf('h');
        if (e == -1) e = info.indexOf('a');
        return Integer.parseInt(info.substring(1, e));
    }
    
    private EvenComplex<R> oneCrossingComplex(int hs, int qs) {
        EvenComplex<R> theComplex = new EvenComplex<R>(theLink.getCross(0), theLink.getPath(0),hs,qs,false,false,unred,red,
                unit, frame, abInf);
        EvenComplex<R> unComp = new EvenComplex<R>(1, unit, false,true, abInf, frame);
        unComp.modifyComplex(theComplex,reducer(), " ", highDetail);
        return unComp;
    }
    
    private EvenComplex<R> firstComplex(int hs, int qs) {
        EvenComplex<R> theComplex = 
                new EvenComplex<R>(theLink.getCross(0), theLink.getPath(0),hs,qs,false,false,true,false,unit,frame,abInf);
        if (theComplex.posNumber() == 2) return theComplex;
        EvenComplex<R> unComp = new EvenComplex<R>(1, unit, false, true, abInf, frame);
        unComp.modifyComplex(theComplex,0," ", highDetail);
        return unComp;
    }
    
    private EvenComplex<R> lastComplex(EvenComplex<R> theComplex, int u) {
        boolean orient = (theComplex.negContains(theLink.getPath(u, 0))| theComplex.negContains(theLink.getPath(u, 2))|
            theComplex.posContains(theLink.getPath(u,1)) | theComplex.posContains(theLink.getPath(u,3)));
        EvenComplex<R> nextComplex = new EvenComplex<R>(theLink.getCross(u),theLink.getPath(u),0,0,orient,false,unred,
                false,unit,null,null);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0);
        int c = 1+theLink.getPath(u, theLink.basepoint());
        if (unred) c = -c;
        theComplex.modifyComplex(nextComplex,c,"0", highDetail);
        return theComplex;
    }
    
    private int reducer() {
        int ucer = 0;
        if (red) ucer = 1+theLink.getPath(0, theLink.basepoint());
        if (unred) ucer = -ucer;
        return ucer;
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        if (!highDetail) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }
}
