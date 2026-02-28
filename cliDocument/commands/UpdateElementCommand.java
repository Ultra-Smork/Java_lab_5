package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import com.model.MusicBand;
import com.model.MusicGenre;
import com.model.Album;
import com.model.Coordinates;
import java.util.Scanner;

/**
 * Command implementation for updating an existing music band in the collection.
 * This command prompts the user for an ID and then all fields to update,
 * allowing them to keep existing values by pressing Enter.
 */
public class UpdateElementCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();

    /**
     * Gets user input with a default value.
     * Returns the default value if the user enters nothing.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the prompt to display
     * @param defaultValue the default value to use if input is empty
     * @return the input value or default
     */
    private String getInputWithDefault(Scanner scanner, String prompt, String defaultValue) {
        System.out.print(prompt + " [" + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    /**
     * Gets an integer input with a default value.
     * Returns the default value if the user enters nothing.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the prompt to display
     * @param defaultValue the default value to use if input is empty
     * @return the input value or default
     */
    private Integer getIntInputWithDefault(Scanner scanner, String prompt, Integer defaultValue) {
        while (true) {
            System.out.print(prompt + " [" + defaultValue + "]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Error: Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format.");
            }
        }
    }

    /**
     * Gets a long input with a default value and maximum constraint.
     * Returns the default value if the user enters nothing.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the prompt to display
     * @param defaultValue the default value to use if input is empty
     * @param max the maximum allowed value
     * @return the input value or default
     */
    private Long getLongInputWithDefault(Scanner scanner, String prompt, Long defaultValue, long max) {
        while (true) {
            System.out.print(prompt + " [" + defaultValue + "]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                long value = Long.parseLong(input);
                if (value <= max) {
                    return value;
                }
                System.out.println("Error: Value must be <= " + max);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format.");
            }
        }
    }

    /**
     * Gets a y-coordinate input with a default value.
     * Returns the default value if the user enters nothing.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the prompt to display
     * @param defaultValue the default value to use if input is empty
     * @return the input value or default
     */
    private Integer getYInputWithDefault(Scanner scanner, String prompt, Integer defaultValue) {
        while (true) {
            System.out.print(prompt + " [" + defaultValue + "]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value <= 782) {
                    return value;
                }
                System.out.println("Error: Value must be <= 782.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format.");
            }
        }
    }

    /**
     * Gets a double input with a default value.
     * Returns the default value if the user enters nothing.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the prompt to display
     * @param defaultValue the default value to use if input is empty
     * @return the input value or default
     */
    private Double getDoubleInputWithDefault(Scanner scanner, String prompt, Double defaultValue) {
        while (true) {
            System.out.print(prompt + " [" + defaultValue + "]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Error: Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format.");
            }
        }
    }

    /**
     * Gets a music genre input with a default value.
     * Returns the default value if the user enters nothing.
     * Enter 'null' to remove the genre.
     *
     * @param scanner the Scanner to read input from
     * @param defaultValue the default value to use if input is empty
     * @return the selected genre, null to remove, or default
     */
    private MusicGenre getGenreInputWithDefault(Scanner scanner, MusicGenre defaultValue) {
        while (true) {
            String defaultStr = defaultValue != null ? defaultValue.toString() : "null";
            System.out.print("Genre [PSYCHEDELIC_ROCK | MATH_ROCK | POST_ROCK] [current: " + defaultStr + ", Enter to keep, 'null' to remove]: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            if (input.equalsIgnoreCase("null")) {
                return null;
            }
            try {
                MusicGenre genre = MusicGenre.valueOf(input.toUpperCase());
                if (genre != MusicGenre.PLACEHOLDER) {
                    return genre;
                }
                System.out.println("Error: PLACEHOLDER is not a valid genre.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid genre. Please enter one of: PSYCHEDELIC_ROCK, MATH_ROCK, POST_ROCK, or 'null' to remove.");
            }
        }
    }

    /**
     * Executes the update command without a predefined ID.
     * Prompts the user to enter an ID, then updates that element.
     */
    @Override
    public void execute() {
        executeWithId(null);
    }

    /**
     * Executes the update command with a predefined ID.
     * Prompts for all fields, keeping existing values as defaults.
     *
     * @param predefinedId the ID of the element to update, or null to prompt
     */
    @Override
    public void executeWithId(Long predefinedId) {
        Scanner scanner = new Scanner(System.in);

        Long targetId = predefinedId;
        if (targetId == null) {
            System.out.println("Enter ID of MusicBand to update:");
            targetId = null;
            while (targetId == null) {
                String input = scanner.nextLine().trim();
                try {
                    targetId = Long.parseLong(input);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid ID format. Please enter a valid number.");
                }
            }
        }

        MusicBand existingBand = heap.findById(targetId);
        if (existingBand == null) {
            System.out.println("No MusicBand found with id " + targetId + ".");
            return;
        }

        System.out.println("\n=== Updating MusicBand (ID: " + existingBand.getId() + ") ===");
        System.out.println("Press Enter to keep the default value.\n");

        MusicBand updatedBand = new MusicBand();
        updatedBand.setId(existingBand.getId());
        updatedBand.setCreationDate(existingBand.getCreationDate());

        String newName = getInputWithDefault(scanner, "Name", existingBand.getName());
        updatedBand.setName(newName);

        Integer newParticipants = getIntInputWithDefault(scanner, "Number of participants", existingBand.getNumberOfParticipants());
        updatedBand.setNumberOfParticipants(newParticipants);

        MusicGenre newGenre = getGenreInputWithDefault(scanner, existingBand.getGenre());
        updatedBand.setGenre(newGenre);

        Long newX = getLongInputWithDefault(scanner, "Coordinates x", existingBand.getCoordinates().getX(), 554);
        Integer newY = getYInputWithDefault(scanner, "Coordinates y", existingBand.getCoordinates().getY());
        updatedBand.setCoordinates(new Coordinates(newX, newY));

        String newDescription = getInputWithDefault(scanner, "Description", existingBand.getDescription() != null ? existingBand.getDescription() : "");
        updatedBand.setDescription(newDescription.isEmpty() ? null : newDescription);

        String albumName = getInputWithDefault(scanner, "Best album name", existingBand.getBestAlbum().getName());
        Double albumSales = getDoubleInputWithDefault(scanner, "Best album sales", existingBand.getBestAlbum().getSales());
        updatedBand.setBestAlbum(new Album(albumName, albumSales));

        heap.updateElement(updatedBand);
        System.out.println("\nMusicBand updated successfully!");
        System.out.println(updatedBand);
    }
}
