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
import java.util.concurrent.locks.ReentrantLock;
import knotjob.Comparer;
import knotjob.Options;
import knotjob.homology.HomologyInfo;
import knotjob.homology.Homology;
import knotjob.homology.QuantumCohomology;
import knotjob.homology.evenkhov.EvenKhovCalculator;

/**
 *
 * @author Dirk
 */
public class LinkData implements Comparable<LinkData> {
    
    public String name;
    public String comment;
    public String sinvariant;
    public String sqEven;
    public String sqOdd;
    public String jones;
    public String torsion;
    public ArrayList<String> unredKhovHom;
    public ArrayList<String> redKhovHom;
    public ArrayList<String> khovInfo;
    public ArrayList<String> oddKhovHom;
    public ArrayList<String> okhovInfo;
    public ArrayList<Link> links;
    private final Comparer comparer;
    public final ReentrantLock reLock = new ReentrantLock();
    int chosen;

    public LinkData(String lname, Link theLink, Comparer cmp) {
        stuffToNull();
        name = lname;
        links = new ArrayList<Link>();
        links.add(theLink);
        comparer = cmp;
    }
    
    public LinkData(String fname, Comparer cmp) {
        links = new ArrayList<Link>();
        stuffToNull();
        name = fname;
        comparer = cmp;
    }
    
    private void stuffToNull() {
        name = null;
        comment = null;
        sinvariant = null;
        sqEven = null;
        sqOdd = null;
        jones = null;
        torsion = null;
        unredKhovHom = null;
        redKhovHom = null;
        khovInfo = null;
        oddKhovHom = null;
        okhovInfo = null;
    }
    
    public Link chosenLink() {
        return links.get(chosen);
    }
    
    public void setChosen(int i) {
        chosen = i;
    }
    
    public int chosen() {
        return chosen;
    }
    
    public boolean showOddKhovButton() {
        if (okhovInfo == null) return false;
        boolean found = false;
        int i = 0;
        while (!found && i < okhovInfo.size()) {
            String inf = okhovInfo.get(i);
            if (inf.charAt(0) == 'o' && inf.indexOf('a') == -1) found = true;
            else i++;
        }
        return found;
    }
    
    public boolean showKhovHomButton(boolean reduced) {
        if (khovInfo == null) return false;
        boolean found = false;
        char start = 'u';
        if (reduced) start = 'r';
        int i = 0;
        while (!found && i < khovInfo.size()) {
            String inf = khovInfo.get(i);
            if (inf.charAt(0) == start && inf.indexOf('a') == -1) found = true;
            else i++;
        }
        return found;
    }

    public boolean integralOddHomologyExists() {
        return (getStartEnd('o', 0, okhovInfo) != null);
    }

    public boolean rationalOddHomologyExists() {
        int[] rel = getStartEnd('o', 1, okhovInfo);
        if (rel == null) rel = getStartEnd('o', -1, okhovInfo);
        return (rel != null);
    }
    
    public boolean integralHomologyExists(boolean reduced) {
        char red = 'u';
        if (reduced) red = 'r';
        ArrayList<String> homStrings = khovInfo;
        return (getStartEnd(red, 0, homStrings)!= null);
    }
    
    public boolean rationalHomologyExists(boolean reduced) {
        char red = 'u';
        if (reduced) red = 'r';
        ArrayList<String> homStrings = khovInfo;
        int[] rel = getStartEnd(red, 1, homStrings);
        if (rel == null) rel = getStartEnd(red, -1, homStrings);
        return (rel != null);
    }
    
