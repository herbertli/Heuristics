from abstract_no_tipping_player import Player

class COMAPlayer(Player):

    def __init__(self):
        # fixed
        self.BOARDLENGTH = 30 # half the board length
        self.BOARDWEIGHT = 3

    def isGameOver(self, board: list) -> bool:
        leftTorque = 0
        rightTorque = 0
        for i in range(-1 * self.BOARDLENGTH, self.BOARDLENGTH + 1):
            if board[i]:
                leftTorque += (i + 3) * board[i]
                rightTorque += (i + 1) * board[i]
        # add torque for initial blocks
        leftTorque += 3 * self.BOARDWEIGHT
        rightTorque += self.BOARDWEIGHT
        return leftTorque < 0 or rightTorque > 0

    def placeBlock(self) -> dict:
        board = self.state['board']
        turn = self.state['current_player']
        weights = self.state['blocks'][turn]
        eweights = self.state['blocks'][turn ^ 1]
        i = 1
        while ((1 << i) <= weights):
            print(1 << i)
            print(weights)
            print((1 << i) <= weights)
            print(((1 << i) & weights) > 0)
            if ((1 << i) & weights) > 0:
                weights ^= (1 << i)
                for j in range(-1 * self.BOARDLENGTH, self.BOARDLENGTH + 1):
                    if board[j] == 0:
                        board[j] = i
                        if(not self.isGameOver(board)):
                            k = 0
                            while ((1 << k) <= eweights):
                                if ((1 << k) & eweights) > 0:
                                    weights ^= (1 << k)
                                    for l in range(-1 * self.BOARDLENGTH, self.BOARDLENGTH + 1):
                                        if board[l] == 0:
                                            board[l] = k
                                            if(not self.isGameOver(board)):
                                                m = 0
                                                while ((1 << m) <= weights):
                                                    if ((1 << m) & weights) > 0:
                                                        weights ^= (1 << m)
                                                        for n in range(-1 * self.BOARDLENGTH, self.BOARDLENGTH + 1):
                                                            if board[n] == 0:
                                                                board[n] = i
                                                                if(not self.isGameOver(board)):
                                                                    return {'weight' : i, 'loc' : j}
                                                                board[n] = 0
                                                        weights ^= (1 << m)
                                                    m += 1
                                            board[l] = 0
                                    weights ^= (1 << k)
                                k += 1
                        board[j] = 0
                weights ^= (1 << i)
            i += 1
        for i in range(-1 * self.BOARDLENGTH, self.BOARDLENGTH + 1):
            if(board[i] == 0):
                print(weights)
                return {'weight' : 100, 'loc' : i}

    def removeBlock(self) -> int:
        pass
        
    def receiveGameState(self, state: dict) -> None:
        self.state = state
        pass