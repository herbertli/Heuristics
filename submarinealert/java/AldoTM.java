/**
 * Turn One: Scan to find the interval where the sub lies.
 * Every turn afterwards, sca
 */

import java.util.ArrayList;

public class AldoTM implements TrenchManager{
    int d, y, r, m, L, p;
    int lb, ub;
    boolean turnOne, safe;
    int[] probes;
    public AldoTM(int d, int y, int r, int m, int L, int p){
        this.d = d;
        this.y = y;
        this.r = r;
        this.m = m;
        this.L = L;
        this.p = p;
        this.lb = -1;
        this.ub = -1;
        this.turnOne = true;
        this.safe = false;
        this.probes = null;
    }

    public int[] getProbes() {
        if (safe) return new int[0];
        else if (turnOne) {
            probes = blanket();
        } else {
            lb = (lb - 1 + 100) % 100;
            ub = (ub + 1 + 100) % 100;
            probes = new int[1];
            probes[0] = (lb + L + 2) % 100;
        }
        return probes;
    }

    int[] blanket(){
        ArrayList<Integer> al = new ArrayList<>();
        for (int i = d; i < d + 100; i = i + 2 * L + 1) {
            al.add((i + L) % 100);
        }
        int[] ia = new int[al.size()];
        for (int i = 0; i < al.size(); i++) {
            ia[i] = al.get(i);
        }
        return ia;
    }

    public void receiveProbeResults(boolean[] proberesults){
        int i = 0;
        if (turnOne) {
            for (; lb == -1 && i < probes.length; i++) {
                if (proberesults[i]) {
                    lb = (probes[i] - L + 100) % 100;
                    ub = (probes[i] + L + 100) % 100;
                }
            }
            turnOne = false;
        }
        int tlb = lb;
        int tub = ub;
        if (tub < tlb) tub += 100;
        for (; i < proberesults.length; i++){
            while(probes[i] + L <= tlb) probes[i] += 100;
            if (proberesults[i]) {
                tlb = Math.max(tlb, probes[i] - L);
                tub = Math.min(tub, probes[i] + L);
            } else {
                if (tlb > probes[i] - L) tlb = Math.max(tlb, probes[i] + L);
                if (tub < probes[i] + L) tub = Math.min(tub, probes[i] - L);
            }
        }
        lb = (tlb + 100) % 100;
        ub = (tub + 100) % 100;
    }

    public boolean shouldGoRed(){
        int td = d;
        int tlb = lb;
        int tub = ub;
        if (tub < tlb) tub += 100;
        if (td + 5 < tlb) td+=100;
        if (td >= tlb && td <= tub) {
            return true;
        } else if(td + 5 >= tlb && td + 5 <= tub){
            return true;
        }
        return false;
    }
}