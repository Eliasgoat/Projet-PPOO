package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

/**
 * Les coordonnées Web Mercator
 *
 * @author Elias Mir(341277)
 */
public final class WebMercator {
    private WebMercator() {}

    /**
     * Retourne  la coordonnée x de la projection d'un point se trouvant à la longitude donnee
     *
     * @param lon longitude en radians
     * @return la coordonnée x de la projection d'un point se trouvant à la longitude donnee
     */
    public static double x(double lon) {
        return (lon + Math.PI) / (2*Math.PI);
    }

    /**
     * Retourne  la coordonnée y de la projection d'un point se trouvant à la latitude donnee
     *
     * @param lat latitude en radians
     * @return la coordonnée y de la projection d'un point se trouvant à la latitude donnee
     */
    public static double y(double lat) {
        return (Math.PI - Math2.asinh(Math.tan(lat))) / (2*Math.PI);
    }

    /**
     * Retourne la longitude, en radians, d'un point dont la projection se trouve à la coordonnée x donnée
     *
     * @param x coordonnees x donnee
     * @return la longitude, en radians, d'un point dont la projection se trouve à la coordonnée x donnée
     */
    public static double lon(double x) {
        return 2*Math.PI*x - Math.PI;
    }

    /**
     * Retourne la longitude, en radians, d'un point dont la projection se trouve à la coordonnée x donnée
     *
     * @param y coordonnees y donnee
     * @return la longitude, en radians, d'un point dont la projection se trouve à la coordonnée x donnée
     */
    public static double lat(double y) {
        return Math.atan(Math.sinh(Math.PI - 2*Math.PI*y));
    }


}
