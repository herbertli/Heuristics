class Aldo_TM():

    def __init__(self, d, y, r, m, L, p):
        self.redZone = set()
        self.redAlert = False
        self.time = 0
        self.probes = []
        self.subFound = False
        self.verbose = False
        self.lowerBound = False
        self.upperBound = False
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
                    self.lowerBound = (self.probes[i] - self.scanRange + 100) % 100
                    self.upperBound = (self.probes[i] + self.scanRange + 100) % 100
                i += 1
        tempLowerBound = self.lowerBound
        tempUpperBound = self.upperBound
        if tempUpperBound < tempLowerBound:
            tempUpperBound += 100
        while i < len(results):
            if self.probes[i] + self.scanRange <= tempLowerBound:
                self.probes[i] += 100
            if results[i]:
                tempLowerBound = max(tempLowerBound, self.probes[i] - self.scanRange)
                tempUpperBound = min(tempUpperBound, self.probes[i] + self.scanRange)
            else:
                if tempLowerBound > self.probes[i] - self.scanRange:
                    tempLowerBound = max(tempLowerBound, self.probes[i] + self.scanRange)
                if tempUpperBound < self.probes[i] + self.scanRange:
                    tempUpperBound = min(tempUpperBound, self.probes[i] - self.scanRange)
            i += 1
        self.lowerBound = (tempLowerBound + 100) % 100
        self.upperBound = (tempUpperBound + 100) % 100

    def shouldGoRed(self) -> bool:
        self.time += 1
        tempRedZoneStart = self.redZoneStart
        tempLowerBound = self.lowerBound
        tempUpperBound = self.upperBound
        if tempUpperBound < tempLowerBound:
            tempUpperBound += 100
        if tempRedZoneStart + 5 < tempLowerBound:
            tempRedZoneStart += 100
        if tempRedZoneStart >= tempLowerBound and tempRedZoneStart <= tempUpperBound:
            return True
        if tempRedZoneStart + 5 >= tempLowerBound and tempRedZoneStart + 5 <= tempUpperBound:
            return True
        if (abs(tempRedZoneStart - tempUpperBound) > self.gameTime - self.time 
            and abs((tempRedZoneStart + 5) - tempLowerBound) > self.gameTime - self.time):
            self.safe = True
        return False

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
