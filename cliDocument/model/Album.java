package com.model;

/**
 * Represents an album with a name and sales figure.
 * This class is used as part of the MusicBand model to store information
 * about a band's best-selling album.
 */
public class Album {
    /**
     * Constructs a new Album with the specified name and sales figure.
     *
     * @param name the name of the album (cannot be null or empty)
     * @param i    the total sales figure for the album (must be greater than 0)
     */
    public Album(String name, Double i) {
        this.name = name;
        this.sales = i;
    }
    /** The name of the album. Cannot be null and cannot be empty. */
    private String name;
    /** The total sales figure for the album. Cannot be null and must be greater than 0. */
    private Double sales;

    /**
     * Gets the name of the album.
     *
     * @return the album name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the total sales figure for the album.
     *
     * @return the sales figure
     */
    public Double getSales() {
        return sales;
    }

    /**
     * Returns a string representation of the album in the format "name (sales sales)".
     *
     * @return a string representation of the album
     */
    @Override
    public String toString() {
        return name + " (" + sales + " sales)";
    }
}
