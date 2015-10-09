package com.weezlabs.soapeditor.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;

/**
 * Created by Admin on 09.10.2015.
 */
public class DrawingOverlay implements Parcelable {
	public static final int DEFAULT_COLOR = Color.GREEN;

	private int color_ = DEFAULT_COLOR;
	private LinkedList<float[]> pathList_ = new LinkedList<>();

	public DrawingOverlay() {
	}

	protected DrawingOverlay(Parcel in) {
		color_ = in.readInt();
		final int linesCount = in.readInt();
		LinkedList<float[]> list = new LinkedList<>();
		for (int i = 0; i < linesCount; i++) {
			list.add(in.createFloatArray());
		}
		pathList_ = list;
	}

	public static final Creator<DrawingOverlay> CREATOR = new Creator<DrawingOverlay>() {
		@Override
		public DrawingOverlay createFromParcel(Parcel in) {
			return new DrawingOverlay(in);
		}

		@Override
		public DrawingOverlay[] newArray(int size) {
			return new DrawingOverlay[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(color_);
		final int linesCount = pathList_.size();
		dest.writeInt(linesCount);
		for (float[] item : pathList_) {
			dest.writeFloatArray(item);
		}
	}

	public void addToPath(float[] item) {
		pathList_.add(item);
	}

	public LinkedList<float[]> getPathList() {
		return pathList_;
	}

	public int getColor() {
		return color_;
	}

	public void setColor(int color) {
		color_ = color;
	}

	public static boolean isDefault(DrawingOverlay drawingOverlay) {
		return drawingOverlay.getPathList().size() == 0 &&
				drawingOverlay.getColor() == DEFAULT_COLOR;
	}

	public void clearDrawing() {
		pathList_.clear();
	}
}
