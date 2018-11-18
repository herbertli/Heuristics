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
        self.holdings = dict()

    def preventWin(self, n_artist):
        if len(self.holdings.keys()) == 0:
            return 0
        amount = 0
        for p, pos in self.holdings.items():
            for artist, quantity in pos.items():
                if quantity == self.required_count - 1 and artist == n_artist:
                    amount = max(amount, self.wealth_table[p])
        return amount

    def check_game_status(self, state):
        if state['finished']:
            exit(0)

    def calculate_bid(self, game_state, wealth, wealth_table):
        if self.target_artist is not None and self.holdings[self.name][self.target_artist] == self.required_count - 1 and self.auction_items[self.current_round] == self.target_artist:
            print("I really want this...")
            return self.wealth

        needToPrevent = self.preventWin(self.auction_items[self.current_round])
        if needToPrevent > 0:
            print("I really need to prevent this...")
            return needToPrevent
        else:
            print("Ehhh... it's whatever...")
            return self.bet_amount

    def recalculateAmount(self):
        maxRounds = (self.player_count - 1) * (self.required_count - 1) + self.required_count
        self.bet_amount = int(100 / maxRounds)

    def play(self):
        while True:
            if self.current_round == 0:
                bid_amt = self.calculate_bid(None, self.wealth, self.wealth_table)
            else:
                bid_amt = self.calculate_bid(game_state, self.wealth, game_state['wealth_table'])

            client.make_bid(self.auction_items[self.current_round], bid_amt)
            game_state = client.receive_round()
            game_state['remain_time'] = game_state['remain_time'][self.name]

            if game_state['bid_winner'] == name:
                print("Holy crap, we won something")
                self.wealth -= game_state['winning_bid']
                if self.target_artist == None:
                    self.target_artist = game_state['bid_item']
            self.recalculateAmount()

            if game_state['bid_winner'] not in self.holdings:
                self.holdings[game_state['bid_winner']] = {}
            if game_state['bid_item'] not in self.holdings[game_state['bid_winner']]:
                self.holdings[game_state['bid_winner']][game_state['bid_item']] = 1
            else:
                self.holdings[game_state['bid_winner']][game_state['bid_item']] += 1

            self.wealth_table[game_state['bid_winner']] -= game_state['winning_bid']
            self.check_game_status(game_state)
            self.current_round += 1


if __name__ == '__main__':

    ip = sys.argv[1]
    port = int(sys.argv[2])
    name = sys.argv[3] if len(sys.argv) == 4 else 'Just a peaceful Botty'

    client = PassiveClient(name, ip, port)
    atexit.register(client.close)

    client.play()
