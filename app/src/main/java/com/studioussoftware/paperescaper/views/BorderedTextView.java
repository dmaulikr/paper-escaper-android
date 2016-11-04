package com.studioussoftware.paperescaper.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;

/**
 * Draws a border line on the bottom of the TextView
 * For now specifically used for high scores table, but easily extendible to
 * have borders on all four sides in any colour
 */
public class BorderedTextView extends TextView {
	
	Paint p;
	final int lineThickness = 1;

	public BorderedTextView(Context context) {
		super(context);
		p = new Paint();
		p.setColor(Color.WHITE);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// draw the horizontal line at the bottom of the view
		canvas.drawLine(0, getMeasuredHeight() - lineThickness, getMeasuredWidth(), getMeasuredHeight() - lineThickness, p);
		super.onDraw(canvas);
	}
}
