package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import java.util.Scanner;

import com.model.Album;
import com.model.Coordinates;
import com.model.MusicBand;
import com.model.MusicGenre;

public class AddElementCommand implements Command{
    MinHeap heap = MinHeap.getInstance();

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

    private MusicGenre getValidGenre(Scanner scanner) {
        while (true) {
            System.out.println("Enter genre:");
            System.out.println("PSYCHEDELIC_ROCK | MATH_ROCK | POST_ROCK");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                MusicGenre genre = MusicGenre.valueOf(input);
                if (genre != MusicGenre.PLACEHOLDER) {
                    return genre;
                }
                System.out.println("Error: PLACEHOLDER is not a valid genre. Please select from the available genres.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid genre. Please enter one of: PSYCHEDELIC_ROCK, MATH_ROCK, POST_ROCK");
            }
        }
    }

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

    @Override
    public void execute(){
        Scanner scanner = new Scanner(System.in);
        MusicBand newBand = new MusicBand();

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
        System.out.println("Added new band: " + newBand);
    }
}
