package com.DanielDv99;

import java.util.Random;

interface Player {
    public void reset();

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
    public void reset() {
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        return opponentLastMove;
    }
}

class CarefulPlayer implements Player {
    private static GreedyPlayer greedy = new GreedyPlayer();
    private int myLastMove;

    public CarefulPlayer() {
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
    private int previousMove;

    public PreviousFieldPlayer() {
        this.previousMove = 0;
    }

    public void reset() {
        previousMove = 0;
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        var myMove = opponentLastMove - 1;
        if (myMove == 0) {
            myMove = 3;
        }

        if (previousMove == myMove) {
            myMove -= 1;
            if (myMove == 0) {
                myMove = 3;
            }
        }

        previousMove = myMove;
        return myMove;
    }
}

class NextFieldPlayer implements Player {
    private int previousMove;

    public NextFieldPlayer() {
        this.previousMove = 0;
    }

    public void reset() {
        previousMove = 0;
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        var myMove = opponentLastMove + 1;
        if (myMove == 4) {
            myMove = 1;
        }

        if (myMove == previousMove) {
            myMove += 1;

            if (myMove == 4) {
                myMove = 1;
            }
        }

        this.previousMove = myMove;
        return myMove;
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
            if(myMove == 0){
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