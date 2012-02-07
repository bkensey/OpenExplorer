package org.brandroid.openmanager.util;

import org.brandroid.openmanager.data.OpenPath;

public interface OpenExplorerOperator {
	public void changePath(final OpenPath path, Boolean addToStack);
	public void onChangeLocation(final OpenPath path);
	public void refreshBookmarks();
	public int getViewMode();
	public void addBookmark(final OpenPath path);
	public void updateTitle(final String path);
	public void editFile(final OpenPath path);
	public void showFileInfo(final OpenPath path);
}
