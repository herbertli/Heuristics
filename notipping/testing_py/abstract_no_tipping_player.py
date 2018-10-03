import abc

class Player(object, metaclass=abc.ABCMeta):
    @abc.abstractmethod
    def placeBlock(self) -> dict:
        raise NotImplementedError('placeBlock not defined')
    @abc.abstractmethod
    def removeBlock(self) -> int:
        raise NotImplementedError('removeBlock not defined')
    """
    Returns a dict: int stage, int currentPlayer, int[] board, 
                    int b0, int b1, int winner
    stage = 0 if block placing phase, 1 if block removal phase
    currentPlayer = 0 if current player is first player, else 1
    board[i] = weight placed at i
    b0 |= i > 0 if player_0 still has block of weight i in hand
    b1 |= i > 0 if player_1 still has block of weight i in hand 
    """
    @abc.abstractmethod
    def receiveGameState(self, state: tuple) -> None:
        raise NotImplementedError('receiveGameState not defined')