#ifndef SOCKET_CLIENT_H
#define SOCKET_CLIENT_H

#include <bits/stdc++.h>
#include "../../json/json.hpp"
#include "socket_client.h"

using namespace std;

class NoTippingClient
{
  SocketClient *sc;
  vector<int> board_state;
  int num_weights;
  long weights;
  int board_length;
  int board_weight = 3;
  int MAX_DEPTH = 3;
  vector<pair<int, int>> dp_blocks;
  bool dp_processed = false;
  int dp[(1 << 20)];
  int dp_mask = 0;

public:
  NoTippingClient(string, int, bool);
  void play_game();
  pair<int, int> place(vector<int>);
  int remove(vector<int>);
  bool isGameOver(vector<int>);

  int alphabeta_search(vector<int>);
  bool terminal_test(vector<int>);
  int eval_fn(vector<int>);
  bool cutoff_test(vector<int>, int);
  int max_value(vector<int>, int, int, int);
  int min_value(vector<int>, int, int, int);
  int utility(vector<int>);
  vector<pair<int, vector<int>>> successors(vector<int>);
  void updateMask(vector<int>);
  void preprocess(vector<int>);
  int dp_search(vector<int>);
  int removeable(vector<int>, int);
  int removeBlock(vector<int>);
};

#endif