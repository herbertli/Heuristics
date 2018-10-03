from abstract_no_tipping_player import Player

class COMAPlayer(Player):
    def placeBlock(self) -> dict:
        return {'weight' : 10, 'loc' : 30}
    def removeBlock(self) -> int:
        pass
    def receiveGameState(self, state: dict) -> None:
        # print(state)
        pass