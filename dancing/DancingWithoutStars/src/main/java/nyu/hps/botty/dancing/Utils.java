package nyu.hps.botty.dancing;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Map;

class Utils {

    static int[][] moves = {
            {0, 1},
            {0, -1},
            {1, 0},
            {-1, 0}
    };

    static int pointToInt(Point p, int boardSize) {
        return p.x * boardSize + p.y;
    }

    static Point intToPoint(int i, int boardSize) {
        return new Point(i / boardSize, i % boardSize);
    }


    /**
     * Process Dancers in order from greatest to least by dijkstra distance.
     * Can swap 2 dancers if that doesn't increase the max dijkstra distance.
     */
    static List<Point>[] generatePathsWithFloydWarshall(boolean[][] starGrid, Point[][] startEndP, int[][] fwDist, int minTurns) {
        //Scanner sc = new Scanner(System.in);
        int boardSize = (int)(Math.sqrt((double)fwDist.length));
        Instant startTime = Instant.now();
        int numDancers = startEndP.length;
        List<Point>[] res = new List[startEndP.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new ArrayList<>();
        }

        // keep track of the current location of every point at current t
        Point[] currentLocs = new Point[startEndP.length];
        for (int i = 0; i < currentLocs.length; i++) {
            Point startP = startEndP[i][0];
            currentLocs[i] = new Point(startP.x, startP.y);
            currentLocs[i].time = 0;
            res[i].add(currentLocs[i]);
        }


        int t = 1; // current time
        while(true) { // while there are dancers who haven't reached their goals.

            // count how many dancers aren't at their end locations
            int stillRunning = 0;
            for (int i = 0; i < currentLocs.length; i++) {
                if (!currentLocs[i].equals(startEndP[i][1])) {
                    stillRunning++;
                }
            }
            // System.out.println("# dancers still running: " + stillRunning);
            if (stillRunning == 0) break; // They all reached their goals.
            
            if(t >= minTurns) {
                System.out.println("Time to fail to find paths with APSP = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli())/1000.);
                return null; // early termination
            }
            // System.out.println("Generating positions for time: " + t);

            int[][] assignAtNextT = new int[boardSize][boardSize];
            for(int i = 0; i < boardSize; i++) Arrays.fill(assignAtNextT[i], -1);
            int[][] dancerToIndex = new int[boardSize][boardSize];
            for(int i = 0; i < boardSize; i++) Arrays.fill(dancerToIndex[i], -1);
            for (int i = 0; i < numDancers; i++) {
                dancerToIndex[currentLocs[i].x][currentLocs[i].y] = i;
            }


            // Utils.printGrid(gridAtT);
            ArrayList<Integer> byDist = new ArrayList<>();
            for(int i = 0; i < numDancers; i++) byDist.add(i);
            // sort (start, end) pairs by their bfs distance
            // this way, we'll look at longer paths before shorter ones
            Comparator<Integer> comp = (o1, o2) -> {
                int is1 = pointToInt(currentLocs[o1], boardSize);
                int is2 = pointToInt(currentLocs[o2], boardSize);
                int ie1 = pointToInt(startEndP[o1][1], boardSize);
                int ie2 = pointToInt(startEndP[o2][1], boardSize);
                int o1Dist = fwDist[is1][ie1];
                int o2Dist = fwDist[is2][ie2];
                return Integer.compare(o2Dist, o1Dist);
            };
            byDist.sort(comp);
            int farthestDancer = byDist.get(0);
            int farthestDancerInt = pointToInt(currentLocs[farthestDancer], boardSize);
            int farthestDancerEndInt = pointToInt(startEndP[farthestDancer][1], boardSize);
            if(fwDist[farthestDancerInt][farthestDancerEndInt] + t > minTurns) {
                //System.out.println("Impossible to find a better solution with current line segments.");
                System.out.println("Bad current line segments");
                return null;
            }
            Point[] nextMoves = new Point[numDancers];
            /**
             * 3 phases.
             * 1. Find a new location for each dancer along their floyd warshall path if possible.
             * 1. a. Also try to find an alternative location if the best location is blocked.
             * 2. Try to move all. Swap any two where one is moving and the second isn't.
             * 3. Anything that doesn't move stays in place.
             */
            for(int dancer : byDist) {
                //System.out.println("Dancer " + dancer + " is looking for a move");
                Point currentLoc = currentLocs[dancer];
                Point endP = startEndP[dancer][1];
                if(currentLoc.equals(endP)){
                    //System.out.println("Dancer " + dancer + " thinks they're at the end");
                    continue;
                }
                int currentLocInt = pointToInt(currentLocs[dancer], boardSize);
                int endPInt = pointToInt(endP, boardSize);
                Point nextMove = null;
                int nextDist = fwDist[currentLocInt][endPInt];
                for(int i = 0; i < moves.length; i++) {
                    Point possibleNextMove = new Point(currentLoc.x + moves[i][0], currentLoc.y + moves[i][1]);
                    if(possibleNextMove.x < 0 || possibleNextMove.x >= boardSize || possibleNextMove.y < 0 || possibleNextMove.y >= boardSize) continue;
                    int possibleNextMoveInt = pointToInt(possibleNextMove, boardSize);
                    if(fwDist[possibleNextMoveInt][endPInt] < nextDist && assignAtNextT[possibleNextMove.x][possibleNextMove.y] == -1) {
                        nextMove = possibleNextMove;
                        nextDist = fwDist[possibleNextMoveInt][endPInt];
                    }
                }
                if(nextMove != null) {
                    nextMoves[dancer] = nextMove;
                    assignAtNextT[nextMove.x][nextMove.y] = dancer;
                } else {
                    //System.out.println("Dancer " + dancer + " thinks there's no move");
                }
            }
            /*
            System.out.println("After Stage 1:");
            for(int i = 0; i < res.length; i++) {
                Point p = nextMoves[i];
                if(p == null) System.out.printf("Dancer %d has not found a move.\n", i);
                else System.out.printf("Dancer %d wants to move to %d %d\n", i, p.x, p.y);
            }
            */
            boolean omgplsno = false;
            boolean[] movedThisT = new boolean[numDancers];
            for(int dancer : byDist) {
                if(movedThisT[dancer]) continue;
                if(nextMoves[dancer] == null) continue;
                processMove(t, dancer, nextMoves, dancerToIndex, assignAtNextT, currentLocs, movedThisT, res, dancer, boardSize, starGrid);
            }
            for(int i = 0; i < numDancers; i++) {
                if(movedThisT[i]) continue;
                Point move = new Point(currentLocs[i].x, currentLocs[i].y);
                move.time = t;
                res[i].add(move);
                currentLocs[i] = move;
            }
            /*
            System.out.println("time = " + t);
            for(int i = 0; i < res.length; i++) {
                System.out.println("Dancer " + i + " " + res[i].size());
                for(int j = 0; j < 2; j++) {
                    Point p = res[i].get(res[i].size() - j - 1);
                    System.out.println(p.x + " " + p.y);
                }
                System.out.println("EndPoint = " + startEndP[i][1].x + " " + startEndP[i][1].y);
            }
            */
            //sc.next();
            // sanity check
            for (List<Point> l: res) {
                if (l.size() != t + 1) {
                    System.out.println("Mismatch size!");
                    return null;
                }
            }
            for (int ti = 0; ti <= t; ti++) {
                for (int i = 0; i < res.length; i++) {
                    for (int j = 0; j < res.length; j++) {
                        if (i == j) continue;
                        Point a = res[i].get(ti);
                        Point b = res[j].get(ti);
                        if (a.x == b.x && a.y == b.y) {
                            //System.out.println("time = " + t);
                            //System.out.println(i + " " + j);
                            /*
                            for(int ii = 0; ii < currentLocs.length; ii++) {
                                Point p = currentLocs[ii];
                                System.out.println("Dancer " + ii + " " + p.x + " " + p.y);
                                System.out.println("EndPoint = " + startEndP[i][1].x + " " + startEndP[i][1].y);
                            }
                            */
                            //sc.next();
                            System.out.println("Same location!");
                            return null;
                        }
                    }
                }
            }

            t++;
        }
        System.out.println("Time to paths with APSP = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli())/1000.);
        return res;
    }

    // returns true if the dancer moves from where it was before. else false.
    static boolean processMove(int t, int dancer, Point[] nextMoves,
        int[][] dancerToIndex, int[][] assignAtNextT, Point[] currentLocs,
        boolean[] movedThisT, List<Point>[] res, int congoHead,
        int boardSize, boolean[][] starGrid) {
        if(movedThisT[dancer]) {
            return !(res[dancer].get(res[dancer].size() - 1).equals(res[dancer].get(res[dancer].size() - 2)));
        }
        Point nextMove = nextMoves[dancer];
        if(nextMove == null) { // try to find a spot
            Point cur = new Point(currentLocs[dancer].x, currentLocs[dancer].y);
            for(int i = 0; i < moves.length; i++) { // Try to find a temporary move.
                Point possibleNextMove = new Point(cur.x + moves[i][0], cur.y + moves[i][1]);
                if(possibleNextMove.x < 0 || possibleNextMove.x >= boardSize || possibleNextMove.y < 0 || possibleNextMove.y >= boardSize) continue;
                int possDancer = dancerToIndex[possibleNextMove.x][possibleNextMove.y];
                if(!starGrid[possibleNextMove.x][possibleNextMove.y] &&
                    assignAtNextT[possibleNextMove.x][possibleNextMove.y] == -1) {
                        if(possDancer == -1 || possDancer == congoHead) {
                            nextMove = possibleNextMove;
                            break;
                        } else {
                            assignAtNextT[possibleNextMove.x][possibleNextMove.y] = dancer;
                            if(processMove(t, possDancer, nextMoves, dancerToIndex, assignAtNextT, currentLocs, movedThisT, res, congoHead, boardSize, starGrid)) {
                                nextMove = possibleNextMove;
                                assignAtNextT[possibleNextMove.x][possibleNextMove.y] = -1;
                                break;
                            }
                            assignAtNextT[possibleNextMove.x][possibleNextMove.y] = -1;
                        }
                }
            }
            boolean ret = true;
            if(nextMove == null || assignAtNextT[nextMove.x][nextMove.y] != -1) {
                nextMove = cur;
                ret = false;
            }
            assignAtNextT[nextMove.x][nextMove.y] = dancer;
            nextMoves[dancer] = nextMove;
            nextMove.time = t;
            res[dancer].add(nextMove);
            currentLocs[dancer] = new Point(nextMove.x, nextMove.y);
            movedThisT[dancer] = true;
            return ret;
        }
        int nextDancer = dancerToIndex[nextMove.x][nextMove.y];
        if(nextDancer == -1 || nextDancer == congoHead) {
            nextMove.time = t;
            res[dancer].add(nextMove);
            currentLocs[dancer] = new Point(nextMove.x, nextMove.y);
            movedThisT[dancer] = true;
            return true;
        } else {
            if(processMove(t, nextDancer, nextMoves, dancerToIndex, assignAtNextT, currentLocs, movedThisT, res, congoHead, boardSize, starGrid)) {
                nextMove.time = t;
                res[dancer].add(nextMove);
                currentLocs[dancer] = new Point(nextMove.x, nextMove.y);
                movedThisT[dancer] = true;
                return true;
            } else {
                Point cur = currentLocs[dancer];
                for(int i = 0; i < moves.length; i++) { // Try to find a temporary move.
                    Point possibleNextMove = new Point(cur.x + moves[i][0], cur.y + moves[i][1]);
                    if(possibleNextMove.x < 0 || possibleNextMove.x >= boardSize || possibleNextMove.y < 0 || possibleNextMove.y >= boardSize) continue;
                    int possDancer = dancerToIndex[possibleNextMove.x][possibleNextMove.y];
                    if(!starGrid[possibleNextMove.x][possibleNextMove.y] &&
                        assignAtNextT[possibleNextMove.x][possibleNextMove.y] == -1) {
                            if(possDancer == -1 || possDancer == congoHead) {
                                nextMove = possibleNextMove;
                                break;
                            } else {
                                assignAtNextT[possibleNextMove.x][possibleNextMove.y] = dancer;
                                if(processMove(t, possDancer, nextMoves, dancerToIndex, assignAtNextT, currentLocs, movedThisT, res, congoHead, boardSize, starGrid)) {
                                    nextMove = possibleNextMove;
                                    assignAtNextT[possibleNextMove.x][possibleNextMove.y] = -1;
                                    break;
                                }
                                assignAtNextT[possibleNextMove.x][possibleNextMove.y] = -1;
                            }
                    }
                }
                boolean ret = true;
                if(assignAtNextT[nextMove.x][nextMove.y] != -1) {
                    nextMove = cur;
                    ret = false;
                }
                assignAtNextT[nextMove.x][nextMove.y] = dancer;
                nextMoves[dancer] = nextMove;
                nextMove.time = t;
                res[dancer].add(nextMove);
                currentLocs[dancer] = new Point(nextMove.x, nextMove.y);
                movedThisT[dancer] = true;
                return ret;
            }
        }
    }

    static void bfs(Point s, String[][] grid, int[][] dist, Point[][] pred, boolean ignoreObs) {
        int boardSize = grid.length;
        Queue<Point> q = new LinkedList<>();
        for(int i = 0; i < dist.length; i++) {
            Arrays.fill(dist[i], Integer.MAX_VALUE);
            if(pred != null) Arrays.fill(pred[i], null);
        }
        boolean[][] vis = new boolean[boardSize][boardSize];
        q.add(s);
        dist[s.x][s.y] = 0;
        while(!q.isEmpty()) {
            Point cur = q.poll();
            int x = cur.x;
            int y = cur.y;
            if(vis[x][y]) continue;
            vis[x][y] = true;
            for(int[] dir : moves) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if(nx < 0 || nx >= boardSize || ny < 0 || ny >= boardSize) continue;
                if (ignoreObs) {
                    if (grid[nx][ny].equals("#")) continue;
                } else {
                    if (!grid[nx][ny].equals(" ")) continue;
                }
                if(dist[nx][ny] > dist[x][y] + 1){
                    dist[nx][ny] = dist[x][y] + 1;
                    if(pred != null) pred[nx][ny] = cur;
                    q.add(new Point(nx, ny));
                }
            }
        }
    }

    /**
     * Process Dancers in order from greatest to least by dijkstra distance.
     * Can swap 2 dancers if that doesn't increase the max dijkstra distance.
     */
    static List<Point>[] generatePaths(Point[][] startEndP, String[][] grid, int minTurns) {
        Instant startTime = Instant.now();
        int boardSize = grid.length;
        int numDancers = startEndP.length;
        List<Point>[] res = new List[startEndP.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new ArrayList<>();
        }

        // keep track of the current location of every point at current t
        Point[] currentLocs = new Point[startEndP.length];
        for (int i = 0; i < currentLocs.length; i++) {
            Point startP = startEndP[i][0];
            currentLocs[i] = new Point(startP.x, startP.y);
            currentLocs[i].time = 0;
            res[i].add(currentLocs[i]);
        }

        // keep track if a dancer NEEDS to swap with another
        ArrayDeque<Point> needsToSwap = new ArrayDeque<>();

        int t = 1; // current time

        while(true) { // while there are dancers who haven't reached their goals.
            if(t >= minTurns) {
                System.out.println("Time to fail to find paths with swapping = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli()));
                return null; // early termination
            }
            // System.out.println("Generating positions for time: " + t);
            // count how many dancers aren't at their end locations
            int stillRunning = 0;
            for (int i = 0; i < currentLocs.length; i++) {
                if (!currentLocs[i].equals(startEndP[i][1])) {
                    stillRunning++;
                }
            }

            if (stillRunning == 0) break; // They all reached their goals.

            // dist takes into account stars and other dancers
            // directDist only takes into account stars
            int[][][] dist = new int[numDancers][boardSize][boardSize];
            int[][][] directDist = new int[numDancers][boardSize][boardSize];
            Point[][][] pred = new Point[numDancers][boardSize][boardSize];
            Point[][][] directPred = new Point[numDancers][boardSize][boardSize];

            String[][] gridAtT = new String[boardSize][boardSize];
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    gridAtT[i][j] = grid[i][j];
                }
            }
            int[][] dancerToIndex = new int[boardSize][boardSize];
            for (int i = 0; i < numDancers; i++) {
                gridAtT[currentLocs[i].x][currentLocs[i].y] = "$";
                dancerToIndex[currentLocs[i].x][currentLocs[i].y] = i;
            }

            ArrayList<Integer> byDist = new ArrayList<>();
            for (int i = 0; i < numDancers; i++) {
                byDist.add(i);
                bfs(currentLocs[i], gridAtT, dist[i], null, false);
                bfs(currentLocs[i], gridAtT, directDist[i], null, true);
            }

            // sort (start, end) pairs by their bfs distance
            // this way, we'll look at longer paths before shorter ones
            Comparator<Integer> comp = (o1, o2) -> {
                int o1Dist = directDist[o1][startEndP[o1][1].x][startEndP[o1][1].y];
                int o2Dist = directDist[o2][startEndP[o2][1].x][startEndP[o2][1].y];
                return Integer.compare(o2Dist, o1Dist);
            };
            byDist.sort(comp);

            boolean[] movedThisT = new boolean[numDancers];
            boolean finishedSwaps = false;

            while (true) {
                int moved = 0;

                if (!finishedSwaps) {
                    while (!needsToSwap.isEmpty()) {
                        Point swapper = needsToSwap.pollFirst();
                        Point swappee = needsToSwap.pollFirst();

                        int dancer = dancerToIndex[swapper.x][swapper.y];
                        int swapeeInd = dancerToIndex[swappee.x][swappee.y];

                        // do swap
                        Point swapeeMove = new Point(swapper.x, swapper.y);
                        swapeeMove.time = t;
                        res[swapeeInd].add(swapeeMove);
                        dancerToIndex[swapper.x][swapper.y] = swapeeInd;
                        currentLocs[swapeeInd] = swapeeMove;
                        movedThisT[swapeeInd] = true;
                        moved++;

                        Point nextMove = new Point(swappee.x, swappee.y);
                        nextMove.time = t;
                        res[dancer].add(nextMove);
                        dancerToIndex[swappee.x][swappee.y] = dancer;
                        currentLocs[dancer] = nextMove;
                        movedThisT[dancer] = true;
                        moved++;
                    }
                }
                finishedSwaps = true;

                for (int dancer : byDist) {
                    if(movedThisT[dancer]) continue;

                    // recompute bfs because it might have changed.
                    bfs(currentLocs[dancer], gridAtT, dist[dancer], pred[dancer], false);
                    bfs(currentLocs[dancer], gridAtT, directDist[dancer], directPred[dancer], true);

                    Point endP = startEndP[dancer][1];

                    int straightDist = dist[dancer][endP.x][endP.y];
                    int dirDist = directDist[dancer][endP.x][endP.y];

                    // dancer needs to swap
                    if (straightDist == Integer.MAX_VALUE && dirDist != Integer.MAX_VALUE) {
                        ArrayList<Point> path = new ArrayList<>();
                        path.add(new Point(endP.x, endP.y));
                        Point currentP = directPred[dancer][endP.x][endP.y];
                        while (currentP != null) {
                            path.add(new Point(currentP.x, currentP.y));
                            currentP = directPred[dancer][currentP.x][currentP.y];
                        }
                        Collections.reverse(path);
                        Point nextMove = path.get(1);
                        if (!gridAtT[nextMove.x][nextMove.y].equals(" ")) {
                            int swapee = dancerToIndex[nextMove.x][nextMove.y];
                            if (movedThisT[swapee]) {
                                boolean swapeeUsed = false;
                                for (Point p: needsToSwap) {
                                    if (p.x == nextMove.x && p.y == nextMove.y) {
                                        swapeeUsed = true;
                                        break;
                                    }
                                }

                                // do swap first next turn
                                Point stayInPlace = new Point(currentLocs[dancer].x, currentLocs[dancer].y);
                                stayInPlace.time = t;
                                res[dancer].add(stayInPlace);
                                gridAtT[currentLocs[dancer].x][currentLocs[dancer].y] = " ";
                                gridAtT[stayInPlace.x][stayInPlace.y] = "$";
                                dancerToIndex[stayInPlace.x][stayInPlace.y] = dancer;
                                currentLocs[dancer] = stayInPlace;
                                movedThisT[dancer] = true;
                                moved++;
                                nextMove.time = t;
                                if (!swapeeUsed) {
                                    needsToSwap.add(stayInPlace);
                                    needsToSwap.add(nextMove);
                                }
                            } else {
                                // do swap
                                Point swapper = currentLocs[dancer];
                                Point swappeeP = currentLocs[swapee];

                                Point swapeeMove = new Point(swapper.x, swapper.y);
                                swapeeMove.time = t;
                                res[swapee].add(swapeeMove);
                                dancerToIndex[swapper.x][swapper.y] = swapee;
                                currentLocs[swapee] = swapeeMove;
                                movedThisT[swapee] = true;
                                moved++;

                                nextMove.time = t;
                                res[dancer].add(nextMove);
                                dancerToIndex[swappeeP.x][swappeeP.y] = dancer;
                                currentLocs[dancer] = nextMove;
                                movedThisT[dancer] = true;
                                moved++;
                            }
                        } else {
                            nextMove.time = t;
                            res[dancer].add(nextMove);
                            dancerToIndex[nextMove.x][nextMove.y] = dancer;
                            gridAtT[currentLocs[dancer].x][currentLocs[dancer].y] = " ";
                            gridAtT[nextMove.x][nextMove.y] = "$";
                            dancerToIndex[currentLocs[dancer].x][currentLocs[dancer].y] = -1;
                            currentLocs[dancer] = nextMove;
                            movedThisT[dancer] = true;
                            moved++;
                        }

                    } else if (straightDist != Integer.MAX_VALUE) {
                        ArrayList<Point> path = new ArrayList<>();
                        path.add(new Point(endP.x, endP.y));
                        Point currentP = pred[dancer][endP.x][endP.y];
                        while (currentP != null) {
                            path.add(new Point(currentP.x, currentP.y));
                            currentP = pred[dancer][currentP.x][currentP.y];
                        }
                        Point nextMove;
                        if (path.size() == 1) {
                            nextMove = path.get(0);
                        } else {
                            nextMove = path.get(path.size() - 2);
                            gridAtT[currentLocs[dancer].x][currentLocs[dancer].y] = " ";
                            gridAtT[nextMove.x][nextMove.y] = "$";
                        }
                        nextMove.time = t;
                        res[dancer].add(nextMove);
                        dancerToIndex[nextMove.x][nextMove.y] = dancer;
                        currentLocs[dancer] = nextMove;
                        movedThisT[dancer] = true;
                        moved++;
                    }
                }
                // resort
                byDist.sort(comp);
                if (moved == 0) break;
            }

            for (int i = 0; i < movedThisT.length; i++) {
                if (!movedThisT[i]) {
                    Point move = new Point(currentLocs[i].x, currentLocs[i].y);
                    move.time = t;
                    res[i].add(move);
                    currentLocs[i] = move;
                }
            }

            if (!validate(res, t)) {
                return null;
            }

            t++;
        }
        System.out.println("Time to paths with swapping = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli())/1000.);
        return res;
    }

    static boolean validate(List<Point>[] res, int t) {
        // sanity check
        for (List<Point> l: res) {
            if (l.size() != t + 1) {
                System.out.println("Mismatch size!");
                return false;
            }
        }
        for (int ti = 0; ti <= t; ti++) {
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res.length; j++) {
                    if (i == j) continue;
                    Point a = res[i].get(ti);
                    Point b = res[j].get(ti);
                    if (a.x == b.x && a.y == b.y) {
                        System.out.println("Same location!");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static List<Point>[] generatePathsWithoutSwaps(Point[][] startEndP, String[][] grid, int minTurns) {
        Instant startTime = Instant.now();
        int boardSize = grid.length;
        List<Point>[] res = new List[startEndP.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new ArrayList<>();
        }

        // keep track of the current location of every point at current t
        Point[] currentLocs = new Point[startEndP.length];
        for (int i = 0; i < currentLocs.length; i++) {
            Point startP = startEndP[i][0];
            currentLocs[i] = new Point(startP.x, startP.y);
            currentLocs[i].time = 0;
            res[i].add(currentLocs[i]);
        }

        // sort (start, end) pairs by their dijkstra distance
        // this way, we'll look at longer paths before shorter ones
        Comparator<Integer> comp = (o1, o2) -> {
            Point startP = currentLocs[o1];
            Point endP = startEndP[o1][1];
            int o1Dist = Math.abs(startP.x - endP.x) + Math.abs(startP.y - endP.y);
            startP = currentLocs[o2];
            endP = startEndP[o2][1];
            int o2Dist = Math.abs(startP.x - endP.x) + Math.abs(startP.y - endP.y);
            return Integer.compare(o2Dist, o1Dist);
        };
        ArrayList<Integer> byL = new ArrayList<>();
        for (int i = 0; i < startEndP.length; i++) {
            byL.add(i);
        }
        byL.sort(comp);

        // current time
        int t = 1;

        while (true) {
            if(t >= minTurns) {
                System.out.println("Time to fail to find paths without swapping = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli()));
                return null;
            }
            //System.out.println("Generating positions for time: " + t);

            // count how many dancers aren't at their start locations (since we're going backwards)
            int stillRunning = 0;
            for (int i = 0; i < currentLocs.length; i++) {
                if (!currentLocs[i].equals(startEndP[i][1])) {
                    stillRunning++;
                }
            }
            //System.out.println("# dancers still running: " + stillRunning);
            if (stillRunning == 0) break;

            // generate initial grid, will be used to mark occupied squares
            // fill in stars and current locations of dancers
            String[][] gridAtT = new String[boardSize][boardSize];
            for (int i = 0; i < gridAtT.length; i++) {
                for (int j = 0; j < gridAtT.length; j++) {
                    if (grid[i][j].equals("#")) gridAtT[i][j] = "#";
                    else gridAtT[i][j] = " ";
                }
            }
            for (Point p : currentLocs) {
                if (p.time != t - 1) {
                    System.out.println("Time mismatch");
                    System.exit(0);
                }
                gridAtT[p.x][p.y] = "$";
            }

            // Utils.printGrid(gridAtT);

            // keep track of who moved this turn
            boolean[] movedThisT = new boolean[startEndP.length];
            int[][] dist = new int[boardSize][boardSize];

            while (true) {
                int moved = 0;
                for (int ind : byL) {

                    if (movedThisT[ind]) continue;

                    // Run Dijkstra's
                    for (int[] i : dist) Arrays.fill(i, Integer.MAX_VALUE);

                    Point[][] pred = new Point[boardSize][boardSize];
                    PriorityQueue<Edge> dq = new PriorityQueue<>(Comparator.comparingInt(Edge::getDist));

                    Point start = currentLocs[ind];
                    dq.add(new Edge(start.x, start.y, 0));
                    dist[start.x][start.y] = 0;

                    int[][] moves = {
                            {0, 1},
                            {0, -1},
                            {1, 0},
                            {-1, 0}
                    };
                    while (!dq.isEmpty()) {
                        Edge e = dq.poll();
                        if (e == null) continue;
                        int currentX = e.x;
                        int currentY = e.y;
                        int currentDist = e.dist;
                        for (int[] dir : moves) {
                            int newX = currentX + dir[0];
                            int newY = currentY + dir[1];
                            int newDist = currentDist + 1;
                            if (newX < 0 || newX >= boardSize || newY < 0 || newY >= boardSize) continue;
                            if (gridAtT[newX][newY].equals("#") || gridAtT[newX][newY].equals("$")) continue;
                            if (dist[newX][newY] > newDist) {
                                Edge newEdge = new Edge(newX, newY, newDist);
                                dist[newX][newY] = newDist;
                                pred[newX][newY] = new Point(currentX, currentY);
                                dq.add(newEdge);
                            }
                        }
                    }

                    Point endP = startEndP[ind][1];
                    Point nextMove;

                    // path found by Dijkstra's, then we should try to move
                    if (dist[endP.x][endP.y] != Integer.MAX_VALUE) {
                        ArrayList<Point> path = new ArrayList<>();
                        path.add(new Point(endP.x, endP.y));
                        Point currentP = pred[endP.x][endP.y];
                        while (currentP != null) {
                            path.add(new Point(currentP.x, currentP.y));
                            currentP = pred[currentP.x][currentP.y];
                        }
                        Collections.reverse(path);
                        if (path.size() == 1) {
                            nextMove = path.get(0);
                        } else {
                            nextMove = path.get(1);
                            gridAtT[nextMove.x][nextMove.y] = "$";
                            gridAtT[currentLocs[ind].x][currentLocs[ind].y] = " ";
                        }
                        nextMove.time = t;
                        res[ind].add(nextMove);
                        currentLocs[ind] = nextMove;
                        movedThisT[ind] = true;
                        moved++;
                    }
                }
                if (moved == 0) break;
            }

            for (int i = 0; i < movedThisT.length; i++) {
                if (!movedThisT[i]) {
                    Point move = new Point(currentLocs[i].x, currentLocs[i].y);
                    move.time = t;
                    res[i].add(move);
                    currentLocs[i] = move;
                }
            }

            // resort
            byL.sort(comp);

            if (!validate(res, t)) {
                return null;
            }

            t++;
        }

        System.out.println("Time to paths without swapping = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli()));
        return res;
    }

    static void generateGrid(String[][] starGrid, Point[] currentLocs) {
        int n = starGrid.length;
        String[][] grid = new String[n][n];
        for(Point p: currentLocs) {
            grid[p.x][p.y] = "$";
        }
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                if(grid[i][j] == null) grid[i][j] = starGrid[i][j];
            }
        }
        printGrid(grid);
    }

    static void printGrid(String[][] grid) {
        for (String[] c : grid) {
            for (String j : c) {
                System.out.print(j);
            }
            System.out.println();
        }
    }

    static void printMoves(List<Point>[] paths) {
        for (List<Point> path: paths) {
            StringBuilder sb = new StringBuilder();
            Point curr = path.get(0);
            sb.append(String.format("Point(%d,%d,%d)", curr.x, curr.y, curr.time));
            for (int j = 1; j < path.size(); j++) {
                curr = path.get(j);
                sb.append(String.format(" -> Point(%d,%d,%d)", curr.x, curr.y, curr.time));
            }
            System.out.println(sb.toString());
        }
    }

    static class Edge {
        int x, y, dist;

        Edge(int x, int y, int dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
        }

        static int getDist(Edge e) {
            return e.dist;
        }
    }

}

// Set of line segments and assignments.
class Instance {
    List<LineSegment> lineSegs;
    Map<Dancer, Integer> assignment;
    int cost;

    Instance(List<LineSegment> lineSegs, Map<Dancer, Integer> assignment, int cost) {
        this.lineSegs = lineSegs;
        this.assignment = assignment;
        this.cost = cost;
    }
}

class LineSegment {
    Point start, end;
    boolean horizontal; // true --> line segment goes right. else down.
    int length;

    LineSegment(Point start, boolean horizontal, int length) {
        this.start = start;
        this.horizontal = horizontal;
        this.length = length;
        this.end = new Point(start.x + (horizontal ? length - 1 : 0), start.y + (horizontal ? 0 : length - 1));
    }
}

class Dancer {
    Point loc;
    int color;

    Dancer(Point loc, int color) {
        this.loc = loc;
        this.color = color;
    }

    @Override
    public int hashCode() {
        return (this.loc.x * this.loc.y) ^ this.color;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        Dancer dancer = (Dancer) o;
        // field comparison
        return this.loc.x == dancer.loc.x && this.loc.y == dancer.loc.y && this.color == dancer.color;
    }

}

/**
 * Java Implementation of Dinitz's max flow algorithm.
 * https://en.wikipedia.org/wiki/Dinic%27s_algorithm
 */
class Dinic {

    final int INF = 2000000000;
    int n, m;
    int[][] edgeList;
    int e = 0;
    int[] deg;
    Edge[][] graph;
    int[] curEdge;
    int[] dist;

    void init(int n, int m) {
        this.n = n;
        this.m = m;
        edgeList = new int[m][3];
        deg = new int[n];
    }

    void addEdge(int u, int v, int cap) {
        edgeList[e][0] = u;
        edgeList[e][1] = v;
        edgeList[e][2] = cap;
        ++deg[u];
        ++deg[v];
        ++e;
    }

    void buildGraph() {
        graph = new Edge[n][];
        for (int i = 0; i < n; i++) {
            graph[i] = new Edge[deg[i]--];
        }
        for (int i = 0; i < m; i++) {
            int u = edgeList[i][0];
            int v = edgeList[i][1];
            int cap = edgeList[i][2];
            Edge uv = new Edge(v, cap);
            Edge vu = new Edge(u, 0);
            uv.rev = vu;
            vu.rev = uv;
            graph[u][deg[u]--] = uv;
            graph[v][deg[v]--] = vu;
        }
    }

    int maxflow(int s, int t) {
        int flow = 0;
        // While there is an augmenting path from source to destination
        while (bfs(s, t) < INF) {
            curEdge = new int[n];
            while (true) {
                int minf = INF;
                minf = dfs(s, t, minf);
                if (minf == 0) break;
                flow += minf;
            }
        }
        return flow;
    }

    /**
     * Finds the distance of the shortest path from the source to destination
     */
    private int bfs(int s, int t) {
        dist = new int[n];
        for (int i = 0; i < n; i++) dist[i] = INF;
        dist[s] = 0;
        Queue<Integer> q = new ArrayDeque<>();
        q.add(s);
        while (!q.isEmpty()) {
            int u = q.poll();
            if (dist[u] > dist[t]) break;
            for (Edge e : graph[u]) {
                if (dist[u] + 1 < dist[e.v] && e.cap > 0) {
                    dist[e.v] = dist[u] + 1;
                    q.add(e.v);
                }
            }
        }
        return dist[t];
    }

    /**
     * Finds an augmenting path, while removing edges leading to nonaugmenting paths.
     */
    private int dfs(int u, int t, int inflow) {
        if (u == t) return inflow;
        for (; curEdge[u] < graph[u].length; ++curEdge[u]) {
            Edge e = graph[u][curEdge[u]];
            if (e.cap > 0 && dist[e.v] == dist[u] + 1) {
                int outflow = dfs(e.v, t, Math.min(inflow, e.cap));
                if (outflow > 0) {
                    e.cap -= outflow;
                    e.rev.cap += outflow;
                    return outflow;
                }
            }
        }
        return 0;
    }

    class Edge {
        int v, cap;
        Edge rev;

        Edge(int _v, int _cap) {
            this.v = _v;
            this.cap = _cap;
        }
    }
}
