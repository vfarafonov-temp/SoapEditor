package com.weezlabs.soapeditor;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vfarafonov on 01.10.2015.
 */
public class FontPicker extends ListView {

	public static final String FONTS_PATH = "fonts";
	public static final String DEFAULT_FONT = "Default";
	private HashMap<String, Typeface> fontsMap_ = new HashMap<>();
	private FontChangedListener fontChangedListener_;
	private FontsArrayAdapter adapter_;

	public FontPicker(Context context) {
		super(context);
		init();
	}

	public FontPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FontPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		if (isInEditMode()) {
			// Skip if IDE is in edit mode
			return;
		}
		// Get fonts from assets
		try {
			AssetManager assets = getContext().getApplicationContext().getAssets();
			String[] fontsList = assets.list(FONTS_PATH);
			for (String font : fontsList) {
				if (font.toLowerCase().endsWith(".ttf")) {
					fontsMap_.put(font, Typeface.createFromAsset(assets, FONTS_PATH + "/" + font));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (fontsMap_.size() == 0) {
			fontsMap_.put(DEFAULT_FONT, Typeface.DEFAULT);
		}
		ArrayList<String> fontsList = new ArrayList<>();
		fontsList.addAll(fontsMap_.keySet());
		adapter_ = new FontsArrayAdapter(getContext(), fontsList);
		setAdapter(adapter_);
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				adapter_.setSelectedItem(position);
				if (fontChangedListener_ != null) {
					fontChangedListener_.onFontChanged(getSelectedFontName(), getSelectedFont());
				}
			}
		});
	}

	private String getSelectedFontName() {
		return adapter_.getSelectedItem();
	}

	public void setSelectedFont(String fontName) {
		ArrayList<String> fontsList = new ArrayList<>();
		fontsList.addAll(fontsMap_.keySet());
		int index = fontsList.indexOf(fontName);
		if (index != -1) {
			adapter_.setSelectedItem(index);
		}
	}

	public Typeface getSelectedFont() {
		return fontsMap_.get(adapter_.getSelectedItem());
	}

	static class FontsArrayAdapter extends ArrayAdapter<String> {
		public final int MIDDLE;
		private final int blue_;
		private final int gray_;
		private int selectedIndex_ = 0;

		public FontsArrayAdapter(Context context, List<String> objects) {
			super(context, R.layout.fonts_row, objects);
			MIDDLE = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % objects.size();

			blue_ = getContext().getResources().getColor(android.R.color.holo_blue_light);
			gray_ = getContext().getResources().getColor(android.R.color.black);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = convertView;
			if (itemView == null) {
				LayoutInflater inflater = LayoutInflater.from(getContext());
				itemView = inflater.inflate(R.layout.fonts_row, null);

				ViewHolder holder = new ViewHolder();
				holder.fontNameTextView = (TextView) itemView.findViewById(R.id.tv_font_name);
				holder.isSelectedImage = (ImageView) itemView.findViewById(R.id.iv_selected);
				itemView.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) itemView.getTag();
			holder.fontNameTextView.setText(getItem(position));
			if (selectedIndex_ == position) {
				holder.isSelectedImage.setVisibility(VISIBLE);
				holder.fontNameTextView.setTextColor(blue_);
			} else {
				holder.isSelectedImage.setVisibility(GONE);
				holder.fontNameTextView.setTextColor(gray_);
			}
			return itemView;
		}

		public void setSelectedItem(int position) {
			selectedIndex_ = position;
			notifyDataSetChanged();
		}

		public String getSelectedItem() {
			return getItem(selectedIndex_);
		}

		static class ViewHolder {
			public TextView fontNameTextView;
			public ImageView isSelectedImage;
		}
	}

	public interface FontChangedListener {
		void onFontChanged(String fontName, Typeface typeface);
	}

	public void setFontChangedListener(FontChangedListener fontChangedListener) {
		this.fontChangedListener_ = fontChangedListener;
	}
}
