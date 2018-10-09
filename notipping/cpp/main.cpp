#include "notipping_client.cpp"

#include <getopt.h>

using namespace std;

int main(int argc, char **argv)
{
    string HOST = "127.0.0.1";
    int PORT = 3000;
    bool goesFirst = false;
    //opterr = 0;
    int c;
    static struct option long_options[] = {
        {"host", optional_argument, 0, 'h'},
        {"port", optional_argument, 0, 'p'},
        {"first", no_argument, 0, 'f'},
        {0, 0, 0, 0}};
    while (true)
    {
        int option_index = 0;
        c = getopt_long(argc, argv, "h:p:f",
                        long_options, &option_index);
        if (c == -1)
            break;
        switch (c)
        {
        case 'p':
            // printf("option -p with value `%s', `%d'\n", optarg, optarg);
            PORT = atoi(optarg);
            break;
        case 'h':
            // printf("option -h with value `%s, `%d''\n", optarg, optarg);
            HOST = optarg;
            break;
        case 'f':
            // printf("option -f\n");
            goesFirst = true;
            break;
        case '?':
            break;
        default:
            printf("?? getopt returned character code 0%o ??\n", c);
        }
    }

    NoTippingClient ntp(HOST, PORT, goesFirst);
    ntp.play_game();

    return 0;
}