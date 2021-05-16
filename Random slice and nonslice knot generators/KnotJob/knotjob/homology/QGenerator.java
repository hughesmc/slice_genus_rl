/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knotjob.homology;

import knotjob.rings.Ring;

/**
 *
 * @author Dirk
 * @param <R>
 */
public class QGenerator<R extends Ring<R>> extends Generator<R> {
    
    protected final int qdeg;
    
    public QGenerator(int hd, int qd) {
        super(hd);
        qdeg = qd;
    }
    
    public int qdeg() {
        return qdeg;
    }

    public int getTopArrowSize() {
        return tMor.size();
    }

    public Arrow<R> getTopArrow(int i) {
        return tMor.get(i);
    }
    
}
