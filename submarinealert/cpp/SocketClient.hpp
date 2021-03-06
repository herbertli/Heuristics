#include <cstring>
#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <string>
#include <arpa/inet.h>
#include <unistd.h>
#include "json.hpp"

using namespace std;
using json = nlohmann::json;

class SocketClient
{
public:
  string ip_address;
  int port;
  int client_sock;

  ~SocketClient();
  SocketClient(string, int);
  void close_socket();
  void send_data(string);
  void send_json(json);
  string receive_data(int);
  string receive_large(int, int);
  json receive_json(int);
};

/*
 *  SocketClient Constructor
 *  Arguments:
 *  string ip_address:
 *      the host name of the server
 *  int port:
 *      the port of the server
 */
SocketClient::SocketClient(string ip_address, int port)
{
  this->ip_address = ip_address;
  this->port = port;

  struct sockaddr_in address;
  int sock = 0;
  struct sockaddr_in serv_addr;

  if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0)
  {
    printf("\n Socket creation error \n");
  }

  memset(&serv_addr, '0', sizeof(serv_addr));

  serv_addr.sin_family = AF_INET;
  serv_addr.sin_port = htons(port);

  if (inet_pton(AF_INET, ip_address.c_str(), &serv_addr.sin_addr) <= 0)
  {
    printf("\nInvalid address/ Address not supported \n");
  }

  if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
  {
    printf("\nConnection Failed \n");
  }

  this->client_sock = sock;
}

/*
 *  SocketClient Connect to Server
 *  Arguments:
 *      string data:
 *          the string of data to send
 */
void SocketClient::send_data(string data)
{
  send(client_sock, data.c_str(), strlen(data.c_str()), 0);
}

/*
 *  SocketClient Send JSON
 *  Arguments:
 *      json data:
 *          the JSON object to send
 */
void SocketClient::send_json(json data)
{
  string s = data.dump();
  send(client_sock, s.c_str(), strlen(s.c_str()), 0);
}


/*
 *  SocketClient Close Connection
 */
void SocketClient::close_socket()
{
  close(client_sock);
}

/*
 *  SocketClient Receive Data
 *  Args:
 *      int buffer_size:
 *          the size of the buffer of data to receive
 *  Return:
 *      the string of data sent by server
 */
string SocketClient::receive_data(int buffer_size)
{
  char *buffer = (char *)malloc(sizeof(*buffer) * buffer_size);
  string data;
  int valread = read(client_sock, buffer, buffer_size);
  if (valread >= 1)
  {
    data.append(buffer);
  }
  free(buffer);
  return data;
}

/*
 *  SocketClient Receive Large Chunk of Data
 *  Arguments:
 *      int buffer_size:
 *          the size of the buffer of data to receive
 *      int timeout:
 *          This method fetches chunks of data from the server until socket
 *          times out.
 *  Return:
 *      the string of data sent by server
 */
string SocketClient::receive_large(int buffer_size, int timeout)
{
  string data;
  struct timeval tv;
  tv.tv_sec = timeout;
  tv.tv_usec = 0;
  setsockopt(client_sock, SOL_SOCKET, SO_RCVTIMEO, (const char *)&tv, sizeof(struct timeval));
  fd_set fdset;
  FD_ZERO(&fdset);
  FD_SET(client_sock, &fdset);

  while (true)
  {
    if (select(client_sock + 1, &fdset, NULL, NULL, &tv) == 0)
    {
      break;
    }
    data.append(receive_data(buffer_size));
  }
  tv.tv_sec = 0;
  tv.tv_usec = 0;
  setsockopt(client_sock, SOL_SOCKET, SO_RCVTIMEO, (const char *)&tv, sizeof(struct timeval));
  return data;
}

/*
 *  SocketClient Receive JSON
 *  Args:
 *      int buffer_size:
 *          the size of the buffer of data to receive
 *  Return:
 *      the json object sent by server
 */
json SocketClient::receive_json(int buffer_size)
{
  char *buffer = (char *)malloc(sizeof(*buffer) * buffer_size);
  json data;
  int valread = read(client_sock, buffer, buffer_size);
  if (valread >= 1)
  {
    data = json::parse(buffer);
  }
  free(buffer);
  return data;
}

/*
 *  SocketClient Destructor
 */
SocketClient::~SocketClient()
{
  close(client_sock);
}
