import sys
import random
from sample import SamplePlayer
from botty import BottyClient

players = [SamplePlayer("A"), BottyClient("Botty"), SamplePlayer("C")]
still_playing = [1] * len(players)

num_players = len(players)
init_wealth = 100
num_artists = 10
required_count = 3
item_types = []
bid_winners = dict()
wealth_table = dict()
winner_ind = set()
winner_order = []

def update_playing():
    for p, pos in bid_winners.items():
        counts = dict()
        for a in pos:
            if a not in counts:
                counts[a] = 1
            else:
                counts[a] += 1
        for a, v in counts.items():
            if v == required_count:
                for ind, player in enumerate(players):
                    if player.name == p and ind not in winner_ind:
                        # print(f"Winner: {p}, they have {required_count} copies of artist {a}")
                        still_playing[ind] = 0
                        winner_ind.add(ind)
                        winner_order.append((p, a))
    return None

if __name__ == "__main__":

    num_players = int(sys.argv[1])
    num_artists = int(sys.argv[2])
    required_count = int(sys.argv[3])

    for artist_id in range(num_artists):
        item_id = 't' + str(artist_id)
        item_types.append(item_id)

    auction_items = []

    type_count = len(item_types)

    # Generate a random type of artist
    # 1000 artists in total
    for i in range(1000):
        rand_item = item_types[int(type_count * random.random())]
        auction_items.append(rand_item)

    for p in players:
        wealth_table[p.name] = init_wealth
        bid_winners[p.name] = []

    for p in players:
        game_state = {
            'artists_types': num_artists,
            'required_count': required_count,
            'auction_items': auction_items,
            'player_count': num_players,
            'wealth_table': wealth_table
        }
        p.receive_init(game_state)

    current_round = 0
    game_state = {
        'bid_item': None,
        'bid_winner': None,
        'winning_bid': None,
        'wealth_table': wealth_table
    }

    while True:

        print("======================")
        print("Round:", current_round)
        print("Item:", auction_items[current_round])
        max_bid = -1
        max_bidder = None
        for i, p in enumerate(players):
            if still_playing[i] != 1:
                continue
            wealth = wealth_table[p.name]
            bid = p.calculate_bid(game_state, wealth, game_state['wealth_table'])
            print(f"Player {p.name} bids ${bid}")
            if bid > max_bid:
                max_bid = bid
                max_bidder = p.name

        bid_winners[max_bidder].append(auction_items[current_round])
        wealth_table[max_bidder] -= max_bid
        print(f"Player {max_bidder} wins with ${max_bid}")
        print("New holdings:", bid_winners)
        print("New wealth:", wealth_table)
        print("======================")
        game_state['bid_item'] = auction_items[current_round]
        game_state['bid_winner'] = max_bidder
        game_state['auction_round'] = current_round
        game_state['wealth_table'] = wealth_table
        game_state['winning_bid'] = max_bid

        for i, p in enumerate(players):
            if still_playing[i] != 1:
                continue
            p.receive_round(game_state)

        current_round += 1

        update_playing()

        if sum(still_playing) == 1:
            for i, player in enumerate(players):
                if still_playing[i] == 1:
                    # print(f"Player {player.name}, is in last place")
                    winner_order.append((player.name, "nothing"))
                    break
            break

    for i, v in enumerate(winner_order):
        print(f"Place {i + 1}: {v[0]} with {required_count} copies of artist {v[1]}")
