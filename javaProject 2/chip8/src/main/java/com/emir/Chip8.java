package com.emir;

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
                    System.out.println("CLS");
                    pc += 2;
                } else if ((opcode & 0x00FF) == 0x00EE) {
                    System.out.println("RETURN");
                    pc += 2;
                }
                break;

            case 0x1000:
                int address = opcode & 0x0FFF;
                pc = address;
                System.out.println("Jump to " + Integer.toHexString(address));
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
            case 0xA000:
                indexRegister = opcode & 0x0FFF;
                pc += 2;
                System.out.println("Set I to " + Integer.toHexString(indexRegister));
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

        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
        }
    }
}
