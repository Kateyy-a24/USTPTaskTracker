package com.ustp.tracker.reminder;

/**
 * How close a task is to its deadline. Each stage is its own little class (enum constant with a body)
 * that overrides the same methods, so callers just ask the stage instead of using if/else chains.
 * This is polymorphism: the same call ( stage.label(days) ) gives a different result for each stage.
 */
public enum DueStage {
    OVERDUE {
        @Override public boolean matches(long days) { return days < 0; }
        @Override public boolean repeats() { return true; }
        @Override public String screenCode() { return "OD"; }
        @Override public String emailCode(long days) { return "OD"; }
        @Override public String label(long days) { return "Overdue by " + (-days) + (days == -1 ? " day" : " days"); }
        @Override public String color() { return "#DC2626"; }
    },
    TODAY {
        @Override public boolean matches(long days) { return days == 0; }
        @Override public boolean repeats() { return true; }
        @Override public String screenCode() { return "D0"; }
        @Override public String label(long days) { return "Due TODAY"; }
        @Override public String color() { return "#EA580C"; }
    },
    TOMORROW {
        @Override public boolean matches(long days) { return days == 1; }
        @Override public boolean repeats() { return false; }
        @Override public String screenCode() { return "D1"; }
        @Override public String label(long days) { return "Due tomorrow"; }
        @Override public String color() { return "#2563EB"; }
    },
    SOON {
        @Override public boolean matches(long days) { return days == 2 || days == 3; }
        @Override public boolean repeats() { return false; }
        @Override public String screenCode() { return "D3"; }
        @Override public String label(long days) { return "Due in " + days + " days"; }
        @Override public String color() { return "#2563EB"; }
    };

    /** True when a task that is `days` days away (negative = already late) belongs to this stage. */
    public abstract boolean matches(long days);

    /** True when the on-screen alert is shown again every hour (overdue and due-today tasks). */
    public abstract boolean repeats();

    /** Short code used to remember on-screen alerts. */
    public abstract String screenCode();

    /** Text shown to the student, for example "Due tomorrow". */
    public abstract String label(long days);

    /** Color used for this stage in emails. */
    public abstract String color();

    /** Code saved in the reminder log (D0, D1, D2, D3 or OD). Stages may override it. */
    public String emailCode(long days) { return "D" + days; }

    /** The stage of a task that is `days` days away, or null when it is more than 3 days away. */
    public static DueStage of(long days) {
        for (DueStage s : values()) {
            if (s.matches(days)) return s;
        }
        return null;
    }
}
