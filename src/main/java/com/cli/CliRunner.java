package com.cli;
import java.util.Scanner;

import com.utils.CommandManager;
import com.utils.MinHeap;

public class CliRunner {
    public CliRunner(){}
    public static void start() {
        String asf = "Main Menu is going to look like this\nChoose your action:\nA - Add new MusicBand to your list **PLACEHOLDER**\nV - Shows current list \nU - Update existing entry **TODO**\nN - Saves current list state **TODO**\nH - Shows actions history **TODO**\nM - Execute script **TODO**\nL - Deletes the whole list (!!!CAN'T BE UNDONE!!!) **TODO**\nX - Exit \n\n\n\n--- help - to see every options avaible ---\n\nDEBUG INFO#################################################\nS - Collection state \n";
        while (true){
            Scanner scanner = new Scanner(System.in);
            System.out.print(asf);
            String command = scanner.nextLine().trim().toUpperCase();
            CommandManager.HandleCommand(command, MinHeap.getInstance());
            scanner.nextLine();
            clearConsole();
        }

    }
    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
