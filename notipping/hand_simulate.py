"""
input a, the position you want to place a block at
input b, the weight of the block you're placing
input x = 'u' to undo a move
see the leeway, how much you can place at an index without the board tipping.
"""

# fixed
BOARDLENGTH = 30  # half the board length
BOARDWEIGHT = 3

def calculateTorque(board: list) -> list:
    leftTorque = 0
    rightTorque = 0
    for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
        if board[i] > 0:
            leftTorque += (i + 3) * board[i]
            rightTorque += (i + 1) * board[i]
    # add torque for weight of board (like have a weight at position 0)
    leftTorque += 3 * BOARDWEIGHT
    rightTorque += BOARDWEIGHT
    # keep lt positive and rt negative
    return (leftTorque, rightTorque)

def undo(board: list, moves: list) -> None:
    moves.pop()
    pos = moves.pop()
    board[pos] = 0

def main():
    board = [0] * (2 * BOARDLENGTH + 1)
    board[-4] = 3
    moves = []
    while(True):
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            print(i, end='\t')
        print()
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            print(board[i], end='\t')
        print()
        lt, rt = calculateTorque(board)
        """
        if(lt < 0 or rt > 0):
            print("Board has tipped!")
        else:
            print(lt, rt, sep=" ")
        """
        leeway = [0] * (2 * BOARDLENGTH + 1)
        for i in range(-1 * BOARDLENGTH, BOARDLENGTH + 1):
            if board[i] > 0:
                leeway[i] = 0
            else:
                if i < -3:
                    leeway[i] = -lt//(i + 3)
                elif i <= -1:
                    leeway[i] = 99
                else:
                    leeway[i] = -rt//(i + 1)
            print(leeway[i], end='\t')
        print()
        a = input()
        if a == "u":
            undo(board, moves)
        else:
            a = int(a)
            if board[a] != 0:
                print("something is there already")
            elif abs(a) > BOARDLENGTH:
                print("Out of bounds")
            else:
                b = int(input())
                board[a] = b
                moves.append(a)
                moves.append(b)


if __name__ == '__main__':
    main()
