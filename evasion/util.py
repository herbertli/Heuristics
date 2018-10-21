"""
Some utility functions for the game
"""
WALL = 2
VERT = (0, 1)
HORI = (1, 0)
DIA = (1, 1)
CDIA = (-1, 1)

GRID_SIZE = 300

verbose = False
def log(*args, **kwargs):
    if verbose:
        print(*args, **kwargs)

def create_grid(size):
    grid = [[0] * (size + 2) for _ in range(size + 2)]
    for i in range(0, len(grid[0])):
        grid[0][i] = WALL
    for i in range(0, len(grid[0])):
        grid[i][0] = WALL
    for i in range(0, len(grid[-1])):
        grid[-1][i] = WALL
    for i in range(0, len(grid[0])):
        grid[i][-1] = WALL
    return grid

def place_wall(hunter_pos, prey_pos, start, direc, length, grid):
    new_grid = [[0] * (GRID_SIZE + 2) for _ in range(GRID_SIZE + 2)]
    for i in range(len(new_grid)):
        for j in range(len(new_grid[i])):
            new_grid[i][j] = grid[i][j]
    touches_hunter = False
    touches_prey = False
    touches_wall = False
    placed = []
    for i in range(length):
        if (start[0] + direc[0] * i, start[1] + direc[1] * i) == hunter_pos:
            touches_hunter = True
            log("Wall touches hunter!")
            break
        elif new_grid[start[0] + direc[0] * i][start[1] + direc[1] * i] == WALL:
            touches_wall = True
            log("Wall touches another wall!")
            break
        elif (start[0] + direc[0] * i, start[1] + direc[1] * i) == prey_pos:
            log("Wall touches prey!")
            touches_prey = True
            break
        else:
            new_grid[start[0] + direc[0] * i][start[1] + direc[1] * i] = WALL
            placed.append((start[0] + direc[0] * i, start[1] + direc[1] * i))

    # if a diagonal wall, add walls above the first length - 1 walls
    # +-+-+-+       +-+-+-+
    # |*| | |       |*|*| |
    # +-+-+-+       +-+-+-+
    # | |*| |   ->  | |*|*|
    # +-+-+-+       +-+-+-+
    # | | |*|       | | |*|
    # +-+-+-+       +-+-+-+
    if direc == CDIA or direc == DIA:
        for i in range(len(placed) - 1):
            place_above = new_grid[placed[i][0]][placed[i][1] + 1]
            if place_above == WALL:
                touches_wall = True
                log("Wall touches another wall!")
                break
            elif place_above == hunter_pos:
                touches_hunter = True
                log("Wall touches hunter!")
                break
            elif place_above == prey_pos:
                log("Wall touches prey!")
                touches_prey = True
                break
            else:
                new_grid[placed[i][0]][placed[i][1] + 1] = WALL

    if touches_hunter or touches_prey or touches_wall:
        return grid, False
    return new_grid, True

def compute_distance(p1, p2):
    return ((p1[0] - p2[0]) ** 2 + (p1[1] - p2[1]) ** 2) ** .5

def remove_wall(start, direc, length, grid):
    new_grid = [[0] * (GRID_SIZE + 2) for _ in range(GRID_SIZE + 2)]
    for i in range(len(new_grid)):
        for j in range(len(new_grid[i])):
            new_grid[i][j] = grid[i][j]
    for i in range(length):
        new_grid[start[0] + direc[0] * i][start[1] + direc[1] * i] = 0
    return new_grid
