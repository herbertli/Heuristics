package edu.nyu.cs.hps.evasion;

import edu.nyu.cs.hps.evasion.game.GameState;

public interface Hunter {

    void receiveGameState(GameState gameState);
    HunterMove playHunter() throws Exception;

}
