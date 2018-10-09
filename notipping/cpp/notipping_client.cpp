#include "json.hpp"
#include <bits/stdc++.h>
#include "notipping_client.hpp"

using namespace std;

int BUFFER_SIZE = 4096;
long INF = 1e9;

NoTippingClient::NoTippingClient(string sockAddress, int sockPort, bool isFirst)
{
    sc = new SocketClient(sockAddress, sockPort);
    nlohmann::json j;
    j["name"] = "Botty McBotFace";
    j["is_first"] = isFirst;
    cout << "Sending message:" << j.dump(2) << "\n";
    this->sc->send_data(j.dump());
    nlohmann::json response = nlohmann::json::parse(this->sc->receive_data(BUFFER_SIZE, '\n'));
    cout << "Received message:" << response.dump(2) << "\n";
    this->board_length = response["board_length"];
    this->num_weights = atoi(response["num_weights"].get<string>().c_str());
    this->weights = (((0x1 << this->num_weights) - 1) << 1);
};

void NoTippingClient::play_game()
{
    nlohmann::json response;
    while (true)
    {
        string res = this->sc->receive_data(BUFFER_SIZE, '\n');
        cout << "Received message:\n"
             << res << "\n";

        response = nlohmann::json::parse(res);
        if (response.find("game_over") != response.end() && response["game_over"] == "1")
        {
            printf("Game Over!\n");
            return;
        }

        this->board_state.clear();
        std::istringstream iss(response["board_state"].get<string>());
        for (std::string s; iss >> s;)
            this->board_state.push_back(atoi(s.c_str()));

        if (response["move_type"] == "place")
        {
            printf("Placement Phase!\n");
            pair<int, int> p = this->place(this->board_state);
            this->weights ^= (0x1 << p.second);
            nlohmann::json j;
            j["position"] = p.first;
            j["weight"] = p.second;
            // printf("Weight: %d, Position: %d\n", p.second, p.first);
            this->sc->send_data(j.dump());
        }
        else
        {
            printf("Removal Phase!\n");
            int position = this->remove(this->board_state);
            nlohmann::json j;
            j["position"] = position;
            // printf("Position: %d\n", position);
            this->sc->send_data(j.dump());
        }
    }
}

pair<int, int> NoTippingClient::place(vector<int> current_board_state)
{
    int w = this->num_weights;
    int smallest = -1;
    int smallestInd = -100;
    while ((1 << w) > 0)
    {
        if (((1 << w) & this->weights) > 0)
        {
            smallest = w;
            for (int i = -1 * this->board_length; i <= this->board_length; i++)
            {
                if (current_board_state[i + 30] == 0)
                {
                    smallestInd = i;
                    current_board_state[i + 30] = w;
                    if (!this->isGameOver(current_board_state))
                    {
                        current_board_state[i + 30] = 0;
                        return make_pair(i, w);
                    }
                    current_board_state[i + 30] = 0;
                }
            }
        }
        w--;
    }
    return make_pair(smallestInd, smallest);
};

int NoTippingClient::remove(vector<int> current_board_state)
{
    int numPlaced = 0;
    for (auto i : current_board_state)
    {
        if (i > 0)
        {
            numPlaced++;
        }
    }
    if (numPlaced >= 20)
    {
        return this->alphabeta_search(current_board_state);
    }
    else
    {
        return this->dp_search(current_board_state);
    }
};

int NoTippingClient::alphabeta_search(vector<int> board)
{
    // printf("alphabeta\n");
    // printf("%d\n", board.size());
    vector<pair<int, vector<int>>> succs = this->successors(board);
    pair<int, vector<int>> best = succs[0];
    // printf("%d\n", succs.size());
    int best_score = min_value(succs[0].second, -INF, INF, 0);
    for (auto p : succs)
    {
        int x_score = min_value(p.second, -INF, INF, 0);
        if (x_score > best_score)
        {
            best = p;
            best_score = x_score;
        }
    }

    return best.first;
}

int NoTippingClient::utility(vector<int> board)
{
    if (this->isGameOver(board))
    {
        return -1;
    }
    else
    {
        return 1;
    }
}

bool NoTippingClient::terminal_test(vector<int> board)
{
    return this->isGameOver(board);
}

int NoTippingClient::eval_fn(vector<int> board)
{
    return this->utility(board);
}

bool NoTippingClient::cutoff_test(vector<int> board, int depth)
{
    return depth > this->MAX_DEPTH || this->terminal_test(board);
}

int NoTippingClient::max_value(vector<int> board, int alpha, int beta, int depth)
{
    // cout << "maxvalue" << endl;
    if (cutoff_test(board, depth))
    {
        return eval_fn(board);
    }
    int v = -INF;
    for (auto p : this->successors(board))
    {
        v = max(v, min_value(p.second, alpha, beta, depth + 1));
        if (v >= beta)
        {
            return v;
        }
        alpha = max(alpha, v);
    }
    return v;
}

vector<pair<int, vector<int>>> NoTippingClient::successors(vector<int> board)
{
    // cout << "succ" << endl;
    vector<pair<int, vector<int>>> succs;
    for (int i = -1 * this->board_length; i <= this->board_length; i++)
    {
        if (board[i + 30] > 0)
        {
            vector<int> copied(board);
            copied[i + 30] = 0;
            if (!this->isGameOver(copied))
            {
                succs.push_back(make_pair(i, copied));
                //cout << succs.size() << endl;
            }
        }
    }
    return succs;
}

