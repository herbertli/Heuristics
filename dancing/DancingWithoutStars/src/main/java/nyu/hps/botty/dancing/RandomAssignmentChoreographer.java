package nyu.hps.botty.dancing;

import java.io.File;
import java.time.Instant;
import java.util.*;

/**
 * Continuously generate k line segments of length c.
 * See whats the max distance to any line such that every line has a dancer of
 * each color and all dancers are assigned to a line.
 * An Instance is an assignment of dancers to line segments.
 */
public class RandomAssignmentChoreographer extends Choreographer {

    Instant globalEnd;      // Moment when we need to start generating paths with what we have.
    Instant withSwapsEnd;   // Moment when we need to stop generating optimal solutions and
                            // fall back to non-optimal method

    static final int SECONDS_PER_SEGMENT = 2;       // Seconds to look to non-intersecting lines
    static final int TOTAL_SECONDS_TO_SOLVE = 110;  // Number of seconds given to look for assignments.
    static final int SECONDS_TO_SOLVE = 86;         // Number of seconds given to look for GOOD assignments.

    Point[][] startEndPairs; // [i][0] = dancer's start position. [i][1] = dancer's end position.
    List<Point>[] paths = null; // Paths for the dancers using the startEndPairs.
    ArrayList<LineSegment> bestLineSegments = null;
    ArrayList<ArrayList<LineSegment>> allPossibleLineSegments = new ArrayList<>();
    int minTurns = 200;
    Random rand = new Random();

