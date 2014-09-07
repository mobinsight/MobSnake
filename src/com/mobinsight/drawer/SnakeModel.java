package com.mobinsight.drawer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class SnakeModel {

	private final List<SnakeBlock> sblocks = new LinkedList<SnakeBlock>();
	private SnakeBlock eatBlock;
	int surfaceWidth, surfaceHeight;
	private Utils util;
	// Direction Sense Flags
	private static final int UP = 0;
	private static final int DOWN = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	private static int mDirection = 2;
	private static int incr = 0;
	private static boolean isBlockConsumed = false;
	public final Object LOCK = new Object();

	public SnakeModel() {
	}

	public void init(int windowWidth, int windowHeight, int snakeIncr) {
		// Align surfaceWidth/surfaceHeight on SnakeBlock boundary
		// This will allow for perfect one-on-one collision with new SnakeBlock
		surfaceWidth = windowWidth - (windowWidth % snakeIncr);
		surfaceHeight = windowHeight - (windowHeight % snakeIncr);

		incr = snakeIncr;
		util = new Utils(surfaceWidth, surfaceHeight, snakeIncr);
		eatBlock = new SnakeBlock(util.GetRandPositionX(),
				util.GetRandPositionY());
	}

	public SnakeBlock getEatBlock() {
		return eatBlock;
	}

	public void updateDirection(float x, float y) {
		synchronized (LOCK) {
			float cur_x = sblocks.get(0).x_loc;
			float cur_y = sblocks.get(0).y_loc;
			// Log.v("updateDirection","cur_x="+cur_x+"cur_y="+cur_y);
			// Skip touch events if Snake head goes past a boundary
			// Otherwise your snake will not be visible at times when
			// user press a button at the instance it crosses the boundary
			if ((cur_x <= (0)) || (cur_x >= (surfaceWidth)) || (cur_y < 0)
					|| (cur_y >= (surfaceHeight)))
				return;

			// Take decision based on current direction of the snake head
			switch (mDirection) {
			case UP:
			case DOWN:
				if (cur_x < x)
					mDirection = RIGHT;
				else
					mDirection = LEFT;
				break;
			case LEFT:
			case RIGHT:
				if (cur_y < y)
					mDirection = DOWN;
				else
					mDirection = UP;
				break;
			}
		}
	}

	public void updateDirectionOnClick(int direction) {
		synchronized (LOCK) {
			float cur_x = sblocks.get(0).x_loc;
			float cur_y = sblocks.get(0).y_loc;
			// Log.v("updateDirectionOnClick","cur_x="+cur_x+"cur_y="+cur_y);
			// Skip touch events if Snake head goes past a boundary
			// Otherwise your snake will not be visible at times when
			// user press a button at the instance it crosses the boundary
			if ((cur_x <= (0)) || (cur_x >= (surfaceWidth)) || (cur_y < 0)
					|| (cur_y >= (surfaceHeight)))
				return;

			// Take decision based on current direction of the snake head
			switch (mDirection) {
			case UP:
				if (direction != DOWN)
					mDirection = direction;
				break;
			case DOWN:
				if (direction != UP)
					mDirection = direction;
				break;
			case LEFT:
				if (direction != RIGHT)
					mDirection = direction;
				break;
			case RIGHT:
				if (direction != LEFT)
					mDirection = direction;
				break;
			}
		}
	}

	public void AddNew(int x, int y) {
		synchronized (LOCK) {
			sblocks.add(new SnakeBlock(x, y));
		}
	}

	public List<SnakeBlock> getSnakeBlocks() {
		synchronized (LOCK) {
			return new ArrayList<SnakeBlock>(sblocks);
		}
	}

	public void updateSnake() {
		synchronized (LOCK) {
			int size = sblocks.size();

			// Save location of Head of the snake first
			sblocks.get(0).prev_x = sblocks.get(0).x_loc;
			sblocks.get(0).prev_y = sblocks.get(0).y_loc;

			// Log.v("updateSnake","cur_x="+sblocks.get(0).x_loc+"cur_y="+sblocks.get(0).y_loc);

			switch (mDirection) {
			case LEFT:
				sblocks.get(0).x_loc -= incr;
				break;
			case RIGHT:
				sblocks.get(0).x_loc += incr;
				break;
			case UP:
				sblocks.get(0).y_loc -= incr;
				break;
			case DOWN:
				sblocks.get(0).y_loc += incr;
				break;
			}
			objectCollision();
			WallCollision();
			// Make changes in the Tail of the snake
			for (int index = 1; index < size; index++) {
				sblocks.get(index).prev_x = sblocks.get(index).x_loc;
				sblocks.get(index).prev_y = sblocks.get(index).y_loc;
				sblocks.get(index).x_loc = sblocks.get(index - 1).prev_x;
				sblocks.get(index).y_loc = sblocks.get(index - 1).prev_y;
			}
		}
	}

	public boolean isTailTouch() {
		synchronized (LOCK) {
			boolean touch = false;
			int size = sblocks.size();
			// Save location of Head of the snake first
			int headx = sblocks.get(0).x_loc;
			int heady = sblocks.get(0).y_loc;

			// Make changes in the Tail of the snake
			for (int index = 1; index < size; index++) {
				if ((headx == (sblocks.get(index).x_loc))
						&& (heady == (sblocks.get(index).y_loc))) {
					touch = true;
					Log.v("isTailTouch", "Touched");
					break;
				}
			}
			return touch;
		}
	}

	public void objectCollision() {
		if (sblocks.get(0).x_loc > eatBlock.x_loc - 10
				&& sblocks.get(0).x_loc < eatBlock.x_loc + 10
				&& sblocks.get(0).y_loc > eatBlock.y_loc - 10
				&& sblocks.get(0).y_loc < eatBlock.y_loc + 10) {
			setBlockConsumed(true);
			// eatBlock = new SnakeBlock(util.GetRandPositionX(),
			// util.GetRandPositionY());
			eatBlock = CreateNewSnakeBlock();
			sblocks.add(new SnakeBlock(eatBlock.x_loc, eatBlock.y_loc));
		}
		setBlockConsumed(false);
	}

	public SnakeBlock CreateNewSnakeBlock() {
		synchronized (LOCK) {
			int x, y;
			boolean isOut = true;
			int size = sblocks.size();
			while (true) {
				isOut = true;
				x = util.GetRandPositionX();
				y = util.GetRandPositionY();

				for (int index = 0; index < size; index++) {
					if ((x == sblocks.get(index).x_loc)
							&& (y == sblocks.get(index).y_loc)) {
						isOut = false;
						Log.v("CreateNewSnakeBlock", "Inside Block");
						break;
					}
				}
				if (isOut) {
					// Log.v("CreateNewSnakeBlock", "OutSide Block");
					break;
				}
			}
			return new SnakeBlock(x, y);
		}
	}

	// Make the snake to come out from the other end instead of ending the game
	// It will give touch screen user time to play happily!!
	public void WallCollision() {
		switch (mDirection) {

		case LEFT:
			if (sblocks.get(0).x_loc <= (0))
				sblocks.get(0).x_loc = surfaceWidth;
			break;
		case RIGHT:
			if (sblocks.get(0).x_loc >= (surfaceWidth))
				sblocks.get(0).x_loc = 0;
			break;
		case UP:
			if (sblocks.get(0).y_loc <= 0)
				sblocks.get(0).y_loc = surfaceHeight;
			break;
		case DOWN:
			if (sblocks.get(0).y_loc >= (surfaceHeight))
				sblocks.get(0).y_loc = 0;
			break;

		}
		setBlockConsumed(false);
	}

	public static boolean isBlockConsumed() {
		return isBlockConsumed;
	}

	public static void setBlockConsumed(boolean isBlockConsumed) {
		SnakeModel.isBlockConsumed = isBlockConsumed;
	}
}