    public HomologyInfo theHomology(long[] special, ArrayList<String> homStrings) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        //ArrayList<String> homStrings = unredKhovHom;
        //if (reduced) homStrings = redKhovHom;
        for (int j = (int)special[2]; j < (int)special[3]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            cohoms.add(coh);
        }
        HomologyInfo hinfo = new HomologyInfo(special[0],(int)special[1]);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    public HomologyInfo approximateHomology(char reduced, ArrayList<Integer> primes, ArrayList<String> theInfo, 
            ArrayList<String> homStrings) {
        HomologyInfo approxInfo = new HomologyInfo(0l,1);
        approxInfo.setPrime(0l);
        ArrayList<HomologyInfo> minusInfos = new ArrayList<HomologyInfo>();
        ArrayList<HomologyInfo> plusInfos = new ArrayList<HomologyInfo>();
        ArrayList<Integer> powers = new ArrayList<Integer>();
        for (int p : primes) powers.add(0);
        ArrayList<String> relInfo = getRelevantInfo(reduced, theInfo);
        long[][] startInfo = getStartInfo(relInfo, primes);
        for (int i = 0; i < relInfo.size(); i++) {
            String info = relInfo.get(i);
            if (info.charAt(1)!='-') plusInfos.add(theHomology(startInfo[i], homStrings));
            else minusInfos.add(theHomology(startInfo[i], homStrings));
        }
        if (minusInfos.isEmpty() && plusInfos.isEmpty()) return null;
        for (HomologyInfo hInfo : minusInfos) {
            for (QuantumCohomology coh : hInfo.getHomologies()) {
                approxInfo.addTorsion(coh,onlyAvailable(primes,powers));
            }
            ArrayList<Integer> prmes = EvenKhovCalculator.getPrimes(hInfo.getPrime(), primes);
            for (int i = 0; i < primes.size(); i++) {
                if (!prmes.contains(primes.get(i))) powers.set(i, 1);
            }
        }
        boolean setBetti = !minusInfos.isEmpty();
        for (HomologyInfo hInfo : plusInfos) {
            int prime = (int) hInfo.getPrime();
            if (powers.get(primes.indexOf(prime)) == 0) {
                ArrayList<Integer> prmes = new ArrayList<Integer>(1);
                prmes.add(prime);
                if (!setBetti) approxInfo.adjustBetti(hInfo);
                for (QuantumCohomology coh : hInfo.getHomologies()) {
                    approxInfo.addTorsion(coh,prmes);
                    if (setBetti) approxInfo.setBetti(coh);
                }
                setBetti = false;
            }
        }
        return approxInfo;
    }
    
    private ArrayList<Integer> onlyAvailable(ArrayList<Integer> primes, ArrayList<Integer> powers) {
        ArrayList<Integer> availables = new ArrayList<Integer>();
        for (int i = 0; i < primes.size(); i++) {
            if (powers.get(i) == 0) availables.add(primes.get(i));
        }
        return availables;
    }
    
    private ArrayList<String> getRelevantInfo(char check, ArrayList<String> theInfo) {
        if (theInfo == null) return new ArrayList<String>();
        //char check = 'u';
        //if (reduced) check = 'r';
        ArrayList<String> rels = new ArrayList<String>();
        for (String checker : theInfo) {
            if (checker.charAt(0)==check && !"1.".equals(checker.substring(1,3))) rels.add(checker);
        }
        return rels;
    }
    
    public long[][] getStartInfo(ArrayList<String> relInfo, ArrayList<Integer> primes) {
        if (relInfo == null) return new long[0][0];
        long[][] theInfo = new long[relInfo.size()][4];
        for (int i = 0; i < relInfo.size(); i++) {
            long[] prime = primeAndPower(relInfo.get(i),primes);
            int[] start = startAndEnd(relInfo.get(i));
            theInfo[i][0] = prime[0];
            theInfo[i][1] = prime[1];
            theInfo[i][2] = start[0];
            theInfo[i][3] = start[1];
        }
        return theInfo;
    }
    
    private int[] startAndEnd(String info) {
        int[] sae = new int[2];
        int sp = info.lastIndexOf('.');
        int mp = info.lastIndexOf('-');
        sae[0] = Integer.parseInt(info.substring(sp+1, mp));
        sae[1] = Integer.parseInt(info.substring(mp+1));
        return sae;
    }
    
    private long[] primeAndPower(String info, ArrayList<Integer> primesOfInterest) {
        long[] pap = new long[2];
        boolean found = false;
        int i = 0;
        int end = info.indexOf(".");
        long ring = Long.parseLong(info.substring(1, end));
        if (ring < 2) {
            pap[0] = ring;
            pap[1] = 1;
            return pap;
        }
        int prime = 2;
        while (!found) {
            prime = primesOfInterest.get(i);
            if (ring%prime == 0) found = true;
            else i++;
        }
        int power = 0;
        while (ring%prime == 0) {
            ring = ring/prime;
            power++;
        }
        pap[0] = prime;
        pap[1] = power;
        return pap;
    }
    
