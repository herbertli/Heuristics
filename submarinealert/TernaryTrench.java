import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Arrays;

public class TernaryTrench implements TrenchManager {

  int redZoneStart;     // d
  int redAlertCost;     // r
  int yellowAlertCost;  // y
  int gameTime;         // m
  int scanRange;        // L
  int probeCost;        // p
  TreeSet<Integer> redZone;
  boolean redAlert = false;
  int time;
  int[] scannedLocations;
  boolean verbose = false;

  TernaryTrench(int d, int y, int r, int m, int L, int p) {
    this.time = 0;
    this.redZoneStart = d;
    this.yellowAlertCost = y;
    this.redAlertCost = r;
    this.gameTime = m;
    this.scanRange = L;
    this.probeCost = p;
    this.redZone = new TreeSet<>();
    for (int i = d; i < d + 6; i++) {
      this.redZone.add(i % 100);
    }
  }

  // send enough probes to cover the entire trench
  int[] sendInitialScan() {
    assert this.time == 0 : "Not the time for an initial scan";
    int probesNeeded = (100 - 1) / (2 * this.scanRange + 1) + 1;
    ArrayList<Integer> probeLocations = new ArrayList();
    int curr = this.scanRange;
    for (int i = 0; i < probesNeeded; i++) {
      probeLocations.add(curr % 100);
      curr += 2 * this.scanRange + 1;
    }
    this.scannedLocations = new int[probeLocations.size()];
    for (int i = 0; i < probeLocations.size(); i++) {
      this.scannedLocations[i] = probeLocations.get(i);
    }
    return scannedLocations;
  }

  int[] sendScan() {
    if (time % this.scanRange == 0) return this.scannedLocations;
    else return new int[0];
  }

  public void receiveProbeResults(boolean[] results) {
    if (time % this.scanRange != 0) return;

    int subLoc = -1;

    // handle case where initial scan produces overlapping interval
    // in this case, the interval is the intersection....  
    if (time == 0 && results[0] && results[results.length - 1]) {
      subLoc = ((this.scannedLocations[0] + this.scannedLocations[scannedLocations.length - 1]) / 2) % 100;
    } else {
      for (int i = 0; i < results.length; i++) {
        if (results[i]) {
          subLoc = this.scannedLocations[i];
          break;
        }
      }
    }

    if (verbose)
      System.out.printf("Sub interval: %d\n", subLoc);

    // now let's get the intervals for the next scan
    // assume you knew that the sub is in interval M at time t - probeRange
    // at time t, you deploy probes at L and R...
    // three cases:
    // |----- LL -----||----- L -----||----- M -----||----- R -----||----- RR -----|
    // 1. L returns true: deploy probes at LL and M at time t + probeRange 
    // 2. R returns true: deploy probes at M and RR at time t + probeRange 
    // 3. L and R return false: deploy probes at L and R at time t + probeRange
    // if L, M, R overlap with redzone, go to red alert over the next probeRange time
    TreeSet<Integer> scanZone = new TreeSet<>();
    ArrayList<Integer> scanIntervals = new ArrayList();

    // sub has moved to L
    int i;
    if (subLoc != -1) {
      int interval1 = (subLoc + 100 - 3 * this.scanRange) % 100;
      for (i = interval1 - scanRange; i < interval1 + scanRange + 1; i++) {
        scanZone.add((i + 100) % 100);
      }
      scanIntervals.add(interval1);
    } 
    // sub has stayed in M
    else {
      int interval1 = this.scannedLocations[0];
      for (i = interval1 - scanRange; i < interval1 + scanRange + 1; i++) {
        scanZone.add((i + 100) % 100);
      }
      scanIntervals.add(interval1);
    }

    // fill in middle interval
    int end = i;
    for (; i < end + scanRange + 1; i++) {
      scanZone.add((i + 100) % 100);
    }
    
    // sub has moved to R
    if (subLoc != -1) {
      int interval2 = (subLoc + 100 + 3 * this.scanRange) % 100;
      for (int j = interval2 - scanRange; j < interval2 + scanRange + 1; j++) {
        scanZone.add((j + 100) % 100);
      }
      scanIntervals.add(interval2);
    }
    // sub has stayed in M
    else {
      int interval2 = this.scannedLocations[1];
      for (int j = interval2 - scanRange; j < interval2 + scanRange + 1; j++) {
        scanZone.add((j + 100) % 100);
      }
      scanIntervals.add(interval2);
    }
  
    this.scannedLocations = new int[scanIntervals.size()];
    for (int j = 0; j < scanIntervals.size(); j++) {
      this.scannedLocations[j] = scanIntervals.get(j);
    }
    if (verbose)
      System.out.printf("New scan locations: %s\n", Arrays.toString(this.scannedLocations));

    // SPECIAL CASE: scanZone is too far from redZone to matter
    int minScan = scanZone.first();
    int maxScan = scanZone.last();
    if (
      Math.abs(minScan - this.redZoneStart) > this.gameTime - this.time &&
      Math.abs(maxScan - this.redZoneStart + 5) > this.gameTime - this.time
    ) {
      this.redAlert = false;
      return;
    }

    // using scan interval, and check for overlap with red zone
    // if so, go red for the next scanRange seconds, otherwise go yellow
    if (verbose) {
      System.out.print("Scan interval: ");
      for (int j : scanZone) {
        System.out.print(j + " ");
      }
      System.out.println();
    }

    this.redAlert = false;
    for (int j: this.redZone) {
      if (scanZone.contains(j)) {
        this.redAlert = true;
        return;
      }
    }    
  }

  public boolean shouldGoRed() {
    this.time++;
    return this.redAlert;
  }

  public int[] getProbes() {
    if (this.time % probeCost == 0) {
      if (this.time == 0) {
        return sendInitialScan();
      } else {
        return sendScan();
      }
    } else {
      return new int[0];
    }
  }
  
}