package ru.skillbox.collection_adv;

public enum Commands {
    IN("in"),
    DEL("del"),
    COUNT("count"),
    AVG("avg"),
    MEDIAN("median"),
    YOUNG("young"),
    OLD("old"),
    PRINT("print"),
    HELP("help"),
    EXIT("exit");

    private final String text;

    // constructor
    Commands(String text) {
        this.text = text;
    }

    // getter
    public String getText() {
        return text;
    }

    // optional: lookup by string
    public static Commands fromString(String text) {
        for (Commands c : Commands.values()) {
            if (c.text.equalsIgnoreCase(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException("No command with text.  try again" + text);
    }
}
