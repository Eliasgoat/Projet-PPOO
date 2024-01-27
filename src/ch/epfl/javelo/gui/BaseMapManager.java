package ch.epfl.javelo.gui;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.io.IOException;


/**
 *
 * The base map manager
 *
 * @author Elias Mir(341277)
 * @author Jan Staszewicz(341201)
 *
 */
public final class BaseMapManager {

    private final Pane pane;
    private final TileManager tileManager;
    private final WaypointsManager wayPointsManager;
    private final ObjectProperty<MapViewParameters> mapViewParametersObjectProperty;
    private boolean redrawNeeded;
    private final Canvas canvas;

    private double lastX; //saves last mouse x coordinate
    private double lastY; //saves last mouse y coordinate
    private boolean isDragged; //true on map dragging, else false

    private static final int TILE_SIZE = 256;
    private static final int MIN_ZOOM = 8;
    private static final int MAX_ZOOM = 19;

    /**
     * Constructs a new basemap manager
     *
     * @param tileManager                     the tile manager
     * @param wayPointsManager                the waypoints manager
     * @param mapViewParametersObjectProperty the map view parameters property
     */
    public BaseMapManager(TileManager tileManager,
                          WaypointsManager wayPointsManager,
                          ObjectProperty<MapViewParameters> mapViewParametersObjectProperty) {

        this.tileManager = tileManager;
        this.wayPointsManager = wayPointsManager;
        this.mapViewParametersObjectProperty = mapViewParametersObjectProperty;

        this.mapViewParametersObjectProperty.addListener((o, oV, nV) ->redrawOnNextPulse());

        this.canvas = new Canvas();
        this.pane = new Pane();

        pane.getChildren().add(canvas);

        //set canvas size
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        //Sets canvas redraw conditions
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        canvas.widthProperty().addListener((o, oV, nV) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((o, oV, nV) ->redrawOnNextPulse());

        //listener handling scrolling actions
        SimpleLongProperty minScrollTime = new SimpleLongProperty();

        pane.setOnScroll(e -> {

            MapViewParameters mvp = getProperty();

            if (e.getDeltaY() == 0d) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            int zoomDelta = (int) Math.signum(e.getDeltaY());

            int newZoomLevel = Math2.clamp(MIN_ZOOM, mvp.zoomLevel()+zoomDelta, MAX_ZOOM); //clamps zoom level

            double mouseX = e.getX();
            double mouseY = e.getY();

            PointWebMercator mouse_pt = mvp.pointAt(mouseX, mouseY);

            //sets new mapViewParameters
            this.mapViewParametersObjectProperty.setValue(new MapViewParameters(
                    newZoomLevel,
                    mouse_pt.xAtZoomLevel(newZoomLevel) - mouseX,
                    mouse_pt.yAtZoomLevel(newZoomLevel) - mouseY)
            );

            redrawOnNextPulse();
        });

        //check if mouse is being pressed for dragging
        pane.setOnMousePressed(e ->{
            lastX = e.getX();
            lastY = e.getY();
        });

        //place a waypoint
        pane.setOnMouseClicked(e ->{

            if(!isDragged) {
                this.wayPointsManager.addWaypoint(e.getX(), e.getY());
            }
            isDragged = false;
        });

        //Listener handling map dragging actions
        pane.setOnMouseDragged(e -> {

            MapViewParameters mvp = getProperty();

            isDragged = true;

            double new_X = e.getX();
            double new_Y = e.getY();

            double deltaX = lastX-new_X;
            double deltaY = lastY-new_Y;

            //moves map background according to drag
            this.mapViewParametersObjectProperty.setValue(mvp.withMinXY(mvp.x() + deltaX, mvp.y() + deltaY));

            lastX = new_X;
            lastY = new_Y;

            redrawOnNextPulse();
        });
    }

    /**
     * Returns the pane containing the base map
     *
     * @return the pane
     */
    public Pane pane(){
        return pane;
    }

    /**
     *  Redraws the map background if needed
     */
    private void redrawIfNeeded(){

        if (!redrawNeeded) return;
        redrawNeeded = false;

        MapViewParameters mvp = getProperty();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double topLeftX = mvp.topLeft().getX();
        double topLeftY = mvp.topLeft().getY();

        int xMin = (int)(topLeftX/TILE_SIZE);
        int xMax = (int)((topLeftX + canvas.getWidth())/TILE_SIZE);
        int yMin = (int)(topLeftY / TILE_SIZE);
        int yMax = (int)((topLeftY + canvas.getHeight())/TILE_SIZE);

        int zoomLevel = mvp.zoomLevel();

        gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());

        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                try{
                    if(TileManager.TileId.isValid(zoomLevel, i, j)){
                        TileManager.TileId tileId = new TileManager.TileId(zoomLevel, i, j);
                        Image image = tileManager.imageForTileAt(tileId);
                        gc.drawImage(image, TILE_SIZE*i - topLeftX, TILE_SIZE*j - topLeftY);
                    }
                } catch (IOException e){
                    //IMAGE IS NOT DRAWN
                }
            }
        }

    }

    /**
     * Redraws on next pulse
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Gets the mapViewParameters from its property
     *
     * @return the mapViewParameters
     */
    private MapViewParameters getProperty() {
        return mapViewParametersObjectProperty.get();
    }
}
