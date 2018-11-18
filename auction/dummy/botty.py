import atexit

class BottyClient():
    def __init__(self, name):
        self.name = name

    def receive_init(self, init_status):
        self.artists_num = init_status['artists_types']
        self.required_count = init_status['required_count']
        self.auction_items = init_status['auction_items']
        self.player_count = init_status['player_count']
        self.wealth_table = init_status['wealth_table']
        self.wealth = 100
        # once we have our target, these are the amounts we should bet
        self.bet_list = [0] * self.required_count
        for i in range(self.wealth):
            self.bet_list[i % self.required_count] += 1
        self.target_artist = None
        self.current_round = 0
        self.calculate_target(self.current_round)
        self.obtained_count = 0

    def calculate_target(self, round_num):
        artist_times = dict()
        for i in range(round_num, len(self.auction_items)):
            if self.auction_items[i] not in artist_times:
                artist_times[self.auction_items[i]] = []
            artist_times[self.auction_items[i]].append(i + 1)
        minTime = 10000
        target = None
        for k, v in artist_times.items():
            if len(v) >= self.required_count:
                minTime = min(minTime, v[self.required_count - 1])
                if minTime == v[self.required_count - 1]:
                    target = k
        self.target_artist = target

    def shouldRecalculate(self):
        if self.obtained_count > 0:
            return False
        else:
            return True

    def calculate_bid(self, game_state, wealth, wealth_table):
        if self.auction_items[self.current_round] != self.target_artist:
            return 0
        else:
            return self.bet_list[0]

    def receive_round(self, game_state):
        self.game_state = game_state
        if game_state['bid_winner'] == self.name:
            print("Hey, we won a painting")
            if game_state['bid_item'] != self.target_artist:
                print("But for some reason it was for something we didn't want in the first place")
            else:
                self.bet_list = self.bet_list[1:]
                self.obtained_count += 1
            self.wealth -= game_state['winning_bid']
        elif game_state['bid_item'] == self.target_artist:
            print("We missed our chance :(")
            if self.shouldRecalculate():
                print("Recalculating target...")
                self.calculate_target(self.current_round + 1)
        self.current_round += 1
