package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.EvasionClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomClient extends EvasionClient {

    RandomClient(String name, int port) throws IOException {
        super(name, port);
    }

    public HunterMove playHunter() throws Exception {
        List<Integer> wallsToDel = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < this.gameState.walls.size(); i++) {
            int toDel = random.nextInt(31);
            if (toDel == 0) {
                wallsToDel.add(i);
            }
        }

        int wallType = random.nextInt(5);
        if (this.gameState.maxWalls <= this.gameState.walls.size() - wallsToDel.size()) {
            wallType = 0;
        }
        return new HunterMove(wallType, wallsToDel);
    }

    public PreyMove playPrey() {
        Random random = new Random();
        return new PreyMove(random.nextInt(3) - 1, random.nextInt(3) - 1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java RandomClient <port> <name>");
        }
        int port = Integer.parseInt(args[0]);
        String name = args[1];
        EvasionClient evasionClient = new RandomClient(name, port);
        evasionClient.playGame();
        evasionClient.socket.close_socket();
    }

}
