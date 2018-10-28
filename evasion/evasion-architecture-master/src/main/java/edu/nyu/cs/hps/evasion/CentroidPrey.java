package edu.nyu.cs.hps.evasion;

import java.util.*;

import edu.nyu.cs.hps.evasion.game.GameState;
import edu.nyu.cs.hps.evasion.game.PositionAndVelocity;
import edu.nyu.cs.hps.evasion.game.Wall;
import edu.nyu.cs.hps.evasion.EvasionPoint;
import org.locationtech.jts.geom.*;

import java.awt.Point;
import java.io.IOException;

public class CentroidPrey extends EvasionClient {

    private CentroidPrey(String name, int port) throws IOException {
        super(name, port);
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java HalfClient <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new CentroidPrey(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }


    public HunterMove playHunter() throws Exception {
        List<Integer> wallsToDel = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < this.gameState.walls.size(); i++) {
            int toDel = random.nextInt(31);
            if (toDel == 0) {
                wallsToDel.add(i);
            }
        }

        int wallType = random.nextInt(5);
        if (this.gameState.maxWalls <= this.gameState.walls.size() - wallsToDel.size()) {
            wallType = 0;
        }
        return new HunterMove(wallType, wallsToDel);
    }

    GameState fakeGameState;

    private boolean isOccupied(EvasionPoint p) {
        if (p.x < 0 || p.x >= fakeGameState.boardSize.x || p.y < 0 || p.y >= fakeGameState.boardSize.y) {
            return true;
        }
        for (Wall wall : fakeGameState.walls) {
            if (wall.occupies(p)) {
                return true;
            }
        }
        return false;
    }

    private static EvasionPoint add(EvasionPoint a, EvasionPoint b) {
        return new EvasionPoint(a.x + b.x, a.y + b.y);
    }

    private PositionAndVelocity move(PositionAndVelocity posAndVel) {
        PositionAndVelocity newPosAndVel = new PositionAndVelocity(posAndVel);
        EvasionPoint newPos = new EvasionPoint(posAndVel.pos.x, posAndVel.pos.y);
        newPosAndVel.vel.x = Math.min(Math.max(newPosAndVel.vel.x, -1), 1);
        newPosAndVel.vel.y = Math.min(Math.max(newPosAndVel.vel.y, -1), 1);
        EvasionPoint newVel = new EvasionPoint(posAndVel.vel.x, posAndVel.vel.y);
        EvasionPoint target = add(newPos, newVel);
        if (!isOccupied(target)) {
            newPosAndVel.pos = target;
        } else {
            if (newPosAndVel.vel.x == 0 || newPosAndVel.vel.y == 0) {
                if (newPosAndVel.vel.x != 0) {
                    newPosAndVel.vel.x = -newPosAndVel.vel.x;
                } else {
                    newPosAndVel.vel.y = -newPosAndVel.vel.y;
                }
            } else {
                boolean oneRight = isOccupied(add(newPos, new EvasionPoint(newPosAndVel.vel.x, 0)));
                boolean oneUp = isOccupied(add(newPos, new EvasionPoint(0, newPosAndVel.vel.y)));
                if (oneRight && oneUp) {
                    newPosAndVel.vel.x = -newPosAndVel.vel.x;
                    newPosAndVel.vel.y = -newPosAndVel.vel.y;
                } else if (oneRight) {
                    newPosAndVel.vel.x = -newPosAndVel.vel.x;
                    newPosAndVel.pos.y = target.y;
                } else if (oneUp) {
                    newPosAndVel.vel.y = -newPosAndVel.vel.y;
                    newPosAndVel.pos.x = target.x;
                } else {
                    boolean twoUpOneRight = isOccupied(
                            add(newPos, new EvasionPoint(newPosAndVel.vel.x, newPosAndVel.vel.y * 2)));
                    boolean oneUpTwoRight = isOccupied(
                            add(newPos, new EvasionPoint(newPosAndVel.vel.x * 2, newPosAndVel.vel.y)));
                    if ((twoUpOneRight && oneUpTwoRight) || (!twoUpOneRight && !oneUpTwoRight)) {
                        newPosAndVel.vel.x = -newPosAndVel.vel.x;
                        newPosAndVel.vel.y = -newPosAndVel.vel.y;
                    } else if (twoUpOneRight) {
                        newPosAndVel.vel.x = -newPosAndVel.vel.x;
                        newPosAndVel.pos.y = target.y;
                    } else {
                        newPosAndVel.vel.y = -newPosAndVel.vel.y;
                        newPosAndVel.pos.x = target.x;
                    }
                }
            }
        }
        return newPosAndVel;
    }

    static class MoveTriple {
        int x, y, t;

