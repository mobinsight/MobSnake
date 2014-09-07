package com.mobinsight.drawer;

import android.util.Log;

public class Utils {

	int displayWidth, displayHeight, precision;

	public Utils(int w, int h, int p) {
		displayWidth = w;
		displayHeight = h;
		precision = p;
	}

	public int GetRandPositionX() {
		int x = (int) (Math.random() * (displayWidth-50));
		x += precision - (x % precision);
		if (x > displayWidth)
			x = displayWidth / 2;
		Log.v("Random","x="+x);
		return x;
	}

	public int GetRandPositionY() {
		int y = (int) (Math.random() * (displayHeight-50));
		y += precision - (y % precision);
		if (y > displayHeight)
			y = displayHeight / 2;
		Log.v("Random","y="+y);
		return y;
	}
}
