package com.utils;

/**
 * Manager class responsible for parsing and routing user commands to the appropriate invoker methods.
 * This class handles command validation, argument parsing, and delegation to the Invoker for execution.
 */
public class CommandManager {
    
    /**
     * Parses and executes a command based on the input string.
     * This method validates the command, extracts any arguments, and routes
     * the command to the appropriate Invoker method for execution.
     * 
     * <p>Supported commands include:</p>
     * <ul>
     *   <li>show - Display all elements in the collection</li>
     *   <li>add - Add a new element to the collection</li>
     *   <li>info - Display collection information</li>
     *   <li>history - Display command history</li>
     *   <li>clear - Clear the collection</li>
     *   <li>help - Display available commands</li>
     *   <li>remove_by_id - Remove element by ID</li>
     *   <li>remove_any_by_best_album - Remove element by best album name</li>
     *   <li>remove_greater - Remove elements with ID greater than specified</li>
     *   <li>update id - Update element by ID</li>
     *   <li>execute_script - Execute commands from a script file</li>
     *   <li>add_if_min - Add element if it's less than minimum</li>
     *   <li>average_of_number_of_participants - Calculate average participants</li>
     *   <li>count_by_number_of_participants - Count elements by participants</li>
     *   <li>save - Save collection to file</li>
     *   <li>exit - Exit the application</li>
     * </ul>
     *
     * @param command the command string entered by the user
     * @param heap the MinHeap collection to operate on
     */
    public static void HandleCommand(String command, MinHeap heap) {
        Invoker invoker = new Invoker();
        if (!validateCommand(command)) {
            return;
        }

        String[] parts = command.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();
        boolean hasArgument = parts.length > 1;
        String argument = hasArgument ? parts[1] : null;
        heap.addToHistory(cmd);

        switch (cmd){
            case "show":
                invoker.ListAll();
                break;
            case "add":
                invoker.AddElement();
                break;
            case "exit":
                invoker.Exit();
                break;
            case "info":
                invoker.ListInfo();
                break;
            case "history":
                invoker.PrintHistory();
                break;
            case "clear":
                invoker.Clear();
                break;
            case "help":
                invoker.PrintHelp();
                break;
            case "remove_by_id":
                if (hasArgument) {
                    try {
                        Long id = Long.parseLong(argument);
                        invoker.RemoveById(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid argument. Please provide a valid ID.");
                    }
                } else {
                    System.out.println("Error: 'remove_by_id' command requires an argument (ID).");
                }
                break;
            case "remove_any_by_best_album":
                if (hasArgument) {
                    invoker.RemoveByBestAlbum(argument);
                } else {
                    System.out.println("Error: 'remove_any_by_best_album' command requires an argument (album name).");
                }
                break;
            case "remove_greater":
                if (hasArgument) {
                    try {
                        Long id = Long.parseLong(argument);
                        invoker.RemoveGreaterThanId(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid argument. Please provide a valid ID.");
                    }
                } else {
                    System.out.println("Error: 'remove_greater' command requires an argument (ID).");
                }
                break;
            case "update":
                if (parts.length >= 3 && parts[1].equalsIgnoreCase("id")) {
                    try {
                        Long id = Long.parseLong(parts[2]);
                        invoker.UpdateElementWithId(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid ID. Please provide a valid number.");
                    }
                } else {
                    System.out.println("Error: 'update id <id>' command requires an ID argument.");
                }
                break;
            case "execute_script":
                if (hasArgument) {
                    invoker.ExecuteScript(argument);
                } else {
                    System.out.println("Error: 'execute_script' command requires a file path argument.");
                }
                break;
            case "add_if_min":
                invoker.AddIfMin();
                break;
            case "average_of_number_of_participants":
                invoker.AverageParticipants();
                break;
            case "count_by_number_of_participants":
                if (hasArgument) {
                    try {
                        Integer count = Integer.parseInt(argument);
                        invoker.CountByParticipants(count);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid argument. Please provide a valid number.");
                    }
                } else {
                    System.out.println("Error: 'count_by_number_of_participants' command requires an argument.");
                }
                break;
            case "save":
                invoker.Save();
                break;

            default:
                System.out.println("Unknown command. Please try again.");
        }
    }

    /**
     * Validates that the provided command is a recognized command.
     * This method checks if the command exists in the list of valid commands
     * and handles special cases like multi-word commands (e.g., "update id").
     *
     * @param command the command string to validate
     * @return true if the command is valid, false otherwise
     */
    private static boolean validateCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            System.out.println("Error: Command cannot be empty. Please try again.");
            return false;
        }
        String[] validCommands = {"show", "add", "info", "history", "exit", "help", "clear", "remove_by_id", "update", "remove_greater", "execute_script", "add_if_min", "average_of_number_of_participants", "count_by_number_of_participants", "save", "remove_any_by_best_album"};
        String trimmed = command.trim();
        String cmdPart = trimmed.split("\\s+")[0].toLowerCase();
        
        if (cmdPart.equals("update")) {
            String[] parts = trimmed.split("\\s+");
            if (parts.length >= 2 && parts[1].equalsIgnoreCase("id")) {
                return true;
            }
            System.out.println("Error: Invalid command. Use 'update id <id>'.");
            return false;
        }
        
        for (String valid : validCommands) {
            if (cmdPart.equalsIgnoreCase(valid)) {
                return true;
            }
        }
        System.out.println("Error: Invalid command. Valid commands are: " + String.join(", ", validCommands));
        return false;
    }
}
