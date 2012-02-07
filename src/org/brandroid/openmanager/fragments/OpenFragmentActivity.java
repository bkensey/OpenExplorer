package org.brandroid.openmanager.fragments;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenClipboard;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class OpenFragmentActivity
			extends FragmentActivity
			implements View.OnClickListener, View.OnLongClickListener
{
	private Preferences mPreferences = null;
	
	protected static final FileManager mFileManager = new FileManager();
	protected static final EventHandler mEvHandler = new EventHandler(mFileManager);
	protected static OpenClipboard mClipboard;
	
	//public static boolean CONTENT_FRAGMENT_FREE = true;
	//public boolean isFragmentValid = true;
	
	public static OpenClipboard getClipboard() {
		return mClipboard;
	}
	
	public String getClassName()
	{
		return this.getClass().getSimpleName();
	}
	
	public void onClick(View v) {
		Logger.LogDebug("View onClick(" + v.getId() + ") - " + v.toString());
	}
	
	public void onClick(int id) {
		Logger.LogDebug("View onClick(" + id + ") / " + getClassName());
	}
	
	public boolean onLongClick(View v) {
		Logger.LogDebug("View onLongClick(" + v.getId() + ") - " + v.toString());
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.LogDebug("Menu selected(" + item.getItemId() + ") - " + item.toString());
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.LogDebug("<-onCreate - " + getClassName());
		//CONTENT_FRAGMENT_FREE = false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Logger.LogDebug("->onDestroy - " + getClassName());
	}
	
	public void showToast(final String message)  {
		Logger.LogInfo("Made toast: " + message);
		showToast(message, Toast.LENGTH_SHORT);
	}
	public void showToast(final int iStringResource) { showToast(getResources().getString(iStringResource)); }
	public void showToast(final String message, final int toastLength)  {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getBaseContext(), message, toastLength).show();
			}
		});
	}


	public Preferences getPreferences() {
		if(mPreferences == null)
			mPreferences = new Preferences(getApplicationContext());
		return mPreferences;
	}
	public String getSetting(OpenPath file, String key, String defValue)
	{
		return getPreferences().getSetting("global", key + (file != null ? "_" + file.getPath() : ""), defValue);
	}
	public Boolean getSetting(OpenPath file, String key, Boolean defValue)
	{
		return getPreferences().getSetting("global", key + (file != null ? "_" + file.getPath() : ""), defValue);
	}
	public Integer getSetting(OpenPath file, String key, Integer defValue)
	{
		return getPreferences().getSetting("global", key + (file != null ? "_" + file.getPath() : ""), defValue);
	}
	public void setSetting(OpenPath file, String key, String value)
	{
		getPreferences().setSetting("global", key + (file != null ? "_" + file.getPath() : ""), value);
	}
	public void setSetting(OpenPath file, String key, Boolean value)
	{
		getPreferences().setSetting("global", key + (file != null ? "_" + file.getPath() : ""), value);
	}
	public void setSetting(OpenPath file, String key, Integer value)
	{
		getPreferences().setSetting("global", key + (file != null ? "_" + file.getPath() : ""), value);
	}

	
	/*
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Logger.LogDebug("<-onCreateView - " + getClassName());
		//CONTENT_FRAGMENT_FREE = false;
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Logger.LogDebug("<-onViewCreated - " + getClassName());
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Logger.LogDebug("->onPause - " + getClassName());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Logger.LogDebug("<-onResume - " + getClassName());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Logger.LogDebug("<-onStart - " + getClassName());
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Logger.LogDebug("->onStop - " + getClassName());
		//CONTENT_FRAGMENT_FREE = true;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logger.LogDebug("->onSaveInstanceState - " + getClassName());
	}
	*/
}
