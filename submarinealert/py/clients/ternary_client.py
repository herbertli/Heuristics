from clients.trench_manager_client import TrenchManager


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
        print(i)
        if not self.subFound:
            self.redAlert = False
            return

        if self.time == 0:
            self.leftProbe = (
                subLoc - self.scanRange * 2 - 1) % 100
            self.rightProbe = (
                subLoc + self.scanRange * 2 + 1) % 100
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
