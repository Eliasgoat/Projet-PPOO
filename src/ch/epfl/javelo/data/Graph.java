package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Un graphe
 *
 * @author Elias Mir(341277)
 */
public final class Graph {

    private final GraphNodes nodes;
    private final GraphSectors sectors;
    private final GraphEdges edges;
    private final List<AttributeSet> attributeSets;

    /**
     * Construit le graphe avec les nœuds, secteurs, arêtes et ensembles d'attributs donnés
     *
     * @param nodes         noeuds
     * @param sectors       secteurs donnés
     * @param edges         aretes donnés
     * @param attributeSets attributs donnés
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets) {
        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    /**
     * Retourne le nombre total de nœuds dans le graphe
     *
     * @return le nombre total de nœuds dans le graphe
     */
    public int nodeCount() {
        return nodes.count();
    }

    /**
     * Retourne la position du nœud d'identité donnée
     *
     * @param nodeId identité du noeud donnée
     * @return la position du nœud d'identité donnée
     */
    public PointCh nodePoint(int nodeId) {
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Retourne le nombre d'arêtes sortant du nœud d'identité donnée
     *
     * @param nodeId identité du noeud donnée
     * @return le nombre d'arêtes sortant du nœud d'identité donnée
     */
    public int nodeOutDegree(int nodeId) {
        return nodes.outDegree(nodeId);
    }

    /**
     * Retourne l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId
     *
     * @param nodeId    identité du noeud donnée
     * @param edgeIndex index de l'arete recherche
     * @return l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Retourne l'identité du nœud se trouvant le plus proche du point donné, à la distance maximale donnée
     *
     * @param point          point donné
     * @param searchDistance distance maximale donné
     * @return l'identité du nœud se trouvant le plus proche du point donné, à la distance maximale donnée
     */
    public int nodeClosestTo(PointCh point, double searchDistance) {
        if(point!= null){
            List<GraphSectors.Sector> possibleSector = sectors.sectorsInArea(point, searchDistance);
            int closestNodeId = -1;
            for (GraphSectors.Sector sector : possibleSector) {
                for (int i = sector.startNodeId(); i < sector.endNodeId(); i++) {
                    PointCh nodePoint = nodePoint(i);
                    if (point.squaredDistanceTo(nodePoint)
                            <= searchDistance * searchDistance) {
                        if (closestNodeId == -1) {
                            closestNodeId = i;
                        } else {
                            if (point.squaredDistanceTo(nodePoint)
                                    <= point.squaredDistanceTo(nodePoint(closestNodeId))) {
                                closestNodeId = i;
                            }
                        }
                    }
                }
            }
            return closestNodeId;
        }
        return -1;
    }

    /**
     * Retourne l'identité du nœud destination de l'arête d'identité donnée
     *
     * @param edgeId identité du noeud donnée
     * @return l'identité du nœud destination de l'arête d'identité donnée
     */
    public int edgeTargetNodeId(int edgeId) {
        return edges.targetNodeId(edgeId);
    }

    /**
     * Retourne vrai ssi l'arête d'identité donnée va dans le sens contraire de la voie OSM dont elle provient
     *
     * @param edgeId identité du noeud donnée
     * @return vrai ssi l'arête d'identité donnée va dans le sens contraire de la voie OSM dont elle provient
     */
    public boolean edgeIsInverted(int edgeId) {
        return edges.isInverted(edgeId);
    }

    /**
     * Retourne l'ensemble des attributs OSM attachés à l'arête d'identité donnée
     *
     * @param edgeId identité du noeud donnée
     * @return l'ensemble des attributs OSM attachés à l'arête d'identité donnée
     */
    public AttributeSet edgeAttributes(int edgeId) {
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Retourne la longueur, en mètres, de l'arête d'identité donnée
     *
     * @param edgeId identité du noeud donnée
     * @return la longueur, en mètres, de l'arête d'identité donnée
     */
    public double edgeLength(int edgeId) {
        return edges.length(edgeId);
    }

    /**
     * Retourne le dénivelé positif total de l'arête d'identité donnée
     *
     * @param edgeId identité du noeud donnée
     * @return le dénivelé positif total de l'arête d'identité donnée
     */
    public double edgeElevationGain(int edgeId) {
        return edges.elevationGain(edgeId);
    }

    /**
     * Retourne le profil en long de l'arête d'identité donnée, sous la forme d'une fonction
     *
     * @param edgeId identité du noeud donnée
     * @return le profil en long de l'arête d'identité donnée, sous la forme d'une fonction
     */
    public DoubleUnaryOperator edgeProfile(int edgeId) {
        if (edges.hasProfile(edgeId)) {
            return Functions.sampled(edges.profileSamples(edgeId), edges.length(edgeId));
        }
        return Functions.constant(Double.NaN);
    }

    /**
     * Retourne le graphe JaVelo obtenu à partir des fichiers se trouvant dans le répertoire
     * dont le chemin d'accès est basePath
     *
     * @param basePath chemin d'acces
     * @return le graphe JaVelo obtenu à partir des fichiers se trouvant dans le répertoire
     * dont le chemin d'accès est basePath
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public static Graph loadFrom(Path basePath) throws IOException {
        Path nodePath = basePath.resolve("nodes.bin");
        Path sectorPath = basePath.resolve("sectors.bin");
        Path edgesPath = basePath.resolve("edges.bin");
        Path profileIdsPath = basePath.resolve("profile_ids.bin");
        Path elevationsPath = basePath.resolve("elevations.bin");
        Path attributePath = basePath.resolve("attributes.bin");

        IntBuffer nodesBuffer1;
        try (FileChannel channel = FileChannel.open(nodePath)) {
            nodesBuffer1 = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asIntBuffer();
        }
        GraphNodes nodes = new GraphNodes(nodesBuffer1);

        ByteBuffer sectorsBuffer2;
        try (FileChannel channel = FileChannel.open(sectorPath)) {
            sectorsBuffer2 = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
        GraphSectors sectors = new GraphSectors(sectorsBuffer2);

        ByteBuffer edgesBuffer;
        try (FileChannel channel = FileChannel.open(edgesPath)) {
            edgesBuffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }

        IntBuffer profileIds;
        try (FileChannel channel = FileChannel.open(profileIdsPath)) {
            profileIds = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asIntBuffer();
        }

        ShortBuffer elevations;
        try (FileChannel channel = FileChannel.open(elevationsPath)) {
            elevations = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asShortBuffer();
        }

        GraphEdges edges = new GraphEdges(edgesBuffer, profileIds, elevations);

        LongBuffer attributesBuffer;
        try (FileChannel channel = FileChannel.open(attributePath)) {
            attributesBuffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asLongBuffer();
        }

        List<AttributeSet> attributeSets = new ArrayList<>();
        for (int i = 0; i < attributesBuffer.capacity(); i++) {
            attributeSets.add(new AttributeSet(attributesBuffer.get(i)));
        }
        return new Graph(nodes, sectors, edges, attributeSets);
    }
}
