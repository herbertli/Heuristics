# from randomHunter import RandomHunter
from halfHunter import HalfHunter
from randomPrey import RandomPrey
from util import create_grid, place_wall, compute_distance, remove_wall

"""
Main game logic
"""

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

def start():
    grid = create_grid(GRID_SIZE)

    prey_pos = (231, 201)
    hunter_pos = (1, 1)
    hunter_dir = (1, 1)

    prey = RandomPrey(prey_pos, hunter_pos, hunter_dir, grid)
    is_prey_move = False

    walls_placed = 0
    hunter = HalfHunter(hunter_pos, hunter_dir, prey_pos, MAX_WALLS)
    hunter.set_grid(grid)

    turn = 0
    cooldown = WALL_COOLDOWN
    while True:

        log("Turn:", turn)
        log("Hunter Position", hunter.position)
        log("Prey Position", prey.position)

        walls_to_remove, new_wall = hunter.placeAndRemoveWall()
        if len(walls_to_remove) > 0:
            for wall in walls_to_remove:
                wall_start_pos, wall_direc, wall_length = wall
                grid = remove_wall(wall_start_pos, wall_direc, wall_length, grid)
            walls_placed -= len(walls_to_remove)

        if cooldown == 0:
            if new_wall != () and walls_placed < MAX_WALLS:
                wall_start_pos, wall_direc, wall_length = new_wall
                new_grid, valid = place_wall(hunter_pos, prey_pos, wall_start_pos, wall_direc, wall_length, grid)
                if valid:
                    walls_placed += 1
                    walls.append((wall_start_pos, wall_direc, wall_length))
                    grid = new_grid
                    log(f"Hunter places wall starting at {wall_start_pos} of length {wall_length} in direction {wall_direc}")
                    cooldown = WALL_COOLDOWN
        elif cooldown > 0:
            cooldown -= 1

        hunter_dir = hunter.direction
        log("Hunter moves in direction:", hunter_dir)
        hunter_pos, new_hunter_dir = get_new_pos(hunter_pos, hunter_dir, grid)
        log("Hunter new position:", hunter_pos)
        hunter_dir = new_hunter_dir
        prey.receiveHunterPosition(hunter_pos, hunter_dir)
        hunter.updatePosition(hunter_pos, hunter_dir)

        if is_prey_move:
            prey_dir = prey.getMove()
            log("Prey moves in direction:", prey_dir)
            prey_pos, prey_dir = get_new_pos(prey.position, prey_dir, grid)
            log("Prey new position:", prey_pos)
            hunter.receivePreyPosition(prey_pos)
            prey.updatePosition(prey_pos)

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
            return prev, (direc[0] * -1,  direc[1] * -1)
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

                # if this gets raised, there's a missing case
                # ... and there shouldn't be a missing case
                else:
                    raise RuntimeError('Unhandled bounce event.')

    else:
        return new_pos, direc

if __name__ == "__main__":
    start()