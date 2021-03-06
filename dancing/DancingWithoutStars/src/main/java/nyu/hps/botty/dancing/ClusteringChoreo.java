package nyu.hps.botty.dancing;

import java.util.*;

class ClusteringChoreo extends Choreographer {

    private Point[][] lineSolution;
    private List<Point>[] pathSolution;
    private String[][] grid;

    void solve() {

        // generate cluster centers
        Dancer[] dancers = new Dancer[k * numOfColor];
        int i = 0;
        for (Map.Entry<Integer, ArrayList<Point>> e : this.dancers.entrySet()) {
            for (Point p : e.getValue()) {
                dancers[i++] = new Dancer(p, e.getKey());
            }
        }
        Map<Point, ArrayList<Point>> centerAndPoints = cluster(dancers, 200, k, numOfColor, boardSize);

        // create grid
        grid = createGrid(centerAndPoints);

        // create lines and generate pairs from each dancer to their final position
        Point[][] startAndEndPairs = assignLines(centerAndPoints);

        // generate paths from each dancer to their assigned line
        pathSolution = Utils.generatePaths(startAndEndPairs, grid, 1000);

    }

    @Override
    List<Point>[] getPaths() {
        return pathSolution;
    }

    @Override
    Point[][] getLines() {
        return lineSolution;
    }

    static HashMap<Point, ArrayList<Point>> cluster(Dancer[] dancers, int iter, int numClusters, int numOfColor, int boardSize) {
        Random random = new Random();
        Point[] centers = new Point[numClusters];


        // randomly get initial cluster centers
        for (int c = 0; c < numClusters; c++) {
            centers[c] = new Point(random.nextInt(boardSize), random.nextInt(boardSize));
        }
        int[] assignments = new int[dancers.length];

        boolean converged = false;
        for (int n = 0; n < iter && !converged; n++) {
            for (int c = 1; c <= numOfColor; c++) {
                bipartiteMatch(assignments, centers, dancers, c, numClusters);
            }
            int[] xs = new int[numClusters];
            int[] ys = new int[numClusters];
            for (int d = 0; d < assignments.length; d++) {
                int cluster = assignments[d];
                xs[cluster] += dancers[d].p.x;
                ys[cluster] += dancers[d].p.y;
            }
            Point[] newClusters = new Point[numClusters];
            for (int c = 0; c < numClusters; c++) {
                newClusters[c] = new Point(Math.round(xs[c] / numOfColor), Math.round(ys[c] / numOfColor));
            }

            converged = true;
            for (int c = 0; c < numClusters; c++) {
                if (newClusters[c].x != centers[c].x || newClusters[c].y != centers[c].y) {
                    converged = false;
                    break;
                }
            }
            if (converged) {
                System.out.printf("Converged after %d iterations.\n", iter);
            }
            centers = newClusters;
        }
        HashMap<Point, ArrayList<Point>> res = new HashMap<>();
        for (int c = 0; c < numClusters; c++) {
            res.put(centers[c], new ArrayList<>());
        }
        for (int d = 0; d < dancers.length; d++) {
            res.get(centers[assignments[d]]).add(dancers[d].p);
        }
        return res;
    }

