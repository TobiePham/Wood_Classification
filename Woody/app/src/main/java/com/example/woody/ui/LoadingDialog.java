package com.example.woody.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.woody.R;


public class LoadingDialog {
    Context context;
    Dialog dialog;
    public LoadingDialog(Context context){
        this.context=context;
    }
    public void showLoading(){
        dialog= new Dialog(context);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.create();
        dialog.show();
    }
    public void hideLoading(){
        dialog.dismiss();
    }
}
