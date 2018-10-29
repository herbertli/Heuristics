package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.GameState;
import org.locationtech.jts.geom.*;

import java.util.*;

public class SimpleHunter implements Hunter {

    private Point hunterLoc;
    private GeometryFactory factory;
    private List<AugmentedWall> walls;
    private GameState gameState;

    // this should be the box the prey is contained in
    private double minX = 0;
    private double maxX = 300;
    private double minY = 0;
    private double maxY = 300;

    private SimpleHunter() {
        factory = new GeometryFactory();
        walls = new ArrayList<>();
    }

    @Override
    public void receiveGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public HunterMove playHunter() throws Exception {
        if (gameState.maxWalls < 2) {
            throw new Exception("This Hunter needs at least 2 walls to work...");
        }

        // current prey location
        Point preyLoc = factory.createPoint(new Coordinate(this.gameState.preyPos.x, this.gameState.preyPos.y));
        checkPreyContained(preyLoc);

        // current hunter location
        Point currentLoc = factory.createPoint(new Coordinate(
                this.gameState.hunterPosAndVel.pos.x,
                this.gameState.hunterPosAndVel.pos.y));

        // we can place a wall at this tick
        if (gameState.wallTimer == 0 && this.hunterLoc != null) {

            if (walls.size() > 0) {
                // if the last wall is a separating wall..
                AugmentedWall lastWallPlaced = walls.get(walls.size() - 1);
                if (lastWallPlaced.isTrappingWall) {
                    boolean rightDir = lastWallPlaced.line.distance(currentLoc) <
                            lastWallPlaced.line.distance(hunterLoc);
                    double distToLastWall = lastWallPlaced.line.distance(currentLoc);
                    System.out.printf("Distance to separating wall: %.2f\n", distToLastWall);
                    if (distToLastWall <= 2.1 && rightDir) {
                        System.out.println("Replacing separating wall!");
                        HunterMove move = new HunterMove();
                        move.wallType = lastWallPlaced.wallType;
                        move.wallsToDel = new ArrayList<>();
                        move.wallsToDel.add(walls.size() - 1);
                        walls.remove(walls.size() - 1);
                        LineString replacementWall;
                        if (move.wallType == 1) {
                            replacementWall = lineCreatedByHorizontal();
                            maxY = Math.max(maxY, replacementWall.getEndPoint().getY());
                            minY = Math.min(minY, replacementWall.getEndPoint().getY());
                        } else {
                            replacementWall = lineCreatedByVertical();
                            maxX = Math.max(maxX, replacementWall.getEndPoint().getX());
                            minX = Math.min(minX, replacementWall.getEndPoint().getX());
                        }
                        walls.add(new AugmentedWall(false, move.wallType, replacementWall));
                        return move;
                    } else {
                        System.out.println("Waiting to replace separating wall!");
                        return emptyMove();
                    }
                }
            }

            // currentArea of the box containing the prey
            double currentArea = (maxX - minX) * (maxY - minY);
            HunterMove move = new HunterMove();

            double newArea;
            // calculate newArea after placing horizontal wall
            LineString newHoriWall = lineCreatedByHorizontal();
            if (preyLoc.getY() > newHoriWall.getEndPoint().getY() && preyLoc.getY() < maxY) {
                newArea = (maxX - minX) * (maxY - newHoriWall.getEndPoint().getY());
            } else if (preyLoc.getY() < newHoriWall.getEndPoint().getY() && preyLoc.getY() > minY) {
                newArea = (maxX - minX) * (newHoriWall.getEndPoint().getY() - minY);
            } else {
                newArea = 1e9;
            }
            if (newArea < currentArea) {
                boolean canPlace = true;
                move.wallType = 1;
                if (walls.size() == gameState.maxWalls) {
                    // remove farthest away wall
                    TreeMap<AugmentedWall, Integer> m = new TreeMap<>(new Comparator<AugmentedWall>() {
                        @Override
                        public int compare(AugmentedWall o1, AugmentedWall o2) {
                            return Double.compare(o1.line.distance(hunterLoc), o2.line.distance(hunterLoc));
                        }
                    });
                    for (int i = 0; i < walls.size(); i++) {
                        if (walls.get(i).wallType == 1)
                            m.put(walls.get(i), i);
                    }
                    if (m.size() == 0) {
                        canPlace = false;
                    } else {
                        int delInd = m.lastEntry().getValue();
                        move.wallsToDel.add(delInd);
                        walls.remove(delInd);
                    }
                }
                if (canPlace) {
                    // add new wall, and determine whether it separates the hunter and prey
                    boolean isTrappingWall = currentLoc.getY() > maxY || currentLoc.getY() < minY;
                    if (preyLoc.getY() > maxY || preyLoc.getY() < minY) {
                        throw new Exception("!");
                    }
                    if (isTrappingWall) {
                        System.out.println("Adding a horizontal separating wall!!!");
                    } else {
                        // update bounds
                        maxY = Math.max(maxY, newHoriWall.getEndPoint().getY());
                        minY = Math.min(minY, newHoriWall.getEndPoint().getY());
                        System.out.println("Adding a regular horizontal wall!!!");
                    }
                    walls.add(new AugmentedWall(isTrappingWall, 1, newHoriWall));
                    hunterLoc = currentLoc;
                    return move;
                }
            }

            // same process for vertical wall
            LineString newVertWall = lineCreatedByVertical();
            if (preyLoc.getX() > hunterLoc.getX() && preyLoc.getX() < maxX) {
                newArea = (maxY - minY) * (maxX - hunterLoc.getX());
            } else if (preyLoc.getX() < hunterLoc.getX() && preyLoc.getX() > minX) {
                newArea = (maxY - minY) * (hunterLoc.getX() - minX);
            } else {
                hunterLoc = currentLoc;
                return emptyMove();
            }
            if (newArea < currentArea) {
                boolean canPlace = true;
                move.wallType = 2;
                maxX = Math.max(maxX, hunterLoc.getX());
                minX = Math.min(minX, hunterLoc.getX());
                if (walls.size() == gameState.maxWalls) {
                    TreeMap<AugmentedWall, Integer> m = new TreeMap<>(new Comparator<AugmentedWall>() {
                        @Override
                        public int compare(AugmentedWall o1, AugmentedWall o2) {
                            return Double.compare(o1.line.distance(hunterLoc), o2.line.distance(hunterLoc));
                        }
                    });
                    for (int i = 0; i < walls.size(); i++) {
                        if (walls.get(i).wallType == 2)
                            m.put(walls.get(i), i);
                    }
                    if (m.size() == 0) {
                        canPlace = false;
                    } else {
                        int delInd = m.lastEntry().getValue();
                        move.wallsToDel.add(delInd);
                        walls.remove(delInd);
                    }
                }
                if (canPlace) {
                    boolean isTrappingWall = currentLoc.getX() > maxX || currentLoc.getX() < minX;
                    if (preyLoc.getY() > maxY || preyLoc.getY() < minY) {
                        throw new Exception("!");
                    }
                    if (isTrappingWall) {
                        System.out.println("Adding a vertical separating wall!!!");
                    } else {
                        System.out.println("Adding a regular vertical wall!!!");
                    }
                    walls.add(new AugmentedWall(isTrappingWall, 2, newVertWall));
                    hunterLoc = currentLoc;
                    return move;
                }
            }
        }
        hunterLoc = currentLoc;
        return emptyMove();
    }

