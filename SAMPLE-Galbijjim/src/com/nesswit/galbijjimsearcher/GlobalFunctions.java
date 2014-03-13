package com.nesswit.galbijjimsearcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Global functions for this app.
 * @author nesswit
 *
 */
public class GlobalFunctions {
	
	/**
	 * Make intent for image share to other apps.
	 * @param context Context
	 * @param imageUrl Url of image to share.
	 * @return Intent to share.
	 * @throws IOException Fail to copy image file.
	 */
	public static Intent getShareImageIntent(Context context, String imageUrl) throws IOException{
		final Intent msg = new Intent(Intent.ACTION_SEND);
		final Uri data = Uri.fromFile(GlobalFunctions.copyFileToTempDir(context, imageUrl));
		msg.putExtra(Intent.EXTRA_STREAM, data);
		msg.addCategory(Intent.CATEGORY_DEFAULT);
		msg.setType(getFileMimetype(context, data));
		return msg;
	}
	
	/**
	 * Copy image file to temporary folder.
	 * @param context Context
	 * @param imageUrl Url of image to copy
	 * @return Copied file 
	 * @throws IOException Fail to copy image file.
	 */
	public static File copyFileToTempDir(Context context, String imageUrl) throws IOException {
		final ImageLoader imageLoader = ImageLoader.getInstance();
		final File cachedFile = DiscCacheUtil.findInCache(imageUrl, imageLoader.getDiscCache());
		final Uri cachedUri = Uri.fromFile(cachedFile);
		
		final String pathDirToMake = StorageUtils.getCacheDirectory(context).getPath() + "/temp";
		File dirToMake = new File(pathDirToMake); 
		if (!dirToMake.exists()) {
			dirToMake.mkdirs();
		} else if (!dirToMake.isDirectory()) {
			dirToMake.delete();
			dirToMake.mkdirs();
		}
		
		File fileToMake = new File(pathDirToMake + "/" + ((Long)(System.currentTimeMillis()/1000)).toString() + "." + getFileExtension(context, cachedUri));

		copyFile(cachedFile, fileToMake);
		
		return fileToMake;
	}
	
	/**
	 * Getting file extension.
	 * @param context Context
	 * @param uri Url to get file extension
	 * @return String of file extension
	 */
	public static String getFileExtension(Context context, Uri uri) {
		ContentResolver cR = context.getContentResolver();
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String fe = mime.getExtensionFromMimeType(cR.getType(uri));
		if (fe == null || fe.equals("null") || fe.equals("")) {
			return "jpeg";
		} else {
			return fe;
		}
	}
	
	/**
	 * Getting file MIME type.
	 * @param context Context
	 * @param uri Url to get file MIME type
	 * @return String of MIME type.
	 */
	public static String getFileMimetype(Context context, Uri uri) {
		ContentResolver cR = context.getContentResolver();
		String fm = cR.getType(uri);
		if (fm == null || fm.equals("null") || fm.equals("")) {
			return "image/jpeg";
		} else {
			return fm;
		}
	}
	
	/**
	 * Copy a file.
	 * @throws IOException Fail to copy.
	 */
	public static void copyFile(File from, File To) throws IOException {
		FileInputStream inputStream = new FileInputStream(from);        
		FileOutputStream outputStream = new FileOutputStream(To);

		FileChannel fcin =  inputStream.getChannel();
		FileChannel fcout = outputStream.getChannel();

		long size = fcin.size();      

		fcin.transferTo(0, size, fcout);
		fcout.close();
		fcin.close();
		outputStream.close();
		inputStream.close();
		return;
	}
	
	/**
	 * Delete folder.
	 * @param path Path of folder to delete.
	 */
	public static void DeleteDir(String path) {
	    File file = new File(path);
	    if (!file.exists()) return;
	    File[] childFileList = file.listFiles();
	    for(File childFile : childFileList)
	    {
	        if(childFile.isDirectory()) {
	            DeleteDir(childFile.getAbsolutePath());
	        }
	        else {
	            childFile.delete();
	        }
	    }     
	    file.delete();
	}
}
