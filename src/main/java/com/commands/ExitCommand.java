package com.commands;
import com.utils.Command;

public class ExitCommand implements Command{
    @Override
    public void execute() {
        System.out.println("Exiting the program...");
        System.exit(0);
    }
}