    private static void bipartiteMatch(int[] assignments, Point[] centers, Dancer[] dancers, int color, int k) {
        // points holds all dancers of color
        Point[] points = new Point[k];
        int j = 0;
        for (Dancer d : dancers) {
            if (d.color == color) {
                points[j++] = d.p;
            }
        }
        double[][] costMatrix = new double[k][k];
        for (int i = 0; i < k; i++) {
            for (j = 0; j < k; j++) {
                double dist = Math.abs(points[i].x - centers[j].x) + Math.abs(points[i].y - centers[j].y);
                costMatrix[i][j] = dist;
            }
        }
        HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costMatrix);
        int[] assignedC = hungarianAlgorithm.execute();
        j = 0;
        for (int i = 0; i < dancers.length; i++) {
            if (dancers[i].color == color) {
                assignments[i] = assignedC[j++];
            }
        }
    }


    // for now, all the lines will be horizontal, or all the lines will be vertical
    private Point[][] assignLines(Map<Point, ArrayList<Point>> centerAndPoints) {
        double vertDist = 0;
        double horiDist = 0;
        for (Point c : centerAndPoints.keySet()) {
            ArrayList<Point> pts = centerAndPoints.get(c);
            for (Point p : pts) {
                vertDist += Math.abs(c.y - p.y);
                horiDist += Math.abs(c.x - p.x);
            }
        }
        vertDist /= numOfColor * k;
        horiDist /= numOfColor * k;
        boolean allHori = true;

        // means the points are closer along the x-axis -> form vertical groups
        if (horiDist <= vertDist) allHori = false;

        Cluster[] lines = new Cluster[k];
        int pInd = 0;
        for (Point c : centerAndPoints.keySet()) {
            Cluster g = new Cluster();
            calculateCluster(c, g, centerAndPoints.get(c), allHori);
            lines[pInd++] = g;
        }

        int iter = 0;
        while (true) {
            iter++;
            if (iter % 100 == 0) {
                System.out.println("Iteration: " + iter);
            }
            boolean hasIntersection = false;
            int inter1 = -1;
            int inter2 = -1;
            outer:
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    if (i == j) continue;
                    Cluster a = lines[i];
                    Cluster b = lines[j];
                    if (allHori && a.center.y != b.center.y) continue;
                    if (!allHori && a.center.x != b.center.x) continue;
                    if (a.start <= b.end && a.start >= b.start) {
                        hasIntersection = true;
                        inter1 = i;
                        inter2 = j;
                        break outer;
                    } else if (b.start <= a.end && b.start >= a.start) {
                        hasIntersection = true;
                        inter1 = i;
                        inter2 = j;
                        break outer;
                    }
                }
            }
            if (!hasIntersection) {
                break;
            } else {
                Cluster worseGroup;
                int worseInd;
                Cluster g1 = lines[inter1];
                Cluster g2 = lines[inter2];
                if (g1.avgDist <= g2.avgDist) {
                    worseGroup = g2;
                    worseInd = inter2;
                } else {
                    worseGroup = g1;
                    worseInd = inter1;
                }
                int shift = 1;
                while (true) {
                    Cluster newCluster = new Cluster();
                    Point newCenter;
                    if (allHori) {
                        if (worseGroup.center.y + shift <= boardSize - 1) {
                            newCenter = new Point(worseGroup.center.x, worseGroup.center.y + shift);
                        } else {
                            newCenter = new Point(worseGroup.center.x, worseGroup.center.y - shift);
                        }
                    } else {
                        if (worseGroup.center.x + shift <= boardSize - 1) {
                            newCenter = new Point(worseGroup.center.x + shift, worseGroup.center.y);
                        } else {
                            newCenter = new Point(worseGroup.center.x - shift, worseGroup.center.y);
                        }
                    }
                    boolean valid = calculateCluster(newCenter, newCluster, worseGroup.points, allHori);
                    lines[worseInd] = newCluster;
                    if (valid) break;
                    shift++;
                }
            }
        }

        // save lines
        lineSolution = new Point[k][2];
        int lInd = 0;
        for (Cluster c: lines) {
            if (allHori) {
                int yCoord = c.center.y;
                lineSolution[lInd][0] = new Point(Math.min(c.start, c.end), yCoord);
                lineSolution[lInd++][1] = new Point(Math.max(c.start, c.end), yCoord);
            } else {
                int xCoord = c.center.x;
                lineSolution[lInd][0] = new Point(xCoord, Math.min(c.start, c.end));
                lineSolution[lInd++][1] = new Point(xCoord, Math.max(c.start, c.end));
            }
        }

        // generate (startLoc, endLoc) pairs...
        Point[][] startAndEnd = new Point[numOfColor * k][2];
        int aInd = 0;
        for (Cluster g : lines) {
            double[][] costMatrix = new double[numOfColor][numOfColor];
            Point[] lineCoords = new Point[numOfColor];
            int currX;
            int currY;
            if (allHori) {
                currY = g.center.y;
                currX = g.start;
            } else {
                currX = g.center.x;
                currY = g.start;
            }
            for (int j = 0; j < this.numOfColor; j++) {
                lineCoords[j] = new Point(currX, currY);
                if (allHori) {
                    currX++;
                } else {
                    currY++;
                }
            }
            pInd = 0;
            for (Point p : g.points) {
                for (int l = 0; l < numOfColor; l++) {
                    costMatrix[pInd][l] = Math.abs(p.x - lineCoords[l].x) + Math.abs(p.y - lineCoords[l].y);
                }
            }
            HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costMatrix);
            int[] assignments = hungarianAlgorithm.execute();
            for (int l = 0; l < g.points.size(); l++) {
                startAndEnd[aInd][0] = g.points.get(l);
                startAndEnd[aInd][1] = lineCoords[assignments[l]];
                aInd++;
            }
        }
        return startAndEnd;
    }

    private boolean calculateCluster(Point c, Cluster g, ArrayList<Point> points, boolean allHori) {
        double dist = 0;
        for (Point p : points) {
            if (allHori) dist += Math.abs(c.y - p.y);
            else dist += Math.abs(c.x - p.x);
        }
        g.points = points;
        g.center = c;
        g.avgDist = dist / numOfColor;
        int start = allHori ? c.x : c.y;
        int end = allHori ? c.x : c.y;
        int length = 0;
        Point startP = new Point(c.x, c.y);
        Point endP = new Point(c.x, c.y);
        for (int i = 0; i < numOfColor; i++) {
            Point newEnd;
            if (allHori) newEnd = new Point(endP.x + 1, endP.y);
            else newEnd = new Point(endP.x, endP.y + 1);
            if (end < this.boardSize - 1 && !grid[newEnd.x][newEnd.y].equals("#")) {
                end++;
                endP = newEnd;
                length++;
                i++;
            }

            Point newStart;
            if (allHori) newStart = new Point(startP.x - 1, startP.y);
            else newStart = new Point(startP.x, startP.y - 1);
            if (start > 0 && !grid[newStart.x][newStart.y].equals("#")) {
                start--;
                startP = newStart;
                length++;
            }

        }
        g.start = start;
        g.end = end;
        return length == numOfColor;
    }

    private String[][] createGrid(Map<Point, ArrayList<Point>> centerAndPoints) {
        String[][] grid = new String[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                grid[i][j] = " ";
            }
        }
        if (stars != null && stars.size() > 0) {
            for (Point s : stars) {
                grid[s.x][s.y] = "#";
            }
        }
        for (Map.Entry<Integer, ArrayList<Point>> e : dancers.entrySet()) {
            for (Point p : e.getValue()) {
                grid[p.x][p.y] = e.getKey() + "";
            }
        }
        for (Point p : centerAndPoints.keySet()) {
            grid[p.x][p.y] = "X";
        }
        return grid;
    }

    static class Dancer {
        Point p;
        int color;

        Dancer(Point p, int color) {
            this.p = p;
            this.color = color;
        }
    }

    static class Cluster {
        double avgDist;
        int start, end;
        ArrayList<Point> points;
        Point center;

    }

    // taken from:
    // https://github.com/KevinStern/software-and-algorithms/blob/master/src/main/java/blogspot/software_and_algorithms/stern_library/optimization/HungarianAlgorithm.java
    // but I'm pretty sure he took it from somewhere else... (similar implementations are floating around)
    static class HungarianAlgorithm {

        private final double[][] costMatrix;
        private final int rows, cols, dim;
        private final double[] labelByWorker, labelByJob;
        private final int[] minSlackWorkerByJob;
        private final double[] minSlackValueByJob;
        private final int[] matchJobByWorker, matchWorkerByJob;
        private final int[] parentWorkerByCommittedJob;
        private final boolean[] committedWorkers;

        HungarianAlgorithm(double[][] costMatrix) {
            this.dim = Math.max(costMatrix.length, costMatrix[0].length);
            this.rows = costMatrix.length;
            this.cols = costMatrix[0].length;
            this.costMatrix = new double[this.dim][this.dim];
            for (int w = 0; w < this.dim; w++) {
                if (w < costMatrix.length) {
                    if (costMatrix[w].length != this.cols) {
                        throw new IllegalArgumentException("Irregular cost matrix");
                    }
                    this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
                } else {
                    this.costMatrix[w] = new double[this.dim];
                }
            }
            labelByWorker = new double[this.dim];
            labelByJob = new double[this.dim];
            minSlackWorkerByJob = new int[this.dim];
            minSlackValueByJob = new double[this.dim];
            committedWorkers = new boolean[this.dim];
            parentWorkerByCommittedJob = new int[this.dim];
            matchJobByWorker = new int[this.dim];
            Arrays.fill(matchJobByWorker, -1);
            matchWorkerByJob = new int[this.dim];
            Arrays.fill(matchWorkerByJob, -1);
        }

        void computeInitialFeasibleSolution() {
            for (int j = 0; j < dim; j++) {
                labelByJob[j] = Double.POSITIVE_INFINITY;
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < labelByJob[j]) {
                        labelByJob[j] = costMatrix[w][j];
                    }
                }
            }
        }

        int[] execute() {
            reduce();
            computeInitialFeasibleSolution();
            greedyMatch();

            int w = fetchUnmatchedWorker();
            while (w < dim) {
                initializePhase(w);
                executePhase();
                w = fetchUnmatchedWorker();
            }
            int[] result = Arrays.copyOf(matchJobByWorker, rows);
            for (w = 0; w < result.length; w++) {
                if (result[w] >= cols) {
                    result[w] = -1;
                }
            }
            return result;
        }

        void executePhase() {
            while (true) {
                int minSlackWorker = -1, minSlackJob = -1;
                double minSlackValue = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dim; j++) {
                    if (parentWorkerByCommittedJob[j] == -1) {
                        if (minSlackValueByJob[j] < minSlackValue) {
                            minSlackValue = minSlackValueByJob[j];
                            minSlackWorker = minSlackWorkerByJob[j];
                            minSlackJob = j;
                        }
                    }
                }
                if (minSlackValue > 0) {
                    updateLabeling(minSlackValue);
                }
                parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
                if (matchWorkerByJob[minSlackJob] == -1) {
                    /*
                     * An augmenting path has been found.
                     */
                    int committedJob = minSlackJob;
                    int parentWorker = parentWorkerByCommittedJob[committedJob];
                    while (true) {
                        int temp = matchJobByWorker[parentWorker];
                        match(parentWorker, committedJob);
                        committedJob = temp;
                        if (committedJob == -1) {
                            break;
                        }
                        parentWorker = parentWorkerByCommittedJob[committedJob];
                    }
                    return;
                } else {
                    /*
                     * Update slack values since we increased the size of the committed
                     * workers set.
                     */
                    int worker = matchWorkerByJob[minSlackJob];
                    committedWorkers[worker] = true;
                    for (int j = 0; j < dim; j++) {
                        if (parentWorkerByCommittedJob[j] == -1) {
                            double slack = costMatrix[worker][j] - labelByWorker[worker]
                                    - labelByJob[j];
                            if (minSlackValueByJob[j] > slack) {
                                minSlackValueByJob[j] = slack;
                                minSlackWorkerByJob[j] = worker;
                            }
                        }
                    }
                }
            }
        }

        int fetchUnmatchedWorker() {
            int w;
            for (w = 0; w < dim; w++) {
                if (matchJobByWorker[w] == -1) {
                    break;
                }
            }
            return w;
        }

        void greedyMatch() {
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (matchJobByWorker[w] == -1 && matchWorkerByJob[j] == -1
                            && costMatrix[w][j] - labelByWorker[w] - labelByJob[j] == 0) {
                        match(w, j);
                    }
                }
            }
        }

        void initializePhase(int w) {
            Arrays.fill(committedWorkers, false);
            Arrays.fill(parentWorkerByCommittedJob, -1);
            committedWorkers[w] = true;
            for (int j = 0; j < dim; j++) {
                minSlackValueByJob[j] = costMatrix[w][j] - labelByWorker[w]
                        - labelByJob[j];
                minSlackWorkerByJob[j] = w;
            }
        }

        void match(int w, int j) {
            matchJobByWorker[w] = j;
            matchWorkerByJob[j] = w;
        }

        void reduce() {
            for (int w = 0; w < dim; w++) {
                double min = Double.POSITIVE_INFINITY;
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < min) {
                        min = costMatrix[w][j];
                    }
                }
                for (int j = 0; j < dim; j++) {
                    costMatrix[w][j] -= min;
                }
            }
            double[] min = new double[dim];
            for (int j = 0; j < dim; j++) {
                min[j] = Double.POSITIVE_INFINITY;
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    if (costMatrix[w][j] < min[j]) {
                        min[j] = costMatrix[w][j];
                    }
                }
            }
            for (int w = 0; w < dim; w++) {
                for (int j = 0; j < dim; j++) {
                    costMatrix[w][j] -= min[j];
                }
            }
        }

        void updateLabeling(double slack) {
            for (int w = 0; w < dim; w++) {
                if (committedWorkers[w]) {
                    labelByWorker[w] += slack;
                }
            }
            for (int j = 0; j < dim; j++) {
                if (parentWorkerByCommittedJob[j] != -1) {
                    labelByJob[j] -= slack;
                } else {
                    minSlackValueByJob[j] -= slack;
                }
            }
        }
    }

}
