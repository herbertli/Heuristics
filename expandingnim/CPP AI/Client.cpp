// Client side C/C++ program to demonstrate Socket programming
#include <stdio.h>
#include <sys/socket.h>
#include <unistd.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string>
#include "AlgoAI.cpp"
#include "json.hpp"
#define PORT 9000

const std::string bot_name = "Botty McBotFace";

struct player_info {
    double time_taken;
    std::string name;
    int resets_left;
};

int order;          // 0 = first, 1 = second
AlgoAI my_ai;
int stones_left; 
int current_max;    // Note: most we can currently take
                    // Not most taken so far.
int stones_removed; 
bool finished;
player_info player_0;
player_info player_1;
bool reset_used;
int init_max;
int sock, valread;
char buffer[1024] = {0};

void send_move(){
    // std::cout << stones_left << " " << current_max << " " << reset_used << " " << player_0.resets_left << " " << player_1.resets_left << std::endl;
    int num_stones_to_take = my_ai.getMove(stones_left, current_max, (reset_used ? 1 : 0), player_0.resets_left, player_1.resets_left);
    bool reset = false;
    if(num_stones_to_take == 0) num_stones_to_take++;   // if in losing state, just take 1 stone.
    if(num_stones_to_take < 0){                         // handle reset
        num_stones_to_take *= -1;
        reset = true;
    }
    // std::cout << num_stones_to_take << std::endl;
    nlohmann::json move = {
        {"order", order},
        {"num_stones", num_stones_to_take},
        {"reset", reset}
    };
    std::cout << "I take " << num_stones_to_take << " " << std::endl;
    const char * move_c_str = move.dump().c_str();
    //std::cout << move_c_str << std::endl;
    send(sock, move_c_str, strlen(move_c_str), 0);
}

void get_move(){
    bool success = false;   // I think read doesn't block and just keep sending so 
                            // I have this success variable to only continue if stones_left
                            // changes.
    while(!finished && !success){
        int n = 0;
        std::fill(buffer, buffer+1024, 0);
        n = read(sock, buffer, 1024);
        if(n <= 0) { // socket closes or some other error.
            finished = true;
            return;
        }
        std::cout << buffer << std::endl;
        nlohmann::json game_state = nlohmann::json::parse(buffer);
        finished = game_state["finished"];
        // deal with current max issues.
        current_max = game_state["current_max"];
        current_max++;
        if(current_max < 3) current_max = 3;
        if(stones_left == game_state["stones_left"]) continue;
        stones_left = game_state["stones_left"];
        success = true;
        reset_used = game_state["reset_used"];
        player_0.resets_left = game_state["player_0"]["resets_left"];
        player_1.resets_left = game_state["player_1"]["resets_left"];
    }
}

int main(int argc, char const *argv[])
{
    // connect to server
    // https://www.geeksforgeeks.org/socket-programming-cc/
    struct sockaddr_in address;
    sock = 0, valread;
    struct sockaddr_in serv_addr;
    if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    {
        printf("\n Socket creation error \n");
        return -1;
    }

    memset(&serv_addr, '0', sizeof(serv_addr));

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(PORT);

    // Convert IPv4 and IPv6 addresses from text to binary form
    if(inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr)<=0) 
    {
        printf("\nInvalid address/ Address not supported \n");
        return -1;
    }

    if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
    {
        printf("\nConnection Failed \n");
        return -1;
    }

    order = 1; //0 if going first, 1 otherwise.
    if(argc == 2 && strcmp(argv[1], "f") == 0){
        order = 0;
    }
    // send initial message
    nlohmann::json init_json = {
        {"name", bot_name}, 
        {"order", order}
    };
    const char * init_c_str = init_json.dump().c_str();
    send(sock , init_c_str, strlen(init_c_str), 0 );
    printf("Initial message sent\n");
    valread = read( sock , buffer, 1024);
    nlohmann::json init_state = nlohmann::json::parse(buffer);

    // init everything to defaults;
    AlgoAI my_ai;
    stones_left = init_state["init_stones"];
    current_max = 3;
    stones_removed = 0;
    finished = false;
    player_0.resets_left = 4;
    player_1.resets_left = 4;
    reset_used = false;
    init_max = 3;

    // send first move if needed.
    if(order == 0){
        send_move();
    }
    while(!finished){
        get_move();
        send_move();
    }
    return 0;
}