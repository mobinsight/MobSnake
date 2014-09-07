package com.mobinsight.drawer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mobinsight.snake.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;

public class SnakeFragment extends Fragment implements SurfaceHolder.Callback,
		OnTouchListener, OnClickListener {
	private static int SurfaceWidth = 0;
	private static int SurfaceHeight = 0;
	private int sBlockWidth;
	private int sBlockHeight;
	private SurfaceView surface;
	private GameLoop gameLoop;
	SnakeModel sModel;
	List<SnakeBlock> sBlocks;
	SnakeBlock eatBlock;

	private Bitmap mSnakeBody;
	private Bitmap mSnakeHead;
	private Bitmap mBackgroundBmp;
	private SurfaceHolder holder;
	private Paint backgroundPaint;
	float ballX = 0, ballY = 0;

	private Handler mHandler;
	Button btnThread;
	Button butLeft, butRight, butUp, butDown;
	private Object mPauseLock;
	private boolean mPaused;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.snakefragment, container, false);
        
        surface = (SurfaceView)  view.findViewById(R.id.snake_surface);
		holder = surface.getHolder();
		surface.getHolder().addCallback(this);
		//backgroundPaint = new Paint();
		//backgroundPaint.setColor(Color.LTGRAY);

		mSnakeBody = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.snake_block);
		mSnakeHead = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.snake_head);
		mBackgroundBmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.wood);

		sBlockWidth = mSnakeBody.getWidth();
		sBlockHeight = mSnakeBody.getHeight();

		surface.setOnTouchListener(this);

		sModel = new SnakeModel();
		// 3 Snake blocks to start with
		sModel.AddNew(sBlockWidth, sBlockHeight);
		sModel.AddNew(sBlockWidth, sBlockHeight + sBlockHeight);
		sModel.AddNew(sBlockWidth, sBlockHeight + sBlockHeight * 2);

		mPauseLock = new Object();
		mPaused = false;

//
//		butLeft = (Button) findViewById(R.id.butLeft);
//		butLeft.setOnClickListener(this);
//
//		butRight = (Button) findViewById(R.id.butRight);
//		butRight.setOnClickListener(this);
//
//		butUp = (Button) findViewById(R.id.butUp);
//		butUp.setOnClickListener(this);
//
//		butDown = (Button) findViewById(R.id.butDown);
//		butDown.setOnClickListener(this);

		mHandler = new HandlerExtension();

        return view;
    }

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void onPause() {
		super.onPause();

		synchronized (mPauseLock) {
			mPaused = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		synchronized (mPauseLock) {
			mPaused = false;
			mPauseLock.notifyAll();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// Get the width and height of SurfaceView
		getSurfaceDimension();

		// Pass the width/height into Model
		sModel.init(SurfaceWidth, SurfaceHeight, sBlockWidth);
		// Start game loop
		gameLoop = new GameLoop();
		gameLoop.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		try {
			gameLoop.safeStop();
		} finally {
			gameLoop = null;
		}
		Log.v("surfaceDestroyed", "surfaceDestroyed");
	}

	private void getSurfaceDimension() {
		Canvas c = null;
		try {
			c = holder.lockCanvas();
			if (c != null) {
				SurfaceWidth = c.getWidth();
				SurfaceHeight = c.getHeight();
			}
		} finally {
			if (c != null) {
				holder.unlockCanvasAndPost(c);
			}
		}
	}

	// Drawing method of the Game
	private void draw() {

		Canvas c = null;
		try {
			c = holder.lockCanvas();
			if (c != null) {
				doDraw(c);
			}
		} finally {
			if (c != null) {
				holder.unlockCanvasAndPost(c);
			}
		}
	}

	// This is where actual drawing goes on !!
	private void doDraw(Canvas c) {
		//int width = c.getWidth();
		//int height = c.getHeight();
		//c.drawRect(0, 0, width, height, backgroundPaint);
		//Draw background with bitmap
		c.drawBitmap(mBackgroundBmp, 0, 0, null);
		sBlocks = sModel.getSnakeBlocks();
		int size = sBlocks.size();

		// Draw Snake Head with Different bitmap
		c.drawBitmap(mSnakeHead, sBlocks.get(0).x_loc, sBlocks.get(0).y_loc,
				null);

		// Draw rest of the Snake Body
		for (int index = 1; index < size; index++) {
			c.drawBitmap(mSnakeBody, sBlocks.get(index).x_loc,
					sBlocks.get(index).y_loc, null);
		}
		// Draw the block to be eaten up..
		eatBlock = sModel.getEatBlock();
		c.drawBitmap(mSnakeBody, eatBlock.x_loc, eatBlock.y_loc, null);
	}

	private static class HandlerExtension extends Handler {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	}

	// Create a private class for GameLoop
	// This will call draw() method of the main class at regular interval
	private class GameLoop extends Thread {
		private volatile boolean running = true;

		public void run() {
			while (running) {
				try {
					// Here is one Hard coding..
					TimeUnit.MILLISECONDS.sleep(120);

					draw();
					running = !sModel.isTailTouch();
					sModel.updateSnake();

				} catch (InterruptedException ie) {
					running = false;
				}
				synchronized (mPauseLock) {
					while (mPaused) {
						try {
							mPauseLock.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}

		public void safeStop() {
			running = false;
			interrupt();
		}
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// On every touch send the touch coordinates to Model class AddNew()
		// Model will create a new SnakeBlock and add it to the list.

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			sModel.updateDirection(event.getX(), event.getY());
		}
		return false;
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		default:
			break;
		}
	}

	public boolean toggleGamePlayPause() {
		if (mPaused == false) {
			synchronized (mPauseLock) {
				mPaused = true;
			}
		} else {
			synchronized (mPauseLock) {
				mPaused = false;
				mPauseLock.notifyAll();
			}
		}
		return mPaused;
	}
}