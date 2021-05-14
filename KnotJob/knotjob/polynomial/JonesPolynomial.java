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

import java.math.BigInteger;
import knotjob.AbortInfo;
import knotjob.Options;
import knotjob.dialogs.CalculationDialog;
import knotjob.links.Link;
import knotjob.links.LinkData;

/**
 *
 * @author Dirk
 */
public class JonesPolynomial {

    private final Link theLink;
    private final CalculationDialog frame;
    private final AbortInfo abInf;
    private final Options options;
    private final int[] girth;
    
    public JonesPolynomial(LinkData link, CalculationDialog frm, Options optns) {
        theLink = link.chosenLink();
        frame = frm;
        options = optns;
        abInf = frame.abInf;
        girth = theLink.totalGirthArray();
    }

    void calculate() {
        Polynomial bracket = getBracket();
        bracket = bracket.multiply(writheShift());
        System.out.println(bracket);
    }

    private Polynomial getBracket() {
        if (theLink.crossingLength() == 1) return oneCrossingPoly();
        BracketPolynomial bracket = firstPolynomial();
        int u = 1;
        while (u < theLink.crossingLength()-1) {
            boolean orient = (bracket.negContains(theLink.getPath(u, 0))| bracket.negContains(theLink.getPath(u, 2))| 
                    bracket.posContains(theLink.getPath(u,1)) | bracket.posContains(theLink.getPath(u,3)));
            BracketPolynomial nextBracket = new BracketPolynomial(theLink.getCross(u), theLink.getPath(u), frame,abInf,orient);
            frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0);
            bracket.modify(nextBracket, girthInfo(u));
            u++;
        }
        boolean orient = (bracket.negContains(theLink.getPath(u, 0))| bracket.negContains(theLink.getPath(u, 2))| 
                    bracket.posContains(theLink.getPath(u,1)) | bracket.posContains(theLink.getPath(u,3)));
        BracketPolynomial nextBracket = new BracketPolynomial(theLink.getCross(u), theLink.getPath(u), frame,abInf,orient);
        frame.setLabelRight(String.valueOf(u+1)+"/"+String.valueOf(theLink.crossingLength()), 0);
        bracket.modifyLast(nextBracket);
        return bracket.finalPolynomial();
    }

    private Polynomial oneCrossingPoly() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private BracketPolynomial firstPolynomial() {
        BracketPolynomial bracket = new BracketPolynomial(theLink.getCross(0), theLink.getPath(0),frame,abInf,false);
        return bracket;
    }
    
    private String girthInfo(int u) {
        String info = String.valueOf(girth[u]);
        if (options.getGirthInfo()!= 2) return info;
        if (u < girth.length - 1) info = info+" ("+girth[u+1];
        else return info;
        for (int i = 1; i < 3; i++) {
            if (u < girth.length - i - 1) info = info+", "+girth[u+1+i];
        }
        info = info+")";
        return info;
    }

    private Polynomial writheShift() {
        int writhe = theLink.writhe();
        BigInteger value = BigInteger.valueOf(-1);
        if (writhe%2 == 0) value = BigInteger.ONE;
        return new Polynomial(new String[] {"A"}, value, new int[] {-3*writhe});
    }
    
}
