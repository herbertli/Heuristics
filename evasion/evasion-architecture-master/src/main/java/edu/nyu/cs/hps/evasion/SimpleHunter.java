package edu.nyu.cs.hps.evasion;

import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.util.*;

public class SimpleHunter extends EvasionClient {

    private Point hunterLoc;
    private GeometryFactory factory;
    List<AugmentedWall> walls;
    double minX = 0;
    double maxX = 300;
    double minY = 0;
    double maxY = 300;

    private SimpleHunter(String name, int port) throws IOException {
        super(name, port);
        factory = new GeometryFactory();
        walls = new ArrayList<>();
    }

    public HunterMove playHunter() throws Exception {
        if (gameState.maxWalls < 2) {
            throw new Exception("This Hunter needs at least 2 walls to work...");
        }

        // current prey location
        Point preyLoc = factory.createPoint(new Coordinate(this.gameState.preyPos.x, this.gameState.preyPos.y));

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
                    if (lastWallPlaced.line.distance(currentLoc) <= 2.1) {
                        HunterMove move = new HunterMove();
                        move.wallType = lastWallPlaced.wallType;
                        move.wallsToDel = new ArrayList<>();
                        move.wallsToDel.add(walls.size() - 1);
                        return move;
                    } else {
                        return emptyMove();
                    }
                }
            }

            // currentArea
            double currentArea = (maxX - minX) * (maxY - minY);
            double newArea;
            HunterMove move = new HunterMove();

            // calculate newArea after placing horizontal wall
            LineString newHoriWall = lineCreatedByHorizontal();
            if (preyLoc.getY() > hunterLoc.getY()) {
                newArea = (maxX - minX) * (maxY - hunterLoc.getY());
            } else {
                newArea = (maxX - minX) * (hunterLoc.getY() - minY);
            }
            if (newArea < currentArea) {
                move.wallType = 1;
                // update bounds
                maxY = Math.max(maxY, hunterLoc.getY());
                minY = Math.min(minY, hunterLoc.getY());
                if (walls.size() == gameState.maxWalls) {
                    // remove farthest away wall
                    TreeMap<AugmentedWall, Integer> m = new TreeMap<>(new Comparator<AugmentedWall>() {
                        @Override
                        public int compare(AugmentedWall o1, AugmentedWall o2) {
                            return Double.compare(o1.line.distance(hunterLoc), o2.line.distance(hunterLoc));
                        }
                    });
                    for (int i = 0; i < walls.size(); i++) {
                        m.put(walls.get(i), i);
                    }
                    int delInd = m.lastEntry().getValue();
                    move.wallsToDel.add(delInd);
                    walls.remove(delInd);
                }
                // add new wall, and determine whether it separates the hunter and prey
                boolean isTrappingWall = hunterLoc.getY() > maxY || hunterLoc.getY() < minY;
                walls.add(new AugmentedWall(isTrappingWall, 1, newHoriWall));
                hunterLoc = currentLoc;
                return move;
            }

            // same process for vertical wall
            LineString newVertWall = lineCreatedByVertical();
            if (preyLoc.getX() > hunterLoc.getX()) {
                newArea = (maxY - minY) * (maxX - hunterLoc.getX());
            } else {
                newArea = (maxY - minY) * (hunterLoc.getX() - minX);
            }
            if (newArea < currentArea) {
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
                        m.put(walls.get(i), i);
                    }
                    int delInd = m.lastEntry().getValue();
                    move.wallsToDel.add(delInd);
                    walls.remove(delInd);
                }
                boolean isTrappingWall = hunterLoc.getX() > maxX || hunterLoc.getX() < minX;
                walls.add(new AugmentedWall(isTrappingWall, 2, newVertWall));
                hunterLoc = currentLoc;
                return move;
            }

        }
        hunterLoc = currentLoc;
        return emptyMove();
    }

    HunterMove emptyMove() {
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
        EvasionClient evasionClient = new SimpleHunter(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
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
