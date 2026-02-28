package com.model;

/**
 * Represents a music band with all its attributes.
 * This class implements Comparable to allow sorting by ID.
 * Each MusicBand has a unique ID, name, coordinates, creation date,
 * number of participants, description, genre, and best album information.
 */
public class MusicBand implements Comparable<MusicBand> {
    /**
     * Constructs a new MusicBand with a randomly generated unique ID
     * and automatically set creation date.
     */
    public MusicBand() {
        this.id = java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        this.creationDate = new java.util.Date();
    }
        /** Unique identifier for the music band. Must be greater than 0 and must be unique. Generated automatically. */
        private long id;
        /** The name of the music band. Cannot be null and cannot be empty. */
        private String name;
        /** The coordinates of the music band. Cannot be null. */
        private Coordinates coordinates;
        /** The date when the music band was created. Cannot be null. Generated automatically. */
        private java.util.Date creationDate;
        /** The number of participants in the band. Cannot be null and must be greater than 0. */
        private Integer numberOfParticipants;
        /** The description of the music band. Can be null. */
        private String description;
        /** The genre of the music band. Can be null. */
		private MusicGenre genre;
        /** The best album of the music band. Cannot be null. */
        private Album bestAlbum;

        /**
         * Sets the unique identifier for the music band.
         *
         * @param id the unique identifier to set
         */
        public void setId(long id) {
			this.id = id;
		}

        /**
         * Sets the name of the music band.
         *
         * @param name the name to set (cannot be null or empty)
         */
		public void setName(String name) {
			this.name = name;
		}

        /**
         * Sets the coordinates of the music band.
         *
		 * @param coordinates the coordinates to set (cannot be null)
		 */
		public void setCoordinates(Coordinates coordinates) {
			this.coordinates = coordinates;
		}

        /**
         * Sets the creation date of the music band.
         *
         * @param creationDate the creation date to set (cannot be null)
         */
		public void setCreationDate(java.util.Date creationDate) {
			this.creationDate = creationDate;
		}

        /**
         * Sets the number of participants in the music band.
         *
         * @param numberOfParticipants the number of participants to set (must be greater than 0)
         */
		public void setNumberOfParticipants(Integer numberOfParticipants) {
			this.numberOfParticipants = numberOfParticipants;
		}

        /**
         * Sets the description of the music band.
         *
         * @param description the description to set (can be null)
         */
		public void setDescription(String description) {
			this.description = description;
		}

        /**
         * Sets the genre of the music band.
         *
         * @param genre the genre to set (can be null)
         */
		public void setGenre(MusicGenre genre) {
			this.genre = genre;
		}

        /**
         * Sets the best album of the music band.
         *
         * @param bestAlbum the best album to set (cannot be null)
         */
		public void setBestAlbum(Album bestAlbum) {
			this.bestAlbum = bestAlbum;
		}

        /**
         * Gets the name of the music band.
         *
         * @return the name of the music band
         */
		public String getName() {
			return name;
		}

        /**
         * Gets the coordinates of the music band.
         *
         * @return the coordinates of the music band
         */
		public Coordinates getCoordinates() {
			return coordinates;
		}

        /**
         * Gets the creation date of the music band.
         *
         * @return the creation date of the music band
         */
		public java.util.Date getCreationDate() {
			return creationDate;
		}

        /**
         * Gets the number of participants in the music band.
         *
         * @return the number of participants
         */
		public Integer getNumberOfParticipants() {
			return numberOfParticipants;
		}

        /**
         * Gets the description of the music band.
         *
         * @return the description (can be null)
         */
		public String getDescription() {
			return description;
		}

        /**
         * Gets the genre of the music band.
         *
         * @return the genre (can be null)
         */
		public MusicGenre getGenre() {
			return genre;
		}

        /**
         * Gets the best album of the music band.
         *
         * @return the best album
         */
		public Album getBestAlbum() {
			return bestAlbum;
		}


    /**
     * Compares this music band to another based on their ID values.
     *
     * @param other the music band to compare to
     * @return a negative integer, zero, or a positive integer as this ID
     *         is less than, equal to, or greater than the specified band's ID
     */
    @Override
    public int compareTo(MusicBand other) {
        return Long.compare(this.id, other.id);
    }

    /**
     * Checks if this music band is equal to another object.
     * Two music bands are considered equal if they have the same ID.
     *
     * @param o the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicBand that = (MusicBand) o;
        return id == that.id;
    }

    /**
     * Returns a hash code value for this music band.
     * The hash code is based on the band's ID.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    /**
     * Gets the unique identifier of the music band.
     *
     * @return the unique identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Returns a detailed string representation of the music band.
     * The format includes all band attributes formatted in a table-like structure.
     *
     * @return a string representation of the music band
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String line = "+" + "-".repeat(50) + "+\n";
        sb.append(line);
        sb.append(String.format("| %-12s: %-34s |\n", "ID", id));
        sb.append(String.format("| %-12s: %-34s |\n", "Name", name));
        sb.append(String.format("| %-12s: %-34s |\n", "Coordinates", coordinates));
        sb.append(String.format("| %-12s: %-34s |\n", "Created", creationDate));
        sb.append(String.format("| %-12s: %-34s |\n", "Participants", numberOfParticipants));
        sb.append(String.format("| %-12s: %-34s |\n", "Description", description != null ? description : "null"));
        sb.append(String.format("| %-12s: %-34s |\n", "Genre", genre != null ? genre : "null"));
        sb.append(String.format("| %-12s: %-34s |\n", "Best Album", bestAlbum));
        sb.append(line);
        return sb.toString();
    }
}
