package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.*;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.*;

/**
 * Route bean handling route computations
 *
 *@author Elias Mir(341277)
 *@author Jan Staszewicz(341201)
 */
public final class RouteBean {

    private final Map<Pair<Integer,Integer>, Route> routeMemoryCache;
    private final RouteComputer routeComputer;

    private final ObservableList<Waypoint> waypoints;
    private final ObjectProperty<Route> routeP;
    private final DoubleProperty highlightedPosition;
    private final ObjectProperty<ElevationProfile> elevationProfileP;

    private final static int MAX_STEP_LENGTH = 5;

    /**
     * Constructs a bean for the route manager
     *
     * @param routeComputer the route computer
     */
    public RouteBean(RouteComputer routeComputer) {

        this.routeComputer = routeComputer;
        routeMemoryCache = new LinkedHashMap<>();
        routeP = new SimpleObjectProperty<>();
        elevationProfileP = new SimpleObjectProperty<>();
        waypoints = FXCollections.observableArrayList();
        highlightedPosition = new SimpleDoubleProperty(Double.NaN);

        //Creates route when new way points are added
        waypoints.addListener((Observable o) -> {

            if (waypoints.size() < 2 || !routeFoundForAllWaypoint()) {
                routeP.setValue(null);
                elevationProfileP.setValue(null);
            } else {
                Route finalRoute = createRoute();
                routeP.setValue(finalRoute);
                elevationProfileP.setValue(ElevationProfileComputer.elevationProfile(finalRoute,MAX_STEP_LENGTH));
            }

        });
    }

    /**
     * Gets the highlighted position Property
     * @return the highlighted position Property
     */
    public DoubleProperty getHighlightedPositionProperty() {
        return highlightedPosition;
    }

    /**
     * Gets the highlighted position
     * @return the highlighted position
     */
    public double getHighlightedPosition() {
        return highlightedPosition.get();
    }

    /**
     * Sets the highlighted position
     * @param highlightedPositionValue the new highlighted position
     */
    public void setHighlightedPosition(double highlightedPositionValue) {
        highlightedPosition.set(highlightedPositionValue);
    }

    /**
     * Gets the elevation profile Property
     * @return the elevation profile Property
     */
    public ReadOnlyObjectProperty<ElevationProfile> getElevationProfileProperty() {
        return elevationProfileP;
    }

    /**
     * Gets the elevation profile
     * @return the ElevationProfile
     */
    public ElevationProfile getElevationProfile() {
        return elevationProfileP.get();
    }

    /**
     * Gets the route Property
     * @return the route Property
     */
    public ReadOnlyObjectProperty<Route> getRouteProperty() {
        return routeP;
    }

    /**
     * Gets the route
     * @return the Route
     */
    public Route getRoute() {
        return routeP.get();
    }

    /**
     * Gets the waypoints Property
     * @return the Waypoint Property
     */
    public ObservableList<Waypoint> getWaypoints() {
        return waypoints;
    }

    /**
     * Gets index of the segment given the position on the route
     *
     * @param position the position on the route
     * @return the segment index
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = getRoute().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 = waypoints.get(i).closestNodeId();
            int n2 = waypoints.get(i + 1).closestNodeId();
            if (n1 == n2) index += 1;
        }
        return index;
    }

    /**
     * Checks if there is a route between two waypoints
     *
     * @param waypoint1 the first waypoint
     * @param waypoint2 the second waypoint
     * @return true if there is a route between the two given waypoints
     */
    private boolean routeFoundBetweenWaypoints(Waypoint waypoint1, Waypoint waypoint2) {
        if(waypoint1.closestNodeId() != waypoint2.closestNodeId()) {
            return routeComputer.bestRouteBetween(waypoint1.closestNodeId(), waypoint2.closestNodeId()) != null;
        }
        return true;
    }

    /**
     * Checks if there is a route for all pair of waypoints
     *
     * @return true if there is a route for all pair of waypoints
     */
    private boolean routeFoundForAllWaypoint() {
        for(int i = 0; i<waypoints.size()-1; i++){
            if(!routeFoundBetweenWaypoints(waypoints.get(i), waypoints.get(i+1))){
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a route
     *
     * @return a new MultiRoute
     */
    private Route createRoute(){

        List<Route> segments = new ArrayList<>();

        //iterate over all pairs of waypoints
        for(int i = 0; i<waypoints.size()-1; i++){

            Waypoint firstWaypoint = waypoints.get(i);
            Waypoint secondWaypoint = waypoints.get(i+1);

            if(firstWaypoint.closestNodeId() == secondWaypoint.closestNodeId()){
                continue;
            }

            Pair<Integer, Integer> waypointPair =
                    new Pair<>(firstWaypoint.closestNodeId(),
                            secondWaypoint.closestNodeId());

            if(routeMemoryCache.containsKey(waypointPair)){ // if pair is in cache
                segments.add(routeMemoryCache.get(waypointPair));
            }else{
                Route bestRoute = routeComputer
                        .bestRouteBetween(firstWaypoint.closestNodeId(), secondWaypoint.closestNodeId());
                segments.add(bestRoute);
                routeMemoryCache.put(waypointPair, bestRoute);
            }
        }

        return new MultiRoute(segments);
    }
}
