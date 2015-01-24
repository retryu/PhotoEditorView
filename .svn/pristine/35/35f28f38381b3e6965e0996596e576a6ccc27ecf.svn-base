package com.tencent.zebra.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.tencent.photoplus.R;
import com.tencent.zebra.util.log.ZebraLog;

/**
 * 进度对话框，与手Q统一样式
 * @author terencewu
 *
 */
public class ZebraProgressDialog extends ProgressDialog {
    public final static String TAG = "ZebraProgressDialog";

    public ZebraProgressDialog(Context context) {
        super(context);
    }

    public ZebraProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate,
            boolean cancelable) {
        try {
            ProgressDialog pd = ProgressDialog.show(context, title, message, indeterminate, cancelable);
            Window window = pd.getWindow();
            window.setContentView(R.layout.qcamer_zebra_progress_dialog);
            pd.setContentView(R.layout.qcamer_zebra_progress_dialog);
            TextView pdTextView = (TextView) pd.findViewById(R.id.progress_dialog_text);
            pdTextView.setText(message);
            return pd;
        } catch (Throwable e) {
            ZebraLog.e(TAG, "show", e);
            return null;
        }
    }
}
