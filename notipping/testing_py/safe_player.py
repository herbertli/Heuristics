from dp_player import DPPlayer
import random

# fixed
BOARDLENGTH = 30  # half the board length
BOARDWEIGHT = 3


class SafePlayer(DPPlayer):

    def receiveGameState(self, state: dict) -> None:
        self.state = state

    def placeBlock(self) -> dict:
        board = self.state['board']
        turn = self.state['current_player']
        weights = self.state['blocks'][turn]
        weight, loc = self.placeable(board, weights)
        return {'weight': weight, 'loc': loc}

    def placeable(self, board, weights):
        w = 1
        smallest = -1
        while (1 << w) <= weights:
            if ((1 << w) & weights) > 0:
                smallest = w
                for i in range(-BOARDLENGTH, BOARDLENGTH):
                    if board[i] == 0:
                        board[i] = w
                        if not self.isGameOver(board):
                            board[i] = 0
                            return w, i
                        board[i] = 0
            w += 1
        return smallest, random.choice([ind for ind in range(-BOARDLENGTH, BOARDLENGTH) if board[ind] == 0])
