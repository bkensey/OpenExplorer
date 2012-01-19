package org.brandroid.openmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.OpenIntentChooser.IntentSelectedListener;
import org.brandroid.utils.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class IntentManager
{
	public static Intent getIntent(OpenPath file, Context activity)
	{
		return getIntent(file, activity, Intent.ACTION_VIEW, Intent.CATEGORY_DEFAULT);
	}
	public static Intent getIntent(OpenPath file, Context activity, String action, String... categories)
	{
		String name = file.getName();
		final String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
		
		if(file.isDirectory()) return null;
		
		Intent ret = new Intent();
		ret.setAction(action);
		for(String category : categories)
			ret.addCategory(category);
		//ret.putExtra(name, value)
		//ret.set
		
		String mimeType = OpenExplorer.getMimeTypes(activity).getMimeType(name);
		ret.setDataAndType(file.getUri(), mimeType);
    	
		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> lApps = pm.queryIntentActivities(ret, 0);
		//for(ResolveInfo ri : lApps)
		//	Logger.LogDebug("ResolveInfo: " + ri.toString());
		if(lApps.size() == 0)
			ret = null;
		
		/* generic intent */
    	//else ret.setDataAndType(file.getUri(), "application/*");
		return ret;
	}

	public static boolean startIntent(OpenPath file, Context activity) { return startIntent(file, activity, false); } 
	public static boolean startIntent(final OpenPath file, final Context activity, boolean bInnerChooser)
	{
		if(!isIntentAvailable(file, activity))
		{
			Logger.LogWarning("No matching intents!");
			return false;
		}
		Logger.LogDebug("Intents match. Use inner chooser? " + bInnerChooser);
		if(bInnerChooser)
		{
			final Intent intent = getIntent(file, activity);
			Logger.LogDebug("Chooser Intent: " + intent.toString());
			final List<ResolveInfo> mResolves = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if(mResolves.size() == 1)
			{
				ResolveInfo item = mResolves.get(0);
				PackageInfo packInfo = null;
				try {
					activity.getPackageManager().getPackageInfo(item.activityInfo.packageName, PackageManager.GET_INTENT_FILTERS);
					intent.setClassName(packInfo != null ? packInfo.packageName : item.activityInfo.packageName, item.activityInfo.name);
					activity.startActivity(intent);
					return true;
				} catch (NameNotFoundException e) {
					Logger.LogError("Package not found for " + item.activityInfo.toString(), e);
				} catch (ActivityNotFoundException ae) {
					Logger.LogError("Activity not found for " + item.activityInfo.name, ae);
				}
			} else if(mResolves.size() > 1) {
				new OpenIntentChooser(activity, mResolves)
					.setTitle(file.getName() + " (" + intent.getType() + ")")
					.setOnIntentSelectedListener(new IntentSelectedListener() {
						public void onIntentSelected(ResolveInfo item) {
							//activity.showToast("Package? [" + item.activityInfo.packageName + " / " + item.activityInfo.targetActivity + "]");
							PackageInfo packInfo = null;
							try {
								packInfo = activity.getPackageManager().getPackageInfo(item.activityInfo.packageName, PackageManager.GET_INTENT_FILTERS);
								if(packInfo != null && packInfo.activities != null)
								{
									for(ActivityInfo info : packInfo.activities)
									{
										Logger.LogInfo("Activity Info: " + info.toString());
									}
									Logger.LogDebug("Intent chosen: " + item.activityInfo.toString());
								}
								//Intent activityIntent = new Intent();
								intent.setClassName(packInfo != null ? packInfo.packageName : item.activityInfo.packageName, item.activityInfo.name);
								//intent.setData(file.getUri());
								//intent.setType(file.ge)
								activity.startActivity(intent);
							} catch (NameNotFoundException e) {
								Logger.LogError("Package not found for " + item.activityInfo.toString(), e);
							} catch (ActivityNotFoundException ae) {
								Logger.LogError("Activity not found for " + item.activityInfo.name, ae);
							}
						}
					})
					.show();
				return true;
			} else {
				Toast.makeText(activity, activity.getText(R.string.s_error_no_intents), Toast.LENGTH_LONG).show();
			}
		}
		Intent intent = getIntent(file, activity);
		//intent.addFlags(Intent.FL);
		if(intent != null)
		{
			try {
				activity.startActivity(Intent.createChooser(intent, file.getName()));
			} catch(ActivityNotFoundException e) {
				Logger.LogWarning("Couldn't launch intent for " + file.getPath(), e);
				return false;
			}
			return true;
		}
		return false;
	}
	
	public static boolean isIntentAvailable(OpenPath file, Context activity)
	{
		Intent toCheck = getIntent(file, activity);
		if(toCheck == null) return false;
		return activity.getPackageManager().queryIntentActivities(toCheck, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
	}

	public static ResolveInfo getResolveInfo(final OpenPath file, final Context activity)
	{
		return getResolveInfo(getIntent(file, activity), activity);
	}
	public static ResolveInfo getResolveInfo(Intent toCheck, final Context activity)
	{
		if(toCheck == null) return null;
		List<ResolveInfo> lResolves = activity.getPackageManager().queryIntentActivities(toCheck, PackageManager.MATCH_DEFAULT_ONLY);
		if(lResolves.size() > 0)
			return lResolves.get(0);
		return null;
	}
	
	public static Drawable getDefaultIcon(final OpenPath file, final Context activity)
	{
		ResolveInfo info = getResolveInfo(file, activity);
		if(info == null) return null;
		return info.loadIcon(activity.getPackageManager());
	}
	
	public static Drawable getDefaultIcon(final Intent intent, final Context activity)
	{
		ResolveInfo info = getResolveInfo(intent, activity);
		if(info == null) return null;
		return info.loadIcon(activity.getPackageManager());
	}
	

	
}