        public MoveTriple(int x, int y, int t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }
    }

    public ArrayList<MoveTriple> simulateTick(int numTurns) {
        //PositionAndVelocity hunter = this.gameState.hunterPosAndVel;
        ArrayList<MoveTriple> hunterMoves = new ArrayList<>();
        fakeGameState = new GameState(gameState.maxWalls, gameState.wallPlacementDelay);
        EvasionPoint hunterPos = new EvasionPoint(gameState.hunterPosAndVel.pos.x, gameState.hunterPosAndVel.pos.y);
        EvasionPoint hunterVel = new EvasionPoint(gameState.hunterPosAndVel.vel.x, gameState.hunterPosAndVel.vel.y);
        fakeGameState.hunterPosAndVel = new PositionAndVelocity(hunterPos, hunterVel);
        fakeGameState.hunterPosAndVel = gameState.hunterPosAndVel;
        fakeGameState.walls = gameState.walls;
        for (int i = 0; i < numTurns; i++) {
            // current hunter location
            // this.hunterLoc stores previous hunter location
            //EvasionPoint currentLoc = new EvasionPoint(hunter.pos.x, hunter.pos.y);
            fakeGameState.hunterPosAndVel = move(fakeGameState.hunterPosAndVel);
            hunterMoves.add(new MoveTriple(fakeGameState.hunterPosAndVel.pos.x, fakeGameState.hunterPosAndVel.pos.y, i + 1));
        }
        return hunterMoves;
    }

    /**
    public ArrayList<MoveTriple> bfs(ArrayList<MoveTriple> hunterMoves, int numTurns) {
        int[] dir = new int[]{-1, -1, -1, 0, -1, 1, 0, -1, 0, 0, 0, 1, 1, -1, 1, 0, 1, 1};
        Queue<MoveTriple> queue = new LinkedList<>();
        MoveTriple prey = new MoveTriple(gameState.preyPos.x, gameState.preyPos.y, 0);
        queue.add(prey);

    }
    */

    public EvasionPoint bfsCentroid(){
        fakeGameState = new GameState(gameState.maxWalls, gameState.wallPlacementDelay);
        EvasionPoint hunterPos = new EvasionPoint(gameState.hunterPosAndVel.pos.x, gameState.hunterPosAndVel.pos.y);
        EvasionPoint hunterVel = new EvasionPoint(gameState.hunterPosAndVel.vel.x, gameState.hunterPosAndVel.vel.y);
        fakeGameState.hunterPosAndVel = new PositionAndVelocity(hunterPos, hunterVel);
        fakeGameState.hunterPosAndVel = gameState.hunterPosAndVel;
        fakeGameState.walls = gameState.walls;
        int[] dir = new int[]{-1, -1, -1, 0, -1, 1, 0, -1, 0, 0, 0, 1, 1, -1, 1, 0, 1, 1};
        Queue<MoveTriple> queue = new LinkedList<>();
        MoveTriple prey = new MoveTriple(gameState.preyPos.x, gameState.preyPos.y, 0);
        queue.add(prey);
        double xsum = 0;
        double ysum = 0;
        int pointCount = 0;
        boolean[][] visited = new boolean[305][305];
        while(!queue.isEmpty()){
            MoveTriple next = queue.poll();
            int nx = next.x;
            int ny = next.y;
            if(nx < 0 || ny < 0 || nx > 300 || ny > 300 || visited[nx][ny]) continue;
            visited[nx][ny] = true;
            if(isOccupied(new EvasionPoint(nx, ny))) continue;
            xsum+=nx; ysum+=ny; pointCount++;
            for(int i = 0; i < dir.length; i+=2){
                queue.add(new MoveTriple(nx+dir[i], ny+dir[i+1], 0));
            }
        }
        return new EvasionPoint((int)Math.round(xsum/pointCount), (int)Math.round(ysum/pointCount));
    }

    // calculate centroid
    // calculate safe path towards centroid using a*
    // move towards closest safepoint near centroid
    EvasionPoint centroid;
    public PreyMove playPrey() {
        //ArrayList<MoveTriple> hunterMoves = simulateTick(20);
        //bfs
        if(centroid == null || gameState.wallPlacementDelay - gameState.wallTimer < 2){
            centroid = bfsCentroid();
        }
        PreyMove pm = new PreyMove(centroid.x - gameState.preyPos.x, centroid.y - gameState.preyPos.y);
        if(pm.x != 0) pm.x/=Math.abs(pm.x);
        if(pm.y != 0) pm.y/=Math.abs(pm.y);
        return pm;
        /**
        Random random = new Random();
        return new PreyMove(random.nextInt(3) - 1, random.nextInt(3) - 1);
        */
    }



}