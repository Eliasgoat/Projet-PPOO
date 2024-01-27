package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;

import java.util.*;
import java.util.List;

/**
 * Calculateur de la meilleur Route
 *
 * @author Elias Mir(341277)
 */
public final class RouteComputer {

    private final Graph graph;
    private final CostFunction costFunction;

    /**
     * Construit un planificateur d'itinéraire pour le graphe et la fonction de coût donnés
     *
     * @param graph        graphe de la route
     * @param costFunction fonction de coût donné
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Retourne la meilleur route possible en prnant en compte la fonction de coût
     *
     * @param startNodeId identité du noeud de depart
     * @param endNodeId   identité du noeud d'arrivee
     * @return l'itinéraire de coût total minimal allant du nœud d'identité startNodeId
     * au nœud d'identité endNodeId dans le graphe passé au constructeur
     * @throws IllegalArgumentException si le nœud de départ et d'arrivée sont identiques
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        Preconditions.checkArgument(startNodeId != endNodeId);

        //Represente un noeud en prenant en compte de sa distance par rapport au noeud de depart
        record WeightedNode(int nodeId, float distance) implements Comparable<WeightedNode> {
            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.distance, that.distance);
            }
        }

        //tableau qui donne ,pour chaque nœud, la distance du plus court chemin actuellement connu jusqu'à lui
        float[] distance = new float[graph.nodeCount()];
        //tableau qui donne ,pour chaque noeud, son prédécesseur dans le plus court chemin menant jusqu'à lui
        int[] predecessor = new int[graph.nodeCount()];
        //in_exploration contient les nœuds en cours d'exploration
        Queue<WeightedNode> in_exploration = new PriorityQueue<>();

        //Initialisations
        Arrays.fill(distance, 0, distance.length, Float.POSITIVE_INFINITY);
        distance[startNodeId] = 0.f;
        WeightedNode initalNode = new WeightedNode(startNodeId, distance[startNodeId]);
        in_exploration.add(initalNode);

        //Method to avoid lag time
        boolean quit = true;
        for(int i = 0; i<graph.nodeOutDegree(endNodeId); i++){
            if(costFunction.costFactor(endNodeId, graph.nodeOutEdgeId(endNodeId, i)) != Double.POSITIVE_INFINITY){
                quit = false;
            }
        }
        if(quit) return null;

        while (!in_exploration.isEmpty()) {
            //nœud de en_exploration tel que sa distance est minimale
            WeightedNode n1 = in_exploration.remove();
            if (n1.distance == Float.NEGATIVE_INFINITY) {
                continue;
            }

            //Cas ou on arrive au noeud d'arrivee d'identite endNodeId
            if (n1.nodeId == endNodeId) {
                //Determine les aretes qui donne le meilleur itineraire
                List<Edge> edges = new ArrayList<>();
                int index = endNodeId;
                while (index != startNodeId) {
                    int bestRouteEdgeId = 0;
                    for (int i = 0; i < graph.nodeOutDegree(predecessor[index]); i++) {
                        int k = graph.nodeOutEdgeId(predecessor[index], i);
                        if (graph.edgeTargetNodeId(k) == index) {
                            bestRouteEdgeId = i;
                            break;
                        }
                    }
                    edges.add(Edge.of(graph, graph.nodeOutEdgeId(predecessor[index], bestRouteEdgeId), predecessor[index], index));
                    index = predecessor[index];
                }
                Collections.reverse(edges);
                return new SingleRoute(edges);
            }
            //Iterations sur chaque noeud sortant du noeud n1
            for (int i = 0; i < graph.nodeOutDegree(n1.nodeId); i++) {
                int startId = n1.nodeId;
                int edgeIdOfStart = graph.nodeOutEdgeId(startId, i);
                int neighbourId = graph.edgeTargetNodeId(edgeIdOfStart);
                if (distance[neighbourId] == Float.NEGATIVE_INFINITY) {
                    continue;
                }
                float d = (float) (distance[startId]
                        + costFunction.costFactor(startId, edgeIdOfStart) * graph.edgeLength(edgeIdOfStart));

                //nœud d'arrivée de la ieme arete sortant du noeud n2
                float crowFliesDistance = (float) graph.nodePoint(neighbourId).distanceTo(graph.nodePoint(endNodeId));
                WeightedNode n2 = new WeightedNode(neighbourId,
                        (float) (d + crowFliesDistance));
                if (d < distance[neighbourId]) {
                    distance[neighbourId] = d;
                    predecessor[neighbourId] = startId;
                    in_exploration.add(n2);
                }
            }
            distance[n1.nodeId] = Float.NEGATIVE_INFINITY;
        }
        return null;
    }
}
