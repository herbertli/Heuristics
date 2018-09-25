#include <string>
#include <vector>
#include "SocketClient.hpp"
#include "json.hpp";

#define PORT 5000
#define BUFFER_SIZE 1024

using json = nlohmann::json;

std::string HOST = "127.0.0.1";

class TrenchClient
{
public:
  std::string name;
  bool isTrenchManager;
  SocketClient* sock;
  int d, y, r, m, L, p;
  TrenchClient(std::string);
  void play_game();
  std::vector<int> send_probes();
  std::string choose_alert(std::vector<int>, std::vector<bool>);
};

TrenchClient::TrenchClient(std::string name)
{
  this->name = name;
  this->isTrenchManager = false;
  this->sock = new SocketClient::SocketClient(HOST, PORT);

  json response = {
      {"name", this->name},
      {"is_trench_manager", this->isTrenchManager}};
  this->sock->send_json(response);
  json game_info = this->sock->receive_json(BUFFER_SIZE);
  printf("trench: %s", game_info.dump(2));
  this->d = game_info["d"];
  this->y = game_info["y"];
  this->r = game_info["r"];
  this->m = game_info["m"];
  this->L = game_info["L"];
  this->p = game_info["p"];
}

void TrenchClient::play_game()
{
  json response;
  while (true)
  {
    std::vector<int> probesToSend = this->send_probes();
    json probeResponse = {
      {"probes", probesToSend}
    };
    this->sock->send_json(probeResponse);
    json response = this->sock->receive_json(BUFFER_SIZE);
    std::string alert = this->choose_alert(probesToSend, response["probe_results"]);
    json alertResponse = {
      {"rejoin", alert}
    };
    this->sock->send_json(alertResponse);
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

std::vector<int> TrenchClient::send_probes() {
  std::vector<int> temp = std::vector<int>();
  return temp;
}

std::string TrenchClient::choose_alert(std::vector<int> sentProces, std::vector<bool> results) {
  return "red";
}
