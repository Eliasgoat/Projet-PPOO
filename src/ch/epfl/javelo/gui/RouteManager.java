package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Route manager for JaVelo
 *
 * @author Elias Mir(341277)
 * @author Jan Staszewicz(341201)
 */
public final class RouteManager {
    private final RouteBean routeBean;
    private final ReadOnlyObjectProperty<MapViewParameters> mapProperty;
    private final Pane pane;
    private final Polyline line;
    private final Circle circle;
    private final static int CIRCLE_RADIUS = 5;

    /**
     * Constructs a route manager
     *
     * @param routeBean   the route bean
     * @param mapProperty the property containing the parameters of the map displayed
     */
    public RouteManager(RouteBean routeBean,
                        ObjectProperty<MapViewParameters> mapProperty) {
        this.routeBean = routeBean;
        this.mapProperty = mapProperty;

        pane = new Pane();
        pane.setPickOnBounds(false);

        line = new Polyline();
        line.setId("route");
        pane.getChildren().add(line);

        circle = new Circle(CIRCLE_RADIUS);
        circle.setId("highlight");
        pane.getChildren().add(circle);
        circle.setVisible(false);

        //Redraws the circle if the highlighted position changes
        this.routeBean.getHighlightedPositionProperty().addListener((o, oV, nV) -> redrawCircle());

        //Redraws the circle and the line when the parameters of the basemap changes
        this.mapProperty.addListener((o, oV, nV) -> {
            redrawCircle();

            if (oV.zoomLevel() != nV.zoomLevel()) {
                redrawLine();
            } else {
                line.setLayoutX(-nV.x());
                line.setLayoutY(-nV.y());
            }
        });

        //Redraws the circle and the line when the waypoints changes
        this.routeBean.getWaypoints().addListener((Observable o) -> {
            redrawLine();
            redrawCircle();
        });

        //If the user presses on the circle, a waypoint is added
        circle.setOnMouseReleased(e -> {
            Point2D pointInPane = circle.localToParent(e.getX(), e.getY());
            addWaypointInCircle(pointInPane.getX(), pointInPane.getY());
        });
    }

    /**
     * Returns the JavaFX panel containing the line representing the route and the highlighted disk
     *
     * @return the JavaFX panel containing the line representing the route and the highlighted disk
     */
    public Pane pane() {
        return pane;
    }


    /**
     * Redraws the line when it is possible
     */
    private void redrawLine() {
        if (routeBean.getRoute() != null) {

            MapViewParameters mvp = this.mapProperty.get();

            line.getPoints().clear();
            List<Double> pointList = new ArrayList<>();
            List<PointCh> points = routeBean.getRoute().points();

            for (PointCh pointCh : points) {
                PointWebMercator point = PointWebMercator.ofPointCh(pointCh);
                int zoomLevel = mvp.zoomLevel();
                
                pointList.add(point.xAtZoomLevel(zoomLevel));
                pointList.add(point.yAtZoomLevel(zoomLevel));
            }

            line.getPoints().addAll(pointList);
            line.setLayoutX(-mvp.x());
            line.setLayoutY(-mvp.y());
            line.setVisible(true);
        } else {
            line.setVisible(false);
        }
    }

    /**
     * Redraws the circle when it is possible
     */
    private void redrawCircle() {
        if (routeBean.getRoute() != null && !Double.isNaN(routeBean.getHighlightedPosition())) {
            PointCh pt_ch = routeBean.getRoute().pointAt(routeBean.getHighlightedPosition());
            PointWebMercator point = PointWebMercator.ofPointCh(pt_ch);

            int zoomLevel = mapProperty.get().zoomLevel();

            circle.setLayoutX(point.xAtZoomLevel(zoomLevel) + line.getLayoutX());
            circle.setLayoutY(point.yAtZoomLevel(zoomLevel) + line.getLayoutY());
            circle.setVisible(true);
        } else {
            circle.setVisible(false);
        }
    }

    /**
     * Adding a waypoint in the circle
     *
     * @param x x coordinate in the general panel
     * @param y y coordinate in the general panel
     */
    private void addWaypointInCircle(double x, double y) {
        if (routeBean.getWaypoints().size() >= 2
                && routeBean.getRoute() != null
                && !Double.isNaN(routeBean.getHighlightedPosition())) {

            PointCh pte = mapProperty
                    .get()
                    .pointAt(x, y)
                    .toPointCh();
            int closestNodeId = routeBean.getRoute().nodeClosestTo(routeBean.getHighlightedPosition());
            Waypoint waypoint = new Waypoint(pte, closestNodeId);
            int index = routeBean.indexOfNonEmptySegmentAt(routeBean.getHighlightedPosition()) + 1;

            routeBean.getWaypoints().add(index, waypoint);
        } else {
            line.setVisible(false);
            circle.setVisible(false);
        }
    }
}
