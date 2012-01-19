package org.brandroid.openmanager.util;

import java.util.List;

import org.brandroid.openmanager.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OpenIntentAdapter extends BaseAdapter
{
	private android.content.Context mExplorer;
	private List<ResolveInfo> mData;
	
	public OpenIntentAdapter(Context explorer, List<ResolveInfo> queriedIntents)
	{
		mExplorer = explorer;
		mData = queriedIntents;
	}

	public int getCount() {
		return mData.size();
	}

	public ResolveInfo getItem(int position) {
		return mData.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ResolveInfo item = mData.get(position);
		
        PackageManager pm = mExplorer.getPackageManager();

        TextView res = (TextView)convertView;
        if (res == null) {
        	res = (TextView) LayoutInflater.from(mExplorer).inflate(android.R.layout.select_dialog_item, null);
        }
        
        res.setTag(item);
        res.setText(item.loadLabel(pm));
        Drawable d = (Drawable)item.loadIcon(pm);
        if(BitmapDrawable.class.equals(d))
        	((BitmapDrawable)d).setGravity(Gravity.CENTER);
        res.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        
		return res;
	}
}