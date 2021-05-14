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

package knotjob.homology;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import knotjob.polynomial.Coefficient;
import knotjob.polynomial.PoincarePolynomial;

/**
 *
 * @author Dirk
 */
public class HomologyInfo implements Comparable<HomologyInfo> {
    
    private long prime;
    private final int power;
    private final int maxpower;
    private final ArrayList<QuantumCohomology> homologies;

    public HomologyInfo(Long pr, Integer pw) {
        prime = pr;
        power = pw;
        maxpower = pw;
        homologies = new ArrayList<QuantumCohomology>();
    }

    public HomologyInfo(Long pr, int pw, int mpw, ArrayList<QuantumCohomology> homs) {
        prime = pr;
        power = pw;
        maxpower = mpw;
        homologies = homs;
    }
    
    public HomologyInfo(ArrayList<String> infos, Long pr, Integer pw) {
        prime = pr;
        power = pw;
        maxpower = pw;
        homologies = new ArrayList<QuantumCohomology>();
        for (String info : infos) homologies.add(new QuantumCohomology(info));
    }
    
    @Override
    public int compareTo(HomologyInfo o) {
        return (int) (prime - o.prime);
    }

    public void addCohomology(QuantumCohomology coh) {
        homologies.add(coh);
    }
    
    public void adjustBetti(HomologyInfo info) {
        for (QuantumCohomology coh : homologies) {
            QuantumCohomology theQ = info.theCohomology(coh.qdeg(), false);
            if (theQ == null) {
                int i = coh.getHomGroups().size()-1;
                while (i>=0) {
                    Homology hom = coh.getHomGroups().get(i);
                    hom.setBetti(0);
                    if (hom.getTorsion().isEmpty()) coh.getHomGroups().remove(i);
                    i--;
                }
            }
            else {
                int i = coh.getHomGroups().size()-1;
                while (i>=0) {
                    Homology hom = coh.getHomGroups().get(i);
                    Homology hm = theQ.findHomology(hom.hdeg(),false);
                    if (hm == null) {
                        hom.setBetti(0);
                        if (hom.getTorsion().isEmpty()) coh.getHomGroups().remove(i);
                    }
                    else {
                        if (hom.getBetti()>hm.getBetti()) {
                            hom.setBetti(hm.getBetti());
                        }
                        if (hom.getBetti() == 0 && hom.getTorsion().isEmpty()) coh.getHomGroups().remove(hom);
                    }
                    i--;
                }
            }
        }
    }
    
    public boolean compareBetti(HomologyInfo hInfo) {
        boolean diff = false;
        int i = 0;
        while (!diff && i < hInfo.getHomologies().size()) {
            QuantumCohomology coh = hInfo.getHomologies().get(i);
            QuantumCohomology theQ = theCohomology(coh.qdeg(),false);
            if (theQ == null) diff = true;
            else {
                for (Homology hom : coh.getHomGroups()) {
                    if (hom.getBetti()>0) {
                        Homology hm = theQ.findHomology(hom.hdeg(), false);
                        if (hm == null) diff = true;
                        else if (hm.getBetti()!=hom.getBetti()) diff = true;
                    }
                }
            }
            i++;
        }
        return diff;
    }
    
    public void setBetti(QuantumCohomology coh) {
        int q = coh.qdeg();
        QuantumCohomology theQ = theCohomology(q,true);
        for (Homology hom : coh.getHomGroups()) {
            int h = hom.hdeg();
            Homology hm = theQ.findHomology(h,true);
            hm.setBetti(hom.getBetti());
        }
    }
    
    public void addTorsion(QuantumCohomology coh, ArrayList<Integer> onlyPrimes) {
        int q = coh.qdeg();
        QuantumCohomology theQ = theCohomology(q,true);
        for (Homology hom : coh.getHomGroups()) {
                if (!hom.getTorsion().isEmpty()) {
                int h = hom.hdeg();
                Homology hm = theQ.findHomology(h,true);
                for (BigInteger biggy : hom.getTorsion()) {
                    if (torsionOk(biggy,onlyPrimes)) hm.addTorsion(biggy);
                }
                Collections.sort(hm.getTorsion());
            }
        }
    }
    
    public HomologyInfo mirror() {
        HomologyInfo mirrorInfo = new HomologyInfo(prime,power);
        for (int i = homologies.size()-1; i>= 0; i--) {
            QuantumCohomology qCoh = homologies.get(i);
            QuantumCohomology qMir = new QuantumCohomology(-qCoh.qdeg());
            boolean first = true;
            for (int j = qCoh.getHomGroups().size()-1; j>=0; j--) {
                Homology hOrg = qCoh.getHomGroups().get(j);
                Homology hMir = new Homology(-hOrg.hdeg(),hOrg.getBetti());
                if (!first) {
                    Homology hPre = qCoh.getHomGroups().get(j+1);
                    if (hPre.hdeg() == hOrg.hdeg()+1) hMir.addTorsion(hPre.getTorsion());
                    else if (!hPre.getTorsion().isEmpty()) {
                        Homology hInb = new Homology(-hPre.hdeg()+1,0);
                        hInb.addTorsion(hPre.getTorsion());
                        qMir.addHomology(hInb);
                    }
                    qMir.addHomology(hMir);
                }
                else if (hMir.getBetti()>0) qMir.addHomology(hMir);
                first = false;
            }
            Homology hOrg = qCoh.getHomGroups().get(0);
            if (!hOrg.getTorsion().isEmpty()) {
                Homology hLas = new Homology(-hOrg.hdeg()+1,0);
                hLas.addTorsion(hOrg.getTorsion());
                qMir.addHomology(hLas);
            }
            mirrorInfo.addCohomology(qMir);
        }
        return mirrorInfo;
    }
    