    private HomologyInfo modularHomology(char reduced, ArrayList<String> homStrings, 
            ArrayList<String> theInfo, int p) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        int[] rel = getStartEnd(reduced, p, theInfo);
        if (rel == null) return null;
        for (int j = rel[0]; j < rel[1]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            cohoms.add(coh);
        }
        int[] data = primeAndPower(p);
        HomologyInfo hinfo = new HomologyInfo((long) data[0], data[1]);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    public HomologyInfo integralHomology(char reduced, ArrayList<String> homStrings, ArrayList<String> theInfo) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        int[] rel = getStartEnd(reduced, 0, theInfo);
        if (rel == null) return null;
        //ArrayList<String> homStrings = unredKhovHom;
        //if (reduced) homStrings = redKhovHom;
        for (int j = rel[0]; j < rel[1]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            cohoms.add(coh);
        }
        HomologyInfo hinfo = new HomologyInfo(0l,1);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    public HomologyInfo rationalHomology(char reduced, ArrayList<String> homStrings, ArrayList<String> theInfo) {
        ArrayList<QuantumCohomology> cohoms = new ArrayList<QuantumCohomology>();
        int[] rel = getStartEnd(reduced,1, theInfo);
        if (rel == null) rel = getStartEnd(reduced,-1, theInfo);
        if (rel == null) return null;
        //ArrayList<String> homStrings = unredKhovHom;
        //if (reduced) homStrings = redKhovHom;
        for (int j = rel[0]; j < rel[1]; j++) {
            String qinfo = homStrings.get(j);
            QuantumCohomology coh = new QuantumCohomology(qinfo);
            for (Homology hom : coh.getHomGroups()) hom.removeTorsion();
            cohoms.add(coh);
        }
        HomologyInfo hinfo = new HomologyInfo(1l,1);
        for (QuantumCohomology coh : cohoms) hinfo.addCohomology(coh);
        return hinfo;
    }
    
    private int[] getStartEnd(char red, int cse, ArrayList<String> theInfo) {
        if (theInfo == null) return null;
        //char red = 'u';
        //if (reduced) red = 'r';
        boolean found = false;
        int i = 0;
        while (!found && i < theInfo.size()) {
            String khoInf = theInfo.get(i);
            long theC = Long.parseLong(khoInf.substring(1, khoInf.indexOf('.')));
            if (theC < 0) theC = -1;
            if (theC == cse && khoInf.charAt(0) == red && khoInf.indexOf('a') == -1) found = true;
            else i++;
        }
        if (!found) return null;
        String info = theInfo.get(i);
        int[] rel = new int[2];
        int u = info.lastIndexOf(".");
        int v = info.lastIndexOf("-");
        rel[0] = Integer.parseInt(info.substring(u+1, v));
        rel[1] = Integer.parseInt(info.substring(v+1));
        return rel;
    }
    
    public void setSqOne(int[] data) {
        sqEven = "("+data[0]+","+data[1]+","+data[2]+","+data[3]+")";
    }
    
    public void setSInvariant(String sinv) {
        int[][] sinvs = sInvariants(sinv);
        for (int i = 0; i < sinvs.length; i++) setSInvariant(sinvs[i][0],sinvs[i][1]);
    }
    
    public void setSInvariant(int chi, int s) {
        reLock.lock();
        try {
        if (sinvariant == null) sinvariant = chi+":"+s;
        else sinvariant = sinvariant+","+chi+":"+s;
        }
        finally {
            reLock.unlock();
        }// */
    }
    
    public boolean otherSInvariants() {
        int[][] invs = sInvariants();
        boolean exist = false;
        int i = 0;
        while (!exist && i < invs.length) {
            if (invs[i][0] >= 3) exist = true;
            i++;
        }
        return exist;
    }
    
