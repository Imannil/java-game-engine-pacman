package com.jgegroup.pacman;

import com.jgegroup.pacman.objects.Map;

import com.jgegroup.pacman.objects.UI;
import com.jgegroup.pacman.objects.characters.Ghost;
import com.jgegroup.pacman.objects.characters.Pac;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.geometry.Pos;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import java.io.File;


public class GameScene implements Runnable{
  public static boolean gameFinished;
  private Thread gameThread;
  private static long FPS = 60;
  private static long targetTimePerFrame = 1000000000 / FPS;
  private static long lastTime = System.nanoTime();
  private static long currentTime;
  private static long elapsedTime;
  private static long sleepTime;
  public static final int TILE_SIZE = 32;
  public static final int NUMBER_OF_TILE_COLUMN = 20;
  public static final int NUMBER_OF_TILE_ROW = 28;

  public static final int RESOLUTION_HORIZONTAL = TILE_SIZE * NUMBER_OF_TILE_COLUMN; // 768
  public static final int RESOLUTION_VERTICAL = TILE_SIZE * NUMBER_OF_TILE_ROW; // 1024
  public CollisionChecker collisionChecker;
  public javafx.scene.Scene gameScene;

  private StackPane stackPane;

  Canvas gameCanvas;
  private GraphicsContext gamePainter;

  public  Map map;
  private KeyHandler keyHandler;
  private Pac pac;
  private Ghost[] ghosts;
  private Color[] colors = {Color.RED, Color.BLUE, Color.YELLOW, Color.PINK};

  private int ghostNumber;

  private Slider speedSlider;

  public UI ui;




  public GameScene(int ghostNumber) {
    stackPane = new StackPane();
    gameScene = new javafx.scene.Scene(stackPane, RESOLUTION_HORIZONTAL, RESOLUTION_VERTICAL, Color.BLACK);
    gameCanvas = new Canvas(RESOLUTION_HORIZONTAL, RESOLUTION_VERTICAL);
    gamePainter = gameCanvas.getGraphicsContext2D();
    addCanvasLayer(gameCanvas);

    ui = new UI(this);
    keyHandler = new KeyHandler();
    gameScene.setOnKeyPressed(keyHandler);
    this.ghostNumber = ghostNumber;


    // New Stuff
    VBox vbox = new VBox();
    vbox.setSpacing(10);

    speedSlider = new Slider();
    speedSlider.setMin(1);
    speedSlider.setMax(3);
    speedSlider.setValue(1);
    speedSlider.setFocusTraversable(false);
    speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      int newSpeed = newValue.intValue();
      pac.setSpeed(newSpeed);
    });

    vbox.getChildren().add(speedSlider);
    StackPane.setAlignment(vbox, Pos.TOP_RIGHT);
    stackPane.getChildren().addAll(vbox);

    // Fix scene focus set on slider.
    speedSlider.setOnMouseClicked(event -> {
      gameCanvas.requestFocus();
    });

    speedSlider.setOnMouseDragged(event -> {
      gameCanvas.requestFocus();
    });
  }



  // FLOW: startThread() --> run() --> update() & redraw()




  /** @@Author: Tung
   * Start thread. Called from Main class.
   */
  public void startThread() {
    gameThread = new Thread(this);
    init();
    gameThread.start();
  }


  /** @@Author: Tung
   * Init the game before run the game.
   */
  public void init() {
    map = Map.getMapInstance();
    map.createMap();
    collisionChecker = new CollisionChecker(map);

    pac = new Pac(this, keyHandler);

    ghosts = new Ghost[ghostNumber];

    for (int i = 0; i < ghostNumber; i++) {
      ghosts[i] = new Ghost(10, this, colors[i], pac);
      ghosts[i].setSpawnPosition(i);
    }
  }

  /** @@Author: Tung
   * Run the game after Init().
   */
  @Override
  public void run() {
    Media media = new Media(new File("res/sounds/du hast.mp3").toURI().toString());
    MediaPlayer mp = new MediaPlayer(media);
    mp.setVolume(0.25f);
    mp.play();
    while (pac.getLives() >= 0) {
      update();
      redraw();
      controlFPS(); // DANGER!!!  REMOVE THIS CAUSE ATOMIC EXPLOSION
    }
    ui.displayGameFinish(getGamePainter());
  }

  /** @@Author: Tung, Noah, Jesse
   * Main game's update(), control entities update().
   */
  public void update() {
    pac.update();
    for (Ghost ghost : ghosts) {
      ghost.update();
    }

  }
  /** @@Author: Tung
   * Main game's redraw(), control map, entities redraw().
   */
  public void redraw() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        map.drawMap(getGamePainter());
        map.drawDot(getGamePainter());
        pac.redraw(getGamePainter());
        for (int i = 0; i < ghosts.length; i++) {
          ghosts[i].redraw(getGamePainter());
        }
        ui.redraw(getGamePainter());
      }
    });
  }



  /** @@Author: Tung
   * Attach canvas to game panel(stackPane)
   */
  public void addCanvasLayer(Canvas canvas) {
    this.stackPane.getChildren().add(canvas);
  }

  /** @@Author: Noah, Tung
   * Control FPS
   */
  public void controlFPS() {
    currentTime = System.nanoTime();
    elapsedTime = currentTime - lastTime;
    lastTime = currentTime;
    sleepTime = targetTimePerFrame - elapsedTime;
    if (sleepTime > 0) {
      try {
        Thread.sleep(sleepTime / 1000000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  // Encapsulation

  public int getPacLives() {
    return pac.getLives();
  }
  public int getPoints() {
    return pac.getPoint();
  }

  public Canvas getCanvas() {
    return this.gameCanvas;
  }
  public GraphicsContext getGamePainter() {
    return this.gamePainter;
  }
}