    private boolean torsionOk(BigInteger p, ArrayList<Integer> primes) {
        boolean okay = false;
        int i = 0;
        while (!okay && i < primes.size()) {
            int op = primes.get(i);
            if (p.mod(BigInteger.valueOf(op)).equals(BigInteger.ZERO)) okay = true;
            else i++;
        }
        return okay;
    }
    
    private QuantumCohomology theCohomology(int q, boolean add) {
        QuantumCohomology theQ;
        boolean found = false;
        int i = 0;
        while (!found && i < homologies.size()) {
            if (homologies.get(i).qdeg() == q) found = true;
            else i++;
        }
        if (!found) {
            if (!add) return null;
            theQ = new QuantumCohomology(q);
            homologies.add(theQ);
            Collections.sort(homologies);
        }
        else theQ = homologies.get(i);
        return theQ;
    }
    
    private boolean tooBig(int q) {
        boolean found = false;
        int i = 0;
        while (!found && i < homologies.size()) {
            if (homologies.get(i).qdeg() >= q) found = true;
            else i++;
        }
        return !found;
    }
    
    public ArrayList<QuantumCohomology> getHomologies() {
        return homologies;
    }
    
    public int getMaxpower() {
        return maxpower;
    }
    
    public int getPower() {
        return power;
    }
    
    public long getPrime() {
        return prime;
    }
    
    public void setPrime(long p) {
        prime = p;
    }

    public int width() {
        int width = 0;
        for (QuantumCohomology quant : homologies) {
            int qwidth = quant.width();
            if (qwidth > width) width = qwidth;
        }
        return width;
    }
    
    public HomologyInfo doubleHom() {
        ArrayList<QuantumCohomology> newHoms = new ArrayList<QuantumCohomology>();
        if (homologies.isEmpty()) return this;
        QuantumCohomology firstOne = homologies.get(0);
        int q = firstOne.qdeg()-1;
        newHoms.add(new QuantumCohomology(q, firstOne.getHomGroups()));
        while (!tooBig(q)) {
            q = q+2;
            QuantumCohomology nextOne = theCohomology(q+1, false);
            if (nextOne != null || firstOne != null) {
                QuantumCohomology newOne = null;
                if (nextOne != null) {
                    newOne = new QuantumCohomology(q, nextOne.getHomGroups());
                    if (firstOne != null) {
                        for (Homology hom : firstOne.getHomGroups()) {
                            Homology newH = newOne.findHomology(hom.hdeg(), true);
                            newH.setBetti(newH.getBetti()+hom.getBetti());
                            newH.addTorsion(hom.getTorsion());
                        }
                    }
                }
                else if (firstOne != null) newOne = new QuantumCohomology(q, firstOne.getHomGroups());
                newHoms.add(newOne);
            }
            firstOne = nextOne;
        }
        return new HomologyInfo(prime, power, maxpower, newHoms);
    }
    
    public PoincarePolynomial poincarePolynomial() {
        PoincarePolynomial poly = new PoincarePolynomial(new String[] {"t", "q"}, 
                new ArrayList<Coefficient>(), BigInteger.ZERO);
        for (QuantumCohomology qcoh : homologies) {
            for (Homology hom : qcoh.getHomGroups()) {
                if (hom.getBetti() > 0) {
                    PoincarePolynomial ext = new PoincarePolynomial(new String[] {"t", "q"}, 
                            BigInteger.valueOf(hom.getBetti()), new int[] {hom.hdeg(), qcoh.qdeg()}, 
                            BigInteger.ZERO);
                    poly = poly.add(ext);
                }
            }
        }
        return poly;
    }
    
    public ArrayList<PoincarePolynomial> torsionPolynomials(boolean doubleTor) {
        ArrayList<PoincarePolynomial> polys = new ArrayList<PoincarePolynomial>();
        ArrayList<BigInteger> torsions = new ArrayList<BigInteger>();
        for (QuantumCohomology qcoh : homologies) {
            for (Homology hom : qcoh.getHomGroups()) {
                for (BigInteger tor : hom.getTorsion()) {
                    if (contains(torsions, tor)) {
                        int pos = positionOf(tor, torsions);
                        PoincarePolynomial poly = polys.get(pos);
                        PoincarePolynomial ext = standardTorPoly(hom.hdeg(), qcoh.qdeg(), tor, 
                                doubleTor);
                        polys.set(pos, poly.add(ext));
                    }
                    else {
                        torsions.add(tor);
                        polys.add(standardTorPoly(hom.hdeg(), qcoh.qdeg(), tor, doubleTor));
                    }
                }
            }
        }
        return polys;
    }
    
    private PoincarePolynomial standardTorPoly(int hdeg, int qdeg, BigInteger tor, 
            boolean doubleTor ) {
        PoincarePolynomial ext = new PoincarePolynomial(new String[] {"t", "q"}, 
                BigInteger.ONE, new int[] {hdeg, qdeg}, tor);
        if (doubleTor) ext = ext.add(new PoincarePolynomial(new String[] {"t", "q"}, 
                BigInteger.ONE, new int[] {hdeg -1, qdeg}, tor));
        return ext;
    }
    
    private boolean contains(ArrayList<BigInteger> torsions, BigInteger tor) {
        boolean con = false;
        int i = 0;
        while (!con && i < torsions.size()) {
            if (tor.equals(torsions.get(i))) con = true;
            else i++;
        }
        return con;
    }

    private int positionOf(BigInteger tor, ArrayList<BigInteger> torsions) {
        boolean found = false;
        int i = 0;
        while (!found) {
            if (tor.equals(torsions.get(i))) found = true;
            else i++;
        }
        return i;
    }
    
}
