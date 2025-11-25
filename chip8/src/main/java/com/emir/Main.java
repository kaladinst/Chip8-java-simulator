package com.emir;

import java.io.File;
import java.nio.file.Files;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private static final int SCALE = 15;
    Chip8 chip8 = new Chip8();
    private GraphicsContext graphContext;
    private boolean isPaused = true;
    private int cyclesPerFrame = 15;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Canvas canvas = new Canvas(WIDTH * SCALE, HEIGHT * SCALE);
        graphContext = canvas.getGraphicsContext2D();
        BorderPane root = new BorderPane();

        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);
        root.setCenter(canvas);

        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        graphContext.setFill(Color.BLACK);
        graphContext.fillRect(0, 0, WIDTH * SCALE, SCALE * HEIGHT);
        primaryStage.setTitle("Chip 8 - Emulator");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPaused) {
                    for (int i = 0; i < cyclesPerFrame; i++) {
                        try {
                            chip8.runCycle();
                        } catch (Exception e) {
                            // TODO: handle exception
                            e.printStackTrace();
                            isPaused = true;
                        }
                    }

                    chip8.updateTimers();
                }

                drawScreen(graphContext);

            }
        }.start();
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem openItem = new MenuItem("Open ROM");
        openItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Chip-8 ROM");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Chip-8 Files", "*.ch8", "*.c8")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                loadGame(file);
            }
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(openItem, exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void loadGame(File file) {
        try {
            byte[] rom = Files.readAllBytes(file.toPath());
            chip8 = new Chip8();
            chip8.loadRom(rom);
            isPaused = false;
            System.out.println("Loaded " + file.getName());
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Failed to load");
            e.printStackTrace();
        }
    }

    private void drawScreen(GraphicsContext graphContext) {
        graphContext.setFill(Color.BLACK);
        graphContext.fillRect(0, 0, WIDTH * SCALE, SCALE * WIDTH);

        graphContext.setFill(Color.CYAN);
        byte[] display = chip8.getDisplay();
        for (int i = 0; i < display.length; i++) {
            if (display[i] == 1) {
                int x = (i % WIDTH) * SCALE;
                int y = (i / WIDTH) * SCALE;
                graphContext.fillRect(x, y, SCALE, SCALE);
            }
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.ADD) {
            cyclesPerFrame += 2;
            return;
        }
        if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
            if (cyclesPerFrame > 2) {
                cyclesPerFrame -= 2;
            }
            return;
        }
        int key = mapKey(event.getCode());
        if (key != -1) {
            chip8.keypad[key] = 1;
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        int key = mapKey(event.getCode());
        if (key != -1) {
            chip8.keypad[key] = 0;
        }
    }

    private int mapKey(KeyCode code) {
        switch (code) {
            case DIGIT1:
                return 0x1; // Key 1
            case DIGIT2:
                return 0x2; // Key 2
            case DIGIT3:
                return 0x3; // Key 3
            case DIGIT4:
                return 0xC; // Key C

            case Q:
                return 0x4; // Key 4
            case W:
                return 0x5; // Key 5
            case E:
                return 0x6; // Key 6
            case R:
                return 0xD; // Key D

            case A:
                return 0x7; // Key 7
            case S:
                return 0x8; // Key 8
            case D:
                return 0x9; // Key 9
            case F:
                return 0xE; // Key E

            case Z:
                return 0xA; // Key A
            case X:
                return 0x0; // Key 0
            case C:
                return 0xB; // Key B
            case V:
                return 0xF; // Key F

            default:
                return -1;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
