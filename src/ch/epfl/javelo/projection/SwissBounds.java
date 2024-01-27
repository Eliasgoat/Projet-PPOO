package ch.epfl.javelo.projection;

/**
 * Limites pour les coordonnees suisses
 *
 * @author Elias Mir(341277)
 */
public final class SwissBounds {

    //Coordonnee minimal de la coordonnee E
    public static final double MIN_E = 2485000;
    //Coordonnee maximal de la coordonnee E
    public static final double MAX_E = 2834000;
    //Coordonnee minimal de la coordonnee N
    public static final double MIN_N = 1075000;
    //Coordonnee maximal de la coordonnee N
    public static final double MAX_N = 1296000;
    //Largueur d'un secteur
    public static final double WIDTH = MAX_E - MIN_E;
    //Hauteur d'un secteur
    public static final double HEIGHT = MAX_N - MIN_N;

    private SwissBounds() {
    }

    /**
     * Verifie que le point est dans les bonnes limites
     *
     * @param e coordonnes est
     * @param n coordonnes nord
     * @return vrai si le point est dans les limites, faux sinon
     */
    public static boolean containsEN(double e, double n) {
        return (e >= MIN_E) && (e <= MAX_E) && (n >= MIN_N) && (n <= MAX_N);
    }
}
