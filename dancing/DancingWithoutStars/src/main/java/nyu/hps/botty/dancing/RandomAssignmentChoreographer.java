package nyu.hps.botty.dancing;

import java.io.File;
import java.time.Instant;
import java.util.*;

// Continuously generate k line segments of length c.
// See whats the max distance to any line such that every line has a dancer of
// each color and all dancers are assigned to a line.
// An Instance is an assignment of dancers to line segments.
public class RandomAssignmentChoreographer extends Choreographer {

	Instant globalEnd;
	static final int SECONDS_PER_SEGMENT = 2;
	static final int SECONDS_TO_SOLVE = 5;
	Instance best;
	Point[][] startEndPairs;
	List<Point>[] paths;
    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (args.length > 0) {
            for (String s : args) {
                if (sb.length() == 0) {
                    Scanner sc = new Scanner(new File(args[0]));
                    while (sc.hasNextLine()) {
                        sb.append(sc.nextLine()).append("\n");
                    }
                    sc.close();
                }
            }
        } else {
            System.out.println("Please specify an input file!");
            System.exit(0);
		}
		RandomAssignmentChoreographer rac = new RandomAssignmentChoreographer();
        rac.receiveInput(sb.toString());
        rac.receiveGameInfo("30 4 40");
        rac.solve();
	}
	
	public void solve() {
		globalEnd = Instant.now().plusSeconds(SECONDS_TO_SOLVE);
		ArrayList<Dancer> dancers = new ArrayList<>();
		for(int color : this.dancers.keySet()) {
			ArrayList<Point> dancerCoors = this.dancers.get(color);
			for(Point dancerCoor : dancerCoors) {
				dancers.add(new Dancer(dancerCoor, color));
			}
		}
		// Try to generate Instances while keeping the best one.
        while(Instant.now().isBefore(globalEnd)) {
            // generate line segments
			ArrayList<LineSegment> lines = generateRandomNonIntersectingLines(SECONDS_PER_SEGMENT);
			if(Instant.now().isAfter(globalEnd)) break;
			/*****************************************
			 what happens if we can't find any lines?
			 */
			if(lines == null) continue;
            // binary search min cost + assingment
            int lo = 0;
			int hi = 100;
			Dinic workingAssignmentDinic = null; // I want to access outside the binary search
			while(hi - lo > 1) {
				int mid = lo + (hi - lo)/2;
				// 1 edge from s to each dancer with capacity 1.
				// 1 edge from each lineSegment*color to t with capacity 1.
				int s = 2*(this.numOfColor * this.k);
				int t = s + 1;
				int numEdges = s;
				ArrayList<ArrayList<Integer>> g = new ArrayList<>();
				for(int i = 0; i < s; i++) {
					g.add(new ArrayList<>());
				}
				for(int i = 0; i < this.numOfColor; i++) {
					for(int j = 0; j < this.k; j++) {
						Dancer d = dancers.get(i*this.k+j);
						for(int l = 0; l < this.k; l++) {
							LineSegment ls = lines.get(l);
							int d1 = Math.abs(d.loc.x - ls.start.x) + Math.abs(d.loc.y - ls.start.y);
							int d2 = Math.abs(d.loc.x - ls.end.x) + Math.abs(d.loc.y - ls.end.y);
							if(Math.max(d1, d2) <= mid) {
								g.get(i*this.k+j).add(this.numOfColor*this.k + l*this.numOfColor+i);
								numEdges++;
							}
						}
					}
				}
				Dinic dinic = new Dinic();
				dinic.init(t+1, numEdges);
				for(int i = 0; i < this.numOfColor*this.k; i++) {
					dinic.addEdge(s, i, 1);
					dinic.addEdge(this.numOfColor*this.k+i, t, 1);
					for(int v : g.get(i)) {
						dinic.addEdge(i, v, 1);
					}
				}
				dinic.buildGraph();
				if(dinic.maxflow(s, t) == this.numOfColor*this.k) {
					hi = mid;
					workingAssignmentDinic = dinic;
				}
				else lo = mid;
			}
			if(best == null || hi < best.cost) {
				best = generateInstance(dancers, lines, workingAssignmentDinic, hi);
				System.out.println(best.cost);
			}
		}
		startEndPairs = new Point[this.numOfColor * this.k][2];
		// run max flow on line segments.
		for(int ii = 0; ii < this.k; ii++) {
			Point[] pointsInLine = new Point[this.numOfColor];
			int x = best.lineSegs.get(ii).start.x;
			int y = best.lineSegs.get(ii).start.y;
			boolean dir = best.lineSegs.get(ii).horizontal;
			int curx = x;
			int cury = y;
			for(int j = 0; j < this.numOfColor; j++) {
				pointsInLine[j] = new Point(curx, cury);
				curx += (dir ? 1 : 0);
				cury += (dir ? 0 : 1);
			}
			Point[] dancerCoors = new Point[this.numOfColor];
			int last = 0;
			for(Dancer d : best.assignment.keySet()) {
				if(best.assignment.get(d) == ii) {
					dancerCoors[last++] = new Point(d.loc.x, d.loc.y);
				}
			}
			// binary search min cost + assingment
            int lo = 0;
			int hi = 100;
			Dinic workingAssignmentDinic = null; // I want to access outside the binary search
			while(hi - lo > 1) {
				int mid = lo + (hi - lo)/2;
				// 1 edge from s to each dancer with capacity 1.
				// 1 edge from each lineSegment*color to t with capacity 1.
				int s = 2*(this.numOfColor);
				int t = s + 1;
				int numEdges = s;
				ArrayList<ArrayList<Integer>> g = new ArrayList<>();
				for(int i = 0; i < s; i++) {
					g.add(new ArrayList<>());
				}
				for(int i = 0; i < this.numOfColor; i++) {
					for(int j = 0; j < this.numOfColor; j++) {
						if(Math.abs(dancerCoors[i].x - pointsInLine[j].x) + Math.abs(dancerCoors[i].y - pointsInLine[j].y) <= mid) {
							g.get(i).add(this.numOfColor+j);
							numEdges++;

						}
					}
				}
				Dinic dinic = new Dinic();
				dinic.init(t+1, numEdges);
				for(int i = 0; i < this.numOfColor; i++) {
					dinic.addEdge(s, i, 1);
					dinic.addEdge(this.numOfColor+i, t, 1);
					for(int v : g.get(i)) {
						dinic.addEdge(i, v, 1);
					}
				}
				dinic.buildGraph();
				if(dinic.maxflow(s, t) == this.numOfColor) {
					hi = mid;
					workingAssignmentDinic = dinic;
				}
				else lo = mid;
			}
			for(int i = 0; i < this.numOfColor; i++) {
				for(int j = 0; j < workingAssignmentDinic.graph[i].length; j++) {
					// if saturated edge
					Dinic.Edge e = workingAssignmentDinic.graph[i][j];
					if(e.v == 2*this.numOfColor) continue;
					if(e.cap == 0) {
						startEndPairs[ii*this.numOfColor+i][0] = dancerCoors[i];
						startEndPairs[ii*this.numOfColor+i][1] = pointsInLine[e.v-this.numOfColor];
					}
				}
			}
		}
		String[][] grid = new String[this.boardSize][this.boardSize];
		for(int i = 0; i < this.boardSize; i++) {
			for(int j = 0; j < this.boardSize; j++) {
				grid[i][j] = "";
			}
		}
		for(Point star: this.stars) {
			grid[star.x][star.y] = "#";
		}
		this.paths = Utils.generatePaths(startEndPairs, grid);
		Utils.printMoves(paths);
	}
	@Override
	Point[][] getLines() {
		Point[][] startEndPoints = new Point[this.k][2];
		for(int i = 0; i < k; i++) {
			startEndPoints[i][0] = best.lineSegs.get(i).start;
			startEndPoints[i][1] = best.lineSegs.get(i).end;
		}
		return startEndPoints;
	}
	@Override
	List<Point>[] getPaths() {
		solve();
		return paths;
	}
    public Instance generateInstance(List<Dancer> dancers, List<LineSegment> lines, Dinic dinic, int cost) {
		Map<Dancer, Integer> m = new HashMap<>();
		int countAssigned = 0;
		for(int i = 0; i < this.numOfColor * this.k; i++) {
			for(int j = 0; j < dinic.graph[i].length; j++) {
				// if saturated edge
				Dinic.Edge e = dinic.graph[i][j];
				if(e.v == 2*(this.numOfColor * this.k)) continue;
				if(e.cap == 0) {
					m.put(dancers.get(i), (e.v-(this.numOfColor * this.k))/this.numOfColor);
					countAssigned++;
				}
			}
		}
		if(countAssigned != dancers.size()) System.out.println("Not valid Instance");
		Instance instance = new Instance(lines, m, cost);
		return instance;
    }
    // Generates k line segments of length c.
    // Returns 2*k line segments represented by the end points.
    // Line segments won't intersect or cross stars.
    public ArrayList<LineSegment> generateRandomNonIntersectingLines(int secondsPerSegment) {
        ArrayList<LineSegment> lineSegments = new ArrayList<>();
        boolean[][] occupied = new boolean[this.boardSize][this.boardSize];
		if(stars != null)
			for(Point p: stars) {
				occupied[p.x][p.y] = true;
			}
		Instant end = Instant.now().plusSeconds((lineSegments.size() + 1) * secondsPerSegment);
		while(true) {
			// place one more line
			while(Instant.now().isBefore(globalEnd) && Instant.now().isBefore(end) && lineSegments.size() < k) {
				Random rand = new Random();
				int x = rand.nextInt(this.boardSize);
				int y = rand.nextInt(this.boardSize);
				boolean dir = rand.nextBoolean();
				boolean placable = true;
				int curx = x;
				int cury = y;
				// bounds check
				if(x + (dir ? this.numOfColor : 0) >= this.boardSize || y + (dir ? 0 : this.numOfColor) >= this.boardSize) continue;
				for(int i = 0; placable && i < this.numOfColor; i++) {
					if(!occupied[curx][cury]) {
						curx += (dir ? 1 : 0);
						cury += (dir ? 0 : 1);
					} else {
						placable = false;
					}
				}
				if(placable) {
					curx = x;
					cury = y;
					for(int i = 0; i < this.numOfColor; i++) {
						occupied[curx][cury] = true;
						curx += (dir ? 1 : 0);
						cury += (dir ? 0 : 1);
					}
					lineSegments.add(new LineSegment(new Point(x, y), dir, this.numOfColor));
					end = Instant.now().plusSeconds((lineSegments.size() + 1) * secondsPerSegment);
				}
			}
			// we found enough line segments.
			if(lineSegments.size() == this.k) {
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
				break;
			}
			// we are out of time.
			if(Instant.now().isAfter(globalEnd)) return null;
			// can't find a placement, remove last.
			if(lineSegments.size() > 0) {
				LineSegment last = lineSegments.get(lineSegments.size()-1);
				int curx = last.start.x;
				int cury = last.start.y;
				boolean dir = last.horizontal;
				for(int i = 0; i < this.numOfColor; i++) {
					occupied[curx][cury] = false;
					curx += (dir ? 1 : 0);
					cury += (dir ? 0 : 1);
				}
				lineSegments.remove(lineSegments.size()-1);
				end = Instant.now().plusSeconds((lineSegments.size() + 1) * secondsPerSegment);
			}
		}
        return lineSegments;
    }
}

