import sys

from random import randint
import random

from clients.trench_manager_client import TrenchManager
from clients.submarine_captain_client import SubmarineCaptain


class TrenchyMcTrenchFace(TrenchManager):

    def __init__(self, name):
        super().__init__("Trenchy McTrenchFace")
        wins = self.simulate(self.d, self.y, self.r, self.m, self.L, self.p)
        self.bestWins = wins.index(max(wins))
        self.tms = [
            Aldo_TM(self.d, self.y, self.r, self.m, self.L, self.p),
            Useless_Trench(self.d, self.y, self.r, self.m, self.L, self.p),
            TernaryManager(self.d, self.y, self.r, self.m, self.L, self.p)
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
            tms.append(TernaryManager(d, y, r, m, L, p))

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

        sub = SubbyMcSubFace("something", subPosition, m, L)

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


class TernaryManager(TrenchManager):

    def __init__(self, d=None, y=None, r=None, m=None, L=None, p=None):
        if d is None:
            super().__init__("Trenchy McTrenchFace")
            self.redZoneStart = self.d
            self.yellowAlertCost = self.y
            self.redAlertCost = self.r
            self.gameTime = self.m
            self.scanRange = self.L
            self.probeCost = self.p
        else:
            self.redZoneStart = d
            self.yellowAlertCost = y
            self.redAlertCost = r
            self.gameTime = m
            self.scanRange = L
            self.probeCost = p
        self.isSpecial = False
        self.redZone = set()
        self.redAlert = False
        self.time = 0
        self.subFound = False
        self.verbose = False
        self.leftProbe = False
        self.rightProbe = False
        for i in range(self.redZoneStart, self.redZoneStart + 6):
            self.redZone.add(i % 100)
        self.scannedLocations = []

    def sendBoundaryScan(self) -> list:
        return [self.leftProbe, self.rightProbe]

    def sendInitialScan(self) -> list:
        probeLocations = []
        probeLocations.append((self.redZoneStart + 2) % 100)

        left = (self.redZoneStart + 2 - self.scanRange) % 100
        while left in self.redZone:
            probeLocations.append((left - self.scanRange - 1) % 100)
            left = left - 2 * self.scanRange - 1
        probeLocations.append((left - self.scanRange - 1) % 100)
        self.leftProbe = (left - self.scanRange - 1) % 100

        right = (self.redZoneStart + 2 + self.scanRange) % 100
        while right in self.redZone:
            probeLocations.append((right + self.scanRange + 1) % 100)
            right = right + 2 * self.scanRange + 1
        self.rightProbe = (right + self.scanRange + 1) % 100
        probeLocations.append((right + self.scanRange + 1) % 100)

        self.scannedLocations = []
        for i in probeLocations:
            self.scannedLocations.append(i)
        # print(self.scanRange)
        # print(self.scannedLocations)
        # input()
        return self.scannedLocations

    def sendScan(self) -> list:
        if self.time % (2 * self.scanRange + 1) == 0:
            return [self.leftProbe, self.rightProbe]
        else:
            return []

    def receiveProbeResults(self, results: list) -> None:
        if len(results) == 0:
            return

        subLoc = False
        # if self.time == 0:
        # print(self.scannedLocations)
        # print(self.scanRange)
        # print(results)
        # input()
        for i in range(len(results)):
            if results[i]:
                if self.time == 0:
                    subLoc = self.scannedLocations[i]
                    self.subFound = True
                    break
                else:
                    if i == 0:
                        subLoc = self.leftProbe
                    else:
                        subLoc = self.rightProbe
                    break

        if not self.subFound:
            self.redAlert = False
            return

        if self.time == 0:
            self.leftProbe = (
                self.scannedLocations[i] - self.scanRange * 2 - 1) % 100
            self.rightProbe = (
                self.scannedLocations[i] + self.scanRange * 2 + 1) % 100
            self.redAlert = True
            return

        # now let's get the intervals for the next scan
        # assume you knew that the sub is in interval M at time t - probeRange
        # at time t, you deploy probes at L and R...
        # three cases:
        # |----- LL -----||----- L -----||----- M -----||----- R -----||----- RR -----|
        # 1. L returns true: deploy probes at LL and M at time t + probeRange
        # 2. R returns true: deploy probes at M and RR at time t + probeRange
        # 3. L and R return false: deploy probes at L and R at time t + probeRange
        # if L, M, R overlap with redzone, go to red alert over the next probeRange time

        if self.verbose:
            print(f"Sub loc: %d\n", subLoc)

        # sub has moved to L or R, unscanned interval
        if subLoc != -1:
            if subLoc == self.leftProbe:
                self.rightProbe = (
                    self.leftProbe + self.scanRange * 2 + 1) % 100
                self.leftProbe = (
                    self.leftProbe - self.scanRange * 2 - 1) % 100
            elif subLoc == self.rightProbe:
                self.leftProbe = (self.rightProbe -
                                  self.scanRange * 2 - 1) % 100
                self.rightProbe = (self.rightProbe +
                                   self.scanRange * 2 + 1) % 100

        scanZone = set()
        if self.scanRange > 16:
            for i in range(0, 100):
                scanZone.add(i)
        else:
            i = self.leftProbe - self.scanRange
            while i != (self.rightProbe + self.scanRange + 1) % 100:
                scanZone.add(i % 100)
                i = (i + 1) % 100

        if self.verbose:
            print("Scan Zone:", scanZone)

        # SPECIAL CASE: subLoc is too far from redZone to matter
        # should just check middle interval!
        tooFar = True
        d = self.redZoneStart
        i = (self.leftProbe + self.scanRange + 1) % 100
        while i != (self.rightProbe - self.scanRange) % 100:
            if abs(i - d) <= self.gameTime - self.time + 1:
                tooFar = False
                break
            # i was to the left of red zone in range [0, 99], so check distance if sub goes left
            if (i < d):
                if abs(i + 100 - d) <= self.gameTime - self.time + 1:
                    tooFar = False
                    break
            else:  # i was to the right of redzone so check if i goes right.
                if abs(i - (d + 100)) <= self.gameTime - self.time + 1:
                    tooFar = False
                    break

            if abs(i - (d + 5)) <= self.gameTime - self.time + 1:
                tooFar = False
                break
            if (i < (d + 5)):
                if abs(i + 100 - (d + 5)) <= self.gameTime - self.time + 1:
                    tooFar = False
                    break
            else:
                if abs(i - (d + 5 + 100)) <= self.gameTime - self.time + 1:
                    tooFar = False
                    break
            i = (i + 1) % 100

        if tooFar:
            if self.verbose:
                print("Special case!")
            self.redAlert = False
            self.isSpecial = True
            return

        # using scan zone, check for overlap with red zone
        # if so, go red for the next scanRange seconds, otherwise go yellow
        self.redAlert = False
        for j in self.redZone:
            if j in scanZone:
                self.redAlert = True
                return

    def shouldGoRed(self) -> bool:
        self.time += 1
        return self.redAlert

    def getProbes(self) -> list:
        if (self.time == 0):
            return self.sendInitialScan()
        elif not self.subFound and (self.time % (2 * self.scanRange + 1) == 0):
            return self.sendBoundaryScan()
        elif self.subFound and (self.time % (2 * self.scanRange + 1) == 0):
            return self.sendScan()
        else:
            return []

    def send_probes(self):
        return self.getProbes()

    def choose_alert(self, sent_probes, results):
        self.receiveProbeResults(results)
        return "red" if self.shouldGoRed() else "yellow"


class Aldo_TM():

    def __init__(self, d, y, r, m, L, p):
        self.redZone = set()
        self.redAlert = False
        self.time = 0
        self.probes = []
        self.subFound = False
        self.verbose = False
        self.lowerBound = -1
        self.upperBound = -1
        self.safe = False
        self.redZoneStart = d
        self.yellowAlertCost = y
        self.redAlertCost = r
        self.gameTime = m
        self.scanRange = L
        self.probeCost = p
        for i in range(d, d + 6):
            self.redZone.add(i % 100)

    def blanket(self) -> None:
        for i in range(self.redZoneStart, self.redZoneStart+100, 2*self.scanRange+1):
            self.probes.append(i % 100)

    def receiveProbeResults(self, results: list) -> None:
        i = 0
        if self.time == 0:
            while self.lowerBound == -1 and i < len(results):
                if results[i]:
                    self.lowerBound = (
                        self.probes[i] - self.scanRange + 100) % 100
                    self.upperBound = (
                        self.probes[i] + self.scanRange + 100) % 100
                i += 1
        tempLowerBound = self.lowerBound
        tempUpperBound = self.upperBound
        if tempUpperBound < tempLowerBound:
            tempUpperBound += 100
        while i < len(results):
            if self.probes[i] + self.scanRange <= tempLowerBound:
                self.probes[i] += 100
            #print(str(self.probes[i] - self.scanRange) + " " + str(self.probes[i] + self.scanRange) + " probe")
            if results[i]:
                tempLowerBound = max(
                    tempLowerBound, self.probes[i] - self.scanRange)
                tempUpperBound = min(
                    tempUpperBound, self.probes[i] + self.scanRange)
            else:
                if tempLowerBound > self.probes[i] - self.scanRange:
                    tempLowerBound = max(
                        tempLowerBound, self.probes[i] + self.scanRange)
                if tempUpperBound < self.probes[i] + self.scanRange:
                    tempUpperBound = min(
                        tempUpperBound, self.probes[i] - self.scanRange)
            i += 1
            #print(str(tempLowerBound) + " " + str(tempUpperBound) + "\n")
        self.lowerBound = (tempLowerBound + 100) % 100
        self.upperBound = (tempUpperBound + 100) % 100

    def shouldGoRed(self) -> bool:
        #print(str(self.lowerBound) + " " + str(self.upperBound))
        self.time += 1
        self.redAlert = False
        pos = self.lowerBound
        while True:
            # print(pos)
            if(pos == ((self.upperBound + 1) % 100)):
                break
            if pos in self.redZone:
                # print(pos)
                self.redAlert = True
            pos = (pos + 1) % 100
        allSafe = True
        for red in self.redZone:
            if(abs(red - self.lowerBound) > (self.gameTime - self.time)
               and abs(red - self.upperBound) > (self.gameTime - self.time)):
                allSafe = False
        if allSafe:
            safe = True
        return self.redAlert

    def getProbes(self) -> list:
        if (self.safe):
            return []
        elif self.time == 0:
            self.blanket()
        else:
            self.lowerBound = (self.lowerBound - 1 + 100) % 100
            self.upperBound = (self.upperBound + 1 + 100) % 100
            self.probes = [0]
            self.probes[0] = (self.lowerBound + self.scanRange + 2 + 100) % 100
        return self.probes


class Useless_Trench():

    def __init__(self, d, y, r, m, L, p):
        pass

    def getProbes(self) -> list:
        return []

    def receiveProbeResults(self, results: list) -> None:
        pass

    def shouldGoRed(self) -> bool:
        return True


class SubbyMcSubFace(SubmarineCaptain):

    def __init__(self, name, subPosition=None, m=None, L=None):
        if subPosition is None:
            super().__init__("Subby McSubFace")
            self.gameTime = self.m
            self.scanRange = self.L
        else:
            self.gameTime = m
            self.scanRange = L
            self.position = subPosition
        self.tolerance = self.gameTime / 10
        self.direction = -1 if random.randint == 0 else 1
        self.probeTimes = []
        self.time = 0
        self.times_probed = 0

    def getMove(self) -> int:
        if len(self.probeTimes) == 0:
            self.time += 1
            return self.direction
        lastProbed = self.probeTimes[len(self.probeTimes) - 1]
        if self.time - lastProbed > self.tolerance:
            self.time += 1
            self.direction *= -1
            return self.direction
        else:
            self.time += 1
            return self.direction

    def your_algorithm(self, times_probed):
        if self.times_probed < times_probed:
            self.hasBeenProbed(True)
        return self.getMove()

    def hasBeenProbed(self, probed):
        if probed:
            self.probeTimes.append(self.time)
