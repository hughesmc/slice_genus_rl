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

package knotjob.rings;

/**
 *
 * @author Dirk
 */
public class ModNX implements Ring<ModNX> {

    private final int mod;
    private final int value0;
    private final int value1;
    
    public ModNX(int val0, int md) {
        mod = md;
        value0 = (mod + val0 % mod) % mod;
        value1 = 0;
    }
    
    public ModNX(int val0, int val1, int md) {
        mod = md;
        value0 = (mod + val0 % mod) % mod;
        value1 = (mod + val1 % mod) % mod;
    }
    
    @Override
    public ModNX add(ModNX r) {
        return new ModNX(value0+r.value0, value1+r.value1, mod);
    }

    @Override
    public ModNX div(ModNX r) {
        return multiply(r.invert());
    }

    @Override
    public ModNX getZero() {
        return new ModNX(0, mod);
    }

    @Override
    public ModNX invert() {
        if (this.isInvertible()) return new ModNX(value0, value1, mod); // only works for mod = 3;
        return new ModNX(0, 1, mod); // this is just a trick to get X.
    }

    @Override
    public boolean isBigger(ModNX r) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isInvertible() { // this currently only works for mod = 3
        if (value0 == 0) return false;
        if (value1 == 0) return true;
        return ((value0+value1)% mod != 0);
    }

    @Override
    public boolean isZero() {
        return ((value0 % mod == 0 ) & (value1 % mod == 0));
    }

    @Override
    public ModNX multiply(ModNX r) {
        return new ModNX(value0 * r.value0, value0 * r.value1 + value1 * r.value0 + value1 * r.value1,
                mod);
    }

    @Override
    public ModNX negate() {
        return new ModNX(mod - value0, mod - value1, mod);
    }
    
    @Override
    public String toString() {
        if (value1 % mod == 0) return ""+value0+" ("+mod+")";
        if (value0 % mod == 0) {
            if ((mod+value1) % mod != 1) return ""+value1+"X ("+mod+")";
            else return "X ("+mod+")";
        }
        if ((mod+value1) % mod != 1) return ""+value0+" + "+value1+"X ("+mod+")";
        return ""+value0+" + X ("+mod+")";
    }
    
}
