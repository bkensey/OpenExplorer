package org.brandroid.openmanager.util;

import org.brandroid.openmanager.R;
import org.brandroid.utils.Logger;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;


/**
 * This class does most of the work of wrapping the {@link PopupWindow} so it's simpler to use.
 * 
 * @author qberticus
 * 
 */
public class BetterPopupWindow {
	private Context mContext;
	protected final View anchor;
	private final PopupWindow popup;
	private View root;
	private Drawable background = null;
	private final WindowManager windowManager;
	
	private View backgroundView;


	/**
	 * Create a BetterPopupWindow
	 * 
	 * @param anchor
	 *		the view that the BetterPopupWindow will be displaying 'from'
	 */
	public BetterPopupWindow(Context mContext, View anchor, int anim) {
		this.mContext = mContext;
		this.anchor = anchor;
		this.popup = new PopupWindow(mContext);
		this.popup.setAnimationStyle(anim);


		// when a touch even happens outside of the window
		// make the window go away
		this.popup.setTouchInterceptor(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
						BetterPopupWindow.this.popup.dismiss();
						return true;
				}
				return false;
			}
		});


		this.windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		onCreate();
	}


	/**
	 * Anything you want to have happen when created. Probably should create a view and setup the event listeners on
	 * child views.
	 */
	protected void onCreate() {
		
	}


	/**
	 * In case there is stuff to do right before displaying.
	 */
	protected void onShow() {}


	private void preShow() {
		if(this.root == null) {
				throw new IllegalStateException("setContentView was not called with a view to display.");
		}
		onShow();


		//backgroundView.addView(root);
		this.popup.setBackgroundDrawable(new BitmapDrawable());
		if(backgroundView == null)
		{
			backgroundView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.contextmenu_layout, null);
			((ViewGroup)backgroundView.findViewById(android.R.id.widget_frame)).addView(this.root);
		}
		// if using PopupWindow#setBackgroundDrawable this is the only values of the width and hight that make it work
		// otherwise you need to set the background of the root viewgroup
		// and set the popupwindow background to an empty BitmapDrawable
		this.popup.setWidth(400);
		this.popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		this.popup.setTouchable(true);
		this.popup.setFocusable(true);
		this.popup.setOutsideTouchable(true);


		this.popup.setContentView(backgroundView);
	}


	public void setBackgroundDrawable(Drawable background) {
		this.background = background;
	}


	/**
	 * Sets the content view. Probably should be called from {@link onCreate}
	 * 
	 * @param root
	 *		the view the popup will display
	 */
	public void setContentView(View root) {
		this.root = root;
		this.popup.setContentView(root);
	}


	/**
	 * Will inflate and set the view from a resource id
	 * 
	 * @param layoutResID
	 */
	public void setContentView(int layoutResID) {
		LayoutInflater inflator =
					(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.setContentView(inflator.inflate(layoutResID, null));
	}


	/**
	 * If you want to do anything when {@link dismiss} is called
	 * 
	 * @param listener
	 */
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		this.popup.setOnDismissListener(listener);
	}


	/**
	 * Displays like a popdown menu from the anchor view
	 */
	public void showLikePopDownMenu() {
		this.showLikePopDownMenu(0,0);
	}


	/**
	 * Displays like a popdown menu from the anchor view.
	 * 
	 * @param xOffset
	 *		offset in X direction
	 * @param yOffset
	 *		offset in Y direction
	 */
	public void showLikePopDownMenu(int xOffset, int yOffset) {
		this.preShow();

		if(anchor == null)
		{
			xOffset = (windowManager.getDefaultDisplay().getWidth() / 2) - (popup.getWidth() / 2);
			if(popup.getHeight() > 0)
				yOffset = (windowManager.getDefaultDisplay().getHeight() / 2) + (popup.getHeight() / 2);
			else
				yOffset = 100;
			//placeArrow(0, popup.getWidth());
			setViewWidth(backgroundView.findViewById(R.id.space_left), 0);
			setViewWidth(backgroundView.findViewById(R.id.space_right), LayoutParams.FILL_PARENT);
			backgroundView.findViewById(R.id.indicator).setVisibility(View.GONE);
			popup.showAsDropDown(new View(mContext), xOffset, yOffset);
		} else {
			int arrowOffset = 20;
			boolean fromRight = anchor.getLeft() > windowManager.getDefaultDisplay().getWidth() / 2;
			if(fromRight)
			{
				popup.setAnimationStyle(R.style.Animations_GrowFromTopRight);
				arrowOffset *= -1;
			} else {
				popup.setAnimationStyle(R.style.Animations_GrowFromTopLeft);
			}
	
			placeArrow(arrowOffset, this.popup.getWidth() - 46);
			
			if(this.anchor.getY() > windowManager.getDefaultDisplay().getHeight() / 2)
				popup.showAtLocation(anchor, Gravity.BOTTOM, xOffset, yOffset);
			else
				this.popup.showAsDropDown(this.anchor, xOffset, yOffset);

		}
	}

	public void showLikeQuickAction(int arrowOffset) {
		showLikeQuickAction(0, 0, arrowOffset);
	}

	/**
	 * Displays like a QuickAction from the anchor view.
	 */
	public void showLikeQuickAction() {
		this.showLikeQuickAction(0, 0, anchor != null ? anchor.getLeft() : 20);
	}
	
	private void placeArrow(int arrowOffset, int rootWidth)
	{
		if(arrowOffset >= 0)
		{
			setViewWidth(backgroundView.findViewById(R.id.space_left), arrowOffset);
			setViewWidth(backgroundView.findViewById(R.id.space_right), LayoutParams.FILL_PARENT);
		} else {
			arrowOffset *= -1;
			setViewWidth(backgroundView.findViewById(R.id.space_left), rootWidth - arrowOffset - backgroundView.findViewById(R.id.indicator).getWidth());
			setViewWidth(backgroundView.findViewById(R.id.space_right), arrowOffset - 9);
		}
		backgroundView.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
		
	}
	
	private View setViewWidth(View v, int w)
	{
		LayoutParams lp = v.getLayoutParams();
		lp.width = w;
		v.setLayoutParams(lp);
		return v;
	}


	/**
	 * Displays like a QuickAction from the anchor view.
	 * 
	 * @param xOffset
	 *		offset in the X direction
	 * @param yOffset
	 *		offset in the Y direction
	 */
	public void showLikeQuickAction(int xOffset, int yOffset, int arrowOffset) {
		this.preShow();
		
		int[] location = new int[2];
		if(this.anchor == null)
		{
			showLikePopDownMenu(xOffset, yOffset);
			return;
		}
		this.anchor.getLocationOnScreen(location);


		Rect anchorRect =
					new Rect(location[0], location[1], location[0] + this.anchor.getWidth(), location[1]
						+ this.anchor.getHeight());


		this.root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);


		int rootWidth = this.root.getMeasuredWidth();
		int rootHeight = this.root.getMeasuredHeight();


		int screenWidth = this.windowManager.getDefaultDisplay().getWidth();
		int screenHeight = this.windowManager.getDefaultDisplay().getHeight();


		int xPos = ((screenWidth - rootWidth) / 2) + xOffset;
		int yPos = anchorRect.top - rootHeight + yOffset;

		placeArrow(arrowOffset, rootWidth);

		// display on bottom
		if(rootHeight > anchorRect.top) {
				yPos = anchorRect.bottom + yOffset;
				//this.window.setAnimationStyle(anim == R.style.Animations_GrowFromBottom ? R.style.Animations_GrowFromTop : anim);
		}

		int grav = Gravity.NO_GRAVITY; 
		if(anchorRect.top > windowManager.getDefaultDisplay().getHeight() / 2)
			grav = Gravity.BOTTOM;
		
		Logger.LogDebug("Showing Popup @ " + xPos + "," + yPos + " root:" + rootWidth + "x" + rootHeight + " screen:" + screenWidth + "x" + screenHeight + " anchor:" + anchorRect.toString());

		this.popup.showAtLocation(this.anchor, grav, xPos, yPos);
	}


	public void dismiss() {
		this.popup.dismiss();
	}


	public void setTitle(CharSequence title) {
		if(backgroundView != null && backgroundView.findViewById(R.id.contextmenu_title) != null)
		{
			if(title != null && title.length() > 0)
			{
				backgroundView.findViewById(R.id.contextmenu_title).setVisibility(View.VISIBLE);
				((TextView)backgroundView.findViewById(android.R.id.title)).setText(title);
			} else backgroundView.findViewById(R.id.contextmenu_title).setVisibility(View.GONE);
		}
	}


	public void setTitle(int stringId) {
		if(backgroundView != null && backgroundView.findViewById(R.id.contextmenu_title) != null)
		{
			backgroundView.findViewById(R.id.contextmenu_title).setVisibility(View.VISIBLE);
			((TextView)backgroundView.findViewById(android.R.id.title)).setText(stringId);
		}
	}
}