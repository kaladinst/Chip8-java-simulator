package com.emir;

import java.util.Random;

public class Chip8 {

    private final byte[] memory = new byte[4096];

    private final byte[] registers = new byte[16];

    private int indexRegister;

    private int pc;

    private final int[] stack = new int[16];

    private int sp;

    private int delayTimer;
    private int soundTimer;

    private final byte[] display = new byte[64 * 32];
    public byte[] keypad = new byte[16];

    private Random random = new Random();

    public Chip8() {
        this.pc = 0x200;
    }

    public byte[] getMemory() {
        return this.memory;
    }

    public byte[] getDisplay() {
        return this.display;
    }

    public void loadRom(byte[] romData) {
        for (int i = 0; i < romData.length; i++) {
            memory[512 + i] = romData[i];
        }
    }

    public void runCycle() {

        short opcode = (short) ((memory[pc] << 8) | (memory[pc + 1] & 0xFF));

        switch (opcode & 0xF000) {
            case 0x0000:
                if ((opcode & 0x00FF) == 0x00E0) {
                    for (int i = 0; i < display.length; i++) {
                        display[i] = 0;
                    }
                    pc += 2;
                } else if ((opcode & 0x00FF) == 0x00EE) {
                    sp--;
                    pc = stack[sp];
                    pc += 2;
                    System.out.println("RETURN");
                }
                break;

            case 0x1000:
                int address = opcode & 0x0FFF;
                pc = address;
                System.out.println("Jump to " + Integer.toHexString(address));
                break;

            case 0x2000:
                stack[sp] = pc;
                sp++;
                pc = opcode & 0xFFF;
                System.out.println("Call Subroutine at " + Integer.toHexString(pc));
                break;
            case 0x3000:
                int x3 = (opcode & 0x0F00) >> 8;
                int kk3 = opcode & 0x00FF;
                if (registers[x3] == kk3) {
                    pc += 2;
                }
                pc += 2;
                break;

            case 0x4000:
                int x4 = (opcode & 0x0F00) >> 8;
                int nn4 = opcode & 0x00FF;
                if (registers[x4] != nn4) {
                    pc += 2;
                }
                pc += 2;
                break;

            case 0x5000:
                int x5 = (opcode & 0x0F00) >> 8;
                int y5 = (opcode & 0x00F0) >> 4;
                if (registers[x5] == registers[y5]) {
                    pc += 2;
                }
                pc += 2;
                break;
            case 0x6000:
                int x = (opcode & 0x0F00) >> 8;
                byte kk = (byte) (opcode & 0x0FF);
                registers[x] = kk;
                pc += 2;
                System.out.println("Set V" + x + " to " + kk);
                break;

            case 0x7000:
                int x7 = (opcode & 0x0F00) >> 8;
                byte kk7 = (byte) (opcode & 0x00FF);

                registers[x7] += kk7;
                pc += 2;
                System.out.println("Add " + kk7 + " to V" + x7);
                break;
            case 0x8000:
                int x8 = (opcode & 0x0F00) >> 8;
                int y8 = (opcode & 0x00F0) >> 4;

                switch (opcode & 0x000F) {
                    case 0x0:
                        registers[x8] = registers[y8];
                        break;
                    case 0x1:
                        registers[x8] |= registers[y8];
                        break;
                    case 0x2:
                        registers[x8] &= registers[y8];
                        break;
                    case 0x3:
                        registers[x8] ^= registers[y8];
                        break;
                    case 0x4:
                        int sum = (registers[x8] & 0xFF) + (registers[y8] & 0xFF);
                        if (sum > 255) {
                            registers[0xF] = (byte) 1;
                        } else {
                            registers[0xF] = (byte) 0;
                        }
                        registers[x8] = (byte) (sum & 0xFF);
                        break;
                    case 0x5:
                        if ((registers[x8] & 0xFF) > (registers[y8] & 0xFF)) {
                            registers[0xF] = (byte) 1;
                        } else {
                            registers[0xF] = (byte) 0;
                        }
                        int subFromX = (registers[x8] & 0xFF) - (registers[y8] & 0xFF);
                        registers[x8] = (byte) (subFromX & 0xFF);
                        break;
                    case 0x6:
                        registers[0xF] = (byte) (registers[x8] & 0x1);
                        registers[x8] >>= 1;
                        break;
                    case 0x7:
                        if ((registers[x8] & 0xFF) >= (registers[y8] & 0xFF)) {
                            registers[0xF] = (byte) 1;
                        } else {
                            registers[0xF] = (byte) 0;
                        }
                        int subFromY = (registers[y8] & 0xFF) - (registers[x8] & 0xFF);
                        registers[x8] = (byte) (subFromY & 0xFF);
                        break;
                    case 0xE:
                        registers[0xF] = (byte) ((registers[x8] & 0x80) >> 7);
                        registers[x8] <<= 1;
                        break;
                    default:
                        break;
                }
                pc += 2;
                break;
            case 0x9000:
                int x9 = (opcode & 0x0F00) >> 8;
                int y9 = (opcode & 0x00F0) >> 4;
                if (registers[x9] != registers[y9]) {
                    pc += 2;
                }
                pc += 2;
                break;
            case 0xA000:
                indexRegister = opcode & 0x0FFF;
                pc += 2;
                System.out.println("Set I to " + Integer.toHexString(indexRegister));
                break;
            case 0xB000:
                int kkkB = (opcode & 0x0FFF) & 0xFFF;
                pc = (registers[0] & 0xFF) + kkkB;
                break;

            case 0xC000:
                int xC = (opcode & 0x0F00) >> 8;
                int kkC = opcode & 0x00FF;
                registers[xC] = (byte) (random.nextInt(256) & kkC);
                pc += 2;
                break;
            case 0xE000:
                int xE = (opcode & 0x0F00) >> 8;
                int key = registers[xE];
                if ((opcode & 0x00FF) == 0x9E) {
                    if (keypad[key] != 0) {
                        pc += 2;
                    }
                    pc += 2;
                } else if ((opcode & 0x00FF) == 0xA1) {
                    if (keypad[key] == 0) {
                        pc += 2;
                    }
                    pc += 2;
                }
                break;
            case 0xF000:
                int xF = (opcode & 0x0F00) >> 8;
                switch (opcode & 0x00FF) {
                    case 0x07:
                        registers[xF] = (byte) delayTimer;
                        break;
                    case 0x15:
                        delayTimer = registers[xF] & 0xFF;
                        break;
                    case 0x18:
                        soundTimer = registers[xF] & 0xFF;
                        break;
                    case 0x1E:
                        indexRegister += (registers[xF] & 0xFF);
                        break;
                    case 0x29:
                        indexRegister = (registers[xF] & 0x0F) * 5;
                        break;
                    case 0x33:
                        int value = registers[xF] & 0xFF;
                        memory[indexRegister] = (byte) (value / 100);
                        memory[indexRegister + 1] = (byte) ((value / 10) % 10);
                        memory[indexRegister + 2] = (byte) (value % 10);
                        break;
                    case 0x55:
                        for (int i = 0; i <= xF; i++) {
                            memory[indexRegister + i] = registers[i];
                        }
                        break;
                    case 0x65:
                        for (int i = 0; i <= xF; i++) {
                            registers[i] = memory[indexRegister + i];
                        }
                        break;
                    case 0x0A:
                        boolean keyPressed = false;
                        for (int i = 0; i < keypad.length; i++) {
                            if (keypad[i] != 0) {
                                registers[xF] = (byte) i;
                                keyPressed = true;
                                break;
                            }
                        }
                        if (!keyPressed) {
                            pc -= 2;
                        }
                        pc += 2;
                        break;
                }
                pc += 2;
                break;
            case 0xD000:
                int xIndex = (opcode & 0x0F00) >> 8;
                int yIndex = (opcode & 0x000F0) >> 4;
                int xI = registers[xIndex] & 0xFF;
                int yI = registers[yIndex] & 0xFF;
                int height = opcode & 0x000F;

                registers[0xF] = 0;
                for (int row = 0; row < height; row++) {
                    int pixelByte = memory[indexRegister + row];

                    for (int col = 0; col < 8; col++) {
                        if ((pixelByte & (0x80 >> col)) != 0) {
                            int targetX = (xI + col) % 64;
                            int targetY = (yI + row) % 32;

                            int index = targetX + (targetY * 64);

                            if (display[index] == 1) {
                                registers[0xF] = 1;
                            }

                            display[index] ^= 1;
                        }
                    }
                }
                pc += 2;
                break;
            default:
                System.out.printf("Unkown code: 0x%04X\n", opcode);
                pc += 2;
                break;
        }

    }

    public void updateTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
        }
    }
}
