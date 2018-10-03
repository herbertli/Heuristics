from random import randint

from abstract_no_tipping_player import Player
from checks_one_move_ahead_player import COMAPlayer

# fixed
BOARDLENGTH = 30 # half the board length
BOARDWEIGHT = 3

# number of tests to run. Both players play as first player n times
n = randint(1, 1)
# max weight of the block, from 1 to 25
k = randint(25, 25)
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

# [winner, reason]
def run_test(p: list) -> list:
    turn = 0
    board = [0] * (2 * BOARDLENGTH + 1)
    board[-4] = 3
    blocks = [(1 << (k + 1)) - 2] * 2
    stage = 0
    for i in range(2 * k):
        state = {'state' : stage, 'current_player' : turn, 'board' : board.copy(), 'blocks' : blocks.copy()}
        p[turn].receiveGameState(state)
        pair = p[turn].placeBlock()
        if (not (blocks[turn] & (1 << pair['weight']))): # turn player doesn't have the block of the weight that he's trying to place
            if verbose:
                print("Player {} doesn't have block {} to place.".format(turn, pair['weight']))
            return [turn ^ 1, 0]
        if (abs(pair['loc'] > BOARDLENGTH)):
            if verbose:
                print("Player {} is tried to place at {}.".format(turn, pair['loc']))
            return [turn ^ 1,  1]
        if (board[pair['loc']] > 0): # there's already a block where turn player is trying to play
            if verbose:
                print("Player {} is tried to place a block on top of another block at {}.".format(turn, pair['loc']))
            return [turn ^ 1, 2]
        if verbose:
            print("Player {} placed a block {} at {}.".format(turn, pair['weight'], pair['loc']))
        blocks[turn] ^= (1 << pair['weight'])
        board[pair['loc']] = pair['weight']
        if (isGameOver(board)):  # turn player tipped the board
            if verbose:
                print("Player {} tipped the board".format(turn))
            return [turn ^ 1, 3]
        turn = turn ^ 1
    stage = 1
    for i in range(2 * k + 1):
        state = {'state' : stage, 'current_player' : turn, 'board' : board.copy(), 'blocks' : blocks.copy()}
        p[turn].receiveGameState(state)
        loc = p[turn].removeBlock()
        if (abs(loc) > BOARDLENGTH):
            if verbose:
                print("Player {} tried to remove from {}.".format(turn, loc))
            return [turn ^ 1, 4]
        if (board[loc] == 0): # there's already a block where turn player is trying to play
            if verbose:
                print("Player {} tried to remove a block where there is no block at {}.".format(turn, loc))
            return [turn ^ 1, 5]
        if verbose:
            print("Player {} removed a block of weight {} from {}".format(turn, board[loc], loc))
        board[loc] = 0
        if (isGameOver(board)):  # turn player tipped the board
            if verbose:
                print("Player {} tipped the board".format(turn))
            return [turn ^ 1, 6]
        turn = turn ^ 1
    return [-1, -1, -1]

def main():
    wins = [0] * 2
    fail_reason = [[0] * 7] * 2
    p1 = COMAPlayer()
    p2 = COMAPlayer()
    players = [p1, p2]
    for i in range(n):
        winner, reason = run_test(players)
        wins[winner] += 1
        fail_reason[winner^1][reason] += 1
    print("Player 0 won {} times.".format(wins[0]))
    print("Player 1 won {} times.".format(wins[1]))
    for i in range(2):
        print("Player {} failed:".format(i))
        for j in range(7):
            print("\ton stage {}: {} times".format(j, fail_reason[i][j]))
    wins = [0] * 2
    fail_reason = [[0] * 7] * 2
    players = [p2, p1]
    for i in range(n):
        winner, reason = run_test(players)
        wins[winner] += 1
        fail_reason[winner^1][reason] += 1
    print("Player 0 won {} times.".format(wins[1]))
    print("Player 1 won {} times.".format(wins[0]))
    for i in range(2):
        print("Player {} failed:".format(i))
        for j in range(7):
            print("\ton stage {}: {} times".format(j, fail_reason[i^1][j]))

if __name__ == '__main__':
    main()