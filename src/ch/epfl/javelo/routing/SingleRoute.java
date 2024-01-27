package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.*;
import java.util.List;

/**
 * Une route simple
 *
 * @author Elias Mir(341277)
 */
public final class SingleRoute implements Route {

    private final List<Edge> edges;
    private final double[] positionsAtEveryNode;
    private final double length;
    private final List<PointCh> points;

    /**
     * Construit l'itinéraire simple composé des arêtes données
     *
     * @param edges listes des aretes de la route
     * @throws IllegalArgumentException si la liste d'arêtes est vide
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        positionsAtEveryNode = new double[edges.size() + 1];

        positionsAtEveryNode[0] = 0;
        for (int i = 0; i < edges.size(); i++) {
            positionsAtEveryNode[i + 1] = positionsAtEveryNode[i] + edges.get(i).length();
        }

        double length1 = 0;
        for (Edge edge : edges) {
            length1 += edge.length();
        }
        this.length = length1;

        List<PointCh> pointList = new ArrayList<>();
        for (Edge edge : edges) {
            pointList.add(edge.fromPoint());
        }
        pointList.add(edges.get(edges.size() - 1).toPoint());
        this.points = Collections.unmodifiableList(pointList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double length() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Edge> edges() {
        return edges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PointCh> points() {
        return points;
    }

    //Trouve l'arete ou la position donne se trouve
    private int binarySearchCycle(double position) {
        int binarySearchResult = Arrays.binarySearch(positionsAtEveryNode, position);
        if (position == length) {
            return (positionsAtEveryNode.length - 2);
        } else if (binarySearchResult >= 0) {
            return binarySearchResult;
        } else {
            return -binarySearchResult - 2;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, length());
        int edgeIndex = binarySearchCycle(position);
        return edges
                .get(edgeIndex)
                .pointAt(position - positionsAtEveryNode[edgeIndex]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position, length());
        int edgeIndex = binarySearchCycle(position);
        return edges
                .get(edgeIndex)
                .elevationAt(position - positionsAtEveryNode[edgeIndex]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position, length());
        int edgeIndex = binarySearchCycle(position);
        if ((position - positionsAtEveryNode[edgeIndex]) <= (positionsAtEveryNode[edgeIndex + 1] - position)) {
            return edges.get(edgeIndex).fromNodeId();
        }
        return edges
                .get(edgeIndex)
                .toNodeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {

        RoutePoint bestRoutePoint = RoutePoint.NONE;
        double l = 0;

        for (Edge edge : edges) {
            double position = Math2.clamp(0, edge.positionClosestTo(point), edge.length());
            PointCh edgePoint = edge.pointAt(position);
            double distanceToEdgePoint = point.distanceTo(edgePoint);

            bestRoutePoint = bestRoutePoint.min(edgePoint, l + position, distanceToEdgePoint);

            l += edge.length();
        }

        return bestRoutePoint;
    }
}
