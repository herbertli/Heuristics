import random
from prey import Prey

class RandomPrey(Prey):

    def __init__(self, start_pos, hunter_pos, hunter_dir, grid):
        super().__init__(start_pos, hunter_pos, hunter_dir, grid)

    def receiveHunterPosition(self, position, direction):
        self.hunter_pos = position
        self.hunter_dir = direction

    def getMove(self):
        return random.randint(-1, 1), random.randint(-1, 1)

    def updatePosition(self, position):
        self.position = position