    public int[] getSqOne(boolean even) {
        if (even && sqEven == null) return null;
        if (!even && sqOdd == null) return null;
        int[] sqone = new int[4];
        String relString = sqOdd;
        if (even) relString = sqEven;
        int a = relString.indexOf(',');
        int b = a+1+relString.substring(a+1).indexOf(',');
        int c = b+1+relString.substring(b+1).indexOf(',');
        int d = relString.indexOf(')');
        sqone[0] = Integer.parseInt(relString.substring(1, a));
        sqone[1] = Integer.parseInt(relString.substring(a+2, b));
        sqone[2] = Integer.parseInt(relString.substring(b+2, c));
        sqone[3] = Integer.parseInt(relString.substring(c+2, d));
        return sqone;
    }
    
    public int[][] sInvariants() {
        return sInvariants(sinvariant);
    }
    
    private int[][] sInvariants(String sinv) {
        if (sinv == null) return new int[0][0];
        ArrayList<String> infos = new ArrayList<String>();
        String helpString = sinv.substring(0);
        int i = helpString.indexOf(',');
        while (i >= 0) {
            infos.add(helpString.substring(0, i));
            helpString = helpString.substring(i+1);
            i = helpString.indexOf(',');
        }
        infos.add(helpString);
        int[][] info = new int[infos.size()][2];
        i = 0;
        for (String inf : infos) {
            int k = inf.indexOf(':');
            info[i][0] = Integer.parseInt(inf.substring(0, k));
            info[i][1] = Integer.parseInt(inf.substring(k+1));
            i++;
        }
        return info;
    }

    public Integer sInvariant(int chr) {
        int[][] sinvs = sInvariants();
        boolean found = false;
        int i = 0;
        while (!found && i < sinvs.length) {
            if (sinvs[i][0] == chr) found = true;
            else i++;
        }
        if (found) return sinvs[i][1];// */
        return null;
    }
    
    @Override
    public int compareTo(LinkData o) {
        return Comparer.compare(this, o, comparer.getType());
    }

