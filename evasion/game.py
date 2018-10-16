import random
import operator

WALL = 2
VERT = (0, 1)
HORI = (1, 0)
DIA = (1, 1)
CDIA = (-1, 1)

GRID_SIZE = 10

PREY_MOVES = [
    (0, 1),
    (1, 0),
    (0, -1),
    (-1, 0),
    (1, 1),
    (-1, 1),
    (1, -1),
    (-1, -1)
]

MAX_WALLS = 100
walls = []

def start():
    grid = [[0] * (GRID_SIZE + 2) for _ in range(GRID_SIZE + 2)]
    for i in range(0, len(grid[0])):
        grid[0][i] = WALL
    for i in range(0, len(grid[0])):
        grid[i][0] = WALL
    for i in range(0, len(grid[-1])):
        grid[-1][i] = WALL
    for i in range(0, len(grid[0])):
        grid[i][-1] = WALL
    
    hunter_pos = (1, 1)
    prey_pos = (231, 201)
    turn = 0

    hunter_dir = (1, 1)
    is_prey_move = False
    placed = 0
    while True:
        print("Turn:", turn)
        print("Hunter Position", hunter_pos)
        print("Prey Position", prey_pos)

        should_place_wall = False
        if should_place_wall:
            wall_start_pos = (0, 0)
            wall_length = 10
            wall_dir = random.choice([VERT, HORI, DIA, CDIA])
            new_grid, valid = place_wall(hunter_pos, prey_pos, wall_start_pos, wall_dir, wall_length, grid)
            if valid:
                placed += 1
                walls.append((wall_start_pos, wall_dir, wall_length))
                grid = new_grid
                print(f"Hunter places wall starting at {wall_start_pos} of length {wall_length} in direction {wall_dir}")
                if placed == MAX_WALLS:
                    random_wall = random.choice(walls)
                    grid = remove_wall(random_wall[0], random_wall[1], random_wall[2], grid)
        
        print("Hunter moves in direction:", hunter_dir)
        hunter_pos, new_hunter_dir = get_new_pos(hunter_pos, hunter_dir, grid)
        print("Hunter new position:", hunter_pos)
        hunter_dir = new_hunter_dir

        if is_prey_move:
            prey_dir = random.choice(PREY_MOVES)
            print("Prey moves in direction:", prey_dir)
            prey_pos, prey_dir = get_new_pos(prey_pos, prey_dir, grid)
            print("Prey new position:", prey_pos)

        if compute_distance(hunter_pos, prey_pos) <= 4:
            break
        
        print()
        if is_prey_move:
            is_prey_move = False
        else:
            is_prey_move = True
    
    print("Hunter caught prey in", turn, "turns")


def get_new_pos(prev, direc, grid):
    new_pos = tuple(map(operator.add, prev, direc))
    if grid[new_pos[0]][new_pos[1]] == WALL:
        if direc in [(0, 1), (0, -1), (1, 0), (-1, 0)]:
            new_pos = prev
            new_direc = (direc[0] * -1,  direc[1] * -1)
        else:
            adj_x = grid[new_pos[0]][new_pos[1] + direc[1] * -1]
            adj_y = grid[new_pos[0] + direc[0] * -1][new_pos[1]]
            if adj_x == WALL and adj_y == WALL:
                return prev, (direc[0] * -1, direc[1] * -1)
            elif adj_x == WALL and adj_y != WALL:
                return (prev[0], new_pos[1]), (direc[0], direc[1] * -1)
            elif adj_x != WALL and adj_y == WALL:
                return (new_pos[0], prev[1]), (direc[0] * -1, direc[1])
            else:
                adj_new_x = grid[new_pos[0]][new_pos[1] + direc[1]]
                adj_new_y = grid[new_pos[0] + direc[0]][new_pos[1]]
                if (adj_new_x == WALL and adj_new_y == WALL) or (adj_new_x != WALL and adj_new_y != WALL):
                    return prev, (direc[0] * -1, direc[1] * -1)
                elif adj_new_x == WALL and adj_new_y != WALL:
                    return (prev[0], new_pos[1]), (direc[0] * -1, direc[1])
                else:
                    return (new_pos[0], prev[1]), (direc[0], direc[1] * -1)
    else:
        return new_pos, direc

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

def place_wall(hunter_pos, prey_pos, start, direc, length, grid):
    new_grid = [[0] * (GRID_SIZE + 2) for _ in range(GRID_SIZE + 2)]
    for i in range(len(new_grid)):
        for j in range(len(new_grid[i])):
            new_grid[i][j] = grid[i][j]
    touches_hunter = False
    touches_prey = False
    touches_wall = False
    for i in range(length):
        if (start[0] + direc[0] * i, start[1] + direc[1] * i) == hunter_pos:
            touches_hunter = True
            print("Wall touches hunter!")
            break
        elif new_grid[start[0] + direc[0] * i][start[1] + direc[1] * i] == WALL:
            touches_wall = True
            print("Wall touches another wall!")
            break
        elif (start[0] + direc[0] * i, start[1] + direc[1] * i) == prey_pos:
            print("Wall touches prey!")
            touches_prey = True
            break
        else:
            new_grid[start[0] + direc[0] * i][start[1] + direc[1] * i] = WALL
    if touches_hunter or touches_prey or touches_wall:
        return grid, False
    return new_grid, True

if __name__ == "__main__":
    start()