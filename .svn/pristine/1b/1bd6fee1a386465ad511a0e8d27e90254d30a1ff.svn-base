package com.tencent.zebra.util;

import cooperation.zebra.ZebraPluginProxy;

public class ReportUtils {
	
    public static void report(String tag, String mainAction, String toUin, String subAction, String actionName, int fromType, int result, String r2, String r3, String r4, String r5) {
    	try {
    		ZebraPluginProxy.reportController_reportClickEvent(tag, mainAction, toUin, subAction, actionName, fromType, result, r2, r3, r4, r5);
    	} catch (NoSuchMethodError err) {
    		err.printStackTrace();
    	}
    }
}
