package com.weezlabs.soapeditor;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

	private static final String STATE_TEXT_OVERLAY = "STATE_TEXT_OVERLAY";
	private View.OnClickListener changeTextClickListener_;
	private FrameLayout contentLayout_;
	private View contentView_;
	private EditText overlayTextView_;
	private ColorPicker colorPicker_;
	private FontPicker fontPicker_;

	View.OnTouchListener pickersTouchListener_ = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.requestFocus();
			hideKeyboard(v.getWindowToken());
			return false;
		}
	};
	private Button resizeTextButton_;
	private boolean isResizing_;
	private TextOverlay textOverlay_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			textOverlay_ = new TextOverlay();
		} else {
			textOverlay_ = savedInstanceState.getParcelable(STATE_TEXT_OVERLAY);
		}
		contentLayout_ = (FrameLayout) findViewById(R.id.content_layout);
		contentView_ = findViewById(R.id.iv_background_image);
		colorPicker_ = (ColorPicker) findViewById(R.id.color_picker);
		colorPicker_.setOnTouchListener(pickersTouchListener_);
		colorPicker_.setColorChangedListener(new ColorPicker.ColorChangedListener() {
			@Override
			public void onColorChanged(int color) {
				if (overlayTextView_ != null) {
					overlayTextView_.setTextColor(color);
					textOverlay_.setColor(color);
				}
			}
		});
		fontPicker_ = (FontPicker) findViewById(R.id.font_picker);
		fontPicker_.setOnTouchListener(pickersTouchListener_);
		fontPicker_.setFontChangedListener(new FontPicker.FontChangedListener() {
			@Override
			public void onFontChanged(String fontName, Typeface typeface) {
				if (overlayTextView_ != null) {
					overlayTextView_.setTypeface(typeface);
					textOverlay_.setFont(fontName);
				}
			}
		});

		resizeTextButton_ = (Button) findViewById(R.id.btn_move_text);
		resizeTextButton_.setOnTouchListener(pickersTouchListener_);
		resizeTextButton_.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isResizing_) {
					resizeTextButton_.setText(getBaseContext().getResources().getString(R.string.start_resizing));
					if (overlayTextView_ != null) {
						overlayTextView_.setOnTouchListener(null);
					}
				} else {
					resizeTextButton_.setText(getBaseContext().getResources().getString(R.string.stop_resizing));
					if (overlayTextView_ != null) {
						overlayTextView_.setOnTouchListener(resizeTouchListener_);
					}
				}
				isResizing_ = !isResizing_;
			}
		});

		changeTextClickListener_ = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (overlayTextView_ == null) {
					initiateOverlayEditText();
				}
				overlayTextView_.requestFocus();
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(overlayTextView_.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
			}
		};
		Button changeTextButton = (Button) findViewById(R.id.btn_change_text);
		changeTextButton.setOnClickListener(changeTextClickListener_);
	}

	private ResizeTouchListener resizeTouchListener_ = new ResizeTouchListener(new ResizeTouchListener.OnChangesGesturesListener() {
		public float initialScale_;
		public float initialRotation_;
		private int initialMarginLeft_;
		private int initialMarginTop_;
		private FrameLayout.LayoutParams textParams_;

		@Override
		public void onDragGestureReset() {
			if (overlayTextView_ != null) {
				textParams_ = (FrameLayout.LayoutParams) overlayTextView_.getLayoutParams();
				initialMarginTop_ = textParams_.topMargin;
				initialMarginLeft_ = textParams_.leftMargin;
			}
		}

		@Override
		public void onRotateGestureReset() {
			initialRotation_ = overlayTextView_.getRotation();
			initialScale_ = overlayTextView_.getScaleX();
		}

		@Override
		public void onDrag(int dragX, int dragY) {
			if (overlayTextView_ != null && textParams_ != null) {
				textParams_.leftMargin = initialMarginLeft_ + dragX;
				textParams_.topMargin = initialMarginTop_ + dragY;
				overlayTextView_.setLayoutParams(textParams_);
				textOverlay_.setLocation(new float[]{((float) textParams_.leftMargin) / contentView_.getWidth(), ((float) textParams_.topMargin) / contentView_.getHeight()});
			}
		}

		@Override
		public void onRotate(int degrees) {
			if (overlayTextView_ != null) {
				overlayTextView_.setRotation(initialRotation_ - degrees);
				textOverlay_.setRotation(initialRotation_ - degrees);
			}
		}

		@Override
		public void onZoom(float zoom) {
			if (overlayTextView_ != null) {
				overlayTextView_.setScaleX(initialScale_ * zoom);
				overlayTextView_.setScaleY(initialScale_ * zoom);
				textOverlay_.setScale(initialScale_ * zoom);
			}
		}
	});

	private void hideKeyboard(IBinder windowToken) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextOverlay.isDefault(textOverlay_) && overlayTextView_ == null) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					initiateOverlayEditText();
				}
			}, 500);
		}
	}

	/**
	 * Creates and adds EditText for text overlapping
	 */
	private void initiateOverlayEditText() {
		overlayTextView_ = TextOverlay.createOverlayEditText(this, textOverlay_, contentView_);
		contentLayout_.addView(overlayTextView_);
		overlayTextView_.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				textOverlay_.setText(s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		colorPicker_.setColor(textOverlay_.getColor());
		fontPicker_.setSelectedFont(textOverlay_.getFont());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_TEXT_OVERLAY, textOverlay_);
	}
}
