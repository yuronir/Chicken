package com.nesswit.galbijjimsearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.nesswit.galbijjimsearcher.MenuAdapter.MenuListener;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Main Activity!
 * @author nesswit
 *
 */
public class MainActivity extends SherlockFragmentActivity {
	private MenuDrawer mDrawer;
	private static ActionBar mActionBar;
	public static FragmentManager mFragmentManager;
	
	public static boolean isLoading = false;
	public static boolean isExistMore = true;
	
	private static int page = 1;
	private static Image selectedImage;
	
	private static String textToSearch = "갈비찜";
	
	private static RelativeLayout progressLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Init Device Data
		DeviceData.initDeviceScreenSize(this);
		mDrawer = MenuDrawer.attach(this);
		mDrawer.setContentView(R.layout.activity_main);
		mActionBar = getSupportActionBar();
		mFragmentManager = getSupportFragmentManager();
		
		progressLayout = (RelativeLayout) findViewById(R.id.main_progress);
		
		initImageLoader();
		initDrawer();
        versionCheck();
        
        //search when start app first time.
        ((GridFragment)getSupportFragmentManager().findFragmentById(R.id.grid_fragment)).Search();
	}
	
	private void initImageLoader() {
		// Create global configuration and initialize ImageLoader with this configuration
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.threadPoolSize(5)
				.memoryCache(new WeakMemoryCache())
				.memoryCacheSize(2 * 1024 * 1024)
				.discCacheSize(50 * 1024 * 1024)
				//.enableLogging()
				.build();
		ImageLoader.getInstance().init(config);
		
		//Clear temporary folder. 
		GlobalFunctions.DeleteDir(StorageUtils.getCacheDirectory(this).getPath() + "/temp");
	}

	private void initDrawer() {
		List<Object> items = new ArrayList<Object>();
		items.add(new Category(getResources().getString(R.string.menu_category_search_for)));
		items.add(new Item(getResources().getString(R.string.menu_item_search_for_1), android.R.drawable.ic_menu_directions));
		items.add(new Item(getResources().getString(R.string.menu_item_search_for_2), android.R.drawable.ic_menu_directions));
		items.add(new Item(getResources().getString(R.string.menu_item_search_for_3), android.R.drawable.ic_menu_directions));
		items.add(new Item(getResources().getString(R.string.menu_item_search_for_4), android.R.drawable.ic_menu_directions));
		items.add(new Item(getResources().getString(R.string.menu_item_search_for_5), android.R.drawable.ic_menu_directions));

        ListView mList = new ListView(this);
        MenuAdapter mAdapter = new MenuAdapter(this, items);
        mAdapter.setListener(new MenuListener() {@Override public void onActiveViewChanged(View v) {}});
        mList.setAdapter(mAdapter);

        mList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				MenuAdapter adapter = (MenuAdapter) arg0.getAdapter();
				if (adapter.getItem(arg2).getClass() == Item.class) {
					if (!textToSearch.equals(((Item)adapter.getItem(arg2)).mTitle)) {
						textToSearch = ((Item)adapter.getItem(arg2)).mTitle;
						((GridFragment)getSupportFragmentManager().findFragmentById(R.id.grid_fragment)).Search();
					}
					mDrawer.closeMenu();
				}
			}
		});
        mDrawer.setMenuView(mList);
        
        int LimitWidth = DeviceData.getDeviceWidthPx()*5/6;
        mDrawer.setMenuSize(Math.min(LimitWidth, DeviceData.pxToDp(this, 300)));

        // The drawable that replaces the up indicator in the action bar
        mDrawer.setSlideDrawable(R.drawable.ic_drawer);
        // Whether the previous drawable should be shown
        mDrawer.setDrawerIndicatorEnabled(true);
	}

	private void versionCheck() {
		SharedPreferences pref = getSharedPreferences("VER", 0);

		try{
			PackageManager pm = this.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
			int VERSION = packageInfo.versionCode;
			int old_Ver = pref.getInt("version", 0);

			if(old_Ver<VERSION){
				ImageLoader.getInstance().clearDiscCache();
			}
		}
		catch(Exception e){}
	}

	@Override
	public void onBackPressed() {
		if (mDrawer.isMenuVisible()) {
			mDrawer.closeMenu();
			return;
		}
		super.onBackPressed();
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(getResources().getString(R.string.context_image_share));
        menu.add(getResources().getString(R.string.context_link_share));
        menu.add(getResources().getString(R.string.context_link_open));
        selectedImage = (Image)v.getTag();
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
    	if (item.toString().equals(getResources().getString(R.string.context_image_share))) {
    		try {
    			Intent intent = GlobalFunctions.getShareImageIntent(this, selectedImage.getUrl());
				startActivity(intent);
			} catch (IOException e) {
				Toast.makeText(this, getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
			}
    	} else if (item.toString().equals(getResources().getString(R.string.context_link_share))) {
    		Intent msg = new Intent(Intent.ACTION_SEND);
    		msg.putExtra(Intent.EXTRA_TEXT, selectedImage.getFrom());
    		msg.setType("text/plain");
    		startActivity(msg);
    	} else if (item.toString().equals(getResources().getString(R.string.context_link_open))) {
    		Uri uri = Uri.parse(selectedImage.getFrom());
    	    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	    startActivity(intent);
    	}
        return true;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final MenuItem searchMenuItem = menu.findItem(R.id.menu_main_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if (null != searchView ) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()  {
            public boolean onQueryTextChange(String newText)  {
                return true;
            }

            public boolean onQueryTextSubmit(String query)  {
            	query = query.trim();
            	if (query.length() > 0) {
            		textToSearch = query;
            		((GridFragment)getSupportFragmentManager().findFragmentById(R.id.grid_fragment)).Search();
            		mDrawer.closeMenu();
            		searchMenuItem.collapseActionView();
            		searchView.setQuery("", false);
            	}
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View arg0, boolean queryTextFocused) {
				if(!queryTextFocused) {
					searchMenuItem.collapseActionView();
	                searchView.setQuery("", false);
	            }
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
            mDrawer.toggleMenu();
            break;
		case R.id.menu_main_refresh:
			mDrawer.closeMenu();
			Toast.makeText(this, getResources().getString(R.string.reload), Toast.LENGTH_SHORT).show();
			((GridFragment)getSupportFragmentManager().findFragmentById(R.id.grid_fragment)).Search();
			break;
		case R.id.menu_main_search:
			mDrawer.closeMenu();
			break;
		default:
			return false;
		}
		return true;
	}
	
	private final static HttpClient client = new DefaultHttpClient();
	public static String httpRequestPost(final String url, final ArrayList<BasicNameValuePair> params) throws Exception {
		final HttpPost post = new HttpPost(url);
		final UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
		post.setEntity(ent);
		final HttpResponse responsePost = client.execute(post);
		final HttpEntity resEntity = responsePost.getEntity();
		return EntityUtils.toString(resEntity);
	}
	
	public static class GridFragment extends SherlockFragment {
		private GridView gridView;
		private GridViewAdapter adapter;
		private Context mContext;
		private View mView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
        
        public void LoadMore() {
        	new LoadMoreTask().execute(textToSearch);
        }
        
        public void Search() {
        	new SearchTask().execute(textToSearch);
        }

        /**
         * Create the view for this fragment, using the arguments given to it.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	mView = inflater.inflate(R.layout.gridview, container, false);
            mContext = mView.getContext();
            gridView = (GridView) mView.findViewById(R.id.gridview);
            final int numColumns = DeviceData.getDeviceWidthDpi()/120;
            adapter = new GridViewAdapter(mContext, new ArrayList<Image>());
    		gridView.setAdapter(adapter);
    		gridView.setOnScrollListener(new OnScrollListener() {
    			@Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
    			@Override
    			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    				int endCount = firstVisibleItem + visibleItemCount-1;
    				if (totalItemCount > 0 && isExistMore && !isLoading && endCount > totalItemCount - numColumns*2) {
    					LoadMore();
    				}
    				
    			}
    		});
    		
            return mView;
        }
        
        private class SearchTask extends AsyncTask<String, Void, ArrayList<Image>> {
    		@Override
    		protected ArrayList<Image> doInBackground(String... params) {
    			String stringToSearch = params[0].replaceAll(" ", "+");
    			String url2 = "http://srv.kwzf.net/image/index.php?___mvwizDL___=part.result_image.php%3C:[input]:%3Eresult_image&mvwizFramework_page_root=index.app/kwzf&mvwizJSTimeStamp=" + ((Long)(System.currentTimeMillis()/1000)).toString() + "&search=" + stringToSearch;
    			ArrayList<BasicNameValuePair> urlParams = new ArrayList<BasicNameValuePair>();
    			urlParams.add(new BasicNameValuePair("search", stringToSearch));
    			
    			ArrayList<Image> images = new ArrayList<Image>();
    			try {
    				String html = httpRequestPost(url2, urlParams);
    				images = HTMLParser.parseHtml(html);
    				if (images.size() == 0) {
    					isExistMore = false;
    				} else {
    					isExistMore = true;
    				}
    			} catch (Exception e) {
    				publishProgress();
    			}
    			return images;
    		}
    		
    		@Override
    		protected void onProgressUpdate(Void... values) {
    			Toast.makeText(mContext, getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
    			super.onProgressUpdate(values);
    		}
    		
    		@Override
    		protected void onPreExecute() {
    			String title;
    			if (textToSearch.length() > 10) {
    				title = textToSearch.substring(0, 10) + "...";
    			} else {
    				title = textToSearch;
    			}
    			mActionBar.setTitle(title + " searcher");
    			mView.setVisibility(View.INVISIBLE);
    			progressLayout.setVisibility(View.VISIBLE);
    			isLoading = true;
    			page = 1;
    			adapter.setImages(new ArrayList<Image>());
    			super.onPreExecute();
    		}
    		
    		@Override
    		protected void onPostExecute(ArrayList<Image> result) {
    			adapter.setImages(result);
    			isLoading = false;
    			mView.setVisibility(View.VISIBLE);
    			progressLayout.setVisibility(View.INVISIBLE);
    			super.onPostExecute(result);
    		}
    		
    		
    	}
    	
    	private class LoadMoreTask extends AsyncTask<String, Void, ArrayList<Image>> {
    		@Override
    		protected ArrayList<Image> doInBackground(String... params) {
    			String stringToSearch = params[0].replaceAll(" ", "+");
    			String url2 = "http://srv.kwzf.net/image/index.php?___mvwizDL___=part.result_image.php%3Fsearch%3D" + stringToSearch + "%26page%3D" + page + "%3C:[input]:%3Eresult_image&mvwizFramework_page_root=index.app/kwzf&mvwizJSTimeStamp=" + ((Long)(System.currentTimeMillis()/1000)).toString() + "&false=";
    			ArrayList<BasicNameValuePair> urlParams = new ArrayList<BasicNameValuePair>();
    			urlParams.add(new BasicNameValuePair("false", ""));
    			
    			ArrayList<Image> images = new ArrayList<Image>();
    			try {
    				String html = httpRequestPost(url2, urlParams);
    				images = HTMLParser.parseHtml(html);
    				if (images.size() == 0) {
    					isExistMore = false;
    				} else {
    					isExistMore = true;
    				}
    			} catch (Exception e) {
    				publishProgress();
    			}
    			return images;
    		}
    		
    		@Override
    		protected void onProgressUpdate(Void... values) {
    			Toast.makeText(mContext, getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
    			super.onProgressUpdate(values);
    		}
    		
    		@Override
    		protected void onPreExecute() {
    			isLoading = true;
    			page++;
    			super.onPreExecute();
    		}
    		
    		@Override
    		protected void onPostExecute(ArrayList<Image> result) {
    			adapter.addImages(result);
    			isLoading = false;
    			super.onPostExecute(result);
    		}
    	}
    }

}
