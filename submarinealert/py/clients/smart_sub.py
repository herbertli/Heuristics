import random


class SmartSub():

    def __init__(self, position: int, gameTime: int, scanRange: int):
        self.gameTime = gameTime
        self.scanRange = scanRange
        self.tolerance = self.gameTime / 10
        self.position = position
        self.direction = -1 if random.randint == 0 else 1
        self.probeTimes = []
        self.time = 0

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

    def hasBeenProbed(self, probed: bool) -> None:
        self.probeTimes.append(self.time)


if __name__ == "__main__":
    ss = SmartSub(0, 10, 12)
    for i in range(10):
        print(ss.getMove())
