package nyu.hps.botty.dancing;

import java.util.*;

class Utils {

    /**
     * General idea: work backwards and prioritize pairs with longer paths (solve them first)
     * @param startEndP - Point[k * numOfColors][2], denoting a startPoint, endPoint pair for a particular dancer
     * @param grid - the dance-floor, filled in with #s
     * @return List of paths each dancer should take
     */
    static List<Point>[] generatePaths1(Point[][] startEndP, String[][] grid) {
        int boardSize = grid.length;
        List<Point>[] res = new List[startEndP.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = new ArrayList<>();
        }

        // keep track of the current location of every point at current t
        // initially every dancer is at their end location
        Point[] currentLocs = new Point[startEndP.length];
        for (int i = 0; i < currentLocs.length; i++) {
            Point endPoint = startEndP[i][1];
            currentLocs[i] = new Point(endPoint.x, endPoint.y);
            currentLocs[i].time = 0;
            res[i].add(currentLocs[i]);
        }

        // sort (start, end) pairs by decreasing distance
        // this way, we'll look at longer paths before shorter ones
        Comparator<Integer> comp = (o1, o2) -> {
            Point startP = currentLocs[o1];
            Point endP = startEndP[o1][1];
            int o1Dist = Math.abs(startP.x - endP.x) + Math.abs(startP.y - endP.y);
            startP = startEndP[o2][0];
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
            System.out.println("Generating positions for time: " + t);

            // count how many dancers aren't at their start locations (since we're going backwards)
            int stillRunning = 0;
            for (int i = 0; i < currentLocs.length; i++) {
                if (!currentLocs[i].equals(startEndP[i][0])) {
                    stillRunning++;
                }
            }
            System.out.println("# dancers still running: " + stillRunning);
            if (stillRunning == 0) break;

            // generate initial grid, will be used to mark occupied squares
            String[][] gridAtT = new String[boardSize][boardSize];
            for (int i = 0; i < gridAtT.length; i++) {
                for (int j = 0; j < gridAtT.length; j++) {
                    // mark placed starts
                    if (grid[i][j].equals("#")) {
                        gridAtT[i][j] = "#";
                    }
                }
            }

            // iterator over dancers, starting with the dancer who needs to
            for (int ind: byL) {

                // Run Dijkstra's
                int[][] dist = new int[boardSize][boardSize];
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
                        if (grid[newX][newY].equals("#")) continue;
                        if (dist[newX][newY] > newDist) {
                            Edge newEdge = new Edge(newX, newY, newDist);
                            dist[newX][newY] = newDist;
                            pred[newX][newY] = new Point(currentX, currentY);
                            dq.add(newEdge);
                        }
                    }
                }

                // endPoint is the starting location
                Point endP = startEndP[ind][0];
                Point nextMove;

                // no path found by Dijkstra's, then we should stay put
                if (dist[endP.x][endP.y] == Integer.MAX_VALUE) {
                    Point currentP = currentLocs[ind];
                    nextMove = new Point(currentP.x, currentP.y);
                }
                // otherwise, get the next move in the path
                else {
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
                    }
                }
                nextMove.time = t;
                res[ind].add(nextMove);
                gridAtT[nextMove.x][nextMove.y] = "#";
                currentLocs[ind] = nextMove;
            }

            // resort
            byL.sort(comp);
            t++;
        }

        for (List<Point> l: res) {
            Collections.reverse(l);
            int currT = 0;
            for (Point p: l) {
                p.time = currT;
                currT++;
            }
        }

        return res;
    }

    static int[][] moves = {
            {0, 1},
            {0, -1},
            {1, 0},
            {-1, 0}
    };

    static void bfs(Point s, String[][] grid, int[][] dist, Point[][] pred) {
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
                if(!grid[nx][ny].equals(" ")) continue;
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
    static List<Point>[] generatePaths2(Point[][] startEndP, String[][] grid) {
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

        int t = 1; // current time
        while(true) { // while there are dancers who haven't reached their goals.
            System.out.println("Generating positions for time: " + t);

            // count how many dancers aren't at their start locations (since we're going backwards)
            int stillRunning = 0;
            for (int i = 0; i < currentLocs.length; i++) {
                if (!currentLocs[i].equals(startEndP[i][1])) {
                    stillRunning++;
                }
            }
            System.out.println("# dancers still running: " + stillRunning);
            if (stillRunning == 0) break; // They all reached their goals.

            int[][][] dist = new int[numDancers][boardSize][boardSize];
            Point[][][] pred = new Point[numDancers][boardSize][boardSize];

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

            Utils.printGrid(gridAtT);
            ArrayList<Integer> byDist = new ArrayList<>();
            //int globalMaxDistThisT = 0;
            for (int i = 0; i < numDancers; i++) {
                byDist.add(i);
                bfs(currentLocs[i], gridAtT, dist[i], null);
                //globalMaxDistThisT = Math.max(globalMaxDistThisT, dist[i][startEndP[i][1].x][startEndP[i][1].y]);
            }
            // sort (start, end) pairs by their bfs distance
            // this way, we'll look at longer paths before shorter ones
            Comparator<Integer> comp = (o1, o2) -> {
                int o1Dist = dist[o1][startEndP[o1][1].x][startEndP[o1][1].y];
                int o2Dist = dist[o2][startEndP[o2][1].x][startEndP[o2][1].y];
                return Integer.compare(o2Dist, o1Dist);
            };
            byDist.sort(comp);

            boolean[] movedThisT = new boolean[numDancers];
            while (true) {
                int moved = 0;
                for(int dancer : byDist) {
                    if(movedThisT[dancer]) continue;
                    // recompute bfs because it might have changed.
                    bfs(currentLocs[dancer], gridAtT, dist[dancer], pred[dancer]);

                    Point endP = startEndP[dancer][1];

                    int straightDist = dist[dancer][endP.x][endP.y];
                    if(straightDist > 0) System.out.println(dancer + " " + currentLocs[dancer].x + " " + currentLocs[dancer].y + " " + straightDist);
                    // Maybe the best move is a swap, idk.
                    int x = currentLocs[dancer].x;
                    int y = currentLocs[dancer].y;
                    int bestSwapDir = -1;
                    int bestSwapee = -1;
                    for(int i = 0; i < moves.length; i++) {
                        int nx = x + moves[i][0];
                        int ny = y + moves[i][1];
                        if (nx < 0 || nx >= boardSize || ny < 0 || ny >= boardSize) continue;
                        if(!gridAtT[nx][ny].equals("$")) continue;
                        int nd = dancerToIndex[nx][ny];
                        // recompute bfs because things might have changed.
                        bfs(currentLocs[nd], gridAtT, dist[nd], pred[nd]);
                        if(movedThisT[nd]) continue;
                        Point nEndPoint = startEndP[nd][1];
                        int swappeeDist = dist[nd][nEndPoint.x][nEndPoint.y];
                        /*
                        if((dist[nd][endP.x][endP.y] < straightDist - 5 &&
                            dist[dancer][nEndPoint.x][nEndPoint.y] < swappeeDist - 5) || straightDist == Integer.MAX_VALUE) {
                                bestSwapDir = i;
                                bestSwapee = nd;
                        }
                        */
                    }

                    Point nextMove;

                    if(bestSwapDir != -1) { // swap
                        nextMove = new Point(x + moves[bestSwapDir][0], y + moves[bestSwapDir][1]);
                        Point swapMove = new Point(x, y);
                        nextMove.time = t;
                        res[dancer].add(nextMove);
                        swapMove.time = t;
                        res[bestSwapee].add(swapMove);
                        currentLocs[dancer] = nextMove;
                        currentLocs[bestSwapee] = swapMove;
                        dancerToIndex[nextMove.x][nextMove.y] = dancer;
                        dancerToIndex[swapMove.x][swapMove.y] = bestSwapee;
                        movedThisT[dancer] = true;
                        moved++;
                        movedThisT[bestSwapee] = true;
                        moved++;
                    } else if (straightDist != Integer.MAX_VALUE) {
                        ArrayList<Point> path = new ArrayList<>();
                        path.add(new Point(endP.x, endP.y));
                        Point currentP = pred[dancer][endP.x][endP.y];
                        while (currentP != null) {
                            path.add(new Point(currentP.x, currentP.y));
                            currentP = pred[dancer][currentP.x][currentP.y];
                        }
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

            // sanity check
            for (List<Point> l: res) {
                if (l.size() != t + 1) {
                    System.out.println("Mismatch size!");
                    System.exit(0);
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
                            System.exit(0);
                        }
                    }
                }
            }

            t++;
        }
        return res;
    }

    static List<Point>[] generatePaths(Point[][] startEndP, String[][] grid) {
        Scanner sc = new Scanner(System.in);
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
            System.out.println("Generating positions for time: " + t);

            // count how many dancers aren't at their start locations (since we're going backwards)
            int stillRunning = 0;
            for (int i = 0; i < currentLocs.length; i++) {
                if (!currentLocs[i].equals(startEndP[i][1])) {
                    stillRunning++;
                }
            }
            System.out.println("# dancers still running: " + stillRunning);
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

            // sanity check
            for (List<Point> l: res) {
                if (l.size() != t + 1) {
                    System.out.println("Mismatch size!");
                    System.exit(0);
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
                            System.exit(0);
                        }
                    }
                }
            }

            t++;
            //sc.next();
        }

        return res;
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
