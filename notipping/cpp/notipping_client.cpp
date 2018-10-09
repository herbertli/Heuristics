#include "json.hpp"
#include <bits/stdc++.h>
#include "notipping_client.hpp"

using namespace std;

string HOST = "localhost";
int PORT = 3000;
int BUFFER_SIZE = 2048;

NoTippingClient::NoTippingClient(bool isFirst)
{
    sc = new SocketClient(HOST, PORT);
    nlohmann::json j;
    j["name"] = this->name;
    j["is_first"] = isFirst;
    this->sc->send_json(j);
    nlohmann::json response = this->sc->receive_json(BUFFER_SIZE);
    this->board_length = response['board_length'];
    this->num_weights = response['num_weights'];
}

void NoTippingClient::play_game()
{
    nlohmann::json response;
    while (true)
    {
        response = this->sc->receive_json(BUFFER_SIZE);
        if (response.find("game_over") != response.end() && response["game_over"] == "1")
        {
            printf("Game Over!\n");
            return;
        }

        istringstream iss(response["board_state"]);
        vector<std::string> results((istream_iterator<string>(iss)), istream_iterator<string>());
        this->board_state.clear();
        for (auto c : results)
            this->board_state.push_back(stoi(c));
        if (response['move_type'] == 'place')
        {
            pair<int, int> p = this->place(this->board_state);
            nlohmann::json j;
            j["position"] = p.first;
            j["weight"] = p.second;
            this->sc->send_json(j);
        }
        else
        {
            int position = this->remove(this->board_state);
            nlohmann::json j;
            j["position"] = position;
            this->sc->send_json(j);
        }
    }
}

pair<int, int> NoTippingClient::place(vector<int> current_board_state)
{
    return make_pair(1, 1);
}

int NoTippingClient::remove(vector<int> current_board_state)
{
    return 1;
}
