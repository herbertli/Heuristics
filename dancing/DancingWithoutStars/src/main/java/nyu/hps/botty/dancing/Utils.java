package nyu.hps.botty.dancing;

import java.util.*;

class Utils {

    /**
     * General idea: work backwards and prioritize pairs with longer paths (solve them first)
     * @param startEndP - Point[k * numOfColors][2], denoting a startPoint, endPoint pair for a particular dancer
     * @param grid - the dance-floor, filled in with #s
     * @return List of paths each dancer should take
     */
    static List<Point>[] generatePaths(Point[][] startEndP, String[][] grid) {
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
