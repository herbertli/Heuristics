from dp_player import DPPlayer

class MRPlayer(DPPlayer):

    def removeBlock(self) -> int:
        board = self.state['board']
        numPlaced = len([_ for i in board if i > 0])
        if numPlaced => 25: 
            return alphabeta_search(state, game)
        else:
            return super().removeBlock()
    
    def minimax_decision(state, game):
        """Given a state in a game, calculate the best move by searching
        forward all the way to the terminal states. [Fig. 6.4]"""

        player = game.to_move(state)

        def max_value(state):
            if game.terminal_test(state):
                return game.utility(state, player)
            v = -infinity
            for (a, s) in game.successors(state):
                v = max(v, min_value(s))
            return v

        def min_value(state):
            if game.terminal_test(state):
                return game.utility(state, player)
            v = infinity
            for (a, s) in game.successors(state):
                v = min(v, max_value(s))
            return v

        # Body of minimax_decision starts here:
        action, state = argmax(game.successors(state), lambda ((a, s)): min_value(s))
        return action
    
    def alphabeta_search(state, game, d=4, cutoff_test=None, eval_fn=None):
        """Search game to determine best action; use alpha-beta pruning.
        This version cuts off search and uses an evaluation function."""

        player = game.to_move(state)

        def max_value(state, alpha, beta, depth):
            if cutoff_test(state, depth):
                return eval_fn(state)
            v = -infinity
            for (a, s) in game.successors(state):
                v = max(v, min_value(s, alpha, beta, depth+1))
                if v >= beta:
                    return v
                alpha = max(alpha, v)
            return v

        def min_value(state, alpha, beta, depth):
            if cutoff_test(state, depth):
                return eval_fn(state)
            v = infinity
            for (a, s) in game.successors(state):
                v = min(v, max_value(s, alpha, beta, depth+1))
                if v <= alpha:
                    return v
                beta = min(beta, v)
            return v

        # Body of alphabeta_search starts here:
        # The default test cuts off at depth d or at a terminal state
        cutoff_test = (cutoff_test or
                    (lambda state,depth: depth>d or game.terminal_test(state)))
        eval_fn = eval_fn or (lambda state: game.utility(state, player))
        action, state = argmax(game.successors(state),
                            lambda ((a, s)): min_value(s, -infinity, infinity, 0))
        return action
    
    def successors(self, state):
        return self.succs.get(state, [])

    def utility(self, state, player):
        if player == 'MAX':
            return self.utils[state]
        else:
            return -self.utils[state]

