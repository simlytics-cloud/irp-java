package iso.example.irpsystem.irpmodel.algorithms;

import iso.example.irpsystem.irpdomain.ImmutableCoordinate;

/**
 * The Distance class provides utility methods for calculating distances
 * between two coordinate points.
 */
public class Distance {

    /**
     * Calculates the Euclidean distance between two coordinate points.
     *
     * @param c1 the first coordinate point
     * @param c2 the second coordinate point
     * @return the distance between the two points
     */
    public static double distanceBetween(ImmutableCoordinate c1, ImmutableCoordinate c2) {
        double deltaX = c1.getX() - c2.getX();
        double deltaY = c1.getY() - c2.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

}
