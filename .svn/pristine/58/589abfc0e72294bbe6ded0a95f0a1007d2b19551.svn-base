
package com.tencent.zebra.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.photoplus.R;
import com.tencent.zebra.util.log.ZebraLog;

/**
 * 弱提示Toast,自定义样式(手Q结合版与手Q样式相同)
 * FIXME，LouisPeng，这个Toast在插件环境下是不可用的，暂时使用系统Toast
 * 
 * @author terencewu
 */
public class ZebraToast {
    public static final String TAG = "ZebraToast";
    public static final int ICON_NONE = -1; // 无图标
    public static final int ICON_DEFAULT = 0; // 默认图标,蓝色
    public static final int ICON_ERROR = 1; // 错误图标
    public static final int ICON_SUCCESS = 2; // 成功图标

    private Context mContext;
    private Resources mResources;
    private LayoutInflater mInflater;

    private CharSequence message = null;
    private int mDuration = Toast.LENGTH_SHORT;

    private ZebraToast(Context context) {
        mContext = context;
        mResources = context.getResources();
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 设置Toast显示的文字.
     * 
     * @param msg
     */
    private void setToastMsg(CharSequence msg) {
        message = msg;
    }

    /**
     * 设置Toast显示的文字.
     * 
     * @param msg
     */
    private void setToastMsg(int msgResId) {
        setToastMsg(mResources.getString(msgResId));
    }

    /**
     * 设置显示时间 Toast.LENGTH_LONG和Toast.LENGTH_SHORT
     * 
     * @param dura
     */
    public void setDuration(int dura) {
        mDuration = dura;
    }

    /**
     * 创建Toast对象
     */
    private Toast create(int yOffset) {
        ViewGroup view = null;
        
        try {
            view = (ViewGroup) mInflater.inflate(R.layout.qcamer_zebra_padqq_toast_base, null);
        } catch (InflateException e) {
            ZebraLog.e(TAG, "create", e);
        }
        
        if (view != null && message != null) {
            // 插件不能用findViewById
            LinearLayout layout = new LinearLayout(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            // android:layout_width="match_parent"
            // android:layout_height="wrap_content"
            // android:minHeight="42dp"
            // android:layout_gravity="top"
            // android:layout_marginLeft="10dp"
            // android:layout_marginRight="10dp"
            // android:background="@drawable/qcamer_zebra_tips_popup_win_bg"
            // android:gravity="center"
            // android:orientation="horizontal"
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layout.setMinimumHeight(dip2px(mContext, 42));
            layoutParams.gravity = Gravity.TOP;
            layoutParams.leftMargin = dip2px(mContext, 10);
            layoutParams.rightMargin = dip2px(mContext, 10);
            // 这里会有问题
            layout.setBackgroundResource(R.drawable.qcamer_zebra_tips_popup_win_bg);
            layout.setGravity(Gravity.CENTER);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(layoutParams);

            // android:layout_width="wrap_content"
            // android:layout_height="wrap_content"
            // android:ellipsize="end"
            // android:gravity="center"
            // android:minHeight="10dp"
            // android:paddingLeft="10dp"
            // android:paddingTop="13dp"
            // android:paddingRight="10dp"
            // android:paddingBottom="13dp"
            // android:textColor="#ffffff"
            TextView textView = new TextView(mContext);
            ViewGroup.LayoutParams paramsTextView = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsTextView.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            paramsTextView.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textView.setGravity(Gravity.CENTER);
            textView.setMinHeight(dip2px(mContext, 10));
            textView.setPadding(dip2px(mContext, 10), dip2px(mContext, 13), dip2px(mContext, 10), dip2px(mContext, 13));
            textView.setTextColor(Color.WHITE);
            textView.setLayoutParams(paramsTextView);
            textView.setText(message);

            layout.addView(textView);
            view.addView(layout);

            Toast toast = new Toast(mContext.getApplicationContext());
            toast.setGravity(Gravity.CENTER, 0, yOffset);
            toast.setView(view);
            toast.setDuration(mDuration);
            return toast;
        } else {
            ZebraLog.e(TAG, "mInflater.inflate(R.layout.qcamer_zebra_padqq_toast_base, null) is null");
        }

        return null;

    }

    /**
     * 显示这个PadQQToast 默认显示在系统tilte顶部，可调用此方法
     */
    private void show() {
        Toast toast = create(0);
        if (null != toast) {
            toast.show();
        }
    }

    /**
     * 显示Toast
     * 
     * @param yOffset y轴偏移量 如果现在IphoneTitleBar底部，获取titleBar设置yOffset
     */
    private void show(int yOffset) {
        Toast toast = create(yOffset);
        if (null != toast) {
            toast.show();
        }
    }

	/**
	 * 配置一个PadQQToast
	 * 
	 * @param context
	 * @param iconType
	 *            文字左边的图标类型,可能的值为ICON_DEFAULT,ICON_ERROR,ICON_SUCCESS
	 * @param msg
	 * @param duration
	 * @return
	 */
    private static ZebraToast makeText(Context context, int iconType, CharSequence msg, int duration) {
		ZebraToast qqToast = new ZebraToast(context);
		qqToast.setToastMsg(msg);
		qqToast.setDuration(duration);
		return qqToast;
	}

	/**
	 * 配置一个PadQQToast
	 * 
	 * @param context
	 * @param iconType
	 *            文字左边的图标类型,可能的值为ICON_DEFAULT,ICON_ERROR,ICON_SUCCESS
	 * @param msgResId
	 * @param duration
	 * @return
	 */
	private static ZebraToast makeText(Context context, int iconType, int msgResId, int duration) {
		ZebraToast qqToast = new ZebraToast(context);
		qqToast.setToastMsg(msgResId);
		qqToast.setDuration(duration);
		return qqToast;
	}

	/**
	 * 配置一个PadQQToast(默认图标)
	 * 
	 * @param context
	 * @param msg
	 * @param duration
	 * @return
	 */
	private static ZebraToast makeText(Context context, CharSequence msg, int duration) {
		return makeText(context, ICON_DEFAULT, msg, duration);
	}

	/**
	 * 配置一个PadQQToast(默认图标)
	 * 
	 * @param context
	 * @param msgResId
	 * @param duration
	 * @return
	 */
	private static ZebraToast makeText(Context context, int msgResId, int duration) {
		return makeText(context, ICON_DEFAULT, msgResId, duration);
	}
    
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
	private static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
	private static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
}
