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

package org.brandroid.openmanager.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.Paint.Align;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.RejectedExecutionException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenCommand;
import org.brandroid.openmanager.data.OpenCursor;
import org.brandroid.openmanager.data.OpenFTP;
import org.brandroid.openmanager.data.OpenFile;
import org.brandroid.openmanager.data.OpenMediaStore;
import org.brandroid.openmanager.data.OpenNetworkPath;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.data.OpenSFTP;
import org.brandroid.openmanager.data.OpenSMB;
import org.brandroid.openmanager.views.RemoteImageView;
import org.brandroid.utils.ImageUtils;
import org.brandroid.utils.Logger;
import org.brandroid.utils.LruCache;

public class ThumbnailCreator extends Thread {
	//private static HashMap<String, Bitmap> mCacheMap = new HashMap<String, Bitmap>();
	private static LruCache<String, Bitmap> mCacheMap = new LruCache<String, Bitmap>(200);
	private Handler mHandler;
	
	private boolean mStop = false;
	private static int iVideoThumbErrors = 0;
	
	public static boolean useCache = true;
	public static boolean showThumbPreviews = true;
	
	private static Hashtable<String, Integer> fails = new Hashtable<String, Integer>();
	//private static Hashtable<String, Drawable> defaultDrawables = new Hashtable<String, Drawable>();

	public interface OnUpdateImageListener
	{
		void updateImage(Bitmap d);
	}
	
