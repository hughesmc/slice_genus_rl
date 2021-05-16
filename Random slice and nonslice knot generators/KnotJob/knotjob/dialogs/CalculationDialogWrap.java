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
package knotjob.dialogs;

import javax.swing.JFrame;
import knotjob.AbortInfo;

/**
 *
 * @author Dirk
 */
public class CalculationDialogWrap extends DialogWrap {
    
    private final CalculationDialog frame;
    private final boolean detailed;
    
    public CalculationDialogWrap(CalculationDialog frm, boolean det) {
        super(null, null);
        frame = frm;
        detailed = det;
    }

    @Override
    public void dispose() {
        delay(200);
        frame.dispose();
    }
    
    @Override
    public JFrame getFrame() {
        return frame.frame;
    }
    
    @Override
    public AbortInfo getAbortInfo() {
        return frame.abInf;
    }
    
    @Override
    public void setLabelLeft(String substring, int lv) {
        if (detailed) frame.setLabelLeft(substring, lv);
    }
    
    @Override
    public void setLabelRight(String substring, int lv) {
        if (detailed) frame.setLabelRight(substring, lv);
    }
    
    @Override
    public void setTitleLabel(String substring) {
        frame.setTitleLabel(substring);
    }
}
