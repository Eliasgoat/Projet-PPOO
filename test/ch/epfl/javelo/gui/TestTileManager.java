package ch.epfl.javelo.gui;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.nio.file.Path;

import static javafx.application.Application.launch;

public class TestTileManager extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TileManager tm = new TileManager(
                Path.of("./diskCache"), "tile.openstreetmap.org");
        Image tileImage = tm.imageForTileAt(
                new TileManager.TileId(19, 271725, 185422));
        Image tileImage2 = tm.imageForTileAt(
                new TileManager.TileId(19, 271725, 185423));
        Image tileImage3 = tm.imageForTileAt(
                new TileManager.TileId(19, 271725, 185424));
        Image tileImage4 = tm.imageForTileAt(
                new TileManager.TileId(19, 271725, 185425));
        Image tileImage5 = tm.imageForTileAt(
                new TileManager.TileId(10, 3, 4));
        Image tileImage6 = tm.imageForTileAt(
                new TileManager.TileId(5, 5, 6));
        Platform.exit();
    }
}


