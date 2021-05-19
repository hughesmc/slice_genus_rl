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

package knotjob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFileChooser;
import knotjob.dialogs.LoadDialog;
import knotjob.dialogs.LoadDialogWrap;
import knotjob.dialogs.DialogWrap;
import knotjob.links.Link;
import knotjob.links.LinkCreator;
import knotjob.links.LinkData;
import knotjob.links.StringData;

/**
 *
 * @author Dirk
 */
public class LinkLoader extends Thread {
    
    private final File[] files;
    private final Knobster knob;
    private final DialogWrap frame;
    private final Comparer comparer;
    private final Options options;
    private final ArrayList<LinkData> theLinks;
    private int counter;
    public AbortInfo abInf;

    LinkLoader(JFileChooser choose, LoadDialog fram, Knobster knb) {
        files = choose.getSelectedFiles();
        frame = new LoadDialogWrap(fram);
        knob = knb;
        options = knb.getOptions();
        options.setLoadLinksFrom(choose.getCurrentDirectory());
        abInf = fram.abInf;
        theLinks = new ArrayList<LinkData>();
        comparer = knb.getComparer();
        counter = 1;
    }
    
    LinkLoader(File[] fls, CountDownLatch countDown) {
        files = fls;
        frame = new DialogWrap(countDown, null);
        knob = null;
        options = new Options();
        abInf = new AbortInfo();
        theLinks = new ArrayList<LinkData>();
        comparer = new Comparer(0);
        counter = 1;
    }

    @Override
    public void run() {
        ArrayList<ArrayList<String>>infos = new ArrayList<ArrayList<String>>();
        try {
            for (File file : files) {
                if (abInf.isCancelled()) break;
                String fname = file.getAbsolutePath();
                String line;
                int y = fname.lastIndexOf(options.getSlash());
                frame.setText(fname.substring(y+1));
                try (FileReader fr = new FileReader(file)) {
                    BufferedReader in = new BufferedReader(fr);
                    boolean keepreading = true;
                    ArrayList<String> info = new ArrayList<String>();
                    info.add(fname);
                    while (keepreading) {
                        line = in.readLine();
                        if (line == null) keepreading = false;
                        else info.add(line);
                    }
                    infos.add(info);
                    fr.close();
                }
            }
        }
        catch (IOException io) {
            abInf.cancel();
        }
        for (ArrayList<String> info : infos) {
            if (abInf.isCancelled()) break;
            ArrayList<LinkData> newlinks = newLinkFrom(info);
            for (LinkData newlink : newlinks) theLinks.add(newlink);
        }
        frame.dispose();
    }

    public ArrayList<LinkData> getLinks() {
        return theLinks;
    }
    
    private ArrayList<LinkData> newLinkFrom(ArrayList<String> info) {
        ArrayList<LinkData> theData = new ArrayList<LinkData>();
        String filename = info.get(0);
        info.remove(filename);
        int v = filename.lastIndexOf(".");
        if (v == -1) return theData;
        String ext = filename.substring(v+1);
        if (fromKnotJob(ext)) enterKnotJobData(theData,info,ext);
        else enterOtherData(theData, info, ext);
        return theData;
    }
    
    private boolean fromKnotJob(String ext) {
        return ext.equals("kjb") || ext.equals("tkj") || ext.equals("kts");
    }
    
    private void enterOtherData(ArrayList<LinkData> theData, ArrayList<String> info, String ext) {
        if (ext.equals("gld")) addXKnotJob(theData, info);
        while (!info.isEmpty()) {
            if (abInf.isCancelled()) return;
            String inf = info.get(0);
            info.remove(inf);
            if (ext.equals("dtc")) addDowker(theData, inf);
            if (ext.equals("txt")) addPlanar(theData, inf);
            if (ext.equals("adc")) addAlpha(theData, inf);
            if (ext.equals("brd")) addBraids(theData, inf);
        }
    }
    
    private void addXKnotJob(ArrayList<LinkData> theData, ArrayList<String> info) {
        while (!info.isEmpty()) {
            if (abInf.isCancelled()) return;
            String name = info.get(0);
            String comm = info.get(1);
            String cros = info.get(2);
            String path = info.get(3);
            info.remove(0);
            info.remove(0);
            info.remove(0);
            info.remove(0);
            LinkData theNewData = new LinkData(name,comparer);
            frame.setText(name);
            if (comm.length()>0) theNewData.comment = comm;
            Link link = diagramCreator(cros,path,0,null).girthMinimize();
            if ((link != null && !linkContainsZero(link))) {
                theNewData.links.add(link);
                theData.add(theNewData);
            }
        }
    }
    
