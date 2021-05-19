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

package knotjob.homology.oddkhov;

import java.math.BigInteger;
import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.dialogs.TimerDialog;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.links.LinkData;
import knotjob.rings.BigInt;
import knotjob.rings.BigRat;
import knotjob.rings.LocalP;
import knotjob.rings.ModN;

/**
 *
 * @author dirk
 */
public class OddKhovCalculator extends Thread {
    
    private final long coeff;
    private final ArrayList<LinkData> linkList;
    private final DialogWrap frame;
    private final Options options;
    private final AbortInfo abInf;
    
    public OddKhovCalculator(ArrayList<LinkData> lnkLst, long val, Options optns, DialogWrap frm) {
        coeff = val;
        linkList = lnkLst;
        frame = frm;
        options = optns;
        abInf = frame.getAbortInfo();
    }
    
    @Override
    public void run() {
        long start = System.nanoTime();
        for (LinkData theLink : linkList) {
            frame.setTitleLabel(theLink.name);
            frame.setLabelLeft("Crossing : ", 0);
            frame.setLabelLeft("Girth : ", 1);
            frame.setLabelLeft("Objects : ", 2);
            if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3);
            if (calculationRequired(theLink)) {
                if (coeff == 0) {
                    OddKhovHomology<BigInt> hom = new OddKhovHomology<BigInt>(theLink, coeff, frame, options, new BigInt(1), null);
                    hom.calculate();
                    if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
                }
                if (coeff == 1) {// rational calculation
                    OddKhovHomology<BigRat> hom = new OddKhovHomology<BigRat>(theLink, coeff, frame, options, 
                            new BigRat(BigInteger.ONE), null);
                    hom.calculate();
                    if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
                }
                if (coeff >= 2) {// modular calculation
                    int coff = (int) coeff;
                    OddKhovHomology<ModN> hom = new OddKhovHomology<ModN>(theLink, coeff, frame, options, new ModN(1, coff), 
                    new ModN(primeOf(coff), coff));
                    hom.calculate();
                    if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
                }
                if (coeff < 0) {// local calculation
                    ArrayList<Integer> prms = EvenKhovCalculator.getPrimes(coeff, options.getPrimes());
                    int[] primes = new int[prms.size()];
                    for (int i = 0; i < prms.size(); i++) primes[i] = prms.get(i);
                    OddKhovHomology<LocalP> hom = new OddKhovHomology<LocalP>(theLink, coeff, frame, options, 
                            new LocalP(BigInteger.ONE,primes), null);
                    hom.calculate();
                    if (!abInf.isAborted()) wrapUp(theLink, hom.getOddHomology());
                }
            }
            if (abInf.isCancelled()) break;
            if (abInf.isAborted()) abInf.deAbort();
        }
        long end = System.nanoTime();
        frame.dispose();
        if (options.getTimeInfo()) {
            TimerDialog dialog = new TimerDialog(frame.getFrame(), "Calculation Time", true, end - start);
            dialog.setup();
        }
    }
    
    private boolean calculationRequired(LinkData theLink) {//will be a check similar to with even Khov 
        if (theLink.integralOddHomologyExists()) return false;
        if (coeff == 0) return true;
        if (coeff < 0) {
            ArrayList<Integer> primes = EvenKhovCalculator.getPrimes(coeff,options.getPrimes());
            return noBetterCalculation(primes,theLink);
        }
        if (coeff == 1 && theLink.rationalOddHomologyExists()) return false;
        if (coeff == 1) return true;
        int[] pap = primeAndPower((int) coeff);
        return noBetterCalculation(pap,theLink);
    }

    private boolean noBetterCalculation(int[] pap, LinkData theLink) {
        if (theLink.okhovInfo == null) return true;
        for (String info : theLink.okhovInfo) {
            long rng = Long.parseLong(info.substring(1, info.indexOf('.')));
            if (rng < 0 && info.indexOf('a') == -1) {
                if (!EvenKhovCalculator.getPrimes(rng,options.getPrimes()).contains(pap[0])) return false;
            }
            if (rng > 1 && info.indexOf('a') == -1) {
                if (rng % pap[0] == 0 && rng / pap[0] >= pap[1]) return false;
            }
        }
        return true;
    }
    
    private boolean noBetterCalculation(ArrayList<Integer> primes, LinkData theLink) {
        if (theLink.okhovInfo == null) return true;
        ArrayList<ArrayList<Integer>> altPrimes = new ArrayList<ArrayList<Integer>>();
        for (String info : theLink.okhovInfo) {
            if (info.charAt(1)=='-' && info.indexOf('a') == -1) {
                long rng = Long.parseLong(info.substring(1, info.indexOf('.')));
                altPrimes.add(EvenKhovCalculator.getPrimes(rng,options.getPrimes()));
            }
        }
        boolean noBetter = true;
        int i = 0;
        while (i < altPrimes.size() && noBetter) {
            if (betterThan(primes,altPrimes.get(i))) noBetter = false;
            i++;
        }
        return noBetter;
    }
    
    private void wrapUp(LinkData theLink, ArrayList<String> endHom) {
        theLink.wrapUpOddKhov(endHom, coeff, options);
        
        /*if (!endHom.isEmpty()) {
            String newInfo = endHom.get(0)+"0-"+(endHom.size()-1);
            endHom.remove(0);
            ArrayList<String> oldInfo = theLink.oddKhovHom;
            boolean aborted = (newInfo.indexOf('a')>=0);
            if (theLink.okhovInfo != null) {
                int i = theLink.okhovInfo.size()-1;
                while (i>=0) {
                    String infoString = theLink.okhovInfo.get(i);
                    if (aborted | keepTheInfo(infoString)) {
                        int st = Integer.parseInt(infoString.substring(infoString.lastIndexOf('.')+1, infoString.lastIndexOf('-')));
                        int en = Integer.parseInt(infoString.substring(infoString.lastIndexOf('-')+1));
                        int be = endHom.size();
                        for (int j = st; j < en; j++) endHom.add(oldInfo.get(j));
                        theLink.okhovInfo.set(i, infoString.substring(0, infoString.lastIndexOf('.')+1)+be+"-"+endHom.size());
                    }
                    else theLink.okhovInfo.remove(i);
                    
                    i--;
                }
            }
            else theLink.okhovInfo = new ArrayList<String>();
            theLink.okhovInfo.add(newInfo);
            theLink.oddKhovHom = endHom;
        }// */
    }
    
    /*private boolean keepTheInfo(String info) {
        if (coeff == 0) return false;
        long rng = Long.parseLong(info.substring(1,info.indexOf('.')));
        if (coeff == 1) {
            return rng != 1;
        }
        if (coeff < 0) {
            if (rng == 1) return false;
            ArrayList<Integer> primes = EvenKhovCalculator.getPrimes(coeff, options.getPrimes());
            if (rng > 1) {
                int[] pap = primeAndPower((int) rng);
                return primes.contains(pap[0]);
            }
            if (rng < 0) {
                ArrayList<Integer> altPrimes = EvenKhovCalculator.getPrimes(rng, options.getPrimes());
                return !betterThan(altPrimes,primes);
            }
        }// now coeff >1
        if (rng <= 1) return true;
        int[] pap = primeAndPower((int) coeff);
        int[] pop = primeAndPower((int) rng);
        if (pap[0] != pop[0]) return true;
        return pap[1] < pop[1];
    }// */
    
    private int[] primeAndPower(int rng) {
        int[] pap = new int[2];
        boolean found = false;
        int i = 0;
        while (!found) {
            int p = options.getPrimes().get(i);
            if (rng % p == 0) found = true;
            else i++;
        }
        pap[0] = options.getPrimes().get(i);
        pap[1] = rng / pap[0];
        return pap;
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
    
    private int primeOf(int ring) {
        int prime = 2;
        boolean found = false;
        while (!found) {
            if (ring % prime == 0) found = true;
            else prime++;
        }
        return prime;
    }
    
}
