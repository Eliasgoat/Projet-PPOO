package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

public interface Route {

    /**
     * Retourne l'index du segment à la position donnée (en mètres)
     *
     * @param position position donnee
     * @return l'index du segment à la position donnée (en mètres)
     */
    abstract int indexOfSegmentAt(double position);

    /**
     * Retourne la longueur de l'itinéraire, en mètres
     *
     * @return la longueur de l'itinéraire, en mètres
     */
    abstract double length();

    /**
     * Retourne la totalité des arêtes de l'itinéraire
     *
     * @return la totalité des arêtes de l'itinéraire
     */
    abstract List<Edge> edges();

    /**
     * Retourne la totalité des points situés aux extrémités des arêtes de l'itinéraire
     *
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire
     */
    abstract List<PointCh> points();

    /**
     * Retourne le point se trouvant à la position donnée le long de l'itinéraire
     *
     * @param position position donnee
     * @return le point se trouvant à la position donnée le long de l'itinéraire
     */
    abstract PointCh pointAt(double position);

    /**
     * Retourne l'altitude à la position donnée le long de l'itinéraire
     *
     * @param position position donnee
     * @return l'altitude à la position donnée le long de l'itinéraire
     */
    abstract double elevationAt(double position);

    /**
     * Retourne l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     *
     * @param position position donnée
     * @return l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     */
    abstract int nodeClosestTo(double position);

    /**
     * Retourne le point de l'itinéraire se trouvant le plus proche du point de référence donné.
     *
     * @param point point donné
     * @return le point de l'itinéraire se trouvant le plus proche du point de référence donné.
     */
    abstract RoutePoint pointClosestTo(PointCh point);
}
