package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import com.model.MusicBand;
import java.util.List;

public class AverageParticipantsCommand implements Command {
    MinHeap heap = MinHeap.getInstance();

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
