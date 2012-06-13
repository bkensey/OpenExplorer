package org.brandroid.openmanager.activities;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockFragment;

import java.io.IOException;
import java.util.Locale;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.adapters.IconContextMenu;
import org.brandroid.openmanager.adapters.IconContextMenu.IconContextItemSelectedListener;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenPath.OpenContentUpdater;
import org.brandroid.openmanager.interfaces.OpenContextProvider;
import org.brandroid.openmanager.util.MimeTypeParser;
import org.brandroid.openmanager.util.MimeTypes;
import org.brandroid.utils.Logger;
import org.brandroid.utils.MenuBuilder;
import org.brandroid.utils.Preferences;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public abstract class OpenFragmentActivity
			extends SherlockFragmentActivity
			implements View.OnClickListener, View.OnLongClickListener
				, OpenContextProvider
{
	//public static boolean CONTENT_FRAGMENT_FREE = true;
	//public boolean isFragmentValid = true;
	public static Thread UiThread = Thread.currentThread();
	private Preferences mPreferences = null;
	private final static boolean DEBUG = OpenExplorer.IS_DEBUG_BUILD && true;
	
	public String getClassName()
	{
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Logger.LogDebug(getClassName() + ".onAttachToWindow()");
	}
	
	public void onClick(View v) {
		if(DEBUG)
			Logger.LogDebug(getClassName() + ".onClick(0x" + Integer.toHexString(v.getId()) + ") - " + v.toString());
	}
	
	public void onClick(int id) {
		if(DEBUG)
			Logger.LogDebug("View onClick(0x" + Integer.toHexString(id) + ") / " + getClassName());
	}
	
	public boolean onClick(int id, MenuItem item, View from)
	{
		return false;
	}
	
	public boolean onLongClick(View v) {
		if(DEBUG)
			Logger.LogDebug("View onLongClick(0x" + Integer.toHexString(v.getId()) + ") - " + v.toString());
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(DEBUG)
			Logger.LogDebug("Menu selected(0x" + Integer.toHexString(item.getItemId()) + ") - " + item.toString());
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
		super.onCreate(savedInstanceState);
		} catch(Exception e) { 
			Logger.LogError("Error while creating.", e);
		}
		Logger.LogDebug("<-onCreate - " + getClassName());
		//CONTENT_FRAGMENT_FREE = false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Logger.LogDebug("->onDestroy - " + getClassName());
	}

	/**
	 * Set Application specific language. This has no effect on the system language.
	 * @param context
	 * @param language 2 Letter Language Code
	 */
    public static void setLanguage(Context context, String language) {
        Locale locale;
        if (language == null || language.equals("")) {
            locale = Locale.getDefault();
        } else if (language.length() == 5 && language.charAt(2) == '_') {
            // language is in the form: en_US
            locale = new Locale(language.substring(0, 2), language.substring(3));
            language = language.substring(0, 2);
        } else {
            locale = new Locale(language);
        }
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
        new Preferences(context).setSetting("global", "pref_language", language);
    }
	
	@SuppressWarnings("deprecation")
	public int getWindowWidth() {
		if(Build.VERSION.SDK_INT > 13)
		{
			Point p = new Point();
			getWindowManager().getDefaultDisplay().getSize(p);
			return p.x;
		} else return getWindowManager().getDefaultDisplay().getWidth();
	}
	
	public Preferences getPreferences() {
		if(mPreferences == null)
			mPreferences = new Preferences(getApplicationContext());
		return mPreferences;
	}

	public String getSetting(OpenPath file, String key, String defValue)
	{
		return getPreferences().getSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), defValue);
	}
	public Boolean getSetting(OpenPath file, String key, Boolean defValue)
	{
		return getPreferences().getSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), defValue);
	}
	public Integer getSetting(OpenPath file, String key, Integer defValue)
	{
		return getPreferences().getSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), defValue);
	}
	public Float getSetting(OpenPath file, String key, Float defValue)
	{
		return getPreferences().getSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), defValue);
	}
	public void setSetting(OpenPath file, String key, String value)
	{
		getPreferences().setSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), value);
	}
	public void setSetting(OpenPath file, String key, Boolean value)
	{
		getPreferences().setSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), value);
	}
	public void setSetting(OpenPath file, String key, Integer value)
	{
		getPreferences().setSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), value);
	}
	public void setSetting(OpenPath file, String key, Float value)
	{
		getPreferences().setSetting(file == null ? "global" : "views", key + (file != null ? "_" + file.getPath() : ""), value);
	}
	public void setSetting(String globalKey, Boolean value)
	{
		setSetting("global", globalKey, value);
	}
	public void setSetting(String globalKey, Integer value)
	{
		setSetting("global", globalKey, value);
	}
	public void setSetting(String globalKey, String value)
	{
		setSetting("global", globalKey, value);
	}
	public void setSetting(String file, String key, Boolean value)
	{
		getPreferences().setSetting(file, key, value);
	}
	public void setSetting(String file, String key, Integer value)
	{
		getPreferences().setSetting(file, key, value);
	}
	public void setSetting(String file, String key, String value)
	{
		getPreferences().setSetting(file, key, value);
	}
	
	public static boolean isBlackBerry() {
		return Build.MANUFACTURER.trim().equalsIgnoreCase("rim") ||
				Build.MODEL.toLowerCase().indexOf("blackberry") > -1;
	}
	public boolean isGTV() { return isGTV(this); }
	public static boolean isGTV(Context context) { return context.getPackageManager().hasSystemFeature("com.google.android.tv"); }
	public void showToast(final CharSequence message)  {
		showToast(message, Toast.LENGTH_LONG);
	}
	public void showToast(final int iStringResource) { showToast(getResources().getString(iStringResource)); }
	public void showToast(final CharSequence message, final int toastLength)  {
		Logger.LogInfo("Made toast: " + message);
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getBaseContext(), message, toastLength).show();
			}
		});
	}
	public void showToast(final int resId, final int length)  {
		showToast(getString(resId), length);
	}

	@Override
	public Context getContext() {
		return this;
	}
	@Override
	public abstract void onChangeLocation(OpenPath path);

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
