import java.util.*;

public class Main {

  static boolean verbose = false;
  static int algoWins = 0;
  static int aiWins = 0;
  static int correctGuesses = 0;

  static void playGame(AI ai, AI bi) {
    Random rand = new Random();
    int remain = rand.nextInt(1000)+1;
    int turn = rand.nextInt(2);

    int oR = 4;
    int eR = 4;
    int currMax = 3;

    int reset = 0;

    boolean winnable = (bi.getMove(remain, 3, 0, 4, 4) != 0);
    if(turn != 0) winnable = !winnable;

    while (remain != 0) {
      if (verbose) {
        System.out.println("Remaining: " + remain);
        System.out.println("=================");
      }

      if (turn == 0) {
        int best;
        best = bi.getMove(remain, currMax, reset, oR, eR);
        if(best == 0) best = 1;
        //assert best != 0;
        if (best < 0) {
          reset = 1;
          if(verbose) System.out.println("Algo resets on " + remain + "!");
          oR--;
        } else {
          reset = 0;
        }
        remain -= Math.abs(best);
        // TODO: Does picking 3 after a reset cause current max to increase.
        // YES, if current max is 3, do we handle this case? Or does our code have to change?
        if (best == currMax) {
          currMax++;
        }
        if (verbose) {
          System.out.println("Algo picks: " + Math.abs(best));
          System.out.println("=================");
        }
      } else {
        int n;
        n = ai.getMove(remain, currMax, reset, eR, oR);
        if(n == 0) n = 1;
        assert n != 0;
        if (n < 0) {
          reset = 1;
          eR--;
        } else {
          reset = 0;
        }
        remain -= Math.abs(n);
        // TODO: Does picking 3 after a reset cause current move to increase.
        // YES, if current max is 3, do wehandle this case? Or does our code have to change?
        if (Math.abs(n) == currMax) currMax++;
        if (verbose) {
          System.out.println("Not Algo picks: " + Math.abs(n));
          System.out.println("=================");
        }
      }

      if (remain == 0) {
        if (turn == 0) {
          if (verbose) System.out.println("Algo win");
          if(winnable) correctGuesses++;
          algoWins++;
        } else {
          if (verbose) System.out.println("We win :)))))");
          if(!winnable) correctGuesses++;
          aiWins++;
        }
      }

      turn ^= 1;

    }

  }

  public static void main(String[] args) {
    // Initialize AI
    AI ai = new AlgoAI();
    RandomAI bi = new RandomAI();
    int numRuns = 1000000;
    for (int i = 0; i < numRuns; i++) {
      if(i % 1000 == 0)System.out.println(i);
      playGame(ai, bi);
    }
    //playGame(ai, bi);
    System.out.printf("Correct prediction from starting game state: %.9f\n", (correctGuesses+0.0)/numRuns);
    System.out.printf("Algo wins: %d, AI wins: %d\n", algoWins, aiWins);
    System.out.printf("Algo win pct: %.2f\n", (algoWins + 0.0) / (algoWins + aiWins) * 100);
  }
}
