package com.DanielDv99;

import java.util.LinkedList;
import java.util.Queue;

class Tournament {
    private Queue<Round> rounds;
    private GamePlayer[] players;

    public Tournament(GamePlayer ... players){
        this.players = players;

        rounds = new LinkedList<>();
        for(int i = 0; i < players.length; i++){
            for(int j = i; j < players.length; j++){
                if(i == j){
                    var p1 = players[i];
                    var p2 = p1.getNewCopy();
                    rounds.add(new Round(p1, p2));
                }
                else {
                    rounds.add(new Round(players[i], players[j]));
                }
            }
        }
    }

    public void playAll(){
        Logger.log("LET THE FIGHT BEGIN!");
        Logger.printSeparator();
        while(!this.rounds.isEmpty()){
            this.playNext();
        }

        var names = new String[this.players.length];
        var scores = new String[this.players.length];
        var wins = new String[this.players.length];
        for(int i = 0; i < this.players.length; i++){
            names[i] = this.players[i].getName();
            scores[i] = String.format("%.2f", this.players[i].getScore());
            wins[i] = Integer.toString(this.players[i].getWins());
        }

        var scoreTable = Logger.asTable(names, scores);
        Logger.log("Final scores:" + System.lineSeparator() + scoreTable);

        var winsTable = Logger.asTable(names, wins);

        Logger.log(System.lineSeparator() + "Number of won rounds:" + System.lineSeparator() + winsTable);
    }

    private void playNext(){
        if(this.rounds.isEmpty()){
            return;
        }

        var nextGame = this.rounds.remove();
        nextGame.playRound();
    }
}

public class Round {
    private String roundID;

    private GamePlayer player1;
    private GamePlayer player2;
    private Environment environment;

    public Round(GamePlayer player1, GamePlayer player2){
        initialize(player1, player2);
    }

    protected void initialize(GamePlayer p1, GamePlayer p2){
        this.player1 = p1;
        this.player2 = p2;
        this.environment = new Environment();

        this.roundID = player1.getName() + " vs. " + player2.getName();
    }

    private MoveResults nextMove() {
        var xA = environment.getFieldValue(1);
        var xB = environment.getFieldValue(2);
        var xC = environment.getFieldValue(3);

        var p1LastMove = player1.getPreviousMove();
        var p2LastMove = player2.getPreviousMove();

        var p1Move = this.player1.move(p2LastMove, xA, xB, xC);
        var p2Move = this.player2.move(p1LastMove, xA, xB, xC);

        var scores = GamePlayer.calculateScores(p1Move, p2Move, this.environment);
        double p1Score = scores[0], p2Score = scores[1];

        this.player1.updateScore(p1Score);
        this.player2.updateScore(p2Score);

        this.environment.updateFields(p1Move, p2Move);

        return new MoveResults(p1Move, p2Move, p1Score, p2Score, environment.clone());
    }

    public void playRound() {
        var nRounds = Parameters.MOVES_PER_ROUND;
        var roundResults = new MoveResults[nRounds];
        double p1Score = 0, p2Score = 0;

        this.player1.reset();
        this.player2.reset();

        for (int i = 0; i < nRounds; i++) {
            var tournamentResult = nextMove();
            p1Score += tournamentResult.p1Score;
            p2Score += tournamentResult.p2Score;
            roundResults[i] = tournamentResult;
        }

        if(p1Score > p2Score){
            player1.addWin();
        }
        else if(p2Score > p1Score){
            player2.addWin();
        }

        var names = new String[] {player1.getName(), player2.getName()};
        var scores = new String[] {
                String.format("%.2f", p1Score),
                String.format("%.2f", p2Score)
        };
        var scoresTable = Logger.asTable(names, scores);

        var roundReport = String.format("Round %s finished. " +
                        "%nPlayer round scores: %n%s%n",
                this.roundID, scoresTable);
        Logger.printSeparator();
        Logger.log(roundReport);

        // 2 players (moves) + 3 fields values
        var results = new String[roundResults.length + 1][5];

        results[0] = new String[] {
                player1.getName(),
                player2.getName(),
                Logger.moveToString(1),
                Logger.moveToString(2),
                Logger.moveToString(3)
        };

        for (int i = 0; i < roundResults.length; i++) {
            var currResults = roundResults[i];

            results[i + 1] = new String[]{
                    String.format("%s/%.2f pts", Logger.moveToString(currResults.p1Move), currResults.p1Score),
                    String.format("%s/%.2f pts", Logger.moveToString(currResults.p2Move), currResults.p2Score),
                    Integer.toString(currResults.environment.getFieldValue(1)),
                    Integer.toString(currResults.environment.getFieldValue(2)),
                    Integer.toString(currResults.environment.getFieldValue(3)),
            };
        }

        var reportTable = Logger.asTable(results);

        Logger.log("Tournament summary:\n" + reportTable);
        Logger.printSeparator();
    }

    private static class MoveResults {
        private int p1Move;
        private double p1Score;

        private int p2Move;
        private double p2Score;

        private Environment environment;

        MoveResults(int p1Move, int p2Move, double p1Score, double p2Score, Environment env) {
            this.p1Move = p1Move;
            this.p2Move = p2Move;

            this.p1Score = p1Score;
            this.p2Score = p2Score;

            this.environment = env;
        }
    }
}

class GamePlayer implements Player {
    static final String DEFAULT_PLAYER_NAME = "DanielDv99";

    private Player player;
    private int previousMove;
    private double score;
    private int wins;
    private String name;

    public GamePlayer(Player player, String name) {
        if (name == null) {
            name = DEFAULT_PLAYER_NAME;
        }
        this.name = name;

        this.player = player;
        this.score = 0;
        this.previousMove = 0;
        this.wins = 0;
    }

    public void reset() {
        this.previousMove = 0;
        this.player.reset();
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        this.previousMove = player.move(opponentLastMove, xA, xB, xC);
        return this.previousMove;
    }

    public double getScore() {
        return score;
    }

    public void updateScore(double newScore) {
        this.score += newScore;
    }

    public int getPreviousMove() {
        return this.previousMove;
    }

    public String getName() {
        return this.name;
    }

    public void addWin(){
        this.wins++;
    }

    public int getWins() {
        return wins;
    }

    /**
     * Get a fresh copy of this player.
     * Useful in rounds against themselves
     * @return a new instance with current player
     */
    public GamePlayer getNewCopy(){
        var res = new GamePlayer(this.player, this.name);
        res.reset();

        return res;
    }

    public static double[] calculateScores(int p1Move, int p2Move, Environment env) {
        if (p1Move == p2Move) {
            return new double[] {0, 0};
        }
        var p1Score = calcPlayerScore(env.getFieldValue(p1Move));
        var p2Score = calcPlayerScore(env.getFieldValue(p2Move));

        return new double[] {p1Score, p2Score};
    }

    public static double f(int x) {
        double exponent = Math.exp(x);
        return 10 * exponent / (1 + exponent);
    }

    public static double calcPlayerScore(int fieldScore) {
        return f(fieldScore) - f(0);
    }
}