	public static void postImageBitmap(final ImageView image, final Bitmap bmp)
	{
		if(Thread.currentThread().equals(OpenExplorer.UiThread))
			image.setImageBitmap(bmp);
		else
			image.post(new Runnable() {public void run() {
				image.setImageBitmap(bmp);
			}});
	}
	public static void postImageResource(final ImageView image, final int resId)
	{
		if(Thread.currentThread().equals(OpenExplorer.UiThread))
			image.setImageResource(resId);
		else
			image.post(new Runnable() {public void run() {
				image.setImageResource(resId);
			}});
	}
	public static void postImageFromPath(final ImageView mImage, final OpenPath file, final boolean useLarge)
	{
		mImage.post(new Runnable() {
			public void run() {
				mImage.setImageBitmap(getFileExtIcon(file.getExtension(), mImage.getContext(), useLarge));
			}
		});
	}
	public static boolean setThumbnail(final ImageView mImage,
			final OpenPath file, final int mWidth, final int mHeight,
			final OnUpdateImageListener mListener)
	{
		//if(mImage instanceof RemoteImageView)
		//{
		//	return setThumbnail((RemoteImageView)mImage, file, mWidth, mHeight);
		//}
		if(file == null) return false;
		if(mImage == null) return false;
		final String mName = file.getName();
		final String ext = mName.substring(mName.lastIndexOf(".") + 1);
		final String sPath2 = mName.toLowerCase();
		final boolean useLarge = mWidth > 72;
		
		final Context mContext = mImage.getContext().getApplicationContext();
		
		if(!file.isDirectory() && file.isTextFile())
			postImageFromPath(mImage, file, useLarge);
		else if(mImage.getTag() == null)
			postImageResource(mImage, getDefaultResourceId(file, mWidth, mHeight));
		
		if(file.hasThumbnail())
		{
			if(showThumbPreviews && !file.requiresThread())
			{
				Bitmap thumb = //!mCacheMap.containsKey(file.getPath()) ? null :
					getThumbnailCache(mContext, file.getPath(), mWidth, mHeight);
				
				if(thumb == null)
				{
					//mImage.setImageResource(getDefaultResourceId(file, mWidth, mHeight));
					//ThumbnailTask task = new ThumbnailTask();
					//ThumbnailStruct struct = new ThumbnailStruct(file, mListener, mWidth, mHeight);
					//if(mImage.getTag() != null && mImage.getTag() instanceof ThumbnailTask) ((ThumbnailTask)mImage.getTag()).cancel(true);
					
					try {
						if(!fails.containsKey(file.getPath()))
						{
							if(!mCacheMap.containsKey(file.getPath()))
								new Thread(new Runnable(){
									public void run() {
										SoftReference<Bitmap> gen = generateThumb(file, mWidth, mHeight, mContext);
										if(gen != null && gen.get() != null)
											mListener.updateImage(gen.get());
										else Logger.LogWarning("Couldn't generate thumb for " + file.getPath());
									}
								}).start();
							else 
								//new Thread(new Runnable() {public void run() {
										mListener.updateImage(getThumbnailCache(mContext, file.getPath(), mWidth, mHeight));
								//}}).start();
						}
						//mImage.setTag(task);
						//if(struct != null) task.execute(struct);
					} catch(RejectedExecutionException rej) {
						Logger.LogError("Couldn't generate thumbnail because Thread pool was full.", rej);
					}
				}
				if(thumb != null)
				{
					//final BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), thumb);
					//bd.setGravity(Gravity.CENTER);
					//mListener.updateImage(thumb);
					mImage.setImageBitmap(thumb);
					mImage.setTag(file.getPath());
					/*
					mImage.post(new Runnable(){
						public void run() {
							ImageUtils.fadeToDrawable(mImage, bd);	
							mImage.setTag(file.getPath());
						}
					});*/
				}
			
			}
			
		}
		return false;
	}
	public static String getImagePath(ImageView mImage)
	{
		Object ret = mImage.getTag();
		if(ret != null && ret instanceof String) return (String)ret;
		return "";
	}
	public static boolean setThumbnail(final RemoteImageView mImage, OpenPath file, int mWidth, int mHeight)
	{
		final String mName = file.getName();
		final String ext = mName.substring(mName.lastIndexOf(".") + 1);
		final String sPath2 = mName.toLowerCase();
		final boolean useLarge = mWidth > 72;
		if(getImagePath(mImage).equals(file.getPath()))
			return true;
		
		final Context mContext = mImage.getContext().getApplicationContext();
		
		if(!file.isDirectory() && file.isTextFile())
		{
			mImage.post(new Runnable() {
				public void run() {
					mImage.setImageBitmap(getFileExtIcon(ext, mContext, useLarge));
				}
			});
		} else if(!file.isImageFile() || mImage.getDrawable() == null)
			mImage.setImageDrawable(mContext.getResources().getDrawable(getDefaultResourceId(file, mWidth, mHeight)));
		
		if(file.hasThumbnail())
		{
			if(showThumbPreviews && !file.requiresThread())
			{

				Bitmap thumb = ThumbnailCreator.getThumbnailCache(mImage.getContext(), file.getPath(), mWidth, mHeight);
				
				if(thumb == null)
				{
					mImage.setImageDrawable(mContext.getResources().getDrawable(getDefaultResourceId(file, mWidth, mHeight)));
					//mImage.setImageFromFile(file, mWidth, mHeight);
					/*
					ThumbnailTask task = new ThumbnailTask();
					ThumbnailStruct struct = new ThumbnailStruct(file, mImage, mWidth, mHeight);
					if(mImage.getTag() != null && mImage.getTag() instanceof ThumbnailTask)
						((ThumbnailTask)mImage.getTag()).cancel(true);
					
					try {
						mImage.setTag(task);
						if(struct != null)
							task.execute(struct);
					} catch(RejectedExecutionException rej) {
						Logger.LogError("Couldn't generate thumbnail because Thread pool was full.", rej);
					}
					*/
				}
				if(thumb != null)
				{
					BitmapDrawable bd = new BitmapDrawable(mContext.getResources(), thumb);
					bd.setGravity(Gravity.CENTER);
					ImageUtils.fadeToDrawable(mImage, bd);
					mImage.setTag(file.getPath());
				}
			
			}
			
		}
		return false;
	}
	
	public static Bitmap getFileExtIcon(String ext, Context mContext, Boolean useLarge)
	{
		Bitmap src = BitmapFactory.decodeResource(mContext.getResources(), useLarge ? R.drawable.lg_file : R.drawable.sm_file);
		Bitmap b = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint p = new Paint();
		c.drawBitmap(src, 0, 0, p);
		p.setColor(Color.WHITE);
		p.setStyle(Paint.Style.FILL_AND_STROKE);
		p.setTextAlign(Align.CENTER);
		p.setTypeface(Typeface.MONOSPACE);
		float th = (float)src.getHeight() * ((float)(3f / 16f));
		p.setTextSize(th);
		p.setShadowLayer(2.5f, 1, 1, Color.BLACK);
		c.drawText(ext, src.getWidth() / 2, (src.getHeight() / 2) + ((th / 3) * 2), p);
		c.save();
		return b;
	}

