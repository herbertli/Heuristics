# No Tipping

## Setup:

Given a uniform, flat board 60 meters long and weighing 3 kilograms, consider it ranging from -30 meters to 30 meters. So the center of gravity is at 0. We place two supports of equal heights at positions -3 and -1 and a 3 kilogram block at position -4.

## Rules:
1. Two players each start with k blocks having weights 1 kg through k kg where 2k is less than 50. 
2. The first player places one block anywhere on the board, then the second player places one block anywhere on the board
3. Play alternates with each player placing one block until the second player places his or her last block.
4. If after any play, the placement of a block causes the board to tip, then the player who did that play loses.
5. Then the players remove one block at a time in turns. 
6. At each play, each player may remove a block placed by any player or the initial block. If the board tips following a removal, then the player who removed the last block loses.

## Details:

* The torque is computed by weight times the distance to each support. 
* Clockwise is negative torque and counterclockwise is positive torque. 
* You want the net torque on the left support to be negative and the net torque on the right support to be positive. 

## Tip Check (taken from game architecture):

```python
def isGameOver():
    leftTorque = 0
    rightTorque = 0
    for i in range(-1 * boardLength, boardLength + 2):
        if blockedPlacedAt[i]:
            leftTorque += (i + 3) * blockPlacedAt[i]
            rightTorque += (i + 1) * blockPlacedAt[i]
    # add torque for initial blocks
    leftTorque += 3 * boardWeight
    rightTorque += boardWeight
    return leftTorque < 0 or rightTorque > 0
```