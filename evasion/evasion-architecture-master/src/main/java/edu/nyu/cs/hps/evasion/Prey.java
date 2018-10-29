package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.GameState;

public interface Prey {

    void receiveGameState(GameState gameState);
    PreyMove playPrey() throws Exception;

}
