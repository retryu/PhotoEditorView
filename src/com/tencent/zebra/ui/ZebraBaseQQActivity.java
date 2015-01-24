package com.tencent.zebra.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.tencent.mobileqq.pluginsdk.PluginActivity;
import com.tencent.mobileqq.pluginsdk.ResourceIdMapper;
import com.tencent.photoplus.R;

public class ZebraBaseQQActivity extends PluginActivity {
    
    /**
     * 返回真实的Activity，插件化时需要修改
     * @return
     */
    public final Activity getOutActivity() {
      if (this instanceof PluginActivity) {
          // 如果是插件，返回壳Activity
          Activity activity = super.getOutActivity();
          if (activity == null) {
              return this;
          }
          return activity;
      } else {
          // 非插件，返回自身
          return this;
      }
//      return this;
    }

    public final Activity getThisActivity() {
        return this;
    }
    
    public final void startActivity(Class<? extends PluginActivity> destClass, Intent intent) {
        startActivity(intent);
    }
    
    public final void startActivityForResult(Class<? extends PluginActivity> destClass, Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        try {
//            int hostEnterAnim = getHostResId(enterAnim);
//            int hostExitAnim = getHostResId(exitAnim);
//            super.overridePendingTransition(hostEnterAnim, hostExitAnim);
            // 目前不存在activity的动画切换，为了精简资源去掉动画
            super.overridePendingTransition(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
//    public static final int getHostResId(int resId) {
//        try {
//            ResourceIdMapper resourceIdMapper = ResourceIdMapper.sFactory.getInstance("com.tencent.mobileqq.utils.QQResourceIdMapper");
//            int result = 0;
//            if (resId != 0) {
//                result = resourceIdMapper.getHostResourceId(resIdToMapperId(resId));
//            }
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
    
//    private static int resIdToMapperId(int resId) {
//        int result = 0;
//        switch (resId) {
//        case R.anim.zebra_bottom_in:
//            result = ResourceIdMapper.RES_ID_ANIM_BOTTOM_IN;
//            break;
//        case R.anim.zebra_bottom_out:
//            result = ResourceIdMapper.RES_ID_ANIM_BOTTOM_OUT;
//            break;
//        case R.anim.zebra_right_in:
//            result = ResourceIdMapper.RES_ID_ANIM_RIGHT_IN;
//            break;
//        case R.anim.zebra_right_out:
//            result = ResourceIdMapper.RES_ID_ANIM_RIGHT_OUT;
//            break;
//        case R.anim.zebra_left_in:
//            result = ResourceIdMapper.RES_ID_ANIM_LEFT_IN;
//            break;
//        case R.anim.zebra_left_out:
//            result = ResourceIdMapper.RES_ID_ANIM_LEFT_OUT;
//            break;
//        case R.style.zebra_animBottomInAndOut:
//            result = ResourceIdMapper.RES_ID_STYLE_BOTTOM_IN_OUT;
//            break;
//        }
//        return result;
//    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.IOnRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.IOnSaveInstanceState(outState);
    }
    
    @Override
    public boolean IIsWrapContent() {
        // 屏蔽右滑关闭
        return false;
    }
}
