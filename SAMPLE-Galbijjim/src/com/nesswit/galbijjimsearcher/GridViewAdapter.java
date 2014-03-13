package com.nesswit.galbijjimsearcher;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Adapter for gridview to show search results.
 * @author nesswit
 *
 */
public class GridViewAdapter extends BaseAdapter{
	private Context mContext;
	public static ArrayList<Image> images;
	
	private final ImageLoader imageLoader = ImageLoader.getInstance();
	private final DisplayImageOptions options = new DisplayImageOptions.Builder()
			.resetViewBeforeLoading()
			.cacheOnDisc()
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();

	public GridViewAdapter(Context context, ArrayList<Image> images) {
		if (images == null) images = new ArrayList<Image>();
		mContext = context;
		GridViewAdapter.images = images;
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public Image getItem(int arg0) {
		return images.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int positionForListen = position;
		
		// view-holder pattern
		Holder holder;
		if (convertView == null) {
			holder = new Holder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gridimage, parent, false);
			holder.imageView = (ImageView)convertView.findViewById(R.id.grid_image);
			convertView.setTag(holder);
		} else {
			holder = (Holder)convertView.getTag();
		}
		
		
		if (holder.url == null || !holder.url.equals(getItem(position).getUrl())) {
			holder.url = getItem(position).getUrl();
			
			//Start Activity for showing full-sized image when image clicked.
			final OnClickListener clickListner = new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(mContext, FullViewActivity.class);
					i.putExtra("index", positionForListen);
					mContext.startActivity(i);
				}
			};
			
			final ImageLoadingListener imageListener = new ImageLoadingListener() {
				@Override public void onLoadingCancelled(String imageUri, View view) {}
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					/*
					 * unregister context menu,
					 * show progress,
					 * change background color to grey,
					 * disable click,
					 * remove fail image.
					 */
					if (((View)view.getParent()).findViewById(R.id.grid_image_progress).getVisibility() == View.INVISIBLE) {
						view.setBackgroundColor(mContext.getResources().getColor(R.color.grey));
					}
					((ImageView)view).setImageResource(0);
					((ImageView)view).setScaleType(ScaleType.CENTER_CROP);
					view.setOnClickListener(null);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					/*
					 * register context menu for share,
					 * hide progress,
					 * change background color to grey(not changed),
					 * enable click for full-sized image view,
					 * remove fail image(not change).
					 */
					((Activity)mContext).registerForContextMenu(view);
					((View)view.getParent()).findViewById(R.id.grid_image_progress).setVisibility(View.INVISIBLE);
					view.setOnClickListener(clickListner);
				}

				@Override
				public void onLoadingFailed(final String imageUri, View view, FailReason failReason) {
					/*
					 * unregister context menu for share,
					 * hide progress,
					 * change background color to transparent,
					 * enable click for reload the image,
					 * change fail image.
					 */
					((Activity)mContext).unregisterForContextMenu(view);
					final ImageLoadingListener listener = this;
					((View)view.getParent()).findViewById(R.id.grid_image_progress).setVisibility(View.INVISIBLE);
					view.setBackgroundColor(mContext.getResources().getColor(R.color.md__transparent));
					view.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							((View)v.getParent()).findViewById(R.id.grid_image_progress).setVisibility(View.VISIBLE);
							imageLoader.displayImage(imageUri, ((ImageView)v), options, listener);
							((ImageView)v).setTag(getItem(positionForListen));
						}
					});
					((ImageView)view).setImageResource(R.drawable.emo_im_undecided_error);
					((ImageView)view).setScaleType(ScaleType.CENTER);
				}
			};
			imageLoader.displayImage(getItem(position).getUrl(), holder.imageView, options, imageListener);
		}

		return convertView;
	}
	
	public class Holder {
		ImageView imageView;
		String url;
	}
	
	/**
	 * add images
	 * @param images images to add
	 */
	public void addImages (ArrayList<Image> images) {
		GridViewAdapter.images.addAll(images);
		if (FullViewActivity.mAdapter != null) FullViewActivity.mAdapter.notifyDataSetChanged();
		notifyDataSetChanged();
	}
	
	/**
	 * set images
	 * @param images images to set
	 */
	public void setImages (ArrayList<Image> images) {
		GridViewAdapter.images = images;
		if (FullViewActivity.mAdapter != null) FullViewActivity.mAdapter.notifyDataSetChanged();
		notifyDataSetChanged();
	}
	
}
