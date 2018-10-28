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

public class RectangularHunter extends EvasionClient {

    // above/right/below/left are with respect to the prey.
    int aboveCoor = -1;
    int rightCoor = -1;
    int belowCoor = -1;
    int leftCoor = -1;
    // indices of the walls in play. (-1 means there isn't one there right now).
    int[] wallIndices = {-1, -1, -1, -1};
    // number of wallIndices that doesn't equal -1.
    int wallsInPlay = 0;
    // number of walls built so far. (also index of next wall).
    int wallsBuilt = 0;
    // index of the wall seperating hunter from prey. If -1 then doesn't exist.
    int trappingWall = -1;

    Scanner sc;

    private RectangularHunter(String name, int port) throws IOException {
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
            // Check the case where there's a wall between hunter and prey.
            // What is code reuse?
            if(trappingWall == 0 && (hposy + hvely == aboveCoor)) {
                hm.wallType = 1;
                hm.wallsToDel.add(del(true, aboveCoor));
                wallIndices[0] = wallsBuilt;
                wallsBuilt++;
                aboveCoor = horizontalWallCoor;
                trappingWall = -1;
                return hm;
            } else if(trappingWall == 1 && (hposx + hvelx == rightCoor)) {
                hm.wallType = 2;
                hm.wallsToDel.add(del(false, rightCoor));
                wallIndices[1] = wallsBuilt;
                wallsBuilt++;
                rightCoor = verticalWallCoor;
                trappingWall = -1;
                return hm;
            } else if(trappingWall == 2 && (hposy + hvely == belowCoor)) {
                hm.wallType = 1;
                hm.wallsToDel.add(del(true, belowCoor));
                wallIndices[2] = wallsBuilt;
                wallsBuilt++;
                belowCoor = horizontalWallCoor;
                trappingWall = -1;
                return hm;
            } else if(trappingWall == 3 && (hposx + hvelx == leftCoor)) {
                hm.wallType = 2;
                hm.wallsToDel.add(del(false, leftCoor));
                wallIndices[3] = wallsBuilt;
                wallsBuilt++;
                leftCoor = verticalWallCoor;
                trappingWall = -1;
                return hm;
            }
            if(trappingWall == -1) {
                if(py < horizontalWallCoor) {
                    if(aboveCoor == -1) {
                        hm.wallType = 1;
                        wallIndices[0] = wallsBuilt;
                        wallsBuilt++;
                        wallsInPlay++;
                        if(horizontalWallCoor < hposy + hvely) trappingWall = 0;
                        aboveCoor = horizontalWallCoor;
                        return hm;
                    } else if(horizontalWallCoor < aboveCoor) {
                        //System.out.println("py: " + py + " coor: " + horizontalWallCoor + " aboveCoor: " + aboveCoor);
                        //sc.next();
                        hm.wallType = 1;
                        hm.wallsToDel.add(del(true, aboveCoor));
                        wallIndices[0] = wallsBuilt;
                        wallsBuilt++;
                        if(horizontalWallCoor < hposy + hvely) trappingWall = 0;
                        aboveCoor = horizontalWallCoor;
                        return hm;
                    }
                } else if(px < verticalWallCoor) {
                    //System.out.println("px: " + px + " coor: " + verticalWallCoor + " rightCoor: " + rightCoor + " trap: " + trappingWall);
                    //sc.next();
                    if(rightCoor == -1) {
                        hm.wallType = 2;
                        wallIndices[1] = wallsBuilt;
                        wallsBuilt++;
                        wallsInPlay++;
                        if(verticalWallCoor < hposx + hvelx) trappingWall = 1;
                        rightCoor = verticalWallCoor;
                        return hm;
                    } else if(verticalWallCoor < rightCoor) {
                        hm.wallType = 2;
                        hm.wallsToDel.add(del(false, rightCoor));
                        wallIndices[1] = wallsBuilt;
                        wallsBuilt++;
                        if(verticalWallCoor < hposx + hvelx) trappingWall = 1;
                        rightCoor = verticalWallCoor;
                        return hm;
                    }
                } else if(py > horizontalWallCoor) {
                    //System.out.println("py: " + py + " coor: " + horizontalWallCoor + " belowCoor: " + belowCoor);
                    //sc.next();
                    if(belowCoor == -1) {
                        //System.out.println("there");
                        hm.wallType = 1;
                        wallIndices[2] = wallsBuilt;
                        wallsBuilt++;
                        wallsInPlay++;
                        if(horizontalWallCoor > hposy + hvely) trappingWall = 2;
                        belowCoor = horizontalWallCoor;
                        return hm;
                    } else if(horizontalWallCoor > belowCoor) {
                        //System.out.println("here");
                        hm.wallType = 1;
                        hm.wallsToDel.add(del(true, belowCoor));
                        wallIndices[2] = wallsBuilt;
                        wallsBuilt++;
                        if(horizontalWallCoor > hposy + hvely) trappingWall = 2;
                        belowCoor = horizontalWallCoor;
                        return hm;
                    }
                } else if(px > verticalWallCoor) {
                    //System.out.println("py: " + py + " coor: " + verticalWallCoor + " leftCoor: " + leftCoor);
                    //sc.next();
                    if(leftCoor == -1) {
                        hm.wallType = 2;
                        wallIndices[3] = wallsBuilt;
                        wallsBuilt++;
                        wallsInPlay++;
                        if(verticalWallCoor > hposx + hvelx) trappingWall = 3;
                        leftCoor = verticalWallCoor;
                        return hm;
                    } else if(verticalWallCoor > leftCoor) {
                        hm.wallType = 2;
                        hm.wallsToDel.add(del(false, leftCoor));
                        wallIndices[3] = wallsBuilt;
                        wallsBuilt++;
                        if(verticalWallCoor > hposx + hvelx) trappingWall = 3;
                        leftCoor = verticalWallCoor;
                        return hm;
                    }
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
            System.out.println("Usage: java RectangularHunter <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new RectangularHunter(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }
}