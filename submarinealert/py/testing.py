import sys

from multiprocessing import Process
from time import sleep
from random import randint

from submarine_server import GameServer

from clients.submarine_captain_client import SubmarineCaptain
from clients.trench_manager_client import TrenchManager
from clients.aldo_client import ATrenchManager
from clients.useless_client import UTrenchManager

def init_submarine_captain(name, is_manual_mode, fd):
    sleep(0.2)
    player = SubmarineCaptain(name=name)
    player.play_game()

def init_trench_manager(name, is_manual_mode, fd):
    sleep(0.2)
    player = TrenchManager(name=name)
    player.play_game()

def init_atrench_manager(name, is_manual_mode, fd):
    sleep(0.2)
    player = ATrenchManager(name=name)
    player.play_game()

def init_utrench_manager(name, is_manual_mode, fd):
    sleep(0.2)
    player = UTrenchManager(name=name)
    player.play_game()

def main():
    trench_managers = [init_trench_manager, init_atrench_manager, init_utrench_manager]
    n = len(trench_managers)
    wins = [0] * n
    failCount = [0] * n
    for i in range(100):
        d = randint(0, 99)
        y = randint(1, 1000)
        r = y + randint(1, 1000)
        m = randint(1, 50)
        L = randint(1, 11)
        p = randint(1, 1000)
        cost = [0] * n
        for j in range(n):
            player_1 = Process(target=init_submarine_captain, args=('Captain Joe', False, sys.stdin.fileno()))
            player_1.start()
            player_2 = Process(target=trench_managers[j], args=('Manager Zach', False, sys.stdin.fileno()))
            player_2.start()
            controller = GameServer(d, y, r, m, L, p)
            cost[j] = controller.trench_cost
            if not controller.trench_condition_achieved:
                input("check fail " + str(j))
                failCount[j] += 1
            controller.server.close()
        minCost = min(cost)
        for j in range(3):
            if cost[j] == minCost:
                wins[j] += 1
                break
    print("wins: ")
    print(wins)
    print("\nfails: ")
    print(failCount)
    print("\n")

if __name__ == '__main__':
    main()
