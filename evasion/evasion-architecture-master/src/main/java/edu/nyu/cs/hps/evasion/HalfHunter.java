package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.GameState;
import org.locationtech.jts.geom.*;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class HalfHunter implements Hunter {

    private Point hunterLoc;
    private Polygon currentPoly;
    private double currentArea;
    private GeometryFactory factory;
    private ArrayDeque<AugmentedWall> wallsPlaced;
    private GameState gameState;

    HalfHunter() {
        Coordinate[] initialPoints = {
                new Coordinate(0, 0),
                new Coordinate(0, 300),
                new Coordinate(300, 300),
                new Coordinate(300, 0),
                new Coordinate(0, 0)
        };
        wallsPlaced = new ArrayDeque<>();
        factory = new GeometryFactory();
        currentPoly = factory.createPolygon(initialPoints);
        currentArea = currentPoly.getArea();
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

        // current hunter location
        // this.hunterLoc stores previous hunter location
        Point currentLoc = factory.createPoint(new Coordinate(
                this.gameState.hunterPosAndVel.pos.x,
                this.gameState.hunterPosAndVel.pos.y));
        HunterMove move = new HunterMove();

        // we can place a wall at this tick
        if (gameState.wallTimer == 0 && this.hunterLoc != null) {
            // if the last wall we placed is a trapping wall, wait for bounce back
            if (wallsPlaced.size() > 0 && wallsPlaced.peekLast().isTrappingWall) {
                System.out.printf("Distance to trapping wall: %.2f\n", currentLoc.distance(wallsPlaced.peekLast().trappingPoly));

                // check to see if we are bouncing back... if we are, then
                // remove trapping wall and re-place the same exact type of wall on the same tick!
                if (currentLoc.distance(wallsPlaced.peekLast().trappingPoly) < hunterLoc.distance(wallsPlaced.peekLast().trappingPoly) &&
                        currentLoc.isWithinDistance(wallsPlaced.peekLast().trappingPoly, 2.1)) {
                    System.out.println("!!!!!!!!!!!!!!!!!!! Replacing a trapping wall !!!!!!!!!!!!!!!!!!!");
                    Coordinate[] coordinates;
                    if (move.wallType == 1) coordinates = polyCreatedByHorizontal();
                    else coordinates = polyCreatedByVertical();
                    Polygon other = factory.createPolygon(coordinates);
                    Polygon diff = (Polygon) currentPoly.difference(other);
                    move.wallsToDel.add(wallsPlaced.size() - 1);
                    move.wallType = wallsPlaced.peekLast().wallType;
                    wallsPlaced.removeLast();
                    wallsPlaced.add(new AugmentedWall(false, move.wallType, null));
                    currentPoly = diff;
                    currentArea = diff.getArea();
                    hunterLoc = currentLoc;
                    return move;
                } else {
                    System.out.println("Waiting to replace a trapping wall...");
                    move.wallType = 0;
                    move.wallsToDel = new ArrayList<>();
                    hunterLoc = currentLoc;
                    return move;
                }
            }

            for (int i = 1; i <= 2; i++) {
                if (wallsPlaced.size() > 0) {
                    if (i == wallsPlaced.peekLast().wallType) continue;
                }
                Coordinate[] coordinates;
                if (i == 1) coordinates = polyCreatedByHorizontal();
                else coordinates = polyCreatedByVertical();

                Polygon other = factory.createPolygon(coordinates);

                Geometry temp = currentPoly.difference(other);
                if (!(temp instanceof Polygon)) {
                    move.wallType = 0;
                    move.wallsToDel = new ArrayList<>();
                    hunterLoc = currentLoc;
                    return move;
                }
                Polygon diff = (Polygon) temp;

                temp = currentPoly.intersection(other);
                if (!(temp instanceof Polygon)) {
                    move.wallType = 0;
                    move.wallsToDel = new ArrayList<>();
                    hunterLoc = currentLoc;
                    return move;
                }
                Polygon inter = (Polygon) temp;

                // TODO: place wall even if hunter and prey will be separated
                Polygon preyPoly;
                if (diff.contains(preyLoc)) {
                    preyPoly = diff;
                } else {
                    preyPoly = inter;
                }
                double newArea = preyPoly.getArea();
                boolean sameRegion = preyPoly.contains(currentLoc);
                boolean didPlaceWall = false;
                if (sameRegion && newArea <= .6 * currentArea) {
                    System.out.println("Placed a regular wall!");
                    wallsPlaced.add(new AugmentedWall(false, i, null));
                    didPlaceWall = true;
                    currentArea = newArea;
                    currentPoly = diff;
                } else if (!sameRegion && newArea <= .3 * currentArea) {
                    System.out.println("Placed a trapping wall!");
                    wallsPlaced.add(new AugmentedWall(true, i, preyPoly));
                    didPlaceWall = true;
                }
                if (didPlaceWall) {
                    if (gameState.walls.size() == this.gameState.maxWalls) {
                        move.wallsToDel = new ArrayList<>();
                        move.wallsToDel.add(0);
                        wallsPlaced.removeFirst();
                    }
                    move.wallType = i;
                    hunterLoc = currentLoc;
                    return move;
                }

            }
        }
        move.wallType = 0;
        move.wallsToDel = new ArrayList<>();
        hunterLoc = currentLoc;
        return move;
    }

    private Coordinate[] polyCreatedByHorizontal() {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(0, this.hunterLoc.getY());
        coordinates[1] = new Coordinate(300, this.hunterLoc.getY());
        if (this.gameState.hunterPosAndVel.vel.y < 0) {
            coordinates[2] = new Coordinate(300, 300);
            coordinates[3] = new Coordinate(0, 300);
        } else {
            coordinates[2] = new Coordinate(300, 0);
            coordinates[3] = new Coordinate(0, 0);
        }
        coordinates[4] = coordinates[0];
        return coordinates;
    }

    private Coordinate[] polyCreatedByVertical() {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(this.hunterLoc.getX(), 0);
        coordinates[1] = new Coordinate(this.hunterLoc.getX(), 300);
        if (this.gameState.hunterPosAndVel.vel.x < 0) {
            coordinates[2] = new Coordinate(300, 300);
            coordinates[3] = new Coordinate(300, 0);
        } else {
            coordinates[2] = new Coordinate(0, 300);
            coordinates[3] = new Coordinate(0, 0);
        }
        coordinates[4] = coordinates[0];
        return coordinates;
    }

    static class AugmentedWall {
        boolean isTrappingWall;
        int wallType;
        Polygon trappingPoly;

        AugmentedWall(boolean isTrappingWall, int wallType, Polygon trappingPoly) {
            this.isTrappingWall = isTrappingWall;
            this.wallType = wallType;
            this.trappingPoly = trappingPoly;
        }
    }

}
