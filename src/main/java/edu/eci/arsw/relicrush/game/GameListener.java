package edu.eci.arsw.relicrush.game;

import edu.eci.arsw.relicrush.model.ForgeStation;

import java.util.List;

/**
 * Observer hook for GameEngine, used by the GUI. All methods are default
 * no-ops so existing console entry points (RelicRushMain, InvariantProbe,
 * DeadlockProbe, LedgerRaceProbe) keep working unchanged without ever
 * registering a listener.
 */
public interface GameListener {

    GameListener NONE = new GameListener() {
    };

    default void onGameStarted(GameConfig config, List<ForgeStation> stations, List<Adventurer> adventurers) {
    }

    default void onRoundCompleted(RoundSnapshot snapshot) {
    }

    /**
     * @param finalSnapshot last snapshot actually completed (may be null if
     *                       the game was stopped before finishing round 1)
     * @param stoppedEarly  true if this game ended because of a Stop request
     *                       rather than by completing every configured round
     */
    default void onGameFinished(RoundSnapshot finalSnapshot, boolean stoppedEarly) {
    }
}
