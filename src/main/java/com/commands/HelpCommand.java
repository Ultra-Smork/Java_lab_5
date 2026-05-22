package com.commands;

import com.common.Response;
import java.util.Map;

public class HelpCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        String help = 
            """
            ╔═══════════════════════════════════════════╗
            ║       MUSIC BAND COLLECTION — HELP       ║
            ╚═══════════════════════════════════════════╝

            ─── TABLE ───
            Double-click a row → view full band details
            Click column headers → sort asc/desc
            Filter fields above table → narrow results

            ─── CANVAS (right panel) ───
            Hover a dot → tooltip with band name + owner
            Click a dot → info dialog with full details
            Colors = different owners, size = participants

            ─── TOOLBAR BUTTONS ───
            Add             → create a new band (dialog)
            Add If Min      → add band only if it's the smallest
            Update          → modify a band (select row first)
            Remove by ID    → delete band by number
            Remove Greater  → delete all your bands with ID > N
            Remove by Album → delete bands matching album name
            Clear           → delete ALL your bands
            Info            → collection stats (count, type, date)
            History         → last 11 commands executed
            Execute Script  → run commands from a .txt file

            ─── ACCOUNT ───
            Logout (red)    → return to login screen
            Language menu   → switch UI language on the fly

            ─── TIPS ───
            • You can only modify bands you created
            • ID must be a positive integer
            • Coordinates: X ≤ 554, Y ≤ 782
            """;
        return Response.success(help);
    }
}