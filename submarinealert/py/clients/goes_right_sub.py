class GoesRightSub():

    def __init__(self: GoesRightSub, position: int):
        self.position = position
        self.probedTimes = []
        self.time = 0

    def getMove(self) -> int:
        self.position += 1
        self.time += 1
        return 1

    def hasBeenProbed(self) -> None:
        self.probedTimes.append(self.time)
        return
