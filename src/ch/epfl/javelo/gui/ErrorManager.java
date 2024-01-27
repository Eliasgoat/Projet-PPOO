package ch.epfl.javelo.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Error manger for javelo
 *
 * @author Jan Staszewicz (341201)
 * @author Elias Mir(341277)
 */
public final class ErrorManager {

    private final VBox errorPane;
    private final Text errorText;
    private final SequentialTransition errorAnimation;
    private final static Duration FADE_IN_TIME = Duration.seconds(0.2);
    private final static Duration PAUSE_TIME = Duration.seconds(2);
    private final static Duration FADE_OUT_TIME = Duration.seconds(0.5);
    private final static double MAX_OPACITY = 0.8;
    private final static double MIN_OPACITY = 0d;

    /**
     * Constructs the error manager
     */
    public ErrorManager() {
        errorPane = new VBox();
        errorPane.getStylesheets().add("error.css");
        errorPane.setMouseTransparent(true);

        errorText = new Text();
        errorPane.getChildren().add(errorText);

        FadeTransition fadeIn = new FadeTransition(FADE_IN_TIME, errorPane);
        fadeIn.setFromValue(MIN_OPACITY);
        fadeIn.setToValue(MAX_OPACITY);
        PauseTransition pauseTransition = new PauseTransition(PAUSE_TIME);
        FadeTransition fadeOut = new FadeTransition(FADE_OUT_TIME, errorPane);
        fadeOut.setFromValue(MAX_OPACITY);
        fadeOut.setToValue(MIN_OPACITY);

        errorAnimation = new SequentialTransition(fadeIn, pauseTransition, fadeOut);
    }

    /**
     * Returns the panel on which the error messages appear
     *
     * @return the panel on which the error messages appear
     */
    public Pane pane() {
        return errorPane;
    }


    /**
     * Displays the error message temporarily on the screen, accompanied by a sound indicating the error.
     *
     * @param errorMessage message appearing when there is an error
     */
    public void displayError(String errorMessage) {
        errorAnimation.stop();

        java.awt.Toolkit.getDefaultToolkit().beep();

        errorText.setText(errorMessage);

        errorAnimation.play();
    }
}
