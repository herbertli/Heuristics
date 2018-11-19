import sys
import random
import numpy as np
import math
import atexit
import warnings
from collections import defaultdict
from client import Client

warnings.simplefilter('ignore', np.RankWarning)

"""
Yeah this doesn't work... and I don't feel like working on it anymore T_T
"""
class McBotFaceClient(Client):
    def __init__(self, name, ip, port):
        super().__init__(name, (ip, port))
        self.name = name
        self.wealth = 100
        self.current_round = 0

        self.unique_artists = None
        self.player_names = None
        self.default_bid = 2
        self.player_items = None
        self.item_prices = None

    def check_game_status(self, state):
        if state['finished']:
            exit(0)

    def calculate_block(self, current_item):
        for name, artists in self.player_items.items():
            freq = defaultdict(int)
            for artist in artists:
                freq[artist] += 1
            for artist, artist_freq in freq.items():
                if artist_freq == self.required_count and current_item == artist:
                    return name, self.wealth_table[name]
        return None, 0

    """
    params:
        player_items = {
            player_name: {
                artist_name: [bids]
            }
        }
    returns:
        player_strat = {
            player_name: {
                artist_name: np.poly1d
            }
        }
        everyone has a strategy besides us
    """
    def get_strategies(self, player_items):
        new_strategies = {player_name: {artist_name: None for artist_name in self.unique_artists} for player_name in self.player_names}
        for player_name, strategies in new_strategies.items():
            if player_name == self.name:
                continue
            for artist_name, artist_strategy in strategies.items():
                pics = player_items[player_name][artist_name]
                num_of_pics = len(pics)
                if num_of_pics == 0:
                    strategies[artist_name] = None
                else:
                    x = np.array([i for i in range(num_of_pics)])
                    strategies[artist_name] = np.poly1d(np.polyfit(x, np.array(pics), 2))
        return new_strategies

    def simulate_game(self, my_bid=None, n_rounds=1000):
        best_place = self.player_count
        best_win_time = 1001
        best_strategy = None
        for i in range(n_rounds):
            if my_bid is None:
                place, strategy, win_time = self.run_game(self.current_round, self.player_items, bound=best_win_time)
            else:
                place, strategy, win_time = self.run_game(self.current_round + 1, self.player_items, bound=best_win_time - 1)
            if place < best_place:
                best_place = place
                best_win_time = win_time
                best_strategy = strategy
            elif place == best_place and win_time < best_win_time:
                best_win_time = win_time
                best_strategy = strategy
        return best_place, best_strategy

    def get_playing(self, player_items):
        is_playing = {name: True for name in self.player_names}
        for name, coll in player_items.items():
            for artist, bids in coll.items():
                if len(bids) == self.required_count:
                    is_playing[name] &= False
        return is_playing

    def calculate_bid(self, game_state, wealth, wealth_table):
        bid = self.default_bid
        self.current_item = self.auction_items[self.current_round]

        # predict strategies for current round
        strategies = self.get_strategies(self.player_items)
        predicted_bids = {name: 0 for name in self.player_names}
        for name, strats in strategies.items():
            if strats[self.current_item] is not None:
                num_owned = len(self.player_items[name][self.current_item])
                if num_owned == self.required_count - 1:
                    predicted_bids[name] = self.wealth_table[name]
                else:
                    poly = strats[self.current_item]
                    predicted_bids[name] = min(self.wealth_table[name], math.ceil(self.wealth_table[name] * poly(num_owned) / 100))
        print("My predictions for this round:", predicted_bids)

        # simulate and get the best strategy
        # best_place, my_strategy = self.simulate_game(n_rounds=1000)

        # block might be necessary
        # about_to_win, min_bet = self.calculate_block(self.current_item)
        # if about_to_win is not None:
        #     print("Someone's about to win, blocking...")
        #     # predict my place if I block
        #     block_place, block_strategy = self.simulate_game(my_bid=min_bet + 1, n_rounds=1000)

        # # if there's no difference, just do it...
        # # otherwise just follow the current strategy
        # if best_place == block_place:
        #     self.my_strategy = block_strategy
        # else:
        #     self.my_strategy = my_strategy

        # return self.my_strategy[0]
        return 0

    def play(self):

        self.unique_artists = set(self.auction_items)
        self.player_names = set(self.wealth_table.keys())

        # who has what artists
        self.player_items = {name: {artist: [] for artist in self.unique_artists} for name in self.wealth_table}

        # pice history of each artist
        self.artist_prices = {artist: [] for artist in self.unique_artists}

        while True:
            if self.current_round == 0:
                bid_amt = self.calculate_bid(None, self.wealth, self.wealth_table)
            else:
                bid_amt = self.calculate_bid(game_state, self.wealth, game_state['wealth_table'])
            client.make_bid(self.auction_items[self.current_round], bid_amt)
            game_state = client.receive_round()
            game_state['remain_time'] = game_state['remain_time'][self.name]
            if game_state['bid_winner'] == self.name:
                self.wealth -= game_state['winning_bid']

            proportion = float(game_state['winning_bid']) / float(self.wealth_table[game_state['bid_winner']]) * 100.0
            self.player_items[game_state['bid_winner']][self.current_item].append(proportion)
            self.wealth_table[game_state['bid_winner']] -= game_state['winning_bid']
            self.artist_prices[self.current_item].append(game_state['winning_bid'])

            self.check_game_status(game_state)
            self.current_round += 1

if __name__ == '__main__':

    ip = sys.argv[1]
    port = int(sys.argv[2])
    name = sys.argv[3] if len(sys.argv) == 4 else 'Just a peaceful Botty'

    client = McBotFaceClient(name, ip, port)
    atexit.register(client.close)

    client.play()
