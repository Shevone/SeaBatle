package com.example.seabattle.services;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationService {

    private final Vibrator vibrator;

    public VibrationService(Context context) {
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Выполняет короткую вибрацию.
     */
    public void vibrateShort() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrate(200); // 200 миллисекунд
        }
    }

    /**
     * Выполняет длинную вибрацию.
     */
    public void vibrateLong() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrate(500); // 500 миллисекунд
        }
    }

    /**
     * Выполняет вибрацию с заданной продолжительностью.
     * @param milliseconds продолжительность вибрации в миллисекундах
     */
    private void vibrate(long milliseconds) {
        if (vibrator == null) return;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(milliseconds);
        }
    }
}