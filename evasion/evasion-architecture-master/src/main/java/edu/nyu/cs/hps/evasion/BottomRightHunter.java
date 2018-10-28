package edu.nyu.cs.hps.evasion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

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

public class BottomRightHunter extends EvasionClient {

    // below/left are with respect to the prey.
    int belowCoor = -1;
    int leftCoor = -1;
    // for pausing tests
    Scanner sc;

    private BottomRightHunter(String name, int port) throws IOException {
        super(name, port);
        sc = new Scanner(System.in);
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
            if(py > horizontalWallCoor) {
                //System.out.println("py: " + py + " coor: " + horizontalWallCoor + " belowCoor: " + belowCoor);
                //sc.next();
                if(belowCoor == -1) {
                    //System.out.println("there");
                    hm.wallType = 1;
                    if(horizontalWallCoor > hposy + hvely) return emptyMove();
                    belowCoor = horizontalWallCoor;
                    return hm;
                } else if(horizontalWallCoor > belowCoor) {
                    //System.out.println("here");
                    hm.wallType = 1;
                    hm.wallsToDel.add(del(true, belowCoor));
                    if(horizontalWallCoor > hposy + hvely) return emptyMove();
                    belowCoor = horizontalWallCoor;
                    return hm;
                }
            } else if(px > verticalWallCoor) {
                //System.out.println("py: " + py + " coor: " + verticalWallCoor + " leftCoor: " + leftCoor);
                //sc.next();
                if(leftCoor == -1) {
                    hm.wallType = 2;
                    if(verticalWallCoor > hposx + hvelx) return emptyMove();
                    leftCoor = verticalWallCoor;
                    return hm;
                } else if(verticalWallCoor > leftCoor) {
                    hm.wallType = 2;
                    hm.wallsToDel.add(del(false, leftCoor));
                    if(verticalWallCoor > hposx + hvelx) return emptyMove();
                    leftCoor = verticalWallCoor;
                    return hm;
                }
            }
        }
        // Nothing to do so do nothing.
        return emptyMove();
    }

    public PreyMove playPrey() {
        Random random = new Random();
        return new PreyMove(random.nextInt(3) - 1, random.nextInt(3) - 1);
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java BottomRightHunter <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new BottomRightHunter(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }
}