	public static Drawable getDefaultDrawable(OpenPath file, int mWidth, int mHeight, Context c)
	{
		if(c != null && c.getResources() != null)
			return c.getResources().getDrawable(getDefaultResourceId(file, mWidth, mHeight));
		else return null;
	}
	public static int getDefaultResourceId(OpenPath file, int mWidth, int mHeight)
	{
		final String mName = file.getName();
		final String ext = mName.substring(mName.lastIndexOf(".") + 1);
		final String sPath2 = mName.toLowerCase();
		final boolean useLarge = mWidth > 72;
		
		if(file.isDirectory()) {
			if(file instanceof OpenSMB)
				return (useLarge ? R.drawable.lg_folder_pipe : R.drawable.sm_folder_pipe);
			if(file instanceof OpenSFTP)
				return (useLarge ? R.drawable.lg_folder_secure : R.drawable.sm_folder_secure);
			if(file.requiresThread())
				return (useLarge ? R.drawable.lg_ftp : R.drawable.sm_ftp);
			if(file.getAbsolutePath().equals("/") && mName.equals(""))
				return (R.drawable.drive);
			else if(sPath2.indexOf("download") > -1)
				return (useLarge ? R.drawable.lg_download : R.drawable.sm_download);
			else if(mName.equals("Photos") || sPath2.indexOf("dcim") > -1 || sPath2.indexOf("pictures") > -1)
				return (useLarge ? R.drawable.lg_photo : R.drawable.sm_photo);
			else if(mName.equals("Videos"))
				return (useLarge ? R.drawable.lg_movie : R.drawable.sm_movie);
			else if(mName.equals("Music"))
				return (useLarge ? R.drawable.lg_music : R.drawable.sm_music);
			else if(sPath2.indexOf("ext") > -1 || sPath2.indexOf("sdcard") > -1 || sPath2.indexOf("microsd") > -1)
				return (useLarge ? R.drawable.lg_sdcard : R.drawable.sm_sdcard);
			else if(sPath2.indexOf("usb") > -1 || sPath2.indexOf("removeable") > -1)
				return (useLarge ? R.drawable.lg_usb : R.drawable.sm_usb);
			else {
				OpenPath[] lists = null;
				if(!file.requiresThread())
					try {
						lists = file.list();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
				if(file.canRead() && lists != null && lists.length > 0)
					return (useLarge ? R.drawable.lg_folder_full : R.drawable.sm_folder_full);
				else
					return (useLarge ? R.drawable.lg_folder : R.drawable.sm_folder);
			}
		} else if(file instanceof OpenCommand) {
				return ((OpenCommand)file).getDrawableId();
		} else if(ext.equalsIgnoreCase("doc") || ext.equalsIgnoreCase("docx")) {
			return (useLarge ? R.drawable.lg_doc : R.drawable.sm_doc);
			
		} else if(ext.equalsIgnoreCase("xls")  || 
				  ext.equalsIgnoreCase("xlsx") ||
				  ext.equalsIgnoreCase("csv") ||
				  ext.equalsIgnoreCase("xlsm")) {
			return (useLarge ? R.drawable.lg_excel : R.drawable.sm_excel);
			
		} else if(ext.equalsIgnoreCase("ppt") || ext.equalsIgnoreCase("pptx")) {
			return (useLarge ? R.drawable.lg_powerpoint : R.drawable.sm_powerpoint);
			
		} else if(ext.equalsIgnoreCase("zip") || ext.equalsIgnoreCase("gzip") || ext.equalsIgnoreCase("rar") || ext.equalsIgnoreCase("gz") || ext.equalsIgnoreCase("7z")) {
			return (useLarge ? R.drawable.lg_zip : R.drawable.sm_zip);
		//} else if(ext.equalsIgnoreCase("apk")) {
		//	return (R.drawable.apk);
			
		} else if(ext.equalsIgnoreCase("pdf")) {
			return (useLarge ? R.drawable.lg_pdf : R.drawable.sm_pdf);
			
		} else if(ext.equalsIgnoreCase("xml") || ext.equalsIgnoreCase("html")) {
			return (useLarge ? R.drawable.lg_xml_html : R.drawable.sm_xml_html);
			
		} else if(ext.equalsIgnoreCase("mp3") || ext.equalsIgnoreCase("wav") ||
				  ext.equalsIgnoreCase("wma") || ext.equalsIgnoreCase("m4p") ||
				  ext.equalsIgnoreCase("m4a") || ext.equalsIgnoreCase("ogg")) {
			return (useLarge ? R.drawable.lg_music : R.drawable.sm_music);
		} else if(file.isImageFile())
			return (useLarge ? R.drawable.lg_photo_50 : R.drawable.sm_photo_50);
		else if(file.isAPKFile())
			return (useLarge ? R.drawable.lg_apk : R.drawable.sm_apk);
		else if(file.isVideoFile())
			return (useLarge ? R.drawable.lg_movie : R.drawable.sm_movie);
		else if(file instanceof OpenFTP && file.isDirectory())
			return (useLarge ? R.drawable.lg_ftp : R.drawable.sm_ftp);
		else if(file instanceof OpenNetworkPath && file.isDirectory())
			return (useLarge ? R.drawable.lg_folder_secure : R.drawable.sm_folder_secure);
		else if(file.isTextFile())
			return (useLarge ? R.drawable.lg_file : R.drawable.sm_file);
		else
			return (useLarge ? R.drawable.lg_unknown : R.drawable.sm_unknown);
	}
	
	public static boolean hasThumbnailCached(OpenPath file, int w, int h)
	{
		return mCacheMap.containsKey(getCacheFilename(file.getPath(), w, h));
	}
	public static Bitmap getThumbnailCache(Context mContext, OpenPath file, int w, int h)
	{
		return getThumbnailCache(mContext, getCacheFilename(file.getPath(), w, h), w, h);
	}
	public static Bitmap getThumbnailCache(Context mContext, String name, int w, int h) {
		String cacheName = getCacheFilename(name, w, h);
		if(!mCacheMap.containsKey(cacheName))
		{
			File f = mContext.getFileStreamPath(cacheName);
			if(f.exists())
				mCacheMap.put(cacheName, BitmapFactory.decodeFile(f.getPath()));
		}
		if(mCacheMap.containsKey(cacheName))
			return mCacheMap.get(cacheName);
		return null;
	}
	private static void putThumbnailCache(String cacheName, Bitmap value)
	{
		//String cacheName = getCacheFilename(name, w, h);
		mCacheMap.put(cacheName, value);
	}
	
	private static String getCacheFilename(String path, int w, int h)
	{
		return w + "x" + h + "_" + path.replaceAll("[^A-Za-z0-9]", "-") + ".jpg";
	}
	
	public static SoftReference<Bitmap> generateThumb(final OpenPath file, int mWidth, int mHeight, Context context) { return generateThumb(file, mWidth, mHeight, true, true, context); }
	public static SoftReference<Bitmap> generateThumb(final OpenPath file, int mWidth, int mHeight, boolean readCache, boolean writeCache, Context mContext)
	{
		if(file.isImageFile())
		{
			//mWidth *= mContext.getResources().getDimension(R.dimen.one_dp);
			//mHeight *= mContext.getResources().getDimension(R.dimen.one_dp);
		}
		final boolean useLarge = mWidth > 72;
		//SoftReference<Bitmap> mThumb = null;
		Bitmap bmp = null;
		//final Handler mHandler = next.Handler;
		
		//readCache = writeCache = true;

		Boolean useGeneric = false;
		String mParent = file.getParent() != null ? file.getParent().getName() : null;
		if(fails == null)
			fails = new Hashtable<String, Integer>();
		if(mParent != null && (fails.containsKey(mParent) && fails.get(mParent) > 10))
			useGeneric = true;
		
		if(mContext != null)
		{
			if(!file.isDirectory() && file.isTextFile())
				return new SoftReference<Bitmap>(getFileExtIcon(file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase(), mContext, mWidth > 72));
			
			bmp = BitmapFactory.decodeResource(mContext.getResources(), getDefaultResourceId(file, mWidth, mHeight));
			if(file.requiresThread() || useGeneric)
				return new SoftReference<Bitmap>(bmp);
		}
		
		String path = file.getPath();
		
		if((file.isImageFile() || file.isVideoFile() || file.isAPKFile()) && (bmp = getThumbnailCache(mContext, path, mWidth, mHeight)) != null)
			return new SoftReference<Bitmap>(bmp);
		
		String mCacheFilename = getCacheFilename(path, mWidth, mHeight);
		
		//we already loaded this thumbnail, just return it.
		if (mCacheMap.get(mCacheFilename) != null) 
			return new SoftReference<Bitmap>(mCacheMap.get(mCacheFilename));
		if(readCache && bmp == null)
			bmp = loadThumbnail(mContext, mCacheFilename);
		
		if(bmp == null && !useGeneric && !OpenExplorer.LOW_MEMORY)
		{
			Boolean valid = false;
			if ((file instanceof OpenMediaStore || file instanceof OpenCursor)
					&& (!fails.containsKey(mParent) || fails.get(mParent) < 10))
			{
				if(!fails.containsKey(mParent))
					fails.put(mParent, 0);
				OpenMediaStore om = (OpenMediaStore)file;
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 1;
				opts.inPurgeable = true;
				opts.outHeight = mHeight;
				//opts.outWidth = mWidth;
				int kind = mWidth > 96 ? MediaStore.Video.Thumbnails.MINI_KIND : MediaStore.Video.Thumbnails.MICRO_KIND;
				try {
					if(om.getParent().getName().equals("Photos"))
						bmp = MediaStore.Images.Thumbnails.getThumbnail(
									mContext.getContentResolver(),
									om.getMediaID(), kind, opts
								);
					else // if(om.getParent().getName().equals("Videos"))
						if(iVideoThumbErrors < 5)
							bmp = MediaStore.Video.Thumbnails.getThumbnail(
									mContext.getContentResolver(),
									om.getMediaID(), kind, opts
								);
				} catch(Exception e) {
					iVideoThumbErrors++;
					Logger.LogWarning("Couldn't get MediaStore thumbnail for " + file.getName(), e);
				}
				if(bmp != null) {
					//Logger.LogDebug("Bitmap is " + bmp.getWidth() + "x" + bmp.getHeight() + " to " + mWidth + "x" + mHeight);
					valid = true;
				} else Logger.LogError("Unable to create MediaStore thumbnail.");
			}
			if (!valid && file.isAPKFile() && !useGeneric)
			{
				//Logger.LogInfo("Getting apk icon for " + file.getName());
				JarFile apk = null;
				InputStream in = null;
				try {
					apk = new JarFile(((OpenFile)file).getFile());
					JarEntry icon = apk.getJarEntry("res/drawable-hdpi/icon.apk");
					if(icon != null && icon.getSize() > 0) {
						in = apk.getInputStream(icon);
						bmp = BitmapFactory.decodeStream(in);
						in.close();
						in = null;
					}
					if(!valid) {
						PackageManager man = mContext.getPackageManager();
						PackageInfo pinfo = man.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
						if(pinfo != null)
						{
							ApplicationInfo ainfo = pinfo.applicationInfo;
							if(Build.VERSION.SDK_INT >= 8)
								ainfo.publicSourceDir = ainfo.sourceDir = file.getPath();
							Drawable mIcon = ainfo.loadIcon(man);
							if(mIcon != null)
								bmp = ((BitmapDrawable)mIcon).getBitmap();
						}
					}
					if(!valid) {
						Logger.LogWarning("Couldn't get icon for " + file.getAbsolutePath());
						String iconName = "icon"; //getIconName(apk, file);
						if(iconName.indexOf(" ") > -1)
							iconName = "icon";
						for(String s : new String[]{"drawable-mdpi","drawable","drawable-hdpi","drawable-ldpi"})
						{
							icon = apk.getJarEntry("res/" + s + "/" + iconName + ".png");
							if(icon != null && icon.getSize() > 0)
							{
								Logger.LogDebug("Found fallback icon (res/" + s + "/" + iconName + ".png)");
								in = apk.getInputStream(icon);
								bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in), mWidth, mHeight, true);
								in.close();
								in = null;
								break;
							}
						}
					}
					if(bmp == null)
						bmp = BitmapFactory.decodeResource(mContext.getResources(), useLarge ? R.drawable.lg_apk : R.drawable.sm_apk);
					else
						saveThumbnail(mContext, mCacheFilename, bmp);
				} catch(IOException ix) {
					Logger.LogError("Invalid APK: " + file.getPath(), ix);
				}
				finally {
					try {
						if(apk != null)
							apk.close();
					} catch(IOException nix) {
						Logger.LogError("Error closing APK while handling invalid APK exception.", nix);
					}
					try {
						if(in != null)
							in.close();
					} catch(IOException nix) {
						Logger.LogError("Error closing input stream while handling invalid APK exception.", nix);
					}
				}
			} else if (!valid && file.isImageFile() && !useGeneric) {
				//mHeight *= 2; mWidth *= 2;
				//long len_kb = file.length() / 1024;
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(file.getPath(), options);
				options.inSampleSize = Math.min(options.outWidth / mWidth, options.outHeight / mHeight);
				options.inJustDecodeBounds = false;
				options.inPurgeable = true;
				bmp = BitmapFactory.decodeFile(file.getPath(), options);
			} else if (bmp == null && file.getClass().equals(OpenFile.class))
			{
				useGeneric = true;
				if(file.isDirectory())
					bmp = BitmapFactory.decodeResource(mContext.getResources(), useLarge ? R.drawable.lg_folder : R.drawable.sm_folder);
				else
					bmp = BitmapFactory.decodeResource(mContext.getResources(), useLarge ? R.drawable.lg_unknown : R.drawable.sm_unknown);
			}
		}
		
