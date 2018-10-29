package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.*;
import org.locationtech.jts.geom.*;


public class SimplePrey implements Prey {

    private GeometryFactory factory = new GeometryFactory();
    private Point currentCentroid;
    private Point preyLoc;
    private Point hunterLoc;
    private Point hunterDir;
    private GameState gameState;

    private int[][] moves = { {0, 1}, {1, 0}, {0, -1}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1} };

    @Override
    public void receiveGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public PreyMove playPrey() {
        PreyMove move = new PreyMove();
        updateFields();
        final int TOLERANCE = 20;
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

}
