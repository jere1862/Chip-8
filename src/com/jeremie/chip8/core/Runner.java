package com.jeremie.chip8.core;

import com.jeremie.chip8.ui.GameScreen;
import com.jeremie.chip8.utils.RomReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Runner extends JFrame {
    private GameScreen gameScreen;
    private Cpu cpu;
    private RomReader romReader;
    private Timer clock;
    private Consumer<Integer> debugHandler;
    private BiConsumer<Integer, int[]> informationUpdater;

    public Runner() {
        super("Chip-8");
        gameScreen = new GameScreen();
        cpu = new Cpu(gameScreen);
        gameScreen.setKeyAdapter(new GameAdapter(this.cpu));
        romReader = new RomReader();
        clock = new Timer(1, new MyActionListener());
        clock.setRepeats(true);
        initializeFrame();
    }

    public void setDebuggerHandler(Consumer<Integer> handler) {
        this.debugHandler = handler;
    }

    public void setInformationUpdater(BiConsumer<Integer, int[]> handler) {
        this.informationUpdater = handler;
    }

    public void start(String gameName) {
        byte[] rom = romReader.read(gameName);
        cpu.loadRom(rom);
        clock.start();
    }

    public void nextCycle() {
        clock.setRepeats(false);
        clock.restart();
    }

    public void resumeCycles() {
        clock.setRepeats(true);
        clock.restart();
    }

    private void initializeFrame() {
        setLayout(new BorderLayout());
        add(gameScreen, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }

    private int mapKey(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_1:
                return 1;
            case KeyEvent.VK_2:
                return 2;
            case KeyEvent.VK_3:
                return 3;
            case KeyEvent.VK_4:
                return 0xC;
            case KeyEvent.VK_Q:
                return 4;
            case KeyEvent.VK_W:
                return 5;
            case KeyEvent.VK_E:
                return 6;
            case KeyEvent.VK_R:
                return 0xD;
            case KeyEvent.VK_A:
                return 7;
            case KeyEvent.VK_S:
                return 8;
            case KeyEvent.VK_D:
                return 9;
            case KeyEvent.VK_F:
                return 0xE;
            case KeyEvent.VK_Z:
                return 0xA;
            case KeyEvent.VK_X:
                return 0;
            case KeyEvent.VK_C:
                return 0xB;
            case KeyEvent.VK_V:
                return 0xF;
            case KeyEvent.VK_SPACE:
                return 0xFFF;
            case KeyEvent.VK_ENTER:
                return 0xFFE;
            default:
                return 0xFF;
        }
    }

    private class GameAdapter extends KeyAdapter {
        private Cpu cpu;

        GameAdapter(Cpu cpu) {
            this.cpu = cpu;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int intKey = mapKey(e);

            if(intKey <= 0xF) {
                this.cpu.unsetKey(intKey);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int intKey = mapKey(e);

            if(intKey > 0xFF && debugHandler != null) {
                debugHandler.accept(intKey);
            }

            if(intKey <= 0xF) {
                this.cpu.setKey(intKey);
            }
        }
    }

    private class MyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            cpu.cycle();
            if(informationUpdater != null) {
                informationUpdater.accept(cpu.getOpcode(), cpu.getRegisters());
            }
        }
    }
}
