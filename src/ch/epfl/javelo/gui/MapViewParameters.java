package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * The parameters of the basemap presented in the GUI
 *
 * @author Elias Mir(341277)
 * @author Jan Staszewicz(341201)
 */
public record MapViewParameters(int zoomLevel, double x, double y) {

    /**
     * Returns coordinates of the top-left corner as an object of type Point2D
     *
     * @return coordinates of the top-left corner as an object of type Point2D
     */
    public Point2D topLeft() {
        return new Point2D(x, y);
    }

    /**
     * Returns an instance of MapViewParameters with the same zoomLevel
     * and with the x and y given as arguments
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return an instance of MapViewParameters with the same zoomLevel
     */
    public MapViewParameters withMinXY(double x, double y) {
        return new MapViewParameters(zoomLevel, x, y);
    }

    /**
     * Returns the point as an instance of PointWebMercator
     *
     * @param xDiff coordinates x of a point, expressed relative to the top-left corner
     *              of the map portion displayed on the screen
     * @param yDiff coordinates y of a point, expressed relative to the top-left corner
     *              of the map portion displayed on the screen
     * @return the point as an instance of PointWebMercator
     */
    public PointWebMercator pointAt(double xDiff, double yDiff) {
        return PointWebMercator.of(zoomLevel, x + xDiff, y + yDiff);
    }

    /**
     * Returns the corresponding x position
     * expressed relative to the top-left corner of the map portion displayed on the screen
     *
     * @param point given PointWebMercator point
     * @return the corresponding x position
     * expressed relative to the top-left corner of the map portion displayed on the screen
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - x;
    }

    /**
     * Returns the corresponding y position
     * expressed relative to the top-left corner of the map portion displayed on the screen
     *
     * @param point given PointWebMercator point
     * @return the corresponding y position
     * expressed relative to the top-left corner of the map portion displayed on the screen
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - y;
    }
}