    private void checkPreyContained(Point preyLoc) throws Exception {
        if (preyLoc.getY() > maxY || preyLoc.getY() < minY || preyLoc.getX() < minX || preyLoc.getX() > maxX) {
            throw new Exception("Prey is not in the box!");
        }
    }

    private HunterMove emptyMove() {
        HunterMove move = new HunterMove();
        move.wallType = 0;
        move.wallsToDel = new ArrayList<>();
        return move;
    }

    private LineString lineCreatedByHorizontal() {
        Coordinate[] coordinates = new Coordinate[2];
        coordinates[0] = new Coordinate(0, this.hunterLoc.getY());
        coordinates[1] = new Coordinate(300, this.hunterLoc.getY());
        return factory.createLineString(coordinates);
    }

    private LineString lineCreatedByVertical() {
        Coordinate[] coordinates = new Coordinate[2];
        coordinates[0] = new Coordinate(this.hunterLoc.getX(), 0);
        coordinates[1] = new Coordinate(this.hunterLoc.getX(), 300);
        return factory.createLineString(coordinates);
    }

    static class AugmentedWall {
        boolean isTrappingWall;
        int wallType;
        LineString line;

        AugmentedWall(boolean isTrappingWall, int wallType, LineString line) {
            this.isTrappingWall = isTrappingWall;
            this.wallType = wallType;
            this.line = line;
        }
    }

}
