import json
import socket
from Client import Client
import atexit
import argparse

class AlgoAI:

  def __init__(self):
    self.state = [[[[[0 for i in range(5)] for j in range(6)] for k in range(3)] for l in range(52)] for m in range(1002)]
    self.fillBaseCases()
    for i in range(4, 1001):
      self.fill(i)

  def getMove(self, n, cM, cR, oR, eR):
    if cM < 3:
      cM = 3
    return self.state[n][cM][cR][oR][eR]

  def fillBaseCases(self):
    # if n = 0, current player loses
    for cM in range(0, 51):
      for cR in range(0, 2):
        for oR in range(0, 5):
          for tR in range(0, 5):
            self.state[0][cM][cR][oR][tR] = 0

    # if n <= 3, current player wins
    for n in range(1, 4):
      for cM in range(0, 51):
        for cR in range(0, 2):
          for oR in range(0, 5):
            for tR in range(0, 5):
              if n <= cM:
                self.state[n][cM][cR][oR][tR] = n

  def fill(self, n):
    for cM in range(3, 50):
      for cR in range(0, 2):
        for oR in range(0, 5):
          for tR in range(0, 5):
            win = 0   # number of stones to take that make the current state a winning one.
            maxTake = 3 if cR == 1 else cM
            if maxTake > n:
              maxTake = n
            reset = False
            # current player doesn't use a reset
            for i in range(1, maxTake + 1):
              if self.state[n - i][cM + (1 if i == cM else 0)][0][tR][oR] == 0:
                win = max(i, win)
            # current player uses a reset
            # if we can win without using a reset then we should so that we save resets.
            if oR > 0 and win == 0:
              for i in range(1, maxTake + 1):
                if self.state[n - i][cM + (1 if i == cM else 0)][1][tR][oR - 1] == 0:
                  win = max(i, win)
                  reset = True
            self.state[n][cM][cR][oR][tR] = win * (-1 if reset else 1)

  def printStartingMove(self, ):
    wins = 0
    for i in range(0, 1002):
      move = self.state[i][3][0][4][4]
      print(i, ":", move)
      if move != 0:
        wins += 1
    print(wins/1000)

def check_game_status(game_state):    
  if game_state['finished']:
    print(game_state['reason'])
    exit(0)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='A Client for expanding nim.')
    parser.add_argument('-p', type=int, dest="port", help='host port', default=9000)
    parser.add_argument('-f', type=bool, dest="goes_first", help='goes first', default=True)
    parser.add_argument('-i', type=str, dest="ip", help='host name', default="127.0.0.1")
    args = parser.parse_args()

    client = Client('Botty McBotFace', args.goes_first, (args.ip, args.port))
    atexit.register(client.close)
    stones = client.init_stones
    resets = client.init_resets
    ai = AlgoAI()
    my_resets = resets
    their_resets = resets
    current_max = 2

    if args.goes_first:
        num_stones = ai.getMove(stones, current_max, 0, my_resets, their_resets)
        reset = True if num_stones < 0 else False
        if reset:
          my_resets -= 1
        print('You took %d stones%s' % (num_stones, ' and used reset.' if reset else '.'))
        check_game_status(client.make_move(abs(num_stones), reset))
    while True:
        game_state = client.receive_move()
        check_game_status(game_state)
        # Some parsing logic to convert game state to algo_inputs
        num_stones = game_state["stones_left"]
        reset_used = game_state["reset_used"]
        current_max = max(game_state["current_max"] + 1, current_max)
        if reset_used:
          their_resets -= 1
        num_stones = ai.getMove(num_stones, current_max, 1 if reset_used else 0, my_resets, their_resets)
        reset = True if num_stones < 0 else False
        if num_stones == 0:
          num_stones += 1
        print('You took %d stones%s' % (num_stones, ' and used reset.' if reset else '.'))
        print('Current max: %d' % game_state['current_max'])
        print('Stones left: %d' % game_state['stones_left'])
        print('Player %s has %d resets left' % (game_state['player_0']['name'], game_state['player_0']['resets_left']))
        print('Player %s has %d resets left' % (game_state['player_1']['name'], game_state['player_1']['resets_left']))
        print('---------------------------------------')
        if game_state['finished']:
            print('Game over\n%s' % game_state['reason'])
            exit(0)
        
        check_game_status(client.make_move(abs(num_stones), reset))
