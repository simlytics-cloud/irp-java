package iso.example.irpsystem.irpmodel.algorithms;

import iso.example.irpsystem.irpdomain.ImmutableCoordinate;

public class Distance {

    public static double distanceBetween(ImmutableCoordinate c1, ImmutableCoordinate c2) {
        double deltaX = c1.getX() - c2.getX();
        double deltaY = c1.getY() - c2.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

}
