import abc

class Prey(abc.ABC):

    def __init__(self, start_pos, hunter_pos, hunter_dir, grid):
        self.position = start_pos
        self.hunter_pos = hunter_pos
        self.hunter_dir = hunter_dir
        self.grid = grid

    @abc.abstractmethod
    def getMove(self):
        pass

    @abc.abstractmethod
    def receiveHunterPosition(self, position, direction):
        pass
