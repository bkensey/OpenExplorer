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

import org.apache.commons.net.ftp.FTPFile;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.activities.SettingsActivity;
import org.brandroid.openmanager.data.BookmarkHolder;
import org.brandroid.openmanager.data.OpenBookmarks;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenServer;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.ftp.FTPManager;
import org.brandroid.openmanager.util.DFInfo;
import org.brandroid.openmanager.util.OpenInterfaces.OnBookMarkChangeListener;
import org.brandroid.openmanager.util.RootManager;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.File;

public class BookmarkFragment extends OpenFragment implements OnBookMarkChangeListener {
	
	private OpenBookmarks mBookmarks;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	
	@Override
	public void onPause() {
		super.onPause();
		//mBookmarks.saveBookmarks();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.bookmarks_fragment, null);
		//return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	public ExpandableListView getListView() { return (ExpandableListView) getView().findViewById(android.R.id.list); }
	public void setListAdapter(ExpandableListAdapter adapter) { getListView().setAdapter(adapter); }
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//Logger.LogDebug("Bookmark Fragment Created");

		final ExpandableListView lv = getListView();
		mBookmarks = new OpenBookmarks(getExplorer(), lv);
		mBookmarks.setupListView(lv);
		final ExpandableListAdapter adapter = mBookmarks.getListAdapter();
		setListAdapter(adapter);
		registerForContextMenu(lv);
	}
	

	public void onBookMarkAdd(OpenPath path) {
		if(mBookmarks != null)
			mBookmarks.onBookMarkAdd(path);
	}

	public void scanBookmarks() {
		if(mBookmarks != null)
			mBookmarks.scanBookmarks();
	}



	public void expandAll() {
		ExpandableListView list = getListView();
		for(int i = 0; i < list.getCount(); i++)
			list.expandGroup(i);
	}

}
