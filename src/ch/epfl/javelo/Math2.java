package ch.epfl.javelo;

import java.lang.Math;

/**
 * Offre les calculs mathematiques necessaires
 *
 * @author Elias Mir(341277)
 */
public final class Math2 {

    private Math2() {
    }

    /**
     * Retourne la partie entière par excès de la division de x par y
     *
     * @param x numerateur
     * @param y denominateur
     * @return la partie entière par excès de la division de x par y
     * @throws IllegalArgumentException si x est négatif ou si y est négatif ou nul
     */
    public static int ceilDiv(int x, int y) {
        Preconditions.checkArgument(
                x >= 0
                        && y > 0
        );
        return (x + y - 1) / y;
    }

    /**
     * Retourne la coordonnée y du point se trouvant sur la droite passant par (0,y0) et (1,y1) et de coordonnée x
     *
     * @param x  coordonne x du point dont on cherche la valeur
     * @param y0 valeur en x = 0
     * @param y1 valeur en x = 1
     * @return la coordonnée y du point se trouvant sur la droite passant par (0,y0) et (1,y1) et de coordonnée x
     */
    public static double interpolate(double y0, double y1, double x) {
        return Math.fma(y1 - y0, x, y0);
    }

    /**
     * Fait en sorte de mettre v dans l'intervalle [min, max]
     *
     * @param max maximum autorise
     * @param min minimum autorise
     * @param v   valeur qu'on teste
     * @return v pour qu'il soit dans l'intervalle autorise
     * @throws IllegalArgumentException si min>max
     */
    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        if (v < min) {
            return min;
        } else return Math.min(v, max);
    }

    /**
     * Fait en sorte de mettre v dans l'intervalle [min, max]
     *
     * @param max maximum autorise
     * @param min minimum autorise
     * @param v   valeur qu'on teste
     * @return v pour qu'il soit dans l'intervalle autorise
     * @throws IllegalArgumentException si min>max
     */
    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(min <= max);
        if (v < min) {
            return min;
        } else return Math.min(v, max);
    }

    /**
     * Retourne la valeur asinh(x)
     *
     * @param x valeur dont ont cherche le sinus hyperbolique inverse
     * @return asinh(x)
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + x*x));
    }

    /**
     * Retourne le produit scalaire de u et v
     *
     * @param uX Coordonnes x de u
     * @param uY Coordonnes y de u
     * @param vX Coordonnes x de v
     * @param vY Coordonnes y de v
     * @return le produit scalaire des vecteurs u et v
     */
    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return uX*vX + uY*vY;
    }

    /**
     * Retourne la norme au carre du vecteur u
     *
     * @param uX Coordonnes x de u
     * @param uY Coordonnes y de u
     * @return la norme au carre du vecteur u
     */
    public static double squaredNorm(double uX, double uY) {
        return uX*uX + uY*uY;
    }

    /**
     * Retourne la norme du vecteur u
     *
     * @param uX Coordonnes x de u
     * @param uY Coordonnes y de u
     * @return la norme du vecteur u
     */
    public static double norm(double uX, double uY) {
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * Retourne la longueur de la projection du vecteur allant du point A au point P sur le vecteur allant du point A au point B
     *
     * @param aX Coordonnee x du point A
     * @param aY Coordonnee Y du point A
     * @param bX Coordonnee x du point B
     * @param bY Coordonnee y du point B
     * @param pX Coordonnee x du point P
     * @param pY Coordonnee y du point P
     * @return la longueur de la projection du vecteur allant du point A au point P sur le vecteur allant du point A au point B
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY) {
        return dotProduct(pX - aX, pY - aY, bX - aX, bY - aY) / norm(bX - aX, bY - aY);
    }
}