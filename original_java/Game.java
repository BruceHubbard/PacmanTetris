public class Game implements GlobalVars {

	/*
		Data Members
	*/
	Colors[][] colorGrid = new Colors[ROWS][COLS];	//grid for game pieces
	boolean killed;	// true if the current life has been terminated
	boolean BlockIsActive;	// true if currentBlock is active
	boolean killFlash;		//used to toggle colors in death animation
	boolean paused;	// true if the game is to be paused
	boolean RowsClearing; //true if we are clearing rows
	int score;	//current score
	int gameID;		//used so no timer affect the next game
	int gametimer;		// determines how fast game proceeds
	int[] rowsToClear = new int[4];	//rows needed to be cleared
	int numRowsToClear;	//number of rows needed to be cleared
	int level;		//level of game
	int blocksSeen;	//number of blocks dropped
	int rowsCleared;	//number of rows cleared in current game
	TetrisPiece currentBlock;	//holds the current block
	TetrisPiece nextBlock;		//holds the next block to be dropped

	/*
		Methods
	*/

	// Game - Constructor
	Game()
	{
		gameID = 0;
		Restart();
	}


	//restarts the game to initial values
	public void Restart()
	{
		ClearGrid();
		getNewBlock();
		currentBlock = nextBlock;
		getNewBlock();
		gameID++;
		level = 0;
		killed = false;
		BlockIsActive = false;
		gametimer = 700;
		blocksSeen = 0;
		score = 0;
		rowsCleared = 0;
		paused = false;
		killFlash = false;
		blocksSeen = 0;
		//glutTimerFunc( gametimer, timer, gameID );
	}


	// ClearGrid - Erases all oncoming blocks from the grid.
	public void ClearGrid()
	{
		for (int r=0; r < ROWS; r++) {
			for (int c=0; c < COLS; c++) {
				colorGrid[r][c] = new Colors();
			}
		}
	}


	//gets a new block for nextblock
	public void getNewBlock()
	{
		nextBlock = new TetrisPiece();
	}


	//checks for death
	public void checkDeath()
	{
		for( int i = 0; i < COLS; i++ )
		{
			//if the row above the board has anything in it
			//then death has occurred
			if( colorGrid[ROWS-STAGING_ROWS][i].isSet )
				killed = true;
		}
	}


	//returns true if the row is full
	public boolean fullRow( int row )
	{
		for( int i = 0; i < numRowsToClear; i++ ) {
			if( rowsToClear[i] == row )
				return true;
		}
		return false;
	}


	//checks all rows from top to bottom and if they are full it
	//adds them to rowsToClear
	public void CheckRows()
	{
		for( int n= ROWS - STAGING_ROWS; n>=0; n-- ) {
			boolean rowFull = true;
			for (int c=0; c < COLS; c++)
				if (!colorGrid[n][c].isSet)
					rowFull = false; //if we find a column not set
									//then the row is not full

			//if the row was full and it isn't already in
			//rowsToClear then add it and increment numRowsToClear
			if( rowFull && !fullRow( n ))
			{
				RowsClearing = true;
				rowsToClear[numRowsToClear] = n;
				numRowsToClear++;
			}
		}
	}


	//used to smash cleared rows, drop non full rows, and update score
	public void SmashRows()
	{
		switch( numRowsToClear )
		{
		case 1:
			score += 10;	//single
			break;
		case 2:
			score += 80;	//double
			break;
		case 3:
			score += 270;	//triple
			break;
		case 4:
			score += 640;	//tetris
			break;
		}

		//for each row needed to be cleared it clears it out and then
		//decrements all rows above it by one
		for( int i = 0; i < numRowsToClear; i++ )
		{
			rowsCleared++;
			for( int c = 0; c < COLS; c++ )
				colorGrid[rowsToClear[i]][c].isSet = false;
			for( int j = rowsToClear[i]; j < ROWS - STAGING_ROWS - 1; j++ )
				for( int c = 0; c < COLS; c++ )
				{
					if( colorGrid[j+1][c].isSet )
					{
						colorGrid[j][c].isSet = true;
						colorGrid[j][c].r = colorGrid[j+1][c].r;
						colorGrid[j][c].g = colorGrid[j+1][c].g;
						colorGrid[j][c].b = colorGrid[j+1][c].b;
						colorGrid[j+1][c].isSet = false;
					}
				}
			//obviously nothing could be on the top row so...
			for( int c = 0; c < COLS; c++ )
				colorGrid[ROWS - STAGING_ROWS][c].isSet = false;
		}
		//if we've reached a new multiple of 10 in rows cleared
		//increment the level and decrement the timer
		if( (int)(rowsCleared / 10) > level )
		{
			level++;
			gametimer *= .85;
		}
		numRowsToClear = 0;	//just cleared them all
	}



	// Move - Move the users block left (dir = -1) or right (dir = +1).
	public void Move(int dir)
	{
		int i = 0, j= 0;
		boolean movable = true;
		switch( dir ) {
		case -1: //left
			for(; i < 4; i++ ) {
				if( currentBlock.xvalues[i] <= 0
					|| colorGrid[currentBlock.yvalues[i]][currentBlock.xvalues[i]-1].isSet)
					movable = false;
			}
			if( movable ) {
				for(; j < 4; j++ )
					currentBlock.xvalues[j] -= 1;
				currentBlock.xposition--;
			}
			break;
		case 1: //right
			for(; i < 4; i++ ) {
				if( currentBlock.xvalues[i] >= COLS - 1 ||
					colorGrid[currentBlock.yvalues[i]][currentBlock.xvalues[i]+1].isSet)
					movable = false;
			}
			if( movable ) {
				for(; j < 4; j++ )
					currentBlock.xvalues[j] += 1;
				currentBlock.xposition++;
			}
			break;
		}
	}



	//rotates the currentBlock in it's place if necessary
	public void rotateBlock()
	{
		boolean rotatable = true;
		int counter = 0, adjust = 0;
		int newPose, newWidth, newHeight;
		int[] newx = new int[4];
		int[] newy = new int[4];

		newPose = (currentBlock.brickPose + 1) % NumPoses;

		newWidth  = bricks[currentBlock.brickType][newPose].w;
		newHeight = bricks[currentBlock.brickType][newPose].h;

		if( currentBlock.xposition + currentBlock.height > COLS )
			if( currentBlock.height != 4 )
				adjust = -1 * ( currentBlock.height - 2 );
			else
				adjust = -1 * ( currentBlock.height - 1 );

		for( int i = 0; i < newWidth; i++)
			for( int j = 0; j < newHeight; j++)
			{
				if( bricks[currentBlock.brickType][newPose].grid[j].charAt(i) == 'X' )
				{
					newx[counter] = currentBlock.xposition+i+adjust;
					newy[counter] = currentBlock.yposition+j;
					if( colorGrid[newy[counter]][newx[counter]].isSet )
						rotatable = false;
					counter++;
				}
			}

		if( rotatable )
			for( int i = 0; i < 4; i++ )
			{
				currentBlock.brickPose = newPose;
				currentBlock.xvalues[i] = newx[i];
				currentBlock.yvalues[i] = newy[i];
				currentBlock.height=newHeight;
				currentBlock.width=newWidth;
			}
	}



	//decrements the y values of current block by one
	public void Drop()
	{
		//assume the block can drop unless we find otherwise
		boolean movable = true;

		for( int i = 0; i < 4; i++ )
		{
			int newLevel = currentBlock.yvalues[i] - 1;
			//if there is something below currentBlock then it can't
			//go any further down
			if( currentBlock.yvalues[i] == 0 ||
				colorGrid[newLevel][currentBlock.xvalues[i]].isSet == true
				 )
				movable = false;
		}

		//if it can move then decrement the y values
		if( movable )
		{
			for( int j = 0; j < 4; j++ )
				currentBlock.yvalues[j] -= 1;
			currentBlock.yposition--;
		}
		//otherwise set it into the grid
		else
		{
			for( int k = 0; k < 4; k++ )
			{
				colorGrid[currentBlock.yvalues[k]][currentBlock.xvalues[k]].isSet = true;
				colorGrid[currentBlock.yvalues[k]][currentBlock.xvalues[k]].r
						= currentBlock.red;
				colorGrid[currentBlock.yvalues[k]][currentBlock.xvalues[k]].g
						= currentBlock.green;
				colorGrid[currentBlock.yvalues[k]][currentBlock.xvalues[k]].b
						= currentBlock.blue;
			}
			BlockIsActive = false; //since the block has been added to the
									//grid it is no longer active
		}

	}

}//end of Game class