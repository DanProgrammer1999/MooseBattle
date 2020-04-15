import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


class Parameters {
    static final int MOVES_PER_ROUND = 20;

    static final boolean IS_TESTING = true;
    static final long TESTING_SEED = 42;

    static final boolean LOG_ENABLED = true;
}

interface Player {
    void reset();

    int move(int opponentLastMove, int xA, int xB, int xC);
}

class RandomPlayer implements Player {
    private Random generator;

    public RandomPlayer() {
        reset();
    }

    public void reset() {
        if (Parameters.IS_TESTING) {
            this.generator = new Random(Parameters.TESTING_SEED);
        }
        else {
            this.generator = new Random();
        }
    }

    public int move(int opponentLastMove, int xA, int xB, int xC) {
        return generator.nextInt(3) + 1;
    }
}

class GreedyPlayer implements Player {
    public void reset() {
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        if (xA >= xB && xA >= xC) {
            return 1;
        }

        else if (xB >= xA && xB >= xC) {
            return 2;
        }
        else {
            return 3;
        }
    }
}

class CopycatPlayer implements Player {
    public void reset() { }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        if(opponentLastMove == 0){
            var rand = new Random();
            if(Parameters.IS_TESTING){
                rand.setSeed(Parameters.TESTING_SEED);
            }

            return rand.nextInt(3) + 1;
        }

        return opponentLastMove;
    }
}

class LingeringPlayer implements Player {
    private static GreedyPlayer greedy = new GreedyPlayer();
    private int myLastMove;

    public LingeringPlayer() {
        myLastMove = 1;
    }

    public void reset() {
        myLastMove = 1;
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        var fieldScores = new int[] {xA, xB, xC};
        int move;
        int myFieldScore = fieldScores[myLastMove - 1];

        if(myFieldScore == 0){
            move = greedy.move(opponentLastMove, xA, xB, xC);
        }
        else{
            move = myLastMove;
        }

        myLastMove = move;
        return move;
    }
}

class SequentialPlayer implements Player {
    private int previousMove;

    public SequentialPlayer() {
        reset();
    }

    public void reset() {
        this.previousMove = 0;
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        // next position
        // 0 % 3 + 1 = 0 + 1 = 1
        // 1 % 3 + 1 = 1 + 1 = 2
        // 2 % 3 + 1 = 2 + 1 = 3
        // 3 % 3 + 1 = 0 + 1 = 1
        // ... (also works in general case, with n instead of 3)
        this.previousMove = this.previousMove % 3 + 1;
        return this.previousMove;
    }
}

class PreviousFieldPlayer implements Player {
    private int myLastMove;
    private Random generator;

    public PreviousFieldPlayer() {
        reset();
    }

    public void reset() {
        myLastMove = 0;
        this.generator = new Random();
        if(Parameters.IS_TESTING){
            this.generator.setSeed(Parameters.TESTING_SEED);
        }
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        var myMove = getPreviousMove(opponentLastMove);
        if(myMove == myLastMove){
            myMove = getPreviousMove(myMove);
        }

        myLastMove = myMove;

        return myMove;
    }

    private int getPreviousMove(int move){
        var nextMove = move - 1;
        if(nextMove == -1){
            // first move
            nextMove = generator.nextInt(3) + 1;
        }
        else if(nextMove == 0){
            nextMove = 3;
        }

        return nextMove;
    }
}

class RandomDirectionMovePlayer implements Player {
    private Random generator;

    public RandomDirectionMovePlayer(){
        reset();
    }

    @Override
    public void reset() {
        if(Parameters.IS_TESTING){
            this.generator = new Random(Parameters.TESTING_SEED);
        }
        else {
            this.generator = new Random();
        }
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        int directionChoice = generator.nextInt(2);
        int myMove;

        // previous
        if(directionChoice == 0){
            myMove = opponentLastMove - 1;
            if(myMove <= 0){
                myMove = 3;
            }
        }
        // next
        else {
            myMove = opponentLastMove + 1;
            if(myMove == 4){
                myMove = 1;
            }

        }

        return myMove;
    }
}

class Logger {
    public static void log(String text) {
        if (Parameters.LOG_ENABLED) {
            System.out.println(text);
        }
    }

