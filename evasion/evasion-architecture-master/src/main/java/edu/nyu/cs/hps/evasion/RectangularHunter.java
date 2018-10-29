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
    int aboveCoor = 300;
    int rightCoor = 300;
    int belowCoor = 0;
    int leftCoor = 0;
    // indices of the walls in play. (-1 means there isn't one there right now).
    int[] wallIndices = {-1, -1, -1, -1};
    // number of wallIndices that doesn't equal -1.
    int wallsInPlay = 0;
    // number of walls built so far. (also index of next wall).
    int wallsBuilt = 0;
    // Build a horizontal (0) or vertical (1) wall next turn.
    int rebuild = 0;

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
        int hposx = gameState.hunterPosAndVel.pos.x;
        int hposy = gameState.hunterPosAndVel.pos.y;
        int hvelx = gameState.hunterPosAndVel.vel.x;
        int hvely = gameState.hunterPosAndVel.vel.y;
        int verticalWallCoor = hposx;
        int horizontalWallCoor = hposy;
        int px = gameState.preyPos.x;
        int py = gameState.preyPos.y;
        // Check the case where there's a wall between hunter and prey.
        // What is code reuse?
        HunterMove hm = new HunterMove();
        ArrayList<Integer> deleteWalls = new ArrayList<>();
        hm.wallsToDel = deleteWalls;
        if(rebuild > 0) {
            hm.wallType = rebuild;
            rebuild = 0;
            wallIndices[0] = wallsBuilt;
            wallsBuilt++;
            aboveCoor = horizontalWallCoor;
            return hm;
        }
        if(gameState.wallTimer < 2) {
            if(hposy + hvely == aboveCoor && hposy > aboveCoor && py < aboveCoor - 1) {
                rebuild = 1;
                hm.wallsToDel.add(del(true, aboveCoor));
                return hm;
            } else if(hposx + hvelx == rightCoor && hposx > rightCoor && px < rightCoor - 1) {
                rebuild = 2;
                hm.wallsToDel.add(del(false, rightCoor));
                return hm;
            }else if(hposy + hvely == belowCoor && hposy < belowCoor && py > belowCoor + 1) {
                rebuild = 1;
                hm.wallsToDel.add(del(true, belowCoor));
                return hm;
            }else if(hposx + hvelx == leftCoor && hposx < leftCoor && px > leftCoor + 1) {
                rebuild = 2;
                hm.wallsToDel.add(del(false, leftCoor));
                return hm;
            }
        }
        if(gameState.wallTimer == 0 && !(hposx < leftCoor || hposy < belowCoor || hposx > rightCoor || hposy > aboveCoor)) {
            if(py < horizontalWallCoor && horizontalWallCoor < aboveCoor) {
                if(hposy + hvely < horizontalWallCoor || horizontalWallCoor - belowCoor > 8) {
                    //System.out.println("py: " + py + " coor: " + horizontalWallCoor + " aboveCoor: " + aboveCoor);
                    //sc.next();
                    hm.wallType = 1;
                    hm.wallsToDel.add(del(true, aboveCoor));
                    wallIndices[0] = wallsBuilt;
                    wallsBuilt++;
                    aboveCoor = horizontalWallCoor;
                    return hm;

                }
            }
            if(px < verticalWallCoor && verticalWallCoor < rightCoor) {
                if(hposx + hvelx < verticalWallCoor || verticalWallCoor - leftCoor > 8) {
                    hm.wallType = 2;
                    hm.wallsToDel.add(del(false, rightCoor));
                    wallIndices[1] = wallsBuilt;
                    wallsBuilt++;
                    rightCoor = verticalWallCoor;
                    return hm;
                }
            }
            if(py > horizontalWallCoor && horizontalWallCoor > belowCoor) {
                if(hposy + hvely > horizontalWallCoor || aboveCoor - horizontalWallCoor > 8) {
                    //System.out.println("here");
                    hm.wallType = 1;
                    hm.wallsToDel.add(del(true, belowCoor));
                    wallIndices[2] = wallsBuilt;
                    wallsBuilt++;
                    belowCoor = horizontalWallCoor;
                    return hm;
                }
            }
            if(px > verticalWallCoor && verticalWallCoor > leftCoor) {
                if(hposx + hvelx > verticalWallCoor || rightCoor - verticalWallCoor > 8) {
                    hm.wallType = 2;
                    hm.wallsToDel.add(del(false, leftCoor));
                    wallIndices[3] = wallsBuilt;
                    wallsBuilt++;
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
            System.out.println("Usage: java RectangularHunter <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new RectangularHunter(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }
}