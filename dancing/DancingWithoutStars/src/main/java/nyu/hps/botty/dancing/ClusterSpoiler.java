package nyu.hps.botty.dancing;

import java.io.File;
import java.util.*;

public class ClusterSpoiler extends Spoiler {

    public static void main(String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (args.length == 1) {
            Scanner sc = new Scanner(new File(args[0]));
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine()).append("\n");
            }
            sc.close();
        } else {
            System.out.println("Please specify an input file!");
            System.exit(0);
        }
        ClusterSpoiler r = new ClusterSpoiler();
        r.receiveInput(sb.toString());
        r.receiveGameInfo("30 4 40");
        r.solve();
    }

    private void solve() {
        placeInCluster();
    }

    private void placeInCluster() {
        System.out.println("Trying to place stars in dense places...");
        ClusteringChoreo.Dancer[] dancers = new ClusteringChoreo.Dancer[k * numOfColor];
        int i = 0;
        for (Map.Entry<Integer, ArrayList<Point>> e : this.dancers.entrySet()) {
            for (Point p : e.getValue()) {
                dancers[i++] = new ClusteringChoreo.Dancer(p, e.getKey());
            }
        }
        Map<Point, ArrayList<Point>> centerAndPoints = ClusteringChoreo.cluster(dancers, 300, k, numOfColor, boardSize);
        stars = new ArrayList<>(centerAndPoints.keySet());
        if (!validateStars(stars)) {
            placeInDense();
        }
    }

    private void placeInDense() {
        String[][] cGrid = createGrid();
        System.out.println("Trying to place stars in dense places...");
        PriorityQueue<Triple> l = new PriorityQueue<>((Triple o1, Triple o2) -> {
            if (o1.d == o2.d && o1.x == o2.x) {
                return Integer.compare(o1.y, o2.y);
            } else if (o1.d == o2.d) {
                return Integer.compare(o1.x, o2.x);
            }
            return Integer.compare(o2.d, o1.d);
        });
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                l.add(new Triple(i, j, bfs(cGrid, i, j)));
            }
        }

        stars = new ArrayList<>();
        int placed = 0;
        while (placed < k && !l.isEmpty()) {
            Triple t = l.poll();
            if (t == null) continue;
            Point candidate = new Point(t.x, t.y);
            if (validateStars(stars, candidate)) {
                stars.add(candidate);
            }
        }
        if (stars.size() != k) {
            placeSimple();
        }
    }

    private int bfs(String[][] cGrid, int i, int j) {
        int count = 0;
        boolean[][] visited = new boolean[boardSize][boardSize];
        int[][] moves = {
                {0, 1},
                {0, -1},
                {1, 0},
                {-1, 0}
        };
        visited[i][j] = true;
        ArrayDeque<Triple> q = new ArrayDeque<>();
        q.add(new Triple(i, j, 0));
        while (!q.isEmpty()) {
            Triple t = q.pollFirst();
            if (t == null) continue;
            if (!cGrid[t.x][t.y].equals(" ")) {
                count++;
            }
            for (int[] dir: moves) {
                int newX = t.x + dir[0];
                int newY = t.y + dir[1];
                if (newX < 0 || newX >= boardSize || newY < 0 || newY >= boardSize) continue;
                if (t.d == (numOfColor + 1) / 2) continue;
                if (visited[newX][newY]) continue;
                visited[newX][newY] = true;
                q.add(new Triple(newX, newY, t.d + 1));
            }
        }
        return count;
    }

    private String[][] createGrid() {
        String[][] cGrid = new String[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                cGrid[i][j] = " ";
            }
        }
        for (Map.Entry<Integer, ArrayList<Point>> e: dancers.entrySet()) {
            for (Point p: e.getValue()) {
                cGrid[p.x][p.y] = "" + e.getKey();
            }
        }
        return cGrid;
    }

    private void placeSimple() {
        System.out.println("Falling back to circle packing...");
        stars = new ArrayList<>();
        CirclePack.load();
        if (!CirclePack.m.containsKey(k)) {
            placeRandom();
        }
        ArrayList<Point> centers = CirclePack.m.get(k);
        for (Point p: centers) {
            Point scaled = new Point(p.x * boardSize / 1000, p.y * boardSize / 1000);
            stars.add(scaled);
        }
        if (stars.size() != k || !validateStars(stars)) {
            placeRandom();
        }
    }

    private void placeRandom() {
        System.out.println("Falling back to place stars in random places...");
        int iter = 10000;
        for (int i = 0; i < iter; i++) {
            stars = new ArrayList<>();
            String[][] cGrid = createGrid();
            int placed = 0;
            Random random = new Random();
            while (placed < k) {
                int x = random.nextInt(boardSize);
                int y = random.nextInt(boardSize);
                if (cGrid[x][y].equals(" ") && validateStars(stars, new Point(x, y))) {
                    placed++;
                    stars.add(new Point(x, y));
                    cGrid[x][y] = "#";
                }
            }
            if (stars.size() == k) break;
        }
        if (!validateStars(stars)) {
            HashSet<Integer> toRemove = new HashSet<>();
            for (int i = 0; i < stars.size(); i++) {
                for (int j = i + 1; j < stars.size(); j++) {
                    if (toRemove.contains(i) || toRemove.contains(j)) continue;
                    Point p = stars.get(i);
                    Point q = stars.get(j);
                    int dist = Math.abs(p.x - q.x) + Math.abs(p.y - q.y);
                    if (dist < numOfColor + 1) {
                        toRemove.add(j);
                    }
                }
            }
            List<Point> newStars = new ArrayList<>();
            for (int i = 0; i < stars.size(); i++) {
                if (!toRemove.contains(i)) {
                    newStars.add(stars.get(i));
                }
            }
            stars = newStars;
        }
    }

    private boolean validateStars(List<Point> stars, Point newS) {
        for (Point p: stars) {
            int dist = Math.abs(p.x - newS.x) + Math.abs(p.y - newS.y);
            if (dist < numOfColor + 1) return false;
        }
        return validateStars(stars);
    }

    private boolean validateStars(List<Point> stars) {
        for (int i = 0; i < stars.size(); i++) {
            for (int j = 0; j < stars.size(); j++) {
                if (i == j) continue;
                Point p = stars.get(i);
                Point q = stars.get(j);
                int dist = Math.abs(p.x - q.x) + Math.abs(p.y - q.y);
                if (dist < numOfColor + 1) return false;
            }
        }
        return true;
    }

    @Override
    List<Point> getStars() {
        solve();
        return stars;
    }

    static class Triple {
        int x, y, d;

        Triple(int x, int y, int d) {
            this.x = x;
            this.y = y;
            this.d = d;
        }
    }

}
