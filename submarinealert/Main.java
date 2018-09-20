import java.util.Random;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

  static final boolean randomize = true;
  static final boolean verbose = false;
  public static void main(String[] args) {

    int d = 50;
    int y = 10;
    int r = 20;
    int m = 150;
    int L = 11;
    int p = 20;
    int subPosition = 0;
    if(randomize){
      Random rand = new Random();
      d = rand.nextInt(100);
      y = rand.nextInt(10) + 1;
      r = rand.nextInt(y) + y + 1;
      m = rand.nextInt(100) + 100;
      L = rand.nextInt(11) + 1;
      p = rand.nextInt(20) + 1;
      subPosition = rand.nextInt(100);
    }

    ArrayList<TrenchManager> list = new ArrayList<>();
    list.add(new AldoTM(d, y, r, m, L, p));
    list.add(new UselessTrenchManager(d, y, r, m, L, p));
    for(TrenchManager tm : list){
      int cost = test(tm, d, y, r, m, L, p, subPosition);
      System.out.println("Cost: " + cost);
    }
    
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
      if (redAlert) {
        cost += r;
        if (verbose) System.out.println("TM goes on red alert");
      } else {
        cost += y;
        if (verbose) System.out.println("TM goes on yellow alert");
        if (redZone.contains(subPosition)) {
          if (verbose) System.out.println("Uh oh! Game over!");
          failed = true;
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