    public HomologyInfo integralKhovHomology(boolean reduced) {
        ArrayList<String> theStrings = unredKhovHom;
        ArrayList<String> theInfo = khovInfo;
        if (reduced) theStrings = redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return integralHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo integralOddKhHomology() {
        ArrayList<String> theInfo = okhovInfo;
        ArrayList<String> theStrings = oddKhovHom;
        char reduz = 'o';
        return integralHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo rationalKhovHomology(boolean reduced) {
        ArrayList<String> theStrings = unredKhovHom;
        ArrayList<String> theInfo = khovInfo;
        if (reduced) theStrings = redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return rationalHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo rationalOddKhHomology() {
        ArrayList<String> theInfo = okhovInfo;
        ArrayList<String> theStrings = oddKhovHom;
        char reduz = 'o';
        return rationalHomology(reduz, theStrings, theInfo);
    }
    
    public HomologyInfo modKhovHomology(boolean reduced, int p) {
        ArrayList<String> theStrings = unredKhovHom;
        ArrayList<String> theInfo = khovInfo;
        if (reduced) theStrings = redKhovHom;
        char reduz = 'u';
        if (reduced) reduz = 'r';
        return modularHomology(reduz, theStrings, theInfo, p);
    }
    
    public HomologyInfo modOddKhHomology(int p) {
        ArrayList<String> theInfo = okhovInfo;
        ArrayList<String> theStrings = oddKhovHom;
        char reduz = 'o';
        return modularHomology(reduz, theStrings, theInfo, p);
    }
    
    public void wrapUpEvenKhov(ArrayList<String> unredInfo, ArrayList<String> redInfo,
            long coeff, Options options) {
        reLock.lock();
        try {
            if (khovInfo == null) khovInfo = new ArrayList<String>();
            if (!unredInfo.isEmpty()) {
                if (unredKhovHom == null) unredKhovHom = new ArrayList<String>();
                wrapUpNewInfo('u', unredInfo, unredKhovHom, khovInfo, coeff, options);
            }
            if (!redInfo.isEmpty()) {
                if (redKhovHom == null) redKhovHom = new ArrayList<String>();
                wrapUpNewInfo('r', redInfo, redKhovHom, khovInfo, coeff, options);
            }
        }
        finally {
            reLock.unlock();
        }
    }

    public void wrapUpOddKhov(ArrayList<String> homInfo,
            long coeff, Options options) {
        reLock.lock();
        try {
            if (okhovInfo == null) okhovInfo = new ArrayList<String>();
            if (oddKhovHom == null) oddKhovHom = new ArrayList<String>();
            wrapUpNewInfo('o', homInfo, oddKhovHom, okhovInfo, coeff, options);
        }
        finally {
            reLock.unlock();
        }
    }
    
    private void wrapUpNewInfo(char redChar, ArrayList<String> endHom, ArrayList<String> oldInfo,
            ArrayList<String> origInfo, long coeff, Options options) {
        String newInfo = endHom.get(0)+"0-"+(endHom.size()-1);
        endHom.remove(0);
        boolean aborted = (newInfo.indexOf('a')>=0);
        int i = origInfo.size()-1;
        while (i>=0) {
            if (origInfo.get(i).charAt(0) == redChar) {
                String infoString = origInfo.get(i);
                if (aborted | keepTheInfo(infoString, coeff, options)) {
                    int st = Integer.parseInt(infoString.substring(infoString.lastIndexOf('.')+1, infoString.lastIndexOf('-')));
                    int en = Integer.parseInt(infoString.substring(infoString.lastIndexOf('-')+1));
                    int be = endHom.size();
                    for (int j = st; j < en; j++) endHom.add(oldInfo.get(j));
                    origInfo.set(i, infoString.substring(0, infoString.lastIndexOf('.')+1)+be+"-"+endHom.size());
                }
                else origInfo.remove(i);
            }
            i--;
        }
        origInfo.add(newInfo);
        oldInfo.clear();
        for (String info : endHom) oldInfo.add(info);
    }
    
    private boolean keepTheInfo(String info, long coeff, Options options) {
        if (coeff == 0) return false;
        long rng = Long.parseLong(info.substring(1,info.indexOf('.')));
        if (coeff == 1) {
            return rng != 1;
        }
        if (coeff < 0) {
            if (rng == 1) return false;
            ArrayList<Integer> primes = getPrimes(coeff, options.getPrimes());
            if (rng > 1) {
                int[] pap = primeAndPower((int) rng, options);
                return primes.contains(pap[0]);
            }
            if (rng < 0) {
                ArrayList<Integer> altPrimes = getPrimes(rng, options.getPrimes());
                return !betterThan(altPrimes,primes);
            }
        }// now coeff >1
        if (rng <= 1) return true;
        int[] pap = primeAndPower((int) coeff, options);
        int[] pop = primeAndPower((int) rng, options);
        if (pap[0] != pop[0]) return true;
        return pap[1] < pop[1];
    }
    
    private int[] primeAndPower(int rng, Options options) {
        int[] pap = new int[2];
        boolean found = false;
        int i = 0;
        while (!found) {
            int p = options.getPrimes().get(i);
            if (rng % p == 0) found = true;
            else i++;
        }
        pap[0] = options.getPrimes().get(i);
        pap[1] = powerOf(rng, pap[0]);
        return pap;
    }
    
    private int[] primeAndPower(int rng) {
        int[] pap = new int[2];
        boolean found = false;
        int p = 2;
        while (!found) {
            if (rng % p == 0) found = true;
            else p++;
        }
        pap[0] = p;
        pap[1] = powerOf(rng, p);
        return pap;
    }
    
    private int powerOf(int rng, int p) {
        int i = 1;
        while (rng /p != 0) {
            rng = rng / p;
            i++;
        }
        return i;
    }
    
    private boolean betterThan(ArrayList<Integer> primes, ArrayList<Integer> subprimes) {
        boolean better = true;
        int i = 0;
        while (i < subprimes.size() && better) {
            if (!primes.contains(subprimes.get(i))) better = false;
            i++;
        }
        return better;
    }
    
    public ArrayList<Integer> getPrimes(long rng, ArrayList<Integer> primes) {
        ArrayList<Integer> prms = new ArrayList<Integer>();
        long pwr = 1;
        for (int p = 0; p < primes.size(); p++) {
            if (rng % (2*pwr) != 0) {
                prms.add(primes.get(p));
                rng = rng + pwr;
            }
            pwr = 2 * pwr;
        }
        return prms;
    }
}
