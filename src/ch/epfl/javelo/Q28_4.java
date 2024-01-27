package ch.epfl.javelo;

/**
 * Nombre de type Q28_4
 *
 * @author Elias Mir(341277)
 */
public final class Q28_4 {

    private Q28_4() {
    }

    /**
     * Multiplie l'argument par 2^4 = 16
     *
     * @param i entier donne
     * @return la valeur Q28.4 correspondant à l'entier donné
     */
    public static int ofInt(int i) {
        return 16 * i;
    }

    /**
     * Multiplie par 2^-4
     *
     * @param q28_4 valeur Q28.4 donnée
     * @return la valeur de type double égale à la valeur Q28.4 donnée en double
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -4);
    }

    /**
     * Multiplie par 2^-4
     *
     * @param q28_4 valeur Q28.4 donnée
     * @return la valeur de type double égale à la valeur Q28.4 donnée en float
     */
    public static float asFloat(int q28_4) {
        return Math.scalb(q28_4, -4);
    }
}
