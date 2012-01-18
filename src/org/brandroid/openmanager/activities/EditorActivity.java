package org.brandroid.openmanager.activities;

import android.content.Intent;
import android.os.Bundle;

public class EditorActivity extends OpenExplorer
{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		intent.setAction(Intent.ACTION_EDIT);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(intent.getData());
		setIntent(intent);
		super.onCreate(savedInstanceState);
	}
}
