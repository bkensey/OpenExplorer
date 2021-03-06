package org.brandroid.openmanager.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenServers;
import org.brandroid.openmanager.fragments.CarouselFragment;
import org.brandroid.openmanager.fragments.ContentFragment;
import org.brandroid.openmanager.fragments.OpenFragment;
import org.brandroid.openmanager.fragments.OpenFragment.OnFragmentDPADListener;
import org.brandroid.openmanager.fragments.OpenPathFragmentInterface;
import org.brandroid.openmanager.fragments.SearchResultsFragment;
import org.brandroid.openmanager.fragments.TextEditorFragment;
import org.brandroid.openmanager.util.ThumbnailCreator;
import org.brandroid.utils.Logger;
import com.viewpagerindicator.TitleProvider;
import com.viewpagerindicator.TabPageIndicator.TabView;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;

public class ArrayPagerAdapter extends FragmentStatePagerAdapter
		implements TitleProvider
{
	//private static Hashtable<OpenPath, Fragment> mPathMap = new Hashtable<OpenPath, Fragment>();
	private ArrayList<OpenFragment> mFrags = new ArrayList<OpenFragment>();
	private OnPageTitleClickListener mListener = null;
	private final Context mContext;

	public ArrayPagerAdapter(FragmentActivity activity, ViewPager pager) {
		super(activity.getSupportFragmentManager());
		mContext = activity;
	}
	
	public interface OnPageTitleClickListener
	{
		public boolean onPageTitleLongClick(int position, View view);
	}
	
	public void setOnPageTitleClickListener(OnPageTitleClickListener l)
	{
		mListener = l;
	}
	
	@Override
	public OpenFragment getItem(int pos) {
		if(mFrags.size() == 0) return null;
		while(pos < 0)
			pos += mFrags.size();
		return mFrags.get(pos % mFrags.size());		
	}

	public OpenFragment getLastItem() {
		return mFrags.get(mFrags.size() - 1);
	}

	@Override
	public int getCount() {
		return mFrags.size();
	}
	
	/*
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return OpenFragment.instantiate(mContext, getItem(position).getClassName(), new Bundle());
	}*/
	
	public synchronized void sort()
	{
		Collections.sort(mFrags, new Comparator<OpenFragment>() {
			@Override
			public int compare(OpenFragment a, OpenFragment b) {
				return a.compareTo(b);
			}
		});
	}
	
	@Override
	public void notifyDataSetChanged() {
		sort();
		super.notifyDataSetChanged();
	}
	
	@Override
	public Parcelable saveState() {
		Parcelable psuper = null;
		try { psuper = super.saveState(); } catch(Exception e) { Logger.LogError("Couldn't save ArrayPagerAdapter state.", e); }
		Bundle state = new Bundle();
		if(psuper != null)
		{
			if(psuper instanceof Bundle)
				state = (Bundle)psuper;
			else
				state.putParcelable("super", psuper);
		}
		List<OpenPath> items = new ArrayList<OpenPath>();
		for(OpenFragment f : mFrags)
		{
			if(f.isDetached()) continue;
			if(!f.isAdded()) continue;
			if(f instanceof OpenPathFragmentInterface)
			{
				OpenPath p = ((OpenPathFragmentInterface)f).getPath();
				Logger.LogDebug("ArrayPagerAdapter.savingState(" + p + ")");
				items.add(p);
			}
		}
		state.putParcelableArray("pages", items.toArray(new OpenPath[items.size()]));
		return state;
	}
	
	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		if(state == null) return;
		Bundle bundle = null;
		if (state instanceof SavedState)
		{
			super.restoreState(state, loader);
			bundle = ((SavedState)state).getBundle();
		} else if(state instanceof Bundle)
			bundle = (Bundle)state;
		else bundle = new Bundle();
		
		bundle.setClassLoader(loader);
		if(bundle.containsKey("super"))
			super.restoreState(bundle.getParcelable("super"), loader);
		if(!bundle.containsKey("pages")) return;
		Logger.LogDebug("ArrayPagerAdapter restore :: " + bundle.getParcelableArray("pages").toString());
		Parcelable[] items = bundle.getParcelableArray("pages");
		mFrags.clear();
		for(int i = 0; i < items.length; i++)
			if(items[i] != null && items[i] instanceof OpenPath)
			{
				OpenPath path = (OpenPath)items[i];
				if(path.isDirectory())
				{
					do {
						add(0, ContentFragment.getInstance(path));
						path = path.getParent();
					} while(path != null);
					notifyDataSetChanged();
				} else if(path.isTextFile() || path.length() < 500000)
					mFrags.add(new TextEditorFragment(path));
			}
	}
	
	@Override
	public int getItemPosition(Object object) {
		if(object instanceof OpenPathFragmentInterface)
		{
			OpenPath tofind = ((OpenPathFragmentInterface)object).getPath();
			if(tofind == null) return super.getItemPosition(object);
			for(int i = 0; i < getCount(); i++)
			{
				OpenFragment f = getItem(i);
				if(f instanceof OpenPathFragmentInterface)
				{
					OpenPath tocheck = ((OpenPathFragmentInterface)f).getPath();
					if(tocheck != null && tocheck.getPath().equals(tofind.getPath()))
						return i;
				}
			}
		}
		return super.getItemPosition(object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		OpenFragment f = getItem(position);
		if(f != null) return f.getTitle();
		return null;
		
		//CharSequence ret = null;
		
		/*
		if(f instanceof TextEditorFragment)
			return ((TextEditorFragment)f).getPath().getName();
		else if(f instanceof SearchResultsFragment)
			return ((SearchResultsFragment)f).getTitle();
		
		OpenPath path = null;
		if(f instanceof ContentFragment)
			path = ((ContentFragment)f).getPath();
		
		if(path == null)
			return ret;
		else if(path instanceof OpenNetworkPath && path.getParent() == null && ((OpenNetworkPath)path).getServersIndex() >= 0)
			return OpenServers.DefaultServers.get(((OpenNetworkPath)path).getServersIndex()).getName();
		else if(path.getName().equals(""))
			return path.getPath();
		else if(path.getParent() == null)
			return path.getName();
		else if(path.isDirectory() && !path.getName().endsWith("/"))
			return path.getName() + "/";
		else return path.getName();
		*/
	}
	
	public boolean checkForContentFragmentWithPath(OpenPath path) {
		if(mFrags == null) return false;
		for (OpenFragment f : mFrags)
			if (f instanceof OpenPathFragmentInterface
					&& ((OpenPathFragmentInterface) f).getPath() != null
					&& ((OpenPathFragmentInterface) f).getPath().equals(path))
				return true;
		return false;
	}

	public boolean add(OpenFragment frag) {
		add(getCount(), frag);
		return true;
	}
	
	public synchronized void add(int index, OpenFragment frag)
	{
		if (frag == null)
		{
			Logger.LogWarning("Cannot add null fragment");
			return;
		}
		if (mFrags.contains(frag))
		{
			Logger.LogInfo("ArrayPagerAdapter already contains fragment");
			return;
		}
		if (frag instanceof ContentFragment
				&& checkForContentFragmentWithPath(((ContentFragment) frag)
						.getPath()))
		{
			Logger.LogInfo("ArrayPagerAdapter already contains Path " + ((ContentFragment)frag).getPath().getPath());
			return;
		}
		//mFrags.remove(frag);
		//mFrags.add(Math.min(0, Math.max(getCount(), index)), frag);
		if(index >= getCount())
			mFrags.add(frag);
		else if(index < 0)
			mFrags.add(0, frag);
		else mFrags.add(index, frag);
		//try {
			//notifyDataSetChanged();
		/*
		}
		catch(IllegalStateException eew) { Logger.LogWarning("Illegal ArrayPagerAdapter", eew); }
		catch(IndexOutOfBoundsException e) {
			Logger.LogWarning("Recovering Adapter", e);
			ArrayList<OpenFragment> recoveryArray = new ArrayList<OpenFragment>(mFrags);
			//recoveryArray.add(Math.max(0, Math.min(getCount() - 1, index)), frag);
			mFrags.clear();
			for(OpenFragment f : recoveryArray)
				mFrags.add(f);
			notifyDataSetChanged();
		}*/
	}

	public boolean remove(OpenFragment frag) {
		return remove(mFrags.indexOf(frag)) != null;
	}
	public synchronized OpenFragment remove(int index) {
		if(index < 0 || index >= mFrags.size()) return null;
		OpenFragment frag = mFrags.remove(index);
		//destroyItem(mViewPager, index, frag);
		return frag;
	}

	public int removeOfType(Class c) {
		int ret = 0;
		for (int i = getCount() - 1; i >= 0; i--) {
			OpenFragment f = getItem(i);
			if (f == null || f.getClass() == null || (f.getClass().equals(c)))
				remove(i);
		}
		notifyDataSetChanged();
		return ret;
	}

	public synchronized void clear() {
		mFrags.clear();
	}

	public List<OpenFragment> getFragments() {
		return mFrags;
	}
	
	public List<OpenFragment> getNonContentFragments() {
		ArrayList<OpenFragment> ret = new ArrayList<OpenFragment>();
		for(OpenFragment f : mFrags)
			if(f != null && !(f instanceof ContentFragment))
				ret.add(f);
		return ret;
	}

	public synchronized void set(int index, OpenFragment frag) {
		if(index < getCount())
			mFrags.set(index, frag);
		else mFrags.add(frag);
		notifyDataSetChanged();
	}

	public int getLastPositionOfType(Class class1) {
		for(int i = getCount() - 1; i > 0; i--)
			if(getItem(i).getClass().equals(class1))
				return i;
		return 0;
	}

	@Override
	public boolean modifyTab(TabView tab, final int position) {
		if(mListener != null)
		{
			tab.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return mListener.onPageTitleLongClick(position, v);
				}
			});
			tab.setLongClickable(true);
		}
		return true;
	}

	public synchronized void replace(OpenFragment old, OpenFragment newFrag) {
		int pos = getItemPosition(old);
		if(pos == -1)
			mFrags.add(newFrag);
		else
			mFrags.set(pos, newFrag);
	}

	public void add(List<OpenFragment> newFrags) {
		for(OpenFragment f : newFrags)
			mFrags.add(f);
	}
}
