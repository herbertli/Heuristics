package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.*;
import org.locationtech.jts.geom.*;

import java.io.IOException;


public class SimpleClient extends RandomClient {

    private GeometryFactory factory;
    private Point currentCentroid;
    private Point preyLoc;
    private Point hunterLoc;
    private Point hunterDir;
    private final int TOLERANCE = 20;

    private int[][] moves = { {0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1} };

    private SimpleClient(String name, int port) throws IOException {
        super(name, port);
        factory = new GeometryFactory();
    }

    @Override
    public PreyMove playPrey() {
        PreyMove move = new PreyMove();
        updateFields();
        if (preyLoc.distance(hunterLoc) <= TOLERANCE) {
            double maxDist = preyLoc.distance(hunterLoc);
            int[] bestMove = null;
            for (int[] dir: moves) {
                Point newLoc = factory.createPoint(new Coordinate(preyLoc.getX() + dir[0], preyLoc.getY() + dir[1]));
                double dist = hunterLoc.distance(newLoc);
                if (dist > maxDist) {
                    maxDist = dist;
                    bestMove = dir;
                }
            }
            if (bestMove == null) {
                System.out.println("!!!!!!!!!!! Prey Move not found! !!!!!!!!!!!!!!!!!");
                System.exit(1);
            }
            move.x = bestMove[0];
            move.y = bestMove[1];
            return move;
        } else {
            updateCentroid();
        }
        System.out.println(currentCentroid.toText());

        double minDist = 1e9;
        int[] bestMove = null;
        for (int[] dir: moves) {
            Point newLoc = factory.createPoint(new Coordinate(preyLoc.getX() + dir[0], preyLoc.getY() + dir[1]));
            double dist = currentCentroid.distance(newLoc);
            if (dist < minDist) {
                minDist = dist;
                bestMove = dir;
            }
        }
        if (bestMove == null) {
            System.out.println("!!!!!!!!!!! Prey Move not found! !!!!!!!!!!!!!!!!!");
            System.exit(1);
        }
        move.x = bestMove[0];
        move.y = bestMove[1];
        return move;
    }

    private void updateFields() {
        preyLoc = factory.createPoint(new Coordinate(gameState.preyPos.x, gameState.preyPos.y));
        hunterDir = factory.createPoint(new Coordinate(gameState.hunterPosAndVel.vel.x, gameState.hunterPosAndVel.vel.y));
        hunterLoc = factory.createPoint(new Coordinate(gameState.hunterPosAndVel.pos.x, gameState.hunterPosAndVel.pos.y));
    }

    private void updateCentroid() {
        Coordinate[] initialPoints = {
                new Coordinate(0, 0),
                new Coordinate(0, 300),
                new Coordinate(300, 300),
                new Coordinate(300, 0),
                new Coordinate(0, 0)
        };
        Geometry preyGeometry = factory.createPolygon(initialPoints);

        /*
        for (Wall w: gameState.walls) {
            Polygon polyCreatedByWall = null;
            if (w instanceof VerticalWall) {
                polyCreatedByWall = polyCreatedByVertical((VerticalWall) w);
            } else if (w instanceof HorizontalWall) {
                polyCreatedByWall = polyCreatedByHorizontal((HorizontalWall) w);
            } else if (w instanceof DiagonalWall) {
                polyCreatedByWall = polyCreatedByDiagonal((DiagonalWall) w);
            } else if (w instanceof CounterDiagonalWall) {
                polyCreatedByWall = polyCreatedByCounterDiagonal((CounterDiagonalWall) w);
            }
            if (polyCreatedByWall != null) {
                boolean foundGeo = false;
                try {
                    Geometry diff = preyGeometry.difference(polyCreatedByWall);
                    for (int i = 0; i < diff.getNumGeometries(); i++) {
                        if (diff.getGeometryN(i).contains(preyLoc)) {
                            preyGeometry = diff.getGeometryN(i);
                            foundGeo = true;
                            break;
                        }
                    }
                } catch (Exception e) {

                }
                try {
                    Geometry inter = preyGeometry.intersection(polyCreatedByWall);
                    if (!foundGeo) {
                        for (int i = 0; i < inter.getNumGeometries(); i++) {
                            if (inter.getGeometryN(i).contains(preyLoc)) {
                                preyGeometry = inter.getGeometryN(i);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }
        */

        Polygon trajPoly = polyCreatedByTrajectory(hunterLoc, hunterDir);
        Geometry diff = preyGeometry.difference(trajPoly);
        boolean foundGeo = false;
        for (int i = 0; i < diff.getNumGeometries(); i++) {
            if (diff.getGeometryN(i).contains(preyLoc)) {
                preyGeometry = diff.getGeometryN(i);
                foundGeo = true;
                break;
            }
        }
        if (!foundGeo) {
            Geometry inter = preyGeometry.intersection(trajPoly);
            for (int i = 0; i < inter.getNumGeometries(); i++) {
                if (inter.getGeometryN(i).contains(preyLoc)) {
                    preyGeometry = inter.getGeometryN(i);
                    break;
                }
            }
        }

        if (preyGeometry == null) {
            System.out.println("!!!!!!!!!!! Prey Poly not found! !!!!!!!!!!!!!!!!!");
            System.exit(1);
        }
        currentCentroid = preyGeometry.getCentroid();
    }

    private Polygon polyCreatedByTrajectory(Point loc, Point vel) {
        Coordinate[] coordinates = new Coordinate[4];
        coordinates[0] = new Coordinate(loc.getX() + 1000 * vel.getX(), loc.getY() + 1000 * vel.getY());
        coordinates[1] = new Coordinate(loc.getX() + 1000 * loc.getX(), 0);
        coordinates[0] = new Coordinate(loc.getX() - 1000 * vel.getX(), loc.getY() - 1000 * vel.getY());
        coordinates[3] = coordinates[0];
        return factory.createPolygon(coordinates);
    }

    private Polygon polyCreatedByDiagonal(DiagonalWall d) {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(d.x1, d.y1);
        coordinates[1] = new Coordinate(d.x2, d.y2);
        coordinates[2] = new Coordinate(300, d.y2);
        coordinates[3] = new Coordinate(300, d.y1);
        coordinates[4] = coordinates[0];
        return factory.createPolygon(coordinates);
    }

    private Polygon polyCreatedByCounterDiagonal(CounterDiagonalWall c) {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(c.x1, c.y1);
        coordinates[1] = new Coordinate(c.x2, c.y2);
        coordinates[2] = new Coordinate(c.x2, 300);
        coordinates[3] = new Coordinate(c.x1, 300);
        coordinates[4] = coordinates[0];
        return factory.createPolygon(coordinates);
    }

    private Polygon polyCreatedByHorizontal(HorizontalWall h) {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(h.x1, h.y);
        coordinates[1] = new Coordinate(h.x2, h.y);
        coordinates[2] = new Coordinate(h.x2, 300);
        coordinates[3] = new Coordinate(h.x1, 300);
        coordinates[4] = coordinates[0];
        return factory.createPolygon(coordinates);
    }

    private Polygon polyCreatedByVertical(VerticalWall v) {
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(v.x, v.y1);
        coordinates[1] = new Coordinate(v.x, v.y2);
        coordinates[2] = new Coordinate(300, v.y2);
        coordinates[3] = new Coordinate(300, v.y1);
        coordinates[4] = coordinates[0];
        return factory.createPolygon(coordinates);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java SimpleClient <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new SimpleClient(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }
}
