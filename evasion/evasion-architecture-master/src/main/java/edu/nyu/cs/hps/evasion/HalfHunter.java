package edu.nyu.cs.hps.evasion;

import java.util.ArrayDeque;
import java.util.ArrayList;

import edu.nyu.cs.hps.evasion.game.GameState;
import edu.nyu.cs.hps.evasion.game.HorizontalWall;
import edu.nyu.cs.hps.evasion.game.VerticalWall;

public class HalfHunter implements Hunter {

    private int aboveCoord = 301;
    private int belowCoord = -1;
    private int leftCoord = -1;
    private int rightCoord = 301;
    private ArrayDeque<Integer> leftWalls = new ArrayDeque<>();
    private ArrayDeque<Integer> rightWalls = new ArrayDeque<>();
    private ArrayDeque<Integer> aboveWalls = new ArrayDeque<>();
    private ArrayDeque<Integer> belowWalls = new ArrayDeque<>();
    private boolean isTrappingH = false;
    private boolean isTrappingV = false;
    private GameState gameState;

    @Override
    public void receiveGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public HunterMove playHunter() {

        int hunterLocX = gameState.hunterPosAndVel.pos.x;
        int hunterLocY = gameState.hunterPosAndVel.pos.y;
        int velX = gameState.hunterPosAndVel.vel.x;
        int velY = gameState.hunterPosAndVel.vel.y;
        int preyLocX = gameState.preyPos.x;
        int preyLocY = gameState.preyPos.y;
        if (gameState.wallTimer == 0) {
            if (isTrappingH || isTrappingV) {
                return waitOrRemoveTrapping();
            }
            double currentArea = (aboveCoord - belowCoord) * (rightCoord - leftCoord);
            HunterMove move = new HunterMove();
            boolean foundMove = false;
            if (hunterLocX > leftCoord && preyLocX > hunterLocX) {
                double newArea = (aboveCoord - belowCoord) * (rightCoord - hunterLocX);
                if (velX < 0 && newArea <= currentArea * .4) {
                    isTrappingV = true;
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(false, leftCoord));
                    }
                    leftCoord = hunterLocX;
                    move.wallType = 2;
                    leftWalls.add(leftCoord);
                    foundMove = true;
                } else if (newArea <= currentArea * .6 && velX > 0) {
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(false, leftCoord));
                    }
                    leftCoord = hunterLocX;
                    move.wallType = 2;
                    leftWalls.add(leftCoord);
                    foundMove = true;
                }
            }
            if (!foundMove && hunterLocX < rightCoord && preyLocX < hunterLocX) {
                double newArea = (aboveCoord - belowCoord) * (hunterLocX - leftCoord);
                if (velX > 0 && newArea <= currentArea * .4) {
                    isTrappingV = true;
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(false, rightCoord));
                    }
                    rightCoord = hunterLocX;
                    move.wallType = 2;
                    rightWalls.add(rightCoord);
                    foundMove = true;
                } else if (newArea <= currentArea *.6 && velX < 0) {
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(false, rightCoord));
                    }
                    rightCoord = hunterLocX;
                    move.wallType = 2;
                    rightWalls.add(rightCoord);
                    foundMove = true;
                }
            }
            if (!foundMove && hunterLocY > belowCoord && preyLocY > hunterLocY) {
                double newArea = (aboveCoord - hunterLocY) * (rightCoord - leftCoord);
                if (velY < 0 && newArea <= currentArea * .4) {
                    isTrappingH = true;
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(true, belowCoord));
                    }
                    belowCoord = hunterLocY;
                    move.wallType = 1;
                    belowWalls.add(belowCoord);
                    foundMove = true;
                } else if (newArea <= currentArea * .6 && velY > 0) {
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(true, belowCoord));
                    }
                    belowCoord = hunterLocY;
                    move.wallType = 1;
                    belowWalls.add(belowCoord);
                    foundMove = true;
                }
            }
            if (!foundMove && hunterLocY < aboveCoord  && preyLocY < hunterLocY) {
                double newArea = (hunterLocY - belowCoord) * (rightCoord - leftCoord);
                if (velY > 0 && newArea <= currentArea * .4) {
                    isTrappingH = true;
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(true, aboveCoord));
                    }
                    aboveCoord = hunterLocY;
                    move.wallType = 1;
                    aboveWalls.add(aboveCoord);
                    foundMove = true;
                } else if (newArea <= currentArea * .6 && velY < 0) {
                    if (gameState.walls.size() == gameState.maxWalls) {
                        move.wallsToDel.add(del(true, aboveCoord));
                    }
                    aboveCoord = hunterLocY;
                    move.wallType = 1;
                    aboveWalls.add(aboveCoord);
                    foundMove = true;
                }
            }
            if(foundMove) return move;
        }
        return emptyMove();
    }

    private HunterMove waitOrRemoveTrapping() {
        System.out.println("Here!");
        int hunterLocX = gameState.hunterPosAndVel.pos.x;
        int hunterLocY = gameState.hunterPosAndVel.pos.y;
        int velX = gameState.hunterPosAndVel.vel.x;
        int velY = gameState.hunterPosAndVel.vel.y;
        int preyLocX = gameState.preyPos.x;
        int preyLocY = gameState.preyPos.y;
        HunterMove move = new HunterMove();
        if (isTrappingH) {
            move.wallType = 1;
            if (hunterLocY > preyLocY) {
                if (hunterLocY + velY == aboveCoord) {
                    int farthestAbove = aboveWalls.pollLast();
                    int ind = del(true, farthestAbove);
                    move.wallsToDel.add(ind);
                    isTrappingH = false;
                    aboveCoord = hunterLocY;
                    aboveWalls.add(aboveCoord);
                    return move;
                } else {
                    return emptyMove();
                }
            } else {
                if (hunterLocY + velY == belowCoord) {
                    int farthestBelow = belowWalls.pollLast();
                    int ind = del(true, farthestBelow);
                    move.wallsToDel.add(ind);
                    isTrappingH = false;
                    belowCoord = hunterLocY;
                    belowWalls.add(belowCoord);
                    return move;
                } else {
                    return emptyMove();
                }
            }
        } else {
            move.wallType = 2;
            if (hunterLocX > preyLocX) {
                if (hunterLocX + velX == rightCoord) {
                    int farthestRight = rightWalls.pollLast();
                    int ind = del(false, farthestRight);
                    move.wallsToDel.add(ind);
                    rightCoord = hunterLocX;
                    rightWalls.add(rightCoord);
                    isTrappingV = false;
                    return move;
                } else {
                    return emptyMove();
                }
            } else {
                if (hunterLocX + velX == leftCoord) {
                    int farthestLeft = leftWalls.pollLast();
                    int ind = del(false, farthestLeft);
                    move.wallsToDel.add(ind);
                    leftCoord = hunterLocX;
                    leftWalls.add(leftCoord);
                    isTrappingV = false;
                    return move;
                } else {
                    return emptyMove();
                }
            }
        }
    }

    private HunterMove emptyMove() {
        HunterMove move = new HunterMove();
        move.wallType = 0;
        move.wallsToDel = new ArrayList<>();
        return move;
    }

    private int del(boolean horizontal, int coor) {
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

}
