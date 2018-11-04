package nyu.hps.botty.dancing;

import java.util.List;

abstract class Spoiler extends Player {

    String getMoveString(List<Point> stars) {
        StringBuilder sb = new StringBuilder();
        for (Point p : stars) {
            sb.append(p.x).append(" ").append(p.y).append(" ");
        }
        if (stars.size() != 0) sb.deleteCharAt(sb.length() - 1);
        sb.append("&");
        return sb.toString();
    }

    abstract List<Point> getStars();
    abstract void placeStars();

}
