package com.weezlabs.soapeditor.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;

/**
 * Customizable text message class
 */
public class TextMessage implements Parcelable {

	private static final String DEFAULT_FONT = "";
	private static final String DEFAULT_TEXT = "";
	private static final int DEFAULT_TEXT_COLOR = Color.RED;
	private static final int DEFAULT_BACKGROUND_COLOR = Color.BLUE;
	private static final int DEFAULT_TEXT_ALIGNMENT = Gravity.CENTER_HORIZONTAL;

	private String text_ = DEFAULT_TEXT;
	private int backgroundColor_ = DEFAULT_BACKGROUND_COLOR;
	private int textColor_ = DEFAULT_TEXT_COLOR;
	private int textAlignment_ = DEFAULT_TEXT_ALIGNMENT;
	private String fontName_ = DEFAULT_FONT;

	public TextMessage() {
	}

	protected TextMessage(Parcel in) {
		text_ = in.readString();
		backgroundColor_ = in.readInt();
		textColor_ = in.readInt();
		textAlignment_ = in.readInt();
		fontName_ = in.readString();
	}

	public static final Creator<TextMessage> CREATOR = new Creator<TextMessage>() {
		@Override
		public TextMessage createFromParcel(Parcel in) {
			return new TextMessage(in);
		}

		@Override
		public TextMessage[] newArray(int size) {
			return new TextMessage[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(text_);
		dest.writeInt(backgroundColor_);
		dest.writeInt(textColor_);
		dest.writeInt(textAlignment_);
		dest.writeString(fontName_);
	}

	public String getText() {
		return text_;
	}

	public int getBackgroundColor() {
		return backgroundColor_;
	}

	public int getTextColor() {
		return textColor_;
	}

	public int getTextAlignment() {
		return textAlignment_;
	}

	public String getFontName() {
		return fontName_;
	}

	public void setText(String text) {
		this.text_ = text;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor_ = backgroundColor;
	}

	public void setTextColor(int textColor) {
		this.textColor_ = textColor;
	}

	public void setTextAlignment(int textAlignment) {
		this.textAlignment_ = textAlignment;
	}

	public void setFont(String fontName) {
		this.fontName_ = fontName;
	}
}
