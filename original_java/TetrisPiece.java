public class TetrisPiece implements GlobalVars
{
	GlobalVars globals;
	int width, height, brickType, brickPose;
	float red, green, blue;
	int xposition;
	int yposition;
	int[] xvalues = new int[4];
	int[] yvalues = new int[4];

	TetrisPiece()
	{
		int type = (int) (NumBrickTypes * Math.random() ); //rand() % globals.NumBrickTypes;
		int pose = (int) (NumPoses * Math.random() );//rand() % globals.NumPoses;
		brickType = type;
		brickPose = pose;
		width  = bricks[type][pose].w;
		height = bricks[type][pose].h;
		xposition = COLS/2 - 1;
		yposition = ROWS - STAGING_ROWS;
		int counter = 0;

		for( int i = 0; i < width; i++)
			for( int j = 0; j < height; j++)
			{
				String tempRow = bricks[type][pose].grid[j];
				if( tempRow.charAt(i) == 'X' )
				{
					xvalues[counter] = xposition + i;
					yvalues[counter] = yposition + j;
					counter++;
				}

			}
		assignColor();
	}

	//int die = (int) (6.0 * Math.random() + 1.0);



	void assignColor() //assigns color to a piece according to it's brickType
	{
		switch( brickType )
		{
		case 0:	//square, "T" = yellow
		case 2:
			red = green = 1.0F; blue = 0.0F;
			break;
		case 1: //stick, 2x2 = blue
		case 5:
			blue = 1.0F; red = green = 0.0F;
			break;
		case 3: //3x1 = purple
			blue = red = 1.0F; green = 0.0F;
			break;
		case 4: //1x3 = green
			green = 1.0F; red = blue = 0.0F;
			break;
		case 6: //l 2x2 = red
			red = 1.0F; blue = green = 0.0F;
			break;
		}
	}

}