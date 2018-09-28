import sys

from multiprocessing import Process
from time import sleep
from random import randint

from submarine_server import GameServer

# socket clients
from clients.smart_sub import SmartSub
from clients.ternary_client import TernaryManager
from clients.aldo_client import ATrenchManager
from clients.useless_client import UTrenchManager

# regular clients
from clients.aldo_tm import Aldo_TM
from clients.useless_trench import Useless_Trench
from clients.ternary_client import TernaryManager

def init_submarine_captain(name, is_manual_mode, fd):
    sleep(0.2)
    player = SmartSub()
    player.play_game()

def init_trench_manager(name, is_manual_mode, fd):
    sleep(0.2)
    player = TernaryManager()
    player.play_game()

def init_atrench_manager(name, is_manual_mode, fd):
    sleep(0.2)
    player = ATrenchManager()
    player.play_game()

def init_utrench_manager(name, is_manual_mode, fd):
    sleep(0.2)
    player = UTrenchManager()
    player.play_game()

def main():
    trench_managers = [init_trench_manager, init_atrench_manager, init_utrench_manager]
    # trench_managers = [init_trench_manager]
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


def simulate():
    tms = []
    wins = []
    fails = []
    for i in range(1000):

        d = randint(1, 100)
        y = randint(1, 10)
        r = y * 10;
        m = randint(100, 500)
        L = randint(1, 11)
        p = randint(1, 50)
        subPosition = randint(0, 99)

        tms = []
        tms.append(Aldo_TM(d, y, r, m, L, p))
        tms.append(Useless_Trench(d, y, r, m, L, p))
        tms.append(TernaryManager(d, y, r, m, L, p))

        if len(wins) == 0:
            wins = [0] * len(tms)

        if len(fails) == 0:
            fails = [0] * len(tms)

        if i % 1000 == 0:
            print("Run:", i);
      
        costs = [0] * len(tms)
        
        for j in range(len(tms)):
            cost, failed = test(i, tms[j], d, y, r, m, L, p, subPosition)
            costs[j] = cost; 

            if failed:
                fails[j] += 1
        
        minCost = min(costs)
        for j in range(len(costs)):
            if costs[j] == minCost:
                wins[j] += 1
    
    print("Failures:", fails)
    print("Wins:", wins)

def test(run, tm, d, y, r, m, L, p, subPosition, verbose=False):
    redZone = set()
    for i in range(d, d + 6):
      redZone.add(i % 100)
    if verbose:
        print("d: %d, y: %d, r: %d, m: %d, L: %d, p: %d, subPosition: %d".format(d, y, r, m, L, p, subPosition))
    
    sub = SmartSub(subPosition, m, L);

    cost = 0

    failed = False
    probesUsed = 0

    for i in range(m):
        subPosition = (subPosition + sub.getMove()) % 100;

        if verbose:
            print("Time:", i)
            print("Submarine Position:", subPosition)

        probes = tm.getProbes()
        cost += len(probes) * p
        probesUsed += len(probes)
        if verbose:
            print("TM probes:", probes);
      
        yes = [False] * len(probes)
        probed = False
        for j in range(len(probes)):
            probe = probes[j]
            lb = (probe + 100 - L) % 100
            ub = (probe + 100 + L) % 100
            if ub < lb:
                ub += 100
            tempSubPosition = subPosition
            while tempSubPosition < lb: 
                tempSubPosition += 100
            yes[j] = (tempSubPosition <= ub)
            probed |= yes[j];
        if verbose:
            print("Probe result:", yes)
        
        tm.receiveProbeResults(yes)
        redAlert = tm.shouldGoRed()
      
        if redAlert:
            cost += r
            if verbose:
                print("TM goes on red alert")
        else:
            cost += y
            if verbose:
                print("TM goes on yellow alert")
        
            if subPosition in redZone:
                print("Run:", run)
                print("Time:", i)
                print("Probes:", probes)
                print(yes)
                print("d: {}, y: {}, r: {}, m: {}, L: {}, p: {}, subPosition: {}".format(d, y, r, m, L, p, subPosition))
                print("Uh oh! Game over!")
                failed = True
                print("Special Case?:", tm.isSpecial)
                break
        
        sub.hasBeenProbed(probed)

    if not failed:
        return cost, failed
    else:
        return 5 * m * p + r * m, failed

if __name__ == '__main__':
    # main()
    simulate()
