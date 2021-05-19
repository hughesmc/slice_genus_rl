/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.polynomial;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Dirk
 */
public class PoincarePolynomial extends Polynomial implements Comparable<PoincarePolynomial> {

    private final BigInteger torsionGroup;
    
    public PoincarePolynomial(String[] lbls, BigInteger val, int[] coef, BigInteger tor) {
        super(lbls, val, coef);
        torsionGroup = tor;
    }
    
    public PoincarePolynomial(String[] lbls, ArrayList<Coefficient> coeffs, BigInteger tor) {
        super(lbls, coeffs);
        torsionGroup = tor;
    }
    
    public BigInteger torsion() {
        return torsionGroup;
    }
    
    public PoincarePolynomial add(PoincarePolynomial pol) {
        ArrayList<Coefficient> add = new ArrayList<Coefficient>();
        for (Coefficient coe : coefficients) add.add(coe);
        for (Coefficient coe : pol.coefficients) add.add(coe);
        Collections.sort(add);
        combineCoefficients(add);
        return new PoincarePolynomial(labels, add, torsionGroup);
    }
    
    @Override
    public int compareTo(PoincarePolynomial po) {
        return torsionGroup.compareTo(po.torsionGroup);
    }
    
}
