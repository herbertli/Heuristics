package edu.nyu.cs.hps.evasion;

import java.util.ArrayList;
import java.util.List;

public class HunterMove {
    int wallType;
    List<Integer> wallsToDel = new ArrayList<>();

    public HunterMove(int wallType, List<Integer> wallsToDel) {
        this.wallType = wallType;
        this.wallsToDel = wallsToDel;
    }

    public HunterMove() {

    }
}