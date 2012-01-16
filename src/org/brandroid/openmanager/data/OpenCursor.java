package org.brandroid.openmanager.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.RejectedExecutionException;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;

import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

@SuppressWarnings("unused")
public class OpenCursor
	extends OpenPath // Actual object is the "Parent" of the OpenMediaStore children 
	implements Cursor // when referenced as Cursor, object represents the children
{
	private static OpenCursor mPhotoParent, mVideoParent, mMusicParent, mApkParent, mDownloadParent;
	private static long mLastCursorEnsure = 0;
	
	private static final long serialVersionUID = -8828123354531942575L;
	
	private static final boolean CURSOR_MODE = true;
	private Cursor mCursor;
	private OpenMediaStore[] mChildren;
	private String mName;
	private Long mTotalSize = 0l;
	
	public static final int CURSOR_TYPE_VIDEO = 0;
	public static final int CURSOR_TYPE_PHOTO = 1;
	public static final int CURSOR_TYPE_MUSIC = 2;
	public static final int CURSOR_TYPE_APK = 3;
	public static final int CURSOR_TYPE_DOWNLOADS = 4;
	
	public OpenCursor(Cursor c, String name)
	{
		mName = name;
		if(CURSOR_MODE) {
			mCursor = c;
			moveToFirst();
			return;
		}
		if(c == null) return;
		ArrayList<OpenMediaStore> kids = new ArrayList<OpenMediaStore>(c.getCount());
		//mChildren = new OpenMediaStore[(int)c.getCount()];
		c.moveToFirst();
		for(int i = 0; i < c.getCount(); i++)
		{
			c.moveToPosition(i);
			OpenMediaStore tmp = new OpenMediaStore(this, c);
			if(!tmp.exists()) continue;
			if(!tmp.getFile().exists()) continue;
			kids.add(tmp);
			mTotalSize += tmp.getFile().length();
		}
		mChildren = new OpenMediaStore[kids.size()];
		mChildren = kids.toArray(mChildren);
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public String getAbsolutePath() {
		return getString(getColumnIndex("_data"));
	}

	@Override
	public long length() {
		return mChildren != null ? mChildren.length : mCursor.getCount();
	}

	@Override
	public OpenPath getParent() {
		return null;
	}

	@Override
	public OpenPath getChild(String name) {
		return null;
	}

	@Override
	public OpenMediaStore[] list() {
		if(mChildren != null)
		{
			return mChildren;
		}
		OpenMediaStore[] ret = new OpenMediaStore[(int)length()];
		mCursor.moveToFirst();
		int i = 0;
		while(!mCursor.isAfterLast())
		{
			if(!mCursor.isBeforeFirst())
			{
				try {
					ret[i] = new OpenMediaStore(this, mCursor);
					mTotalSize += ret[i].length();
					i++;
				} catch(Exception e) {
					Logger.LogWarning("Had to exit OpenCursor.list() at index " + i);
					break;
				}
			}
			if(!mCursor.moveToNext()) break;
		}
		mChildren = ret;
		return ret;
	}

	@Override
	public OpenMediaStore[] listFiles() {
		return list();
	}

	@Override
	public Boolean isDirectory() {
		return true;
	}

	@Override
	public Boolean isFile() {
		return false;
	}

	@Override
	public Boolean isHidden() {
		return false;
	}

	@Override
	public Uri getUri() {
		return null;
	}

	@Override
	public Long lastModified() {
		return mCursor.getLong(getColumnIndex("last_modified"));
	}

	@Override
	public Boolean canRead() {
		return true;
	}

	@Override
	public Boolean canWrite() {
		return true;
	}

	@Override
	public Boolean canExecute() {
		return false;
	}

	@Override
	public Boolean exists() {
		return true;
	}

	@Override
	public Boolean requiresThread() {
		return false;
	}

	@Override
	public Boolean delete() {
		return new OpenFile(getAbsolutePath()).delete();
	}

	@Override
	public Boolean mkdir() {
		return new OpenFile(getAbsolutePath()).mkdir();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new OpenFile(getAbsolutePath()).getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new OpenFile(getAbsolutePath()).getOutputStream();
	}
	
	@Override
	public void setPath(String path) {
		
	}
	
	public long getTotalSize()
	{
		list();
		return mTotalSize;
	}
	
	public static CursorLoader createCursorLoader(Context context, int type, int minSize, String sort)
	{
		Uri base = null;
		switch(type)
		{
			case CURSOR_TYPE_VIDEO:
				base = Uri.parse("content://media/external/video/media");
				break;
			case CURSOR_TYPE_PHOTO:
				base = Uri.parse("content://media/external/images/media");
				break;
			case CURSOR_TYPE_MUSIC:
				base = Uri.parse("content://media/external/audio/media");
				break;
			case CURSOR_TYPE_APK:
				base = android.providercompat.MediaStore.Files.getContentUri(Environment.getExternalStorageDirectory().getPath());
				break;
			case CURSOR_TYPE_DOWNLOADS:
				base = android.providercompat.MediaStore.Files.getContentUri(Environment.getExternalStorageDirectory().getPath());
				break;
		}
		if(base == null) return null;
		return new CursorLoader(context,
				base,
				new String[]{"_id", "_display_name", "_data", "_size", "date_modified"},
				minSize <= 0 ? null : " _size > " + minSize, null,
				sort);
	}
	
	public static void refreshCursors(Context context)
    {
		if(mVideoParent == null)
    	{
			//if(!IS_DEBUG_BUILD)
			try {
				mVideoParent = new OpenCursor(
						createCursorLoader(context, CURSOR_TYPE_VIDEO, 100000, null).loadInBackground(),
						"Videos");
    		} catch(IllegalStateException e) { Logger.LogError("Couldn't query videos.", e); }
    	}
    	if(mPhotoParent == null)
    	{
    		try {
    			mPhotoParent = new OpenCursor(
    					createCursorLoader(context, CURSOR_TYPE_PHOTO, 10000, null).loadInBackground(),
    					"Photos");
    		} catch(IllegalStateException e) { Logger.LogError("Couldn't query photos.", e); }
		}
		if(mMusicParent == null)
		{
			try {
				mMusicParent = new OpenCursor(
						createCursorLoader(context, CURSOR_TYPE_MUSIC, 10000, null).loadInBackground(),
						"Music");
    		} catch(IllegalStateException e) { Logger.LogError("Couldn't query music.", e); }
		}
		if(mApkParent == null && Build.VERSION.SDK_INT > 10)
		{
			try {
				mApkParent = new OpenCursor(
						createCursorLoader(context, CURSOR_TYPE_APK, 100, null).loadInBackground(),
						"Apps");
			} catch(IllegalStateException e) { Logger.LogError("Couldn't get Apks.", e); }
		}
		if(mDownloadParent == null)
		{
			if(Build.VERSION.SDK_INT > 10)
			{
				mDownloadParent = new OpenCursor(
						createCursorLoader(context, CURSOR_TYPE_DOWNLOADS, -1, null).loadInBackground(),
						"Downloads");
			}
			
		}
		//Cursor mAudioCursor = managedQuery(MediaStore.Audio, projection, selection, selectionArgs, sortOrder)
		ensureCursorCache();
    }
	

    public static void ensureCursorCache()
    {
    	if(mLastCursorEnsure == 0 || new Date().getTime() - mLastCursorEnsure < 10000) // at least 10 seconds
    		return;
    	mLastCursorEnsure = new Date().getTime();
    	
    	// group into blocks
    	int iTotalSize = 0;
    	for(OpenCursor cur : new OpenCursor[]{mVideoParent, mPhotoParent, mApkParent})
    		if(cur != null)
    			iTotalSize += cur.length();
    	int enSize = Math.max(20, iTotalSize / 10);
    	Logger.LogDebug("EnsureCursorCache size: " + enSize + " / " + iTotalSize);
    	ArrayList<OpenPath> buffer = new ArrayList<OpenPath>(enSize);
    	for(OpenCursor curs : new OpenCursor[]{mVideoParent, mPhotoParent, mApkParent})
    	{
    		if(curs == null) continue;
	    	for(OpenMediaStore ms : curs.list())
	    	{
	    		buffer.add(ms);
	    		if(buffer.size() == enSize)
	    		{
	    			OpenMediaStore[] buff = new OpenMediaStore[buffer.size()];
	    			buffer.toArray(buff);
	    			buffer.clear();
	    			try {
	    				new EnsureCursorCacheTask().execute(buff);
	    			} catch(RejectedExecutionException e) {
	    				Logger.LogWarning("Couldn't ensure cache.", e);
	    				return;
	    			}
	    		}
	    	}
    	}
    	if(buffer.size() > 0)
    	{
    		OpenMediaStore[] buff = new OpenMediaStore[buffer.size()];
			buffer.toArray(buff);
			buffer.clear();
			try {
				new EnsureCursorCacheTask().execute(buff);
			} catch(RejectedExecutionException e) {
				Logger.LogWarning("Couldn't ensure cache.", e);
				return;
			}
    	}
    	mLastCursorEnsure = new Date().getTime();
    }
    
    public static boolean hasVideos() {
    	return mVideoParent != null && mVideoParent.length() > 0;
    }
    
    public static boolean hasPhotos() {
    	return mPhotoParent != null && mPhotoParent.length() > 0;
    }
    
    public static OpenCursor getPhotoParent() { return mPhotoParent; }
    public static OpenCursor getVideoParent() { return mVideoParent; }
    public static OpenCursor getMusicParent() { return mMusicParent; }
    public static OpenCursor getApkParent() { return mApkParent; }
    public static OpenCursor getDownloadParent() { return mDownloadParent; }

	public static class EnsureCursorCacheTask extends AsyncTask<OpenPath, Void, Void>
	{
		@Override
		protected Void doInBackground(OpenPath... params) {
			int done = 0;
			for(OpenPath path : params)
			{
				if(path.isDirectory())
				{
					try {
						for(OpenPath kid : path.list())
						{
							ThumbnailCreator.generateThumb(kid, 36, 36);
							ThumbnailCreator.generateThumb(kid, 96, 96);
							done++;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					//if(!ThumbnailCreator.hasContext())
					//	ThumbnailCreator.setContext(getApplicationContext());
					ThumbnailCreator.generateThumb(path, 36, 36);
					ThumbnailCreator.generateThumb(path, 96, 96);
					done++;
				}
			}
			//Logger.LogDebug("cursor cache of " + done + " generated.");
			return null;
		}
		
	}

	public void close() {
		mCursor.close();
	}

	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
		mCursor.copyStringToBuffer(columnIndex, buffer);
	}

	public void deactivate() {
		mCursor.deactivate();
	}

	public byte[] getBlob(int columnIndex) {
		return mCursor.getBlob(columnIndex);
	}

	public int getColumnCount() {
		return mCursor.getColumnCount();
	}

	public int getColumnIndex(String columnName) {
		return mCursor.getColumnIndex(columnName);
	}

	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		return mCursor.getColumnIndexOrThrow(columnName);
	}

	public String getColumnName(int columnIndex) {
		return mCursor.getColumnName(columnIndex);
	}

	public String[] getColumnNames() {
		return mCursor.getColumnNames();
	}

	public int getCount() {
		return mCursor.getCount();
	}

	public double getDouble(int columnIndex) {
		return mCursor.getDouble(columnIndex);
	}

	public Bundle getExtras() {
		return mCursor.getExtras();
	}

	public float getFloat(int columnIndex) {
		return mCursor.getFloat(columnIndex);
	}

	public int getInt(int columnIndex) {
		return mCursor.getInt(columnIndex);
	}

	public long getLong(int columnIndex) {
		return mCursor.getLong(columnIndex);
	}

	public int getPosition() {
		return mCursor.getPosition();
	}

	public short getShort(int columnIndex) {
		return mCursor.getShort(columnIndex);
	}

	public String getString(int columnIndex) {
		if(columnIndex < 0 || columnIndex >= getColumnCount())
			return null;
		return mCursor.getString(columnIndex);
	}

	public int getType(int columnIndex) {
		return mCursor.getType(columnIndex);
	}

	public boolean getWantsAllOnMoveCalls() {
		return mCursor.getWantsAllOnMoveCalls();
	}

	public boolean isAfterLast() {
		return mCursor.isAfterLast();
	}

	public boolean isBeforeFirst() {
		return mCursor.isBeforeFirst();
	}

	public boolean isClosed() {
		return mCursor.isClosed();
	}

	public boolean isFirst() {
		return mCursor.isFirst();
	}

	public boolean isLast() {
		return mCursor.isLast();
	}

	public boolean isNull(int columnIndex) {
		return mCursor.isNull(columnIndex);
	}

	public boolean move(int offset) {
		return mCursor.move(offset);
	}

	public boolean moveToFirst() {
		return mCursor.moveToFirst();
	}

	public boolean moveToLast() {
		return mCursor.moveToLast();
	}

	public boolean moveToNext() {
		return mCursor.moveToNext();
	}

	public boolean moveToPosition(int position) {
		return mCursor.moveToPosition(position);
	}

	public boolean moveToPrevious() {
		return mCursor.moveToPrevious();
	}

	public void registerContentObserver(ContentObserver observer) {
		mCursor.registerContentObserver(observer);
		
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		mCursor.registerDataSetObserver(observer);
	}

	public boolean requery() {
		return mCursor.requery();
	}

	public Bundle respond(Bundle extras) {
		return mCursor.respond(extras);
	}

	public void setNotificationUri(ContentResolver cr, Uri uri) {
		mCursor.setNotificationUri(cr, uri);
	}

	public void unregisterContentObserver(ContentObserver observer) {
		mCursor.unregisterContentObserver(observer);
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		mCursor.unregisterDataSetObserver(observer);
	}	
}
