package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import com.model.MusicBand;

public class ParticipantsByIdCommand implements Command {
    private MinHeap heap;
    private Long id;

    public ParticipantsByIdCommand(Long id) {
        this.heap = MinHeap.getInstance();
        this.id = id;
    }

    @Override
    public void execute() {
        MusicBand band = heap.findById(id);
        
        if (band == null) {
            System.out.println("Error: No music band found with ID " + id);
            return;
        }

        Integer participants = band.getNumberOfParticipants();
        if (participants == null) {
            System.out.println("Error: Number of participants is not set for band with ID " + id);
            return;
        }

        System.out.println("Number of participants for band ID " + id + ": " + participants);
    }
}
