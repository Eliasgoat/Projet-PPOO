package ch.epfl.javelo.gui;

import javafx.scene.image.Image;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Un gestionnaire de tuiles OSM
 *
 * @author Elias Mir(341277)
 * @author Jan Staszewicz(341201)
 */
public final class TileManager {

    private final Path basePath;
    private final String tileServerName;
    private final LinkedHashMap<TileId, Image> memoryCache;

    private final static int MAX_CAPACITY = 100;

    /**
     * Builds the tile manager
     *
     * @param basePath       the access path to the directory containing the disk cache
     * @param tileServerName the name of the tile server
     */
    public TileManager(Path basePath, String tileServerName) {
        this.basePath = basePath;
        this.tileServerName = "https://" + tileServerName;

        this.memoryCache = new LinkedHashMap() {
            //Set size limit to 100 and remove the eldest Entry when we exceed limit.
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_CAPACITY;
            }
        };

    }

    /**
     * Loads image from server and place it in the disk cache
     *
     * @param tileId the tileId of the desired image
     * @param path   the path to where the image is supposed to be saved
     * @throws IOException if there is an error with the image loading or saving
     */
    private void loadImageFromServer(TileId tileId, String path) throws IOException {

        URL u = new URL(
                tileServerName
                        + "/" + tileId.zoomLevel
                        + "/" + tileId.x
                        + "/" + tileId.y
                        + ".png");

        URLConnection c = u.openConnection();
        c.setRequestProperty("User-Agent", "JaVelo");

        try (InputStream imageStream = c.getInputStream();) {
            placeInDiskCache(imageStream, path);
        }

    }

    /**
     * Checks if the given image is in the memory cache
     *
     * @param tileId the tileId of the image
     * @return boolean true if image is in the memory cache, else false
     */
    private boolean isInMemoryCache(TileId tileId) {
        return memoryCache.containsKey(tileId);
    }

    /**
     * Loads image from memory cache.
     *
     * @param tileId the tileId of the desired image
     * @return the desired image
     */
    private Image getFromMemoryCache(TileId tileId) {
        return memoryCache.get(tileId);
    }

    /**
     * Places the image in the memory cache
     *
     * @param tileId the tileId of the image
     * @param image  the image
     */
    private void placeInMemoryCache(TileId tileId, Image image) {
        memoryCache.put(tileId, image);
    }

    /**
     * Checks if the image is in the disk cache
     *
     * @param path the path to the image
     * @return boolean true if the image is in the disk cache, else false
     */
    private boolean isInDisk(String path) {
        return Files.exists(Path.of(path));
    }

    /**
     * Places the image in the disk cache given its InputStream
     *
     * @param imageStream the InputStream of the image
     * @param path        the path to where the image is supposed to be saved
     * @throws IOException if there is an error while saving the image
     */
    private void placeInDiskCache(InputStream imageStream, String path) throws IOException {

        try (OutputStream outputStream = new FileOutputStream(path)) {
            imageStream.transferTo(outputStream);
        }

    }

    /**
     * Loads image from the disk cache
     *
     * @param path   the path to the image
     * @return the image
     * @throws IOException if there is an error while loading the image
     */
    private Image getImageFromDiskCache(String path) throws IOException {

        File file = new File(path);

        try (FileInputStream imageStream = new FileInputStream(file)) {
            return new Image(imageStream);
        }
    }

    /**
     * Returns the image given its tileId
     *
     * @param tileId the image tileId
     * @return the image which has the tileId given
     * @throws IOException              in case of stream errors
     * @throws IllegalArgumentException if the tileId isn't valid
     */
    public Image imageForTileAt(TileId tileId) throws IOException {

        if(!TileId.isValid(tileId.zoomLevel, tileId.x, tileId.y))return null;

        if (isInMemoryCache(tileId)) {

            return getFromMemoryCache(tileId);

        } else {

            String path = basePath.toString()
                    + "/" + tileId.zoomLevel
                    + "/" + tileId.x
                    + "/" + tileId.y + ".png";

            if (isInDisk(path + "/" + tileId.y + ".png")) {

                Image image = getImageFromDiskCache(path);
                placeInMemoryCache(tileId, image);

                return image;

            } else {

                Files.createDirectories(Path.of(basePath.toString()
                        + "/" + tileId.zoomLevel
                        + "/" + tileId.x ));

                loadImageFromServer(tileId, path);
                Image image = getImageFromDiskCache(path);
                placeInMemoryCache(tileId, image);

                return image;
            }
        }
    }

    /**
     * Identity of an OSM tile
     *
     * @param zoomLevel the tile zoom level
     * @param x         the x index of the tile
     * @param y         the y index of the tile
     */
    public record TileId(int zoomLevel, int x, int y) {

        /**
         * Checks if tile is valid
         *
         * @param zoomLevel the tile zoom level
         * @param x         the x index of the tile
         * @param y         the y index of the tile
         * @return True if and only if the tile attributes are valid, else false
         */
        public static boolean isValid(int zoomLevel, int x, int y) {
            int maxTileNumber = (int)Math.pow(2, zoomLevel);
            return (x >= 0 && x < maxTileNumber) && (y >= 0 && y < maxTileNumber) && zoomLevel >= 0;
        }
    }
}
