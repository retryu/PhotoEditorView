package com.tencent.zebra.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;

import com.tencent.zebra.util.log.ZebraLog;

import cooperation.zebra.ZebraPluginProxy;

public class ZebraBaseActivity extends ZebraBaseQQActivity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	IntentFilter intentFilter = new IntentFilter();
    	intentFilter.addAction("com.tencent.process.exit");
    	
    	try {
    		getOutActivity().getApplicationContext().registerReceiver(
					qqExitBroadcastReceiver, intentFilter);
			ZebraLog.d("MqqCameraActivity", "register qqExitBroadcastReceiver done!");
		} catch (IllegalArgumentException e) {
			ZebraLog.e("MqqCameraActivity", "register qqExitBroadcastReceiver exception!", e);
		}
    }
	
//    public Activity getThisActivity() {
//        return this;
//    }
//    
//    public Activity getOutActivity() {
//        return this;
//    }
//    
//    public void startActivity(Class<? extends Activity> destClass, Intent intent) {
//        startActivity(intent);
//    }
//    
//    public void startActivityForResult(Class<? extends Activity> destClass, Intent intent, int requestCode) {
//        startActivityForResult(intent, requestCode);
//    }
//    
//    public static int getHostResId(int resId) {
//        return resId;
//    }
    
    @Override
    protected void onDestroy() {
        if (null != qqExitBroadcastReceiver) {
            try {
                getOutActivity().getApplicationContext().unregisterReceiver(
                        qqExitBroadcastReceiver);
                ZebraLog.d("MqqCameraActivity", "unregister qqExitBroadcastReceiver done!");
            } catch (IllegalArgumentException e) {
                ZebraLog.e("MqqCameraActivity", "unregister qqExitBroadcastReceiver exception!", e);
            }
        }
        super.onDestroy();
    }

    private BroadcastReceiver qqExitBroadcastReceiver = new BroadcastReceiver() 
    {
   		@Override
   		public void onReceive(Context context, Intent intent) 
   		{
   			if (intent != null) 
   			{
   				String action = intent.getAction();
   				if ("com.tencent.process.exit".equals(action)) 
   				{
   					ZebraLog.d("MqqCameraActivity", "receive qq exit broadcast intent! verifying...");
   	        		ArrayList<String> nameList = intent.getExtras().getStringArrayList("procNameList");
   	        		String verifyValue = intent.getExtras().getString("verify");
   	        		if(isLegalBroadcast(verifyValue, nameList) && isContainsProc(nameList)){
   	        			ZebraLog.d("MqqCameraActivity", "receive qq exit broadcast intent! verify OK! kill process");
   	        			android.os.Process.killProcess(android.os.Process.myPid());
   	        		}
   	        		 
   				}
   			}
   		} 
   	};
   	
   	private String getLocalVerify(ArrayList<String> nameList, boolean isPreMinute){
   		Time time = new Time();
   		time.setToNow();
   		StringBuilder strVerify = new StringBuilder();
   		strVerify.append("com.tencent.process.exit");
   		strVerify.append(time.year).append(time.month + 1).append(time.monthDay);
   		strVerify.append(time.hour);
   		if(isPreMinute){
   			strVerify.append(time.minute-1);
   		}else{
   			strVerify.append(time.minute);
   		}
   		strVerify.append(nameList==null?"null":nameList.toString());
   		
   		String localVerify = ZebraPluginProxy.md5_toMD5(strVerify.toString());
   		localVerify = ZebraPluginProxy.md5_toMD5(localVerify+strVerify.toString());
   		return localVerify;
   	}
   	
   	private boolean isLegalBroadcast(String verifyValue, ArrayList<String> nameList){
   		if(verifyValue == null || verifyValue.length() == 0){
   	   		ZebraLog.d("MqqCameraActivity.isLegalBroadcast", "false");
   			return false;
   		}
   		/*
   		 *  先校验当前时间退出广播请求的合法性;
   		 *  如果没通过，再校验前一分钟时间退出广播请求的合法性(防止QQ在59秒发广播，进程在下一秒收到);
   		 */
   		if(verifyValue.equals(getLocalVerify(nameList, false))
   			|| verifyValue.equals(getLocalVerify(nameList, true))){
   	   		ZebraLog.d("MqqCameraActivity.isLegalBroadcast", "true");
   			return true;
   		}
   		
   		ZebraLog.d("MqqCameraActivity.isLegalBroadcast", "false");
   		return false;
   	}
   	
   	private boolean isContainsProc(ArrayList<String> nameList){
   		if(nameList == null || nameList.size() == 0){
   	   		ZebraLog.d("MqqCameraActivity.isContainsProc", "true");
   			return true;
   		}
   		
   		String myProcName = getCurProcessName(this);
   		for(int i = 0; i < nameList.size(); i ++){
   			if(myProcName.equals( nameList.get(i))){
   				ZebraLog.d("MqqCameraActivity.isContainsProc", "true");
   				return true;
   			}
   		}
   		
   		ZebraLog.d("MqqCameraActivity.isContainsProc", "false");
   		return false;
   	}
   	
	String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procList = mActivityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : procList) {
			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
	}
	
}
