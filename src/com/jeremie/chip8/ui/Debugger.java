package com.jeremie.chip8.ui;

import com.jeremie.chip8.core.Runner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Debugger extends JFrame {
    private Runner runner;
    private static final Dimension dimension = new Dimension(200, 400);
    private JLabel registerText;
    private JLabel opcodeText;

    public Debugger(Runner runner) {
        super("Chip-8 Debugger");
        this.runner = runner;
        this.runner.setDebuggerHandler((key) -> keyHandler(key));
        this.runner.setInformationUpdater((opcode, registers) -> infoUpdate(opcode, registers));
        initializeFrame();
        initializeText();

        int[] test = new int[16];
        parseRegisterArray(test);
        parseOpcode(0x061A);
    }

    public void start(String romName) {
        // Start one cycle
        // Next cycle when spacebar key is pressed
        runner.start(romName);
    }

    private void initializeFrame() {
        setLayout(new BorderLayout());
        setSize(new Dimension(200, runner.getHeight()));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(runner);
        setLocation((int) (runner.getWidth()/2.0 + getWidth()/2.0 + getX()), getY());
        setVisible(true);
        setResizable(false);
    }

    private void infoUpdate(int opcode, int[] registers) {
        parseRegisterArray(registers);
        parseOpcode(opcode);
    }

    private void keyHandler(int key) {
        if(key == 0xFFF) {
            // Spacebar
            runner.nextCycle();
        }

        if(key == 0xFFE) {
            runner.resumeCycles();
        }
    }

    private void parseRegisterArray(int[] registers) {
        String text = "<html>Registers<br>";
        for(int i = 0; i < 16; i++) {
            text = text.concat(String.format("V%X: %d<br>", i, registers[i]));
        }
        text = text.concat("</html>");

        registerText.setText(text);
    }

    private void parseOpcode(int opcode) {
        String text = "Opcode: 0x%04X";
        opcodeText.setText(String.format(text, opcode & 0xFFFF));
    }

    private void initializeText() {
        Container container = new Container();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        EmptyBorder margin = new EmptyBorder(10,10,10,10);
        registerText = new JLabel("Registers");
        registerText.setBorder(margin);
        registerText.setText("temp");
        container.add(registerText);

        opcodeText = new JLabel("Opcode");
        opcodeText.setBorder(margin);
        opcodeText.setText("temp");
        container.add(opcodeText);

        // Todo: Add program counter

        add(container);
    }
}
