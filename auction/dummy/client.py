import json
import socket


class Client():

    def __init__(self, name):
        self.name = name

    def make_bid(self, bid_item, bid_amount):
        return bid_amount

    def receive_round(self, round_status):
        self.game_state = round_status

    def receive_init(self, init_status):
        self.artists_num = init_status['artists_types']
        self.required_count = init_status['required_count']
        self.auction_items = init_status['auction_items']
        self.player_count = init_status['player_count']
        self.wealth_table = init_status['wealth_table']
