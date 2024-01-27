package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

/**
 * ElevationProfile manager
 *
 * @author Elias Mir(341277)
 * @author Jan Staszewicz(341201)
 */
public final class ElevationProfileManager {

    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile;
    private final ReadOnlyDoubleProperty highlightedPosition;
    private final BorderPane borderPane;
    private final DoubleProperty mousePositionOnProfileProperty;
    private Affine screenToWorld;
    private Affine worldToScreen;
    private final ObjectProperty<Rectangle2D> rectangleProperty;
    private final Pane pane;
    private final Polygon polygon;
    private final Path grid;
    private final Group group;
    private Line line;
    private double width;
    private double height;

    private final static int[] POS_STEPS = {1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000};
    private final static int[] ELE_STEPS = {5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000};
    private final static Font FONT = Font.font("Avenir", 10);
    private final static Insets INSETS = new Insets(10, 10, 20, 40);
    private final static double METERS_IN_ONE_KM = 1000d;
    private final static int MIN_ELEVATION_PIXEL = 25;
    private final static int MIN_POSITION_PIXEL = 50;

    /**
     * Constructs the elevation profile manager
     *
     * @param elevationProfile    the elevation profile property
     * @param highlightedPosition the highlighted position property
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfile,
                                   ReadOnlyDoubleProperty highlightedPosition) {

        //Initialization of the attributes
        this.elevationProfile = elevationProfile;
        this.highlightedPosition = highlightedPosition;

        screenToWorld = new Affine();
        worldToScreen = new Affine();

        rectangleProperty = new SimpleObjectProperty<>(Rectangle2D.EMPTY);
        mousePositionOnProfileProperty = new SimpleDoubleProperty(Double.NaN);

        borderPane = new BorderPane();
        borderPane.getStylesheets().add("elevation_profile.css");

        pane = new Pane();
        borderPane.setCenter(pane);

        grid = new Path();
        grid.setId("grid");
        pane.getChildren().add(grid);

        group = new Group();
        pane.getChildren().add(group);

        polygon = new Polygon();
        polygon.setId("profile");
        pane.getChildren().add(polygon);

        //Redraws the profile if the pane's height changes
        pane.heightProperty().addListener((o, oV, nV) -> {
            if (this.elevationProfile.get() != null) {
                redrawPolygon();
                createGrid();
            }
        });

        //Redraws the profile if the pane's width changes
        pane.widthProperty().addListener((o, oV, nV) -> {
            if (this.elevationProfile.get() != null) {
                redrawPolygon();
                createGrid();
            }
        });

        createLine();

        pane.setOnMouseMoved(e -> {
            if (rectangleProperty.get().contains(e.getX(), e.getY())) {
                mousePositionOnProfileProperty.set(screenToWorld.transform(e.getX(), e.getY()).getX());
            } else {
                mousePositionOnProfileProperty.set(Double.NaN);
            }
        });

        pane.setOnMouseExited(e -> mousePositionOnProfileProperty.set(Double.NaN));

        //Redraws the whole pane if the elevation profile changes
        this.elevationProfile.addListener((o, oV, nV) -> {
            if (this.elevationProfile.get() != null) {
                drawDataInfo();
                redrawPolygon();
                createGrid();
            }
        });
    }

    /**
     * Returns the pane containing the profile drawing
     *
     * @return the pane containing the profile drawing
     */
    public Pane pane() {
        return borderPane;
    }

    /**
     * Returns the property containing the position of the mouse pointer along the profile
     *
     * @return the property containing the position of the mouse pointer along the profile
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionOnProfileProperty;
    }

    /**
     * Redraws the polygon
     */
    private void redrawPolygon() {
        polygon.getPoints().clear();

        width = (int) (pane.getWidth() - INSETS.getLeft() - INSETS.getRight());
        height = (int) (pane.getHeight() - INSETS.getBottom() - INSETS.getTop());
        width = Math.max(width, 0);
        height = Math.max(height, 0);

        rectangleProperty.set(new Rectangle2D(INSETS.getLeft(), INSETS.getTop(), width, height));
        Rectangle2D rectangle = rectangleProperty.get();

        Affine affine = new Affine();
        affine.prependTranslation(-rectangle.getMinX(), -rectangle.getMinY());
        affine.prependScale(elevationProfile.get().length() / rectangle.getWidth(),
                (elevationProfile.get().minElevation() - elevationProfile.get().maxElevation()) / rectangle.getHeight());

        affine.prependTranslation(0, elevationProfile.get().maxElevation());
        screenToWorld = affine;

        try {
            worldToScreen = screenToWorld.createInverse();
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }

        createGraph();
    }

