

public interface GlobalVars
{
	final double PI = 3.14159265359;	//value of pi
	final int ROWS = 25;		// number of rows on board
	final int COLS = 12;		//number of columns on board
	final int STAGING_ROWS = 5;	//rows needed to set up the block
	final int FLASHES = 30;		//number of times pac is drawn
	final int PACTIMER = 30;	//timer for pac animation in milliseconds
	final int KILLTIMER = 100;	//timer for kill animation in milliseconds

	static final int NumPoses = 4;		// N, S, E, W
	static final int NumBrickTypes = 7;	// 2x2, 1x4, "T", left and right "L", left and right offset

	static final int NumColors = 6; // red, blue, green, cyan, magenta, yellow

	static final BrickPose[][] bricks = {

											//piece 1/7 - forming the 2x2 tetrisPiece
											{
												//phase 1/4
												new BrickPose(2, 2, 	"XX  ",
																		"XX  ",
																		"    ",
																		"    "),
												//phase 2/4
												new BrickPose(2, 2, 	"XX  ",
																		"XX  ",
																		"    ",
																		"    "),
												//phase 3/4
												new BrickPose(2, 2, 	"XX  ",
																		"XX  ",
																		"    ",
																		"    "),
												//phase 4/4
												new BrickPose(2, 2, 	"XX  ",
																		"XX  ",
																		"    ",
																		"    ")
											},

											//piece 2/7 - forming the 1x4 tetrisPiece
											{
												//phase 1/4
												new BrickPose(1, 4, 	"X   ",
																		"X   ",
																		"X   ",
																		"X   "),
												//phase 2/4
												new BrickPose(4, 1, 	"XXXX",
																		"    ",
																		"    ",
																		"    "),
												//phase 3/4
												new BrickPose(1, 4, 	"X   ",
																		"X   ",
																		"X   ",
																		"X   "),
												//phase 4/4
												new BrickPose(4, 1, 	"XXXX",
																		"    ",
																		"    ",
																		"    ")
											},

											//piece 3/7 - forming the "T" block
											{
												//phase 1/4
												new BrickPose(2, 3, 	"X   ",
																		"XX  ",
																		"X   ",
																		"    "),
												//phase 2/4
												new BrickPose(3, 2, 	" X  ",
																		"XXX ",
																		"    ",
																		"    "),
												//phase 3/4
												new BrickPose(2, 3, 	" X  ",
																		"XX  ",
																		" X  ",
																		"    "),
												//phase 4/4
												new BrickPose(3, 2, 	"XXX ",
																		" X  ",
																		"    ",
																		"    ")
											},


											//piece 4/7 - forming the Left "L" block
											{
												//phase 1/4
												new BrickPose(2, 3, 	"XX  ",
																		"X   ",
																		"X   ",
																		"    "),
												//phase 2/4
												new BrickPose(3, 2, 	"X   ",
																		"XXX ",
																		"    ",
																		"    "),
												//phase 3/4
												new BrickPose(2, 3, 	" X  ",
																		" X  ",
																		"XX  ",
																		"    "),
												//phase 4/4
												new BrickPose(3, 2, 	"XXX ",
																		"  X ",
																		"    ",
																		"    ")
											},


											//piece 5/7 - forming the right "L" block
											{
												//phase 1/4
												new BrickPose(2, 3, 	"XX  ",
																		" X  ",
																		" X  ",
																		"    "),
												//phase 2/4
												new BrickPose(3, 2, 	"XXX ",
																		"X   ",
																		"    ",
																		"    "),
												//phase 3/4
												new BrickPose(2, 3, 	"X   ",
																		"X   ",
																		"XX  ",
																		"    "),
												//phase 4/4
												new BrickPose(3, 2, 	"  X ",
																		"XXX ",
																		"    ",
																		"    ")
											},


											//piece 6/7 - forming the left Offset block
											{
												//phase 1/4
												new BrickPose(2, 3, 	"X   ",
																		"XX  ",
																		" X  ",
																		"    "),
												//phase 2/4
												new BrickPose(3, 2, 	" XX ",
																		"XX  ",
																		"    ",
																		"    "),
												//phase 3/4
												new BrickPose(2, 3, 	"X   ",
																		"XX  ",
																		" X  ",
																		"    "),
												//phase 4/4
												new BrickPose(3, 2, 	" XX ",
																		"XX  ",
																		"    ",
																		"    ")
											},


											//piece 7/7 - forming the right Offset block
											{
												//phase 1/4
												new BrickPose(2, 3, 	" X  ",
																		"XX  ",
																		"X   ",
																		"    "),
												//phase 2/4
												new BrickPose(3, 2, 	"XX  ",
																		" XX ",
																		"    ",
																		"    "),
												//phase 3/4
												new BrickPose(2, 3, 	" X  ",
																		"XX  ",
																		"X   ",
																		"    "),
												//phase 4/4
												new BrickPose(3, 2, 	"XX  ",
																		" XX ",
																		"    ",
																		"    ")
											}
									    };


}





