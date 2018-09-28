import random

from clients.submarine_captain_client import SubmarineCaptain

class SmartSub(SubmarineCaptain):

    def __init__(self):
        super().__init__("Subby McSubFace")
        self.gameTime = self.m
        self.scanRange = self.L
        self.tolerance = self.gameTime / 10
        self.direction = -1 if random.randint == 0 else 1
        self.probeTimes = []
        self.time = 0
        self.times_probed = 0

    def your_algorithm(self, times_probed):
        if self.times_probed < times_probed:
            self.probeTimes.append(self.time)
        return self.getMove()

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
