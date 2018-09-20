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
  boolean subFound = false;

  int leftProbe;
  int rightProbe;

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

  int[] sendBoundaryScan() {
    return new int[]{this.leftProbe, this.rightProbe};
  }

  // send enough probes to cover the red zone
  // as well as probes on the extreme left and right
  int[] scannedLocations;
  int[] sendInitialScan() {
    ArrayList<Integer> probeLocations = new ArrayList();
    probeLocations.add((this.redZoneStart + 2) % 100);

    int left = (this.redZoneStart + 2 - this.scanRange + 100) % 100;
    while (left > this.redZoneStart) {
      probeLocations.add((left - this.scanRange - 1 + 100) % 100);
      left = left - 2 * this.scanRange - 1;
    }
    probeLocations.add((left - this.scanRange - 1 + 100) % 100);
    this.leftProbe = (left - this.scanRange - 1 + 100) % 100;
    
    int right = (this.redZoneStart + 2 + this.scanRange) % 100;
    while (this.redZone.contains(right)) {
      probeLocations.add((right + this.scanRange + 1) % 100);
      right = right + 2 * this.scanRange + 1;
    }
    this.rightProbe = (right + this.scanRange + 1) % 100;
    probeLocations.add((right + this.scanRange + 1) % 100);

    this.scannedLocations = new int[probeLocations.size()];
    for (int i = 0; i < probeLocations.size(); i++) {
      this.scannedLocations[i] = probeLocations.get(i);
    }
    return this.scannedLocations;
  }

  int[] sendScan() {
    if (time % (2 * this.scanRange + 1) == 0) return new int[]{this.leftProbe, this.rightProbe};
    else return new int[0];
  }

  public void receiveProbeResults(boolean[] results) {
    if (results.length == 0) return;
    
    int subLoc = -1;
    for (int i = 0; i < results.length; i++) {
      if (time == 0) {
        if (results[i]) {
          subLoc = this.scannedLocations[i];
          this.subFound = true;
          break;
        }
      }
      else {
        if (results[i]) {
          if (i == 0)
            subLoc = this.leftProbe;
          else
            subLoc = this.rightProbe;
          this.subFound = true;
          break;
        }
      }
    }

    // now let's get the intervals for the next scan
    // assume you knew that the sub is in interval M at time t - probeRange
    // at time t, you deploy probes at L and R...
    // three cases:
    // |----- LL -----||----- L -----||----- M -----||----- R -----||----- RR -----|
    // 1. L returns true: deploy probes at LL and M at time t + probeRange 
    // 2. R returns true: deploy probes at M and RR at time t + probeRange 
    // 3. L and R return false: deploy probes at L and R at time t + probeRange
    // if L, M, R overlap with redzone, go to red alert over the next probeRange time
    
    // System.out.printf("Sub loc: %d\n", subLoc);
    // sub has moved to L or R, unscanned interval
    if (subLoc != -1) {
      if (subLoc == this.leftProbe) {
        this.rightProbe = (this.leftProbe + this.scanRange * 2 + 1) % 100;
        this.leftProbe = (this.leftProbe - this.scanRange * 2 - 1 + 100) % 100;
      } else if (subLoc == this.rightProbe) {
        this.leftProbe = (this.rightProbe - this.scanRange * 2 - 1 + 100) % 100;
        this.rightProbe = (this.rightProbe + this.scanRange * 2 + 1) % 100;
      }
    }

    TreeSet<Integer> scanZone = new TreeSet<>();
    // System.out.println(this.leftProbe + " " + this.rightProbe);
    for (int i = this.leftProbe - this.scanRange; i != (this.rightProbe + this.scanRange + 1) % 100; i=(i+1)%100) {
      scanZone.add((i + 100) % 100);
    }

    // System.out.print("Scan Zone:");
    // for (int j: scanZone) {
    //   System.out.print(j + " ");
    // }
    // System.out.println();

    // SPECIAL CASE: scanZone is too far from redZone to matter
    boolean tooFar = true;
    int d = this.redZoneStart;
    for (int i: scanZone) {
      if (Math.abs(i - d) <= this.gameTime - this.time) {
        tooFar = false;
        break;
      } else if (Math.abs(i - ((d + 5) % 100)) <= this.gameTime - this.time) {
        tooFar = false;
        break;
      }
    }
      
    if (tooFar) {
      this.redAlert = false;
      return;
    }

    // using scan zone, check for overlap with red zone
    // if so, go red for the next scanRange seconds, otherwise go yellow
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
    if (this.time == 0) {
      return sendInitialScan();
    } else if (!this.subFound && (this.time % (2 * this.scanRange + 1) == 0)) {
      return sendBoundaryScan();
    } else if (this.subFound && (this.time % (2 * this.scanRange + 1) == 0)) {
      return sendScan();
    } else {
      return new int[0];
    }
  }
  
}