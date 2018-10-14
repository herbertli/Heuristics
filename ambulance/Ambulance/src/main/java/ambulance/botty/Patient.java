package ambulance.botty;

import org.apache.commons.math3.ml.clustering.Clusterable;

public class Patient implements Clusterable {

    int id, x, y, deathTime;

    public Patient(int id, int x, int y, int deathTime) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.deathTime = deathTime;
    }

    public double[] getPoint() {
        return new double[]{this.x, this.y};
    }
}
