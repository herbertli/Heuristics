from random import randint

from abstract_no_tipping_player import Player
from checks_one_move_ahead_player import COMAPlayer

# fixed
BOARDLENGTH = 30 # half board
BOARDWEIGHT = 3

# number of tests to run. Both players play as first player n times
n = randint(1, 1)
# max weight of the block, from 1 to 25
k = randint(10, 10)
# print debugging statements
verbose = False

def isGameOver(board: list) -> bool:
    leftTorque = 0
    rightTorque = 0
    for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
        if board[i]:
            leftTorque += (i + 3) * board[i]
            rightTorque += (i + 1) * board[i]
    # add torque for initial blocks
    leftTorque += 3 * BOARDWEIGHT
    rightTorque += BOARDWEIGHT
    return leftTorque < 0 or rightTorque > 0

# 0 if first player wins, 1 if second player wins.
def run_test(p: list) -> int:
    turn = 0
    board = [0] * (BOARDLENGTH + 1)
    board[-4] = 3
    blocks = [(1 << (k + 1)) - 2] * 2
    for i in range(2 * k):
        state = dict()
        state['stage'] = 0
        state['current_player'] = turn
        state['board'] = board
        state['blocks'] = blocks
        p[turn].receiveGameState(state)
        pair = p[turn].placeBlock()
        if (not (blocks[turn] & (1 << pair['weight']))): # turn player doesn't have the block of the weight that he's trying to place
            print("Player {} is trying to place a block they don't have".format(turn))
            return turn ^ 1, 
        if (abs(pair['loc'] > BOARDLENGTH)):
            print("Player {} is trying to place a block out of bounds at {}.".format(turn, pair['loc']))
        if (board[pair['loc']] > 0): # there's already a block where turn player is trying to play
            print("Player {} is trying to place a block at an invalid location".format(turn))
            return turn ^ 1
        blocks[turn] ^= (1 << pair['weight'])
        board[pair['loc']] = pair['weight']
        if (isGameOver(board)):  # turn player tipped the board
            print("Player {} tipped the board".format(turn))
            return turn ^ 1
        turn = turn ^ 1
    for i in range(2 * k + 1):
        state = dict()
        state['stage'] = 1
        state['current_player'] = turn
        state['board'] = board
        state['blocks'] = blocks
        p[turn].receiveGameState(state)
        loc = p[turn].removeBlock()
        if (abs(loc) > BOARDLENGTH):
            print("Player {} is trying to place a block out of bounds at {}.".format(turn, pair['loc']))
            return turn ^ 1
        if (board[loc] == 0): # there's already a block where turn player is trying to play
            print("Player {} is trying to place a block at an invalid location".format(turn))
            return turn ^ 1
        blocks[turn] ^= (1 << pair['weight'])
        board[pair['loc']] = pair['weight']
        if (isGameOver(board)):  # turn player tipped the board
            print("Player {} tipped the board".format(turn))
            return turn ^ 1
        turn = turn ^ 1
    return 0


def main():
    wins = [0] * 2
    p1 = COMAPlayer()
    p2 = COMAPlayer()
    for i in range(n):
        wins[run_test([p1, p2])] += 1
    print("Player 1 won {} times.", wins[0])
    print("Player 2 won {} times.", wins[1])
    wins = [0] * 2
    for i in range(n):
        wins[run_test([p2, p1])] += 1
    print("Player 1 won {} times.", wins[1])
    print("Player 2 won {} times.", wins[0])


if __name__ == '__main__':
    main()