package com.weezlabs.soapeditor;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

	private View.OnClickListener changeTextClickListener_;
	private RelativeLayout rootView_;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rootView_ = (RelativeLayout) findViewById(R.id.root_layout);
		contentView_ = findViewById(R.id.iv_background_image);
		colorPicker_ = (ColorPicker) findViewById(R.id.color_picker);
		colorPicker_.setOnTouchListener(pickersTouchListener_);
		colorPicker_.setColorChangedListener(new ColorPicker.ColorChangedListener() {
			@Override
			public void onColorChanged(int color) {
				if (overlayTextView_ != null) {
					overlayTextView_.setTextColor(color);
				}
			}
		});
		fontPicker_ = (FontPicker) findViewById(R.id.font_picker);
		fontPicker_.setOnTouchListener(pickersTouchListener_);
		fontPicker_.setFontChangedListener(new FontPicker.FontChangedListener() {
			@Override
			public void onFontChanged(Typeface typeface) {
				if (overlayTextView_ != null){
					overlayTextView_.setTypeface(typeface);
				}
			}
		});

		Button changeTextButton = (Button) findViewById(R.id.btn_change_text);

		changeTextClickListener_ = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (overlayTextView_ == null) {
					initiateOverlayTextView();
				}
				overlayTextView_.requestFocus();
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(overlayTextView_.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
			}
		};

		changeTextButton.setOnClickListener(changeTextClickListener_);
	}

	private void hideKeyboard(IBinder windowToken) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
	}

	private void initiateOverlayTextView() {
		overlayTextView_ = new EditText(getBaseContext());
		overlayTextView_.setTextSize(50);
		overlayTextView_.setBackground(null);
		overlayTextView_.setText("asdasda");
		overlayTextView_.setGravity(Gravity.CENTER);
		overlayTextView_.setWidth(contentView_.getWidth());
		overlayTextView_.setHeight(contentView_.getHeight());
		overlayTextView_.setTextColor(colorPicker_.getSelectedColor());
		overlayTextView_.setTypeface(fontPicker_.getSelectedFont());
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) contentView_.getLayoutParams();
		overlayTextView_.setLayoutParams(layoutParams);
		rootView_.addView(overlayTextView_);
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
}
