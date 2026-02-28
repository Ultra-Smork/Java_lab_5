package com.commands;

import com.utils.MinHeap;
import com.utils.Command;
import com.model.MusicBand;

/**
 * Command implementation for displaying the number of participants for a specific band.
 * This command finds a music band by its ID and displays the number of participants.
 */
public class ParticipantsByIdCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    private MinHeap heap;
    /** The ID of the music band to look up */
    private Long id;

    /**
     * Constructs a ParticipantsByIdCommand with the specified band ID.
     *
     * @param id the unique identifier of the music band
     */
    public ParticipantsByIdCommand(Long id) {
        this.heap = MinHeap.getInstance();
        this.id = id;
    }

    /**
     * Executes the participants_by_id command, displaying the participant count.
     */
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
