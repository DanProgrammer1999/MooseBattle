package com.DanielDv99;

class Solution {
    public static void main(String[] args) {
        var careful = new GamePlayer(new CarefulPlayer(), "Careful");
        var random = new GamePlayer(new RandomPlayer(), "Random");
        var greedy = new GamePlayer(new GreedyPlayer(), "Greedy");
        var copycat = new GamePlayer(new CopycatPlayer(), "Copycat");
        var sequential = new GamePlayer(new SequentialPlayer(), "Sequential");
        var previous = new GamePlayer(new PreviousFieldPlayer(), "Previous field");
        var t = new Battle(careful, random, greedy, copycat, sequential, previous);
        t.playAll();
    }
}