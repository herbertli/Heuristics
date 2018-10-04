"""
This strategy involves placing the heaviest block I have
as far away as possible during the placement stage

On the remove stage I take away the block that makes leftTorque and rightTorque as close
to each other as possible (hoping the next remove will tip)
"""
import random

from abstract_no_tipping_player import Player

# fixed
BOARDLENGTH = 30  # half the board length
BOARDWEIGHT = 3


class MaxPlayer(Player):

    def placeHeaviest(self, boardLength: int, boardWeight: int, myBlocks: set, blockPlacedAt: list) -> tuple:
        """Finds the heaviest block the player can place and where to place it (starting from farthest)

        Arguments:
            boardLength {int} -- length of the board
            boardWeight {int} -- weight of the
            myBlocks {set} -- set of integer weights denoting the blocks I have available
            blockPlacedAt {list} -- blockPlacedAt[i] = True means there is a block already placed at i

        Returns:
            tuple -- tuple of (blockWeight, position)
        """

        for block in sorted(myBlocks, reverse=True):
            for i in range(-1 * boardLength, 1):
                if blockPlacedAt[i]:
                    continue
                else:
                    newPlacement = blockPlacedAt[:]
                    newPlacement[i] = True
                    gameOver = self.isGameOver(blockPlacedAt)
                    if not gameOver:
                        return block, i
                if blockPlacedAt[-i]:
                    continue
                else:
                    newPlacement = blockPlacedAt[:]
                    newPlacement[-i] = True
                    gameOver = self.isGameOver(blockPlacedAt)
                    if not gameOver:
                        return block, -i
        return random.choice(block), i

    def placeBlock(self) -> dict:
        board = self.state['board']
        turn = self.state['current_player']
        tempWeights = self.state['blocks'][turn]
        weights = set()
        i = 1
        while ((1 << i) <= tempWeights):
            if ((1 << i) & tempWeights) > 0:
                weights.add(i)
            i += 1
        weight, loc = self.placeHeaviest(
            BOARDLENGTH, BOARDWEIGHT, weights, board)
        return {'weight': weight, 'loc': loc}

    def removeable(self, board: list, turns_left: int) -> int:
        smallestDiff = False
        diffInd = -1
        if turns_left < 0:
            return 0
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                temp = board[i]
                board[i] = 0
                if (not self.isGameOver(board)):
                    left, right = self.getTorque(board)
                    smallestDiff = min(smallestDiff, abs(left - right))
                    if not smallestDiff or smallestDiff == abs(left - right):
                        diffInd = i
                board[i] = temp
            return diffInd

    def removeBlock(self) -> int:
        board = self.state['board']
        return self.removeable(board, 2)

    def receiveGameState(self, state: dict) -> None:
        self.state = state
        pass

    def getTorque(self, board: list):
        leftTorque = 0
        rightTorque = 0
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i]:
                leftTorque += (i + 3) * board[i]
                rightTorque += (i + 1) * board[i]
        # add torque for initial blocks
        leftTorque += 3 * BOARDWEIGHT
        rightTorque += BOARDWEIGHT
        return leftTorque, rightTorque

    def isGameOver(self, board: list):
        leftTorque, rightTorque = self.getTorque(board)
        return leftTorque < 0 or rightTorque > 0
