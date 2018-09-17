import java.util.*;

class HumanAI implements AI {
  Scanner sc;
  public HumanAI() {
    sc = new Scanner(System.in);
  }
  // Assume that the human player follows the rules.
  public int getMove(int n, int cM, int cR, int oR, int eR) {
    int maxTake = cR == 1 ? 3 : cM;
    System.out.printf("You have %d resets.\n", oR);
    System.out.printf("Choose [1, %d]: ", maxTake);
    int move = sc.nextInt();
    return move;
  }
}
