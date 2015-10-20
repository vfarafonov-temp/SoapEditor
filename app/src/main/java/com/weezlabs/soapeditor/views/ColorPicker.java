package com.weezlabs.soapeditor.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.weezlabs.soapeditor.R;

/**
 * Created by vfarafonov on 30.09.2015.
 */
public class ColorPicker extends View {
	private static final int[] COLORS = new int[]{0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};
	private static final float DEFAULT_PROGRESS_VALUE = 0.5f;

	private int circleRadius_;
	private Paint backgroundPaint_;
	private int lineThickness;
	private Paint circleFillPaint_;
	private Paint circleStrokePaint_;
	private int circleStrokeThickness_;
	private float currentProgress_;
	private boolean isTouchInProgress_;
	private float circleX_;
	private float circleY_;
	private int fillableWidth_;
	private int currentColor_;
	private ColorChangedListener colorChangedListener_;

	public ColorPicker(Context context) {
		super(context);
		init(null, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs, defStyleAttr);
	}

	/**
	 * Performs initialisation using attributes or default values
	 */
	private void init(AttributeSet attrs, int defStyleAttr) {
		TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPicker, defStyleAttr, 0);
		Resources resources = getContext().getResources();
		circleRadius_ = attributes.getDimensionPixelSize(R.styleable.ColorPicker_picker_radius, resources.getDimensionPixelSize(R.dimen.picker_radius));
		lineThickness = attributes.getDimensionPixelSize(R.styleable.ColorPicker_line_thickness, resources.getDimensionPixelSize(R.dimen.line_thickness));
		circleStrokeThickness_ = attributes.getDimensionPixelSize(R.styleable.ColorPicker_circle_stroke_thickness, resources.getDimensionPixelSize(R.dimen.circle_stroke_thickness));
		currentProgress_ = attributes.getFloat(R.styleable.ColorPicker_progress, DEFAULT_PROGRESS_VALUE);
		attributes.recycle();
		currentColor_ = getColorFromProgress(currentProgress_);

		backgroundPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint_.setStyle(Paint.Style.FILL);

		circleFillPaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		circleFillPaint_.setStyle(Paint.Style.FILL);
		circleFillPaint_.setColor(currentColor_);

		circleStrokePaint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
		circleStrokePaint_.setStyle(Paint.Style.STROKE);
		circleStrokePaint_.setStrokeWidth(circleStrokeThickness_);
		circleStrokePaint_.setARGB(255, 255, 255, 255);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int minWidth = 1;
		int desiredHeight = Math.max(circleRadius_ * 2 + circleStrokeThickness_, lineThickness) + getPaddingBottom() + getPaddingTop();

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		//Measure Width
		if (widthMode == MeasureSpec.EXACTLY) {
			//Must be this size
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			//Can't be bigger than...
			width = Math.min(minWidth, widthSize);
		} else {
			width = minWidth;
		}

		//Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			//Must be this size
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			//Can't be bigger than...
			height = Math.min(desiredHeight, heightSize);
		} else {
			height = desiredHeight;
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int left = getPaddingLeft();
		int right = getMeasuredWidth() - getPaddingRight();
		int top = getPaddingTop();
		int bottom = getMeasuredHeight() - getPaddingBottom();
		int fillableHeight = bottom - top;
		fillableWidth_ = right - left;

		LinearGradient gradientShader_ = new LinearGradient(0, 0, fillableWidth_, 0, COLORS, null, Shader.TileMode.CLAMP);
		backgroundPaint_.setShader(gradientShader_);

		canvas.drawRect(left, (fillableHeight - lineThickness) / 2 + top, right, (fillableHeight + lineThickness) / 2 + top, backgroundPaint_);
		circleX_ = fillableWidth_ * currentProgress_ + left;
		circleY_ = fillableHeight / 2f + top;
		canvas.drawCircle(circleX_, circleY_, circleRadius_, circleFillPaint_);
		canvas.drawCircle(circleX_, circleY_, circleRadius_, circleStrokePaint_);
	}

	private static int getColorFromProgress(float progress) {
		float pos = progress * (COLORS.length - 1);
		int intPos = (int) pos;
		float fraction = pos - intPos;
		int leftColor = COLORS[intPos];
		if (intPos == COLORS.length - 1) {
			intPos--;
		}
		int rightColor = COLORS[intPos + 1];
		int a = average(Color.alpha(leftColor), Color.alpha(rightColor), fraction);
		int r = average(Color.red(leftColor), Color.red(rightColor), fraction);
		int g = average(Color.green(leftColor), Color.green(rightColor), fraction);
		int b = average(Color.blue(leftColor), Color.blue(rightColor), fraction);

		return Color.argb(a, r, g, b);
	}

	/**
	 * Sets new color and notifies listeners
	 */
	public void setColor(int color) {
		float[] colors = new float[3];
		Color.colorToHSV(color, colors);
		currentProgress_ = (360 - colors[0]) / 360;
		currentColor_ = getColorFromProgress(currentProgress_);
		circleFillPaint_.setColor(currentColor_);
		if (colorChangedListener_ != null) {
			colorChangedListener_.onColorChanged(currentColor_);
		}
		invalidate();
	}

	/**
	 * Calculates value between two values with given fraction
	 */
	private static int average(int left, int right, float fraction) {
		return left + Math.round(fraction * (right - left));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (x > (circleX_ - circleRadius_) && x < (circleX_ + circleRadius_)
						&& y > (circleY_ - circleRadius_) && y < (circleY_ + circleRadius_)) {
					isTouchInProgress_ = true;
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (isTouchInProgress_) {
					if (x < getPaddingLeft()) {
						// Left bound
						x = getPaddingLeft();
					} else if (x > getPaddingLeft() + fillableWidth_) {
						// Right bound
						x = getPaddingLeft() + fillableWidth_;
					}
					currentProgress_ = ((float) (x - getPaddingLeft())) / fillableWidth_;
					currentColor_ = getColorFromProgress(currentProgress_);
					circleFillPaint_.setColor(currentColor_);
					if (colorChangedListener_ != null) {
						colorChangedListener_.onColorChanged(currentColor_);
					}
					invalidate();
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (isTouchInProgress_) {
					isTouchInProgress_ = false;
					return true;
				}
				break;
		}
		return super.onTouchEvent(event);
	}

	public interface ColorChangedListener {
		void onColorChanged(int color);
	}

	public void setColorChangedListener(ColorChangedListener colorChangedListener) {
		this.colorChangedListener_ = colorChangedListener;
	}

	public int getSelectedColor() {
		return currentColor_;
	}
}
