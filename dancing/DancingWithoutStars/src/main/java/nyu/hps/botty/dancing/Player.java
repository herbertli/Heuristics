package nyu.hps.botty.dancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

abstract class Player {

    int boardSize;
    int numOfColor;
    int k;
    HashMap<Integer, ArrayList<Point>> dancers;
    List<Point> stars;

    void receiveStars(String starString) {
        starString = starString.replace("&", "");
        stars = new ArrayList<>();
        String[] temp = starString.split(" ");
        for (int i = 0; i < temp.length; i += 2) {
            int x = Integer.parseInt(temp[i]);
            int y = Integer.parseInt(temp[i + 1]);
            stars.add(new Point(x, y));
        }
    }

    void receiveInput(String danceData) {
        danceData = danceData.replace("&", "");
        dancers = new HashMap<>();
        int currentColor = -1;
        for (String line: danceData.split("\n")) {
            String[] byWord = line.split(" ");
            if (byWord.length > 2) {
                currentColor = Integer.parseInt(byWord[byWord.length - 1]);
            } else {
                if (!dancers.containsKey(currentColor)) {
                    dancers.put(currentColor, new ArrayList<>());
                }
                int x = Integer.parseInt(byWord[0]);
                int y = Integer.parseInt(byWord[1]);
                dancers.get(currentColor).add(new Point(x, y));
            }
        }
    }

    void receiveGameInfo(String gameInfo) {
        String[] temp = gameInfo.replace("&", "").trim().split(" ");
        this.boardSize = Integer.parseInt(temp[0]);
        this.numOfColor = Integer.parseInt(temp[1]);
        this.k = Integer.parseInt(temp[2]);
    }

}
