"""
This bot looks ahead "lookahead" moves to make sure the game continues that number of turns
That for the move that the bot is currently playing, both players have a move they can continue to play.
"""

from abstract_no_tipping_player import Player

# fixed
BOARDLENGTH = 30 # half the board length
BOARDWEIGHT = 3

# number of turns to look ahead.
# 0 means to just find anywhere to place that doesn't tip that current turn.
# 1 means to make a move that allows the opponent to make a move on their turn.
# etc.
lookahead = 6

class COMAPlayer(Player):

    def isGameOver(self, board: list) -> bool:
        leftTorque = 0
        rightTorque = 0
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                leftTorque += (i + 3) * board[i]
                rightTorque += (i + 1) * board[i]
        # add torque for initial blocks
        leftTorque += 3 * BOARDWEIGHT
        rightTorque += BOARDWEIGHT
        return leftTorque < 0 or rightTorque > 0

    def placeable(self, board: list, weights: int, eweights: int, turns_left: int) -> tuple:
        if turns_left < 0 or weights == 0:
            return(0, 0)
        i = 1
        while ((1 << i) <= weights):
            if ((1 << i) & weights) > 0:
                weights ^= (1 << i)
                for j in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
                    if board[j] == 0:
                        board[j] = i
                        if(not self.isGameOver(board)):
                                weight, loc = self.placeable(board, eweights, weights, turns_left - 1)
                                if(weight != 100):
                                    return (i, j)
                        board[j] = 0
                weights ^= (1 << i)
            i += 1
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if(board[i] == 0):
                return (100, i)
        

    def placeBlock(self) -> dict:
        board = self.state['board']
        turn = self.state['current_player']
        weights = self.state['blocks'][turn]
        eweights = self.state['blocks'][turn ^ 1]
        weight, loc =  self.placeable(board, weights, eweights, lookahead)
        return {'weight': weight , 'loc': loc}

    def removeable(self, board: list, turns_left: int) -> int:
        if turns_left < 0:
            return 0
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                temp = board[i]
                board[i] = 0
                if(not self.isGameOver(board)):
                        loc = self.removeable(board, turns_left - 1)
                        if(loc != 100):
                            return i
                board[i] = temp
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if(board[i] > 0):
                return i

    def removeBlock(self) -> int:
        board = self.state['board']
        return self.removeable(board, 2)

    def receiveGameState(self, state: dict) -> None:
        self.state = state
        pass