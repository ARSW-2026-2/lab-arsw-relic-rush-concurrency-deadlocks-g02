package edu.eci.arsw.relicrush.game;

import java.util.List;

/**
 * Immutable snapshot handed to the GUI (or any other listener) exactly at the
 * point where GameEngine already knows scoreSum/ledgerTotal/eventCount agree
 * (i.e. right after roundEnd.await() returns). Building and publishing this
 * snapshot does not add any coordination of its own: it simply reads values
 * that are already safe to read at this point in the round, for the same
 * reason the console printout is safe (see Part I, question 4 of the report).
 */
public record RoundSnapshot(
        int round,
        int totalRounds,
        int scoreSum,
        int ledgerTotal,
        int eventCount,
        boolean invariantOk,
        List<PlayerScore> playerScores) {

    public record PlayerScore(int playerId, String name, int score, String status) {
    }
}
