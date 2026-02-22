package com.cli;
import java.util.Scanner;
import java.util.NoSuchElementException;
import com.utils.CommandManager;
import com.utils.Invoker;
import com.utils.MinHeap;

public class CliRunner {
    public static void start() {
        Invoker invoker = new Invoker();
        Scanner scanner = new Scanner(System.in);
        invoker.Load();
        while (true){
            try {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                invoker.PrintHelp();
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }
                String command;
                if (input.contains(" ")) {
                    String cmdPart = input.substring(0, input.indexOf(" ")).toUpperCase();
                    String argPart = input.substring(input.indexOf(" ") + 1);
                    command = cmdPart + " " + argPart;
                } else {
                    command = input.toUpperCase();
                }
                CommandManager.HandleCommand(command, MinHeap.getInstance());
            } catch (NoSuchElementException e) {
                System.out.println("\nInput closed. Exiting...");
                break;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            scanner.nextLine();
        }
    }
}
