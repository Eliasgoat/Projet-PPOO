package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.Q28_4;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Les aretes d'un graphe
 *
 * @param edgesBuffer la mémoire tampon contenant la valeur des attributs figurant dans la première table de l'enonce
 *                    pour la totalité des arêtes du graphe
 * @param profileIds  la mémoire tampon contenant la valeur des attributs figurant dans la deuxieme table de l'enonce
 *  *                 pour la totalité des arêtes du graphe
 * @param elevations  la mémoire tampon contenant la totalité des échantillons des profils, compressés ou non
 * @author Elias Mir(341277)
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    private static final int OFFSET_NODE_IDENTITY = 0;
    private static final int OFFSET_LENGTH = OFFSET_NODE_IDENTITY + Integer.BYTES;
    private static final int OFFSET_ELEVATION_GAIN = OFFSET_LENGTH + Short.BYTES;
    private static final int OFFSET_ATTRIBUTES = OFFSET_ELEVATION_GAIN + Short.BYTES;
    private static final int EDGES_INTS = OFFSET_ATTRIBUTES + Short.BYTES;

    private static final int COMPRESSED_Q4_4_NUMBER_OF_SEPARATION = 2;
    private static final int COMPRESSED_Q4_4_ELEVATION_LENGTH = 8;
    private static final int COMPRESSED_Q0_4_NUMBER_OF_SEPARATION = 4;
    private static final int COMPRESSED_Q0_4_ELEVATION_LENGTH = 4;

    /**
     * Retourne vrai ssi l'arête d'identité donnée va dans le sens inverse de la voie OSM dont elle provient
     *
     * @param edgeId identité de l'arête
     * @return vrai ssi l'arête d'identité donnée va dans le sens inverse de la voie OSM dont elle provient
     * @throws IllegalArgumentException si le edgeId est negatif
     */
    public boolean isInverted(int edgeId) {
        Preconditions.checkArgument(edgeId >= 0);
        return edgesBuffer.getInt(EDGES_INTS*edgeId + OFFSET_NODE_IDENTITY) < 0;
    }

    /**
     * Retourne l'identité du nœud destination de l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return l'identité du nœud destination de l'arête d'identité donnée
     */
    public int targetNodeId(int edgeId) {
        if (this.isInverted(edgeId)) {
            return ~(edgesBuffer.getInt(EDGES_INTS*edgeId + OFFSET_NODE_IDENTITY));
        } else return (edgesBuffer.getInt(EDGES_INTS*edgeId + OFFSET_NODE_IDENTITY));
    }

    /**
     * Retourne la longueur, en mètres, de l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return la longueur, en mètres, de l'arête d'identité donnée
     */
    public double length(int edgeId) {
        return Q28_4.asDouble(
                Short.toUnsignedInt(edgesBuffer.getShort(EDGES_INTS*edgeId + OFFSET_LENGTH))
        );
    }

    /**
     * Retourne le dénivelé positif, en mètres, de l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return le dénivelé positif, en mètres, de l'arête d'identité donnée
     */
    public double elevationGain(int edgeId) {
        return Q28_4.asDouble(
                Short.toUnsignedInt(edgesBuffer.getShort(EDGES_INTS*edgeId + OFFSET_ELEVATION_GAIN))
        );
    }

    /**
     * Retourne vrai ssi l'arête d'identité donnée possède un profil
     *
     * @param edgeId identité de l'arête
     * @return vrai ssi l'arête d'identité donnée possède un profil
     */
    public boolean hasProfile(int edgeId) {
        return Bits.extractUnsigned(profileIds.get(edgeId), 30, 2) != 0;
    }

    private enum profileType {
        NO_PROFILE(0),
        NOT_COMPRESSED(1),
        COMPRESSED_Q4_4(2),
        COMPRESSED_Q0_4(3);

        final int profileValue;

        profileType(int valueProfile) {
            this.profileValue = valueProfile;
        }

        static profileType getProfile(int profileValue) {
            for (profileType type : profileType.values()) {
                if (type.profileValue == profileValue) return type;
            }
            return null;
        }

    }

    /**
     * Retourne le tableau des échantillons du profil de l'arête d'identité donnée
     * qui est vide si l'arête ne possède pas de profil,
     *
     * @param edgeId identité de l'arête
     * @return le tableau des échantillons du profil de l'arête d'identité donnée
     * qui est vide si l'arête ne possède pas de profil
     */
    public float[] profileSamples(int edgeId) {
        float[] array = new float[0];
        int startIndex = Bits.extractUnsigned(profileIds.get(edgeId), 0, 30);
        int sampleNumber = 1 + Math2.ceilDiv(
                edgesBuffer.getShort(EDGES_INTS*edgeId + OFFSET_LENGTH), Q28_4.ofInt(2)
        );

        int profileValue = Bits.extractUnsigned(profileIds.get(edgeId), 30, 2);
        switch (profileType.getProfile(profileValue)) {
            case NO_PROFILE -> array = new float[0];

            case NOT_COMPRESSED -> {
                array = new float[sampleNumber];
                for (int i = 0; i < sampleNumber; i++) {
                    array[i] = Q28_4.asFloat(
                            Short.toUnsignedInt(elevations.get(startIndex + i))
                    );
                }
            }

            case COMPRESSED_Q4_4 -> {
                array = new float[sampleNumber];
                for (int i = 0; i < sampleNumber; i++) {
                    if (i == 0) {
                        array[0] = Q28_4.asFloat(Short.toUnsignedInt(elevations.get(startIndex)));
                    } else {
                        int extractedValue = elevations.get(Math2.ceilDiv(i, 2) + startIndex);
                        float realElevationValue;
                        if (i % COMPRESSED_Q4_4_NUMBER_OF_SEPARATION == 0) {
                            realElevationValue = Q28_4.asFloat(
                                    Bits.extractSigned(extractedValue, 0, COMPRESSED_Q4_4_ELEVATION_LENGTH)
                            );
                        } else {
                            realElevationValue = Q28_4.asFloat(
                                    Bits.extractSigned(extractedValue, 8, COMPRESSED_Q4_4_ELEVATION_LENGTH)
                            );
                        }
                        array[i] = array[i - 1] + realElevationValue;
                    }
                }
            }

            case COMPRESSED_Q0_4 -> {
                array = new float[sampleNumber];
                for (int i = 0; i < sampleNumber; i++) {
                    if (i == 0) {
                        array[0] = Q28_4.asFloat(Short.toUnsignedInt(elevations.get(startIndex)));
                    } else {
                        int extractedValue = elevations.get(Math2.ceilDiv(i, 4) + startIndex);
                        float realElevationValue;
                        if (i % COMPRESSED_Q0_4_NUMBER_OF_SEPARATION == 0) {
                            realElevationValue = Q28_4.asFloat(
                                    Bits.extractSigned(extractedValue, 0, COMPRESSED_Q0_4_ELEVATION_LENGTH)
                            );
                        } else if (i % COMPRESSED_Q0_4_NUMBER_OF_SEPARATION == 1) {
                            realElevationValue = Q28_4.asFloat(
                                    Bits.extractSigned(extractedValue, 12, COMPRESSED_Q0_4_ELEVATION_LENGTH)
                            );
                        } else if (i % COMPRESSED_Q0_4_NUMBER_OF_SEPARATION == 2) {
                            realElevationValue = Q28_4.asFloat(
                                    Bits.extractSigned(extractedValue, 8, COMPRESSED_Q0_4_ELEVATION_LENGTH)
                            );
                        } else {
                            realElevationValue = Q28_4.asFloat(
                                    Bits.extractSigned(extractedValue, 4, COMPRESSED_Q0_4_ELEVATION_LENGTH)
                            );
                        }
                        array[i] = array[i - 1] + realElevationValue;
                    }
                }
            }
        }

        //Reversing array
        if (array.length == 0) {
            return array;
        } else if (this.isInverted(edgeId)) {
            for (int i = 0; i < array.length / 2; i++) {
                float array1 = array[i];
                array[i] = array[array.length - 1 - i];
                array[array.length - 1 - i] = array1;
            }
            return array;
        } else {
            return array;
        }
    }

    /**
     * Retourne l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée
     */
    public int attributesIndex(int edgeId) {
        return Short.toUnsignedInt(
                edgesBuffer.getShort(EDGES_INTS*edgeId + OFFSET_ATTRIBUTES)
        );
    }
}
