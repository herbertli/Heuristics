#ifndef SOCKET_CLIENT_H
#define SOCKET_CLIENT_H

#include <bits/stdc++.h>
#include "json.hpp"
#include "socket_client.h"

using namespace std;

class NoTippingClient
{
  string sockAddress;
  int sockPort;
  SocketClient *sc;
  vector<int> board_state;
  int num_weights;
  int board_length;
  string name = "Botty McBotFace";

public:
  NoTippingClient(bool);
  void play_game();
  pair<int, int> place(vector<int>);
  int remove(vector<int>);
};

#endif