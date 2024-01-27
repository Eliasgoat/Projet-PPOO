package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * Une fonction
 *
 * @author Elias Mir(341277)
 */
public final class Functions {
    private Functions() {
    }

    /**
     * Retourne la fonction obtenue par interpolation linéaire entre les échantillons samples,
     * espacés régulièrement et couvrant la plage allant de 0 à xMax
     *
     * @param samples échantillon de valeurs
     * @param xMax    fin de la plage couverte par l'echantillon
     * @return la fonction obtenue par interpolation linéaire entre les échantillons samples,
     * espacés régulièrement et couvrant la plage allant de 0 à xMax
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        return new Sampled(samples, xMax);
    }

    /**
     * Retourne une fonction Contante tel que f(x) = y
     *
     * @param y valeur constante
     * @return une fonction Contante tel que f(x) = y
     */
    public static DoubleUnaryOperator constant(double y) {
        return new Constant(y);
    }


    /**
     * Fonction constante
     */
    private static final class Constant implements DoubleUnaryOperator {

        private final double y;

        private Constant(double y) {
            this.y = y;
        }

        /**
         * Retourne la valeur contante y quelque soit la valeur de x
         *
         * @param x valeur dont on cherche l'image
         * @return la valeur contante y quelque soit la valeur de x
         */
        @Override
        public double applyAsDouble(double x) {
            return y;
        }
    }

    /**
     * Fonction obtenu par interpolation lineaire
     */
    private static final class Sampled implements DoubleUnaryOperator {

        private final double xMax;
        private final float[] samples;

        private Sampled(float[] samples, double xMax) {
            Preconditions.checkArgument(
                    xMax > 0
                            && samples.length >= 2
            );
            this.xMax = xMax;
            this.samples = samples;
        }

        /**
         * Retourne la valeur de la fonction en x obtenu par interpolation lineaire de samples
         *
         * @param x valeur dont on cherche l'image
         * @return la valeur de la fonction en x obtenu par interpolation lineaire de samples
         */
        @Override
        public double applyAsDouble(double x) {
            if (x < 0) {
                return samples[0];
            } else if (x >= xMax) {
                return samples[samples.length - 1];
            } else {
                double delta = xMax / (samples.length - 1);
                int leftIndex = (int) Math.floor(x / delta);
                return Math2.interpolate(
                        samples[leftIndex],
                        samples[leftIndex + 1],
                        (x - leftIndex * delta) / delta
                );
            }
        }
    }
}


