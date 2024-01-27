package ch.epfl.javelo;

/**
 * Classe Preconditions
 *
 * @author Elias Mir(341277)
 */
public final class Preconditions {

    private Preconditions() {}
    /**
     * Verifie que l'argument donne est vrai
     * @param shouldBeTrue argument cense etre vrai
     * @throws IllegalArgumentException si l'argument donne est faux
     */
    public static void checkArgument(boolean shouldBeTrue){
        if(!shouldBeTrue){
            throw new IllegalArgumentException();
        }
    }
}
