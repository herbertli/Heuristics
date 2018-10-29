package edu.nyu.cs.hps.evasion;

import java.util.ArrayList;

import edu.nyu.cs.hps.evasion.game.GameState;
import edu.nyu.cs.hps.evasion.game.HorizontalWall;
import edu.nyu.cs.hps.evasion.game.Wall;


/**
 * This question is a pain.
 * Only place vertical and rectangular walls because diagonal walls are a pain.
 * Only keep 4 walls in place at a time.
 * If we want to place a wall to the side of the prey where there's already a wall,
 * only do so if the new wall is closer to the prey than the previous wall.
 * Then delete the previous wall.
 */

public class AboveBelowHunter implements Hunter {

    private int belowCoor = -1;
    private int aboveCoor = -1;
    GameState gameState;

    @Override
    public void receiveGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private HunterMove emptyMove() {
        HunterMove move = new HunterMove();
        move.wallType = 0;
        move.wallsToDel = new ArrayList<>();
        return move;
    }

    private int del(int coord) {
        int i = 0;
        for (Wall w: gameState.walls) {
            if (w instanceof HorizontalWall) {
                if (((HorizontalWall) w).y == coord) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    public HunterMove playHunter() {
        boolean canPlaceWall = (gameState.wallTimer == 0);
        if (canPlaceWall) {
            int hposy = gameState.hunterPosAndVel.pos.y;
            int hvely = gameState.hunterPosAndVel.vel.y;
            int py = gameState.preyPos.y;
            HunterMove hm = new HunterMove();
            ArrayList<Integer> deleteWalls = new ArrayList<>();
            hm.wallsToDel = deleteWalls;
            if(py > hposy) {
                if(belowCoor == -1) {
                    hm.wallType = 1;
                    if(hvely < 0) return emptyMove();
                    belowCoor = hposy;
                    return hm;
                } else if(hposy > belowCoor) {
                    hm.wallType = 1;
                    hm.wallsToDel.add(del(belowCoor));
                    if(hvely < 0) return emptyMove();
                    belowCoor = hposy;
                    return hm;
                }
            } else if (py < hposy) {
                if(aboveCoor == -1) {
                    hm.wallType = 1;
                    if(hvely > 0) return emptyMove();
                    aboveCoor = hposy;
                    return hm;
                } else if(hposy < aboveCoor) {
                    hm.wallType = 1;
                    hm.wallsToDel.add(del(aboveCoor));
                    if(hvely > 0) return emptyMove();
                    aboveCoor = hposy;
                    return hm;
                }
            }
        }
        // Nothing to do so do nothing.
        return emptyMove();
    }

}