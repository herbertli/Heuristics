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

    int subPosition = rand.nextInt(100);
    Submarine sub = new RandomSub(subPosition);

    TrenchManager tm = new DPTrench();
    int cost = 0;

    for (int i = 0; i < m; i++) {
      System.out.printf("Time: %d\n", i);
      System.out.printf("Submarine Position: %d\n", subPosition);
      

    }
    
  }

}
