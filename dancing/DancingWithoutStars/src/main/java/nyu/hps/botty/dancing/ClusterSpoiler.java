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

    private void placeInDense() {
        String[][] cGrid = createGrid();

        PriorityQueue<Triple> l = new PriorityQueue<>((Triple o1, Triple o2) -> Integer.compare(o2.d, o1.d));
        int[][] dancersInRange = new int[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                dancersInRange[i][j] = bfs(cGrid, i, j);
                l.add(new Triple(i, j, dancersInRange[i][j]));
            }
        }

        stars = new ArrayList<>();
        int placed = 0;
        outer:
        while (placed < k) {
            Triple t = l.poll();
            if (t == null) continue;
            Point candidate = new Point(t.x, t.y);
            for (Point p: stars) {
                int dist = Math.abs(p.x - candidate.x) + Math.abs(p.y - candidate.y);
                if (dist < numOfColor + 1) {
                    continue outer;
                }
            }
            stars.add(candidate);
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
        while (true) {
            stars = new ArrayList<>();
            String[][] cGrid = createGrid();
            int placed = 0;
            Random random = new Random();
            outer:
            while (placed < k) {
                int x = random.nextInt(boardSize);
                int y = random.nextInt(boardSize);
                for (Point p : stars) {
                    int dist = Math.abs(p.x - x) + Math.abs(p.y - y);
                    if (dist < numOfColor + 1) {
                        continue outer;
                    }
                }
                if (cGrid[x][y].equals(" ")) {
                    placed++;
                    stars.add(new Point(x, y));
                    cGrid[x][y] = "#";
                }
            }
            if (stars.size() == k) break;
        }
    }

    @Override
    void placeStars() {
        solve();
    }

    private void solve() {
        placeInDense();
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
