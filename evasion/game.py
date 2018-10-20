from randomHunter import RandomHunter
from randomPrey import RandomPrey

WALL = 2
VERT = (0, 1)
HORI = (1, 0)
DIA = (1, 1)
CDIA = (-1, 1)

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

MAX_WALLS = 100
WALL_COOLDOWN = 20
walls = []

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

def start():
    grid = create_grid(GRID_SIZE)

    prey_pos = (231, 201)
    hunter_pos = (1, 1)
    hunter_dir = (1, 1)

    prey = RandomPrey(prey_pos, hunter_pos, hunter_dir, grid)
    is_prey_move = False

    walls_placed = 0
    hunter = RandomHunter(hunter_pos, hunter_dir, prey_pos, MAX_WALLS)

    turn = 0
    cooldown = WALL_COOLDOWN
    while True:

        log("Turn:", turn)
        log("Hunter Position", hunter.position)
        log("Prey Position", prey.position)

        should_remove_wall, wall = hunter.removeWall()
        if should_remove_wall:
            wall_start_pos, wall_direc, wall_length = wall
            grid = remove_wall(wall_start_pos, wall_direc, wall_length, grid)

        if cooldown == 0:
            should_place_wall, wall = hunter.placeWall()
            if should_place_wall and walls_placed < MAX_WALLS:
                wall_start_pos, wall_direc, wall_length = wall
                new_grid, valid = place_wall(hunter_pos, prey_pos, wall_start_pos, wall_direc, wall_length, grid)
                if valid:
                    walls_placed += 1
                    walls.append((wall_start_pos, wall_direc, wall_length))
                    grid = new_grid
                    log(f"Hunter places wall starting at {wall_start_pos} of length {wall_length} in direction {wall_direc}")
                    cooldown = WALL_COOLDOWN
        else:
            cooldown -= 1

        hunter_dir = hunter.direction
        log("Hunter moves in direction:", hunter_dir)
        hunter_pos, new_hunter_dir = get_new_pos(hunter_pos, hunter_dir, grid)
        log("Hunter new position:", hunter_pos)
        hunter_dir = new_hunter_dir

        if is_prey_move:
            prey_dir = prey.getMove()
            log("Prey moves in direction:", prey_dir)
            prey_pos, prey_dir = get_new_pos(prey.position, prey_dir, grid)
            log("Prey new position:", prey_pos)

        if compute_distance(hunter_pos, prey_pos) <= 4:
            break

        turn += 1

        log()
        if is_prey_move:
            is_prey_move = False
        else:
            is_prey_move = True

    print("Hunter caught prey in", turn, "turns")


def get_new_pos(prev, direc, grid):
    new_pos = (prev[0] + direc[0], prev[1] + direc[1])
    # handle bounce
    if grid[new_pos[0]][new_pos[1]] == WALL:
        # if direction is one of the 4 cardinal directions, new pos is just the previous position
        # except the the new direction is now the reverse of the previous direction
        if direc in [(0, 1), (0, -1), (1, 0), (-1, 0)]:
            new_pos = prev
            new_direc = (direc[0] * -1,  direc[1] * -1)
        # handle hard cases
        else:
            # TODO: is this right?

            # consider adjacent pixels
            # +-+-+
            # |*|A|
            # +-+-+
            # |B|↖|
            # +-+-+
            adj_x = grid[new_pos[0]][new_pos[1] + direc[1] * -1] # pixel B
            adj_y = grid[new_pos[0] + direc[0] * -1][new_pos[1]] # pixel A
            # if both A and B are walls, new position is the previous position
            # new direction is -1 * (previous direction)
            if adj_x == WALL and adj_y == WALL:
                return prev, (direc[0] * -1, direc[1] * -1)

            # +-+-+     +-+-+
            # |*| |     |*|↗|
            # +-+-+ ->  +-+-+
            # |*|↖|     |*| |
            # +-+-+     +-+-+
            # x-position remains same, y position changes
            # x-direction is reversed, y-direction is the same
            elif adj_x == WALL and adj_y != WALL:
                return (prev[0], new_pos[1]), (direc[0] * -1, direc[1])

            # +-+-+     +-+-+
            # |*|*|     |*|*|
            # +-+-+ ->  +-+-+
            # | |↖|     |↙| |
            # +-+-+     +-+-+
            # x-position changes, y position remains same
            # x-direction is the same, y-direction is reversed
            elif adj_x != WALL and adj_y == WALL:
                return (new_pos[0], prev[1]), (direc[0], direc[1] * -1)
            else:
                # +-+-+
                # |*| |
                # +-+-+
                # | |↖|
                # +-+-+
                # need to check additional squares...

                # +-+-+-+
                # | |B| |
                # +-+-+-+
                # |A|*| |
                # +-+-+-+
                # | | |↖|
                # +-+-+-+
                adj_new_x = grid[new_pos[0]][new_pos[1] + direc[1]] # B
                adj_new_y = grid[new_pos[0] + direc[0]][new_pos[1]] # A

                # +-+-+-+       +-+-+-+
                # | | | |       | |*| |
                # +-+-+-+       +-+-+-+
                # | |*| |   or  |*|*| |
                # +-+-+-+       +-+-+-+
                # | | |↖|       | | |↖|
                # +-+-+-+       +-+-+-+
                # new position is the same as previous position
                # new direction is the reverse of the previous direction
                if (adj_new_x == WALL and adj_new_y == WALL) or (adj_new_x != WALL and adj_new_y != WALL):
                    return prev, (direc[0] * -1, direc[1] * -1)

                # +-+-+-+       +-+-+-+
                # | |*| |       | |*| |
                # +-+-+-+       +-+-+-+
                # | |*| |   ->  | |*|↗|
                # +-+-+-+       +-+-+-+
                # | | |↖|       | | | |
                # +-+-+-+       +-+-+-+
                # x-position is the same as previous, y-position changes
                # x-direction is reversed, y-direction is the same
                elif adj_new_x == WALL and adj_new_y != WALL:
                    return (prev[0], new_pos[1]), (direc[0] * -1, direc[1])

                # +-+-+-+       +-+-+-+
                # | | | |       | | | |
                # +-+-+-+       +-+-+-+
                # |*|*| |   ->  |*|*| |
                # +-+-+-+       +-+-+-+
                # | | |↖|       | |↙| |
                # +-+-+-+       +-+-+-+
                # x-position changes, y-position is the same as previous
                # x-direction is the same, y-direction is reversed
                elif adj_new_x != WALL and adj_new_y == WALL:
                    return (new_pos[0], prev[1]), (direc[0], direc[1] * -1)

                # if this gets raised... there's a missing case
                else:
                    raise ValueError('Unhandled bounce event.')

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
    placed = []
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
                print("Wall touches another wall!")
                break
            elif place_above == hunter_pos:
                touches_hunter = True
                print("Wall touches hunter!")
                break
            elif place_above == prey_pos:
                print("Wall touches prey!")
                touches_prey = True
                break
            else:
                new_grid[placed[i][0]][placed[i][1] + 1] = WALL

    if touches_hunter or touches_prey or touches_wall:
        return grid, False
    return new_grid, True

if __name__ == "__main__":
    start()