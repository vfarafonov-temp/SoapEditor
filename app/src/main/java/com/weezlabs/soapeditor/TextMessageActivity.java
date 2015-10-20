package com.weezlabs.soapeditor;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.weezlabs.soapeditor.models.TextMessage;
import com.weezlabs.soapeditor.views.FontPicker;
import com.weezlabs.soapeditor.views.TextToolsView;

public class TextMessageActivity extends AppCompatActivity {

	private static final String STATE_TEXT_MESSAGE = "STATE_TEXT_MESSAGE";
	private TextToolsView textTools_;
	private Menu menu_;
	private TextMessage textMessage_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_message);

		if (savedInstanceState == null) {
			textMessage_ = new TextMessage();
		} else {
			textMessage_ = savedInstanceState.getParcelable(STATE_TEXT_MESSAGE);
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		textTools_ = (TextToolsView) findViewById(R.id.text_tools);

		final EditText textField_ = (EditText) findViewById(R.id.et_message_field);
		textField_.setBackgroundColor(textMessage_.getBackgroundColor());
		textField_.setTextColor(textMessage_.getTextColor());
		textField_.setText(textMessage_.getText());
		textField_.setGravity(textMessage_.getTextAlignment());

		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(this.getAssets(), FontPicker.FONTS_PATH + "/" + textMessage_.getFontName());
		} catch (Exception e) {
			// Do nothing. Font just cannot be found
		} finally {
			if (typeface == null) {
				typeface = Typeface.DEFAULT_BOLD;
			}
		}
		textField_.setTypeface(typeface);

		textTools_.setEditableView(textField_);
		textTools_.setTextToolsListener(new TextToolsView.TextToolsListener() {
			@Override
			public void onTextAlignmentChanged(int newGravity) {
				textMessage_.setTextAlignment(newGravity);
			}

			@Override
			public void onModeChanged(TextToolsView.EditingModes newMode, TextToolsView.EditingModes oldMode) {
				switch (newMode) {
					case BACKGROUND_COLOR_CHANGING:
						hideKeyboard(findViewById(android.R.id.content).getWindowToken());
						menu_.findItem(R.id.action_done).setVisible(true);
						break;
					case FONT_CHANGING:
						hideKeyboard(findViewById(android.R.id.content).getWindowToken());
						menu_.findItem(R.id.action_done).setVisible(true);
						break;
					case TEXT_EDITING:
						menu_.findItem(R.id.action_done).setVisible(false);
						break;
				}
			}

			@Override
			public void onBackgroundColorChanged(int color) {
				textMessage_.setBackgroundColor(color);
			}

			@Override
			public void onTextColorChanged(int color) {
				textMessage_.setTextColor(color);
			}

			@Override
			public void onFontChanged(String fontName, Typeface typeface) {
				textMessage_.setFont(fontName);
			}
		});

		textField_.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				textMessage_.setText(s.toString());
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_text_message, menu);
		menu_ = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_done:
				textTools_.setMode(TextToolsView.EditingModes.TEXT_EDITING);
				menu_.findItem(R.id.action_done).setVisible(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_TEXT_MESSAGE, textMessage_);
	}

	private void hideKeyboard(IBinder windowToken) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
	}
}
