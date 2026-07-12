package com.hfr.util;

import com.hfr.config.XFConfig;
import com.hfr.main.MainRegistry;

/**
 * Logging helpers for messages that are useful while diagnosing a server but too noisy for normal runtime.
 */
public final class XFLog {

	private XFLog() { }

	public static boolean isDebugEnabled() {
		return XFConfig.enableDebugLogging;
	}

	public static void debug(String message) {
		if(!isDebugEnabled()) return;
		if(MainRegistry.logger != null) MainRegistry.logger.info(message);
		else System.out.println(message);
	}

	public static void info(String message) {
		if(MainRegistry.logger != null) MainRegistry.logger.info(message);
		else System.out.println(message);
	}

	public static void warn(String message) {
		if(MainRegistry.logger != null) MainRegistry.logger.warn(message);
		else System.out.println("WARN: " + message);
	}

	public static void error(String message, Throwable t) {
		if(MainRegistry.logger != null) MainRegistry.logger.error(message, t);
		else {
			System.out.println("ERROR: " + message);
			if(t != null) t.printStackTrace();
		}
	}
}
