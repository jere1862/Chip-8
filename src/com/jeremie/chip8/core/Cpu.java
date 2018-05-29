package com.jeremie.chip8.core;

import com.jeremie.chip8.ui.GameScreen;

import java.util.Arrays;

public class Cpu {
    private static final int fontSet[] = {
            0xF0, 0x90, 0x90, 0x90, 0xF0,		// 0
            0x20, 0x60, 0x20, 0x20, 0x70,		// 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0,		// 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0,		// 3
            0x90, 0x90, 0xF0, 0x10, 0x10,		// 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0,		// 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0,		// 6
            0xF0, 0x10, 0x20, 0x40, 0x40,		// 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0,		// 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0,		// 9
            0xF0, 0x90, 0xF0, 0x90, 0x90,		// A
            0xE0, 0x90, 0xE0, 0x90, 0xE0,		// B
            0xF0, 0x80, 0x80, 0x80, 0xF0,		// C
            0xE0, 0x90, 0x90, 0x90, 0xE0,		// D
            0xF0, 0x80, 0xF0, 0x80, 0xF0,		// E
            0xF0, 0x80, 0xF0, 0x80, 0x80		// F
    };

    private final Runnable[] commands = {
        this::displayOrReturn, this::gotoNNN, this::callSubroutine, this::skipIfEqualConstant,
        this::skipIfNotEqualConstant, this::skipIfEqual, this::assignConstant, this::addConstant,
        this::aluOperation, this::skipIfNotEqual, this::assignIndexPointer, this::jumpToV0PlusConstant,
        this::randomNumber, this::drawSprite, this::keyOperation, this::miscOperation
    };

    private final Runnable[] aluOperations = {
        this::aluAssign, this::aluOR, this::aluAND, this::aluXOR, this::aluADD, this::aluSUB,
        this::aluRIGHTSHIFT, this::aluSUB2, this::aluLEFTSHIFT
    };

    private final Runnable[] keyOperations = {this::skipIfKeyPressed, this::skipIfKeyReleased};

    private final Runnable[] miscOperations = new Runnable[0x66];

    private int opcode;
    private byte[] memory;
    private int[] registers = new int[16];
    private int[] stack = new int[16];
    private boolean[] keyboard = new boolean[16];
    private int delayTimer;
    private int soundTimer;
    private int programCounter;
    private int stackPointer = 0;
    private int indexPointer;
    private byte[] monitor = new byte[64 * 32];
    private GameScreen gameScreen;
    private int vx, vy;

    public Cpu(GameScreen gameScreen) {
        this.programCounter = 0x200;
        this.memory = new byte[4096];
        this.gameScreen = gameScreen;
        copyFontSetToMemory();
        initializeMiscOperations();
    }

    public void loadRom(byte[] rom) {
        System.arraycopy(rom, 0, memory, 0x200, rom.length);
    }

    public void cycle() {
        fetchOpcode();
        executeOpcode();
        gameScreen.drawScreen(monitor);

        if(delayTimer > 0) {
            delayTimer--;
        }

        if(soundTimer > 0) {
            soundTimer--;
        }
    }

    private void fetchOpcode() {
        int msb = memory[programCounter] & 0xFF;
        int lsb = memory[programCounter + 1] & 0xFF;

        opcode = msb << 8 | lsb;
        vx = (opcode & 0xF00) >> 8;
        vy = (opcode & 0xF0) >> 4;
        programCounter += 2;
    }

    private void executeOpcode() {
        commands[opcode >> 12].run();
    }

    private void aluOperation() {
        aluOperations[opcode & 0xF].run();
    }

    private void keyOperation() {
        keyOperations[opcode & 0x1].run();
    }

    private void miscOperation() {
        miscOperations[opcode & 0xFF].run();
    }

    public void setKey(int key) {
        if(key < 16) {
            keyboard[key & 0xF] = true;
        }
    }

    public void unsetKey(int key) {
        if(key < 16) {
            keyboard[key & 0xF] = false;
        }
    }

    private void clearScreen() {
        Arrays.fill(monitor, (byte)0x0);
    }

    private void displayOrReturn() {
        if((opcode & 0xFF) == 0xE0) {
            clearScreen();
        }else{
            subroutineReturn();
        }
    }

    private void subroutineReturn() {
        programCounter = stackPop();
    }

    private void gotoNNN() {
        programCounter = opcode & 0xFFF;
    }

    private void callSubroutine() {
        int address = opcode & 0xFFF;
        // Put current address in the stack
        stackPush(programCounter);
        programCounter = address;
    }

    private void skipIfEqualConstant() {
        int registerValue = registers[vx];
        if (registerValue == (opcode & 0xFF)) {
            programCounter += 2;
        }
    }

    private void skipIfNotEqualConstant() {
        int registerValue = registers[vx];
        if (registerValue != (opcode & 0xFF)) {
            programCounter += 2;
        }
    }

    private void skipIfEqual() {
        if(registers[vx] == registers[vy]){
            programCounter += 2;
        }
    }

    private void assignConstant() {
        registers[vx] = (opcode & 0xFF);
    }

    private void addConstant() {
        registers[vx] = (registers[vx] + (opcode & 0xFF)) & 0xFF;
    }

    private void aluAssign() {
        registers[vx] = registers[vy];
    }

    private void aluOR() {
        registers[vx] |= registers[vy];
    }

