package com.client.handlers;

import com.model.Album;
import com.model.Coordinates;
import com.model.MusicBand;
import com.model.MusicGenre;

import java.util.Scanner;

public class ClientBandPrompt {
    public static MusicBand promptForBand(Scanner scanner, MusicBand existing) {
        MusicBand band = new MusicBand();
        
        String defaultName = existing != null ? existing.getName() : "";
        String name = "";
        while (true) {
            System.out.print("Enter band name" + (existing != null ? " [" + defaultName + "]" : "") + ": ");
            name = scanner.nextLine().trim();
            if (!name.isEmpty()) {
                break;
            }
            if (existing != null && name.isEmpty()) {
                name = defaultName;
                break;
            }
            System.out.println("Error: Name cannot be empty. Please try again.");
        }
        band.setName(name);
        
        String defaultParticipants = existing != null ? String.valueOf(existing.getNumberOfParticipants()) : "";
        int participants = 0;
        while (true) {
            System.out.print("Enter number of members" + (existing != null ? " [" + defaultParticipants + "]" : "") + ": ");
            String participantsStr = scanner.nextLine().trim();
            if (participantsStr.isEmpty() && existing != null) {
                participants = existing.getNumberOfParticipants();
                break;
            }
            if (participantsStr.isEmpty()) {
                System.out.println("Error: Number cannot be empty. Please try again.");
                continue;
            }
            try {
                participants = Integer.parseInt(participantsStr);
                if (participants > 0) {
                    break;
                } else {
                    System.out.println("Error: Number must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
        band.setNumberOfParticipants(participants);
        
        String defaultGenre = existing != null && existing.getGenre() != null ? existing.getGenre().toString() : "";
        MusicGenre genre = null;
        while (true) {
            System.out.print("Enter genre (PSYCHEDELIC_ROCK, MATH_ROCK, POST_ROCK)" + (existing != null ? " [" + defaultGenre + "]" : "") + " (or press Enter to skip): ");
            String genreStr = scanner.nextLine().trim();
            if (genreStr.isEmpty()) {
                genre = existing != null ? existing.getGenre() : null;
                break;
            }
            if (genreStr.equalsIgnoreCase("null")) {
                genre = null;
                break;
            }
            try {
                genre = MusicGenre.valueOf(genreStr.toUpperCase());
                if (genre == MusicGenre.PLACEHOLDER) {
                    System.out.println("Error: PLACEHOLDER is not a valid genre. Please try again.");
                } else {
                    break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: Invalid genre. Please enter one of: PSYCHEDELIC_ROCK, MATH_ROCK, POST_ROCK or press Enter to skip.");
            }
        }
        band.setGenre(genre);
        
        long defaultX = existing != null ? existing.getCoordinates().getX() : 0;
        long x = 0;
        while (true) {
            System.out.print("Enter coordinates x (max 554)" + (existing != null ? " [" + defaultX + "]" : "") + ": ");
            String xStr = scanner.nextLine().trim();
            if (xStr.isEmpty() && existing != null) {
                x = defaultX;
                break;
            }
            if (xStr.isEmpty()) {
                System.out.println("Error: X coordinate cannot be empty. Please try again.");
                continue;
            }
            try {
                x = Long.parseLong(xStr);
                if (x <= 554) {
                    break;
                } else {
                    System.out.println("Error: X coordinate must be <= 554. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
        
        int defaultY = existing != null ? existing.getCoordinates().getY() : 0;
        int y = 0;
        while (true) {
            System.out.print("Enter coordinates y (max 782)" + (existing != null ? " [" + defaultY + "]" : "") + ": ");
            String yStr = scanner.nextLine().trim();
            if (yStr.isEmpty() && existing != null) {
                y = defaultY;
                break;
            }
            if (yStr.isEmpty()) {
                System.out.println("Error: Y coordinate cannot be empty. Please try again.");
                continue;
            }
            try {
                y = Integer.parseInt(yStr);
                if (y <= 782) {
                    break;
                } else {
                    System.out.println("Error: Y coordinate must be <= 782. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid integer.");
            }
        }
        band.setCoordinates(new Coordinates(x, y));
        
        String defaultDesc = existing != null ? existing.getDescription() : "";
        while (true) {
            System.out.print("Enter description" + (existing != null ? " [" + defaultDesc + "]" : "") + " (or press Enter to skip): ");
            String desc = scanner.nextLine().trim();
            if (desc.isEmpty() && existing != null) {
                desc = defaultDesc;
            }
            if (!desc.isEmpty() && desc.length() > 100) {
                System.out.println("Error: Description must be <= 100 characters. Please try again.");
                continue;
            }
            band.setDescription(desc.isEmpty() ? null : desc);
            break;
        }
        
        String defaultAlbumName = existing != null && existing.getBestAlbum() != null ? existing.getBestAlbum().getName() : "";
        String albumName = "";
        while (true) {
            System.out.print("Enter best album name" + (existing != null ? " [" + defaultAlbumName + "]" : "") + ": ");
            albumName = scanner.nextLine().trim();
            if (!albumName.isEmpty()) {
                break;
            }
            if (existing != null && albumName.isEmpty()) {
                albumName = defaultAlbumName;
                break;
            }
            System.out.println("Error: Album name cannot be empty. Please try again.");
        }
        
        double defaultSales = existing != null && existing.getBestAlbum() != null ? existing.getBestAlbum().getSales() : 0;
        double sales = 0;
        while (true) {
            System.out.print("Enter best album sales" + (existing != null ? " [" + defaultSales + "]" : "") + ": ");
            String salesStr = scanner.nextLine().trim();
            if (salesStr.isEmpty() && existing != null) {
                sales = defaultSales;
                break;
            }
            if (salesStr.isEmpty()) {
                System.out.println("Error: Sales cannot be empty. Please try again.");
                continue;
            }
            try {
                sales = Double.parseDouble(salesStr);
                if (sales >= 0) {
                    break;
                } else {
                    System.out.println("Error: Sales must be >= 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format. Please enter a valid number.");
            }
        }
        band.setBestAlbum(new Album(albumName, sales));
        
        return band;
    }
}