// Set of line segments and assignments.
class Instance {
	List<LineSegment> lineSegs;
	Map<Dancer, Integer> assignment;
	int cost;

    public Instance() {};

    public Instance(List<LineSegment> lineSegs, Map<Dancer, Integer> assignment, int cost) {
		this.lineSegs = lineSegs;
		this.assignment = assignment;
		this.cost = cost;
    }
}

class LineSegment {
	Point start, end;
	boolean horizontal; // true --> line segment goes right. else down.
	int length;
	public LineSegment(Point start, boolean horizontal, int length) {
		this.start = start;
		this.horizontal = horizontal;
		this.length = length;
		this.end = new Point(start.x + (horizontal ? length : 0), start.y + (horizontal ? 0 : length));
	}
}

class Dancer {
	Point loc;
	int color;
	public Dancer(Point loc, int color) {
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

 	void init(int n, int m){
		this.n = n;
		this.m = m;
		edgeList = new int[m][3];
		deg = new int[n];
  	}
	
	void addEdge(int u, int v, int cap){
		edgeList[e][0] = u;
		edgeList[e][1] = v;
		edgeList[e][2] = cap;
		++deg[u];
		++deg[v];
		++e;
	}

	void buildGraph(){
		graph = new Edge[n][];
		for(int i = 0; i < n; i++){
			graph[i] = new Edge[deg[i]--];
		}
		for(int i = 0; i < m; i++){
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

  	int maxflow(int s, int t){
		int flow = 0;
		// While there is an augmenting path from source to destination
		while(bfs(s, t) < INF){
			curEdge = new int[n];
			while(true){
				int minf = INF;
				minf = dfs(s, t, minf);
				if(minf == 0) break;
				flow+=minf;
			}
		}
		return flow;
	}

	/**
	 * Finds the distance of the shortest path from the source to destination
	 */
	int bfs(int s, int t){
		dist = new int[n];
		for(int i = 0; i < n; i++) dist[i] = INF;
		dist[s] = 0;
		Queue<Integer> q = new ArrayDeque<>();
		q.add(s);
		while(!q.isEmpty()){
			int u = q.poll();
			if(dist[u] > dist[t]) break;
			for(Edge e : graph[u]){
				if(dist[u] + 1 < dist[e.v] && e.cap > 0){
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
	int dfs(int u, int t, int inflow){
		if(u == t) return inflow;
		for(; curEdge[u] < graph[u].length; ++curEdge[u]){
			Edge e = graph[u][curEdge[u]];
			if(e.cap > 0 && dist[e.v] == dist[u] + 1){
				int outflow = dfs(e.v, t, Math.min(inflow, e.cap));
				if(outflow > 0){
					e.cap -= outflow;
					e.rev.cap +=  outflow;
					return outflow;
				}
			}
		}
		return 0;
	}
	
	class Edge{
		int v, cap;
		Edge rev;
	
		public Edge(int _v, int _cap){
			this.v = _v;
			this.cap = _cap;
		}
	}	
}
