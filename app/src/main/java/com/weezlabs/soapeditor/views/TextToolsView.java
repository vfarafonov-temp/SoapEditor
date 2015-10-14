package com.weezlabs.soapeditor.views;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weezlabs.soapeditor.R;

import java.lang.ref.WeakReference;

/**
 * Class for displaying text editing tools
 */
public class TextToolsView extends FrameLayout {
	public static final float FONT_PICKER_HEIGHT_PERCENTAGE = 0.3f;

	private WeakReference<TextView> editableView_;
	private TextToolsListener textToolsListener_;
	private LinearLayout rootLayout_;
	private EditingModes currentMode_;
	private ColorPicker colorPicker_;
	private FontPicker fontPicker_;
	private int fontPickerMinHeight_;

	public TextToolsView(Context context) {
		super(context);
		init();
	}

	public TextToolsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TextToolsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		currentMode_ = EditingModes.TEXT_EDITING;

		inflate(getContext(), R.layout.layout_text_tools, this);

		rootLayout_ = (LinearLayout) findViewById(R.id.root_layout);

		ImageView leftAlignmentImage = (ImageView) findViewById(R.id.img_left_alignment);
		leftAlignmentImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setTextGravity(Gravity.LEFT);
			}
		});

		ImageView centerAlignmentImage = (ImageView) findViewById(R.id.img_center_alignment);
		centerAlignmentImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setTextGravity(Gravity.CENTER_HORIZONTAL);
			}
		});

		ImageView rightAlignmentImage = (ImageView) findViewById(R.id.img_right_alignment);
		rightAlignmentImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setTextGravity(Gravity.RIGHT);
			}
		});

		ImageView backgroundColor = (ImageView) findViewById(R.id.img_background_color);
		backgroundColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMode(EditingModes.BACKGROUND_COLOR_CHANGING);
			}
		});

		ImageView changeFont = (ImageView) findViewById(R.id.img_text_color);
		changeFont.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setMode(EditingModes.FONT_CHANGING);
			}
		});
	}

	public void setMode(EditingModes newMode) {
		switch (newMode) {
			case BACKGROUND_COLOR_CHANGING:
				switchToBackgroundColorChanging();
				break;
			case TEXT_EDITING:
				if (colorPicker_ != null) {
					colorPicker_.setColorChangedListener(null);
					rootLayout_.removeView(colorPicker_);
					colorPicker_ = null;
				}
				if (fontPicker_ != null) {
					fontPicker_.setOnFocusChangeListener(null);
					rootLayout_.removeView(fontPicker_);
					fontPicker_ = null;
				}
				rootLayout_.setOrientation(LinearLayout.HORIZONTAL);
				changeChildVisibility(VISIBLE);
				break;
			case FONT_CHANGING:
				switchToFontCustomizing();
				break;
		}
		EditingModes oldMode = currentMode_;
		currentMode_ = newMode;
		if (textToolsListener_ != null) {
			textToolsListener_.onModeChanged(currentMode_, oldMode);
		}
	}

	/**
	 * Switches layout for font customizing
	 */
	private void switchToFontCustomizing() {
		changeChildVisibility(GONE);
		rootLayout_.setOrientation(LinearLayout.VERTICAL);
		colorPicker_ = new ColorPicker(getContext());
		colorPicker_.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView textView = getEditableTextView();

		// Set up ColorPicker color from text view
		if (textView != null) {
			colorPicker_.setColor(textView.getCurrentTextColor());
		}
		colorPicker_.setColorChangedListener(new ColorPicker.ColorChangedListener() {
			@Override
			public void onColorChanged(int color) {
				if (editableView_ != null) {
					TextView textView = getEditableTextView();
					if (textView != null) {
						textView.setTextColor(color);
					}
				}
				if (textToolsListener_ != null) {
					textToolsListener_.onTextColorChanged(color);
				}
			}
		});
		rootLayout_.addView(colorPicker_);

		// Create and add font picker
		fontPicker_ = new FontPicker(getContext());
		if (fontPickerMinHeight_ == 0) {
			WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Point size = new Point();
			windowManager.getDefaultDisplay().getSize(size);
			fontPickerMinHeight_ = (int) (size.y * FONT_PICKER_HEIGHT_PERCENTAGE);
		}
		fontPicker_.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fontPickerMinHeight_));
		fontPicker_.setFontChangedListener(new FontPicker.FontChangedListener() {
			@Override
			public void onFontChanged(String fontName, Typeface typeface) {
				if (editableView_ != null) {
					TextView textView = getEditableTextView();
					if (textView != null) {
						textView.setTypeface(typeface);
					}
				}
				if (textToolsListener_ != null) {
					textToolsListener_.onFontChanged(fontName, typeface);
				}
			}
		});
		rootLayout_.addView(fontPicker_);
	}

	/**
	 * Switches layout for changing background color
	 */
	private void switchToBackgroundColorChanging() {
		changeChildVisibility(GONE);
		rootLayout_.setOrientation(LinearLayout.HORIZONTAL);
		colorPicker_ = new ColorPicker(getContext());
		colorPicker_.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		TextView textView = getEditableTextView();

		// Set up ColorPicker's color from text view
		if (textView != null) {
			ColorDrawable colorDrawable = null;
			try {
				colorDrawable = (ColorDrawable) textView.getBackground();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (colorDrawable != null) {
				colorPicker_.setColor(colorDrawable.getColor());
			} else {
				textView.setBackgroundColor(colorPicker_.getSelectedColor());
			}
		}
		colorPicker_.setColorChangedListener(new ColorPicker.ColorChangedListener() {
			@Override
			public void onColorChanged(int color) {
				if (editableView_ != null) {
					TextView textView = getEditableTextView();
					if (textView != null) {
						textView.setBackgroundColor(color);
					}
				}
				if (textToolsListener_ != null) {
					textToolsListener_.onBackgroundColorChanged(color);
				}
			}
		});

		rootLayout_.addView(colorPicker_);
	}

	private TextView getEditableTextView() {
		if (editableView_ != null) {
			return editableView_.get();
		}
		return null;
	}

	/**
	 * Changes visibility of all child of root layout
	 */
	private void changeChildVisibility(int visibility) {
		for (int i = 0; i < rootLayout_.getChildCount(); i++) {
			rootLayout_.getChildAt(i).setVisibility(visibility);
		}
	}

	/**
	 * Changes text gravity if view to update was set up
	 */
	private void setTextGravity(int gravity) {
		if (editableView_ != null) {
			TextView editableView = editableView_.get();
			if (editableView != null) {
				editableView.setGravity(gravity);
			}
		}
		if (textToolsListener_ != null) {
			textToolsListener_.onTextAlignmentChanged(gravity);
		}
	}

	/**
	 * Sets up view to update when text parameters changed
	 */
	public void setEditableView(TextView editableView) {
		this.editableView_ = new WeakReference<>(editableView);
	}

	/**
	 * Interface for handling changes applied with TextToolsView
	 */
	public interface TextToolsListener {
		void onTextAlignmentChanged(int newGravity);

		void onModeChanged(EditingModes newMode, EditingModes oldMode);

		void onBackgroundColorChanged(int color);

		void onTextColorChanged(int color);

		void onFontChanged(String fontName, Typeface typeface);
	}

	public void setTextToolsListener(TextToolsListener textToolsListener) {
		this.textToolsListener_ = textToolsListener;
	}

	public enum EditingModes {
		TEXT_EDITING, BACKGROUND_COLOR_CHANGING, FONT_CHANGING
	}
}
