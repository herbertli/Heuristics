import atexit

from client import Client
import time
import random
import sys

class SamplePlayer():

    def __init__(self, name):
        self.name = name

    def calculate_bid(self, game_state, wealth, wealth_table):
        return random.randrange(0, wealth)

    def receive_init(self, init_status):
        self.artists_num = init_status['artists_types']
        self.required_count = init_status['required_count']
        self.auction_items = init_status['auction_items']
        self.player_count = init_status['player_count']
        self.wealth_table = init_status['wealth_table']

    def receive_round(self, round_state):
        self.game_state = round_state
