import json
from random import randint

from client_abstract_class import Player


class SubmarineCaptain(Player):
    def __init__(self, name):
        super(SubmarineCaptain, self).__init__(
            name=name, is_trench_manager=False)
        game_info = json.loads(self.client.receive_data())
        print('sub', game_info)
        self.m = game_info['m']
        self.L = game_info['L']
        self.position = game_info['pos']

    def play_game(self):
        response = {}
        while True:
            move = self.your_algorithm(
                0 if not response else response['times_probed'])
            self.client.send_data(json.dumps({"move": move}))
            self.position += move
            response = json.loads(self.client.receive_data())
            if 'game_over' in response:
                print("The trench manager's final cost is: ",
                      response['trench_cost'])
                if response['was_condition_achieved']:
                    print("The safety condition was satified.")
                else:
                    print("The safety condition was not satified.")
                exit(0)

    def your_algorithm(self, times_probed):
        """
        You must return an integer between [-1, 1]
        """
        return randint(-1, 1)
