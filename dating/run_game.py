import sys

from argparse import ArgumentParser
from multiprocessing import Process, Lock
from time import sleep

from dating_server import GameServer

# from clients.matchmaker_client import MatchMaker
# from clients.player_client import Player
from clients.botty_matchmaker import BottyMatchMaker
from clients.botty_player import BottyPlayer


def init_matchmaker(name):
    sleep(1)
    # player = MatchMaker(name=name)
    player = BottyMatchMaker()
    player.play_game()

def init_player(name):
    sleep(1)
    # player = Player(name=name)
    player = BottyPlayer()
    player.play_game()

def main():

    # randomFile = sys.argv[2]
    player_1 = Process(target=init_matchmaker, args=('Player Sam',))
    player_1.start()
    player_2 = Process(target=init_player, args=('Matchmaker Inav',))
    player_2.start()
    n = sys.argv[1]
    controller = GameServer(n)

    # if sys.argv[1] == "m":
    #     # player = MatchMaker(name="MatchMaker")
    #     player = BottyMatchMaker(name="MatchMaker")
    #     player.play_game()
    # elif sys.argv[1] == "p":
    #     player = BottyPlayer()
    #     # player = Player(name="Player")
    #     player.play_game()
    # else:
    #     n = sys.argv[1]
    #     controller = GameServer(n)

if __name__ == '__main__':
    main()
