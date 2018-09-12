import java.util.*;

interface AI {
  int getMove(int n, int cM, int cR, int oR, int eR);
}

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

class AlgoAI implements AI {
  // state[n][currMax][currentlyReset][currResets][enemyResets]
  // - int moves, where abs(moves) is the number of stones you should take
  // - if moves < 0, current player should also reset.
  int[][][][][] state = new int[1001][51][2][5][5];

  public AlgoAI(){
    fillBaseCases();
    for(int i = 4; i < 1001; i++){
      fill(i);
    }
  }

  public int getMove(int n, int cM, int cR, int oR, int eR) {
    /*
    // System.out.printf("%d %d %d %d\n", n, cM, oR, eR);
    if (state[n][cM][cR][oR][eR] == 0) {
      //System.out.println("Algo thinks it lost :(");
      return 1;
    }
    */
    return state[n][cM][cR][oR][eR];
  }

  private void fillBaseCases() {
    // if n = 0, current player loses
    for (int cM = 3; cM <= 50; cM++) {
      for(int cR = 0; cR <= 1; cR++){
        for (int oR = 0; oR <= 4; oR++) {
          for (int tR = 0; tR <= 4; tR++) {
            state[0][cM][cR][oR][tR] = 0;
          }
        }
      }
    }

    // if n <= 3, current player wins
    for (int n = 1; n <= 3; n++) {
      for (int cM = 3; cM <= 50; cM++) {
        for(int cR = 0; cR <= 1; cR++){
          for (int oR = 0; oR <= 4; oR++) {
            for (int tR = 0; tR <= 4; tR++) {
              state[n][cM][cR][oR][tR] = n;
            }
          }
        }
      }
    }
  }

  private void fill(int n){
    for(int cM = 3; cM <= 49; cM++){
      for(int cR = 0; cR <= 1; cR++){
        for(int oR = 0; oR <= 4; oR++){
          for(int tR = 0; tR <= 4; tR++){
            int win = 0; // number of stones to take that make the current state a winning one.
            int maxTake = (cR == 1) ? 3 : cM;
            if(maxTake > n) maxTake = n;
            boolean reset = false;
            // current player doesn't use a reset
            for(int i = 1; i <= maxTake; i++){
              if(state[n-i][cM+(i == cM ? 1 : 0)][0][tR][oR] == 0) {
                win = Math.max(i, win);
              }
            }
            // current player uses a reset
            // if we can win without using a reset then we should so that we save resets.
            if(oR > 0 && win == 0){
              for(int i = 1; i <= maxTake; i++){
                if(state[n-i][cM+(i == cM ? 1 : 0)][1][tR][oR-1] == 0) {
                  win = Math.max(i, win);
                  reset = true;
                }
              }
            }
            state[n][cM][cR][oR][tR] = win*(reset?-1:1);
          }
        }
      }
    }
  }

  public void printStartingMove(){
    double wins = 0;
    for(int i = 0; i < 1001; i++){
      int move = state[i][3][0][4][4];
      System.out.println(i + ": " + move);
      if(move != 0) wins++;
    }
    System.out.println(wins/1000);
  }
}

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
        // TODO: Does picking 3 after a reset cause current move to increase.
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
    AI bi = new AlgoAI();
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
