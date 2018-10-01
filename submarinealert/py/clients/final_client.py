import sys

from random import randint

from clients.trench_manager_client import TrenchManager

# regular clients
from clients.aldo_tm import Aldo_TM
from clients.useless_trench import Useless_Trench
from clients.ternary_trench import TernaryTrench
from clients.smart_sub import SmartSub


class TrenchyMcTrenchFace(TrenchManager):

    def __init__(self):
        super().__init__("Trenchy McTrenchFace")
        wins = self.simulate(self.d, self.y, self.r, self.m, self.L, self.p)
        print(wins)
        self.bestWins = wins.index(max(wins))
        self.tms = [
            Aldo_TM(self.d, self.y, self.r, self.m, self.L, self.p),
            Useless_Trench(self.d, self.y, self.r, self.m, self.L, self.p),
            TernaryTrench(self.d, self.y, self.r, self.m, self.L, self.p)
        ]

    def simulate(self, d, y, r, m, L, p):
        tms = []
        wins = []
        fails = []
        for i in range(1000):
            subPosition = randint(0, 99)

            tms = []
            tms.append(Aldo_TM(d, y, r, m, L, p))
            tms.append(Useless_Trench(d, y, r, m, L, p))
            tms.append(TernaryTrench(d, y, r, m, L, p))

            if len(wins) == 0:
                wins = [0] * len(tms)

            if len(fails) == 0:
                fails = [0] * len(tms)

            costs = [0] * len(tms)

            for j in range(len(tms)):
                cost, failed = self.test(
                    i, tms[j], d, y, r, m, L, p, subPosition)
                costs[j] = cost

                if failed:
                    fails[j] += 1

            minCost = min(costs)
            for j in range(len(costs)):
                if costs[j] == minCost:
                    wins[j] += 1

        return wins

    def test(self, run, tm, d, y, r, m, L, p, subPosition, verbose=False):
        redZone = set()
        for i in range(d, d + 6):
            redZone.add(i % 100)
        if verbose:
            print("d: %d, y: %d, r: %d, m: %d, L: %d, p: %d, subPosition: %d".format(
                d, y, r, m, L, p, subPosition))

        sub = SmartSub(subPosition, m, L)

        cost = 0

        failed = False
        probesUsed = 0

        for i in range(m):
            subPosition = (subPosition + sub.getMove()) % 100

            if verbose:
                print("Time:", i)
                print("Submarine Position:", subPosition)

            probes = tm.getProbes()
            cost += len(probes) * p
            probesUsed += len(probes)
            if verbose:
                print("TM probes:", probes)

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
                probed |= yes[j]
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
                    print("d: {}, y: {}, r: {}, m: {}, L: {}, p: {}, subPosition: {}".format(
                        d, y, r, m, L, p, subPosition))
                    print("Uh oh! Game over!")
                    failed = True
                    print("Special Case?:", tm.isSpecial)
                    break

            sub.hasBeenProbed(probed)

        if not failed:
            return cost, failed
        else:
            return 5 * m * p + r * m, failed

    def send_probes(self):
        return self.tms[self.bestWins].getProbes()

    def choose_alert(self, sent_probes, results):
        self.tms[self.bestWins].receiveProbeResults(sent_probes)
        return "red" if self.tms[self.bestWins].shouldGoRed() else "yellow"
