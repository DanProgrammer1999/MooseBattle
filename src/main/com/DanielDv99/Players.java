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
    public void reset(){ }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        if(xA >= xB && xA >= xC){
            return 1;
        }

        else if(xB >= xA && xB >= xC){
            return 2;
        }
        else {
            return 3;
        }
    }
}

class CopycatPlayer implements Player {
    public void reset(){}

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        return opponentLastMove;
    }
}

class CarefulPlayer implements Player {

    private static int cooldownPeriod = 1;
    private static GreedyPlayer greedy = new GreedyPlayer();
    private int myLastSafeMove;
    private int myLastMove;
    private int currentCooldown;

    public CarefulPlayer(){
        initialize();
    }

    public void reset(){
        initialize();
    }

    private void initialize(){
        myLastSafeMove = 1;
        myLastMove = 0;

        currentCooldown = 0;
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC){
        var fieldScores = new int[]{xA, xB, xC};
        int move = myLastSafeMove;

        if(opponentLastMove == 0){
            // First move is always A
            move = 1;
        }
        else if(myLastMove != opponentLastMove){
            myLastSafeMove = myLastMove;
            var safeScore = fieldScores[myLastSafeMove - 1];

            // If you are "hungry" (cooldown passed) and the safe field doesn't give any payoff
            if(currentCooldown == 0 && safeScore == 0){
                move = greedy.move(opponentLastMove, xA, xB, xC);
            }
            else {
                move = myLastSafeMove;
                currentCooldown--;
            }
        }
        else{
            // last move there was a fight
            currentCooldown = cooldownPeriod;
            move = myLastSafeMove;
        }
        myLastMove = move;
        return move;
    }
}

class SequentialPlayer implements Player{
    private int previousMove;

    public SequentialPlayer(){
        reset();
    }

    public void reset(){
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

class PreviousFieldPlayer implements Player{
    private int previousMove;

    public void reset(){ }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        var myMove = opponentLastMove - 1;
        if(myMove == 0){
            myMove = 3;
        }

        return myMove;
    }
}

class NextFieldPlayer implements Player{
    private int previousMove;

    public void reset(){ }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        var myMove = opponentLastMove - 1;
        if(myMove == 0){
            myMove = 3;
        }

        return myMove;
    }
}