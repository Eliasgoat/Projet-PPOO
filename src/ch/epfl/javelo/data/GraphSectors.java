package ch.epfl.javelo.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

/**
 * Les secteurs d'un graph
 *
 * @param buffer la mémoire tampon contenant la valeur des attributs de la totalité des secteurs
 * @author Elias Mir(341277)
 */
public record GraphSectors(ByteBuffer buffer) {

    private final static double sectorWidth = (SwissBounds.MAX_E - SwissBounds.MIN_E) / 128;
    private final static double sectorLength = (SwissBounds.MAX_N - SwissBounds.MIN_N) / 128;
    private static final int OFFSET_START_NODE_ID = 0;
    private static final int OFFSET_NODE_COUNT = OFFSET_START_NODE_ID + Integer.BYTES;
    private static final int SECTORS_INTS = OFFSET_NODE_COUNT + Short.BYTES;

    /**
     * Retourne la liste de tous les secteurs ayant une intersection avec le carré centré au point donné
     * et de côté égal au double de la distance donnée
     *
     * @param center   centre du carre
     * @param distance un demi de la distance d'un cote du carre
     * @return la liste de tous les secteurs ayant une intersection avec le carré centré au point donné
     * et de côté égal au double de la distance donnée
     */
    public List<Sector> sectorsInArea(PointCh center, double distance) {
        List<Sector> sectors = new ArrayList<>();

        int xMin = (int) (((center.e() - distance) - SwissBounds.MIN_E) / sectorWidth);
        int xMax = (int) (((center.e() + distance) - SwissBounds.MIN_E) / sectorWidth);
        int yMin = (int) (((center.n() - distance) - SwissBounds.MIN_N) / sectorLength);
        int yMax = (int) (((center.n() + distance) - SwissBounds.MIN_N) / sectorLength);

        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                int index = i + 128 * j;
                int startNodeId = buffer.getInt(SECTORS_INTS*index + OFFSET_START_NODE_ID);
                int endNodeId = startNodeId + Short.toUnsignedInt(
                        buffer.getShort(SECTORS_INTS*index + OFFSET_NODE_COUNT)
                );
                sectors.add(new Sector(startNodeId, endNodeId));
            }
        }
        return sectors;
    }

    /**
     * Enregistrement Sector
     *
     * @param startNodeId l'identité du premier nœud du secteur
     * @param endNodeId   l'identité du nœud situé juste après le dernier nœud du secteur
     */
    public record Sector(int startNodeId, int endNodeId) {}
}
