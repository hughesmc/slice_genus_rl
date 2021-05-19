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

package knotjob.polynomial;

import java.util.ArrayList;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.CalculationDialog;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class JonesCalculator extends Thread {
    
    private final ArrayList<LinkData> linkList;
    private final CalculationDialog frame;
    private final Options options;
    private final AbortInfo abInf;
    
    public JonesCalculator(ArrayList<LinkData> lnkLst, Options optns, CalculationDialog frm) {
        linkList = lnkLst;
        frame = frm;
        options = optns;
        abInf = frame.abInf;
    }
    
    @Override
    public void run() {
        for (LinkData theLink : linkList) {
            frame.setTitleLabel(theLink.name);
            frame.setLabelLeft("Crossing : ", 0);
            frame.setLabelLeft("Girth : ", 1);
            //frame.setLabelLeft("Objects : ", 2);
            if (calculationRequired(theLink)) {
                JonesPolynomial jones = new JonesPolynomial(theLink, frame, options);
                jones.calculate();
            }
        }
        delay(200);
        frame.dispose();
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
        return (theLink.jones == null);
    }
    
}
