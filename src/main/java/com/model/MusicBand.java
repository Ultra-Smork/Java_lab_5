package com.model;


public class MusicBand implements Comparable<MusicBand> {
    public MusicBand() {
        this.id = java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        this.creationDate = new java.util.Date();
    }
        private long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
        private String name; //Поле не может быть null, Строка не может быть пустой
        private Coordinates coordinates; //Поле не может быть null
        private java.util.Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
        private Integer numberOfParticipants; //Поле не может быть null, Значение поля должно быть больше 0
        private String description; //Поле может быть null
		private MusicGenre genre; //Поле может быть null
        private Album bestAlbum; //Поле не может быть null

        public void setId(long id) {
			this.id = id;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setCoordinates(Coordinates coordinates) {
			this.coordinates = coordinates;
		}

		public void setCreationDate(java.util.Date creationDate) {
			this.creationDate = creationDate;
		}

		public void setNumberOfParticipants(Integer numberOfParticipants) {
			this.numberOfParticipants = numberOfParticipants;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setGenre(MusicGenre genre) {
			this.genre = genre;
		}

		public void setBestAlbum(Album bestAlbum) {
			this.bestAlbum = bestAlbum;
		}

		public String getName() {
			return name;
		}

		public Coordinates getCoordinates() {
			return coordinates;
		}

		public java.util.Date getCreationDate() {
			return creationDate;
		}

		public Integer getNumberOfParticipants() {
			return numberOfParticipants;
		}

		public String getDescription() {
			return description;
		}

		public MusicGenre getGenre() {
			return genre;
		}

		public Album getBestAlbum() {
			return bestAlbum;
		}


    @Override
    public int compareTo(MusicBand other) {
        return Long.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicBand that = (MusicBand) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    public long getId() {
        return id;
    }

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

