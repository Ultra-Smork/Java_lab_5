package com.utils;

import com.commands.AddElementCommand;
import com.commands.AddIfMinCommand;
import com.commands.AverageParticipantsCommand;
import com.commands.CleanHeapCommand;
import com.commands.CountByParticipantsCommand;
import com.commands.ExitCommand;
import com.commands.ExecuteScriptCommand;
import com.commands.HelpCommand;
import com.commands.ListEverythingCommand;
import com.commands.ListInfoCommand;
import com.commands.LoadCommand;
import com.commands.ParticipantsByIdCommand;
import com.commands.RemoveByIdCommand;
import com.commands.RemoveByBestAlbumCommand;
import com.commands.RemoveGreaterThanIdCommand;
import com.commands.SaveCommand;
import com.commands.ShowHistoryCommand;
import com.commands.UpdateElementCommand;

/**
 * Invoker class that acts as a central hub for executing commands.
 * This class holds references to all command implementations and provides
 * methods to invoke them based on user input. It follows the Command pattern
 * to decouple command invocation from command execution logic.
 */
public class Invoker {
    /** Command to list all elements in the collection */
    ListEverythingCommand listEverythingCommand = new ListEverythingCommand();
    /** Command to exit the application */
    ExitCommand exitCommand = new ExitCommand();
    /** Command to display collection information */
    ListInfoCommand listInfoCommand = new ListInfoCommand();
    /** Command to add a new element */
    AddElementCommand addElementCommand = new AddElementCommand();
    /** Command to add element if it's minimum */
    AddIfMinCommand addIfMinCommand = new AddIfMinCommand();
    /** Command to calculate average participants */
    AverageParticipantsCommand averageParticipantsCommand = new AverageParticipantsCommand();
    /** Command to show command history */
    ShowHistoryCommand printHistoryCommand = new ShowHistoryCommand();
    /** Command to display help */
    HelpCommand helpCommand = new HelpCommand();
    /** Command to clear the collection */
    CleanHeapCommand cleanHeapCommand = new CleanHeapCommand();
    /** Command to remove element by ID */
    RemoveByIdCommand removeByIdCommand = new RemoveByIdCommand();
    /** Command to remove element by best album */
    RemoveByBestAlbumCommand removeByBestAlbumCommand = new RemoveByBestAlbumCommand();
    /** Command to remove elements greater than ID */
    RemoveGreaterThanIdCommand removeGreaterThanIdCommand = new RemoveGreaterThanIdCommand();
    /** Command to update element */
    UpdateElementCommand updateElementCommand = new UpdateElementCommand();
    /** Command to save collection to file */
    SaveCommand saveCommand = new SaveCommand();
    /** Command to execute script - initialized lazily */
    ExecuteScriptCommand executeScriptCommand;

    /**
     * Removes a music band from the collection by its unique ID.
     *
     * @param id the unique identifier of the music band to remove
     */
    public void RemoveById(Long id){
        removeByIdCommand.executeWithInt(id);
    }
    
    /**
     * Removes a music band from the collection by its best album name.
     * Only removes one element (the first match found).
     *
     * @param albumName the name of the best album to search for
     */
    public void RemoveByBestAlbum(String albumName){
        removeByBestAlbumCommand.executeWithString(albumName);
    }
    
    /**
     * Removes all music bands from the collection that have an ID greater than the specified value.
     *
     * @param id the ID threshold - all bands with ID greater than this will be removed
     */
    public void RemoveGreaterThanId(Long id){
        removeGreaterThanIdCommand.executeWithLong(id);
    }
    
    /**
     * Updates an element in the collection by prompting the user for new values.
     * This method prompts for the ID and then all fields.
     */
    public void UpdateElement(){
        updateElementCommand.execute();
    }
    
    /**
     * Lists all elements in the collection, displaying their string representation.
     */
    public void ListAll(){
        listEverythingCommand.execute();
    }
    
    /**
     * Exits the application without saving.
     */
    public void Exit(){
        exitCommand.execute();
    }
    
    /**
     * Clears all elements from the collection.
     */
    public void Clear(){
        cleanHeapCommand.execute();
    }
    
    /**
     * Displays information about the collection including type, initialization date, and element count.
     */
    public void ListInfo(){
        listInfoCommand.execute();
    }
    
    /**
     * Adds a new element to the collection by prompting the user for all field values.
     */
    public void AddElement(){
        addElementCommand.execute();
    }

    /**
     * Adds a new element to the collection only if its ID is less than the minimum ID in the collection.
     * Prompts the user for all field values.
     */
    public void AddIfMin(){
        addIfMinCommand.execute();
    }

    /**
     * Calculates and displays the average value of the numberOfParticipants field
     * across all elements in the collection.
     */
    public void AverageParticipants(){
        averageParticipantsCommand.execute();
    }

    /**
     * Displays the number of participants for a specific music band by its ID.
     *
     * @param id the unique identifier of the music band
     */
    public void ParticipantsById(Long id){
        ParticipantsByIdCommand cmd = new ParticipantsByIdCommand(id);
        cmd.execute();
    }

    /**
     * Displays the history of the last 11 commands executed.
     */
    public void PrintHistory(){
        printHistoryCommand.execute();
    }

    /**
     * Displays help information about all available commands.
     */
    public void PrintHelp(){
        helpCommand.execute();
    }

    /**
     * Executes a script file containing commands to be processed.
     *
     * @param filePath the path to the script file to execute
     */
    public void ExecuteScript(String filePath){
        executeScriptCommand = new ExecuteScriptCommand(filePath);
        executeScriptCommand.setInvoker(this);
        executeScriptCommand.execute();
    }

    /**
     * Updates a specific element in the collection by its ID with new values.
     *
     * @param id the unique identifier of the music band to update
     */
    public void UpdateElementWithId(Long id){
        updateElementCommand.executeWithId(id);
    }

    /**
     * Saves the collection to the default file path.
     */
    public void Save(){
        saveCommand.execute();
    }

    /**
     * Loads the collection from the default file path.
     */
    public void Load(){
        LoadCommand cmd = new LoadCommand();
        cmd.execute();
    }

    /**
     * Loads the collection from a specified file path.
     *
     * @param filePath the path to the file to load from
     */
    public void Load(String filePath){
        LoadCommand cmd = new LoadCommand(filePath);
        cmd.execute();
    }

    /**
     * Counts and displays the number of elements in the collection
     * that have the specified number of participants.
     *
     * @param count the number of participants to search for
     */
    public void CountByParticipants(Integer count){
        CountByParticipantsCommand cmd = new CountByParticipantsCommand(count);
        cmd.execute();
    }
}
