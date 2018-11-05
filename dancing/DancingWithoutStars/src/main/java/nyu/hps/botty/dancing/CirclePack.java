package nyu.hps.botty.dancing;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

class CirclePack {

    static TreeMap<Integer, ArrayList<Point>> m = new TreeMap<>();

    static void load() {
        Scanner in = new Scanner(CirclePack.class.getClassLoader()
                .getResourceAsStream("circlepacking"));

        int n = 1;
        while (in.hasNextLine()) {
            String lineS = in.nextLine();
            String[] line = lineS.trim().split(" ");
            if (line.length == 0) break;
            int radius;
            if (n == 1) {
                radius  = 500;
            } else {
                int firstX = Integer.parseInt(line[0]);
                int firstY = Integer.parseInt(line[1]);
                int secondX = Integer.parseInt(line[2]);
                int secondY = Integer.parseInt(line[3]);
                double distanceSq = Math.pow(firstX - secondX, 2) + Math.pow(firstY - secondY, 2);
                double distance = Math.sqrt(distanceSq);
                radius = (int) Math.ceil(distance / 2);
            }
            m.put(radius, new ArrayList<>());
            for (int i = 0; i < line.length; i += 2) {
                int a = Integer.parseInt(line[i]);
                int b = Integer.parseInt(line[i + 1]);
                m.get(radius).add(new Point(a, b));
            }
            n++;
        }
    }

}
