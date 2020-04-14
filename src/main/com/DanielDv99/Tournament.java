package com.DanielDv99;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class Battle{
    private Queue<Tournament> tournaments;
    private GamePlayer[] players;

    public Battle(GamePlayer ... players){
        this.players = players;

        tournaments = new LinkedList<>();
        for(int i = 0; i < players.length; i++){
            for(int j = i + 1; j < players.length; j++){
                assert i < j;
                tournaments.add(new Tournament(players[i], players[j]));
            }
        }
    }

    public void playAll(){
        Logger.log("LET THE FIGHT BEGIN!");
        Logger.printSeparator();
        while(!this.tournaments.isEmpty()){
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

        Logger.log(System.lineSeparator() + "Number of won tournaments:" + System.lineSeparator() + winsTable);
    }

    private void playNext(){
        if(this.tournaments.isEmpty()){
            return;
        }

        var nextGame = this.tournaments.remove();
        nextGame.playTournament();
    }
}

public class Tournament {
    private String tournamentID;

    private GamePlayer player1;
    private GamePlayer player2;
    private Environment environment;

    public Tournament(Player p1, String p1Name, Player p2, String p2Name) {
        initialize(new GamePlayer(p1, p1Name), new GamePlayer(p2, p2Name));
    }

    public Tournament(GamePlayer player1, GamePlayer player2){
        initialize(player1, player2);
    }

    protected void initialize(GamePlayer p1, GamePlayer p2){
        this.player1 = p1;
        this.player2 = p2;
        this.environment = new Environment();

        this.tournamentID = player1.getName() + " vs. " + player2.getName();
    }

    private RoundResults playRound(int roundNumber) {

        var xA = environment.getFieldValue(1);
        var xB = environment.getFieldValue(2);
        var xC = environment.getFieldValue(3);

//        Logger.log("Round " + roundNumber + ":");

        var p1LastMove = player1.getPreviousMove();
        var p2LastMove = player2.getPreviousMove();

        var p1Move = this.player1.move(p2LastMove, xA, xB, xC);
        var p2Move = this.player2.move(p1LastMove, xA, xB, xC);

        var scores = GamePlayer.calculateScores(p1Move, p2Move, this.environment);
        double p1Score = scores[0], p2Score = scores[1];

//        var p1Name = this.player1.getName();
//        var p2Name = this.player2.getName();
//        var nameLength = Math.max(p1Name.length(), p2Name.length());
//
//        var format = " %-" + nameLength + "s -> %s -> %.2f";
//        Logger.log(String.format(format, p1Name, Logger.moveToString(p1Move), p1Score));
//        Logger.log(String.format(format, p2Name, Logger.moveToString(p2Move), p2Score));

        this.player1.updateScore(p1Score);
        this.player2.updateScore(p2Score);

        this.environment.updateFields(p1Move, p2Move);

//        Logger.log(String.format("New field scores: %n%s%n", environment));

        return new RoundResults(p1Move, p2Move, p1Score, p2Score, environment.clone());
    }

    public void playTournament() {
        var nRounds = Parameters.TOURNAMENT_ROUNDS;
        var tournamentResults = new RoundResults[nRounds];
        double p1Score = 0, p2Score = 0;

        Logger.printSeparator();

        for (int i = 0; i < nRounds; i++) {
            var tournamentResult = playRound(i + 1);
            p1Score += tournamentResult.p1Score;
            p2Score += tournamentResult.p2Score;
            tournamentResults[i] = tournamentResult;
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

        var tournamentReport = String.format("Tournament %s finished. " +
                        "%nPlayer tournament scores: %n%s%n",
                this.tournamentID, scoresTable);
        Logger.log(tournamentReport);

        // 2 players (moves) + 3 fields values
        var results = new String[tournamentResults.length + 1][5];

        results[0] = new String[] {
                player1.getName(),
                player2.getName(),
                Logger.moveToString(1),
                Logger.moveToString(2),
                Logger.moveToString(3)
        };

        for (int i = 0; i < tournamentResults.length; i++) {
            var currResults = tournamentResults[i];

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

    public String getTournamentID() {
        return tournamentID;
    }

    private static class RoundResults {
        private int p1Move;
        private double p1Score;

        private int p2Move;
        private double p2Score;

        private Environment environment;

        RoundResults(int p1Move, int p2Move, double p1Score, double p2Score, Environment env) {
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
        this.score = 0;
        this.previousMove = 0;
        this.wins = 0;
        player.reset();
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

    public static double[] calculateScores(int p1Move, int p2Move, Environment env) {
        if (p1Move == p2Move) {
            return new double[] {0, 0};
        }
        var p1Score = Utils.calcPlayerScore(env.getFieldValue(p1Move));
        var p2Score = Utils.calcPlayerScore(env.getFieldValue(p2Move));

        return new double[] {p1Score, p2Score};
    }
}