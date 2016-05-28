
package com.web2mine.chatpaser.common;

import android.util.Log;

/**
 * Format text log and decide whether logcat is printed out or not
 */
public class DEBUG
{
	private static String getLogTagWithMethod() {
			Throwable stack = new Throwable().fillInStackTrace();
			StackTraceElement[] trace = stack.getStackTrace();
            int idx = 2;
			return trace[idx].getClassName() + "." + trace[idx].getMethodName() + ":" + trace[idx].getLineNumber();
	}

	public static void show(String message)
	{
		if (CONSTANT.IS_LOG_MODE) {
            String tag = getLogTagWithMethod();
            Log.i(tag, message);

        }
	}

	public static void showBreakLine(String message)
	{
		if (CONSTANT.IS_LOG_MODE) {
            String tag = getLogTagWithMethod();
            Log.i(tag, "===============================================");
        }
	}

	public static void error(String message)
	{
		if (CONSTANT.IS_LOG_MODE) {
            String tag = getLogTagWithMethod();
            Log.e(tag, message);
        }
	}
}
