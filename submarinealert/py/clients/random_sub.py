import random
import collections


class RandomSub():

    def __init__(self, position: int):
        self.position = position
        self.commandList = collections.deque()

    def generateNewCommand(self):
        magnitude = random.randint(1, 10)
        dir = random.choice([-1, 1])
        for _ in range(magnitude):
            self.commandList.append(1 * dir)

    def getMove(self) -> int:
        if len(self.commandList) == 0:
            self.generateNewCommand()
        return self.commandList.popleft()
