"""
Plays like COMAPlayer for the remove stage
For add stage, tries to win by forcing opponent into an unwinnable state
"""

from checks_one_move_ahead_player import COMAPlayer

# fixed
BOARDLENGTH = 30  # half the board length
BOARDWEIGHT = 3


class TempPlayer(COMAPlayer):

    # meaning that the player sees if it can force a win in at most _ moves
    # from the current state
    lookAhead = 3

    def placeBlock(self) -> dict:
        board = self.state['board']
        turn = self.state['current_player']
        weights = self.state['blocks'][turn]
        eweights = self.state['blocks'][turn ^ 1]

        weight, loc = self.forceable(board, weights, eweights)
        if weight and loc:
            return {'weight': weight, 'loc': loc}

        weight, loc = self.placeable(board, weights, eweights, lookahead)
        return {'weight': weight, 'loc': loc}

    """
    Keys are tuples: (board, myBlocks, theirBlocks)
    Values are: -1 definitely a losing state
                0 not sure
                1 definitely a winning state
    """
    visited = {}

    def forceable(self, board, weights, eweights):
        self.preprocess(board, weights, eweights)
        i = 1
        while ((1 << i) <= weights):
            if ((1 << i) & weights) > 0:
                weights ^= (1 << i)
                for j in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
                    if board[j] == 0:
                        board[j] = i
                        if self.visited[(','.join([str(_) for _ in board]), eweights, weights)] == -1:
                            return i, j
                        board[j] = 0
                weights ^= (1 << i)
            i += 1
        return None, None

    """
    Returns true if anywhere you place a block, you are guarenteed to lose
    """

    def preprocess(self, board, myBlocks, eBlocks, depth=0):
        if myBlocks == 0 or eBlocks == 0:
            return
        if depth == self.lookAhead:
            self.visited[(','.join([str(_) for _ in board]),
                          myBlocks, eBlocks)] = 0
        if (','.join([str(_) for _ in board]), myBlocks, eBlocks) in self.visited:
            return self.visited[(','.join([str(_) for _ in board]), myBlocks, eBlocks)]
        if self.isGameOver(board):
            self.visited[(','.join([str(_) for _ in board]),
                          myBlocks, eBlocks)] = -1
            return self.visited[(','.join([str(_) for _ in board]), myBlocks, eBlocks)]
        for i in range(-BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] == 0:
                w = 1
                while ((1 << w) <= myBlocks):
                    if ((1 << w) & myBlocks) > 0:
                        myBlocks ^= (1 << w)
                        board[i] = w
                        res = self.preprocess(
                            board, eBlocks, myBlocks, depth + 1)
                        board[i] = 0
                        if res == 1:
                            self.visited[(','.join([str(_) for _ in board]),
                                          myBlocks, eBlocks)] = -1
                            return -1
                        myBlocks ^= (1 << w)
                    w += 1
        self.visited[(','.join([str(_) for _ in board]),
                      myBlocks, eBlocks)] = 1
        return 1

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
