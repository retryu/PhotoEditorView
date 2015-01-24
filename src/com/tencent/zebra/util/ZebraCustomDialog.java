package com.tencent.zebra.util;

import com.tencent.photoplus.R;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

public class ZebraCustomDialog {
    
    public static Dialog newCustomDialog(Context context, String title, String content, 
            String posBtnText, View.OnClickListener posBtnListener,
            String negBtnText, View.OnClickListener negBtnListener) {
        final Dialog dialog = newCustomDialog(context, title, content);
        dialog.setCancelable(true);
        if (!TextUtils.isEmpty(posBtnText)) {
            TextView posBtn = (TextView) dialog.findViewById(R.id.dialogRightBtn);
            if (posBtn != null) {
                posBtn.setText(posBtnText);
                if (posBtnListener != null) {
                    posBtn.setOnClickListener(posBtnListener);
                } else {
                    posBtn.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            try {
                                dialog.cancel();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
        if (!TextUtils.isEmpty(negBtnText)) {
            TextView negBtn = (TextView) dialog.findViewById(R.id.dialogLeftBtn);
            if (negBtn != null) {
                negBtn.setText(negBtnText);
                if (negBtnListener != null) {
                    negBtn.setOnClickListener(negBtnListener);
                } else {
                    negBtn.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            try {
                                dialog.cancel();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
        return dialog;
    }

    public static Dialog newCustomDialog(Context context, String title, String content) {
        Dialog dialog = new Dialog(context, R.style.qZoneInputDialog);
        dialog.setContentView(R.layout.qcamer_zebra_custom_dialog);
        TextView aTitle = (TextView) dialog.findViewById(R.id.dialogTitle);
        if (aTitle != null)
            aTitle.setText(title);
        TextView aContent = (TextView) dialog.findViewById(R.id.dialogText);
        if (aContent != null)
            aContent.setText(content);
        TextView leftBtn = (TextView) dialog.findViewById(R.id.dialogLeftBtn);
        if (leftBtn != null)
            leftBtn.setText(android.R.string.ok);
        TextView rightBtn = (TextView) dialog.findViewById(R.id.dialogRightBtn);
        if (rightBtn != null)
            rightBtn.setText(android.R.string.cancel);

        return dialog;
    }
}
