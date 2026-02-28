package com.model;

/**
 * Represents a 2D coordinate system with x and y values.
 * Used to store the geographical coordinates of a music band's origin location.
 */
public class Coordinates {

    /**
     * Constructs a new Coordinates object with the specified x and y values.
     *
     * @param x the x coordinate (must be less than or equal to 554, cannot be null)
     * @param y the y coordinate (must be less than or equal to 782, cannot be null)
     * @throws IllegalArgumentException if x is greater than 554 or y is greater than 782
     */
    public Coordinates(long x, int y) {
        this.x = x;
            if (x > 554) {
                throw new IllegalArgumentException("X cannot be greater than 554");
            }
        this.y = y;
            if (y > 782) {
                throw new IllegalArgumentException("Y cannot be greater than 782");
            }
    }
    /** The x coordinate. Maximum value is 554. Cannot be null. */
    private Long x;
    /** The y coordinate. Maximum value is 782. Cannot be null. */
    private Integer y;

    /**
     * Gets the x coordinate.
     *
     * @return the x coordinate value
     */
    public Long getX() {
        return x;
    }

    /**
     * Gets the y coordinate.
     *
     * @return the y coordinate value
     */
    public Integer getY() {
        return y;
    }

    /**
     * Returns a string representation of the coordinates in the format "(x, y)".
     *
     * @return a string representation of the coordinates
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
