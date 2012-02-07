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

package org.brandroid.openmanager.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import java.util.ArrayList;
import java.util.List;
import org.brandroid.openmanager.R;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.fragments.ContentFragment;

public class OpenExplorerPager
		extends OpenExplorer
	{	

	private ViewPager mViewPager;
	private static MyFragAdapter mViewPagerAdapter;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		mViewPager = (ViewPager)findViewById(R.id.content_frag_pager);
		if(mViewPager != null)
		{
			mViewPagerAdapter = new MyFragAdapter(fragmentManager);
			mViewPager.setAdapter(mViewPagerAdapter);
			mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
				
				public void onPageSelected(int page) {
					if(page == 0)
						updateTitle("");
					else
						updateTitle(((ContentFragment)mViewPagerAdapter.getItem(page)).getPath().getPath());
				}
				
				public void onPageScrolled(int page, float arg1, int arg2) {
					// TODO Auto-generated method stub
					
				}
				
				public void onPageScrollStateChanged(int arg0) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	@Override
	public void changePath(OpenPath path, Boolean addToStack) {
		//super.changePath(path, addToStack);
		if(path == null) path = mLastPath;
		if(!addToStack && path.getPath().equals("/")) return;
		
		if(addToStack || mViewPagerAdapter.getCount() <= 2)
			mViewPagerAdapter.add(new ContentFragment(path));
		else
			((ContentFragment)mViewPagerAdapter.getItem(mViewPagerAdapter.getCount() - 1)).changePath(path);
		
		mViewPager.setCurrentItem(mViewPagerAdapter.getCount() - 1, true);
	}
	
	public static class MyFragAdapter extends FragmentPagerAdapter
	{
		private List<Fragment> mExtraFrags = new ArrayList<Fragment>();

		public MyFragAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			if(pos == 0)
				return mFavoritesFragment;
			else if(pos == 1)
				return mContentFragment;
			else if(pos < mExtraFrags.size() - 2)
				return mExtraFrags.get(pos - 2);
			else return null;
		}

		@Override
		public int getCount() {
			return 2 + mExtraFrags.size();
		}
		
		public boolean add(Fragment frag)
		{
			boolean ret = mExtraFrags.add(frag);
			notifyDataSetChanged();
			return ret;
		}
		
		public boolean remove(Fragment frag)
		{
			boolean ret = mExtraFrags.remove(frag);
			notifyDataSetChanged();
			return ret;
		}
		
		public void clear()
		{
			mExtraFrags.clear();
			notifyDataSetChanged();
		}
		
	}
	

}

