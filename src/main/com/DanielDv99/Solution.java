package com.DanielDv99;

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