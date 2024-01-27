package ch.epfl.javelo;

/**
 * Les bits
 *
 * @author Elias Mir(341277)
 */
public final class Bits {
    private Bits() {
    }

    /**
     * Retourne l'entier dont le vectuer de 32 bits est extrait du vecteur de 32 bits value
     * la plage de length bits commençant au bit d'index start,
     * `qu'elle interprète comme une valeur non-signée
     *
     * @param value  entier dont on veut l'extraction
     * @param start  index du bit initial
     * @param length taille voulu
     * @return l'entier dont le vectuer de 32 bits est extrait du vecteur de 32 bits value
     * la plage de length bits commençant au bit d'index start,
     * qu'elle interprète comme une valeur non-signée
     * @throws IllegalArgumentException si la plage est invalide
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(
                (length > 0)
                && (length != 32)
                && (start + length <= 32)
                && (start >= 0)
                && (start <= 31)
        );
        return (value << (32 - (start + length))) >>> (32 - length);
    }

    /**
     * Retourne l'entier dont le vectuer de 32 bits est extrait du vecteur de 32 bits value la plage de length bits
     * commençant au bit d'index start, qu'elle interprète comme une valeur signée en complément à deux
     *
     * @param value  entier dont on veut l'extraction
     * @param start  index du bit initial
     * @param length taille voulu
     * @return l'entier dont le vectuer de 32 bits est extrait du vecteur de 32 bits value la plage de length bits
     * commençant au bit d'index start, qu'elle interprète comme une valeur signée en complément à deux
     * @throws IllegalArgumentException si la plage est invalide
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(
                (length > 0)
                && (start + length <= 32)
                && (start >= 0)
                && (start <= 31)
        );
        return (value << (32 - (start + length))) >> (32 - length);
    }
}
