import json
from random import randint, choice

from clients.trench_manager_client import TrenchManager

class ATrenchManager(TrenchManager):
    def __init__(self):
        super().__init__("Trenchy McTrenchFace")
        self.redZone = set()
        self.redAlert = False
        self.time = 0
        self.probes = []
        self.subFound = False
        self.verbose = False
        self.lowerBound = -1
        self.upperBound = -1
        self.safe = False
        self.redZoneStart = self.d
        self.yellowAlertCost = self.y
        self.redAlertCost = self.r
        self.gameTime = self.m
        self.scanRange = self.L
        self.probeCost = self.p
        for i in range(self.d, self.d + 6):
            self.redZone.add(i % 100)

    def send_probes(self):
        return self.getProbes()

    def choose_alert(self, sent_probes, results):
        self.receiveProbeResults(results)
        return "red" if self.shouldGoRed() else "yellow"

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
            #print(str(self.probes[i] - self.scanRange) + " " + str(self.probes[i] + self.scanRange) + " probe")
            if results[i]:
                tempLowerBound = max(tempLowerBound, self.probes[i] - self.scanRange)
                tempUpperBound = min(tempUpperBound, self.probes[i] + self.scanRange)
            else:
                if tempLowerBound > self.probes[i] - self.scanRange:
                    tempLowerBound = max(tempLowerBound, self.probes[i] + self.scanRange)
                if tempUpperBound < self.probes[i] + self.scanRange:
                    tempUpperBound = min(tempUpperBound, self.probes[i] - self.scanRange)
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
            #print(pos)
            if(pos == ((self.upperBound + 1) % 100)):
                break
            if pos in self.redZone:
                #print(pos)
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