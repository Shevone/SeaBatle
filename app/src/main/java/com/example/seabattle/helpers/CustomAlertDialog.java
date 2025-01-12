package com.example.seabattle.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CustomAlertDialog {

    private Context context;
    private AlertDialog dialog;

    public CustomAlertDialog(Context context) {
        this.context = context;
    }

    public void showAlertDialog(String title, String message, String positiveButtonText,
                                final DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (positiveClickListener != null) {
                    positiveClickListener.onClick(dialog, which); // Передаем вызов нашему слушателю
                }
                dialog.dismiss(); // Закрываем диалог после вызова слушателя
            }
        });


        dialog = builder.create();
        dialog.show();
    }
}
