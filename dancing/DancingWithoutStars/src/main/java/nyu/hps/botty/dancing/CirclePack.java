package nyu.hps.botty.dancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

class CirclePack {

    static HashMap<Integer, ArrayList<Point>> m = new HashMap<>();

    static void load() {
        Scanner in = new Scanner(CirclePack.class.getClassLoader()
                .getResourceAsStream("circlepacking"));
        int n = 1;
        while (in.hasNextLine()) {
            String lineS = in.nextLine();
            String[] line = lineS.trim().split(" ");
            if (line.length == 0) break;
            m.put(n, new ArrayList<>());
            for (int i = 0; i < line.length; i += 2) {
                int a = Integer.parseInt(line[i]);
                int b = Integer.parseInt(line[i + 1]);
                m.get(n).add(new Point(a, b));
            }
            n++;
        }
    }

}
