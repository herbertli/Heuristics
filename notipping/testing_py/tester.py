from random import randint

from abstract_no_tipping_player import Player
from checks_one_move_ahead_player import COMAPlayer
from dp_player import DPPlayer
from max_player import MaxPlayer

# fixed
BOARDLENGTH = 30  # half the board length
BOARDWEIGHT = 3

# number of tests to run. Both players play as first player n times
n = 1
# max weight of the block, from 1 to 25
K = None
# print debugging statements
verbose = False


def isGameOver(board: list) -> bool:
    leftTorque = 0
    rightTorque = 0
    for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
        if board[i] > 0:
            leftTorque += (i + 3) * board[i]
            rightTorque += (i + 1) * board[i]
    # add torque for initial blocks
    leftTorque += 3 * BOARDWEIGHT
    rightTorque += BOARDWEIGHT
    return leftTorque < 0 or rightTorque > 0

# [winner, reason]


def run_test(p: list) -> list:
    if K is None:
        k = randint(10, 25)
    else:
        k = K
    if verbose:
        print("k is {}".format(k))
    turn = 0
    board = [0] * (2 * BOARDLENGTH + 1)
    board[-4] = 3
    blocks = [(1 << (k + 1)) - 2] * 2
    stage = 0
    for i in range(2 * k):
        state = {'state': stage, 'current_player': turn,
                 'board': board.copy(), 'blocks': blocks.copy()}
        p[turn].receiveGameState(state)
        pair = p[turn].placeBlock()
        # turn player doesn't have the block of the weight that he's trying to place
        if (not (blocks[turn] & (1 << pair['weight']))):
            if verbose:
                print("Player {} doesn't have block {} to place.".format(
                    turn, pair['weight']))
            return [turn ^ 1, 0]
        if (abs(pair['loc'] > BOARDLENGTH)):
            if verbose:
                print("Player {} is tried to place at {}.".format(
                    turn, pair['loc']))
            return [turn ^ 1,  1]
        if (board[pair['loc']] > 0):  # there's already a block where turn player is trying to play
            if verbose:
                print("Player {} is tried to place a block on top of another block at {}.".format(
                    turn, pair['loc']))
            return [turn ^ 1, 2]
        if verbose:
            print("{}. Player {} placed a block {} at {}.".format(
                i, turn, pair['weight'], pair['loc']))
        blocks[turn] ^= (1 << pair['weight'])
        board[pair['loc']] = pair['weight']
        if (isGameOver(board)):  # turn player tipped the board
            if verbose:
                print("Player {} tipped the board".format(turn))
            return [turn ^ 1, 3]
        turn = turn ^ 1
    stage = 1
    for i in range(2 * k + 1):
        state = {'state': stage, 'current_player': turn,
                 'board': board.copy(), 'blocks': blocks.copy()}
        p[turn].receiveGameState(state)
        loc = p[turn].removeBlock()
        if (abs(loc) > BOARDLENGTH):
            if verbose:
                print("Player {} tried to remove from {}.".format(turn, loc))
            return [turn ^ 1, 4]
        if (board[loc] == 0):  # there's already a block where turn player is trying to play
            if verbose:
                print("Player {} tried to remove a block where there is no block at {}.".format(
                    turn, loc))
            return [turn ^ 1, 5]
        if verbose:
            print("{}. Player {} removed a block of weight {} from {}".format(
                i, turn, board[loc], loc))
        board[loc] = 0
        if (isGameOver(board)):  # turn player tipped the board
            if verbose:
                print("Player {} tipped the board".format(turn))
            return [turn ^ 1, 6]
        turn = turn ^ 1
    return [-1, -1]


def scaffold(p: list) -> None:
    wins = [0] * 2
    fail_reason = [[0] * 7 for i in range(2)]
    for i in range(n):
        print(".", end="")
        if (i+1) % 10 == 0:
            print(flush=True)
        players = [p[0](), p[1]()]
        winner, reason = run_test(players)
        wins[winner] += 1
        fail_reason[winner ^ 1][reason] += 1
    print(flush=True)
    for i in range(2):
        print("Player {} won {} times.".format(i, wins[i]))
        print("Player {} failed:".format(i))
        for j in range(7):
            print("\ton stage {}: {} times".format(j, fail_reason[i][j]))


def main():
    p0 = DPPlayer
    p1 = COMAPlayer
    scaffold([p0, p1])
    print("Switch who is going first.")
    scaffold([p1, p0])


if __name__ == '__main__':
    main()
