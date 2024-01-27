package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;


/**
 * Main program for JaVelo
 *
 * @author Jan Staszewicz (341201)
 * @author Elias Mir(341277)
 */
public final class JaVelo extends Application {


    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage primaryStage) throws Exception {
        Graph graph = Graph.loadFrom(Path.of("javelo-data"));
        Path cacheBasePath = Path.of("osm-cache");
        String serverHost = "tile.openstreetmap.org";
        CostFunction costFunction = new CityBikeCF(graph);

        TileManager tileManager = new TileManager(cacheBasePath, serverHost);
        ErrorManager errorManager = new ErrorManager();
        RouteBean routeBean = new RouteBean(new RouteComputer(graph, costFunction));

        AnnotatedMapManager annotatedMapManager =
                new AnnotatedMapManager(graph, tileManager, routeBean, errorManager::displayError);
        
        ElevationProfileManager elevationProfileManager =
                new ElevationProfileManager(
                        routeBean.getElevationProfileProperty(),
                        routeBean.getHighlightedPositionProperty()
                );

        StackPane stackPane = new StackPane(annotatedMapManager.pane(), errorManager.pane());

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        SplitPane.setResizableWithParent(elevationProfileManager.pane(), false);
        splitPane.getItems().add(stackPane);

        //Adds or change or removes the elevation profile pane depending on the situation
        routeBean.getRouteProperty().addListener((o, oV, nV) ->{

            routeBean.getHighlightedPositionProperty().bind(
                    Bindings.when(annotatedMapManager.mousePositionOnRouteProperty().greaterThanOrEqualTo(0))
                            .then(annotatedMapManager.mousePositionOnRouteProperty())
                            .otherwise(elevationProfileManager.mousePositionOnProfileProperty())
            );

            if(routeBean.getRoute() == null){
                splitPane.getItems().remove(elevationProfileManager.pane());
            }else if(routeBean.getRoute() != null && oV == null){
                splitPane.getItems().add(elevationProfileManager.pane());
            }
        });

        //Creating the menu bar
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Fichier");
        MenuItem menuItem = new MenuItem("ExporterGPX");
        menuBar.getMenus().add(menu);
        menu.getItems().add(menuItem);
        menuBar.setUseSystemMenuBar(true);

        //Creates the gpx file representing the route
        menuItem.setOnAction(e->{
            try{
                GpxGenerator.writeGpx( "javelo.gpx", routeBean.getRoute(), routeBean.getElevationProfile());
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        });

        //Disables the menu item if the route is null
        menuItem.disableProperty().bind(
                Bindings.createBooleanBinding(() ->
                        routeBean.getRouteProperty().get() == null , routeBean.getRouteProperty())
        );

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(splitPane);
        borderPane.setTop(menuBar);

        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.setTitle("JaVelo");
        primaryStage.show();
    }
}
