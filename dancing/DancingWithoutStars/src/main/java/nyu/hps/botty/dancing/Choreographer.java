package nyu.hps.botty.dancing;


import java.util.ArrayList;
import java.util.HashMap;

public abstract class Choreographer extends Player {

    abstract HashMap<Integer, ArrayList<Point>> placeDancers();
    abstract String getMoveString();
    abstract String getLineString();

}
