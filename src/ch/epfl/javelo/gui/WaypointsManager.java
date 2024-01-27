package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.function.Consumer;

/**
 * Waypoints manager for the gui
 *
 * @author Elias Mir(341277)
 * @author Jan Staszewicz(341201)
 */
public final class WaypointsManager {

    private final Graph graph;
    private final ObjectProperty<MapViewParameters> mapviewParametersP;
    private final ObservableList<Waypoint> wayPoints;
    private final Consumer<String> errorConsumer;
    private final Pane pane;

    //event attributes
    private double lastX; //last mouse x coordinate on pane
    private double lastY; //last mouse y coordinate on pane
    private double shiftX; //the mouse x distance to the point
    private double shiftY; //the mouse y distance to the point
    private boolean isDragged; //true during drag

    private final static int SEARCH_RANGE = 500;

    /**
     * Constructs a new waypoint manager
     *
     * @param graph              the JaVelo graph
     * @param mapviewParametersP the MapViewParameters property object
     * @param wayPoints          the observable list of waypoints
     * @param errorConsumer      the string consumer
     */
    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> mapviewParametersP,
                            ObservableList<Waypoint> wayPoints,
                            Consumer<String> errorConsumer) {

        this.graph = graph;
        this.mapviewParametersP = mapviewParametersP;
        this.wayPoints = wayPoints;
        this.errorConsumer = errorConsumer;

        pane = new Pane();
        pane.setPickOnBounds(false);

        //draws already given points (in case waypoints is not null at the beginning)
        updatePoints();

        //Listener that replaces all points on mapViewParameters changes
        this.mapviewParametersP.addListener((p, oldS, newS) -> {
            repositionAllPoints();
        });

        //Listener that redraw all points on waypoints list changes
        this.wayPoints.addListener((Observable o) -> {
            if (!isDragged) {
                updatePoints();
            }
        });
    }


    /**
     * returns the waypointsManager pane
     *
     * @return the pane
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Updates all waypoints and add the needed listeners
     */
    private void updatePoints() {

        pane.getChildren().clear();

        //for each point
        for (int i = 0; i < wayPoints.size(); i++) {

            String type;

            if (i == 0) {
                type = "first";
            } else if (i == wayPoints.size() - 1) {
                type = "last";
            } else {
                type = "middle";
            }

            Group group = createMarker(type);

            placeWaypoint(wayPoints.get(i), group);

            setGroupListeners(i, group);

            pane.getChildren().add(i, group);
        }
    }

    /**
     * Sets listeners on the point group
     *
     * @param waypointIndex the index of the point int the wayPoints list
     * @param group         the group of the point
     */
    private void setGroupListeners(int waypointIndex, Group group) {

        //listener handling mouse release action
        group.setOnMouseReleased(e -> {

            if (!isDragged) {
                wayPoints.remove(waypointIndex);
            } else {

                PointCh pte = PointWebMercator.of(
                                mapviewParametersP.get().zoomLevel(),
                                mapviewParametersP.get().x() + lastX,
                                mapviewParametersP.get().y() + lastY)
                        .toPointCh();

                int closestNodeId = graph.nodeClosestTo(pte, SEARCH_RANGE);
                Waypoint waypoint;

                if (closestNodeId != -1) {
                    waypoint = new Waypoint(pte, closestNodeId);
                } else { // if there isn't a node id in a 500 meters square range
                    waypoint = wayPoints.get(waypointIndex);
                    errorConsumer.accept("Aucune route à proximité !");
                }

                wayPoints.set(waypointIndex, waypoint);
                placeWaypoint(waypoint, group);

                isDragged = false;
            }
        });

        //listener handling mouse press actions
        group.setOnMousePressed(e -> {

            shiftX = e.getX();
            shiftY = e.getY();

            PointWebMercator currentPoint = PointWebMercator.ofPointCh(wayPoints.get(waypointIndex).pointCh());

            lastX = currentPoint.xAtZoomLevel(mapviewParametersP.get().zoomLevel()) - mapviewParametersP.get().x();
            lastY = currentPoint.yAtZoomLevel(mapviewParametersP.get().zoomLevel()) - mapviewParametersP.get().y();

        });

        //listener handling mouse drag actions
        group.setOnMouseDragged(e -> {

            isDragged = true;

            lastX += e.getX() - shiftX; // shifting to keep group at the same place relative to the mouse
            lastY += e.getY() - shiftY;

            group.setLayoutX(lastX);
            group.setLayoutY(lastY);

        });
    }

    /**
     * Adds a waypoint to the waypointManager (condition : the point must be valid)
     *
     * @param x the mouse x coordinates
     * @param y the mouse y coordinates
     */
    public void addWaypoint(double x, double y) {

        //take x y and convert to webmercator relative to the corner
        PointCh pte = mapviewParametersP.get()
                .pointAt(x, y)
                .toPointCh();

        int closestNodeId = graph.nodeClosestTo(pte, SEARCH_RANGE);

        if (closestNodeId != -1 && pte != null) { //if there is a node in a 500 meters square range

            Waypoint waypoint = new Waypoint(pte, closestNodeId);
            wayPoints.add(waypoint);
        } else {
            errorConsumer.accept("Aucune route à proximité !");
        }
    }

    /**
     * Repositions all points from the wayPoints list
     */
    private void repositionAllPoints() {

        for (int i = 0; i < wayPoints.size(); i++) {
            Group group = (Group) pane.getChildren().get(i);
            placeWaypoint(wayPoints.get(i), group);
        }
    }

    /**
     * Creates a waypoint marker
     *
     * @param type the type of marker
     * @return the marker's group
     */
    private Group createMarker(String type) {

        Group group = new Group();
        group.getStyleClass().add("pin");
        group.getStyleClass().add(type);

        SVGPath outline = new SVGPath();
        group.getChildren().add(outline);
        outline.getStyleClass().add("pin_outside");
        outline.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");


        SVGPath innerCircle = new SVGPath();
        innerCircle.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
        innerCircle.getStyleClass().add("pin_inside");
        group.getChildren().add(innerCircle);

        return group;
    }

    /**
     * Places a waypoint according to the given Waypoint instance
     *
     * @param waypoint the Waypoint instance
     * @param group    the waypoint group
     */
    private void placeWaypoint(Waypoint waypoint, Group group) {

        PointWebMercator pte = PointWebMercator.ofPointCh(waypoint.pointCh());

        int zoomLevel = mapviewParametersP.get().zoomLevel();

        double xPos = pte.xAtZoomLevel(zoomLevel) - mapviewParametersP.get().x();
        double yPos = pte.yAtZoomLevel(zoomLevel) - mapviewParametersP.get().y();

        group.setLayoutX(xPos);
        group.setLayoutY(yPos);
    }
}
