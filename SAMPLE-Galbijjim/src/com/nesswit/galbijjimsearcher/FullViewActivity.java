package com.nesswit.galbijjimsearcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.nesswit.galbijjimsearcher.MainActivity.GridFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Activity when clicked grid image.
 * @author nesswit
 *
 */
public class FullViewActivity extends SherlockFragmentActivity{
	private ViewPager mViewPager;
	public static FullViewPagerAdapter mAdapter;
	public static ActionBar mActionBar;
	public static Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullview);
		
		mContext = this;
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.hide();

		mAdapter = new FullViewPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager)findViewById(R.id.full_viewpager);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(getIntent().getIntExtra("index", 0));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override public void onPageScrollStateChanged(int arg0) {}
			@Override
			public void onPageSelected(int arg0) {
				// Load more results when reachd end of viewpager.
				if (arg0 > mAdapter.getCount()-3 && MainActivity.isExistMore && !MainActivity.isLoading) {
					((GridFragment)MainActivity.mFragmentManager.findFragmentById(R.id.grid_fragment)).LoadMore();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// hide actionbar when scrolled.
				if (mActionBar.isShowing()) {
					mActionBar.hide();
				}
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch(keyCode) {
			case KeyEvent.KEYCODE_MENU:
				if (!mActionBar.isShowing()) {
					mActionBar.show();
					return true;
				} else {
					return false;  
				}
			case KeyEvent.KEYCODE_BACK:
				finish();
				return true;
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.fullview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_fullview_share_image:
			try {
				Intent intent = GlobalFunctions.getShareImageIntent(this, GridViewAdapter.images.get(mViewPager.getCurrentItem()).getUrl());
				startActivity(intent);
			} catch (IOException e) {
				Toast.makeText(this, getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.menu_fullview_share_link:
			Intent msg = new Intent(Intent.ACTION_SEND);
			msg.putExtra(Intent.EXTRA_TEXT, GridViewAdapter.images.get(mViewPager.getCurrentItem()).getFrom());
			msg.setType("text/plain");
			startActivity(msg);
			break;
		case R.id.menu_fullview_open_link:
			Uri uri = Uri.parse(GridViewAdapter.images.get(mViewPager.getCurrentItem()).getFrom());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * FragmentStatePagerAdapter for FullViewActivity
	 * @author nesswit
	 *
	 */
	class FullViewPagerAdapter extends FragmentStatePagerAdapter {
		// map for cleanup photoview
		@SuppressLint("UseSparseArrays")
		private Map<Integer, FullViewFragment> mPageReferenceMap = new HashMap<Integer, FullViewFragment>();

		public FullViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return GridViewAdapter.images.size();
		}

		@Override
		public Fragment getItem(int arg0) {
			FullViewFragment fragment = FullViewFragment.newInstance(GridViewAdapter.images.get(arg0));
			mPageReferenceMap.put(arg0, fragment);
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			if (mPageReferenceMap.get(position).mAttacher != null) mPageReferenceMap.get(position).mAttacher.cleanup();
			mPageReferenceMap.remove(position);
		}

	}


	/**
	 * Fragment to show full-screen image.
	 * @author nesswit
	 *
	 */
	public static class FullViewFragment extends SherlockFragment {
		private View mView;
		private final ImageLoader imageLoader = ImageLoader.getInstance();
		private final DisplayImageOptions options = new DisplayImageOptions.Builder()
				.imageScaleType(ImageScaleType.EXACTLY)
				.build();

		public PhotoViewAttacher mAttacher;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		public static FullViewFragment newInstance(Image image) {
			FullViewFragment f = new FullViewFragment();
			f.setArguments(image.toBundle());
			return f;
		}
		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			mView = inflater.inflate(R.layout.fragment_fullview, container, false);
			

			final ImageView imageView = (ImageView)mView.findViewById(R.id.full_view);
			imageLoader.displayImage(getArguments().getString("url"), imageView, options, initListeners());

			return mView;
		}
		
		/**
		 * Initialization for image loader
		 * @return listener for image loader
		 */
		private ImageLoadingListener initListeners() {
			final OnLongClickListener longClickListener = new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (mActionBar.isShowing()) {
						mActionBar.hide();
					} else {
						mActionBar.show();
					}
					return true;
				}
			};
			
			final ImageLoadingListener imageListener = new ImageLoadingListener() {
				@Override public void onLoadingStarted(String imageUri, View view) {
					/*
					 * Hide fail image,
					 * show progress,
					 * disable click,
					 * enable longclick for actionbar toggle. 
					 */
					((ImageView)view).setImageResource(0);
					((ImageView)view).setScaleType(ScaleType.FIT_CENTER);
					mView.findViewById(R.id.full_progress).setVisibility(View.VISIBLE);
					view.setOnClickListener(null);
					view.setOnLongClickListener(longClickListener);
				}
				@Override public void onLoadingFailed(final String imageUri, final View view, FailReason failReason) {
					/*
					 * Show fail image,
					 * hide progress,
					 * enable click for reload,
					 * enable longclick for actionbar toggle(not changed). 
					 */
					final ImageLoadingListener thisImageListener = this;
					mView.findViewById(R.id.full_progress).setVisibility(View.GONE);
					((ImageView)view).setImageResource(R.drawable.emo_im_undecided_error_white);
					((ImageView)view).setScaleType(ScaleType.CENTER);
					view.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							imageLoader.displayImage(imageUri, (ImageView)view, options, thisImageListener);
							if (mActionBar.isShowing()) {
								mActionBar.hide();
							}
						}
					});
				}
				@Override public void onLoadingCancelled(String imageUri, View view) {}
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					/*
					 * hide fail image(not changed),
					 * hide progress,
					 * enable click for actionbar toggle,
					 * enable longclick for actionbar toggle(not changed).
					 * 
					 * show the image.
					 */
					mAttacher = new PhotoViewAttacher((ImageView)view);
					view.setOnLongClickListener(null);
					mAttacher.setOnLongClickListener(longClickListener);
					mAttacher.setOnViewTapListener(new OnViewTapListener() {
						@Override
						public void onViewTap(View view, float x, float y) {
							if (mActionBar.isShowing()) {
								mActionBar.hide();
							} else {
								mActionBar.show();
							}
						}
					});
					mView.findViewById(R.id.full_progress).setVisibility(View.GONE);
				}
			};
			return imageListener;
		}
	}
}
