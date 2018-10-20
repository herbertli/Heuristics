import random
from prey import Prey

class RandomPrey(Prey):

    def __init__(self, start_pos, hunter_pos, hunter_dir, grid):
        super().__init__(start_pos, hunter_pos, hunter_dir, grid)
        self.MOVES = [
            (0, 1),
            (1, 0),
            (0, -1),
            (-1, 0),
            (1, 1),
            (-1, 1),
            (1, -1),
            (-1, -1)
        ]

    def receiveHunterPosition(self, position, direction):
        self.hunter_pos = position
        self.hunter_dir = direction

    def getMove(self):
        return random.choice(self.MOVES)
