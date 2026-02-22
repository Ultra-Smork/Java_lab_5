
package com.commands;
import com.utils.Command;

public class HelpCommand implements Command{
    @Override
    public void execute() {
        System.out.println("Available commands:");
        System.out.println("V                         - View all elements in the collection");
        System.out.println("A                         - Add a new element to the collection");
        System.out.println("ADD_IF_MIN                - Add element if its ID is less than minimum");
        System.out.println("S                         - Show information about the collection");
        System.out.println("H                         - Show command history");
        System.out.println("X                         - Exit the application");
        System.out.println("L                         - Clear the entire collection");
        System.out.println("U                         - Update an existing element");
        System.out.println("R <id>                    - Remove element by ID");
        System.out.println("RB <album_name>           - Remove element(s) by best album name");
        System.out.println("REMOVE_GREATER <id>       - Remove all elements with ID greater than given ID");
        System.out.println("M <file_path>             - Execute commands from script file");
        System.out.println("AVG                       - Show average number of participants");
        System.out.println("PRTS <id>                 - Show number of participants for band with given ID");
        System.out.println("SAVE                      - Save collection to file");
        System.out.println("SAVE <file_path>          - Save collection to custom file path");
        System.out.println("LOAD                      - Load collection from default file");
        System.out.println("LOAD <file_path>          - Load collection from custom file path");
    }
}
