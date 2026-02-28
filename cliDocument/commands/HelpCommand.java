package com.commands;
import com.utils.Command;

/**
 * Command implementation for displaying help information.
 * This command displays a list of all available commands and their usage.
 */
public class HelpCommand implements Command{
    /**
     * Executes the help command, displaying all available commands.
     */
    @Override
    public void execute() {
        System.out.println("Available commands:");
        System.out.println("help                                    - Show this help message");
        System.out.println("info                                    - Show information about the collection");
        System.out.println("show                                    - Show all elements in the collection");
        System.out.println("add                                     - Add a new element to the collection");
        System.out.println("update id <id>                          - Update element by ID");
        System.out.println("remove_by_id <id>                       - Remove element by ID");
        System.out.println("clear                                   - Clear the collection");
        System.out.println("save                                    - Save collection to file");
        System.out.println("execute_script <file_name>             - Execute commands from script file");
        System.out.println("exit                                    - Exit the application");
        System.out.println("add_if_min                              - Add element if it's less than minimum");
        System.out.println("remove_greater <id>                    - Remove elements with ID greater than given");
        System.out.println("history                                 - Show command history");
        System.out.println("remove_any_by_best_album <album_name>   - Remove element by best album name");
        System.out.println("average_of_number_of_participants      - Show average number of participants");
        System.out.println("count_by_number_of_participants <num>  - Count elements with given number of participants");
    }
}
