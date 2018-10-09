from dp_player import DPPlayer

infinity = 1.0e400
BOARDLENGTH = 30


def argmin(seq, fn):
    """Return an element with lowest fn(seq[i]) score; tie goes to first one.
    >>> argmin(['one', 'to', 'three'], len)
    'to'
    """
    best = seq[0]
    best_score = fn(best)
    for x in seq:
        x_score = fn(x)
        if x_score < best_score:
            best, best_score = x, x_score
    return best


def argmax(seq, fn):
    """Return an element with highest fn(seq[i]) score; tie goes to first one.
    >>> argmax(['one', 'to', 'three'], len)
    'three'
    """
    return argmin(seq, lambda x: -fn(x))


class MRPlayer(DPPlayer):

    def removeBlock(self) -> int:
        board = self.state['board']
        numPlaced = len([i for i in board if i > 0])
        if numPlaced >= 25:
            block = self.alphabeta_search(board)
            print("BLOCK:", block)
            return block
        else:
            return super().removeBlock()

    def alphabeta_search(self, board, d=3, cutoff_test=None, eval_fn=None):
        """Search game to determine best action; use alpha-beta pruning.
        This version cuts off search and uses an evaluation function."""

        def max_value(board, alpha, beta, depth):
            if cutoff_test(board, depth):
                return eval_fn(board)
            v = -infinity
            for (a, s) in self.successors(board):
                v = max(v, min_value(s, alpha, beta, depth+1))
                if v >= beta:
                    return v
                alpha = max(alpha, v)
            return v

        def min_value(board, alpha, beta, depth):
            if cutoff_test(board, depth):
                return eval_fn(board)
            v = infinity
            for (a, s) in self.successors(board):
                v = min(v, max_value(s, alpha, beta, depth+1))
                if v <= alpha:
                    return v
                beta = min(beta, v)
            return v

        # Body of alphabeta_search starts here:
        # The default test cuts off at depth d or at a terminal state
        cutoff_test = (cutoff_test or
                       (lambda board, depth: depth > d or self.terminal_test(board)))
        eval_fn = eval_fn or (lambda board: self.utility(board))
        action, board = argmax(self.successors(
            board), lambda t: min_value(t[1], -infinity, infinity, 0))
        return action

    def successors(self, board):
        succs = []
        for i in range(-BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                copied = board.copy()
                copied[i] = 0
                if not self.isGameOver(copied):
                    succs.append((i, copied))
        return succs

    def utility(self, board):
        return -1 if self.isGameOver(board) else 1

    def terminal_test(self, board):
        return self.isGameOver(board)
