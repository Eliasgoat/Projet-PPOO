package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

/**
 * Un point d'un itinéraire
 *
 * @param point               le point sur l'itinéraire
 * @param position            la position du point le long de l'itinéraire, en mètres
 * @param distanceToReference la distance, en mètres, entre le point et la référence
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {

    public static final RoutePoint NONE = new RoutePoint(null, Double.NaN, Double.POSITIVE_INFINITY);

    /**
     * Retourne un point identique au récepteur (this) mais dont la position est décalée de la différence donnée
     *
     * @param positionDifference difference donne
     * @return un point identique au récepteur (this) mais dont la position est décalée de la différence donnée
     */
    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return new RoutePoint(point, position + positionDifference, distanceToReference);
    }

    /**
     * Retourne this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     *
     * @param that point de comparaison
     * @return this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     */
    public RoutePoint min(RoutePoint that) {
        if (this.distanceToReference <= that.distanceToReference) {
            return this;
        }
        return that;
    }

    /**
     * Retourne this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon
     *
     * @param thatPoint               le point sur l'itinéraire
     * @param thatPosition            la position du point le long de l'itinéraire, en mètres
     * @param thatDistanceToReference la distance, en mètres, entre le point et la référence
     * @return this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        if (this.distanceToReference <= thatDistanceToReference) {
            return this;
        }
        return new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }
}
