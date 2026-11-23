package com.ustp.tracker.reminder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/** Alarm ringtone. The sound is generated in code, so no audio file is needed. */
public class AlarmSound {
    private static final float RATE = 44100f;
    private byte[] data;
    private Clip clip;

    /** A cheerful rising chime: C - E - G - high C - G - E - C, then a short pause. */
    private byte[] build() {
        double[] notes = {1046.5, 1318.5, 1568.0, 2093.0, 1568.0, 1318.5, 1046.5};
        double noteLen = 0.20, pause = 0.55;
        int per = (int) (RATE * noteLen);
        int total = per * notes.length + (int) (RATE * pause);
        byte[] out = new byte[total * 2];
        for (int n = 0; n < notes.length; n++) {
            for (int i = 0; i < per; i++) {
                double t = i / (double) RATE;
                double env = Math.min(1.0, t / 0.005) * Math.exp(-5.0 * t);   // quick attack, soft decay
                double v = Math.sin(2 * Math.PI * notes[n] * t) * 0.6
                         + Math.sin(2 * Math.PI * notes[n] * 2 * t) * 0.2;
                short s = (short) (v * env * 26000);
                int idx = (n * per + i) * 2;
                out[idx] = (byte) (s & 0xFF);
                out[idx + 1] = (byte) ((s >> 8) & 0xFF);
            }
        }
        return out;
    }

    /** Plays the ringtone once (method overloading: same name, different parameters). */
    public void play() {
        play(1);
    }

    /** Plays the ringtone `times` times (non-blocking). Does nothing if the PC has no sound device. */
    public synchronized void play(int times) {
        stop();
        try {
            if (data == null) data = build();
            clip = AudioSystem.getClip();
            clip.open(new AudioFormat(RATE, 16, 1, true, false), data, 0, data.length);
            clip.loop(Math.max(0, times - 1));
        } catch (Throwable e) {
            System.err.println("Alarm sound unavailable: " + e);
            clip = null;
        }
    }

    public synchronized void stop() {
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Throwable ignored) { }
            clip = null;
        }
    }
}
