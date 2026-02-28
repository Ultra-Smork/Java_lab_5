package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import com.model.MusicBand;
import java.util.List;

/**
 * Command implementation for calculating the average number of participants.
 * This command calculates the average value of the numberOfParticipants field
 * across all music bands in the collection.
 */
public class AverageParticipantsCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    MinHeap heap = MinHeap.getInstance();

    /**
     * Executes the average participants command, calculating and displaying the average.
     */
    @Override
    public void execute() {
        if (heap.isEmpty()) {
            System.out.println("Error: Collection is empty. Cannot calculate average.");
            return;
        }

        List<MusicBand> allBands = heap.getAllElements();
        int count = 0;
        int sum = 0;

        for (MusicBand band : allBands) {
            if (band.getNumberOfParticipants() != null) {
                sum += band.getNumberOfParticipants();
                count++;
            }
        }

        if (count == 0) {
            System.out.println("Error: No valid number_of_participants data in collection.");
            return;
        }

        double average = (double) sum / count;
        System.out.println("Average number of participants: " + average);
    }
}
