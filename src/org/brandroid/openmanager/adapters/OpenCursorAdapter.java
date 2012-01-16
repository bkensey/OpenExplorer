package org.brandroid.openmanager.adapters;

import java.lang.ref.SoftReference;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class OpenCursorAdapter extends CursorAdapter
{
	private int mLayoutID;
	private OpenCursor mParent;

	public OpenCursorAdapter(Context context, Cursor c, int flags, OpenCursor parent, int layoutId) {
		super(context, c, flags);
		mLayoutID = layoutId;
		mParent = parent;
	}

	@Override
	public void bindView(View view, Context context, Cursor data) {
		Logger.LogWarning("You didn't write this one (OpenCursorAdapter.bindView)");
	}

	@Override
	public View newView(Context context, Cursor data, ViewGroup parent) {
		Logger.LogWarning("You didn't write this one (OpenCursorAdapter.newView)");
		return null;
	}
	
	@Override
	public View getView(int pos, View view, ViewGroup parent)
	{
		mCursor.moveToPosition(pos);
		final String mName = mCursor.getString(mCursor.getColumnIndex("_display_name"));
		
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
		}

		String path = mCursor.getString(mCursor.getColumnIndex("_data"));
		OpenFile file = new OpenFile(path);
		
		if(view.findViewById(R.id.content_fullpath) != null)
			((TextView)view.findViewById(R.id.content_fullpath)).setText(path);
		((TextView)view.findViewById(R.id.content_text)).setText(mName);
		
		ImageView img = ((ImageView)view.findViewById(R.id.content_icon));
		
		SoftReference<Bitmap> sr = file.getThumbnail(mWidth, mHeight, false, false); // ThumbnailCreator.generateThumb(file, mWidth, mHeight, false, false, getContext());
		//Bitmap b = ThumbnailCreator.getThumbnailCache(file.getPath(), mWidth, mHeight);
		if(sr != null && sr.get() != null)
			img.setImageBitmap(sr.get());
		else
			ThumbnailCreator.setThumbnail(img, file, mWidth, mHeight);
		
		return view;
	}

}
