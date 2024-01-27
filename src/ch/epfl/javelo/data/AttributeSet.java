package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Preconditions;

import java.util.StringJoiner;

/**
 * Ensemble d'attribut OSM
 *
 * @author Elias Mir(341277)
 */
public record AttributeSet(long bits) {

    /**
     * Construit l'ensemble d'attributs OSM
     *
     * @param bits le contenu de l'ensemble au moyen d'un bit par valeur possible
     * @throws IllegalArgumentException si la valeur bits contient un bit à 1 qui ne correspond à aucun attribut valide
     */
    public AttributeSet {
        Preconditions.checkArgument(bits >> 62 == 0);
    }

    /**
     * Retourne un ensemble contenant uniquement les attributs donnés en argument
     *
     * @param attributes ensemble des attributs que le (new AttributeSet) retourne possede
     * @return un ensemble contenant uniquement les attributs donnés en argument
     */
    public static AttributeSet of(Attribute... attributes) {
        long bits = 0;
        for (Attribute attribute : attributes) {
            long mask = 1L << attribute.ordinal();
            bits = bits + mask;
        }
        return new AttributeSet(bits);
    }

    /**
     * Retourne vrai ssi l'ensemble récepteur (this) contient l'attribut donné
     *
     * @param attribute attribute donnee
     * @return vrai ssi l'ensemble récepteur (this) contient l'attribut donné
     */
    public boolean contains(Attribute attribute) {
        long mask = 1L << attribute.ordinal();
        return (bits & mask) == mask;
    }

    /**
     * Retourne vrai ssi l'intersection de l'ensemble récepteur avec celui passé en argument that n'est pas vide
     *
     * @param that Ensemble en argument par rapport auquel on compare leu intersection
     * @return vrai ssi l'intersection de l'ensemble récepteur avec celui passé en argument that n'est pas vide
     */
    public boolean intersects(AttributeSet that) {
        return (that.bits & this.bits) != 0;
    }

    /**
     * Retourne une chaîne composée de la représentation textuelle des éléments de l'ensemble entourés d'accolades
     * et séparés par des virgules
     *
     * @return une chaîne composée de la représentation textuelle des éléments de l'ensemble entourés d'accolades
     * et séparés par des virgules
     */
    @Override
    public String toString() {
        StringJoiner j = new StringJoiner(",", "{", "}");
        for (Attribute attribute : Attribute.values()) {
            if (contains(attribute)) {
                j.add(attribute.keyValue());
            }
        }
        return j.toString();
    }
}
