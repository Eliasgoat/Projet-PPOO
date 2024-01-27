package ch.epfl.javelo.routing;

/**
 * Une fonction de coût
 *
 * @author Elias Mir(341277)
 */
public interface CostFunction {

    /**
     * Retourne le facteur de cout
     *
     * @param nodeId identité du noeud de depart
     * @param edgeId identité de l'arete
     * @return le facteur par lequel la longueur de l'arête d'identité edgeId,
     * partant du nœud d'identité nodeId, doit être multipliée
     */
    public abstract double costFactor(int nodeId, int edgeId);
}
