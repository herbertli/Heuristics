from clients.submarine_captain_client import SubmarineCaptain

class GoesRightSub(SubmarineCaptain):

    def __init__(self: GoesRightSub, position: int):
        super().__init__("Subby McSubFace")
        self.probedTimes = []
        self.time = 0

    def getMove(self) -> int:
        self.position += 1
        self.time += 1
        return 1

    def hasBeenProbed(self) -> None:
        self.probedTimes.append(self.time)
        return
