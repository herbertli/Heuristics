"""
Plays like COMAPlayer for the remove stage
For add stage, tries to win by forcing opponent into an unwinnable state
"""

from checks_one_move_ahead_player import COMAPlayer

# fixed
BOARDLENGTH = 30  # half the board length
BOARDWEIGHT = 3
LOOKAHEAD = 3


class TempPlayer(COMAPlayer):

    # meaning that the player sees if it can force a win in at most _ moves
    # from the current state

    def placeBlock(self) -> dict:
        board = self.state['board']
        occ = [_ for _ in board if _ > 0]
        turn = self.state['current_player']
        weights = self.state['blocks'][turn]
        eweights = self.state['blocks'][turn ^ 1]

        if len(occ) > 30:
            weight, loc = self.forceable(board, weights, eweights)
            if weight and loc:
                print("Found winning move:", weight, loc)
                return {'weight': weight, 'loc': loc}

        weight, loc = self.placeable(board, weights, eweights, 6)
        return {'weight': weight, 'loc': loc}

    """
    Keys are tuples: (board, myBlocks, theirBlocks)
    Values is a tuple (weight, loc): 
        weight < 0 -> definitely a losing state, place at loc
        weight = 0 -> not sure
        weight > 0 -> definitely a winning state, place at loc
    """
    visited = {}

    def forceable(self, board, weights, eweights):
        weight, loc = self.dfs(tuple(board), weights, eweights, 0)
        if weight > 0:
            return weight, loc
        return None, None

    def dfs(self, board, myBlocks, eBlocks, depth):
        # print(board, myBlocks, eBlocks)
        if depth == LOOKAHEAD:
            self.visited[(board, myBlocks, eBlocks)] = (-1, -1)
            return (0, -1)
        if (tuple(board), myBlocks, eBlocks) in self.visited:
            return self.visited[(board, myBlocks, eBlocks)]

        haveMove = False
        for i in range(-BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] == 0:
                w = 1
                while ((1 << w) <= myBlocks):
                    if ((1 << w) & myBlocks) > 0:
                        copyB = list(board)
                        copyB[i] = w
                        if not self.isGameOver(copyB):
                            emove, eloc = self.dfs(
                                tuple(copyB), eBlocks, myBlocks ^ (1 << w), depth + 1)
                            # opponent has a losing state, I should go there
                            if emove < 0:
                                self.visited[(board, myBlocks, eBlocks)] = (
                                    w, i)
                                return w, i
                            elif emove == 0:
                                haveMove = (w, i)
                    w += 1

        if not haveMove:
            self.visited[(board, myBlocks, eBlocks)] = (-1, -1)
            return (-1, -1)
        else:
            self.visited[(board, myBlocks, eBlocks)] = (0, 0)
            return (0, 0)

    def isGameOver(self, board: list) -> bool:
        leftTorque = 0
        rightTorque = 0
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                leftTorque += (i + 3) * board[i]
                rightTorque += (i + 1) * board[i]
        leftTorque += 3 * BOARDWEIGHT
        rightTorque += BOARDWEIGHT
        return leftTorque < 0 or rightTorque > 0

    def receiveGameState(self, state: dict) -> None:
        self.state = state
