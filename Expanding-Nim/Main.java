import java.util.*;

public class Main {

  // state[n][currMax][currResets][enemyResets]
  // - true, if the current player wins
  // - false, otherwise
  static int[][][][] state = new int[1001][51][5][5];

  static void playGame() {
    Random rand = new Random();
    // int remain = rand.nextInt(20)+1;
    int remain = 8;
    int oR = 4;
    int eR = 4;
    int currMax = 3;

    int turn = rand.nextInt(2);
    Scanner sc = new Scanner(System.in);

    boolean reset = false;
    while (remain != 0) {
      System.out.println("Remaining: " + remain);
      System.out.println("=================");

      if (turn == 0) {
        int best;
        if (!reset)
          best = state[remain][currMax][oR][eR];
        else
          best = state[remain][3][oR][eR];
        if (best < 0) {
          reset = true;
          oR--;
        } else {
          reset = false;
        }
        remain -= Math.abs(best);
        if (best == currMax) currMax++;
        System.out.println("Algo picks: " + Math.abs(best));
        System.out.println("=================");
      } else {
        int n;
        if (!reset) {
          System.out.printf(String.format("Choose [1, %d]: ", currMax));
          n = sc.nextInt();
          System.out.println();
          remain -= n;
        } else {
          System.out.println("Algo Reset");
          System.out.printf("Choose [1, 3]: ");
          n = sc.nextInt();
          System.out.println();
          remain -= n;
        }
        System.out.printf("Reset?: ");
        String willReset = sc.next();
        if (willReset.equals("r") && eR > 0) {
          reset = true;
          eR--;
        } else {
          reset = false;
        }
        if (n == currMax) currMax++;
      }

      if (remain == 0) {
        if (turn == 0)
          System.out.println("Algo win");
        else
          System.out.println("We win :)))))");
      }

      turn ^= 1;

    }
    sc.close();

  }

  public static void main(String[] args) {
    fillBaseCases();
    for(int i = 4; i < 1001; i++){
      fill(i);
    }

    playGame();
  }

  static void fillBaseCases() {
    // if n = 0, current player loses
    for (int cM = 3; cM <= 50; cM++) {
      for (int oR = 0; oR <= 4; oR++) {
        for (int tR = 0; tR <= 4; tR++) {
          state[0][cM][oR][tR] = 0;
        }
      }
    }

    // if n <= 3, current player wins
    for (int n = 1; n <= 3; n++) {
      for (int cM = 3; cM <= 50; cM++) {
        for (int oR = 0; oR <= 4; oR++) {
          for (int tR = 0; tR <= 4; tR++) {
            state[n][cM][oR][tR] = n;
          }
        }
      }
    }
  }

  static void fill(int n){
    for(int cM = 3; cM < 50; cM++){
      for(int oR = 0; oR < 5; oR++){
        for(int tR = 0; tR < 5; tR++){
          int win = 0;
          boolean reset = false;
          // current player doesn't use a reset
          for(int i = 1; i < cM; i++){
            if(n >= i && state[n-i][cM][tR][oR] == 0 && tR > 0 && state[n-i][cM][tR - 1][oR] == 0) {
              win = Math.max(i, win);
            }
          }
          // i == cM
          if(n >= cM && state[n-cM][cM+1][tR][oR] == 0 && tR > 0 && state[n-cM][cM + 1][tR - 1][oR] == 0) win = cM;
          // current player uses a reset
          if(oR > 0 && win == 0){
            for(int i = 1; i < cM; i++){
              if(n >= i && state[n-i][3][tR][oR-1] == 0 && tR > 0 && state[n-i][3][tR - 1][oR-1] == 0) {
                win = Math.max(i, win);
                reset = true;
              }
            }
            // i == cM
            if(n >= cM && state[n-cM][3][tR][oR-1] == 0 && tR > 0 && state[n-cM][3][tR - 1][oR - 1] == 0) {
              win = cM;
              reset = true;
            }
          }

          if (win == 0) {
            // TODO: What should we do if there are only losing states?
          }

          state[n][cM][oR][tR] = win*(reset?-1:1);
        }
      }
    }
  }

}
