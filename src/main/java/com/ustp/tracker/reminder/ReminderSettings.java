package com.ustp.tracker.reminder;

import java.util.prefs.Preferences;

/** Each student's choice to turn the alarm sound and the reminder emails on or off (both default to ON). */
public class ReminderSettings {
    private final Preferences prefs = Preferences.userNodeForPackage(ReminderSettings.class);

    public boolean isSoundEnabled(int userId) {
        return prefs.getBoolean("sound_" + userId, true);
    }

    public void setSoundEnabled(int userId, boolean on) {
        prefs.putBoolean("sound_" + userId, on);
    }

    public boolean isEmailEnabled(int userId) {
        return prefs.getBoolean("email_" + userId, true);
    }

    public void setEmailEnabled(int userId, boolean on) {
        prefs.putBoolean("email_" + userId, on);
    }
}
