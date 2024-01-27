package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

import static java.lang.Math.scalb;

/**
 * Un point dans le système Web Mercator
 *
 * @author Elias Mir(341277)
 */
public record PointWebMercator(double x, double y) {

    /**
     * @param x la coordonnée x du point
     * @param y la coordonnée y du point
     * @throws IllegalArgumentException si x ou y n'est pas compris dans l'intervalle [0;1]
     */
    public PointWebMercator {
        Preconditions.checkArgument(
                x >= 0
                        && x <= 1
                        && y <= 1
                        && y >= 0
        );
    }

    /**
     * Retourne le pointCh dont les coordonnées sont x et y au niveau de zoom zoomLevel
     *
     * @param zoomLevel niveau de zoom
     * @param x         coordonnee x du point
     * @param y         coordonnee y du point
     * @return le pointCh dont les coordonnées sont x et y au niveau de zoom zoomLevel
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(scalb(x, -(zoomLevel + 8)), scalb(y, -(zoomLevel + 8)));
    }

    /**
     * Retourne un objet PointWebMercator dont les coordonnees sont transformées à partir des cordonnees de pointCh
     *
     * @param pointCh objet PointCh en coordonees suisses
     * @return un objet PointWebMercator dont les coordonnees sont transformées à partir des cordonnees de pointCh
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        double x = WebMercator.x(pointCh.lon());
        double y = WebMercator.y(pointCh.lat());
        return new PointWebMercator(x, y);
    }

    /**
     * Calcule y * 2^(zoomLevel+8)
     *
     * @param zoomLevel niveau de zoom
     * @return une nouvelle coordonnee de type double à partir de y adaptee au niveau de zoom
     */
    public double yAtZoomLevel(int zoomLevel) {
        return scalb(y, zoomLevel + 8);
    }

    /**
     * Calcule x*2^(zoomLevel+8)
     *
     * @param zoomLevel niveau de zoom
     * @return une nouvelle coordonnee de type double à partir de x adaptee au niveau de zoom
     */
    public double xAtZoomLevel(int zoomLevel) {
        return scalb(x, zoomLevel + 8);
    }

    /**
     * Retourne la latitude lat du point, en radians
     *
     * @return la latitude lat du point, en radians
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * Retourne la longitude lon du point, en radians
     *
     * @return la longtiude lon du point, en radians
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * Retourne le point de coordonnées suisses se trouvant à la même position que le récepteur
     *
     * @return le point de coordonnées suisses se trouvant à la même position que le récepteur
     */
    public PointCh toPointCh() {
        double e = Ch1903.e(lon(), lat());
        double n = Ch1903.n(lon(), lat());
        return SwissBounds.containsEN(e, n) ? new PointCh(e, n) : null;
    }
}
