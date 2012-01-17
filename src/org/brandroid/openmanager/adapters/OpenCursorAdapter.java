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
	
	public OpenCursorAdapter setLayout(int layoutId) { mLayoutID = layoutId; return this; }
	public OpenCursorAdapter setParent(OpenCursor parent) { mParent = parent; return this; } 
	
	@Override
	public OpenMediaStore getItem(int position) {
		mCursor.moveToPosition(position);
		return new OpenMediaStore(mParent, getCursor());
	}
	
	@Override
	public View getView(int pos, View view, ViewGroup parent)
	{
		mCursor.moveToPosition(pos);
		final String mName = mCursor.getString(mCursor.getColumnIndex("_display_name"));
		
		int mWidth = 36, mHeight = 36;
		if(OpenExplorer.getViewMode() == OpenExplorer.VIEW_GRID)
			mWidth = mHeight = 128;
		
		if(view == null) {
			LayoutInflater in = (LayoutInflater)mContext
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			view = in.inflate(mLayoutID, parent, false);
		}

		String path = mCursor.getString(mCursor.getColumnIndex("_data"));
		//OpenFile file = new OpenFile(path);
		
		if(view.findViewById(R.id.content_fullpath) != null)
			((TextView)view.findViewById(R.id.content_fullpath)).setText(path.subSequence(0, path.lastIndexOf("/")));
		((TextView)view.findViewById(R.id.content_text)).setText(mName);
		
		ImageView img = ((ImageView)view.findViewById(R.id.content_icon));
		
		Bitmap b = ThumbnailCreator.getThumbnailCache(path, mWidth, mHeight);
		if(b != null)
		{
			img.setImageBitmap(b);
		} else {
			SoftReference<Bitmap> sr = ThumbnailCreator.generateThumb(path, mWidth, mHeight, true, true, mContext);
			if(sr != null && sr.get() != null)
				img.setImageBitmap(sr.get());
			else if(getCursor() instanceof OpenMediaStore)
				ThumbnailCreator.setThumbnail(img, (OpenMediaStore)getCursor(), mWidth, mHeight);
			else if(OpenCursor.class.equals(getCursor().getClass()))
				ThumbnailCreator.setThumbnail(img, new OpenMediaStore((OpenCursor)getCursor(), getCursor()), mWidth, mHeight);
			else
				ThumbnailCreator.setThumbnail(img, new OpenFile(path), mWidth, mHeight);
		}
		
		return view;
	}

}
