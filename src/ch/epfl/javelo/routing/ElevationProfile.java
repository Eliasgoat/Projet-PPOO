package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;

/**
 * Le profil en long d'un itinéraire simple ou multiple
 *
 * @author Elias Mir(341277)
 */
public final class ElevationProfile {

    private final double length;
    private final float[] elevationSamples;

    /**
     * Construit le profil en long d'un itinéraire de longueur length (en mètres) et dont les échantillons d'altitude,
     * répartis uniformément le long de l'itinéraire, sont contenus dans elevationSamples
     *
     * @param length           longueur de l'itinéraire
     * @param elevationSamples échantillons d'altitude
     * @throws IllegalArgumentException si la longueur est négative ou nulle,
     *                                  ou si le tableau d'échantillons contient moins de 2 éléments
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(
                length > 0
                        && elevationSamples.length >= 2
        );
        this.length = length;
        this.elevationSamples = elevationSamples;
    }

    /**
     * Retourne la longueur du profil, en mètres
     *
     * @return la longueur du profil, en mètres
     */
    public double length() {
        return length;
    }

    /**
     * Retourne l'altitude minimum du profil, en mètres
     *
     * @return l'altitude minimum du profil, en mètres
     */
    public double minElevation() {
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        for (float elevationSample : elevationSamples) {
            s.accept(elevationSample);
        }
        return s.getMin();
    }

    /**
     * Retourne l'altitude maximum du profil, en mètres
     *
     * @return l'altitude maximum du profil, en mètres
     */
    public double maxElevation() {
        DoubleSummaryStatistics s = new DoubleSummaryStatistics();
        for (float elevationSample : elevationSamples) {
            s.accept(elevationSample);
        }
        return s.getMax();
    }

    /**
     * Retourne le dénivelé positif total du profil, en mètres
     *
     * @return le dénivelé positif total du profil, en mètres
     */
    public double totalAscent() {
        double totalAscent = 0;
        for (int i = 1; i < elevationSamples.length; i++) {
            if ((elevationSamples[i] - elevationSamples[i - 1]) >= 0) {
                totalAscent += elevationSamples[i] - elevationSamples[i - 1];
            }
        }
        return totalAscent;
    }

    /**
     * Retourne le dénivelé négatif total du profil, en mètres
     *
     * @return le dénivelé négatif total du profil, en mètres
     */
    public double totalDescent() {
        double totalDescent = 0;
        for (int i = 1; i < elevationSamples.length; i++) {
            if ((elevationSamples[i] - elevationSamples[i - 1]) <= 0) {
                totalDescent += elevationSamples[i - 1] - elevationSamples[i];
            }
        }
        return totalDescent;
    }

    /**
     * Retourne l'altitude du profil à la position donnée
     *
     * @param position position donnée
     * @return l'altitude du profil à la position donnée
     */
    public double elevationAt(double position) {
        return Functions
                .sampled(elevationSamples, length)
                .applyAsDouble(position);
    }
}
