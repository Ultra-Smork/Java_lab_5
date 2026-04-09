package com.common;

public enum Command {
    SHOW,
    SELECT,
    INFO,
    HELP,
    HISTORY,
    SAVE,
    AVERAGE_OF_NUMBER_OF_PARTICIPANTS,
    COUNT_BY_NUMBER_OF_PARTICIPANTS,
    PARTICIPANTS_BY_ID,
    REGISTER,
    LOGIN,
    LOGOUT,
    ADD,
    ADD_IF_MIN,
    UPDATE,
    REMOVE_BY_ID,
    REMOVE_GREATER,
    REMOVE_ANY_BY_BEST_ALBUM,
    CLEAR,
    EXECUTE_SCRIPT;

    public static Command fromString(String str) {
        if (str == null) return null;
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}