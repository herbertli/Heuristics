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


class State:
    def __init__(self, board: list, myblocks: set, theirblocks: set):
        self.board = board
        self.myblocks = myblocks
        self.theirblocks = theirblocks

    def __eq__(self, other):
        return self.board == other.board and self.myblocks == other.myBlocks and self.theirblocks == other.theirblocks


class MaxPlayer(Player):

    def placeHeaviest(self, boardLength: int, boardWeight: int, myBlocks: list, blockPlacedAt: list) -> tuple:
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
            for i in range(-boardLength, 1):
                if blockPlacedAt[i] > 0:
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
        weights = []
        i = 1
        while ((1 << i) <= tempWeights):
            if ((1 << i) & tempWeights) > 0:
                weights.append(i)
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

    @staticmethod
    def getTorque(board: list):
        leftTorque = 0
        rightTorque = 0
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                leftTorque += (i + 3) * board[i]
                rightTorque += (i + 1) * board[i]
        # add torque for initial blocks
        leftTorque += 3 * BOARDWEIGHT
        rightTorque += BOARDWEIGHT
        return leftTorque, rightTorque

    @staticmethod
    def DFSAdd(board, myBlocks, theirBlocks):
        visited = set()
        for myMove in myBlocks:
            for place in range(-BOARDLENGTH, BOARDLENGTH + 1):
                theirNewBlocks = theirblocks.copy()
                myNewBlocks = myBlocks.copy()
                myNewBlocks.remove(myMove)
                newBoard = board.copy()
                newBoard[place] = myMove
                state = State(newBoard, myNewBlocks, theirNewBlocks)
                if state not in visited:
                    if DFS(visited, state, 1, 5):
                        return myMove, place
        return None, None

    @staticmethod
    def DFS(visited: set(), current_state: State, level: int, max_level: int) -> bool:
        visited.add(current_state)

    @staticmethod
    def getMaxCandidate(board: list):
        leftTorque, rightTorque = MaxPlayer.getTorque(board)
        maxes = [False] * len(board)
        for i in range(-BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] == 0:
                for w in range(51):
                    board[i] = w
                    if MaxPlayer.isGameOver(board):
                        board[i] = 0
                        break
                    board[i] = 0
                maxes[i] = w - 1
        return maxes

    @staticmethod
    def getClosestCandidate(board: list):
        maxes = MaxPlayer.getMaxCandidate(board)
        closestInd = False
        closestBlock = -1e9
        minDiff = 1e9
        for i in range(-BOARDLENGTH, BOARDLENGTH + 1):
            if maxes[i]:
                board[i] = maxes[i]
                leftTorque, rightTorque = MaxPlayer.getTorque(board)
                diff = abs(leftTorque - rightTorque)
                if diff < minDiff or (diff == minDiff and maxes[i] > closestBlock):
                    minDiff = diff
                    closestInd = i
                    closestBlock = maxes[i]
                board[i] = 0
        return closestBlock, closestInd

    @staticmethod
    def isGameOver(board: list):
        leftTorque, rightTorque = MaxPlayer.getTorque(board)
        return leftTorque < 0 or rightTorque > 0


if __name__ == "__main__":
    board = [0 for _ in range(61)]
    board[-4] = 3
    print(MaxPlayer.getTorque(board))
    print(MaxPlayer.isGameOver(board))
    print(MaxPlayer.getMaxCandidate(board))
