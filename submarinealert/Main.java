import java.util.Random;

public class Main {

  public static void main(String[] args) {

    Random rand = new Random();
    int d = rand.nextInt(100);
    int y = rand.nextInt(10) + 1;
    int r = rand.nextInt(y, y * 2) + 1;
    int m = rand.nextInt(100) + 100;
    int L = rand.nextInt(11) + 1;
    int p = rand.nextInt(20) + 1;

    TreeSet<Integer> redZone = new TreeSet();
    for (int i = d; i < d + 6; i++) {
      redZone.add(i % 100);
    }

    int subPosition = rand.nextInt(100);
    Submarine sub = new RandomSub(subPosition);

    TrenchManager tm = new DPTrench();
    int cost = 0;

    boolean failed = false;

    for (int i = 0; i < m; i++) {
      System.out.printf("Time: %d\n", i);
      System.out.printf("Submarine Position: %d\n", subPosition);

      int[] probes = tm.getProbes();
      System.out.printf("TM probes: %s\n", Arrays.toString(probes));
      // calculate which probes are "yes"
      boolean[] yes;
      System.out.printf("Probe result: %s\n", Arrays.toString(yes));
      tm.receiveProbeResult(yes);

      boolean redAlert = tm.shouldGoRed();
      if (redAlert) {
        cost += r;
        System.out.println("TM goes on red alert");
      } else {
        cost += y;
        System.out.println("TM goes on yellow alert");
        if (redZone.contains(subPosition)) {
          System.out.println("Uh oh! Game over!");
          failed = true;
        }
      }

      boolean probed = false;
      // send to sub if it has been probed

      sub.hasBeenProbed(probed)
      subPosition += sub.getMove();
    }

    if (!failed) {
      System.out.printf("Cost: %d\n", cost)
    } else {
      System.out.printf("Cost: %d\n", 5 * m * p + r * m);
    }
    
  }

}
