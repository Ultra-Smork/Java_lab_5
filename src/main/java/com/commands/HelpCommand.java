package com.commands;

import com.common.Response;
import java.util.Map;

public class HelpCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        String help = 
            "=== MUSIC BAND COLLECTION COMMANDS ===\n\n" +
            "AUTHENTICATION (no login required):\n" +
            "  register <login> <password>    - Create a new user account\n" +
            "  login <login> <password>      - Authenticate to access modify commands\n\n" +
            
            "VIEW COMMANDS (no login required):\n" +
            "  show                         - Display all music bands sorted by name\n" +
            "  info                         - Display collection information\n" +
            "  help                         - Display this help message\n" +
            "  history                      - Display command history\n" +
            "  count_by_number_of_participants <count> - Count bands with N participants\n" +
            "  participants_by_id <id>     - Show participants for band with ID\n" +
            "  average_of_number_of_participants - Show average participants count\n\n" +
            
            "MODIFY COMMANDS (login required, modify own bands only):\n" +
            "  add                          - Add a new music band (interactive)\n" +
            "  add_if_min <id>              - Add band if ID is less than minimum\n" +
            "  update id <id>              - Update band with specified ID\n" +
            "  remove_by_id <id>           - Remove band with specified ID\n" +
            "  remove_greater <id>         - Remove bands with ID greater than N\n" +
            "  remove_any_by_best_album <album> - Remove bands with specified album\n" +
            "  clear                       - Remove all your bands from collection\n" +
            "  execute_script <file_path>  - Execute commands from script file\n\n" +
            
            "EXAMPLES:\n" +
            "  register alice password123   - Create user 'alice'\n" +
            "  login alice password123      - Login as alice\n" +
            "  add                         - Add a new band (interactive)\n" +
            "  update id 123               - Update band with ID 123\n" +
            "  execute_script ~/scripts.txt - Run commands from file\n";
        return Response.success(help);
    }
}