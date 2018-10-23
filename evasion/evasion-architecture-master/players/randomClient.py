import socket
import utils
import argparse
import random

HOST = '127.0.0.1'
# dedicated ports for hunter, prey

class Client:
    def __init__(self, name, port, host=HOST):
        self.name = name
        self.host = host
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((host, port))

    def playHunter(self):
        """
        Insert the hunter move logic here
        returns [wallTypeToAdd, wallsToDelete]
        wallTypeToAdd is:
            0 for no wall
            1 for horizontal wall
            2 for vertical wall
            3 for diagnoal wall
            4 for counter diagonal wall
        wallsToDelete is a list of indices(corresponding to self.gameState['walls']) of walls to delete. Can be []
        """
        wallsToDel = []
        for i in range(self.gameState['numWalls']):
            toDel = random.randint(0,30)
            if toDel == 0:
                wallsToDel.append(i)
        # randomly build wall if possible
        wallType = random.randint(0,4)
        if self.gameState['maxWalls'] <= self.gameState['numWalls'] - len(wallsToDel):
            wallType = 0
        return [wallType, wallsToDel]

    def playPrey(self):
        """
        Insert the prey move logic here
        return pair (moveX, moveY)
        """
        x = random.randint(-1,1)
        y = random.randint(-1,1)
        return (x,y)

    def playGame(self):
        stream = ""
        while True:
            stream, line = utils.recv(self.sock, stream)
            toSend = None
            if line == "done":
                break
            elif line == "hunter":
                self.hunter = True
            elif line == "prey":
                self.hunter = False
            elif line == "sendname":
                toSend = self.name + str(self.port)
            else:
                self.gameState = utils.stringToGame(line)
                if self.hunter:
                    move = self.playHunter()
                    toSend = utils.parseHunterMove(self.gameState, move)
                else:
                    move = self.playPrey()
                    toSend = utils.parsePreyMove(self.gameState, move)
            if toSend is not None:
                print "sending: " + toSend
                self.sock.sendall(toSend + "\n")

if __name__== "__main__":
    parser = argparse.ArgumentParser(description='')
    parser.add_argument('--port', type=int, default= 9000, help='port')
    parser.add_argument('--name', type=str, default= "randomPlayer")
    args = parser.parse_args()

    port = args.port
    name = args.name

    player = Client(name, port)
    player.playGame()
