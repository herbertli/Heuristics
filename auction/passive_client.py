import atexit

from client import Client
import time
import random
import sys

"""
Basically: if there are p players, they will focus on some p artists
We will focus on the other artists, and block the other players :D
"""
class PassiveClient(Client):
    def __init__(self, name, ip, port):
        super().__init__(name, (ip, port))
        self.name = name

        # can be changed
        self.bet_amount = 2
        self.wealth = 100
        self.current_round = 0
        self.target_artist = None
        self.holdings = None

    def preventWin(self, n_artist):
        amount = 0
        for name, poss in self.holdings.items():
            for artist, quantity in poss.items():
                if quantity == self.required_count - 1 and artist == n_artist:
                    amount = max(amount, self.wealth_table[name])
        return amount

    def check_game_status(self, state):
        if state['finished']:
            exit(0)

    def calculate_bid(self, game_state, wealth, wealth_table):
        my_poss = self.holdings[self.name]
        if self.target_artist is not None and self.auction_items[self.current_round] == self.target_artist:
            needed = self.required_count - my_poss[self.target_artist]
            needToPrevent = self.preventWin(self.auction_items[self.current_round])
            if self.wealth - needToPrevent - 1 >= needed:
                return needToPrevent + 1
            else:
                return self.bid_list[0]

        needed = self.required_count - my_poss[self.target_artist]
        needToPrevent = self.preventWin(self.auction_items[self.current_round])
        if needToPrevent > 0:
            print(f"Need to prevent (round {self.current_round})...")
            if self.wealth - needToPrevent - 1 >= needed - 1:
                print(f"Preventing (round {self.current_round})...")
                return needToPrevent + 1
            else:
                return 0

        if self.target_artist is not None:
            return 0
        else:
            return self.bet_amount

    def recalculateAmount(self, item):
        if self.target_artist is None:
            self.target_artist = item
        needed = self.required_count - self.holdings[self.name][item]
        self.bid_list = [0] * needed
        for i in range(self.wealth):
            self.bid_list[i % (needed)] += 1

    def select_best_target(self):
        artist_names = set(self.auction_items)
        item_times = {artist: [] for artist in artist_names}
        for i, v in enumerate(self.auction_items):
            if i < self.current_round:
                continue
            item_times[v].append(i)
        min_win_time = 1001
        best_target = None
        for artist, num_poss in self.holdings[self.name].items():
            needed = self.required_count - num_poss
            win_time = item_times[artist][needed]
            if win_time < min_win_time:
                min_win_time = win_time
                best_target = artist
        return best_target, min_win_time

    def play(self):
        artist_names = set(self.auction_items)
        player_names = self.wealth_table.keys()
        self.holdings = {name: {artist: 0 for artist in artist_names} for name in player_names}
        print(self.holdings)
        while True:
            if self.current_round == 0:
                bid_amt = self.calculate_bid(None, self.wealth, self.wealth_table)
            else:
                bid_amt = self.calculate_bid(game_state, self.wealth, game_state['wealth_table'])

            client.make_bid(self.auction_items[self.current_round], bid_amt)
            game_state = client.receive_round()
            game_state['remain_time'] = game_state['remain_time'][self.name]

            self.holdings[game_state['bid_winner']][game_state['bid_item']] += 1
            self.wealth_table[game_state['bid_winner']] -= game_state['winning_bid']

            if game_state['bid_winner'] == self.name:
                print("Holy crap, we won something")
                self.wealth -= self.wealth_table[self.name]
                new_target_artist, _ = self.select_best_target()
                if self.target_artist != new_target_artist:
                    self.recalculateAmount(new_target_artist)
                elif self.target_artist == new_target_artist:
                    self.bid_list = self.bid_list[1:]

            self.check_game_status(game_state)
            self.current_round += 1


if __name__ == '__main__':

    ip = sys.argv[1]
    port = int(sys.argv[2])
    name = sys.argv[3] if len(sys.argv) == 4 else 'Just a peaceful Botty'

    client = PassiveClient(name, ip, port)
    atexit.register(client.close)

    client.play()
