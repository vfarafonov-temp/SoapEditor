package com.weezlabs.soapeditor;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Touch listener for detecting drag, scale and rotate gestures. Performs check for min scale value ({@link ResizeTouchListener#MIN_SCALE}).
 * Uses scale multiplier {@link ResizeTouchListener#SCALE_MULTIPLIER}. For callback use {@link com.weezlabs.soapeditor.ResizeTouchListener.OnChangesGesturesListener}
 */
public class ResizeTouchListener implements View.OnTouchListener {
	public static final int SCALE_MULTIPLIER = 2;
	public static final float MIN_SCALE = 0.2f;

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;

	public boolean needReset_;
	private PointF start_ = new PointF();
	private int mode_ = NONE;
	private int pointerOneId_ = -1;
	private int pointerTwoId_ = -1;
	private OnChangesGesturesListener listener_;
	private PointF firstPointer_ = new PointF();
	private PointF secondPointer_ = new PointF();
	private PointF newFirstPointer_ = new PointF();
	private PointF newSecondPointer_ = new PointF();
	private float distance_;
	private float lastScale_;

	public ResizeTouchListener(OnChangesGesturesListener listener) {
		this.listener_ = listener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				pointerOneId_ = event.getPointerId(event.getActionIndex());
				start_.set(event.getRawX(), event.getRawY());
				if (listener_ != null) {
					listener_.onDragGestureReset();
				}
				mode_ = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				if (pointerTwoId_ == -1) {
					pointerTwoId_ = event.getPointerId(event.getActionIndex());
					mode_ = ZOOM;
					getRawCoordinates(v, event, pointerOneId_, firstPointer_);
					getRawCoordinates(v, event, pointerTwoId_, secondPointer_);
					distance_ = calculateDistance(firstPointer_.x, firstPointer_.y, secondPointer_.x, secondPointer_.y);
					lastScale_ = 1;
					if (listener_ != null) {
						listener_.onRotateGestureReset();
					}
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				int pointerId = event.getPointerId(event.getActionIndex());
				if (pointerId == pointerTwoId_ || pointerId == pointerOneId_) {
					// Released one of pointers which is tracked for actions
					if (pointerId == pointerOneId_) {
						// Released first pointer so we need to use second as first
						pointerOneId_ = pointerTwoId_;
						getRawCoordinates(v, event, pointerOneId_, firstPointer_);
					}
					pointerTwoId_ = -1;
					if (event.getPointerCount() > 2) {
						// It was more than 2 pointers so we can use third instead of second
						for (int i = 0; i < event.getPointerCount(); i++) {
							int tempid = event.getPointerId(i);
							if (tempid != pointerId && tempid != pointerOneId_) {
								// Found third pointer
								pointerTwoId_ = tempid;
								break;
							}
						}
					}
					if (pointerTwoId_ != -1) {
						// We replaced second pointer with another
						getRawCoordinates(v, event, pointerTwoId_, secondPointer_);
						distance_ = calculateDistance(firstPointer_.x, firstPointer_.y, secondPointer_.x, secondPointer_.y);
						if (listener_ != null) {
							listener_.onRotateGestureReset();
						}
					} else {
						mode_ = DRAG;
						needReset_ = true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				mode_ = NONE;
				pointerOneId_ = -1;
				pointerTwoId_ = -1;
				break;
			case MotionEvent.ACTION_MOVE:
				if (needReset_) {
					start_.set(event.getRawX(), event.getRawY());
					if (listener_ != null) {
						listener_.onDragGestureReset();
					}
					needReset_ = false;
				}
				if (mode_ == DRAG) {
					float deltaX = event.getRawX() - start_.x;
					float deltaY = event.getRawY() - start_.y;
					if (listener_ != null) {
						listener_.onDrag((int) deltaX, (int) deltaY);
					}
				} else if (mode_ == ZOOM) {
					getRawCoordinates(v, event, pointerOneId_, newFirstPointer_);
					getRawCoordinates(v, event, pointerTwoId_, newSecondPointer_);
					int angle = (int) calculateAngle(secondPointer_.x, secondPointer_.y, firstPointer_.x, firstPointer_.y, newSecondPointer_.x, newSecondPointer_.y, newFirstPointer_.x, newFirstPointer_.y);
					if (listener_ != null) {
						listener_.onRotate(angle);
					}
					float currentDistance = calculateDistance(newFirstPointer_.x, newFirstPointer_.y, newSecondPointer_.x, newSecondPointer_.y);
					if (Math.abs(currentDistance - distance_) > 0 && listener_ != null) {
						lastScale_ = lastScale_ * currentDistance / distance_;
						lastScale_ = (lastScale_ - 1) * SCALE_MULTIPLIER + 1;
						if (!(lastScale_ * v.getScaleX() < MIN_SCALE)) {
							listener_.onZoom(lastScale_);
						}
					}
				}
				break;
		}
		return true;
	}

	/**
	 * Calculates raw coordinates for pointers from relative to current view coordinates
	 *
	 * @param point Will insert raw coordinates here
	 */
	private void getRawCoordinates(View v, MotionEvent event, int pointerId, PointF point) {
		int pointerIndex = event.findPointerIndex(pointerId);
		int location[] = {0, 0};
		v.getLocationOnScreen(location);
		float x = event.getX(pointerIndex);
		float y = event.getY(pointerIndex);

		double angle = Math.toDegrees(Math.atan2(y, x));
		angle += v.getRotation();

		float length = PointF.length(x, y);

		point.set((float) ((length * Math.cos(Math.toRadians(angle))) + location[0]),
				(float) ((length * Math.sin(Math.toRadians(angle))) + location[1]));
	}

	/**
	 * Calculates rotation angle from previous and current pointers
	 *
	 * @return Angle in degrees
	 */
	private float calculateAngle(float fX_, float fY_, float sX_, float sY_, float nfX_, float nfY_, float nsX_, float nsY_) {
		float angleOne = (float) Math.atan2((fY_ - sY_), (fX_ - sX_));
		float angleTwo = (float) Math.atan2((nfY_ - nsY_), (nfX_ - nsX_));

		return (float) ((Math.toDegrees(angleOne - angleTwo)) % 360);
	}

	/**
	 * Calculates distance between two points by coordinates
	 */
	private float calculateDistance(float x1, float y1, float x2, float y2) {
		float x = x2 - x1;
		float y = y2 - y1;
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * Interface for handling {@link ResizeTouchListener} callbacks
	 */
	public interface OnChangesGesturesListener {
		/**
		 * Place for reset initial dragging params like margins
		 */
		void onDragGestureReset();

		/**
		 * Used for resetting rotation and scale gesture params
		 */
		void onRotateGestureReset();

		/**
		 * Detected drag gesture
		 */
		void onDrag(int dragX, int dragY);

		/**
		 * Detected rotation
		 */
		void onRotate(int degrees);

		/**
		 * Detected zoom gesture
		 */
		void onZoom(float zoom);
	}

	public void setListener(OnChangesGesturesListener listener) {
		listener_ = listener;
	}
}
