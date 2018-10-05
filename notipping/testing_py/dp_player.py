"""
Plays like checks_one_move_ahead until there's "steps" blocks left in the removal phase.
Then does the bitmask dp, and removes based on dp.
dp[i] == 100 if that state will tip (before any blocks are removed)
dp[i] == -100 if you lose no matter what block you pick from that state
otherwise, dp[i] == j, where j is the block you should take to win
"""

from checks_one_move_ahead_player import COMAPlayer

# fixed
BOARDLENGTH = 30 # half the board length
BOARDWEIGHT = 3

lookahead = 4
DEFAULT_STEPS = 20

class DPPlayer(COMAPlayer):

    def __init__(self):
        self.blocksRemCount = None
        self.blocksRemIndex = []
        self.blocksRemWeight = []
        self.dp = None
        self.curMask = 0
        self.steps = DEFAULT_STEPS

    def updateMask(self, board):
        # print(bin(self.curMask))
        # print(board)
        # print(len(self.blocksRemIndex))
        # print(self.blocksRemIndex)
        # print(self.blocksRemWeight)
        for i in range(len(self.blocksRemIndex)):
            j = self.blocksRemIndex[i]
            if board[j] > 0:
                self.curMask |= (1 << i)
            else:
                self.curMask &= ~(1 << i)
        # print(bin(self.curMask))
            

    def preprocess(self, board):
        # set up blocks rem
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                self.blocksRemIndex.append(i)
                self.blocksRemWeight.append(board[i])
        self.curMask = (1 << self.steps) - 1
        # set up dp
        self.dp = []
        self.dp.append(100)
        for mask in range(1, (1 << self.steps)):
            # print(bin(mask))
            leftTorque = 0
            rightTorque = 0
            i = 0
            while (1 << i) <= mask:
                if ((1 << i) & mask) > 0:
                    leftTorque += (self.blocksRemIndex[i] + 3) * self.blocksRemWeight[i]
                    rightTorque += (self.blocksRemIndex[i] + 1) * self.blocksRemWeight[i]
                i += 1
            # add torque for initial blocks
            leftTorque += 3 * BOARDWEIGHT
            rightTorque += BOARDWEIGHT
            if leftTorque < 0 or rightTorque > 0:
                self.dp.append(100)
            else:
                for i in range(self.steps):
                    if ((1 << i) & mask) and self.dp[mask ^ (1 << i)] == -100:
                        self.dp.append(self.blocksRemIndex[i])
                        break
                if len(self.dp) == mask: # losing state
                    self.dp.append(-100)
            # print("{}: {} {} {}".format(bin(mask), self.dp[mask], leftTorque, rightTorque))

    def removeBlock(self) -> int:
        board = self.state['board']
        if self.blocksRemCount is None:
            self.blocksRemCount = 0
            for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
                if board[i] > 0:
                    self.blocksRemCount += 1
        else:
            self.blocksRemCount -= 2
        if self.blocksRemCount <= self.steps:
            if self.dp is None:
                self.steps = self.blocksRemCount
                self.preprocess(board)
            self.updateMask(board)
            # print(bin(self.curMask))
            if self.dp[self.curMask] == -100:
                # print("I think I lose.")
                # do a move that doesn't lose immediately
                for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
                    if board[i] > 0:
                        temp = board[i]
                        board[i] = 0
                        if not self.isGameOver(board):
                            return i
                        board[i] = temp
                # do any move
                # print("There is no safe block.")
                for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
                    if board[i] > 0:
                        return i
            else:
                # do what dp says
                return self.dp[self.curMask]
        else:
            return self.removeable(board, 0) # make any none tipping move
                    

    def receiveGameState(self, state: dict) -> None:
        self.state = state
        pass