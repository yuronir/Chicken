package com.nesswit.galbijjimsearcher;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Management Device data.
 * @author Nesswit
 */
public class DeviceData {
	public static int deviceWidthDpi = 0;
	public static int deviceHeightDpi = 0;
	public static int deviceWidthPx = 0;
	public static int deviceHeightPx = 0;
	
	/**
	 * Initialization width and height of device screen
	 * @param context Context
	 */
	public static void initDeviceScreenSize(Context context) {
		DisplayMetrics metrics = new DisplayMetrics(); 
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		
		setDeviceWidthPx(metrics.widthPixels); 
		setDeviceHeightPx(metrics.heightPixels); 
		
		//px to dp
		setDeviceWidthDpi(pxToDp(context, getDeviceWidthPx()));
		setDeviceHeightDpi(pxToDp(context, getDeviceHeightPx()));
		return;
	}
	
	/**
	 * Convert pixel to dp
	 * @param context Context
	 * @param px Pixel size to convert 
	 * @return Converted dp size
	 */
	public static int pxToDp(Context context, int px) {
		DisplayMetrics metrics = new DisplayMetrics(); 
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		return (int)((double)px * (metrics.densityDpi / 160f));
	}
	
	/**
	 * Convert Dp to pixel
	 * @param context Context
	 * @param dp Dp size to convert
	 * @return Converted pixel size
	 */
	public static int dpToPx(Context context, int dp) {
		DisplayMetrics metrics = new DisplayMetrics(); 
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		return (int)((double)dp / (metrics.densityDpi / 160f));
	}
	
	
	/*
	 * Getter and setter
	 */
	public static int getDeviceWidthDpi() {
		return deviceWidthDpi;
	}
	public static void setDeviceWidthDpi(int deviceWidthDpi) {
		DeviceData.deviceWidthDpi = deviceWidthDpi;
	}
	public static int getDeviceHeightDpi() {
		return deviceHeightDpi;
	}
	public static void setDeviceHeightDpi(int deviceHeightDpi) {
		DeviceData.deviceHeightDpi = deviceHeightDpi;
	}
	public static int getDeviceWidthPx() {
		return deviceWidthPx;
	}
	public static void setDeviceWidthPx(int deviceWidthPx) {
		DeviceData.deviceWidthPx = deviceWidthPx;
	}
	public static int getDeviceHeightPx() {
		return deviceHeightPx;
	}
	public static void setDeviceHeightPx(int deviceHeightPx) {
		DeviceData.deviceHeightPx = deviceHeightPx;
	}
}
