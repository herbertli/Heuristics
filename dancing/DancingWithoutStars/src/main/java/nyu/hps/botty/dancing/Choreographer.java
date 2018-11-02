package nyu.hps.botty.dancing;


public abstract class Choreographer extends Player {

    abstract void solve();
    abstract String getMoveString();
    abstract String getLineString();

}
