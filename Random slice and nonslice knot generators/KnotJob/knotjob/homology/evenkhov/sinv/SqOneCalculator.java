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

package knotjob.homology.evenkhov.sinv;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.DialogWrap;
import knotjob.dialogs.TimerDialog;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class SqOneCalculator extends Thread {
    
    private final ArrayList<LinkData> linkList;
    private final DialogWrap frame;
    private final Options options;
    private final AbortInfo abInf;
    
    public SqOneCalculator(ArrayList<LinkData> lnkLst, Options optns, DialogWrap frm) {
        linkList = lnkLst;
        options = optns;
        frame = frm;
        abInf = frame.getAbortInfo();
    }
    
    @Override
    public void run() {
        long start = System.nanoTime();
        frame.setLabelLeft("Crossing : ", 0);
        frame.setLabelLeft("Girth : ", 1);
        frame.setLabelLeft("Objects : ", 2);
        if (options.getGirthInfo() == 2) frame.setLabelLeft("h-Level : ", 3);
        for (LinkData theLink : linkList) {
            frame.setTitleLabel(theLink.name);
            if (calculationRequired(theLink)) {
                SqOneInvariant sqInv = new SqOneInvariant(theLink.chosenLink(),frame,options);
                sqInv.calculate();
                if (!abInf.isAborted()) theLink.sqEven = sqInv.getInvariant();
            }
            if (abInf.isCancelled()) break;
            if (abInf.isAborted()) abInf.deAbort();
        }
        long end = System.nanoTime();
        delay(200);
        frame.dispose();
        if (options.getTimeInfo()) {
            TimerDialog dialog = new TimerDialog(frame.getFrame(), "Calculation Time", true, end - start);
            dialog.setup();
        }
    }

    private void delay(int k) {
        try {
            Thread.sleep(k);
        }
        catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private boolean calculationRequired(LinkData theLink) {
        if (theLink.chosenLink().components()>1) return false;
        return (theLink.sqEven == null);
    }
}
