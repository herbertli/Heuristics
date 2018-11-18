import atexit

class BottyClient():
    def __init__(self, name):
        print("Received init.")
        self.name = name

        # once we have our target, these are the amounts we should bet
        self.bet_list = [0] * self.required_count
        for i in range(100):
            self.bet_list[i % self.required_count] += 1
        self.target_artist = None
        self.current_round = 0
        print("Calculating target artist...")
        self.calculate_target(self.current_round)

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
        if self.target_artist is not None:
            return False
        return True

    def check_game_status(self, state):
        if state['finished']:
            exit(0)

    def calculate_bid(self, game_state, wealth, wealth_table):
        if self.auction_items[self.current_round] != self.target_artist:
            return 0
        else:
            return self.bet_list[0]

    def play(self):
        wealth = 100
        while True:
            if self.current_round == 0:
                bid_amt = self.calculate_bid(None, wealth, self.wealth_table)
            else:
                bid_amt = self.calculate_bid(game_state, wealth, game_state['wealth_table'])
            print("Sending bid...")
            client.make_bid(self.auction_items[self.current_round], bid_amt)

            game_state = client.receive_round()
            game_state['remain_time'] = game_state['remain_time'][self.name]

            if game_state['bid_winner'] == name:
                print("Hey, we won a painting")
                if game_state['bid_item'] != self.target_artist:
                    print("But for some reason it was for something we didn't want in the first place")
                else:
                    self.bet_list = self.bet_list[1:]
                wealth -= game_state['winning_bid']
            elif game_state['bid_item'] == self.target_artist:
                print("We missed our chance :(")
                if self.shouldRecalculate():
                    print("Recalculating target...")
                    self.calculate_target(self.current_round + 1)
            self.wealth_table[game_state['bid_winner']] -= game_state['winning_bid']
            self.check_game_status(game_state)
            self.current_round += 1
