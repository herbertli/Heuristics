import random
import numpy as np
import copy
import math
from collections import defaultdict


class McBotFaceClient():
    def __init__(self, name):
        self.name = name

    def receive_init(self, init_status):
        self.artists_num = init_status['artists_types']
        self.required_count = init_status['required_count']
        self.auction_items = init_status['auction_items']
        self.player_count = init_status['player_count']
        self.wealth_table = init_status['wealth_table']
        self.wealth = 100
        self.current_round = 0
        self.unique_artists = set(self.auction_items)
        self.player_names = set(self.wealth_table.keys())
        self.default_bid = 2

        # who has what artists
        self.player_items = {name: {artist: [] for artist in self.unique_artists} for name in self.wealth_table}

        # pice history of each artist
        self.items_prices = {artist: [] for artist in self.unique_artists}

    def receive_round(self, game_state):
        self.game_state = game_state
        item = game_state['bid_item']
        winner = game_state['bid_winner']
        game_round = game_state['auction_round']
        winning_bid = game_state['winning_bid']

        self.wealth_table = game_state['wealth_table']
        if winner == self.name:
            self.wealth -= winning_bid

        if game_round != self.current_round:
            print("For some reason we are not synced...")
            exit(1)

        self.player_items[winner][item].append(winning_bid)
        self.current_round += 1

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
                    x = np.array([i for i in range(1, num_of_pics + 1)])
                    strategies[artist_name] = np.poly1d(np.polyfit(x, np.array(pics), 2))
        return new_strategies

    """
    Assumes a worst-case scenario
    """
    def predict_game(self, player_items, item_list):
        place = 1
        for item in item_list:
            still_playing = {name: True for name in self.player_names}
            currently_playing = 0
            for player, is_playing in still_playing:
                currently_playing += 1 if is_playing else 0
            if currently_playing == 1:
                break
        return place

    def generate_my_strategy():
        my_bids = {}
        return

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
        # predicted = self.predict_game()

        strategies = self.get_strategies(self.player_items)
        for name, strats in strategies.items():
            if strats[self.current_item] is None:
                print(f"I can't tell what {name} will bet")
            else:
                num_owned = len(self.player_items[name][self.current_item])
                if num_owned == self.required_count - 1:
                    print(f"I predict {name} will bet {self.wealth_table[name]}")
                else:
                    poly = strats[self.current_item]
                    prediction = math.ceil(poly(num_owned + 1))
                    print(f"I predict {name} will bet {prediction}")

        # predict my place
        # my_place, my_strategy = self.simulate_game()

        # block might be necessary
        # about_to_win, min_bet = self.calculate_block(self.current_item)
        # print("Someone's about to win, blocking...")
        # if about_to_win is not None:
            # predict my place if I block
            # _, new_place = self.simulate_game(predicted)

            # if there's no difference, just do it...
            # if new_place == my_place:
                # return min_bet

        # otherwise just follow the current strategy
        # bid = my_strategy[self.current_item]

        return 0
