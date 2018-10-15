package ambulance.botty;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListener;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
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

class CompareByNumAmb implements Comparator<Hospital> {
    public int compare(Hospital a, Hospital b) {
        return Integer.compare(a.ambulancesAtStart.size(), b.ambulancesAtStart.size());
    }
}


class CompareByClusterSize implements Comparator<Cluster> {
    public int compare(Cluster a, Cluster b) {
        return Integer.compare(a.getPoints().size(), b.getPoints().size());
    }
}

public class TestLibrary {

    static Solution run(ArrayList<Patient> patients, ArrayList<Hospital> hospitals, ArrayList<Ambulance> ambulances) {
        List<CentroidCluster<Patient>> clusters = cluster(patients);
        Collections.sort(hospitals, new CompareByNumAmb());
        Collections.sort(clusters, new CompareByClusterSize());
        Examples.createOutputFolder();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        // max time of the problem
        double maxDuration = 10000;

        int capacity = 4;
        // define depots
        for (int i = 0; i < hospitals.size(); i++) {
            for (int j = 0; j < hospitals.get(i).ambulancesAtStart.size(); j++) {
                VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(hospitals.get(i).id + "_type")
                        .addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
                String vehicleId = hospitals.get(i).id + "_" + (hospitals.get(i).ambulancesAtStart.get(j)) + "_vehicle";
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

        for (CentroidCluster<Patient> cc : clusters) {
            for (Patient p : cc.getPoints()) {
                Shipment shipment = Shipment.Builder.newInstance("" + p.id)
                        .addPickupTimeWindow(0, p.deathTime)
                        .addDeliveryTimeWindow(0, p.deathTime - 1)
                        .addSizeDimension(0, 1)
                        .setPickupServiceTime(1)
                        .setDeliveryServiceTime(0)
                        .setPickupLocation(Location.newInstance(p.x, p.y))
                        .setDeliveryLocation(Location.newInstance(Math.round(cc.getCenter().getPoint()[0]), Math.round(cc.getCenter().getPoint()[1])))
                        .build();
                vrpBuilder.addJob(shipment);
            }
        }

        // Pre-calculate distances
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);

        // build the problem
        VehicleRoutingProblem vrp = vrpBuilder.build();

        // set time constraint
        TimeTermination prematureTermination = new TimeTermination(110 * 1000);

        // solve the problem
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                .setProperty(Jsprit.Parameter.THREADS, "5")
                .buildAlgorithm();
        vra.setMaxIterations(10000);
        vra.setPrematureAlgorithmTermination(prematureTermination);
        vra.addListener(prematureTermination);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        // print
        SolutionPrinter.print(vrp, bestSolution, SolutionPrinter.Print.VERBOSE);

        // plot
        Plotter plotter = new Plotter(vrp, bestSolution);
        plotter.setLabel(Plotter.Label.SIZE);
        plotter.plot("output/solution.png", "solution");

        return new Solution(hospitals, clusters, bestSolution.getRoutes());

    }

    static List<CentroidCluster<Patient>> cluster(ArrayList<Patient> patients) {
        //KMeansPlusPlusClusterer<Patient> patientClusterer = new KMeansPlusPlusClusterer<>(5, 500);
        //FuzzyKMeansClusterer<Patient> patientClusterer = new FuzzyKMeansClusterer<>(5, 5);
        MultiKMeansPlusPlusClusterer<Patient> patientClusterer = new MultiKMeansPlusPlusClusterer<Patient>(new KMeansPlusPlusClusterer<>(5, 50), 50);
        return patientClusterer.cluster(patients);
    }

    public static void main(String[] args) {
        Random rng = new Random();
        ArrayList<Patient> patients = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            patients.add(new Patient(i, rng.nextInt(1001), rng.nextInt(1001), rng.nextInt(500)));
        }
        ArrayList<Hospital> hospitals = new ArrayList<>();
        int amId = 0;
        for (int i = 0; i < 5; i++) {
            ArrayList<Integer> amIds = new ArrayList<>();
            for (int j = 0; j < rng.nextInt(25) + 1; j++) {
                amIds.add(amId++);
            }
            Hospital h = new Hospital(i);
            h.ambulancesAtStart = amIds;
            hospitals.add(h);
        }

        List<CentroidCluster<Patient>> clusters = cluster(patients);
        Collections.sort(hospitals, new CompareByNumAmb());
        Collections.sort(clusters, new CompareByClusterSize());
        Examples.createOutputFolder();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        // max time of the problem
        double maxDuration = 10000;

        int capacity = 4;
        // define depots
        for (int i = 0; i < hospitals.size(); i++) {
            for (int j = 0; j < hospitals.get(i).ambulancesAtStart.size(); j++) {
                VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance(hospitals.get(i).id + "_type")
                        .addCapacityDimension(0, capacity).setCostPerDistance(1.0).build();
                String vehicleId = hospitals.get(i).id + "_" + (hospitals.get(i).ambulancesAtStart.get(j)) + "_vehicle";
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

        for (CentroidCluster<Patient> cc : clusters) {
            for (Patient p : cc.getPoints()) {
                Shipment shipment = Shipment.Builder.newInstance("" + p.id)
                        .addPickupTimeWindow(0, p.deathTime)
                        .addDeliveryTimeWindow(0, p.deathTime - 1)
                        .addSizeDimension(0, 1)
                        .setPickupServiceTime(1)
                        .setDeliveryServiceTime(0)
                        .setPickupLocation(Location.newInstance(p.x, p.y))
                        .setDeliveryLocation(Location.newInstance(Math.round(cc.getCenter().getPoint()[0]), Math.round(cc.getCenter().getPoint()[1])))
                        .build();
                vrpBuilder.addJob(shipment);
            }
        }

        // Pre-calculate distances
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);

        // build the problem
        VehicleRoutingProblem vrp = vrpBuilder.build();

        // set time constraint
        TimeTermination prematureTermination = new TimeTermination(110 * 1000);

        // solve the problem
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                .setProperty(Jsprit.Parameter.THREADS, "5")
                .buildAlgorithm();
        vra.setMaxIterations(10000);
        vra.setPrematureAlgorithmTermination(prematureTermination);
        vra.addListener(prematureTermination);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        // print
        SolutionPrinter.print(vrp, bestSolution, SolutionPrinter.Print.VERBOSE);

        // plot
        Plotter plotter = new Plotter(vrp, bestSolution);
        plotter.setLabel(Plotter.Label.SIZE);
        plotter.plotShipments(false);
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