package nyu.hps.botty.dancing;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Player {

    int boardSize;
    int numOfColor;
    int k;
    HashMap<Integer, ArrayList<Point>> dancers;

    void receiveInput(String danceData) {
        dancers = new HashMap<Integer, ArrayList<Point>>();
        int currentColor = -1;
        for (String line: danceData.replace("&", "").trim().split("\n")) {
            String[] byWord = line.split(" ");
            if (byWord.length > 2) {
                currentColor = Integer.parseInt(byWord[byWord.length - 1]);
            } else {
                if (!dancers.containsKey(currentColor)) {
                    dancers.put(currentColor, new ArrayList<Point>());
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
