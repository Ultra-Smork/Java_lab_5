package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import java.util.Scanner;

import com.model.Album;
import com.model.Coordinates;
import com.model.MusicBand;
import com.model.MusicGenre;

/**
 * Command implementation for adding a new music band only if its ID is less than the minimum.
 * This command prompts the user for all required fields and adds the new element
 * to the collection only if its ID is less than the current minimum ID in the heap.
 */
public class AddIfMinCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();

    /**
     * Gets a non-empty string input from the user.
     * Continues prompting until valid non-empty input is received.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the message to display to the user
     * @return a non-empty string input
     */
    private String getNonEmptyInput(Scanner scanner, String prompt) {
        String input;
        while (true) {
            System.out.println(prompt);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Error: Input cannot be empty. Please try again.");
        }
    }

    /**
     * Gets a positive integer input from the user.
     * Continues prompting until a value greater than 0 is received.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the message to display to the user
     * @return a positive integer value
     */
    private int getPositiveIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Error: Value must be greater than 0. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Gets a valid x-coordinate from the user.
     * The x-coordinate must be less than or equal to 554.
     *
     * @param scanner the Scanner to read input from
     * @return a valid x-coordinate value
     */
    private long getValidXCoordinate(Scanner scanner) {
        while (true) {
            System.out.println("Enter coordinates x (max 554):");
            String input = scanner.nextLine().trim();
            try {
                long value = Long.parseLong(input);
                if (value <= 554) {
                    return value;
                }
                System.out.println("Error: X coordinate must be <= 554. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Gets a valid y-coordinate from the user.
     * The y-coordinate must be less than or equal to 782.
     *
     * @param scanner the Scanner to read input from
     * @return a valid y-coordinate value
     */
    private int getValidYCoordinate(Scanner scanner) {
        while (true) {
            System.out.println("Enter coordinates y (max 782):");
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value <= 782) {
                    return value;
                }
                System.out.println("Error: Y coordinate must be <= 782. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Gets a valid music genre from the user.
     * Allows the user to select from predefined genres or skip (null).
     *
     * @param scanner the Scanner to read input from
     * @return the selected MusicGenre or null if skipped
     */
    private MusicGenre getValidGenre(Scanner scanner) {
        while (true) {
            System.out.println("Enter genre (or press Enter to skip):");
            System.out.println("PSYCHEDELIC_ROCK | MATH_ROCK | POST_ROCK");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                MusicGenre genre = MusicGenre.valueOf(input.toUpperCase());
                if (genre != MusicGenre.PLACEHOLDER) {
                    return genre;
                }
                System.out.println("Error: PLACEHOLDER is not a valid genre. Please select from the available genres.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid genre. Please enter one of: PSYCHEDELIC_ROCK, MATH_ROCK, POST_ROCK or press Enter to skip.");
            }
        }
    }

    /**
     * Gets a positive double input from the user.
     * Continues prompting until a value greater than 0 is received.
     *
     * @param scanner the Scanner to read input from
     * @param prompt the message to display to the user
     * @return a positive double value
     */
    private Double getPositiveDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Error: Value must be greater than 0. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid number.");
            }
        }
    }

    /**
     * Gets a user ID input that must be less than the current minimum ID.
     * Validates that the ID is positive and less than the current minimum.
     *
     * @param scanner the Scanner to read input from
     * @param currentMinId the current minimum ID in the collection (can be null)
     * @return a valid user ID
     */
    private long getUserIdInput(Scanner scanner, Long currentMinId) {
        while (true) {
            String prompt = currentMinId != null 
                ? "Enter ID (must be less than " + currentMinId + "):"
                : "Enter ID (any positive number):";
            System.out.println(prompt);
            String input;
            try {
                input = scanner.nextLine().trim();
            } catch (Exception e) {
                System.out.println("Error reading input: " + e.getMessage());
                continue;
            }
            try {
                long value = Long.parseLong(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Error: ID must be greater than 0. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Executes the add_if_min command.
     * Prompts for ID first (must be less than current minimum), then all other fields.
     */
    @Override
    public void execute() {
        Scanner scanner = new Scanner(System.in);

        MusicBand currentMin = heap.peek();
        Long currentMinId = currentMin != null ? currentMin.getId() : null;

        if (currentMinId == null) {
            System.out.println("Collection is empty. Any ID will be accepted.");
        } else {
            System.out.println("Current minimum ID in collection: " + currentMinId);
        }

        long userId = getUserIdInput(scanner, currentMinId);

        if (currentMinId != null && userId >= currentMinId) {
            System.out.println("Element not added: ID (" + userId + ") must be less than current minimum (" + currentMinId + ").");
            return;
        }

        System.out.println("Success! ID is less than current minimum. Proceeding with band details...");

        MusicBand newBand = new MusicBand();
        newBand.setId(userId);

        newBand.setName(getNonEmptyInput(scanner, "Enter band name:"));

        newBand.setNumberOfParticipants(getPositiveIntInput(scanner, "Enter number of members:"));

        newBand.setGenre(getValidGenre(scanner));

        long x = getValidXCoordinate(scanner);
        int y = getValidYCoordinate(scanner);
        newBand.setCoordinates(new Coordinates(x, y));

        System.out.println("Write some description for this band (optional, press Enter to skip):");
        System.out.print(" - ");
        String description = scanner.nextLine().trim();
        newBand.setDescription(description.isEmpty() ? null : description);

        System.out.println("Let's set best album");
        String albumName = getNonEmptyInput(scanner, "Input name of this album");
        Double sales = getPositiveDoubleInput(scanner, "Input total sales of this album");
        Album album = new Album(albumName, sales);
        newBand.setBestAlbum(album);

        heap.insert(newBand);
        System.out.println("Added new band (ID was less than target): " + newBand);
    }
}
