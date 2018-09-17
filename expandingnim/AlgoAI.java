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