/*

Copyright (C) 2021 Dirk Schuetz <dirk.schuetz@durham.ac.uk>

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

package knotjob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import knotjob.dialogs.DialogWrap;
import knotjob.homology.HomologyInfo;
import knotjob.homology.evenkhov.EvenKhovCalculator;
import knotjob.homology.evenkhov.sinv.SInvariantCalculator;
import knotjob.homology.evenkhov.sinv.SqOneCalculator;
import knotjob.homology.oddkhov.OddKhovCalculator;
import knotjob.homology.oddkhov.sinv.SqOneOddCalculator;
import knotjob.links.LinkData;
import knotjob.polynomial.PoincarePolynomial;

/**
 *
 * @author Dirk
 */
public class KnotJobCommand {

    private final String[] arguments;
    private final ArrayList<String> fileNames;
    private final ArrayList<String> commands;
    private final ArrayList<ArrayList<LinkData>> theLinks;
    private CountDownLatch countDown;
    private boolean sqOneOdd;
    private boolean sqOneEven;
    private boolean printScreen;
    private boolean printFile;
    private boolean khevenred;
    private boolean khevenunr;
    private boolean help;
    private final ArrayList<Integer> sInvs;
    private final ArrayList<Integer> evenKhovs;
    private final ArrayList<Integer> oddKhovs;
    private final Options options;
    
    public KnotJobCommand(String[] args) {
        arguments = args;
        fileNames = new ArrayList<String>();
        commands = new ArrayList<String>();
        theLinks = new ArrayList<ArrayList<LinkData>>();
        sInvs = new ArrayList<Integer>();
        evenKhovs = new ArrayList<Integer>();
        oddKhovs = new ArrayList<Integer>();
        options = new Options();
        printScreen = true;
        printFile = true;
        help = false;
    }

    public void getStarted() {
        getTheCommandsAndFiles();
        // arguments ending in .txt etc are considered filenames, arguments beginning with
        // "-" are considered commands.
        loadTheLinks();
        // each filename produces an ArrayList of LinkData, and these arraylists are now 
        // in theLinks.
        identifyCommands();
        // the booleans sqOneOdd and sqOneEven are now set depending on whether the 
        // command was found, sInvs contains those primes (or 0) for which s-invariant
        // should be calculated, similar with evenKhovs and oddKhovs.
        calculate();
    }

