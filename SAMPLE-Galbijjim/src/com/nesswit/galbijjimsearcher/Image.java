package com.nesswit.galbijjimsearcher;

import android.os.Bundle;

/**
 * Image class
 * @author nesswit
 *
 */
public class Image {
	private String url;
	private String from;
	public String debug;

	public Image(String url, String from) {
		setUrl(url);
		setFrom(from);
	}
	
	public Image(Bundle bundle) {
		setUrl(bundle.getString("url"));
		setFrom(bundle.getString("from"));
	}
	
    public Bundle toBundle() {
    	Bundle bundle = new Bundle();
    	
    	bundle.putString("url", url);
    	bundle.putString("from", from);
    	bundle.putString("debug", debug);
    	
    	return bundle;
    }
    
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	
	
}
