package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * Un point dans le systeme de coordonnees suisses
 *
 * @author Elias Mir(341277)
 */
public record PointCh(double e, double n) {
    /**
     * @param e coordonnee est
     * @param n coordonnee nord
     * @throws IllegalArgumentException si les coordonnees ne sont pas dans les limites suisses
     */
    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * Retourne la distance au carre entre le pointCh that et this
     *
     * @param that point par rapport auquel on cherche la distance au carre
     * @return la distance au carre entre le pointCh that et this
     */
    public double squaredDistanceTo(PointCh that) {
        double Ux = that.e - this.e;
        double Uy = that.n - this.n;
        return Math2.squaredNorm(Ux, Uy);
    }

    /**
     * Retourne la distance entre le pointCh that et this
     *
     * @param that point par rapport auquel on cherche la distance
     * @return la distance entre le pointCh that et this
     */
    public double distanceTo(PointCh that) {
        return Math.sqrt(squaredDistanceTo(that));
    }

    /**
     * Retourne la latitude en radians du pointCh
     *
     * @return la latitude en radians du pointCh
     */
    public double lat() {
        return Ch1903.lat(e, n);
    }

    /**
     * Retourne la longitude en radians du pointCh
     *
     * @return la longitude en radians du pointCh
     */
    public double lon() {
        return Ch1903.lon(e, n);
    }
}