    private void loadTheLinks() {
        for (int i = 0; i < fileNames.size(); i++) {
            File[] files = new File[1];
            files[0] = new File(fileNames.get(i));
            countDown = new CountDownLatch(1);
            LinkLoader loader = new LinkLoader(files, countDown);
            loader.start();
            try {
                countDown.await();
            }
            catch (InterruptedException ex) {
                Logger.getLogger(KnotJobCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
            theLinks.add(loader.getLinks());
        } 
    }

    private void getTheCommandsAndFiles() {
        for (String command : arguments) {
            if (acceptableFileName(command)) fileNames.add(command);
            if (command.startsWith("-")) commands.add(command);
        }
    }

    private boolean acceptableFileName(String command) {
        if (command.endsWith(".txt")) return true;
        if (command.endsWith(".dtc")) return true;
        if (command.endsWith(".adt")) return true;
        if (command.endsWith(".brd")) return true;
        if (command.endsWith(".kts")) return true;
        return command.endsWith(".kjb");
    }

    private void identifyCommands() {
        for (String command : commands) {
            if (command.startsWith("-s")) checkSInvariant(command);
            if (command.startsWith("-k")) checkKhovInvariant(command);
            if (command.startsWith("-n")) checkPrintOptions(command);
            if (command.equals("-h")) help = true;
        }
        removeRedundancies(evenKhovs);
        removeRedundancies(oddKhovs);
        options.setKhovRed(khevenred);
        options.setKhovUnred(khevenunr);
    }

    private void checkSInvariant(String command) {
        if ("-sqo".equals(command)) {
            sqOneOdd = true;
            return;
        }
        if ("-sqe".equals(command)) {
            sqOneEven = true;
            return;
        }
        int characteristic = getSNumber(command);
        if (characteristic >= 0 && !sInvs.contains(characteristic)) sInvs.add(characteristic);
    }

    private void checkKhovInvariant(String command) {
        if (command.startsWith("-kr")) {
            int coef = getKhNumber(command);
            khevenred = true;
            if (coef >= 0 && !evenKhovs.contains(coef)) evenKhovs.add(coef);
        }
        if (command.startsWith("-ku")) {
            int coef = getKhNumber(command);
            khevenunr = true;
            if (coef >= 0 && !evenKhovs.contains(coef)) evenKhovs.add(coef);
        }
        if (command.startsWith("-kb")) {
            int coef = getKhNumber(command);
            khevenred = true;
            khevenunr = true;
            if (coef >= 0 && !evenKhovs.contains(coef)) evenKhovs.add(coef);
        }
        if (command.startsWith("-ko")) {
            int coef = getKhNumber(command);
            if (coef >= 0 && !oddKhovs.contains(coef)) oddKhovs.add(coef);
        }
    }

    private void checkPrintOptions(String command) {
        if ("-ns".equals(command)) printScreen = false;
        if ("-nf".equals(command)) printFile = false;
    }
    
    private int getSNumber(String command) {
        int ch;
        try {
            ch = Integer.parseInt(command.substring(2));
        }
        catch (NumberFormatException ne) {
            ch = -1;
        }
        if (ch <= 0) return ch;
        if (acceptedPrime(ch)) return ch;
        return -1;
    }

    private boolean acceptedPrime(int ch) {
        boolean accepted = false;
        int i = 0;
        while (!accepted && i < options.getPrimes().size()) {
            if (ch == options.getPrimes().get(i)) accepted = true;
            else i++;
        }
        return accepted;
    }

    private int getKhNumber(String command) {
        int ch;
        try {
            ch = Integer.parseInt(command.substring(3));
        }
        catch (NumberFormatException ne) {
            ch = -1;
        }
        if (ch <= 1) return ch;
        if (acceptedPrimePower(ch)) return ch;
        return -1;
    }

    private boolean acceptedPrimePower(int ch) {
        boolean accepted = false;
        int i = 0;
        while (!accepted && i < options.getPrimes().size()) {
            int prime = options.getPrimes().get(i);
            if (ch%prime == 0 && powerOkay(ch, prime)) accepted = true;
            else i++;
        }
        return accepted;
    }

    private boolean powerOkay(int ch, int prime) {
        int maxPower = options.getPowers().get(options.getPrimes().indexOf(prime));
        int i = 1;
        int p = prime;
        boolean okay = false;
        while (!okay && i <= maxPower) {
            if (ch == p) okay = true;
            else {
                i++;
                p = p * prime;
            }
        }
        return okay;
    }

    private void removeRedundancies(ArrayList<Integer> khovs) {
        if (khovs.contains(0)) {
            if (khovs.size() == 1) return;
            khovs.clear();
            khovs.add(0);
            return;
        }
        for (int prime : options.getPrimes()) {
            ArrayList<Integer> powers = new ArrayList<Integer>();
            for (int u : khovs) {
                if (u % prime == 0) powers.add(u);
            }
            Collections.sort(powers);
            for (int i = 0; i < powers.size()-1; i++) khovs.remove(powers.get(i));
        }
    }

    private void calculate() {
        if (help) {
            schreiNachHilfe();
            return;
        }
        if (fileNames.isEmpty()) { // if there are no files, you can enter a pd-code, or hit return to end the program.
            printFile = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String linkString;
                while (((linkString=reader.readLine())!=null) & (linkString.length() != 0)){
                    readOneLink(linkString);
                    doCalculations(false);
                    printTheStuff(0, null);
                    theLinks.remove(0);
                }
            }
             catch (IOException ex) {
                Logger.getLogger(KnotJobCommand.class.getName()).log(Level.SEVERE, null, ex);
             }
        }
        else {
            doCalculations(true);
            printOutInfo();
        }
    }
    
    private void readOneLink(String linkString) {
        theLinks.add(new ArrayList<LinkData>(1));
        LinkData theData = knotjob.links.LinkCreator.enterPDCode(linkString, "Knot", true, null, null);
        if (theData != null) theLinks.get(0).add(theData);
        // Ideally, this would check whether linkString is a planar diagram, or a DT-code, or whatever.
        // At the moment, it has to be a planar diagram. 
    }
    
    private void doCalculations(boolean extra) {
        int counter = sInvs.size()+evenKhovs.size()+oddKhovs.size();
        if (sqOneOdd) counter++;
        if (sqOneEven) counter++;
        countDown = new CountDownLatch(counter * theLinks.size());
        int count = -1;
        for (ArrayList<LinkData> links : theLinks) {
            count++;
            String extraInfo = null;
            for (int ch : sInvs) {
                if (extra) extraInfo = "S-invariant mod "+ch+" of file "
                                +fileNames.get(count)+" finished.";
                SInvariantCalculator calcS = new SInvariantCalculator(links, ch, options, 
                        new DialogWrap(countDown, extraInfo));
                // the String in DialogWrap can be set null to avoid printout.
                calcS.start();
            }
            if (sqOneEven) {
                if (extra) extraInfo = "Sq^1 even of file "
                                +fileNames.get(count)+" finished.";
                SqOneCalculator calculator = new SqOneCalculator(links, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            if (sqOneOdd) {
                if (extra) extraInfo = "Sq^1 odd of file "
                                +fileNames.get(count)+" finished.";
                SqOneOddCalculator calculator = new SqOneOddCalculator(links, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            for (int co : evenKhovs) {
                if (extra) extraInfo = "Even Kh mod "+co+" of file "
                                +fileNames.get(count)+" finished.";
                EvenKhovCalculator calculator = new EvenKhovCalculator(links, co, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
            for (int co : oddKhovs) {
                if (extra) extraInfo = "Odd Kh mod "+co+" of file "
                                +fileNames.get(count)+" finished.";
                OddKhovCalculator calculator = new OddKhovCalculator(links, co, options, 
                        new DialogWrap(countDown, extraInfo));
                calculator.start();
            }
        }
        try {
            countDown.await(); // all these calculators are threads. Here we wait 
            // until they are all finished.
        } 
        catch (InterruptedException ex) {
            Logger.getLogger(KnotJobCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printOutInfo() {
        for (int i = 0; i < theLinks.size(); i++) {
            String newFile = newFileName(fileNames.get(i));
            if (printScreen) System.out.println(newFile);
            File file = new File(newFile);
            try {
                FileWriter fw = null;
                PrintWriter pw = null;
                if (printFile) {
                    fw = new FileWriter(file);
                    pw = new PrintWriter(fw);
                }
                printTheStuff(i, pw);
                if (printFile) {
                    if (fw != null) fw.close();
                    if (pw != null) pw.close();
                }
            }
            catch (IOException e) {
                
            }
        }
    }
    
    private void khovanovOutput(HomologyInfo info, String extra, PrintWriter pw) {
        printOut(extra+info.poincarePolynomial(), pw);
        ArrayList<PoincarePolynomial> torPolys = info.torsionPolynomials(true);
        for (PoincarePolynomial poly : torPolys) {
            printOut("Torsion of order "+poly.torsion()+" :"+poly, pw);
        }
    }
    
    private void printOut(String str, PrintWriter pw) {
        if (printScreen) System.out.println(str);
        if (printFile) pw.println(str);
    }

    private void printTheStuff(int i, PrintWriter pw) {
        for (LinkData data : theLinks.get(i)) {
            printOut(data.name, pw);
            for (int s : sInvs) {
                printOut("S-Invariant mod "+s+" : "+data.sInvariant(s), pw);
            }
            if (sqOneEven) printOut("Even Sq^1 Invariant : "+data.sqEven, pw);
            if (sqOneOdd) printOut("Odd Sq^1 Invariant : "+data.sqOdd, pw);
            for (int pp : evenKhovs) {
                if (pp == 0) {
                    if (khevenunr) khovanovOutput(data.integralKhovHomology(false), 
                            "Integral unreduced Khovanov Homology :", pw);
                    if (khevenred) khovanovOutput(data.integralKhovHomology(true), 
                            "Integral reduced Khovanov Homology :", pw);
                }
                if (pp == 1) {
                    if (khevenunr) khovanovOutput(data.rationalKhovHomology(false), 
                            "Rational unreduced Khovanov Homology :", pw);
                    if (khevenred) khovanovOutput(data.rationalKhovHomology(true), 
                            "Rational reduced Khovanov Homology :", pw);
                }
                if (pp > 1) {
                    if (khevenunr) khovanovOutput(data.modKhovHomology(false, pp), 
                            "Unreduced Khovanov Homology mod "+pp+" :", pw);
                    if (khevenred) khovanovOutput(data.modKhovHomology(true, pp), 
                            "Reduced Khovanov Homology mod "+pp+" :", pw);
                }
            }
            for (int pp : oddKhovs) {
                if (pp == 0) {
                    khovanovOutput(data.integralOddKhHomology(), 
                            "Odd integral Khovanov Homology :", pw);
                }
                if (pp == 1) {
                    khovanovOutput(data.rationalOddKhHomology(), 
                            "Odd rational Khovanov Homology :", pw);
                }
                if (pp > 1) {
                    khovanovOutput(data.modOddKhHomology(pp), 
                            "Odd Khovanov Homology mod "+pp+" :", pw);
                }
            }
            printOut("", pw);
        }
    }
    
    private String newFileName(String name) {
        for (int s : sInvs) name = name+"_s"+s;
        if (sqOneEven) name = name+"_sqe";
        if (sqOneOdd) name = name+"_sqo";
        String kh = "_kb";
        if (!khevenred) kh = "_ku";
        if (!khevenunr) kh = "_kr";
        for (int p : evenKhovs) name = name+kh+p;
        for (int p : oddKhovs) name = name+"_ko"+p;
        return name;
    }

    private void schreiNachHilfe() {
        File file = new File("README.TXT");
        try (FileReader fr = new FileReader(file); BufferedReader in = new BufferedReader(fr)) {
            boolean keepreading = true;
            while (keepreading) {
                String line = in.readLine();
                if (line == null) keepreading = false;
                else System.out.println(line);
            }
        }
        catch (IOException io) {
            System.out.println("Help!");
        }
    }
    
}