    /**
     * Recreates the grid
     */
    private void createGrid() {

        grid.getElements().clear();
        group.getChildren().clear();

        int x_step = POS_STEPS[POS_STEPS.length - 1];
        int y_step = ELE_STEPS[ELE_STEPS.length - 1];

        //finds the step needed for the position values
        for (int pos_step : POS_STEPS) {
            double x = worldToScreen.deltaTransform(pos_step, 0).getX();
            if (x >= MIN_POSITION_PIXEL) {
                x_step = pos_step;
                break;
            }
        }

        //finds the step needed for the elevation values
        for (int ele_step : ELE_STEPS) {
            double y = -worldToScreen.deltaTransform(0, ele_step).getY();
            if (y >= MIN_ELEVATION_PIXEL) {
                y_step = ele_step;
                break;
            }
        }

        //vertical lines
        for (int i = 0; i <= elevationProfile.get().length(); i += x_step) {

            double step = worldToScreen.deltaTransform(i, 0).getX();

            grid.getElements().addAll(
                    new MoveTo(step + INSETS.getLeft(), INSETS.getTop()),
                    new LineTo(step + INSETS.getLeft(), rectangleProperty.get().getHeight() + INSETS.getTop())
            );

            createPositionLabel(
                    step + INSETS.getLeft(),
                    rectangleProperty.get().getHeight() + INSETS.getTop(),
                    (int) (i / METERS_IN_ONE_KM)
            );
        }

        double firstValue = (elevationProfile.get().minElevation() + (y_step - elevationProfile.get().minElevation() % y_step));

        //horizontal lines
        for (double i = firstValue; i <= elevationProfile.get().maxElevation(); i += y_step) {

            double step = worldToScreen.deltaTransform(0, elevationProfile.get().minElevation() - i).getY();

            grid.getElements().addAll(
                    new MoveTo(INSETS.getLeft(), rectangleProperty.get().getHeight() - step + INSETS.getTop()),
                    new LineTo(
                            rectangleProperty.get().getWidth() + INSETS.getLeft(),
                            rectangleProperty.get().getHeight() - step + INSETS.getTop()
                    )
            );

            createElevationLabel(
                    INSETS.getLeft(),
                    rectangleProperty.get().getHeight() - step + INSETS.getTop(),
                    (int) i
            );
        }
    }

    /**
     * Draws the data of the elevation profile below the profile drawing
     */
    private void drawDataInfo() {
        VBox vbox = new VBox();
        vbox.setId("profile_data");
        Text text = new Text("Longueur : %.1f km".formatted(elevationProfile.get().length() / METERS_IN_ONE_KM) +
                "     Montée : %.0f m".formatted(elevationProfile.get().totalAscent()) +
                "     Descente : %.0f m".formatted(elevationProfile.get().totalDescent()) +
                "     Altitude : de %.0f m à %.0f m".formatted(elevationProfile.get().minElevation(),
                        elevationProfile.get().maxElevation()));
        vbox.getChildren().add(text);
        borderPane.setBottom(vbox);
    }

    /**
     * Creates the graph of the elevation profile
     */
    private void createGraph() {
        ElevationProfile profile = elevationProfile.get();

        for (int i = 0; i < width; i++) {
            double x = screenToWorld.transform(i + INSETS.getLeft(), 0).getX();
            double y = profile.elevationAt(x);
            Point2D newPoint = worldToScreen.transform(0, y);
            polygon.getPoints().addAll((double) i + INSETS.getLeft(), newPoint.getY());
        }
        
        //draws the bottom line
        for (int i = (int) width; i > 0; i--) {
            polygon.getPoints().addAll((double) i + INSETS.getLeft(), INSETS.getTop() + height);
        }
    }

    /**
     * Creates the position label at coordinate (x,y) and with value n
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param n position value
     */
    private void createPositionLabel(double x, double y, int n) {

        Text label = new Text();
        label.setFont(FONT);
        label.setText(Integer.toString(n));
        label.setTextOrigin(VPos.TOP);
        label.setX(x - label.prefWidth(0) / 2);
        label.setY(y);
        label.getStyleClass().add("grid_label");
        label.getStyleClass().add("vertical");

        group.getChildren().add(label);
    }

    /**
     * Creates the elevation label at coordinate (x,y) and with value n
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param n elevation value
     */
    private void createElevationLabel(double x, double y, int n) {

        Text label = new Text();
        label.setFont(FONT);
        label.setText(Integer.toString(n));
        label.setTextOrigin(VPos.CENTER);
        label.setX(x - (label.prefWidth(0) + 2));
        label.setY(y - (label.prefHeight(0)) / 2);
        label.getStyleClass().add("grid_label");
        label.getStyleClass().add("horizontal");

        group.getChildren().add(label);
    }

    /**
     * Creates the line in the ElevationProfile pane
     */
    private void createLine() {

        line = new Line();
        line.layoutXProperty().bind(
            Bindings.createDoubleBinding(
                    () -> worldToScreen.transform(highlightedPosition.get(), 0).getX(),
                        highlightedPosition)
                );
        line.visibleProperty().bind(highlightedPosition.greaterThanOrEqualTo(0));
        line.startYProperty().bind(Bindings.select(rectangleProperty, "minY"));
        line.endYProperty().bind(Bindings.select(rectangleProperty, "maxY"));

        pane.getChildren().add(line);
    }
}
