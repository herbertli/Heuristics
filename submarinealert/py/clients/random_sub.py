import random
import collections

from clients.submarine_captain_client import SubmarineCaptain

class RandomSub(SubmarineCaptain):

    def __init__(self, position: int):
        super().__init__("Subby McSubFace")
        self.commandList = collections.deque()

    def your_algorithm(self, times_probed):
        return self.getMove()

    def generateNewCommand(self):
        magnitude = random.randint(1, 10)
        dir = random.choice([-1, 1])
        for _ in range(magnitude):
            self.commandList.append(1 * dir)

    def getMove(self) -> int:
        if len(self.commandList) == 0:
            self.generateNewCommand()
        return self.commandList.popleft()
