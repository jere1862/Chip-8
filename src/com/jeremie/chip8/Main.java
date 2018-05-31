package com.jeremie.chip8;

        import com.jeremie.chip8.core.Runner;
        import com.jeremie.chip8.ui.Debugger;

public class Main {
    private static final boolean DEBUG = false;

    public static void main(String[] args) {
        Runner runner = new Runner();

        if(DEBUG) {
            Debugger debugger = new Debugger(runner);
            debugger.start("pong");
        }else{
            runner.start("pong");
        }
    }
}
