package com.emir;

import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private static final int SCALE = 15;
    Chip8 chip8 = new Chip8();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Canvas canvas = new Canvas(WIDTH * SCALE, HEIGHT * SCALE);
        GraphicsContext graphContext = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        scene.setFill(Color.BLACK);

        primaryStage.setTitle("Chip 8 - IBM Logo");
        primaryStage.setScene(scene);
        primaryStage.show();

        byte[] rom = Files.readAllBytes(Paths.get("ibm.ch8"));
        chip8.loadRom(rom);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (int i = 0; i < 10; i++) {
                    chip8.runCycle();
                }

                drawScreen(graphContext);
            }
        }.start();
    }

    private void drawScreen(GraphicsContext graphContext) {
        graphContext.setFill(Color.BLACK);
        graphContext.fillRect(0, 0, WIDTH * SCALE, SCALE * HEIGHT);

        graphContext.setFill(Color.WHITE);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int index = x + (y * WIDTH);
                if (chip8.getDisplay()[index] == 1) {
                    graphContext.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
