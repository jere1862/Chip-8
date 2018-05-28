package com.jeremie.chip8;

import com.jeremie.chip8.core.Runner;

public class Main {

    public static void main(String[] args) {
        Runner runner = new Runner();
        runner.start("breakout");
    }
}
