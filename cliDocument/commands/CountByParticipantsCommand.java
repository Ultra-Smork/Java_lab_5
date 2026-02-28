package com.commands;

import com.utils.MinHeap;
import com.utils.Command;

/**
 * Command implementation for counting elements by number of participants.
 * This command counts how many music bands in the collection have exactly
 * the specified number of participants.
 */
public class CountByParticipantsCommand implements Command {
    /** Reference to the singleton MinHeap instance */
    private MinHeap heap = MinHeap.getInstance();
    /** The target number of participants to count */
    private Integer targetCount;

    /**
     * Constructs a CountByParticipantsCommand with the specified target count.
     *
     * @param targetCount the number of participants to search for
     */
    public CountByParticipantsCommand(Integer targetCount) {
        this.targetCount = targetCount;
    }

    /**
     * Executes the count_by_number_of_participants command.
     */
    @Override
    public void execute() {
        int count = heap.countByNumberOfParticipants(targetCount);
        System.out.println("Number of elements with " + targetCount + " participants: " + count);
    }
}
