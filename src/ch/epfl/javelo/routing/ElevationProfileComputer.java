package ch.epfl.javelo.routing;


import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Computer of elevation profiles
 *
 * @author Jan Staszewicz (341201)
 */
public final class ElevationProfileComputer {

    //non-instantiable
    private ElevationProfileComputer() {}

    ;

    /**
     * Gets number of samples in elevation
     *
     * @param length        profile's length
     * @param maxStepLength max step between samples
     * @return number of samples
     */
    private static int getNumberOfSamples(double length, double maxStepLength) {
        return (int) Math.ceil(length / maxStepLength) + 1;
    }

    /**
     * Finds next index with a none NaN value
     *
     * @param i                starting index
     * @param elevationSamples the elevations samples array
     * @return next valid index
     */
    private static int findNextValueIndex(int i, float[] elevationSamples) {

        while (i < elevationSamples.length && Float.isNaN(elevationSamples[i])) {
            i++;
        }
        return i;
    }

    /**
     * Finds next index with a NaN value
     *
     * @param i                starting index
     * @param elevationSamples the elevations samples array
     * @return next valid index
     */
    private static int findNextNaNIndex(int i, float[] elevationSamples) {

        while (i < elevationSamples.length && !Float.isNaN(elevationSamples[i])) {
            i++;
        }

        return i;
    }

    /**
     * Finds previous index with a value
     *
     * @param i                starting index
     * @param elevationSamples the elevations samples array
     * @return next valid index
     */
    private static int findPreviousValueIndex(int i, float[] elevationSamples) {

        while (i >= 0 && Float.isNaN(elevationSamples[i])) {
            i--;
        }

        return i;
    }


    /**
     * Fills NaN values in elevation samples by reference
     *
     * @param elevationSamples the elevation samples array
     */
    private static void fillHoles(float[] elevationSamples, double spacing) {

        int nextValueIndex = 0;

        //check if hole at the beginning
        if (Float.isNaN(elevationSamples[0])) {
            nextValueIndex = findNextValueIndex(0, elevationSamples);

            if (nextValueIndex == elevationSamples.length) {
                Arrays.fill(elevationSamples, 0, elevationSamples.length, 0);
            } else {
                Arrays.fill(elevationSamples, 0, nextValueIndex, elevationSamples[nextValueIndex]);
            }
        }

        //check if ends with hole
        if (Float.isNaN(elevationSamples[elevationSamples.length - 1])) {
            int previousValueIndex = findPreviousValueIndex(elevationSamples.length - 2, elevationSamples);
            Arrays.fill(elevationSamples, previousValueIndex + 1, elevationSamples.length, elevationSamples[previousValueIndex]);
        }

        //file intermediary holes
        for (int i = nextValueIndex; i < elevationSamples.length; i++) {
            int nextNaNIndex = findNextNaNIndex(0, elevationSamples);
            int nextValueIndexI = findNextValueIndex(nextNaNIndex + 1, elevationSamples);

            if (nextNaNIndex == elevationSamples.length) {
                break;
            }

            float y0 = elevationSamples[nextNaNIndex - 1];
            float y1 = elevationSamples[nextValueIndexI];
            int n = nextValueIndexI - nextNaNIndex + 1;

            //interpolate on hole
            for (int j = 0; j < nextValueIndexI - nextNaNIndex; j++) {
                elevationSamples[nextNaNIndex + j] = (float) Math2.interpolate(y0, y1, (j + 1.0) / n);
            }

            i = nextValueIndex;
        }
    }

    /**
     * Gets route elevation profile keeping spacing smaller than maxStepLength.
     *
     * @param route         the route
     * @param maxStepLength the max step length
     * @return new ElevationProfile
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {

        //init vars
        double length = route.length();
        int number_of_samples = getNumberOfSamples(length, maxStepLength);
        double spacing = length / (number_of_samples - 1);

        Preconditions.checkArgument(spacing > 0);
        Preconditions.checkArgument(number_of_samples >= 0);

        float[] elevationSamples = new float[number_of_samples];

        //fill with elevationAt
        for (int l = 0; l < number_of_samples; l++) {
            elevationSamples[l] = (float) route.elevationAt(l * spacing);
        }

        //fill all holes
        fillHoles(elevationSamples, spacing);

        return new ElevationProfile(length, elevationSamples);
    }


}
