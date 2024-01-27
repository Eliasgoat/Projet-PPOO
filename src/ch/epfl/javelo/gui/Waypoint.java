package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;

/**
 * A waypoint
 *
 * @param pointCh       the position of the waypoint in the Swiss coordinate system
 * @param closestNodeId the identity of the JaVelo node closest to this waypoint
 * @author Elias Mir(341277)
 * @author Jan Staszewicz(341201)
 */
public record Waypoint(PointCh pointCh, int closestNodeId) {}
