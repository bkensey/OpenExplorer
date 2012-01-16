package org.brandroid.openmanager.adapters;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.DialogHandler;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class OpenArrayAdapter extends ArrayAdapter<OpenPath> {
	private final int KB = 1024;
	private final int MG = KB * KB;
	private final int GB = MG * KB;
	
	private BookmarkHolder mHolder;
	private String mName;
	private Context mContext;
	
	public OpenArrayAdapter(Context context, int layout, ArrayList<OpenPath> data) {
		super(context, layout, data);
		mContext = context;
	}
	
	@Override
	public void notifyDataSetChanged() {
		//Logger.LogDebug("Data set changed.");
		try {
			//if(mFileManager != null)
			//	getExplorer().updateTitle(mFileManager.peekStack().getPath());
			super.notifyDataSetChanged();
		} catch(NullPointerException npe) {
			Logger.LogError("Null found while notifying data change.", npe);
		}
	}
	
	private final Handler handler = new Handler(new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			notifyDataSetChanged();
			return true;
		}
	});
	
	////@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		final OpenPath file = super.getItem(position);
		return getView(file, mContext, view, parent);
	}
	public static View getView(OpenPath file, Context mContext, View view, ViewGroup parent)
	{
		if(file == null) return null;
		final String mName = file.getName();
		
		int mWidth = 36, mHeight = 36;
		if(OpenExplorer.getViewMode() == OpenExplorer.VIEW_GRID)
			mWidth = mHeight = 128;
		
		BookmarkHolder mHolder = null;
		
		if(view == null) {
			LayoutInflater in = (LayoutInflater)mContext
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			view = in.inflate(OpenExplorer.getViewMode() == OpenExplorer.VIEW_GRID ?
						R.layout.grid_content_layout : R.layout.list_content_layout
					, parent, false);
			
			mHolder = new BookmarkHolder(file, mName, view);
			
			view.setTag(mHolder);
			file.setTag(mHolder);
			
		} else {
			mHolder = (BookmarkHolder)view.getTag();
			if(mHolder == null)
				mHolder = new BookmarkHolder(file, mName, view);
			//mHolder.cancelTask();
		}

		if(OpenExplorer.getViewMode() == OpenExplorer.VIEW_LIST) {
			mHolder.setInfo(getFileDetails(file, false)); //mShowLongDate));
			
			if(file.getClass().equals(OpenMediaStore.class))
			{
				mHolder.setPath(file.getPath());
				mHolder.showPath(true);
			}
			else
				mHolder.showPath(false);
		}
		
		if(!mHolder.getTitle().equals(mName))
			mHolder.setTitle(mName);
		
		SoftReference<Bitmap> sr = file.getThumbnail(mWidth, mHeight, false, false); // ThumbnailCreator.generateThumb(file, mWidth, mHeight, false, false, getContext());
		//Bitmap b = ThumbnailCreator.getThumbnailCache(file.getPath(), mWidth, mHeight);
		if(sr != null && sr.get() != null)
			mHolder.getIconView().setImageBitmap(sr.get());
		else
			ThumbnailCreator.setThumbnail(mHolder.getIconView(), file, mWidth, mHeight);
		
		return view;
	}
	
	private static String getFileDetails(OpenPath file, Boolean longDate) {
		//OpenPath file = mFileManager.peekStack().getChild(name); 
		String deets = ""; //file.getPath() + "\t\t";
		
		if(file.isDirectory() && !file.requiresThread()) {
			try {
				deets = file.list().length + " items";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			deets = DialogHandler.formatSize(file.length());
		}
		
		deets += " | ";
		
		DateFormat df = new SimpleDateFormat(longDate ? "MM-dd-yyyy HH:mm" : "MM-dd");
		deets += df.format(file.lastModified());
		
		deets += " | ";
		
		deets += (file.isDirectory()?"d":"-");
		deets += (file.canRead()?"r":"-");
		deets += (file.canWrite()?"w":"-");
		deets += (file.canExecute()?"x":"-");
		
		return deets;
	}
	
}
