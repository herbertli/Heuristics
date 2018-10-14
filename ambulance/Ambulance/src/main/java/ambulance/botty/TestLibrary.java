package ambulance.botty;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.*;
import com.graphhopper.jsprit.util.Examples;
import org.apache.commons.math3.ml.clustering.*;

import java.util.*;
import java.util.ArrayList;

class CompareByNumAmb implements Comparator<Hospital> {
    public int compare(Hospital a, Hospital b){
        return Integer.compare(a.ambulancesAtStart.size(), b.ambulancesAtStart.size());
    }
}


class CompareByClusterSize implements Comparator<Cluster> {
    public int compare(Cluster a, Cluster b){
        return Integer.compare(a.getPoints().size(), b.getPoints().size());
    }
}

public class TestLibrary {

    static void run(ArrayList<Patient> patients, ArrayList<Hospital> hospitals, ArrayList<Ambulance> ambulances) {
        List<CentroidCluster<Patient>> clusters = cluster(patients);
        Collections.sort(hospitals, new CompareByNumAmb());
        Collections.sort(clusters, new CompareByClusterSize());
        Examples.createOutputFolder();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        // max time of the problem
        double maxDuration = 10000;

        int nuOfVehicles = 0;
        int capacity = 4;
        // define depots
        int depotCounter = 1;
        for(int i = 0; i < hospitals.size(); i++){
            for(int j = 0; j < hospitals.get(i).ambulancesAtStart.size(); j++){
                VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type")
                        .addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
                String vehicleId = depotCounter + "_" + (hospitals.get(i).ambulancesAtStart.get(j)) + "_vehicle";
                VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleId);
                vehicleBuilder.setStartLocation(Location.newInstance(Math.round(clusters.get(i).getCenter().getPoint()[0]), Math.round(clusters.get(i).getCenter().getPoint()[1])));
                vehicleBuilder.setType(vehicleType);
                vehicleBuilder.setLatestArrival(maxDuration);
                VehicleImpl vehicle = vehicleBuilder.build();
                vrpBuilder.addVehicle(vehicle);

            }
        }
        // define problem with finite fleet
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        for(CentroidCluster<Patient> cc : clusters){
            for(Patient p: cc.getPoints()){
                Shipment shipment = Shipment.Builder.newInstance("" + p.id)
                        .addPickupTimeWindow(0, p.deathTime)
                        .addDeliveryTimeWindow(0, p.deathTime)
                        .addSizeDimension(0, 1)
                        .setPickupLocation(Location.newInstance(p.x, p.y))
                        .setDeliveryLocation(Location.newInstance(Math.round(cc.getCenter().getPoint()[0]), Math.round(cc.getCenter().getPoint()[1]))
                        )
                        .build();
                vrpBuilder.addJob(shipment);

            }
        }


        // Pre-calculate distances
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);

        // build the problem
        VehicleRoutingProblem vrp = vrpBuilder.build();

        // solve the problem
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                .setProperty(Jsprit.Parameter.THREADS, "5")
                .buildAlgorithm();

        vra.setMaxIterations(2000);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        SolutionPrinter.print(bestSolution);

        // plot
        Plotter plotter = new Plotter(vrp, bestSolution);
        plotter.setLabel(Plotter.Label.SIZE);
        plotter.plot("output/solution.png", "solution");

    }

    static List<CentroidCluster<Patient>> cluster(ArrayList<Patient> patients){
        KMeansPlusPlusClusterer<Patient> patientClusterer = new KMeansPlusPlusClusterer<Patient>(5, 500);
        //Clusterer<Patient> patientClusterer = new FuzzyKMeansClusterer<Patient>(10, 5);
        //Clusterer<Patient> patientClusterer = new KMeansPlusPlusClusterer<Patient>(10, 5);
        //Clusterer<Patient> patientClusterer = new MultiKMeansPlusPlusClusterer<Patient>(10, 5);
        return patientClusterer.cluster(patients);
    }

    public static void main(String[] args) {
        Examples.createOutputFolder();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        // max time of the problem
        double maxDuration = 10000;

        int nuOfVehicles = 2;
        int capacity = 4;
        Coordinate firstDepotCoord = Coordinate.newInstance(0, 33);
        Coordinate second = Coordinate.newInstance(33, -33);

        // define depots
        int depotCounter = 1;
        for (Coordinate depotCoord : Arrays.asList(firstDepotCoord, second)) {
            for (int i = 0; i < nuOfVehicles; i++) {
                VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(depotCounter + "_type")
                        .addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
                String vehicleId = depotCounter + "_" + (i + 1) + "_vehicle";
                VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(vehicleId);
                vehicleBuilder.setStartLocation(Location.newInstance(depotCoord.getX(), depotCoord.getY()));
                vehicleBuilder.setType(vehicleType);
                vehicleBuilder.setLatestArrival(maxDuration);
                VehicleImpl vehicle = vehicleBuilder.build();
                vrpBuilder.addVehicle(vehicle);
            }
            depotCounter++;
        }

        // define problem with finite fleet
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        // define pickup locations and their time windows
        Random random = RandomNumberGeneration.newInstance();
        for (int i = 0; i < 40; i++) {
            Shipment shipment = Shipment.Builder.newInstance("" + (i + 1))
                    .addPickupTimeWindow(random.nextInt(50), 1000 + random.nextInt(50))
                    .addDeliveryTimeWindow(0, 1000)
                    .addSizeDimension(0, 1)
                    .addSizeDimension(0, 1)
                    .setPickupLocation(Location.newInstance(random.nextInt(50), random.nextInt(50)))
                    .setDeliveryLocation(random.nextInt(2) == 1 ?
                            Location.newInstance(firstDepotCoord.getX(), firstDepotCoord.getY()) :
                            Location.newInstance(second.getX(), second.getY())
                    )
                    .build();
            vrpBuilder.addJob(shipment);
        }

        // Pre-calculate distances
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);

        // build the problem
        VehicleRoutingProblem vrp = vrpBuilder.build();

        // solve the problem
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                .setProperty(Jsprit.Parameter.THREADS, "5")
                .buildAlgorithm();

        vra.setMaxIterations(2000);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        SolutionPrinter.print(bestSolution);

        // plot
        Plotter plotter = new Plotter(vrp, bestSolution);
        plotter.setLabel(Plotter.Label.SIZE);
        plotter.plot("output/solution.png", "solution");
    }

    // calculate the Manhattan distance between all points
    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                double distance = ManhattanDistanceCalculator.calculateDistance(fromCoord, toCoord);
                matrixBuilder.addTransportDistance(from, to, distance);
                matrixBuilder.addTransportTime(from, to, distance);
            }
        }
        return matrixBuilder.build();
    }

}