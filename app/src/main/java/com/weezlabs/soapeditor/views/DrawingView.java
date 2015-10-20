package com.weezlabs.soapeditor.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.weezlabs.soapeditor.models.DrawingOverlay;

import java.util.LinkedList;

/**
 * View which handles finger drawing
 */
public class DrawingView extends View {
	public static final float STROKE_WIDTH = 32f;

	private Paint paint_;
	private Path path_ = new Path();
	private PointF start_ = new PointF();
	private int color_ = DrawingOverlay.DEFAULT_COLOR;
	private DrawingChangedListener drawingChangedListener_;
	private boolean isDrawing_;

	public DrawingView(Context context) {
		super(context);
		init();
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		paint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint_.setStyle(Paint.Style.STROKE);
		paint_.setColor(color_);
		paint_.setAntiAlias(true);
		paint_.setDither(true);
		paint_.setStrokeJoin(Paint.Join.ROUND);
		paint_.setStrokeCap(Paint.Cap.ROUND);
		paint_.setStrokeWidth(STROKE_WIDTH);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isDrawing_) {
			// We are not drawing
			return false;
		}
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				start_.set(x, y);
				path_.moveTo(x, y);
				break;
			case MotionEvent.ACTION_MOVE:
				if (drawingChangedListener_ != null) {
					drawingChangedListener_.onLineAdded(new float[]{start_.x, start_.y, x, y});
				}
				path_.quadTo(start_.x, start_.y, (x + start_.x) / 2, (y + start_.y) / 2);
				start_.set(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				path_.lineTo(start_.x, start_.y);
				invalidate();
				break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawPath(path_, paint_);
	}

	public void setColor(int color) {
		color_ = color;
		paint_.setColor(color);
		invalidate();
	}

	public void clearDrawing() {
		path_.reset();
		invalidate();
	}

	public interface DrawingChangedListener {
		void onLineAdded(float[] newLine);
	}

	public void setDrawingChangedListener(DrawingChangedListener drawingChangedListener) {
		this.drawingChangedListener_ = drawingChangedListener;
	}

	/**
	 * Creates {@link Path} from {@link LinkedList} with relative to parent coordinates
	 *
	 * @param relativeList every line coordinates like {startX, startY, endX, endY}
	 * @param width        target width
	 * @param height       target height
	 */
	public void setPathFromRelativeList(LinkedList<float[]> relativeList, int width, int height) {
		Path path = new Path();
		for (float[] item : relativeList) {
			path.moveTo(item[0] * width, item[1] * height);
			path.quadTo(item[0] * width, item[1] * height, (item[2] * width + item[0] * width) / 2, (item[3] * height + item[1] * height) / 2);
			path.lineTo(item[2] * width, item[3] * height);
		}
		path_.set(path);
		invalidate();
	}

	/**
	 * Sets up either we are able to draw or not
	 *
	 * @param isEnabled
	 */
	public void enableDrawing(boolean isEnabled) {
		isDrawing_ = isEnabled;
	}
}
