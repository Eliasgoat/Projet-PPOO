package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Une route multiple
 *
 * @author Elias Mir (341277)
 */
public final class MultiRoute implements Route {

    private final List<Route> segments;
    private final double[] positionsAtEverySegments;
    private final double length;
    private final List<Edge> edges;
    private final List<PointCh> points;

    /**
     * Construit une route multiple
     * @param segments liste des routes formant la route multiple
     */
    public MultiRoute(List<Route> segments) {
        Preconditions.checkArgument(!segments.isEmpty());
        this.segments = List.copyOf(segments);

        double length1 = 0;
        for (Route route : segments) {
            length1 += route.length();
        }
        this.length = length1;


        positionsAtEverySegments = new double[segments.size() + 1];
        positionsAtEverySegments[0] = 0;
        for (int i = 0; i < segments.size(); i++) {
            positionsAtEverySegments[i + 1] = positionsAtEverySegments[i] + segments.get(i).length();
        }

        List<Edge> edges1 = new ArrayList<>();
        for (Route route : segments) {
            edges1.addAll(route.edges());
        }
        this.edges = List.copyOf(edges1);

        List<PointCh> list = new ArrayList<>();
        for (Edge edge : edges) {
            list.add(edge.fromPoint());
        }
        list.add(edges.get(edges.size() - 1).toPoint());
        this.points = Collections.unmodifiableList(list);
    }

    //Index de la route dont on a la position
    private int binarySearchCycle(double position) {
        if (position == length) {
            return (positionsAtEverySegments.length - 2);
        } else if (Arrays.binarySearch(positionsAtEverySegments, position) >= 0) {
            return Arrays.binarySearch(positionsAtEverySegments, position);
        } else {
            return -Arrays.binarySearch(positionsAtEverySegments, position) - 2;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOfSegmentAt(double position) {
        position = Math2.clamp(0, position, length());
        int indexOfSegment = 0;
        for (Route route : segments) {
            if (position > route.length()) {
                indexOfSegment += route.indexOfSegmentAt(position) + 1;
                position -= route.length();
            }else {
                indexOfSegment += route.indexOfSegmentAt(position);
                break;
            }
        }
        return indexOfSegment;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, length());
        int segmentIndex = binarySearchCycle(position);
        return segments
                .get(segmentIndex)
                .pointAt(position - positionsAtEverySegments[segmentIndex]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position, length());
        int segmentIndex = binarySearchCycle(position);
        return segments
                .get(segmentIndex)
                .elevationAt(position - positionsAtEverySegments[segmentIndex]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position, length());
        int segmentIndex = binarySearchCycle(position);
        return segments
                .get(segmentIndex)
                .nodeClosestTo(position - positionsAtEverySegments[segmentIndex]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {

        RoutePoint bestRoutePoint = RoutePoint.NONE;
        double l = 0;

        for (Route route : segments) {
            bestRoutePoint = bestRoutePoint.min(route.pointClosestTo(point).withPositionShiftedBy(l));
            l += route.length();
        }

        return bestRoutePoint;
    }
}
