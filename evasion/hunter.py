import abc

class Hunter(abc.ABC):

    def __init__(self, start_pos, direction, prey_pos, max_walls):
        self.position = start_pos
        self.direction = direction
        self.prey_pos = prey_pos
        self.walls = []
        self.max_walls = max_walls

    @abc.abstractmethod
    def placeAndRemoveWall(self):
        pass

    @abc.abstractmethod
    def receivePreyPosition(self, position):
        pass
