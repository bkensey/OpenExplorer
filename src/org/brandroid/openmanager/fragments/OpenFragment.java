package org.brandroid.openmanager.fragments;

import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenClipboard;
import org.brandroid.openmanager.util.EventHandler;
import org.brandroid.openmanager.util.FileManager;
import org.brandroid.openmanager.util.OpenExplorerOperator;
import org.brandroid.utils.Logger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class OpenFragment
			extends Fragment
			implements View.OnClickListener, View.OnLongClickListener
{
	//public static boolean CONTENT_FRAGMENT_FREE = true;
	//public boolean isFragmentValid = true;
	
	public String getClassName()
	{
		return this.getClass().getSimpleName();
	}
	
	public static void setAlpha(float alpha, View... views)
	{
		for(View kid : views)
			setAlpha(kid, alpha);
	}
	public static void setAlpha(View v, float alpha)
	{
		if(v == null) return;
		if(!OpenExplorer.BEFORE_HONEYCOMB)
			v.setAlpha(alpha);
		else if(v instanceof ImageView)
			((ImageView)v).setAlpha((int)(255 * alpha));
		else if(v instanceof TextView)
			((TextView)v).setTextColor(((TextView)v).getTextColors().withAlpha((int)(255 * alpha)));
	}
	public static void setAlpha(float alpha, View root, int... ids)
	{
		for(int id : ids)
			setAlpha(root.findViewById(id), alpha);
	}
	
	public OpenExplorerOperator getExplorer() { return (OpenExplorerOperator)getActivity(); }
	public OpenFragmentActivity getExplorerActivity() { return (OpenFragmentActivity)getActivity(); }
	public static EventHandler getEventHandler() { return OpenExplorer.getEventHandler(); }
	public static FileManager getFileManager() { return OpenExplorer.getFileManager(); }
	protected OpenClipboard getClipboard() {
		return OpenExplorer.getClipboard();
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
	public void onCreate(Bundle savedInstanceState) {
		Logger.LogDebug("<-onCreate - " + getClassName());
		//CONTENT_FRAGMENT_FREE = false;
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		Logger.LogDebug("->onDestroy - " + getClassName());
		super.onDestroy();
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
