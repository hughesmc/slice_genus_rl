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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import knotjob.links.Link;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class LinkSaver {

    private final ArrayList<ArrayList<String>> allData;
    private final ArrayList<String> filenames;
    private final ArrayList<LinkData> theLinks;
    private final int[] choices;
    private final int maxCount;
    private final Options options;
    private final JFrame relFrame;
    
    public LinkSaver(ArrayList<LinkData> links, int[] chcs, Options optns, JFrame frm) {
        theLinks = links;
        allData = new ArrayList<ArrayList<String>>();
        filenames = new ArrayList<String>();
        choices = chcs;
        options = optns;
        relFrame = frm;
        maxCount = options.getMaxSaveCount();
    }

    public void export() {
        createData(false);
        saveTheData("txt");
    }

    public void save() {
        createData(true);
        saveTheData("kjb");
    }
    
    private void saveTheData(String ext) {
        for (int j = 0; j < filenames.size(); j++) {
            JFileChooser chooser = new JFileChooser();
            if (options.getLoadLinksFrom() != null) chooser.setCurrentDirectory(options.getLoadLinksFrom());
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Knots and Links (*.kjb)", "kjb");
            if ("txt".equals(ext)) filter = new FileNameExtensionFilter("PD-Diagrams (*.txt)", "txt");
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(new File(filenames.get(j)+"."+ext));
            int val = chooser.showSaveDialog(relFrame);
            if (val == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    options.setLoadLinksFrom(chooser.getCurrentDirectory());
                    String fname = file.getAbsolutePath();
                    if(!fname.endsWith("."+ext) ) {
                        file = new File(fname + "."+ext);
                    }
                    try (FileWriter fw = new FileWriter(file)) {
                        try (PrintWriter pw = new PrintWriter(fw)) {
                            for (String data : allData.get(j)) pw.println(data);
                        }
                    }
                }
                catch (IOException e) {

                }
            }
        }
    }
    
    private void createData(boolean save) {
        int i = 0;
        boolean keepgoing = true;
        while (keepgoing) {
            ArrayList<String> theData = new ArrayList<String>();
            int counter = 0;
            String filename = theLinks.get(choices[i]).name;
            while (i < choices.length && counter < maxCount) {
                LinkData ink = theLinks.get(choices[i]);
                if (save) addSaveData(ink, theData);
                else addExportData(ink, theData);
                i++;
                counter++;
            }
            if (counter > 1) filename = filename+"-"+theLinks.get(choices[i-1]).name;
            filenames.add(filename);
            allData.add(theData);
            if (i >= choices.length) keepgoing = false;
        }
    }

    private void addSaveData(LinkData ink, ArrayList<String> theData) {
        theData.add("0:"+ink.name);
        if (ink.comment != null) theData.add("1:"+ink.comment);
        for (Link link : ink.links) {
            theData.add("2:"+link.crossingsToString());
            theData.add("3:"+link.pathToString());
            theData.add("9:"+link.orientToString());
        }
        if (ink.unredKhovHom != null) for (String ukhov : ink.unredKhovHom) theData.add("4:"+ukhov);
        if (ink.redKhovHom != null) for (String rkhov : ink.redKhovHom) theData.add("5:"+rkhov);
        if (ink.khovInfo != null) for (String rinf : ink.khovInfo) theData.add("6:"+rinf);
        if (ink.oddKhovHom != null) for (String okhov : ink.oddKhovHom) theData.add("10:"+okhov);
        if (ink.okhovInfo != null) for (String oinf : ink.okhovInfo) theData.add("11:"+oinf);
        if (ink.sinvariant != null) theData.add("8:"+ink.sinvariant);
        if (ink.sqEven != null) theData.add("7:"+ink.sqEven);
        if (ink.sqOdd != null) theData.add("12:"+ink.sqOdd);
    }

    private void addExportData(LinkData ink, ArrayList<String> theData) {
        theData.add(exportData(ink));
    }
    
    private String exportData(LinkData ink) {
        String info = changedTitle(ink.name)+"PD[";
        Link link = ink.chosenLink().breakUp();
        ArrayList<Integer> transformation = transfer(link);
        ArrayList<Integer> used = new ArrayList<Integer>();
        info = info + pdInfo(link, transformation);
        return info+"]";
    }
    
    private String changedTitle(String title) {
        if (!options.getChangeOfNumbers()) return title+" = ";
        String change = "";
        for (char c : title.toCharArray()) {
            String nc = String.valueOf(c);
            int v = (int) c;
            if (v >= 48 && v <= 57) nc = String.valueOf((char) (v+16));
            if (v == 44) nc = ";";
            change = change+nc;
        }
        return change+" = ";
    }
    
    private ArrayList<Integer> transfer(Link link) {
        ArrayList<Integer> trans = new ArrayList<Integer>();
        trans.add(0);
        for (ArrayList<Integer> comp : link.getComponents()) {
            for (int a : comp) trans.add(a);
        }
        return trans;
    }
    
    private String pdInfo(Link link, ArrayList<Integer> trans) {
        String info = "";
        for (int i = 0; i < link.crossingLength(); i++) {
            if (!"".equals(info)) info = info + ", ";
            info = info+"X[";
            int add = 0;
            if (link.getCross(i) > 0) {
                int t0 = trans.indexOf(link.getPath(i, 0));
                int t1 = trans.indexOf(link.getPath(i, 2));

                if (t0 > t1) add = 2;
                if (t0 - t1 > 1) add = 0;
                if (t1 - t0 > 1) add = 2;
            }
            if (link.getCross(i) < 0) {
                add = 1;
                int t0 = trans.indexOf(link.getPath(i, 3));
                int t1 = trans.indexOf(link.getPath(i, 1));
                if (t0 < t1) add = 3;
                if (t0 - t1 > 1) add = 3;
                if (t1 - t0 > 1) add = 1;
            }
            for (int j = 0; j < 3; j++) info = info+trans.indexOf(link.getPath(i, (j+add)%4))+", ";
            info = info+trans.indexOf(link.getPath(i, (3+add)%4))+"]";
        }
        return info;
    }
    
}
