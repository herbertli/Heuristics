package ambulance.botty;

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.apache.commons.math3.ml.clustering.CentroidCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Solution {
    ArrayList<Hospital> hospitalArrayList;
    List<CentroidCluster<Patient>> clusters;
    Collection<VehicleRoute> routes;

    public Solution(ArrayList<Hospital> hospitalArrayList, List<CentroidCluster<Patient>> clusters, Collection<VehicleRoute> routes) {
        this.hospitalArrayList = hospitalArrayList;
        this.clusters = clusters;
        this.routes = routes;
    }
}
