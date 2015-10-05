package com.weezlabs.soapeditor;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by vfarafonov on 05.10.2015.
 */
public class ResizeTouchListener implements View.OnTouchListener {
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;

	public boolean needReset_;
	private PointF lastEvent_ = new PointF();
	;
	private PointF start_ = new PointF();
	private int initialMarginRight_;
	private int initialMarginBottom_;
	private int initialMarginLeft_;
	private int initialMarginTop_;
	private int mode_ = NONE;
	private FrameLayout.LayoutParams textParams_;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				textParams_ = (FrameLayout.LayoutParams) v.getLayoutParams();
				resetVariables(event);
				mode_ = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				mode_ = ZOOM;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode_ = DRAG;
				needReset_ = true;
				break;
			case MotionEvent.ACTION_UP:
				mode_ = NONE;
				break;
			case MotionEvent.ACTION_MOVE:
				if (needReset_) {
					resetVariables(event);
					needReset_ = false;
				}
				if (mode_ == DRAG) {
					float deltaX = event.getRawX() - start_.x;
					float deltaY = event.getRawY() - start_.y;
					textParams_.leftMargin = (int) (initialMarginLeft_ + deltaX);
					textParams_.topMargin = (int) (initialMarginTop_ + deltaY);
					textParams_.rightMargin = (int) (initialMarginRight_ - deltaX);
					textParams_.bottomMargin = (int) (initialMarginBottom_ - deltaY);
					v.setLayoutParams(textParams_);
					lastEvent_.set(event.getRawX(), event.getRawY());
				}
				break;
		}
		return true;
	}

	/**
	 * Sets up initial variables
	 */
	private void resetVariables(MotionEvent event) {
		start_.set(event.getRawX(), event.getRawY());
		initialMarginTop_ = textParams_.topMargin;
		initialMarginLeft_ = textParams_.leftMargin;
		initialMarginBottom_ = textParams_.bottomMargin;
		initialMarginRight_ = textParams_.rightMargin;
		lastEvent_.set(event.getRawX(), event.getRawY());
	}

	private float calculateDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}
}
