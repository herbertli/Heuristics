package edu.nyu.cs.hps.evasion;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class HalfHunter extends EvasionClient {

    Polygon currentShape;
    double currentArea;

    HalfHunter(String name, int port) throws IOException {
        super(name, port);
        currentShape = new Polygon();
        currentShape.addPoint(0, 0);
        currentShape.addPoint(0, 300);
        currentShape.addPoint(300, 300);
        currentShape.addPoint(300, 0);
        currentArea = polygonArea(currentShape.xpoints, currentShape.ypoints, currentShape.npoints);
    }

    public static double polygonArea(int X[], int Y[], int n) {
        double area = 0.0;
        int j = n - 1;
        for (int i = 0; i < n; i++) {
            area += (X[j] + X[i]) * (Y[j] - Y[i]);
            j = i;
        }
        return Math.abs(area / 2.0);
    }

    @Override
    public HunterMove playHunter() {
        HunterMove move = new HunterMove();
        if (gameState.wallTimer == 0) {

        } else {
            move.wallType = 0;
            move.wallsToDel = new ArrayList<>();
        }
        return move;
    }



}
