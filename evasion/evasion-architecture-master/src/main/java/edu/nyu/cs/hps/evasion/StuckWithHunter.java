package edu.nyu.cs.hps.evasion;

import java.util.ArrayList;
import java.util.Scanner;

import edu.nyu.cs.hps.evasion.game.GameState;
import edu.nyu.cs.hps.evasion.game.HorizontalWall;
import edu.nyu.cs.hps.evasion.game.VerticalWall;


/**
 * This question is a pain.
 * Only place vertical and rectangular walls because diagonal walls are a pain.
 * Only keep 4 walls in place at a time.
 * If we want to place a wall to the side of the prey where there's already a wall,
 * only do so if the new wall is closer to the prey than the previous wall. 
 * Then delete the previous wall.
 */

public class StuckWithHunter implements Hunter {

    // below/left are with respect to the prey.
    int aboveCoor = 300;
    int rightCoor = 300;
    int belowCoor = 0;
    int leftCoor = 0;
    // for pausing tests
    Scanner sc = new Scanner(System.in);
    GameState gameState;

    @Override
    public void receiveGameState(GameState gameState) {
        this.gameState = gameState;
    }

    HunterMove emptyMove() {
        HunterMove move = new HunterMove();
        move.wallType = 0;
        move.wallsToDel = new ArrayList<>();
        return move;
    }

    int del(boolean horizontal, int coor) {
        if(horizontal) {
            for(int i = 0; i < gameState.walls.size(); i++) {
                if(gameState.walls.get(i) instanceof HorizontalWall) {
                    if(((HorizontalWall) gameState.walls.get(i)).y == coor) return i;
                }
            }
        } else {
            for(int i = 0; i < gameState.walls.size(); i++) {
                if(gameState.walls.get(i) instanceof VerticalWall) {
                    if(((VerticalWall) gameState.walls.get(i)).x == coor) return i;
                }
            }
        }
        return -1;
    }

    public HunterMove playHunter() {
        boolean canPlaceWall = (gameState.wallTimer == 0);
        if(canPlaceWall) {
            int hposx = gameState.hunterPosAndVel.pos.x;
            int hposy = gameState.hunterPosAndVel.pos.y;
            int hvelx = gameState.hunterPosAndVel.vel.x;
            int hvely = gameState.hunterPosAndVel.vel.y;
            int verticalWallCoor = hposx;
            int horizontalWallCoor = hposy;
            int px = gameState.preyPos.x;
            int py = gameState.preyPos.y;
            HunterMove hm = new HunterMove();
            ArrayList<Integer> deleteWalls = new ArrayList<>();
            hm.wallsToDel = deleteWalls;
            if(py > horizontalWallCoor + 1 && horizontalWallCoor > belowCoor && horizontalWallCoor < hposy + hvely) {
                hm.wallType = 1;
                hm.wallsToDel.add(del(true, belowCoor));
                belowCoor = horizontalWallCoor;
                return hm;
            }
            if(px > verticalWallCoor && verticalWallCoor > leftCoor && verticalWallCoor < hposx + hvelx) {
                hm.wallType = 2;
                hm.wallsToDel.add(del(false, leftCoor));
                leftCoor = verticalWallCoor;
                return hm;
            }
            if(py < horizontalWallCoor - 1 && horizontalWallCoor < aboveCoor && horizontalWallCoor > hposy + hvely) {
                hm.wallType = 1;
                hm.wallsToDel.add(del(true, aboveCoor));
                aboveCoor = horizontalWallCoor;
                return hm;
            }
            if(px < verticalWallCoor && verticalWallCoor < rightCoor && verticalWallCoor > hposx + hvelx) {
                hm.wallType = 2;
                hm.wallsToDel.add(del(false, rightCoor));
                rightCoor = verticalWallCoor;
                return hm;
            }
        }
        // Nothing to do so do nothing.
        return emptyMove();
    }

}