    private void addAlpha(ArrayList<LinkData> theData, String info) {
        LinkData theLink = LinkCreator.enterADTCode(info, info, true, knob, comparer);
        if (theLink != null) {
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private void addPlanar(ArrayList<LinkData> theData, String info) {
        LinkData theLink = LinkCreator.enterPDCode(info, "Knot "+counter, true, knob, comparer);
        if (theLink != null) {
            if (theLink.chosenLink().components()>1) theLink.name = "Link "+counter;
            if (info.contains(" = ")) theLink.name = info.substring(0, info.indexOf(" = "));
            counter++;
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private void addBraids(ArrayList<LinkData> theData, String info) {
        LinkData theLink = LinkCreator.enterBraidCode(info, info, knob, true, comparer);
        if (theLink != null) {
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private void addDowker(ArrayList<LinkData> theData, String info) {
        int a = info.indexOf(' ');
        int cn = Integer.parseInt(info.substring(0, a));
        info = info.substring(a);
        while (info.charAt(0) == ' ') info = info.substring(1);
        a = info.indexOf(' ');
        int number = Integer.parseInt(info.substring(0,a));
        info = info.substring(a);
        while (info.charAt(0) == ' ') info = info.substring(1);
        String name = getDTName(cn,number,info);
        LinkData theLink = LinkCreator.enterDTCode(info, name, true, knob, comparer);
        if (theLink != null) {
            frame.setText(theLink.name);
            theData.add(theLink);
        }
    }
    
    private String getDTName(int cn, int number, String info) {
        boolean alt = info.indexOf('-') == -1;
        int length = lengthOf(cn,alt);
        String al = "n";
        if (alt) al = "a";
        String rest = String.valueOf(number);
        while (rest.length() < length) rest = "0"+rest;
        String name = cn+al+rest;
        return name;
    }
    
    private int lengthOf(int cn, boolean alt) {
        if (cn > 16) return 1;
        int[] alts = {1,1,1,1,1,1,1,1,2,2,3,3,4,4,5,5,6};
        int[] nons = {1,1,1,1,1,1,1,1,1,1,2,3,3,4,5,6,7};
        if (alt) return alts[cn];
        return nons[cn];
    }
    
    private void enterKnotJobData(ArrayList<LinkData> theData, ArrayList<String> info, String ext) {
        StringData currentData = null;
        while (!info.isEmpty()) {
            if (abInf.isCancelled()) return;
            String inf = info.get(0);
            info.remove(inf);
            if ("0:".equals(inf.substring(0,2))) {
                if (currentData != null) finishOff(currentData,theData,ext);
                currentData = new StringData(inf.substring(2));
                currentData.name = inf.substring(2);
                frame.setText(currentData.name);
            }
            if (currentData != null) {
                if (checker(currentData, "1:", inf)) currentData.comment = inf.substring(2);
                if (checker(currentData, "2:", inf)) currentData.crossings.add(inf.substring(2));
                if (checker(currentData, "3:", inf)) currentData.paths.add(inf.substring(2));
                if (checker(currentData, "4:", inf)) currentData.unredKhovHom.add(inf.substring(2));
                if (checker(currentData, "5:", inf)) currentData.redKhovHom.add(inf.substring(2));
                if (checker(currentData, "6:", inf)) addSix(currentData,inf.substring(2),ext);
                if (checker(currentData, "7:", inf)) currentData.sqeven = inf.substring(2);
                if (checker(currentData, "8:", inf)) currentData.sinvariant = inf.substring(2);
                if (checker(currentData, "9:", inf)) currentData.orientations.add(inf.substring(2));
                if (checker(currentData, "10:", inf)) currentData.oddKhovHom.add(inf.substring(3));
                if (checker(currentData, "11:", inf)) currentData.okhovInfo.add(inf.substring(3));
                if (checker(currentData, "12:", inf)) currentData.sqodd = inf.substring(3);
            }
        }
        if (currentData != null) finishOff(currentData,theData,ext);
    }
    
    private boolean checker(StringData data, String start, String inf) {
        if (inf.length() < start.length()) return false;
        return start.equals(inf.substring(0, start.length()));
    }
    
    private void addSix(StringData theLink, String info, String ext) {
        if ("kts".equals(ext)) {
            int a = info.indexOf('.');
            int b = info.lastIndexOf('.');
            String fs = info.substring(0,a);
            String ss = info.substring(a+1,b);
            String ts = info.substring(b+1);
            String rel = "";
            if (!"null".equals(fs)) rel = "0:"+fs;
            if (!"null".equals(ss)) {
                if ("".equals(rel)) rel = "2:"+ss;
                else rel = rel+",2:"+ss;
            }
            if (!"null".equals(ts)) {
                if ("".equals(rel)) rel = "3:"+ts;
                else rel = rel+",3:"+ts;
            }
            theLink.sinvariant = rel;
        }
        else theLink.khovInfo.add(info);
    }
    
    private void finishOff(StringData theLink, ArrayList<LinkData> theData, String ext) {
        if (theLink.crossings.size()!=theLink.paths.size()) return;
        LinkData theNewData = new LinkData(theLink.name,comparer);
        theNewData.comment = theLink.comment;
        boolean okay = true;
        for (int i = 0; i < theLink.crossings.size(); i++) {
            String crossings = theLink.crossings.get(i);
            String paths = theLink.paths.get(i);
            String orien = null;
            int comp = 0;
            if ("kjb".equals(ext)) {
                int a = crossings.indexOf(',');
                if (a >= 0) {
                    comp = Integer.parseInt(crossings.substring(0, a));
                    crossings = crossings.substring(a+1);
                }
                else {
                    comp = Integer.parseInt(crossings);
                    crossings = "";
                }
                orien = theLink.orientations.get(i);
            }
            Link link = diagramCreator(crossings,paths,comp,orien);
            if (link == null || linkContainsZero(link)) okay = false;
            else theNewData.links.add(link);
        }
        if (okay) {
            theNewData.setSInvariant(theLink.sinvariant);
            theNewData.sqEven = theLink.sqeven;
            theNewData.sqOdd = theLink.sqodd;
            if (!theLink.unredKhovHom.isEmpty()) theNewData.unredKhovHom = theLink.unredKhovHom;
            if (!theLink.redKhovHom.isEmpty()) theNewData.redKhovHom = theLink.redKhovHom;
            if (!theLink.oddKhovHom.isEmpty()) theNewData.oddKhovHom = theLink.oddKhovHom;
            if (!theLink.okhovInfo.isEmpty()) theNewData.okhovInfo = theLink.okhovInfo;
            if ("kts".equals(ext)) {
                if (!theLink.unredKhovHom.isEmpty() || !theLink.redKhovHom.isEmpty()) 
                    theNewData.khovInfo = new ArrayList<String>();
                if (!theLink.unredKhovHom.isEmpty()) theNewData.khovInfo.add("u0.0-"+theLink.unredKhovHom.size());
                if (!theLink.redKhovHom.isEmpty()) theNewData.khovInfo.add("r0.0c.0-"+theLink.redKhovHom.size());
            }
            else {
                if ("tkj".equals(ext)) {
                    int[][] rel = relevantInfo(theLink);
                    if (rel.length > 0) theNewData.khovInfo = new ArrayList<String>();
                    for (int u = 0; u < rel.length; u++) {
                        theNewData.khovInfo.add("u"+rel[u][0]+"."+rel[u][1]+"-"+rel[u][2]);
                        theNewData.khovInfo.add("r"+rel[u][0]+".0c."+rel[u][3]+"-"+rel[u][4]);
                    }
                }
                else theNewData.khovInfo = theLink.khovInfo;
            }
            theData.add(theNewData);
        }
    }
    
    private int[][] relevantInfo(StringData theLink) {
        int c = theLink.khovInfo.size();
        int[][] rel = new int[c][5];
        if (c == 0) return rel;
        for (int i = 0; i < c; i++) {
            String info = theLink.khovInfo.get(i);
            int a = info.indexOf('.');
            int b = info.lastIndexOf('.');
            rel[i][0] = Integer.parseInt(info.substring(1,a));
            rel[i][1] = Integer.parseInt(info.substring(a+1,b));
            rel[i][3] = Integer.parseInt(info.substring(b+1));
            if (i>0) {
                rel[i-1][2] = rel[i][1];
                rel[i-1][4] = rel[i][3];
            }
        }
        rel[c-1][2] = theLink.unredKhovHom.size();
        rel[c-1][4] = theLink.redKhovHom.size();
        return rel;
    }
    
    private Link diagramCreator(String arg1, String arg2, int comp, String orien) {
        Link link = null;
        int fool = 0;
        int [] crossings;// = new int[3];
        int [][] paths;// = new int[3][4];
        arg1 = arg1+",";
        int k = arg1.length();
        ArrayList<Integer> commas = new ArrayList<Integer>();
        int j = 1;
        while (j < k) {
            if (arg1.charAt(j) == ',') commas.add(j);
            j++;
        }
        crossings = new int [commas.size()];
        try {
            int pos = 0;
            for (int i = 0 ; i < commas.size(); i++) {
                    crossings[i] = Integer.parseInt(arg1.substring(pos,commas.get(i)));
                    pos = commas.get(i)+1;
            }
        }
        catch (NumberFormatException e) {
            fool = 4;
        }
        arg2 = arg2+",";
        k = arg2.length();
        commas.clear();
        j = 1;
        while (j < k) {
            if (arg2.charAt(j) == ',') commas.add(j);
            j++;
        }
        paths = new int [crossings.length][4];
        if (commas.size() == 4 * crossings.length) {
            try {
                int pos = 0;
                for (int i = 0; i < commas.size(); i++) {
                    paths[i/4][i%4] = Integer.parseInt(arg2.substring(pos,commas.get(i)));
                    pos = commas.get(i)+1;
                }
            }
            catch (NumberFormatException e) {
                fool = 5;
            }
        }
        else fool = 5;
        if (fool == 0) link = new Link(crossings,paths,comp,orien);
        return link;
    }

    private boolean linkContainsZero(Link link) {
        boolean noGood = false;
        int i = 0;
        while (!noGood && i < link.crossingLength()) {
            if (link.getCross(i) == 0) noGood = true;
            i++;
        }
        return noGood;
    }
}
