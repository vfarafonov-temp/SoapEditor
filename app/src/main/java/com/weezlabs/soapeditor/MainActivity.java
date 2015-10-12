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

import com.weezlabs.soapeditor.models.DrawingOverlay;
import com.weezlabs.soapeditor.models.TextOverlay;
import com.weezlabs.soapeditor.views.ColorPicker;
import com.weezlabs.soapeditor.views.DrawingView;
import com.weezlabs.soapeditor.views.FontPicker;

public class MainActivity extends AppCompatActivity {

	private static final String STATE_TEXT_OVERLAY = "STATE_TEXT_OVERLAY";
	private static final String STATE_DRAWING_OVERLAY = "STATE_DRAWING_OVERLAY";
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
	private boolean isEditingText_;
	private Button changeDrawingButton_;
	private Button exitButton_;
	private Button changeTextButton_;
	private Button clearDrawingButton_;
	private DrawingView drawingView_;
	private DrawingOverlay drawingOverlay_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			textOverlay_ = new TextOverlay();
			drawingOverlay_ = new DrawingOverlay();
		} else {
			textOverlay_ = savedInstanceState.getParcelable(STATE_TEXT_OVERLAY);
			drawingOverlay_ = savedInstanceState.getParcelable(STATE_DRAWING_OVERLAY);
		}
		contentLayout_ = (FrameLayout) findViewById(R.id.content_layout);
		contentView_ = findViewById(R.id.iv_background_image);
		colorPicker_ = (ColorPicker) findViewById(R.id.color_picker);

		fontPicker_ = (FontPicker) findViewById(R.id.font_picker);
		fontPicker_.setOnTouchListener(pickersTouchListener_);
		fontPicker_.setFontChangedListener(new FontPicker.FontChangedListener() {
			@Override
			public void onFontChanged(String fontName, Typeface typeface) {
				if (overlayTextView_ != null) {
					overlayTextView_.setTypeface(typeface);
					textOverlay_.setFont(fontName);
					setTextOverlayEditable(false);
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

		changeDrawingButton_ = (Button) findViewById(R.id.btn_change_drawing);
		exitButton_ = (Button) findViewById(R.id.btn_exit);

		changeTextClickListener_ = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isEditingText_) {
					if (isResizing_) {
						resizeTextButton_.callOnClick();
					}
					if (overlayTextView_ == null) {
						initiateOverlayEditText();
					}
					setTextOverlayEditable(true);
					overlayTextView_.requestFocus();
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.toggleSoftInputFromWindow(overlayTextView_.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
				} else {
					if (overlayTextView_ != null) {
						setTextOverlayEditable(true);
					}
					isEditingText_ = true;
					showEditTextLayout();
				}
			}
		};
		changeTextButton_ = (Button) findViewById(R.id.btn_change_text);
		changeTextButton_.setOnClickListener(changeTextClickListener_);

		changeDrawingButton_.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (drawingView_ == null) {
					initiateOverlayDrawing();
				}
				showEditDrawingLayout();
			}
		});

		exitButton_.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isEditingText_ = false;
				if (isResizing_) {
					resizeTextButton_.callOnClick();
				}
				showBasicButtons();
				if (overlayTextView_ != null) {
					setTextOverlayEditable(false);
					contentView_.requestFocus();
				}
			}
		});

		clearDrawingButton_ = (Button) findViewById(R.id.btn_clear_drawing);
		clearDrawingButton_.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawingOverlay_.clearDrawing();
				if (drawingView_ != null) {
					drawingView_.clearDrawing();
				}
			}
		});
	}

	private void setTextOverlayEditable(boolean isEditable) {
		overlayTextView_.setFocusable(isEditable);
		overlayTextView_.setFocusableInTouchMode(isEditable);
		overlayTextView_.setClickable(isEditable);
	}

	private void initiateOverlayDrawing() {
		drawingView_ = new DrawingView(this);
		drawingView_.setDrawingChangedListener(new DrawingView.DrawingChangedListener() {
			@Override
			public void onLineAdded(float[] newLine) {
				drawingOverlay_.addToPath(new float[]{
						newLine[0] / drawingView_.getWidth(),
						newLine[1] / drawingView_.getHeight(),
						newLine[2] / drawingView_.getWidth(),
						newLine[3] / drawingView_.getHeight()
				});
			}
		});
		drawingView_.setColor(drawingOverlay_.getColor());
		contentLayout_.addView(drawingView_);
		drawingView_.setPathFromRelativeList(drawingOverlay_.getPathList(), contentView_.getWidth(), contentView_.getHeight());
	}

	private void showBasicButtons() {
		if (drawingView_ != null) {
			drawingView_.enableDrawing(false);
		}
		changeTextButton_.setVisibility(View.VISIBLE);
		clearDrawingButton_.setVisibility(View.GONE);
		exitButton_.setVisibility(View.GONE);
		changeDrawingButton_.setVisibility(View.VISIBLE);
		resizeTextButton_.setVisibility(View.GONE);
		colorPicker_.setVisibility(View.GONE);
		fontPicker_.setVisibility(View.GONE);
	}

	private void showEditDrawingLayout() {
		drawingView_.enableDrawing(true);
		changeDrawingButton_.setVisibility(View.GONE);
		changeTextButton_.setVisibility(View.GONE);
		clearDrawingButton_.setVisibility(View.VISIBLE);
		exitButton_.setVisibility(View.VISIBLE);

		colorPicker_.setVisibility(View.VISIBLE);
		colorPicker_.setOnTouchListener(null);
		colorPicker_.setColor(drawingOverlay_.getColor());
		colorPicker_.setColorChangedListener(new ColorPicker.ColorChangedListener() {
			@Override
			public void onColorChanged(int color) {
				if (drawingView_ != null) {
					drawingView_.setColor(color);
					drawingOverlay_.setColor(color);
				}
			}
		});
	}

	private void showEditTextLayout() {
		changeDrawingButton_.setVisibility(View.GONE);
		resizeTextButton_.setVisibility(View.VISIBLE);
		exitButton_.setVisibility(View.VISIBLE);
		colorPicker_.setVisibility(View.VISIBLE);
		fontPicker_.setVisibility(View.VISIBLE);

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

		if (!DrawingOverlay.isDefault(drawingOverlay_) && drawingView_ == null) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					initiateOverlayDrawing();
				}
			}, 500);
		}
	}

	/**
	 * Creates and adds EditText for text overlapping
	 */
	private void initiateOverlayEditText() {
		overlayTextView_ = TextOverlay.createOverlayEditText(this, textOverlay_, contentView_);
		overlayTextView_.setClickable(false);
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
		outState.putParcelable(STATE_DRAWING_OVERLAY, drawingOverlay_);
	}
}
