import json
import time

def parseWalls(partmsg):
    walls = []
    i = 0
    while i < len(partmsg):
        if partmsg[i] == 0 or partmsg[i] == 1:
            walls.append(partmsg[i:i+4])
            i += 4
        elif partmsg[i] == 2 or partmsg[i] == 3:
            walls.append(partmsg[i:i+6])
            i += 6
        else:
            i += 1
    return walls

def stringToGame(msg):
    listMsg = map(int, msg.split(" "))
    game = {
            "playerTimeLeft" : listMsg[0],
            "gameNum" : listMsg[1],
            "tickNum" : listMsg[2],
            "maxWalls" : listMsg[3],
            "wallPlacementDelay" : listMsg[4],
            "boardsizeX" : listMsg[5],
            "boardsizeY" : listMsg[6],
            "currentWallTimer" : listMsg[7],
            "hunterXPos" : listMsg[8],
            "hunterYPos" : listMsg[9],
            "hunterXVel" : listMsg[10],
            "hunterYVel" : listMsg[11],
            "preyXPos" : listMsg[12],
            "preyYPos" : listMsg[13],
            "numWalls" : listMsg[14],
            "walls" : parseWalls(listMsg[15:])
            }
    return game

def recv(sock, stream):
    while True:
        stream = stream + sock.recv(4096)
        lines = stream.split("\n")
        if len(lines) > 1:
            line = lines[-2]
            stream = lines[-1]
            break
        else:
            continue
    print "received: " + line
    val = .01
    time.sleep(val)
    return stream, line

def parseHunterMove(gameState, move):
    wallType = str(move[0])
    wallsToDel = " ".join(str(x) for x in move[1])
    if wallsToDel != " ":
        return str(gameState['gameNum']) + " " + str(gameState['tickNum'])  + " " + wallType + " " + wallsToDel
    return str(gameState['gameNum']) + " " + str(gameState['tickNum'])  + " " + wallType

def parsePreyMove(gameState, move):
    return str(gameState['gameNum']) + " " + str(gameState['tickNum'])  + " " + str(move[0]) + " " + str(move[1])
