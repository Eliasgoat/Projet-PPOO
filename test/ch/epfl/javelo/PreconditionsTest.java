package ch.epfl.javelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PreconditionsTest {
    @Test
    void checkArgumentSucceedsForTrue() {
        assertDoesNotThrow(() -> {
            Preconditions.checkArgument(true);
        });
    }

    @Test
    void checkArgumentThrowsForFalse() {
        assertThrows(IllegalArgumentException.class, () -> {
            Preconditions.checkArgument(false);
        });
    }
    double grade(int p1, int p2, int pE, double b) {
        double p2b = Math.ceil(130d * Math.pow(p2 / 130d, 1d / b));
        double rawGrade = 0.875 + 5.25 * ((p1 + p2b + pE) / 500d);
        return Math.rint(rawGrade * 4) / 4;
    }
}
