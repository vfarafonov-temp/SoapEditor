package com.weezlabs.soapeditor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.weezlabs.soapeditor.views.TextToolsView;

public class TextMessageActivity extends AppCompatActivity {

	private TextToolsView textTools_;
	private Menu menu_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text_message);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		textTools_ = (TextToolsView) findViewById(R.id.text_tools);
		EditText textField_ = (EditText) findViewById(R.id.et_message_field);
		textTools_.setEditableView(textField_);
		textTools_.setTextToolsListener(new TextToolsView.TextToolsListener() {
			@Override
			public void onTextAlignmentChanged(int newGravity) {

			}

			@Override
			public void onModeChanged(TextToolsView.EditingModes newMode, TextToolsView.EditingModes oldMode) {
				switch (newMode) {
					case BACKGROUND_COLOR_CHANGING:
						menu_.findItem(R.id.action_done).setVisible(true);
						break;
					case FONT_CHANGING:
						menu_.findItem(R.id.action_done).setVisible(true);
						break;
					case TEXT_EDITING:
						menu_.findItem(R.id.action_done).setVisible(false);
						break;
				}
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
}
