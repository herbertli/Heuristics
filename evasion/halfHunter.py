from hunter import Hunter
from util import place_wall

HORI = (1, 0)
VERT = (0, 1)
CDIA = (-1, 1)
DIA = (1, 1)

class HalfHunter(Hunter):

    def __init__(self, start_pos, direction, prey_pos, max_walls):
        if max_walls < 2:
            raise RuntimeError("In order for HalfHunter to run, it must have at least 2 walls")
        self.prev_pos = None
        self.current_area = 300 * 300
        return super().__init__(start_pos, direction, prey_pos, max_walls)

    def set_grid(self, grid):
        self.grid = grid

    def receivePreyPosition(self, position):
        self.prey_pos = position

    def updatePosition(self, position, direction):
        self.prev_pos = self.position
        self.position = position
        self.direction = direction

    def placeAndRemoveWall(self):
        if not self.prev_pos:
            return [], (None, None, None)
        should_place_wall = False
        for direction in [HORI, VERT, DIA, CDIA]:
            loc = self.prev_pos
            length = 1
            while loc[0] >= 1 and loc[1] >= 1:
                length += 1
                loc = (loc[0] - direction[0], loc[1] - direction[1])
            start_pos = loc
            loc = self.prev_pos
            while loc[0] <= 300 and loc[1] <= 300:
                length += 1
                loc = (loc[0] + direction[0], loc[1] + direction[1])
            new_grid, valid = place_wall(self.position, self.prey_pos, start_pos, direction, length, self.grid)
            if valid:
                new_area = self.getArea(self.position, new_grid)
                if new_area <= .5 * self.current_area:
                    self.current_area = new_area
                    remove_walls = []
                    should_place_wall = True
                    if len(self.walls) == self.max_walls:
                        remove_walls.append(self.walls[0])
                        self.walls.append((start_pos, direction, length))
                        self.walls = self.walls[1:]
        if should_place_wall:
            return remove_walls, (start_pos, direction, length)
        else:
            return [], (None, None, None)

    def getArea(self, start, grid):
        visited = [[False * len(grid)] for i in range(len(grid))]
        return self.bfs(start[0], start[1], grid, visited)

    def bfs(self, x, y, grid, visited):
        visited[x][y] = True
        count = 1
        for i in range(-1, 2):
            for j in range(-1, 2):
                if x + i > 300 or x + i < 1 or y + j > 300 or y + j < 1:
                    continue
                if visited[x + i][y + j]:
                    count += self.bfs(x + i, y + j, grid, visited)
        return count
