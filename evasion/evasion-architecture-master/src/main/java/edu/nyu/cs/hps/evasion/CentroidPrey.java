package edu.nyu.cs.hps.evasion;

import java.util.*;

import edu.nyu.cs.hps.evasion.game.GameState;
import edu.nyu.cs.hps.evasion.game.PositionAndVelocity;
import edu.nyu.cs.hps.evasion.game.Wall;

import java.awt.Point;

class EvasionPoint extends Point {

    EvasionPoint(int x, int y) {
        super(x, y);
    }

}

public class CentroidPrey implements Prey {

    GameState gameState;
    GameState fakeGameState;

    @Override
    public void receiveGameState(GameState gameState) {
        this.gameState = gameState;
    }

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
        MoveTriple prev;

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
        hunterMoves.add(new MoveTriple(gameState.hunterPosAndVel.pos.x, gameState.hunterPosAndVel.pos.y, 0));
        for (int i = 0; i < numTurns + 1; i++) {
            // current hunter location
            // this.hunterLoc stores previous hunter location
            //EvasionPoint currentLoc = new EvasionPoint(hunter.pos.x, hunter.pos.y);
            fakeGameState.hunterPosAndVel = move(fakeGameState.hunterPosAndVel);
            hunterMoves.add(new MoveTriple(fakeGameState.hunterPosAndVel.pos.x, fakeGameState.hunterPosAndVel.pos.y, i + 1));
        }
        return hunterMoves;
    }

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

    int dangerCheck() {
        int xd = Math.abs(gameState.hunterPosAndVel.pos.x - gameState.preyPos.x);
        int yd = Math.abs(gameState.hunterPosAndVel.pos.y - gameState.preyPos.y);
        return Math.max(xd, yd);
    }

    double squaredDistance(EvasionPoint a, EvasionPoint b) {
        return (a.getX()-b.getX())*(a.getX()-b.getX())+(a.getY()-b.getY())*(a.getY()-b.getY());
    }

    double squaredDistance(MoveTriple a, MoveTriple b) {
        return (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y);
    }

    class MoveTowardsCentroidComparator implements Comparator<MoveTriple> {
        EvasionPoint centroid;
        public MoveTowardsCentroidComparator(EvasionPoint centroid){
            this.centroid = centroid;
        }
        public int compare(MoveTriple a, MoveTriple b){
            if(a.t == b.t) return Double.compare(squaredDistance(new EvasionPoint(a.x, a.y), centroid),
                squaredDistance(new EvasionPoint(b.x, b.y), centroid));
            return a.t - b.t;
        }
    }

    LinkedList<MoveTriple> findPath(ArrayList<MoveTriple> hunterMoves, int numTurns) {
        LinkedList<MoveTriple> moves = new LinkedList<>();
        PriorityQueue<MoveTriple> queue = new PriorityQueue<>(new MoveTowardsCentroidComparator(centroid));
        queue.add(new MoveTriple(gameState.preyPos.x, gameState.preyPos.y, 0));
        boolean [][][] visited = new boolean[305][305][numTurns+1];
        int[] dir = new int[]{-1, -1, -1, 0, -1, 1, 0, -1, 0, 0, 0, 1, 1, -1, 1, 0, 1, 1};
        while(!queue.isEmpty()){
            MoveTriple cur = queue.poll();
            if(visited[cur.x][cur.y][cur.t]) continue;
            visited[cur.x][cur.y][cur.t] = true;
            if(cur.t > 0 && squaredDistance(cur, hunterMoves.get(cur.t-1)) <= 20) continue;
            if(squaredDistance(cur, hunterMoves.get(cur.t)) <= 20) continue;
            if(squaredDistance(cur, hunterMoves.get(cur.t+1)) <= 20) continue;
            if(cur.t >= numTurns){
                while(cur.prev != null){ //Note that you don't add the t = 0 move (because you're already there).
                    moves.addFirst(cur);
                    cur = cur.prev;
                }
                break;
            }
            for(int i = 0; i < dir.length; i+=2){
                MoveTriple next = new MoveTriple(cur.x+dir[i], cur.y+dir[i+1], cur.t+2);
                next.prev = cur;
                queue.add(next);
            }
        }
        return moves;
    }

    // calculate centroid
    // calculate safe path towards centroid using a*
    // move towards closest safepoint near centroid
    EvasionPoint centroid;
    Queue<MoveTriple> futureMoves = new LinkedList<>();
    public PreyMove playPrey() {
        // if new wall has been placed
        //     see if we're in danger (dist from prey to hunter < 20)
        //          if so, calculate a path for next 10 moves that avoids danger.
        //          if not, then calculate and move towards centroid
        //  otherwise, if we already have moves planned, do them
        //  otherwise move towards centroid
        boolean inDanger = dangerCheck() <= 20;
        if(centroid == null || gameState.wallPlacementDelay - gameState.wallTimer < 2){
            System.out.println("a");
            if(inDanger) {
                ArrayList<MoveTriple> hunterMoves = simulateTick(20);
                futureMoves = findPath(hunterMoves, 20);
                if(futureMoves.size() == 0) System.out.println("No future moves. :(");
            }
            centroid = bfsCentroid();
        }
        if(futureMoves.size() == 0 && inDanger){
            System.out.println("b");
            ArrayList<MoveTriple> hunterMoves = simulateTick(20);
            futureMoves = findPath(hunterMoves, 20);
            if(futureMoves.size() == 0) System.out.println("No future moves. :(");
        }
        PreyMove pm = null;
        if(futureMoves.size() > 0) {
            MoveTriple next = futureMoves.poll();
            System.out.println("Playing from future moves");
            pm = new PreyMove(next.x - gameState.preyPos.x, next.y - gameState.preyPos.y);
        } else {
            System.out.println("Playing towards centroid at " + centroid.x + " " + centroid.y);
            pm = new PreyMove(centroid.x - gameState.preyPos.x, centroid.y - gameState.preyPos.y);
            if(pm.x != 0) pm.x/=Math.abs(pm.x);
            if(pm.y != 0) pm.y/=Math.abs(pm.y);
        }
        return pm;
    }

}