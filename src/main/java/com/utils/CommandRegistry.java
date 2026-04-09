package com.utils;

import com.commands.*;
import com.common.Command;
import com.common.Response;
import com.server.CommandHistory;

import java.util.*;

/**
 * Registry of all available commands on the server.
 *
 * This class holds a map of command names to command implementations.
 * When a command request comes in, this registry executes the
 * appropriate command and returns the result.
 */
public class CommandRegistry {
    private final Map<Command, ServerCommand> commands;
    private final CommandHistory commandHistory;

    public CommandRegistry(CommandHistory commandHistory) {
        this.commandHistory = commandHistory;
        this.commands = new HashMap<>();
        registerCommands();
    }

    private void registerCommands() {
        commands.put(Command.SHOW, new ShowCommand());
        commands.put(Command.SELECT, new SelectCommand());
        commands.put(Command.INFO, new InfoCommand());
        commands.put(Command.HELP, new HelpCommand());
        commands.put(Command.REGISTER, new RegisterCommand());
        commands.put(Command.LOGIN, new LoginCommand());
        commands.put(Command.LOGOUT, new LogoutCommand());
        commands.put(Command.CLEAR, new ClearCommand());
        commands.put(Command.HISTORY, new HistoryCommand(commandHistory));
        commands.put(Command.AVERAGE_OF_NUMBER_OF_PARTICIPANTS, new AverageParticipantsCommand());
        commands.put(Command.ADD, new AddCommand());
        commands.put(Command.ADD_IF_MIN, new AddIfMinCommand());
        commands.put(Command.UPDATE, new UpdateCommand());
        commands.put(Command.REMOVE_BY_ID, new RemoveByIdCommand());
        commands.put(Command.REMOVE_GREATER, new RemoveGreaterCommand());
        commands.put(Command.REMOVE_ANY_BY_BEST_ALBUM, new RemoveByBestAlbumCommand());
        commands.put(Command.COUNT_BY_NUMBER_OF_PARTICIPANTS, new CountByParticipantsCommand());
        commands.put(Command.PARTICIPANTS_BY_ID, new ParticipantsByIdCommand());
        commands.put(Command.EXECUTE_SCRIPT, new ExecuteScriptCommand());
    }

    public Response execute(Command command, Map<String, Object> args) {
        final Map<String, Object> finalArgs = (args == null) ? new HashMap<>() : args;
        ServerCommand cmd = commands.get(command);
        if (cmd != null) {
            return cmd.execute(finalArgs);
        }
        return Response.error("Unknown command: " + command.name().toLowerCase());
    }

    public Response execute(String commandName, Map<String, Object> args) {
        final Map<String, Object> finalArgs = (args == null) ? new HashMap<>() : args;
        Command command = Command.fromString(commandName);
        if (command != null) {
            return execute(command, finalArgs);
        }
        return Response.error("Unknown command: " + commandName);
    }

    public Response executeWithData(Command command, Map<String, Object> args, Object data) {
        if (args == null) {
            args = new HashMap<>();
        }
        if (data != null) {
            args.put("band", data);
        }
        return execute(command, args);
    }

    public Response executeWithData(String commandName, Map<String, Object> args, Object data) {
        if (args == null) {
            args = new HashMap<>();
        }
        if (data != null) {
            args.put("band", data);
        }
        return execute(commandName, args);
    }

    public List<String> getCommandNames() {
        return commands.keySet().stream()
            .map(Command::name)
            .sorted()
            .collect(java.util.stream.Collectors.toList());
    }
}