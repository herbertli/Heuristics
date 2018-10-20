from hunter import Hunter

class RandomHunter(Hunter):

    def __init__(self, start_pos, direction, prey_pos, max_walls):
        super().__init__(start_pos, direction, prey_pos, max_walls)

    def placeWall(self):
        return False, (None, None, None)

    def removeWall(self):
        return False, (None, None, None)

    def receivePreyPosition(self, position):
        self.prey_pos = position