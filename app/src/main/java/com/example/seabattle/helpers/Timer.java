package com.example.seabattle.helpers;

import android.os.Handler;
import java.util.Locale;

public class Timer {

    public interface TimerListener {
        void onTick(String time);
        void onFinished();
    }

    private final long countdownTime;
    private long startTime;
    private final Handler handler;
    private Runnable timerRunnable;
    private TimerListener listener;
    private boolean isRunning = false;

    public Timer() {
        this.countdownTime = 60000;
        handler = new Handler();
    }

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        startTime = System.currentTimeMillis();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millisPassed = System.currentTimeMillis() - startTime;
                long millisLeft = countdownTime - millisPassed;

                if (millisLeft <= 0) {
                    stop();
                    if (listener != null) {
                        listener.onTick("00:00");
                        listener.onFinished();
                    }
                    return;
                }

                String timeString = formatTime(millisLeft);
                if (listener != null) {
                    listener.onTick(timeString);
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(timerRunnable, 0);
    }

    public boolean isRunning() {
        return isRunning;
    }

    private String formatTime(long millisLeft) {
        int seconds = (int) (millisLeft / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }
}
