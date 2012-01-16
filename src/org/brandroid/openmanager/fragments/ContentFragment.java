/*
    Open Explorer, an open source file explorer & text editor
    Copyright (C) 2011 Brandon Bowles <brandroid64@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.brandroid.openmanager.fragments;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.SettingsActivity;
import org.brandroid.openmanager.adapters.IconContextMenu;
import org.brandroid.openmanager.adapters.OpenCursorAdapter;
import org.brandroid.openmanager.adapters.IconContextMenu.IconContextItemSelectedListener;
import org.brandroid.openmanager.adapters.OpenArrayAdapter;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenClipboard;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.fragments.DialogHandler.OnSearchFileSelected;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.IntentManager;
import org.brandroid.openmanager.util.MultiSelectHandler;
import org.brandroid.openmanager.util.EventHandler.OnWorkerThreadFinishedListener;
import org.brandroid.openmanager.util.FileManager.SortType;
import org.brandroid.openmanager.util.ThumbnailStruct;
import org.brandroid.openmanager.util.ThumbnailTask;
import org.brandroid.utils.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.net.Uri;

public class ContentFragment extends OpenFragment implements OnItemClickListener,
															OnWorkerThreadFinishedListener,
															LoaderCallbacks<Cursor>
															{
	
	public static final boolean USE_ACTIONMODE = false;
	private static boolean mMultiSelectOn = false;
	
	private FileManager mFileManager = OpenExplorer.getManager();
	private EventHandler mHandler = OpenExplorer.getHandler();
	private MultiSelectHandler mMultiSelect;
	
	//private LinearLayout mPathView;
	private LinearLayout mMultiSelectView;
	private GridView mGrid = null;
	private View mProgressBarLoading = null;
	
	private int mLayoutID = R.layout.list_content_layout;
	
	private OpenPath mPath = null;
	private static OpenPath mLastPath = null;
	private OpenPath[] mData; 
	private ArrayList<OpenPath> mData2 = null; //the data that is bound to our array adapter.
	private Context mContext;
	private BaseAdapter mContentAdapter;
	private ActionMode mActionMode = null;
	private boolean mActionModeSelected;
	private static boolean mShowThumbnails = true;
	private boolean mReadyToUpdate = true;
	private static boolean mShowHidden = false;
	private int mMenuContextItemIndex = -1;
	private int mListScrollingState = 0;
	private int mListVisibleStartIndex = 0;
	private int mListVisibleLength = 0;
	public static Boolean mShowLongDate = true;
	
	public ContentFragment()
	{
		//Logger.LogDebug("Creating empty ContentFragment", new Exception("Creating empty ContentFragment"));
		mPath = mLastPath;
	}
	public ContentFragment(OpenPath path)
	{
		mPath = mLastPath = path;
	}
	
	//@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(savedInstanceState != null && savedInstanceState.containsKey("last"))
			mPath = mLastPath = new OpenFile(savedInstanceState.getString("last"));
		
		if(mPath == null)
			Logger.LogDebug("Creating empty ContentFragment", new Exception("Creating empty ContentFragment"));
		
		mContext = getActivity().getApplicationContext();

		OpenExplorer.getHandler().setOnWorkerThreadFinishedListener(this);
		
		//refreshData(savedInstanceState);
		if(mData2 == null)
			mData2 = new ArrayList<OpenPath>();
		new Thread(new Runnable() {
			public void run() {
				refreshData(savedInstanceState);
			}
		}).run();
	}
	public void refreshData(Bundle savedInstanceState)
	{
		if(mData2 == null)
			mData2 = new ArrayList<OpenPath>();
		else
			mData2.clear();
		
		OpenPath path = mPath;
		if(path == null) path = mLastPath;
		if(path == null)
		{
			if (savedInstanceState != null && savedInstanceState.containsKey("last"))
				path = new OpenFile(savedInstanceState.getString("last"));
			else
				path = new OpenFile(Environment.getExternalStorageDirectory());
		}
		/*if(!path.requiresThread() && path.getListLength() < 100)
			try {
				mData = mFileManager.getChildren(path);
			} catch (IOException e) {
				Logger.LogError("Error getting children from FileManager for " + path, e);
			}
		else */ {
			if(mContentAdapter != null)
				mContentAdapter.notifyDataSetChanged();
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.VISIBLE);
			new FileIOTask().execute(new FileIOCommand(FileIOCommandType.ALL, path));
		}
		
		mActionModeSelected = false;
		try {
			mShowThumbnails = getExplorer().getPreferences()
								.getSetting(mPath.getPath(), SettingsActivity.PREF_THUMB_KEY, true);
			mShowHidden = getExplorer().getPreferences()
								.getSetting(mPath.getPath(), SettingsActivity.PREF_HIDDEN_KEY, true);
		} catch(NullPointerException npe) {
			mShowThumbnails = true;
		}

		if(path.getClass().equals(OpenCursor.class) && !OpenExplorer.BEFORE_HONEYCOMB)
			mShowThumbnails = true;
		
		if(getActivity() != null && getActivity().getWindow() != null)
			mShowLongDate = getActivity().getWindow().getWindowManager().getDefaultDisplay().getWidth() > 500
					&& mPath != null
					&& OpenFile.class.equals(mPath.getClass());
		
		//OpenExplorer.setOnSettingsChangeListener(this);
		
		updateData(mData);
	}

	//@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.content_layout, container, false);
		if(mProgressBarLoading == null)
			mProgressBarLoading = v.findViewById(R.id.content_progress);
		if(mProgressBarLoading != null)
			mProgressBarLoading.setVisibility(View.GONE);
		//v.setBackgroundResource(R.color.lightgray);
		
		/*
		if (savedInstanceState != null && savedInstanceState.containsKey("location")) {
			String location = savedInstanceState.getString("location");
			if(location != null && !location.equals("") && location.startsWith("/"))
			{
				Logger.LogDebug("Content location restoring to " + location);
				mPath = new OpenFile(location);
				mData = mFileManager.getChildren(mPath);
				updateData(mData);
			}
			//setContentPath(path, false);
		}
		*/

		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		/*
		if(mPath != null && mPath.getPath() != null)
		{
			Logger.LogDebug("Content location saving to " + mPath.getPath());
			outState.putString("location", mPath.getPath());
		}
		*/
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		
		//mPathView = (LinearLayout)v.findViewById(R.id.scroll_path);
		mGrid = (GridView)v.findViewById(R.id.grid_gridview);
		mMultiSelectView = (LinearLayout)v.findViewById(R.id.multiselect_path);
		if(mProgressBarLoading == null)
			mProgressBarLoading = v.findViewById(R.id.content_progress);
		if(mProgressBarLoading != null)
			mProgressBarLoading.setVisibility(View.GONE);

		if(mGrid == null)
			Logger.LogError("WTF, where are they?");
		else
			updateGridView();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		OpenPath path = mData2.get(mMenuContextItemIndex);
		Logger.LogDebug("Showing context for " + path.getName() + "?");
		return executeMenu(item.getItemId(), path);
		//return super.onContextItemSelected(item);
	}
	
	public void updateGridView()
	{
		if(OpenExplorer.getViewMode() == OpenExplorer.VIEW_GRID) {
			mLayoutID = R.layout.grid_content_layout;
			mGrid.setNumColumns(GridView.AUTO_FIT);
			mGrid.setVerticalSpacing(0);
			mGrid.setHorizontalSpacing(0);
		} else {
			mLayoutID = R.layout.list_content_layout;
			mGrid.setNumColumns(1);
			mGrid.setVerticalSpacing(10);
			mGrid.setHorizontalSpacing(10);
		}
		if(OpenCursor.class.equals(mPath.getClass())) {
			mContentAdapter = new OpenCursorAdapter(mContext, (OpenCursor)mPath, 0, (OpenCursor)mPath, mLayoutID);
		} else
			mContentAdapter = new OpenArrayAdapter(mContext, mLayoutID, mData2);
		mGrid.setAdapter(mContentAdapter);
		mGrid.setSelector(R.drawable.selector_blue);
		mGrid.setVisibility(View.VISIBLE);
		mGrid.setOnItemClickListener(this);
		mGrid.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mListScrollingState = scrollState;
				if(scrollState == 0)
					onScrollStopped(view);
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
				if(firstVisibleItem != mListVisibleStartIndex)
					mListVisibleStartIndex = firstVisibleItem;
				if(visibleItemCount != mListVisibleLength)
					mListVisibleLength = visibleItemCount;
			}
		});
		//mGrid.setOnCreateContextMenuListener(this);
		//if(cm == null)
		mGrid.setOnItemLongClickListener(new OnItemLongClickListener() {
			//@Override
			@SuppressWarnings("unused")
			public boolean onItemLongClick(AdapterView<?> list, View view ,int pos, long id) {
				final OpenPath file = mData2.get(pos);
				final String name = file.getPath().substring(file.getPath().lastIndexOf("/")+1);
				if(OpenExplorer.BEFORE_HONEYCOMB || !USE_ACTIONMODE) {
					mMenuContextItemIndex = pos;
					final IconContextMenu cm = new IconContextMenu(getActivity(), R.menu.context_file, view);
					Menu cmm = cm.getMenu();
					if(getClipboard().size() > 0)
						hideItem(cmm, R.id.menu_context_multi);
					else
						hideItem(cmm, R.id.menu_context_paste);
					if(!name.toLowerCase().endsWith(".zip"))
						hideItem(cmm, R.id.menu_context_unzip);
					cm.setTitle(name);
					cm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener() {	
						public void onIconContextItemSelected(MenuItem item, Object info, View view) {
							executeMenu(item.getItemId(), mData2.get((Integer)info));
						}
					});
					cm.setInfo(pos);
					cm.show();
					//return list.showContextMenu();
					return true;
				}
				
				if(!file.isDirectory() && mActionMode == null && !mMultiSelectOn) {
					mActionMode = getActivity().startActionMode(new ActionMode.Callback() {
						//@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							return false;
						}
						
						//@Override
						public void onDestroyActionMode(ActionMode mode) {
							mActionMode = null;
							mActionModeSelected = false;
						}
						
						//@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							mode.getMenuInflater().inflate(R.menu.context_file, menu);
				    		
				    		mActionModeSelected = true;
							return true;
						}

						//@Override
						public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
							//ArrayList<OpenPath> files = new ArrayList<OpenPath>();
							
							//OpenPath file = mLastPath.getChild(mode.getTitle().toString());
							//files.add(file);
							
							if(item.getItemId() != R.id.menu_context_cut && item.getItemId() != R.id.menu_context_multi && item.getItemId() != R.id.menu_context_copy)
							{
								mode.finish();
								mActionModeSelected = false;
							}
							return executeMenu(item.getItemId(), mode, file);
						}
					});
					mActionMode.setTitle(file.getName());
					
					return true;
				}
				
				if(file.isDirectory() && mActionMode == null && !mMultiSelectOn) {
					if(!OpenExplorer.BEFORE_HONEYCOMB && USE_ACTIONMODE)
					mActionMode = getActivity().startActionMode(new ActionMode.Callback() {
						
						//@Override
						public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
							return false;
						}
						
						//@Override
						public void onDestroyActionMode(ActionMode mode) {
							mActionMode = null;
							mActionModeSelected = false;
						}
						
						//@Override
						public boolean onCreateActionMode(ActionMode mode, Menu menu) {
							mode.getMenuInflater().inflate(R.menu.context_file, menu);
							menu.findItem(R.id.menu_context_paste).setEnabled(isHoldingFiles());
							//menu.findItem(R.id.menu_context_unzip).setEnabled(mHoldingZip);
				        	
				        	mActionModeSelected = true;
							
				        	return true;
						}
						
						//@Override
						public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
							return executeMenu(item.getItemId(), mode, file);
						}
					});
					mActionMode.setTitle(file.getName());
					
					return true;
				}
				
				return false;
			}

			private void hideItem(Menu menu, int itemId) {
				if(menu.findItem(itemId) != null)
					menu.findItem(itemId).setVisible(false);
			}
		});
		if(OpenExplorer.BEFORE_HONEYCOMB && USE_ACTIONMODE)
			registerForContextMenu(mGrid);
	}
	
	protected void onScrollStopped(AbsListView view)
	{
		boolean skipThis = true;
		if(skipThis) return;
		int start = Math.max(0, mListVisibleStartIndex);
		int end = Math.min(mData2.size() - 1, mListVisibleStartIndex + mListVisibleLength);
		int mWidth = 128, mHeight = 128;
		ThumbnailStruct[] thumbs = new ThumbnailStruct[end - start];
		for(int i = start; i < end; i++)
		{
			Object o = view.getItemAtPosition(i);
			if(o != null)
			{
				OpenPath file = (OpenPath)o;
				if(file.getTag() != null && file.getTag().getClass().equals(BookmarkHolder.class))
				{
					BookmarkHolder mHolder = (BookmarkHolder)file.getTag();
					ImageView v = mHolder.getIconView();
					thumbs[i - start] = new ThumbnailStruct(file, mHolder, mWidth, mHeight);
					//new ThumbnailTask().execute(new ThumbnailStruct(file, mHolder, mWidth, mHeight));
				}
			}
			//view.getItemAtPosition(i);
		}
		
		new ThumbnailTask().execute(thumbs);
		//Logger.LogDebug("Visible items " + mData2.get(mListVisibleStartIndex).getName() + " - " + mData2.get().getName());
	}
	
	private OpenClipboard getClipboard() {
		return getExplorer().getClipboard();
	}
	private void addHoldingFile(OpenPath path) {
		getExplorer().addHoldingFile(path);
	}
	private void clearHoldingFiles() { getExplorer().clearHoldingFiles(); }
	private boolean isHoldingFiles() { return getClipboard().size() > 0; }
	
	private void finishMode(Object mode)
	{
		if(!OpenExplorer.BEFORE_HONEYCOMB && mode != null && mode instanceof ActionMode)
			((ActionMode)mode).finish();
	}
	
	public EventHandler getEventHandler() { return getExplorer().getEventHandler(); }
	
	public boolean executeMenu(final int id, OpenPath file)
	{
		return executeMenu(id, null, file);
	}
	public boolean executeMenu(final int id, Object mode, OpenPath file)
	{
		ArrayList<OpenPath> files = new ArrayList<OpenPath>();
		files.add(file);
		return executeMenu(id, mode, file, null);
	}
	public boolean executeMenu(final int id, final Object mode, final OpenPath file, ArrayList<OpenPath> fileList)
	{
		final String path = file != null ? file.getPath() : null;
		final OpenPath folder = file != null ? file.getParent() : null;
		String name = file != null ? file.getName() : null;
		if(fileList == null)
			fileList = new ArrayList<OpenPath>();
		final OpenPath[] fileArray = new OpenPath[fileList.size()];
		fileList.toArray(fileArray);
		
		super.onClick(id);
		
		switch(id)
		{
			case R.id.menu_context_view:
				IntentManager.startIntent(file, getExplorer());
				break;
			case R.id.menu_context_edit:
				Intent intent = IntentManager.getIntent(file, getExplorer());
				if(intent != null)
				{
					try {
						intent.setAction(Intent.ACTION_EDIT);
						getExplorer().startActivity(intent);
					} catch(ActivityNotFoundException e) {
						getExplorer().showToast(R.string.s_error_no_intents);
						getExplorer().editFile(file);
					}
				} else {
					getExplorer().editFile(file);
				}
				break;
			case R.id.menu_context_multi:
				changeMultiSelectState(!mMultiSelectOn, MultiSelectHandler.getInstance(mContext));
				addHoldingFile(file);
				return true;
			case R.id.menu_multi:
				changeMultiSelectState(!mMultiSelectOn, MultiSelectHandler.getInstance(mContext));
				return true;
			case R.id.menu_context_bookmark:
				getExplorer().addBookmark(file);
				finishMode(mode);
				return true;
				
			case R.id.menu_context_delete:
				fileList.add(file);
				mHandler.deleteFile(fileList, getActivity());
				finishMode(mode);
				mContentAdapter.notifyDataSetChanged();
				return true;
				
			case R.id.menu_context_rename:
				mHandler.renameFile(file.getPath(), true, getActivity());
				finishMode(mode);
				return true;
				
			case R.id.menu_context_copy:
			case R.id.menu_context_cut:
				if(id == R.id.menu_context_cut)
					getClipboard().DeleteSource = true;
				else
					getClipboard().DeleteSource = false;

				addHoldingFile(file);
				return false;

			case R.id.menu_context_paste:
			case R.id.menu_paste:
				OpenPath into = file;
				if(!file.isDirectory())
				{
					Logger.LogWarning("Can't paste into file (" + file.getPath() + "). Using parent directory (" + folder.getPath() + ")");
					into = folder;
				}
				OpenClipboard cb = getClipboard();
				if(cb.size() > 0)
					if(cb.DeleteSource)
						mHandler.cutFile(cb, into, getActivity());
					else
						mHandler.copyFile(cb, into, getActivity());
				
				cb.DeleteSource = false;
				if(cb.ClearAfter)
					clearHoldingFiles();
				getExplorer().updateTitle(path);
				finishMode(mode);
				return true;
				
			case R.id.menu_context_zip:
				addHoldingFile(file);
				final String def = getClipboard().size() == 1 ?
						file.getName() + ".zip" :
						file.getParent().getName() + ".zip";
				
				final DialogBuilder dZip = new DialogBuilder(mContext);
				dZip
					.setDefaultText(def)
					.setIcon(getResources().getDrawable(R.drawable.lg_zip))
					.setTitle(R.string.s_menu_zip)
					.setCancelable(true)
					.setPositiveButton(android.R.string.ok,
						new OnClickListener() {
							public void onClick(DialogInterface di, int which) {
								if(which != DialogInterface.BUTTON_POSITIVE) return;
								OpenPath zipFile = folder.getChild(dZip.getInputText());
								Logger.LogInfo("Zipping " + fileArray.length + " items to " + zipFile.getPath());
								mHandler.zipFile(zipFile, fileArray, getActivity());
								finishMode(mode);
							}
						})
					.setMessage(R.string.s_prompt_zip)
					.create().show();
				return true;
				
			case R.id.menu_context_unzip:
				mHandler.unZipFileTo(getClipboard().get(0), file, getActivity());
				
				clearHoldingFiles();
				getExplorer().updateTitle("");
				return true;
			
			case R.id.menu_context_info:
				getExplorer().showFileInfo(file);
				finishMode(mode);
				return true;
				
			case R.id.menu_context_share:
				
				// TODO: WTF is this?
				Intent mail = new Intent();
				mail.setType("application/mail");
				
				mail.setAction(android.content.Intent.ACTION_SEND);
				mail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
				startActivity(mail);
				
				//mode.finish();
				return true;
	
	//			this is for bluetooth
	//			files.add(path);
	//			mHandler.sendFile(files);
	//			mode.finish();
	//			return true;
			}
		return true;
	}
		
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(!OpenExplorer.BEFORE_HONEYCOMB && USE_ACTIONMODE) return;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		OpenPath file = mData2.get(info != null ? info.position : mMenuContextItemIndex);
		new MenuInflater(mContext).inflate(R.menu.context_file, menu);
		menu.findItem(R.id.menu_context_paste).setEnabled(isHoldingFiles());
		if(!mLastPath.isFile() || !IntentManager.isIntentAvailable(mLastPath, getExplorer()))
		{
			menu.findItem(R.id.menu_context_edit).setVisible(false);
			menu.findItem(R.id.menu_context_view).setVisible(false);
		}
	}
	
	//@Override
	public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
		final OpenPath file = (OpenPath)list.getItemAtPosition(pos);
		
		
		Logger.LogDebug("File clicked: " + file.getPath());
		
		//getExplorer().hideBookmarkTitles();
		
		if(mMultiSelectOn) {
			View v;
			
			//if (mThumbnail == null)
				v = mMultiSelect.addFile(file.getPath());
			//else
			//	v = mMultiSelect.addFile(file.getPath(), mThumbnail);
			
			if(v == null)
				return;
			
			v.setOnClickListener(new View.OnClickListener() {
				//@Override
				public void onClick(View v) {					
					int ret = mMultiSelect.clearFileEntry(file.getPath());
					mMultiSelectView.removeViewAt(ret);
				}
			});
			
			mMultiSelectView.addView(v);
			return;
		}
		
		if(file.isDirectory() && !mActionModeSelected ) {
			/* if (mThumbnail != null) {
				mThumbnail.setCancelThumbnails(true);
				mThumbnail = null;
			} */
			
			
			//setContentPath(file, true);
			getExplorer().onChangeLocation(file);

		} else if (!file.isDirectory() && !mActionModeSelected) {
			
			if(file.requiresThread())
			{
				//getExplorer().showToast("Still need to handle this.");
				getExplorer().editFile(file);
				return;
			}
			
			IntentManager.startIntent(file, getExplorer(), true);
		}
	}
	
	private void updateData(final OpenPath[] items) {
		if(!mReadyToUpdate) return;
		if(items == null) return;
		mReadyToUpdate = false;
		
		if(mProgressBarLoading != null)
			mProgressBarLoading.setVisibility(View.GONE);
		
		OpenPath.Sorting = mFileManager.getSorting();
		if(items.length < 200 || (OpenPath.Sorting != SortType.DATE_DESC && OpenPath.Sorting != SortType.NONE))
		{
			Arrays.sort(items);
			//Logger.LogDebug("Sorted by " + OpenPath.Sorting.toString() + "!");
		} //else Logger.LogDebug("No sorting needed");
		
		mData2.clear();
		final boolean dirsFirst = items.length < 200;
		ArrayList<OpenPath> mDirs = new ArrayList<OpenPath>();
		for(OpenPath f : items)
		{
			if(!mShowHidden && f.isHidden())
				continue;
			if(dirsFirst && f.isDirectory())
				mDirs.add(f);
			else
				mData2.add(f);
		}
		if(dirsFirst)
			mData2.addAll(0, mDirs);
		//Logger.LogDebug("Copied to final array");
		//Logger.LogDebug("mData has " + mData2.size());
		if(mContentAdapter != null)
			mContentAdapter.notifyDataSetChanged();
		mReadyToUpdate = true;
		/*
		mPathView.removeAllViews();
		mBackPathIndex = 0;	
		 */
	}
	
	/*
	 * (non-Javadoc)
	 * this will update the data shown to the user after a change to
	 * the file system has been made from our background thread or EventHandler.
	 */
	//@Override
	public void onWorkerThreadComplete(int type, ArrayList<String> results) {
		
		if(type == EventHandler.SEARCH_TYPE) {
			if(results == null || results.size() < 1) {
				Toast.makeText(mContext, "Sorry, zero items found", Toast.LENGTH_LONG).show();
				return;
			}
			
			DialogHandler dialog = DialogHandler.newDialog(DialogHandler.DialogType.SEARCHRESULT_DIALOG, mContext);
			ArrayList<OpenPath> files = new ArrayList<OpenPath>();
			for(String s : results)
				files.add(new OpenFile(s));
			dialog.setHoldingFileList(files);
			dialog.setOnSearchFileSelected(new OnSearchFileSelected() {
				
				//@Override
				public void onFileSelected(String fileName) {
					OpenPath file = null;
					try {
						file = FileManager.getOpenCache(fileName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(file == null)
						file = new OpenFile(fileName);
					
					if (file.isDirectory()) {
						changePath(mFileManager.pushStack(file));
					} else {
						changePath(mFileManager.pushStack(file.getParent()));
					}						
				}
			});
			
			dialog.show(getFragmentManager(), "dialog");
			
		} else if(type == EventHandler.UNZIPTO_TYPE && results != null) {
			String name = new OpenFile(results.get(0)).getName();
			
			addHoldingFile(new OpenFile(results.get(0)));
			getExplorer().updateTitle("Holding " + name);
			
		} else {
			Logger.LogDebug("Worker thread complete?");
			if(!mPath.requiresThread())
				try {
					updateData(mPath.list());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else {
				if(mProgressBarLoading == null)
					mProgressBarLoading = getView().findViewById(R.id.content_progress);
				new FileIOTask().execute(new FileIOCommand(FileIOCommandType.ALL, mPath));
			}
			
			//changePath(mPath, false);
			//mContentAdapter.notifyDataSetChanged();
			//changePath(mFileManager.peekStack(), false);
		}
	}
	
	public void changePath(OpenPath path) { changePath(path, true); }
	public void changePath(OpenPath path, Boolean addToStack) {
		if(path == null)
			path = new OpenFile(Environment.getExternalStorageDirectory());
		getExplorer().changePath(path, addToStack);
	}
	
	//@Override
	public void onHiddenFilesChanged(boolean state)
	{
		if(mFileManager == null)
			mFileManager = new FileManager();
		mFileManager.setShowHiddenFiles(state);
		refreshData(null);
	}

	//@Override
	public void onThumbnailChanged(boolean state) {
		mShowThumbnails = state;
		refreshData(null);
	}
	
	//@Override
	public void onSortingChanged(SortType type) {
		if(mFileManager == null)
			mFileManager = new FileManager();
		mFileManager.setSorting(type);
		refreshData(null);
	}
	
	public void setSettings(SortType sort, boolean thumbs, boolean hidden)
	{
		if(mFileManager == null)
			mFileManager = new FileManager();
		mFileManager.setSorting(sort);
		mShowThumbnails = thumbs;
		mFileManager.setShowHiddenFiles(hidden);
		refreshData(null);
	}

	//@Override
	public void onViewChanged(int state) {
		//mViewMode = state;
		getExplorer().setViewMode(state);
		
		View v = getView();
		if(v != null)
		{
			//if(mPathView == null)
			//	mPathView = (LinearLayout)v.findViewById(R.id.scroll_path);
			if(mGrid == null)
				mGrid = (GridView)v.findViewById(R.id.grid_gridview);
			if(mMultiSelectView == null)
				mMultiSelectView = (LinearLayout)v.findViewById(R.id.multiselect_path);
		}

		if(mGrid == null)
			Logger.LogError("WTF, where are they?");
		else
			updateGridView();
	}
			
	public void changeMultiSelectState(boolean state, MultiSelectHandler handler) {
		if(state && handler != null) {
			mMultiSelect = handler;
			mMultiSelectOn = state;
			
		} else if (!state && handler != null) {
			mMultiSelect = handler;
			mMultiSelect.cancelMultiSelect();
			mMultiSelectView.removeAllViews();
			mMultiSelectOn = state;
		}
	}
	
	
	
	/**
	 * 
	 */
	
	public enum FileIOCommandType
	{
		ALL
	}
	public class FileIOCommand
	{
		public FileIOCommandType Type;
		public OpenPath Path;
		
		public FileIOCommand(FileIOCommandType type, OpenPath path)
		{
			Type = type;
			Path = path;
		}
	}
	
	public class FileIOTask extends AsyncTask<FileIOCommand, Integer, OpenPath[]>
	{
		@Override
		protected OpenPath[] doInBackground(FileIOCommand... params) {
			publishProgress(0);
			ArrayList<OpenPath> ret = new ArrayList<OpenPath>();
			for(FileIOCommand cmd : params)
			{
				if(cmd.Path.requiresThread())
				{
					OpenFTP file = null;
					try {
						file = (OpenFTP)FileManager.getOpenCache(cmd.Path.getAbsolutePath(), true);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					OpenPath[] list = null;
					try {
						if(file != null)
							list = file.list();
					} catch (IOException e) {
						list = null;
					}
					if(list != null) {
						for(OpenPath f : list)
							ret.add(f);
					} else {
						getExplorer().showToast(R.string.s_error_ftp);
					}
				} else {
					try {
						for(OpenPath f : cmd.Path.list())
							ret.add(f);
					} catch (IOException e) {
						Logger.LogError("IOException listing children inside FileIOTask", e);
					}
				}
				if(OpenFTP.class.equals(cmd.Path.getClass()))
					((OpenFTP)cmd.Path).getManager().disconnect();
				mFileManager.pushStack(cmd.Path);
			}
			Logger.LogDebug("Found " + ret.size() + " items.");
			OpenPath[] ret2 = new OpenPath[ret.size()];
			ret.toArray(ret2);
			//Logger.LogDebug("Filled 2nd array");
			return ret2;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//mData.clear();
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.VISIBLE);
			else Logger.LogDebug("Starting FileIOTask");
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(OpenPath[] result)
		{
			if(mProgressBarLoading != null)
				mProgressBarLoading.setVisibility(View.GONE);
			else Logger.LogDebug("Ending FileIOTask");
			mData2.clear();
			updateData(result);
		}
		
	}
	public class DialogBuilder extends Builder
	{
		private View view;
		private EditText mEdit, mEdit2;
		
		public DialogBuilder(Context mContext) {
			super(mContext);
			
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.input_dialog_layout, null);
			super.setView(view);
			mEdit = (EditText)view.findViewById(R.id.dialog_input);
			mEdit2 = (EditText)view.findViewById(R.id.dialog_input_top);
			if(mEdit == null)
				mEdit = mEdit2;
		}
		
		public String getInputText() { return mEdit.getText().toString(); }
		public DialogBuilder setPrompt(String s) {
			((EditText)view.findViewById(R.id.dialog_message_top)).setText(s);
			return this;
		}
		
		public DialogBuilder setDefaultText(String s) 
		{
			mEdit.setText(s);
			return this;
		}
		
		@Override
		public DialogBuilder setMessage(CharSequence message) {
			super.setMessage(message);
			return this;
		}
		
		@Override
		public DialogBuilder setTitle(CharSequence title) {
			super.setTitle(title);
			return this;
		}
		
		@Override
		public DialogBuilder setIcon(Drawable icon) {
			super.setIcon(icon);
			return this;
		}
	}
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String sort = null;
		switch(OpenPath.Sorting)
		{
		case ALPHA:
			sort = "_display_name";
			break;
		case ALPHA_DESC:
			sort = "_display_name DESC";
			break;
		case DATE:
			sort = "date_modified";
			break;
		case DATE_DESC:
			sort = "date_modified DESC";
			break;
		case SIZE:
			sort = "_size";
			break;
		case SIZE_DESC:
			sort = "_size DESC";
			break;
		}
		return OpenCursor.createCursorLoader(mContext, id, 10000, sort);
	}
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(SimpleCursorAdapter.class.equals(mContentAdapter.getClass()))
			((SimpleCursorAdapter)mContentAdapter).swapCursor(data);
		else {
			//mContentAdapter = new SimpleCursorAdapter(mContext, mListLayoutID, data, )
		}
	}
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
}


