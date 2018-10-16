import random
import operator

WALL = 2
VERT = 0
HORI = 1
DIA = 2
CDIA = 3

GRID_SIZE = 300

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

def start():
    grid = [[0] * GRID_SIZE for _ in range(GRID_SIZE)]
    hunter_pos = (0, 0)
    prey_pos = (230, 200)
    turn = 0

    hunter_dir = (1, 1)
    is_prey_move = False
    while True:
        print("Turn:", turn)
        print("Hunter Position", hunter_pos)
        print("Prey Position", prey_pos)

        should_place_wall = False
        if should_place_wall:
            wall_start_pos = (0, 0)
            wall_length = 10
            wall_dir = random.choice([VERT, HORI, DIA, CDIA])
            grid, valid = place_wall(wall_start_pos, wall_dir, wall_length, grid)
            if valid:
                print(f"Hunter places wall starting at {wall_start_pos} of length {wall_length} in direction {wall_dir}")
                
        
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
    if grid[[new_pos[0]][new_pos[1]] == WALL:
        if direc in [(0, 1), (0, -1), (1, 0), (-1, 0)]:
            new_pos = prev
            new_direc = (direc[0] * -1,  direc[1] * -1)
        else:
            new_x_dir = direc[0]
            new_y_dir = direc[1]
            new_y = prev[1] + direc[1]
            if grid[prev[0]][new_y] == WALL:
                new_y = prev[1]
                new_y_dir *= -1
            new_x = prev[0] + direc[0]
            if grid[new_x[new_y] == WALL:
                new_x = prev[1]
                new_x_dir *= -1
            new_direc = (new_x_dir, new_y_dir)
            new_pos = (new_x, new_y)
        return new_pos, new_direc
    else:
        return new_pos, direc

def compute_distance(p1, p2):
    return ((p1[0] - p2[0]) ** 2 + (p1[1] - p2[1]) ** 2) ** .5

def place_wall(i, j, dir, grid):    
    return True

if __name__ == "__main__":
    start()