package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EvasionClient {

    private SocketClient socket;
    private int port;
    private String name;
    private boolean isHunter;
    GameState gameState;
    private Hunter hunter = null;
    private Prey prey = null;

    private EvasionClient(String name, int port) throws IOException {
        this.name = name;
        this.port = port;
        String host = "127.0.0.1";
        this.socket = new SocketClient(host, port);
    }

    private GameState stringToGame(String msg) {
        List<Integer> temp = Arrays.stream(msg.split(" "))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        Integer[] listMsg = new Integer[temp.size()];
        temp.toArray(listMsg);
        GameState game = new GameState(listMsg[3], listMsg[4]);
        if (isHunter) game.hunterTime = listMsg[0];
        else game.preyTime = listMsg[0];
        game.gameNum = listMsg[1];
        game.ticknum = listMsg[2];
        game.boardSize = new Point(listMsg[5], listMsg[6]);
        game.wallTimer = listMsg[7];
        game.hunterPosAndVel = new PositionAndVelocity(
                new Point(listMsg[8], listMsg[9]),
                new Point(listMsg[10], listMsg[11])
        );
        game.preyPos = new Point(listMsg[12], listMsg[13]);
        game.walls = parseWalls(Arrays.copyOfRange(listMsg, 15, listMsg.length));
        return game;
    }

    private List<Wall> parseWalls(Integer[] partMsg) {
        List<Wall> walls = new ArrayList<>();
        int i = 0;
        while (i < partMsg.length) {
            if (partMsg[i] == 0 || partMsg[i] ==1) {
                if (partMsg[i] == 0)
                    walls.add(new HorizontalWall(partMsg[i + 1], partMsg[i + 2], partMsg[i + 3]));
                else
                    walls.add(new VerticalWall(partMsg[i + 1], partMsg[i + 2], partMsg[i + 3]));
                i += 4;
            } else if (partMsg[i] == 2 || partMsg[i] == 3) {
                if (partMsg[i] == 2)
                    walls.add(new DiagonalWall(partMsg[i + 1], partMsg[i + 2], partMsg[i + 3], partMsg[i + 4], partMsg[i + 5]));
                else
                    walls.add(new CounterDiagonalWall(partMsg[i + 1], partMsg[i + 2], partMsg[i + 3], partMsg[i + 4], partMsg[i + 5]));
                i += 6;
            } else {
                i += 1;
            }
        }
        return walls;
    }

    private String parseHunterMove(GameState gameState, HunterMove move) {
        int wallType = move.wallType;
        if (move.wallsToDel == null || move.wallsToDel.size() == 0) {
            return "" + gameState.gameNum + " " + gameState.ticknum + " " + wallType;
        }
        String wallsToDel = String.join(" ", move.wallsToDel
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList()));
        return "" + gameState.gameNum + " " + gameState.ticknum + " " + wallType + " " + wallsToDel;
    }

    private String parsePreyMove(GameState gameState, PreyMove move) {
        return "" + gameState.gameNum + " " + gameState.ticknum + " " + move.x + " " + move.y;
    }

    private void playGame() throws Exception {
        outer: while (true) {
            String line = this.socket.receive_data().trim();
            String toSend = "";
            switch (line) {
                case "done":
                    break outer;
                case "hunter":
                    this.isHunter = true;
                    break;
                case "prey":
                    this.isHunter = false;
                    break;
                case "sendname":
                    toSend = this.name + this.port;
                    break;
                default:
                    this.gameState = this.stringToGame(line);
                    if (this.isHunter) {
                        // TODO: Initialize hunter according to game variables
                        if (hunter == null) {
                            if (this.gameState.maxWalls < 2) {
                                throw new Exception("... We didn't account for this possibility");
                            } else if (this.gameState.maxWalls == 2) {
                                hunter = new AboveBelowHunter();
//                            } else if (this.gameState.maxWalls <= 4) {
//                                hunter = new RectangularHunter();
                            } else if (this.gameState.maxWalls == 3) {
                                hunter = new StuckWithHunter();
                            } else {
                                hunter = new HalfHunter();
                            }
                        }
                        hunter.receiveGameState(this.gameState);
                        HunterMove move = hunter.playHunter();
                        toSend = this.parseHunterMove(this.gameState, move);
                    } else {
                        // TODO: Initialize prey according to game variables
                        if (prey == null) {
                            if (this.gameState.maxWalls < 10) {
                                prey = new SimplePrey();
                            } else {
                                prey = new CentroidPrey();
                            }
                        }
                        prey.receiveGameState(this.gameState);
                        PreyMove move = prey.playPrey();
                        toSend = this.parsePreyMove(this.gameState, move);
                    }
                    break;
            }
            if (toSend.length() > 0) {
                System.out.println("Sending: " + toSend);
                this.socket.send_data(toSend + "\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java EvasionClient <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new EvasionClient(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }

}