		if(bmp != null) bmp = cropBitmap(bmp, mWidth, mHeight);
		
		if(bmp != null)
		{
			if(writeCache && !useGeneric) saveThumbnail(mContext, mCacheFilename, bmp);
			mCacheMap.put(mCacheFilename, bmp);
		} else {
			fails.put(mParent, fails.containsKey(mParent) ? fails.get(mParent) + 1 : 1);
			rememberFailure(file.getPath());
		}
		//Logger.LogDebug("Created " + bmp.getWidth() + "x" + bmp.getHeight() + " thumb (" + mWidth + "x" + mHeight + ")");
		return new SoftReference<Bitmap>(bmp);
	}
	
	private static void rememberFailure(String path)
	{
		fails.put(path, 1);
	}
	
	private static Bitmap cropBitmap(Bitmap src, int dw, int dh)
	{
		int sw = src.getWidth(),
			sh = src.getHeight();
		if(sw < dw && sh < dh) return src;
		//return Bitmap.createBitmap(src, (sw / 2) - (dh / 2), (sh / 2) - (dh / 2), dw, dh, new Matrix(), false);
		Matrix m = new Matrix();
		float scale = Math.max(1, Math.min((float)dh / (float)sh, (float)dw / (float)sw));
		int ox = 0, oy = 0;
		if(sw - dw < sh - dh) // More Width needs to be cropped
		{
			ox = (sw / 2) - (dw / 2);
			if(ox >= 0)
				dw = Math.min(dw, Math.max(sw, sw - ox));
			else {
				ox = 0;
				oy = (sh / 2) - (dh / 2);
				dh = Math.min(dh, Math.max(sh, sw - oy));
			}
		} else {
			oy = (sh / 2) - (dh / 2);
			if(oy >= 0)
				dh = Math.min(dh, Math.max(sh, sw - oy));
			else {
				oy = 0;
				ox = (sw / 2) - (dw / 2);
				dw = Math.min(dw, Math.max(sw, sw - ox));
			} 
		}
		Logger.LogDebug("cropBitmap:(" + sw + "x" + sh + "):(" + ox + "," + oy + ":" + dw + "x" + dh + ") @ " + scale);
		m.postScale(scale, scale);
		return Bitmap.createBitmap(src,
				Math.max(0,  ox), Math.max(0,  oy),
				Math.min(sw, dw), Math.min(sh, dh),
				m, scale != 1.0); 
	}
	
	private static Bitmap loadThumbnail(Context mContext, String file)
	{
		if(mContext != null)
			return BitmapFactory.decodeFile(file);
		return null;
	}
	
	private static void saveThumbnail(Context mContext, String file, Bitmap bmp)
	{
		//Logger.LogVerbose("Saving thumb for " + file);
		FileOutputStream os = null;
		try {
			os = mContext.openFileOutput(file, 0);
			bmp.compress(CompressFormat.JPEG, 98, os);
		} catch(IOException e) {
			Logger.LogError("Unable to save thumbnail for " + file, e);
		} finally {
			if(os != null)
				try {
					os.close();
				} catch (IOException e) { }
		}
	}
	
	
	private void sendThumbBack(Context mContext, SoftReference<Bitmap> mThumb, String path)
	{
		final Bitmap d = mThumb.get();
		new BitmapDrawable(mContext.getResources(), d).setGravity(Gravity.CENTER);
		mCacheMap.put(path, d);
		
		mHandler.post(new Runnable() {
			
			public void run() {
				Message msg = mHandler.obtainMessage();
				msg.obj = d;
				msg.sendToTarget();
			}
		});
	}

	public static void flushCache(Context mContext, boolean deleteFiles) {
		//Logger.LogInfo("Flushing" + mCacheMap.size() + " from memory & " + mContext.fileList().length + " from disk.");
		mCacheMap.clear();
		if(!deleteFiles) return;
		for(String s : mContext.fileList())
			if(!s.toLowerCase().endsWith(".json"))
				mContext.deleteFile(s);
	}
	

	
	

}
