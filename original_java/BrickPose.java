public class BrickPose {

	BrickPose(int wid, int hei, String row1, String row2, String row3, String row4)
	{
		this.w = wid;
		this.h = hei;
		this.grid[0] = row1;
		this.grid[1] = row2;
		this.grid[2] = row3;
		this.grid[3] = row4;
	}


	int w, h;
	String[] grid = new String[4];
}