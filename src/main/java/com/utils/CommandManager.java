package com.utils;
public class CommandManager {
    public static void HandleCommand(String command, MinHeap heap) {
        Invoker invoker = new Invoker();
        if (!validateCommand(command)) {
            return;
        }

        String[] parts = command.trim().split("\\s+");
        // this require for input to look like "R 5" or "r 5" for remove by id command, where 5 is the id of element to remove
        String cmd = parts[0].toUpperCase();
        boolean hasArgument = parts.length > 1;
        String argument = hasArgument ? parts[1] : null;
        heap.addToHistory(cmd);

        switch (cmd){
            case "V":
                invoker.ListAll();
                break;
            case "A":
                invoker.AddElement();
                break;
            case "X":
                invoker.Exit();
                break;
            case "S":
                invoker.ListInfo();
                break;
            case "H":
                invoker.PrintHistory();
                break;
            case "L":
                invoker.Clear();
                break;
            case "HELP":
                invoker.PrintHelp();
                break;
            case "R":
                if (hasArgument) {
                    try {
                        Long id = Long.parseLong(argument);
                        invoker.RemoveById(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid argument. Please provide a valid ID.");
                    }
                } else {
                    System.out.println("Error: 'R' command requires an argument (ID).");
                }
                break;
            case "RB":
                if (hasArgument) {
                    invoker.RemoveByBestAlbum(argument);
                } else {
                    System.out.println("Error: 'RB' command requires an argument (album name).");
                }
                break;
            case "REMOVE_GREATER":
                if (hasArgument) {
                    try {
                        Long id = Long.parseLong(argument);
                        invoker.RemoveGreaterThanId(id);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Invalid argument. Please provide a valid ID.");
                    }
                } else {
                    System.out.println("Error: 'REMOVE_GREATER' command requires an argument (ID).");
                }
                break;
            case "U":
                invoker.UpdateElement();
                break;
            case "M":
                if (hasArgument) {
                    invoker.ExecuteScript(argument);
                } else {
                    System.out.println("Error: 'M' command requires a file path argument.");
                }
                break;
            case "ADD_IF_MIN":
                invoker.AddIfMin();
                break;

            default:
                System.out.println("Unknown command. Please try again.");
        }
    }

    private static boolean validateCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            System.out.println("Error: Command cannot be empty. Please try again.");
            return false;
        }
        String[] validCommands = {"V", "A", "S", "H", "X", "HELP", "L", "R", "RB", "U", "REMOVE_GREATER", "M", "ADD_IF_MIN"};
        String trimmed = command.trim();
        String cmdPart = trimmed.split("\\s+")[0];
        for (String valid : validCommands) {
            if (cmdPart.equalsIgnoreCase(valid)) {
                return true;
            }
        }
            System.out.println("Error: Invalid command. Valid commands are: V, A, S, H, X, HELP, L, R, RB, U, REMOVE_GREATER, M. Please try again.");
        return false;
    }
}
