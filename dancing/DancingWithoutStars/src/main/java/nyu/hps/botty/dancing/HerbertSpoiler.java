package nyu.hps.botty.dancing;

import java.io.File;
import java.util.*;

public class HerbertSpoiler extends Spoiler {

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
        HerbertSpoiler r = new HerbertSpoiler();
        r.receiveInput(sb.toString());
        r.receiveGameInfo("30 4 40");
        r.solve();
        System.out.println(r.getMoveString());
    }

    private void solve() {
        while (true) {
            if (placeSimple()) break;
        }
//        placeStars();
    }

    private boolean placeSimple() {
        stars = new ArrayList<>();
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
        int placed = 0;
        Random random = new Random();
        outer:
        while (placed < k) {
            int x = random.nextInt(boardSize);
            int y = random.nextInt(boardSize);
            for (Point p: stars) {
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
        Utils.printGrid(cGrid);
        return true;
    }

    @Override
    String getMoveString() {
        StringBuilder sb = new StringBuilder();
        for (Point p : stars) {
            sb.append(p.x).append(" ").append(p.y).append(" ");
        }
        if (stars.size() != 0) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    // place stars in the most densely populated areas...
    @Override
    void placeStars() {
        List<Point> potentialStars = new ArrayList<>();
        ArrayList<Point> inputData = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Point>> e : dancers.entrySet()) {
            inputData.addAll(e.getValue());
        }

        while (potentialStars.size() < k) {
            KMeans kMeans = new KMeans(boardSize);

            List<KMeans.Mean> means = kMeans.predict(k - potentialStars.size(), inputData);
            means.sort((o1, o2) -> Integer.compare(o2.mClosestItems.size(), o1.mClosestItems.size()));
            KMeans.Mean bestMean = means.get(0);
            potentialStars.add(new Point(bestMean.mCentroid.x, bestMean.mCentroid.y));
            for (Point p: bestMean.mClosestItems) {
                inputData.remove(p);
            }
        }

        outer:
        while (true) {
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    if (i == j) continue;
                    Point star1 = stars.get(i);
                    Point star2 = stars.get(j);
                    int dist = Math.abs(star1.x - star2.x) + Math.abs(star1.y - star2.y);
                    if (dist < numOfColor + 1) {
                        correct(i, j);
                        continue outer;
                    }
                }
            }
            break;
        }

        stars = potentialStars;
    }

    private void correct(int i, int j) {

    }

    static class KMeans {
        private final Random mRandomState;
        private final int mMaxIterations;
        private float mSqConvergenceEpsilon;
        private final int boardSize;

        KMeans(int boardSize) {
            this(new Random(), boardSize);
        }

        KMeans(Random random, int boardSize) {
            this(random, 30 /* maxIterations */, 0.005f /* convergenceEpsilon */, boardSize);
        }

        KMeans(Random random, int maxIterations, float convergenceEpsilon, int boardSize) {
            this.boardSize = boardSize;
            mRandomState = random;
            mMaxIterations = maxIterations;
            mSqConvergenceEpsilon = convergenceEpsilon * convergenceEpsilon;
        }

        List<Mean> predict(final int k, final ArrayList<Point> inputData) {
            final ArrayList<Mean> means = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                Mean m = new Mean(mRandomState.nextInt(boardSize), mRandomState.nextInt(boardSize));
                means.add(m);
            }

            // Iterate until we converge or run out of iterations
            for (int i = 0; i < mMaxIterations; i++) {
                boolean converged = step(means, inputData);
                if (converged) {
                    break;
                }
            }
            return means;
        }

        private boolean step(final ArrayList<Mean> means, ArrayList<Point> inputData) {
            // Clean up the previous state because we need to compute
            // which point belongs to each mean again.
            for (int i = means.size() - 1; i >= 0; i--) {
                final Mean mean = means.get(i);
                mean.mClosestItems.clear();
            }
            for (int i = inputData.size() - 1; i >= 0; i--) {
                final Point current = inputData.get(i);
                final Mean nearest = nearestMean(current, means);
                nearest.mClosestItems.add(current);
            }
            boolean converged = true;
            // Move each mean towards the nearest data set points
            for (int i = means.size() - 1; i >= 0; i--) {
                final Mean mean = means.get(i);
                if (mean.mClosestItems.size() == 0) {
                    continue;
                }
                // Compute the new mean centroid:
                //   1. Sum all all points
                //   2. Average them
                final Point oldCentroid = mean.mCentroid;
                mean.mCentroid = new Point(-1, -1);
                for (int j = 0; j < mean.mClosestItems.size(); j++) {
                    // Update each centroid component
                    mean.mCentroid.x += mean.mClosestItems.get(j).x;
                    mean.mCentroid.y += mean.mClosestItems.get(j).y;
                }
                mean.mCentroid.x /= mean.mClosestItems.size();
                mean.mCentroid.y /= mean.mClosestItems.size();
                // We converged if the centroid didn't move for any of the means.
                if (sqDistance(oldCentroid, mean.mCentroid) > mSqConvergenceEpsilon) {
                    converged = false;
                }
            }
            return converged;
        }

        static Mean nearestMean(Point point, List<Mean> means) {
            Mean nearest = null;
            float nearestDistance = Float.MAX_VALUE;
            for (Mean next: means) {
                // We don't need the sqrt when comparing distances in euclidean space
                // because they exist on both sides of the equation and cancel each other out.
                float nextDistance = sqDistance(point, next.mCentroid);
                if (nextDistance < nearestDistance) {
                    nearest = next;
                    nearestDistance = nextDistance;
                }
            }
            return nearest;
        }

        static float sqDistance(Point a, Point b) {
            return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
        }


        static class Mean {
            Point mCentroid;
            final ArrayList<Point> mClosestItems = new ArrayList<>();

            Mean(int x, int y) {
                mCentroid = new Point(x, y);
            }
        }
    }
}
