import java.util.*;

class RandomAI implements AI {
  public int getMove(int n, int cM, int cR, int oR, int eR) {
    Random rand = new Random();
    int reset = 1;
    if (oR > 0) {
      reset = rand.nextInt(2) == 1 ? 1 : -1;
    }
    int maxTake = Math.min(n, cR == 1 ? 3 : cM);
    int move = rand.nextInt(maxTake) + 1;
    return reset * move;
  }
}