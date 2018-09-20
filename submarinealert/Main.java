 import java.util.Random;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {

  static final boolean randomize = true;
  static final boolean verbose = false;
  static int fails = 0;
  public static void main(String[] args) {

    // d: 13, y: 10, r: 13, m: 197, L: 8, p: 8, subPosition: 30
    int d = 13;
    int y = 10;
    int r = 13;
    int m = 197;
    int L = 8;
    int p = 8;
    int subPosition = 30;

    ArrayList<TrenchManager> list;
    int[] wins = new int[3];
    for (int i = 0; i < 100000; i++) {

      if (randomize) {
        Random rand = new Random();
        d = rand.nextInt(100);
        y = rand.nextInt(10) + 1;
        r = rand.nextInt(y) + y + 1;
        m = rand.nextInt(100) + 100;
        L = rand.nextInt(11) + 1;
        p = rand.nextInt(20) + 1;
        subPosition = rand.nextInt(100);
      }

      list = new ArrayList<>();
      list.add(new AldoTM(d, y, r, m, L, p));
      // list.add(new UselessTrenchManager(d, y, r, m, L, p));
      list.add(new TernaryTrench(d, y, r, m, L, p));

      if (i % 10000 == 0)
        System.out.printf("Run: %d\n", i);
      
      Integer[] costs = new Integer[list.size()];
      int pt = 0;
      for(TrenchManager tm : list){
        int cost = test(tm, d, y, r, m, L, p, subPosition);
        costs[pt] = cost; 
        pt++;
        if (verbose)
          System.out.println("Cost: " + cost);
      }
      Integer minCost = Collections.min(Arrays.asList(costs));
      for (int j = 0; j < costs.length; j++) {
        if (costs[j] == minCost) wins[j]++;
      }
    }
    System.out.printf("Failures: %d\n", fails);
    System.out.printf("Wins: %s\n", Arrays.toString(wins));
    
  }

  static int test(TrenchManager tm, int d, int y, int r, int m, int L, int p, int subPosition){
    TreeSet<Integer> redZone = new TreeSet<>();
    for (int i = d; i < d + 6; i++) {
      redZone.add(i % 100);
    }
    if (verbose) {
      System.out.printf("d: %d, y: %d, r: %d, m: %d, L: %d, p: %d, subPosition: %d\n", d, y, r, m, L, p, subPosition);
    }
    Submarine sub = new RandomSub(subPosition);

    int cost = 0;

    boolean failed = false;

    for (int i = 0; i < m; i++) {
      if (verbose) {
        System.out.printf("Time: %d\n", i);
        System.out.printf("Submarine Position: %d\n", subPosition);
      }

      int[] probes = tm.getProbes();
      cost += probes.length * p;
      if (verbose) System.out.printf("TM probes: %s\n", Arrays.toString(probes));
      // calculate which probes are "yes"
      boolean[] yes = new boolean[probes.length];
      for (int j = 0; j < probes.length; j++) {
        int probe = probes[j];
        int lb = (probe + 100 - L) % 100;
        int ub = (probe + 100 + L) % 100;
        if(ub < lb) ub += 100;
        int tempSubPosition = subPosition;
        while(tempSubPosition < lb) tempSubPosition+=100;
        yes[j] = (tempSubPosition <= ub);
      }
      if (verbose) System.out.printf("Probe result: %s\n", Arrays.toString(yes));
      tm.receiveProbeResults(yes);

      boolean redAlert = tm.shouldGoRed();
      // System.out.println("Sub pos: " + subPosition);
      if (redAlert) {
        cost += r;
        if (verbose) System.out.println("TM goes on red alert");
      } else {
        cost += y;
        if (verbose) System.out.println("TM goes on yellow alert");
        if (redZone.contains(subPosition)) {
          // if (verbose) {
            // System.out.printf("Time: %d\n", i);
            // System.out.printf("d: %d, y: %d, r: %d, m: %d, L: %d, p: %d, subPosition: %d\n", d, y, r, m, L, p, subPosition);
            // System.out.println("Uh oh! Game over!");
            // System.exit(1);
          // }
          failed = true;
          fails++;
          break;
        }
      }

      boolean probed = false;
      // send to sub if it has been probed

      sub.hasBeenProbed(probed);
      subPosition = (subPosition + sub.getMove() + 100) % 100;
    }

    if (!failed) {
      return cost;
    } else {
      return 5 * m * p + r * m;
    }
    
  }
}
