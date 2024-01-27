package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * The annotated map manger for JaVelo
 *
 * @author Jan Staszewicz (341201)
 * @author Elias Mir(341277)
 */
public final class AnnotatedMapManager {

    private final Graph graph;
    private final TileManager tileManager;
    private final RouteBean routeBean;
    private final Consumer<String> errorConsumer;
    private final WaypointsManager waypointsManager;
    private final BaseMapManager baseMapManager;
    private final RouteManager routeManager;
    private final DoubleProperty mousePositionOnRouteProperty;
    private final ObjectProperty<MapViewParameters> mapViewParametersP;
    private final StackPane mainPane;
    private final static int INITAL_ZOOM_LEVEL = 12;
    private final static int INITAL_TOP_LEFT_X_COORDINATE = 543200;
    private final static int INITAL_TOP_LEFT_Y_COORDINATE = 370650;
    private final static int DISTANCE_FROM_CIRCLE_ALLOWED = 15;

    /**
     * Constructs the annotated map manager
     *
     * @param graph         the graph of the road network used to manage the waypoints
     * @param tileManager   the OpenStreetMap tile manager used for the basemap manager
     * @param routeBean     the route bean for managing the route
     * @param errorConsumer the consumer used to signal an error
     */
    public AnnotatedMapManager(Graph graph,
                               TileManager tileManager,
                               RouteBean routeBean,
                               Consumer<String> errorConsumer) {

        this.graph = graph;
        this.tileManager = tileManager;
        this.routeBean = routeBean;
        this.errorConsumer = errorConsumer;

        MapViewParameters mapViewParameters =
                new MapViewParameters(INITAL_ZOOM_LEVEL, INITAL_TOP_LEFT_X_COORDINATE, INITAL_TOP_LEFT_Y_COORDINATE);
        mapViewParametersP = new SimpleObjectProperty<>(mapViewParameters);
        waypointsManager =
                new WaypointsManager(this.graph, mapViewParametersP, this.routeBean.getWaypoints(), this.errorConsumer);
        baseMapManager =
                new BaseMapManager(this.tileManager, waypointsManager, mapViewParametersP);
        routeManager =
                new RouteManager(this.routeBean, mapViewParametersP);

        mousePositionOnRouteProperty = new SimpleDoubleProperty(Double.NaN);

        mainPane = new StackPane(baseMapManager.pane(), routeManager.pane(), waypointsManager.pane());
        mainPane.getStylesheets().add("map.css");

        //Checks if the mouse is close enough for the circle to be drawn
        mainPane.setOnMouseMoved(e -> {

            if (this.routeBean.getRoute() != null) {
                PointWebMercator mousePosition = mapViewParametersP.get().pointAt(e.getX(), e.getY());
                if (mousePosition.toPointCh() != null) {
                    RoutePoint routePoint = this.routeBean.getRoute().pointClosestTo(mousePosition.toPointCh());
                    PointWebMercator point = PointWebMercator.ofPointCh(routePoint.point());

                    double distance = Math2.norm(
                            mapViewParametersP.get().viewX(point) - e.getX(),
                            mapViewParametersP.get().viewY(point) - e.getY()
                    );
                    
                    if (distance <= DISTANCE_FROM_CIRCLE_ALLOWED) {
                        mousePositionOnRouteProperty.set(routePoint.position());
                    } else {
                        mousePositionOnRouteProperty.set(Double.NaN);
                    }
                }
            }
        });

        mainPane.setOnMouseExited(e -> {
            mousePositionOnRouteProperty.set(Double.NaN);
        });
    }

    /**
     * Returns the pane containing the annotated map
     *
     * @return the pane containing the annotated map
     */
    public StackPane pane() {
        return mainPane;
    }

    /**
     * Returns the property containing the position of the mouse pointer along the route
     *
     * @return the property containing the position of the mouse pointer along the route
     */
    public DoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRouteProperty;
    }


}
