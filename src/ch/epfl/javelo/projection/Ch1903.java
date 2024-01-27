package ch.epfl.javelo.projection;

import java.lang.Math;

/**
 * Classe pour convertir entre les coordonnées WGS 84 et les coordonnées suisses
 *
 * @author Elias Mir(341277)
 */
public final class Ch1903 {

    private Ch1903() {
    }

    /**
     * Convertit de WGS 84 en coordonnes suisse
     * et retourne la coordonnee est du point
     *
     * @param lon longitude du point
     * @param lat latitude du point
     * @return coordonnes est du point
     */
    public static double e(double lon, double lat) {
        double lon0 = Math.toDegrees(lon);
        double lat0 = Math.toDegrees(lat);
        double lon1 = (1e-4) * (3600*lon0 - 26782.5);
        double lat1 = (1e-4) * (3600*lat0 - 169028.66);
        return (
                2600072.37
                + 211455.93*lon1
                - 10938.51 * lon1 * lat1
                - 0.36 * lon1 * Math.pow(lat1,2)
                - 44.54 * Math.pow(lon1,3)
        );
    }

    /**
     * Convertit de WGS 84 en coordonnees suisses
     * et retourne la coordonnee nord du point
     *
     * @param lon longitude du point
     * @param lat latitude du point
     * @return coordonnees nord du point
     */
    public static double n(double lon, double lat) {
        double lon0 = Math.toDegrees(lon);
        double lat0 = Math.toDegrees(lat);
        double lon1 = (1e-4) * (3600*lon0 - 26782.5);
        double lat1 = (1e-4) * (3600*lat0 - 169028.66);
        return (1200147.07
                + 308807.95 * lat1
                + 3745.25 * Math.pow(lon1,2)
                + 76.63 * Math.pow(lat1,2)
                - 194.56 * Math.pow(lon1,2) * lat1
                + 119.79 * Math.pow(lat1,3)
        );
    }


    /**
     * Convertit de coordonnes suisse en WGS 84
     * et retourne la longitude du point
     *
     * @param e coordonnes est du point
     * @param n coordonnees nord du point
     * @return longitude du point
     */
    public static double lon(double e, double n) {
        double x = (1e-6) * (e - 2600000);
        double y = (1e-6) * (n - 1200000);
        double lon0 = 2.6779094
                + 4.728982 * x
                + 0.791484 * x * y
                + 0.1306 * x * Math.pow(y,2)
                - 0.0436 *Math.pow(x,3);
        return Math.toRadians((lon0 * 25) / 9);
    }

    /**
     * Convertit de coordonnes suisse en WGS 84
     * et retourne la latitude du point
     *
     * @param e coordonnes est du point
     * @param n coordonnees nord du point
     * @return latitude du point
     */
    public static double lat(double e, double n) {
        double x = (1e-6) * (e - 2600000);
        double y = (1e-6) * (n - 1200000);
        double lat0 = 16.9023892
                + 3.238272 * y
                - 0.270978 * Math.pow(x,2)
                - 0.002528 * Math.pow(y,2)
                - 0.0447 * y * Math.pow(x,2)
                - 0.0140 * Math.pow(y,3);
        return Math.toRadians((lat0 * 25) / 9);
    }

}