int NoTippingClient::min_value(vector<int> board, int alpha, int beta, int depth)
{
    // cout << "minvalue" << endl;
    if (cutoff_test(board, depth))
    {
        return eval_fn(board);
    }
    int v = INF;
    for (auto p : this->successors(board))
    {
        v = min(v, max_value(p.second, alpha, beta, depth + 1));
        if (v <= alpha)
        {
            return v;
        }
        beta = min(beta, v);
    }
    return v;
}

void NoTippingClient::updateMask(vector<int> board)
{
    cout << "updatemask" << endl;
    for (int i = 0; i < dp_blocks.size(); i++)
    {
        pair<int, int> p = this->dp_blocks[i];
        int index = p.first;
        int weight = p.second;
        if (board[index + 30] == weight)
        {
            this->dp_mask |= (1 << i);
        }
        else
        {
            this->dp_mask &= ~(1 << i);
        }
    }
    cout << bitset<20>(this->dp_mask).to_string() << endl;
}

void NoTippingClient::preprocess(vector<int> board)
{
    // sets up blocks
    for (int i = -1 * this->board_length; i <= this->board_length; i++)
    {
        if (board[i + 30] > 0)
        {
            this->dp_blocks.emplace_back(i, board[i + 30]);
        }
    }
    cout << this->dp_blocks.size() << endl;
    //do dp
    for (int m = 0; m < (1 << this->dp_blocks.size()); m++)
    {
        int leftTorque = 0;
        int rightTorque = 0;
        int i = 0;
        while ((1 << i) <= m)
        {
            if (((1 << i) & m) > 0)
            {
                leftTorque += (this->dp_blocks[i].first +
                               3) *
                              this->dp_blocks[i].second;
                rightTorque += (this->dp_blocks[i].first + 1) * this->dp_blocks[i].second;
            }
            i++;
        }
        //BOARDWEIGHT can be seen as a 3kg block at pos 0, so add that "block" to torques
        leftTorque += 3 * this->board_weight;
        rightTorque += this->board_weight;

        //current state is an inherently unstable state that will tip without any extra
        //blocks, so dp[state] = 100
        if (leftTorque < 0 || rightTorque > 0)
        {
            dp[m] = 100;
        }
        else
        {
            //check to see if next state is a definite loss for opponent
            //if it is, then I should remove blocksRemIndex[i]
            bool allIsLost = true;
            for (int i = 0; i < this->dp_blocks.size(); i++)
            {
                if (((1 << i) & m) > 0 && (dp[m ^ (1 << i)] == -100))
                {
                    this->dp[m] = this->dp_blocks[i].first;
                    allIsLost = false;
                    break;
                }
            }
            //if I haven't computed the next state, this means I couldn't
            //find a removal that results in my opponent's loss
            //which means my current state is a losing one
            if (allIsLost)
            {
                dp[m] = -100;
            }
        }
    }
}

// runs when blocks remaining <= 20
int NoTippingClient::dp_search(vector<int> board)
{
    // printf("dp\n");
    if (!this->dp_processed)
    {
        preprocess(board);
        this->dp_processed = true;
    }
    updateMask(board);
    if (this->dp[this->dp_mask] == -100)
    {
        // printf("I think I lost. :(");
        // do a move that doesn't lose immediately
        for (int i = -1 * this->board_length; i < this->board_length + 1; i++)
        {
            if (board[i + 30] > 0)
            {
                int temp = board[i + 30];
                board[i + 30] = 0;
                if (!isGameOver(board))
                {
                    return i;
                }
                board[i + 30] = temp;
            }
        }
        // every move is a losing move
        return -100;
    }
    else
    {
        // do what dp says
        return this->dp[this->dp_mask];
    }
    // error
    return 99;
}

int NoTippingClient::removeable(vector<int> board, int turns_left)
{
    if (turns_left < 0)
    {
        return 0;
    }
    for (int i = -1 * this->board_length; i <= this->board_length; i++)
    {
        if (board[i + 30] > 0)
        {
            int temp = board[i + 30];
            board[i + 30] = 0;
            if (!this->isGameOver(board))
            {
                int loc = this->removeable(board, turns_left - 1);
                if (loc != 100)
                {
                    board[i + 30] = temp;
                    return i;
                }
            }
            board[i + 30] = temp;
        }
    }
    return 100;
}

int NoTippingClient::removeBlock(vector<int> board)
{
    int lookahead = 5;
    while (lookahead >= 0)
    {
        int loc = this->removeable(board, lookahead);
        if (loc != 100)
        {
            return loc;
        }
        lookahead -= 1;
    }
    for (int i = -1 * this->board_length; i <= this->board_length; i++)
    {
        if (board[i + 30] > 0)
            return i;
    }
    return 100;
}

bool NoTippingClient::isGameOver(vector<int> board)
{
    int leftTorque = 0;
    int rightTorque = 0;
    for (int i = -this->board_length; i <= this->board_length; i++)
    {
        if (board[i + 30] > 0)
        {
            leftTorque += (i + 3) * board[i + 30];
            rightTorque += (i + 1) * board[i + 30];
        }
    }
    leftTorque += 3 * this->board_weight;
    rightTorque += this->board_weight;
    return leftTorque < 0 || rightTorque > 0;
};
