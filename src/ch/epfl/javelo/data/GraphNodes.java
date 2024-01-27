package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.Q28_4;

import java.nio.IntBuffer;

/**
 * Les noeuds d'un graph
 *
 * @param buffer mémoire tampon contenant la valeur des attributs de la totalité des nœuds du graphe
 * @author Elias Mir(341277)
 */
public record GraphNodes(IntBuffer buffer) {

    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;

    /**
     * Retourne le nombre total de nœuds
     *
     * @return le nombre total de nœuds
     */
    public int count() {
        return buffer.capacity() / NODE_INTS;
    }

    /**
     * Retourne la coordonnée E du nœud d'identité donnée
     *
     * @param nodeId identité du noeud
     * @return la coordonnée E du nœud d'identité donnée
     */
    public double nodeE(int nodeId) {
        return Q28_4.asDouble(buffer.get(NODE_INTS*nodeId + OFFSET_E));
    }

    /**
     * Retourne la coordonnée N du nœud d'identité donnée
     *
     * @param nodeId identité du noeud
     * @return la coordonnée N du nœud d'identité donnée
     */
    public double nodeN(int nodeId) {
        return Q28_4.asDouble(buffer.get(NODE_INTS*nodeId + OFFSET_N));
    }

    /**
     * Retourne le nombre d'arêtes sortant du nœud d'identité donné
     *
     * @param nodeId identité du noeud
     * @return le nombre d'arêtes sortant du nœud d'identité donné
     */
    public int outDegree(int nodeId) {
        int value = buffer.get(NODE_INTS*nodeId + OFFSET_OUT_EDGES);
        return Bits.extractUnsigned(value, 28, 4);
    }

    /**
     * Retourne l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId
     *
     * @param nodeId    identité du noeud
     * @param edgeIndex index de l'arete sortant du noeud d'identite nodeId
     * @return l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId
     */
    public int edgeId(int nodeId, int edgeIndex) {
        assert 0 <= edgeIndex && edgeIndex < outDegree(nodeId);
        int value = buffer.get(NODE_INTS*nodeId + OFFSET_OUT_EDGES);
        int intialEdgeId = Bits.extractUnsigned(value, 0, 28);
        return intialEdgeId + edgeIndex;
    }
}
