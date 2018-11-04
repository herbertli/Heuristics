package nyu.hps.botty.dancing;


import java.util.List;

abstract class Choreographer extends Player {

    abstract void solve();
    abstract List<Point>[] getPaths();
    abstract Point[][] getLines();

    String getMoveString(List<Point>[] paths) {
        StringBuilder sb = new StringBuilder();
        Point[] currentLocs = new Point[paths.length];
        int maxTime = -1;
        for (int i = 0; i < paths.length; i++) {
            currentLocs[i] = paths[i].get(0);
            maxTime = Math.max(maxTime, paths[i].size());
        }

        for (int t = 1; t < maxTime; t++) {
            StringBuilder move = new StringBuilder();
            int numMoves = 0;
            for (int i = 0; i < paths.length; i++) {
                if (t >= paths[i].size()) continue;

                Point currP = currentLocs[i];
                Point nextLoc = paths[i].get(t);
                if (currP.equals(nextLoc)) continue;

                numMoves++;
                move.append(currP.x).append(" ").append(currP.y).append(" ");
                move.append(nextLoc.x).append(" ").append(nextLoc.y).append(" ");
                currentLocs[i] = nextLoc;
            }
            if (numMoves == 0) {
                sb.append("0");
            } else {
                if (move.charAt(move.length() - 1) == ' ') move.deleteCharAt(move.length() - 1);
                sb.append(numMoves).append(" ").append(move.toString());
            }
            sb.append("&");
        }
        return sb.toString();
    }

    String getLineString(Point[][] lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            Point startP = lines[i][0];
            Point endP = lines[i][1];
            sb.append(startP.x).append(" ").append(startP.y).append(" ");
            sb.append(endP.x).append(" ").append(endP.y);
            if (i != lines.length - 1) {
                sb.append(" ");
            }
        }
        sb.append("&");
        return sb.toString();
    }

}
