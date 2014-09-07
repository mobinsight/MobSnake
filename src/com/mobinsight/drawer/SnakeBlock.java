package com.mobinsight.drawer;

public class SnakeBlock {
	int x_loc, y_loc;
	int prev_x, prev_y;
	
	public SnakeBlock(int x, int y){
		x_loc = x;
		y_loc = y;
		prev_x = x;
		prev_y = y;
	}
}