    /**
     * Testing code.
     */
    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (args.length == 1) {
            if (sb.length() == 0) {
                Scanner sc = new Scanner(new File(args[0]));
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine()).append("\n");
                }
                sc.close();
            }
        } else {
            System.out.println("Please specify an input file!");
            System.exit(0);
        }
        RandomAssignmentChoreographer rac = new RandomAssignmentChoreographer();
        rac.receiveInput(sb.toString());
        rac.receiveGameInfo("30 4 40");
        // below is output from spoiler running on dancedata...
        rac.receiveStars("27 22 22 6 7 4 14 1 11 7 28 2 25 27 19 8 7 13 6 22 3 1 1 17 1 24 26 15 3 14 19 16 13 17 2 8 25 9 16 19 17 27 9 24 6 27 23 21 16 14 5 18 12 13 20 12 15 10 9 16 0 12 18 22 10 20 9 10 21 24 13 22 17 5 12 26 26 5 20 3");
        rac.solve();
    }

    public void solve() {
        withSwapsEnd = Instant.now().plusSeconds(SECONDS_TO_SOLVE);
        globalEnd = Instant.now().plusSeconds(TOTAL_SECONDS_TO_SOLVE);

        ArrayList<Dancer> dancers = getDancersAsArrayList();
        String[][] starGrid = buildStarGrid();
        int iterations = 0;

        // Try to generate Instances while keeping the best one.
        // until we should fall back to non-optimal way
        System.out.println("Generating paths with swaps...");
        while (Instant.now().isBefore(withSwapsEnd)) {
            if (iterations % 10000 == 0) System.out.println("Iterations: " + iterations);

            // generate line segments
            ArrayList<LineSegment> lines = generateRandomNonIntersectingLines();
            iterations++;
            if (Instant.now().isAfter(withSwapsEnd)) break;

			// what happens if we can't find any lines?
			if(lines == null) continue;
            Instance assignment = assignDancersToLines(dancers, lines);
            if (assignment == null) continue;
			if(assignment.cost >= minTurns) continue; // Impossible to be better than the current best paths we have.
            int cost = generateStartEndPairs(assignment);
			if(cost >= minTurns) continue; // Impossible to be better than the current best paths we have.
			allPossibleLineSegments.add(lines); //TODO: maybe instead of saving line segments, we can save assignments

            List<Point>[] newPaths = Utils.generatePathsWithoutSwaps(startEndPairs, starGrid, minTurns);
            if (newPaths == null) continue;
            if (paths == null || minTurns > newPaths[0].size()) {
                System.out.println("Found an good solution!");
                this.bestLineSegments = lines;
                this.paths = newPaths;
                minTurns = this.paths[0].size();
                System.out.println(this.paths[0].size());
            }
            //Utils.printMoves(paths);
        }
        System.out.printf("Ran %d iterations.\n", iterations);
        System.out.printf("Generated %d possible arrangements of line segments.\n", allPossibleLineSegments.size());

        if (paths == null) {
            System.out.println("No optimal solution found...");
            System.out.println("Generating paths the non-optimal way...");
        } else {
            System.out.println("Returning optimal solution...");
            return;
        }

        for (ArrayList<LineSegment> lines: allPossibleLineSegments) {
            if (Instant.now().isAfter(globalEnd)) break;

            Instance assignment = assignDancersToLines(dancers, lines);
            generateStartEndPairs(assignment);
            List<Point>[] newPaths = Utils.generatePaths(startEndPairs, starGrid, minTurns);
            if (newPaths == null) continue;

            if (paths == null || minTurns > newPaths[0].size()) {
                System.out.println("Found a non-optimal solution!");
                this.bestLineSegments = lines;
                this.paths = newPaths;
                minTurns = this.paths[0].size();
                System.out.println(this.paths[0].size());
            }
        }
    }

    @Override
    Point[][] getLines() {
        Point[][] startEndPoints = new Point[this.k][2];
        for (int i = 0; i < k; i++) {
            startEndPoints[i][0] = bestLineSegments.get(i).start;
            startEndPoints[i][1] = bestLineSegments.get(i).end;
        }
        return startEndPoints;
    }

    @Override
    List<Point>[] getPaths() {
        solve();
        return paths;
    }

    Instance generateInstance(List<Dancer> dancers, List<LineSegment> lines, Dinic dinic, int cost) {
        Map<Dancer, Integer> m = new HashMap<>();
        int countAssigned = 0;
        for (int i = 0; i < this.numOfColor * this.k; i++) {
            for (int j = 0; j < dinic.graph[i].length; j++) {
                // if saturated edge
                Dinic.Edge e = dinic.graph[i][j];
                if (e.v == 2 * (this.numOfColor * this.k)) continue;
                if (e.cap == 0) {
                    m.put(dancers.get(i), (e.v - (this.numOfColor * this.k)) / this.numOfColor);
                    countAssigned++;
                }
            }
        }
        if (countAssigned != dancers.size()) {
            System.out.println("Not valid Instance");
            return null;
        }
        Instance instance = new Instance(lines, m, cost);
        return instance;
    }

    // Generates k line segments of length c.
    // Returns 2*k line segments represented by the end points.
    // Line segments won't intersect or cross stars.
    ArrayList<LineSegment> generateRandomNonIntersectingLines() {
		Instant startTime = Instant.now();
        ArrayList<LineSegment> lineSegments = new ArrayList<>();
        boolean[][] occupied = new boolean[this.boardSize][this.boardSize];
        if (stars != null) for (Point p : stars) occupied[p.x][p.y] = true;
        // Time for current iteration.
        Instant end = Instant.now().plusSeconds((lineSegments.size() + 1) * SECONDS_PER_SEGMENT);
        while (true) {
            // place one more line
            while (Instant.now().isBefore(withSwapsEnd) && Instant.now().isBefore(end) && lineSegments.size() < k) {
                boolean dir = rand.nextBoolean();
                int x = rand.nextInt(dir ? this.boardSize - this.numOfColor + 1 : this.boardSize);
                int y = rand.nextInt(dir ? this.boardSize : this.boardSize - this.numOfColor + 1);
                boolean placable = true;
                int curx = x;
                int cury = y;
                // bounds check
                if (x + (dir ? this.numOfColor - 1 : 0) >= this.boardSize || y + (dir ? 0 : this.numOfColor - 1) >= this.boardSize)
                    continue;
                for (int i = 0; placable && i < this.numOfColor; i++) {
                    if (!occupied[curx][cury]) {
                        curx += (dir ? 1 : 0);
                        cury += (dir ? 0 : 1);
                    } else {
                        placable = false;
                    }
                }
                if (placable) {
                    curx = x;
                    cury = y;
                    for (int i = 0; i < this.numOfColor; i++) {
                        occupied[curx][cury] = true;
                        curx += (dir ? 1 : 0);
                        cury += (dir ? 0 : 1);
                    }
                    lineSegments.add(new LineSegment(new Point(x, y), dir, this.numOfColor));
                    end = Instant.now().plusSeconds((lineSegments.size() + 1) * SECONDS_PER_SEGMENT);
                }
            }
            // we found enough line segments.
            if (lineSegments.size() == this.k) {
				/*
				System.out.println("This is an instance.");
				for(int i = 0; i < this.boardSize; i++) {
					for(int j = 0; j < this.boardSize; j++) {
						if(occupied[i][j]) System.out.print(" ");
						else System.out.print("#");
					}
					System.out.println();
				}
                */
                //System.out.println("Time to generate lineSegments = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli()));
                return lineSegments;
            }
            // we are out of time.
            if (Instant.now().isAfter(withSwapsEnd)) return null;
            // can't find a placement, remove last.
            if (lineSegments.size() > 0) {
                LineSegment last = lineSegments.get(lineSegments.size() - 1);
                int curx = last.start.x;
                int cury = last.start.y;
                boolean dir = last.horizontal;
                for (int i = 0; i < this.numOfColor; i++) {
                    occupied[curx][cury] = false;
                    curx += (dir ? 1 : 0);
                    cury += (dir ? 0 : 1);
                }
                lineSegments.remove(lineSegments.size() - 1);
                // recalculate time to find new line segment.
                end = Instant.now().plusSeconds((lineSegments.size() + 1) * SECONDS_PER_SEGMENT);
            }
        }
    }

    ArrayList<Dancer> getDancersAsArrayList() {
        ArrayList<Dancer> al = new ArrayList<>();
        for (int color : this.dancers.keySet()) {
            ArrayList<Point> dancerCoors = this.dancers.get(color);
            for (Point dancerCoor : dancerCoors) {
                al.add(new Dancer(dancerCoor, color));
            }
        }
        return al;
    }

    // assign dancers to points in line segments.
    int generateStartEndPairs(Instance assignment) {
        Instant startTime = Instant.now();
		startEndPairs = new Point[this.numOfColor * this.k][2];
		int minCost = 0;
        // run max flow on each line segment to assign dancers to a point in the line segment.
        for (int ii = 0; ii < this.k; ii++) {
            Point[] pointsInLine = new Point[this.numOfColor];
            int x = assignment.lineSegs.get(ii).start.x;
            int y = assignment.lineSegs.get(ii).start.y;
            boolean dir = assignment.lineSegs.get(ii).horizontal;
            int curx = x;
            int cury = y;
            for (int j = 0; j < this.numOfColor; j++) {
                pointsInLine[j] = new Point(curx, cury);
                curx += (dir ? 1 : 0);
                cury += (dir ? 0 : 1);
            }
            Point[] dancerCoors = new Point[this.numOfColor];
            int last = 0;
            for (Dancer d : assignment.assignment.keySet()) {
                if (assignment.assignment.get(d) == ii) {
                    dancerCoors[last++] = new Point(d.loc.x, d.loc.y);
                }
            }
            // binary search min cost + assingment
            int lo = 0;
            int hi = 100;
            Dinic workingAssignmentDinic = null; // I want to access outside the binary search
            while (hi - lo > 1) {
                int mid = lo + (hi - lo) / 2;
                // 1 edge from s to each dancer with capacity 1.
                // 1 edge from each cell in segment to t with capacity 1.
                int s = 2 * (this.numOfColor);
                int t = s + 1;
                int numEdges = s;
                ArrayList<ArrayList<Integer>> g = new ArrayList<>();
                for (int i = 0; i < s; i++) {
                    g.add(new ArrayList<>());
                }
                for (int i = 0; i < this.numOfColor; i++) {
                    for (int j = 0; j < this.numOfColor; j++) {
                        if (Math.abs(dancerCoors[i].x - pointsInLine[j].x) + Math.abs(dancerCoors[i].y - pointsInLine[j].y) <= mid) {
                            g.get(i).add(this.numOfColor + j);
                            numEdges++;
                        }
                    }
                }
                Dinic dinic = new Dinic();
                dinic.init(t + 1, numEdges);
                for (int i = 0; i < this.numOfColor; i++) {
                    dinic.addEdge(s, i, 1);
                    dinic.addEdge(this.numOfColor + i, t, 1);
                    for (int v : g.get(i)) {
                        dinic.addEdge(i, v, 1);
                    }
                }
                dinic.buildGraph();
                if (dinic.maxflow(s, t) == this.numOfColor) {
                    hi = mid;
                    workingAssignmentDinic = dinic;
                } else lo = mid;
			}
			minCost = Math.max(minCost, hi);
            for (int i = 0; i < this.numOfColor; i++) {
                for (int j = 0; j < workingAssignmentDinic.graph[i].length; j++) {
                    // if saturated edge
                    Dinic.Edge e = workingAssignmentDinic.graph[i][j];
                    if (e.v == 2 * this.numOfColor) continue;
                    if (e.cap == 0) {
                        startEndPairs[ii * this.numOfColor + i][0] = dancerCoors[i];
                        startEndPairs[ii * this.numOfColor + i][1] = pointsInLine[e.v - this.numOfColor];
                    }
                }
            }
		}
        //System.out.println("Time to generate assignment of dancers to endcoors = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli()));
		return minCost;
    }

    // Generate assignment of dancers to lines minimizing the maximum Manhattan distance.
    Instance assignDancersToLines(ArrayList<Dancer> dancers, ArrayList<LineSegment> lines) {
        Instant startTime = Instant.now();
        // binary search min cost + assingment
        int lo = 0;
        int hi = 100;
        Dinic workingAssignmentDinic = null; // I want to access outside the binary search
        while (hi - lo > 1) {
            int mid = lo + (hi - lo) / 2;
            // 1 edge from s to each dancer with capacity 1.
            // 1 edge from each lineSegment*color to t with capacity 1.
            int s = 2 * (this.numOfColor * this.k);
            int t = s + 1;
            int numEdges = s;
            ArrayList<ArrayList<Integer>> g = new ArrayList<>();
            for (int i = 0; i < s; i++) {
                g.add(new ArrayList<>());
            }
            for (int i = 0; i < this.numOfColor; i++) {
                for (int j = 0; j < this.k; j++) {
                    Dancer d = dancers.get(i * this.k + j);
                    for (int l = 0; l < this.k; l++) {
                        LineSegment ls = lines.get(l);
                        int d1 = Math.abs(d.loc.x - ls.start.x) + Math.abs(d.loc.y - ls.start.y);
                        int d2 = Math.abs(d.loc.x - ls.end.x) + Math.abs(d.loc.y - ls.end.y);
                        if (Math.max(d1, d2) <= mid) {
                            g.get(i * this.k + j).add(this.numOfColor * this.k + l * this.numOfColor + i);
                            numEdges++;
                        }
                    }
                }
            }
            Dinic dinic = new Dinic();
            dinic.init(t + 1, numEdges);
            for (int i = 0; i < this.numOfColor * this.k; i++) {
                dinic.addEdge(s, i, 1);
                dinic.addEdge(this.numOfColor * this.k + i, t, 1);
                for (int v : g.get(i)) {
                    dinic.addEdge(i, v, 1);
                }
            }
            dinic.buildGraph();
            if (dinic.maxflow(s, t) == this.numOfColor * this.k) {
                hi = mid;
                workingAssignmentDinic = dinic;
            } else lo = mid;
        }
        //System.out.println("Time to generate assignment of dancers to line segments = " + (Instant.now().toEpochMilli() - startTime.toEpochMilli()));
        return generateInstance(dancers, lines, workingAssignmentDinic, hi);
    }

    String[][] buildStarGrid() {
        String[][] starGrid = new String[this.boardSize][this.boardSize];
        for (int i = 0; i < this.boardSize; i++) {
            for (int j = 0; j < this.boardSize; j++) {
                starGrid[i][j] = " ";
            }
        }
        for (Point star : this.stars) {
            starGrid[star.x][star.y] = "#";
        }
        return starGrid;
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
