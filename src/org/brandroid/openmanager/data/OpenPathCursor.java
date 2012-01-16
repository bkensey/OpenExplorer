package org.brandroid.openmanager.data;

import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

public class OpenPathCursor implements Cursor
{
	private ArrayList<OpenPath> mPaths;
	private int mIndex = 0;
	private final static String[] mColumns = new String[]{"_id", "_display_name", "_data", "_size", "date_modified"};
	
	public OpenPathCursor(OpenPath... parents)
	{
		mPaths = new ArrayList<OpenPath>();
		for(OpenPath path : parents)
			if(path != null && path.exists())
			{
				if(path.isDirectory())
					try {
						for(OpenPath kid : path.list())
							mPaths.add(kid);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					mPaths.add(path);
			}
		mIndex = 0;
	}

	public void close() {
		mPaths = null;			
	}

	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	public void deactivate() {
		// TODO Auto-generated method stub
		
	}

	public byte[] getBlob(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getColumnCount() {
		return mColumns.length;
	}

	public int getColumnIndex(String columnName) {
		for(int i = 0; i < mColumns.length; i++)
			if(mColumns[i].equals(columnName))
				return i;
		return -1;
	}

	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		if(getColumnIndex(columnName) == -1)
			throw new IllegalArgumentException();
		return 0;
	}

	public String getColumnName(int columnIndex) {
		return mColumns[columnIndex];
	}

	public String[] getColumnNames() {
		return mColumns;
	}

	public int getCount() {
		return mPaths.size();
	}

	public double getDouble(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Bundle getExtras() {
		// TODO Auto-generated method stub
		return null;
	}

	public float getFloat(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInt(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLong(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getShort(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getString(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getType(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getWantsAllOnMoveCalls() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAfterLast() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBeforeFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLast() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNull(int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean move(int offset) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToFirst() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToLast() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToNext() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToPosition(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean moveToPrevious() {
		// TODO Auto-generated method stub
		return false;
	}

	public void registerContentObserver(ContentObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public boolean requery() {
		// TODO Auto-generated method stub
		return false;
	}

	public Bundle respond(Bundle extras) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNotificationUri(ContentResolver cr, Uri uri) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterContentObserver(ContentObserver observer) {
		// TODO Auto-generated method stub
		
	}

	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		
	}
	
}