    private void aluAND() {
        registers[vx] &= registers[vy];
    }

    private void aluXOR() {
        registers[vx] ^= registers[vy];
    }

    private void aluADD() {
        int result = registers[vx] + registers[vy];
        if ((result & 0xF00) != 0) {
            registers[15] = 1;
        }
        registers[vx] = result & 0xFF;
    }

    private void aluSUB() {
        if(registers[vx] > registers[vy]) {
            registers[15] = 1;
        }
        registers[vx] = (registers[vx] - registers[vy]) & 0xFF;
    }

    private void aluRIGHTSHIFT() {
        registers[vx] = registers[vy] >> 1;
        registers[15] = registers[vy] & 1;
    }

    private void aluSUB2() {
        int x = registers[vx],
                y = registers[vy],
                x7 = x >> 7,
                y7 = y >> 7,
                r7 = (y - x) >> 7,
                borrow = (~y7 & x7) | (x7 & r7) | (r7 & ~y7);

        registers[vx] = r7;
        registers[15] = borrow;
    }

    private void aluLEFTSHIFT() {
        registers[15] = registers[vy] & 0x80;
        registers[vx] = registers[vy] <<= 1;
    }

    private void skipIfNotEqual() {
        if(registers[vx] != registers[vy]) {
            programCounter += 2;
        }
    }

    private void assignIndexPointer() {
        indexPointer = opcode & 0xFFF;
    }

    private void jumpToV0PlusConstant() {
        programCounter = registers[0] + (opcode & 0xFFF);
    }

    private void randomNumber() {
        registers[vx] = ((int) Math.round(Math.random() * 256) - 1) & (opcode & 0xFF);
    }

    private void drawSprite() {
        int height = opcode & 0xF,
                xOffset = registers[vx],
                yOffset = registers[vy];

        boolean collision = false;
        for(int i = 0; i < height; i++) {
            byte line = memory[indexPointer + i];
            // Check for collision, then xor in the sprite
            // 8 pixels wide
            for (int j = 0; j < 8; j++) {
                int newBit = (line >> (7 - j)) & 1;
                int globalXPosition = (xOffset + j) % 64;
                int globalYPosition = ((yOffset + i) % 32) * 64;
                int currentBitPosition = globalXPosition + globalYPosition;
                collision = collision || ((monitor[currentBitPosition] & newBit) == 1);
                monitor[currentBitPosition] ^= newBit;
            }
        }
        registers[15] = collision ? 1 : 0;
    }

    private void skipIfKeyPressed() {
        if(keyboard[registers[vx]]) {
            programCounter += 2;
        }
    }

    private void skipIfKeyReleased() {
        if(!keyboard[registers[vx]]) {
            programCounter += 2;
        }
    }

    private void getDelayTimer() {
        registers[vx] = delayTimer;
    }

    private void waitForKey() {
        programCounter  -= 2;
        int i = 0;
        while(i != 16) {
            if(keyboard[i]) {
                break;
            }else{
                i++;
            }
        }

        if(i != 16) {
            registers[vx] = i;
            programCounter += 2;
        }
    }

    private void setDelayTimer(){
        delayTimer = registers[vx];
    }

    private void setSoundTimer() {
        soundTimer = registers[vx];
    }

    private void incrementIndexPointer() {
        indexPointer += registers[vx];
    }

    private void setIndexPointerToSprite() {
        indexPointer = registers[vx];
    }

    private void storeBCD() {
        int value = registers[vx];
        int hundreds = (int) (value / 100.0);
        int tens = (value % 100) / 10;
        int units = value % 10;
        memory[indexPointer] = (byte) hundreds;
        memory[indexPointer + 1] = (byte) tens;
        memory[indexPointer + 2] = (byte) units;
    }

    private void dumpToMemory() {
        for(int i = 0; i < vx + 1; i++) {
            byte register = (byte) registers[i];
            memory[indexPointer] = register;
            indexPointer++;
        }
    }

    private void fillFromMemory() {
        for(int i = 0; i < vx + 1; i++) {
            registers[i] = memory[indexPointer];
            indexPointer++;
        }
    }

    private void initializeMiscOperations() {
        miscOperations[0x07] = this::getDelayTimer;
        miscOperations[0x0A] = this::waitForKey;
        miscOperations[0x15] = this::setDelayTimer;
        miscOperations[0x18] = this::setSoundTimer;
        miscOperations[0x1E] = this::incrementIndexPointer;
        miscOperations[0x29] = this::setIndexPointerToSprite;
        miscOperations[0x33] = this::storeBCD;
        miscOperations[0x55] = this::dumpToMemory;
        miscOperations[0x65] = this::fillFromMemory;
    }

    private void stackPush(int address) {
        stack[stackPointer] = address;
        stackPointer++;
    }

    private int stackPop() {
        stackPointer--;
        int address = stack[stackPointer];
        stack[stackPointer] = 0;
        return address;
    }

    private void printMonitor() {
        for(int i = 0; i < 32; i++) {
            for(int j = 0; j < 64; j++) {
                if(monitor[j + 64*i] == 1) {
                    System.out.print("0 ");
                }else{
                    System.out.print("- ");
                }
            }
            System.out.println();
        }
    }

    private void copyFontSetToMemory() {
        for(int i = 0; i < fontSet.length; i++) {
            memory[i] = (byte) (fontSet[i] & 0xFF);
        }
    }
}
