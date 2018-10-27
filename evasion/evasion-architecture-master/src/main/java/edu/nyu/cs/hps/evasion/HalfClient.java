package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.Wall;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

public class HalfClient extends EvasionClient {

    Point hunterLoc;
    Point preyLoc;
    Polygon currentPoly;
    double currentArea;
    GeometryFactory factory;
    ArrayDeque<AugmentedWall> wallsPlaced;

    HalfClient(String name, int port) throws IOException {
        super(name, port);
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

    public HunterMove playHunter() throws Exception {
        if (gameState.maxWalls < 2) {
            throw new Exception("This Hunter needs at least 2 walls to work...");
        }

        preyLoc = factory.createPoint(new Coordinate(this.gameState.preyPos.x, this.gameState.preyPos.y));
        Point currentLoc = factory.createPoint(new Coordinate(
                this.gameState.hunterPosAndVel.pos.x,
                this.gameState.hunterPosAndVel.pos.y));
        HunterMove move = new HunterMove();

        if (gameState.wallTimer == 0 && this.hunterLoc != null) {

            // TODO: if the last wall we placed is a trapping wall, wait for bounce back
            // and then remove and re-place the same exact wall on the same tick!
//            if (wallsPlaced.size() > 0 && wallsPlaced.peek().isTrappingWall) {
//                if (currentLoc.distance(wallsPlaced.peek().lineString) <= 1) {
//                    move.wallsToDel.add(wallsPlaced.size() - 1);
//                    move.wallType = wallsPlaced.peek().wallType;
//                    return move;
//                }
//            }

            // horizontal
            for (int i = 1; i <= 2; i++) {
                if (wallsPlaced.size() > 0) {
                    if (i == wallsPlaced.peekLast().wallType) continue;
                }
                Coordinate[] coords = new Coordinate[5];
                LineString lineString;
                if (i == 1) {
                    // horizontal wall
                    coords[0] = new Coordinate(0, this.hunterLoc.getY());
                    coords[1] = new Coordinate(300, this.hunterLoc.getY());
                    lineString = factory.createLineString(new Coordinate[]{coords[0], coords[1]});
                    if (this.gameState.hunterPosAndVel.vel.y < 0) {
                        coords[2] = new Coordinate(300, 300);
                        coords[3] = new Coordinate(0, 300);
                    } else {
                        coords[2] = new Coordinate(300, 0);
                        coords[3] = new Coordinate(0, 0);
                    }
                } else {
                    // vertical wall
                    coords[0] = new Coordinate(this.hunterLoc.getX(), 0);
                    coords[1] = new Coordinate(this.hunterLoc.getX(), 300);
                    lineString = factory.createLineString(new Coordinate[]{coords[0], coords[1]});
                    if (this.gameState.hunterPosAndVel.vel.x < 0) {
                        coords[2] = new Coordinate(300, 300);
                        coords[3] = new Coordinate(300, 0);
                    } else {
                        coords[2] = new Coordinate(0, 300);
                        coords[3] = new Coordinate(0, 0);
                    }
                }
                coords[4] = coords[0];
                Polygon other = factory.createPolygon(factory.createLinearRing(coords));
                Polygon diff = (Polygon) currentPoly.difference(other);

                // TODO: place wall even if hunter and prey will be separated
//                Polygon preyPoly;
//                if (diff.contains(preyLoc)) {
//                    preyPoly = diff;
//
//                } else {
//                    preyPoly = other;
//                }
//                double newArea = preyPoly.getArea();
//                boolean sameRegion = preyPoly.contains(currentLoc);
//                boolean didPlaceWall = false;
//                if (!sameRegion && newArea < .2 * currentArea) {
//                    wallsPlaced.add(new AugmentedWall(true, i, lineString));
//                    didPlaceWall = true;
//                } else if (sameRegion && newArea < .5 * currentArea) {
//                    wallsPlaced.add(new AugmentedWall(false, i, lineString));
//                    didPlaceWall = true;
//                }
//                if (didPlaceWall) {
//                    if (gameState.walls.size() == this.gameState.maxWalls) {
//                        move.wallsToDel = new ArrayList<>();
//                        move.wallsToDel.add(0);
//                        wallsPlaced.removeFirst();
//                    }
//                    currentArea = newArea;
//                    currentPoly = diff;
//                    break;
//                }

                double newArea = diff.getArea();
                boolean sameRegion = diff.contains(preyLoc) && diff.contains(currentLoc);
                if (newArea <= currentArea * .5 && sameRegion) {
                    move.wallType = i;
                    if (gameState.walls.size() == this.gameState.maxWalls) {
                        move.wallsToDel = new ArrayList<>();
                        move.wallsToDel.add(0);
                        wallsPlaced.removeFirst();
                    }
                    wallsPlaced.add(new AugmentedWall(false, i, lineString));
                    currentArea = newArea;
                    currentPoly = diff;
                    break;
                }

            }
        } else {
            move.wallType = 0;
            move.wallsToDel = new ArrayList<>();
        }
        hunterLoc = currentLoc;
        return move;
    }

    public PreyMove playPrey() {
        Random random = new Random();
        return new PreyMove(random.nextInt(3) - 1, random.nextInt(3) - 1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java HalfClient <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new HalfClient(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }

    static class AugmentedWall {
        boolean isTrappingWall;
        int wallType;
        LineString lineString;

        public AugmentedWall(boolean isTrappingWall, int wallType, LineString lineString) {
            this.isTrappingWall = isTrappingWall;
            this.wallType = wallType;
            this.lineString = lineString;
        }
    }

}
