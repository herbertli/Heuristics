#include <string>
#include "SocketClient.hpp"
#include "json.hpp";

#define PORT 5000
#define BUFFER_SIZE 1024

using json = nlohmann::json;

std::string HOST = "127.0.0.1";

class SubClient
{
public:
  std::string name;
  bool isTrenchManager;
  SocketClient* sock;
  int m, L, position;
  SubClient(std::string);
  int your_algorithm(int);
  void play_game();
};

SubClient::SubClient(std::string name)
{
  this->name = name;
  this->isTrenchManager = false;
  this->sock = new SocketClient::SocketClient(HOST, PORT);

  json response = {
      {"name", this->name},
      {"is_trench_manager", this->isTrenchManager}};
  this->sock->send_json(response);
  json game_info = this->sock->receive_json(BUFFER_SIZE);
  printf("sub: %s", game_info.dump(2));
  this->m = game_info["m"];
  this->L = game_info["L"];
  this->position = game_info["pos"];
}

void SubClient::play_game()
{
  json response;
  while (true)
  {
    int timesProbed;
    if (response == nullptr)
    {
      timesProbed = 0;
    }
    else
    {
      timesProbed = response["times_probed"];
    }
    int move = this->your_algorithm(timesProbed);
    json moveResponse = {
        {"move", move}};
    this->sock->send_json(moveResponse);
    this->position += move;
    response = this->sock->receive_json(BUFFER_SIZE);
    if (response.find("game_over") != response.end())
    {
      printf("The trench manager's final cost is: %d\n.", response["trench_cost"]);
      if (response["was_condition_achieved"])
      {
        printf("The safety condition was satisfied.\n");
      }
      else
      {
        printf("The safety condition was not satisfied.\n");
      }
      exit(0);
    }
  }
}

int SubClient::your_algorithm(int timesProbed)
{
  return 1;
}