    public static String asTable(String[] headers, String[] values) {
        return asTable(new String[][] {headers, values});
    }

    public static String asTable(String[][] rows) {
        if (rows.length == 0) {
            return "";
        }

        // Assume that ALL rows are of equal length
        var widths = new int[rows[0].length];

        for (String[] strings : rows) {
            for (int col = 0; col < strings.length; col++) {
                widths[col] = Math.max(widths[col], strings[col].length());
            }
        }

        var res = new StringBuilder();

        for (String[] row : rows) {
            res.append("|");
            for (int j = 0; j < row.length; j++) {
                res.append(String.format(" %-" + widths[j] + "s", row[j])).append(" |");
            }
            res.append("\n");
        }

        // delete last '\n'
        res.deleteCharAt(res.length() - 1);
        return res.toString();
    }

    public static void printSeparator() {
        if (Parameters.LOG_ENABLED) {
            log(System.lineSeparator() + "-".repeat(50));
        }
    }

    /**
     * Convert move from integer to string
     * @param move A possible move in a range of (1 .. 3)
     * @return String representation of the given move
     */
    public static String moveToString(int move) {
        switch (move) {
            case 1:
                return "A";
            case 2:
                return "B";
            case 3:
                return "C";
            default:
                return "ERROR";
        }
    }
}

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

class Round {
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

class Environment {
    private Field[] fields;

    public Environment() {
        initialize(3);
    }

    private Environment(Field[] fields) {
        this.fields = fields;
    }

    // Shared code for several constructors
    private void initialize(int nFields) {
        this.fields = new Field[nFields];
        for (int i = 0; i < nFields; i++) {
            this.fields[i] = new Field();
        }
    }

    public Field getFieldByIndex(int index) {
        // decrement index because fields indexing starts with 1, as per task description
        return fields[index - 1];
    }

    public int getFieldValue(int index) {
        var field = this.getFieldByIndex(index);
        return field.getValue();
    }

    public void updateFields(int player1Move, int player2Move) {
        if (player1Move == player2Move) {
            getFieldByIndex(player1Move).decreaseValue();
        }
        else {
            getFieldByIndex(player1Move).decreaseValue();
            getFieldByIndex(player2Move).decreaseValue();
        }
        for (int i = 0; i < fields.length; i++) {
            var fieldIndex = i + 1;
            if (fieldIndex != player1Move && fieldIndex != player2Move) {
                getFieldByIndex(fieldIndex).increaseValue();
            }
        }
    }

    @Override
    public String toString() {
        var headers = new String[this.fields.length];
        var fieldValues = new String[this.fields.length];

        var currLetter = 'A';
        for (int i = 0; i < headers.length; i++) {
            headers[i] = Character.toString(currLetter);
            fieldValues[i] = Integer.toString(this.fields[i].getValue());
            currLetter++;
        }

        return Logger.asTable(headers, fieldValues);
    }

    @Override
    public Environment clone() {
        var newFields = fields.clone();
        for (int i = 0; i < newFields.length; i++) {
            newFields[i] = newFields[i].clone();
        }

        return new Environment(newFields);
    }

    private static class Field {
        private int value;

        public Field() {
            this.value = 1;
        }

        private Field(int initValue) {
            this.value = initValue;
        }

        public int getValue() {
            return value;
        }

        public void increaseValue() {
            this.value += 1;
        }

        public void decreaseValue() {
            this.value -= 1;
            this.value = Math.max(this.value, 0);
        }

        @Override
        public Field clone() {
            return new Field(this.value);
        }
    }
}

class Solution {
    public static void main(String[] args) {
        var lingering = new GamePlayer(new LingeringPlayer(), "Careful");
        var random = new GamePlayer(new RandomPlayer(), "Random");
        var greedy = new GamePlayer(new GreedyPlayer(), "Greedy");
        var copycat = new GamePlayer(new CopycatPlayer(), "Copycat");
        var sequential = new GamePlayer(new SequentialPlayer(), "Sequential");
        var previous = new GamePlayer(new PreviousFieldPlayer(), "Previous field");
        var randomDirection = new GamePlayer(new RandomDirectionMovePlayer(), "Random direction");
        var t = new Tournament(lingering, random, greedy, copycat, sequential, previous, randomDirection);
        t.playAll();
    }
}