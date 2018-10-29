package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomHunterPrey implements Hunter, Prey {

    GameState gameState;

    @Override
    public void receiveGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public HunterMove playHunter() {
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

}
