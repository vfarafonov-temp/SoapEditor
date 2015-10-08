package com.weezlabs.soapeditor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Text overlay model
 */
public class TextOverlay implements Parcelable {
	public static final String DEFAULT_TEXT = "Some text";
	public static final int DEFAULT_SCALE = 1;
	public static final float DEFAULT_LOCATION_X = 0.5f;
	public static final float DEFAULT_LOCATION_Y = 0.5f;
	public static final float DEFAULT_ROTATION = 0f;
	private static final int DEFAULT_COLOR = -16711681;
	private static final String DEFAULT_FONT = "";
	private String text_;
	private float scale_;
	private float[] location_ = {0f, 0f};
	private float rotation_;
	private int color_;
	private String font_;

	public TextOverlay(String text, float scale, float[] location, float rotation, int color, String font) {
		this.text_ = text;
		this.scale_ = scale;
		this.location_ = location;
		this.rotation_ = rotation;
		this.color_ = color;
		this.font_ = font;
	}

	public TextOverlay() {
		this(DEFAULT_TEXT, DEFAULT_SCALE, new float[]{DEFAULT_LOCATION_X, DEFAULT_LOCATION_Y}, DEFAULT_ROTATION, DEFAULT_COLOR, DEFAULT_FONT);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(text_);
		dest.writeFloat(scale_);
		dest.writeFloatArray(location_);
		dest.writeFloat(rotation_);
		dest.writeInt(color_);
		dest.writeString(font_);
	}

	public static final Parcelable.Creator<TextOverlay> CREATOR = new Creator<TextOverlay>() {
		@Override
		public TextOverlay createFromParcel(Parcel source) {
			String text = source.readString();
			float scale = source.readFloat();
			float[] location = {0f, 0f};
			source.readFloatArray(location);
			float rotation = source.readFloat();
			int color = source.readInt();
			String font = source.readString();
			return new TextOverlay(text, scale, location, rotation, color, font);
		}

		@Override
		public TextOverlay[] newArray(int size) {
			return new TextOverlay[size];
		}
	};

	public String getText() {
		return text_;
	}

	public float getScale() {
		return scale_;
	}

	public float[] getLocation() {
		return location_;
	}

	public float getRotation() {
		return rotation_;
	}

	public int getColor() {
		return color_;
	}

	public String getFont() {
		return font_;
	}

	public void setText(String text) {
		this.text_ = text;
	}

	public void setScale(float scale) {
		this.scale_ = scale;
	}

	public void setLocation(float[] location) {
		this.location_ = location;
	}

	public void setRotation(float rotation) {
		if (rotation > 360){
			rotation = (rotation % 360);
		} else if (rotation < 0){
			rotation = 360 + (rotation % 360);
		}
		this.rotation_ = rotation;
	}

	public void setColor(int color) {
		this.color_ = color;
	}

	public void setFont(String font) {
		this.font_ = font;
	}

	public static boolean isDefault(TextOverlay textOverlay) {
		return  DEFAULT_TEXT.equals(textOverlay.getText()) &&
				textOverlay.getScale() == DEFAULT_SCALE &&
				textOverlay.getLocation()[0] == DEFAULT_LOCATION_X &&
				textOverlay.getLocation()[1] == DEFAULT_LOCATION_Y &&
				textOverlay.getRotation() == DEFAULT_ROTATION &&
				textOverlay.getColor() == DEFAULT_COLOR &&
				DEFAULT_FONT.equals(textOverlay.getFont());
	}
}
