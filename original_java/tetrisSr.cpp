/************************************************************
******  Name: Richard Bruce Hubbard				  ***********
******  Class: CSA 386							  ***********
******  Section: A								  ***********
******  Program Number: 3B                        ***********
******  Program Title: Tetris Sr.				  ***********
************************************************************/

#include <iostream>
using namespace std;

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>
#include <GL/glut.h>
#include <GL/gl.h>

const double PI = 3.14159265359;	//value of pi
const int BLOCKSIZE = 15;  //width of block in pixels
const int ROWS = 25;		// number of rows on board
const int COLS = 12;		//number of columns on board
const int VPWIDTH = COLS * BLOCKSIZE;	//width needed for viewport
const int VPHEIGHT = ROWS * BLOCKSIZE;	//height needed for viewport
const int STAGING_ROWS = 5;	//rows needed to set up the block
const int FLASHES = 30;		//number of times pac is drawn
const int PACTIMER = 30;	//timer for pac animation in milliseconds
const int KILLTIMER = 100;	//timer for kill animation in milliseconds
const GLdouble PACMAXANGLE = PI/4;	//max angle that pac's mouth is open
GLdouble PACX = 0.0;	//holder for where pac is on the screen
GLdouble PACANGLE = 0.0;	//current angle for pac's mouth

#include "shape.h"	//given
#include "tetrisPiece.h"	//object repr. a tetris piece

//forward declaration of timer function
void timer(int);

struct Colors {	//struct used for the game grid
	GLdouble r, g, b;
	bool isSet;
};

struct Game {
	Game();
	void ClearGrid();	//clears the grid of blocks
	void Restart();		//resets boards to initial values
	void Move(int dir);	//moves the currentBlock left or right
	void Drop();		//drops currentBlock one space
	void CheckRows();	//checks rows for full rows
	void SmashRows();	//smashes full rows
	void checkDeath();	//checks for death
	void rotateBlock();	//rotates block if possible
	void getNewBlock();	//gets a new block for nextBlock
	bool fullRow( int );	//returns true if the row is full
	Colors colorGrid[ROWS][COLS];	//grid for game pieces
	bool killed;	// true if the current life has been terminated
	bool BlockIsActive;	// true if currentBlock is active
	bool killFlash;		//used to toggle colors in death animation
	bool paused;	// true if the game is to be paused
	bool RowsClearing; //true if we are clearing rows
	int score;	//current score
	int gameID;		//used so no timer affect the next game
	int gametimer;		// determines how fast game proceeds
	int rowsToClear[4];	//rows needed to be cleared
	int numRowsToClear;	//number of rows needed to be cleared
	int level;		//level of game
	int blocksSeen;	//number of blocks dropped
	int rowsCleared;	//number of rows cleared in current game
	TetrisPiece currentBlock;	//holds the current block
	TetrisPiece nextBlock;		//holds the next block to be dropped
};


//used to smash cleared rows, drop non full rows, and update score
void Game::SmashRows()
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
		for( int j = rowsToClear[i]; j < ROWS-STAGING_ROWS - 1; j++ )
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
		for( c = 0; c < COLS; c++ )
			colorGrid[ROWS-STAGING_ROWS][c].isSet = false;
	}
	//if we've reached a new multiple of 10 in rows cleared
	//increment the level and decrement the timer
	if( int(rowsCleared / 10) > level )
	{
		level++;
		gametimer *= .85;
	}
	numRowsToClear = 0;	//just cleared them all
}

//checks all rows from top to bottom and if they are full it
//adds them to rowsToClear
void Game::CheckRows()
{
	for( int n=ROWS-STAGING_ROWS; n>=0; n-- ) {
		bool rowFull = true;
		for (int c=0; c<COLS; c++)
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

//checks for death
void Game::checkDeath()
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
bool Game::fullRow( int row )
{
	for( int i = 0; i < numRowsToClear; i++ ) {
		if( rowsToClear[i] == row )
			return true;
	}
	return false;
}

//decrements the y values of current block by one
void Game::Drop()
{
	//assume the block can drop unless we find otherwise
	bool movable = true;

	for( int i = 0; i < 4; i++ )
	{
		int newLevel = currentBlock.yvalues[i] - 1;
		//if there is something below currentBlock then it can't
		//go any further down
		if( colorGrid[newLevel][currentBlock.xvalues[i]].isSet == true
			|| currentBlock.yvalues[i] == 0 )
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

//rotates the currentBlock in it's place if necessary
void Game::rotateBlock()
{
	bool rotatable = true;
	int counter = 0, adjust = 0;
	int newPose, newWidth, newHeight, newx[4], newy[4];

	newPose = (currentBlock.brickPose + 1) % NumPoses;

	newWidth = bricks[currentBlock.brickType][newPose].w;
	newHeight = bricks[currentBlock.brickType][newPose].h;

	if( currentBlock.xposition + currentBlock.height > COLS )
		if( currentBlock.height != 4 )
			adjust = -1 * ( currentBlock.height - 2 );
		else
			adjust = -1 * ( currentBlock.height - 1 );

	for( int i = 0; i < newWidth; i++)
		for( int j = 0; j < newHeight; j++)
		{
			if( bricks[currentBlock.brickType][newPose].grid[j][i] == 'X' )
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

// Move - Move the users block left (dir = -1) or right (dir = +1).
void Game::Move(int dir)
{
	int i = 0, j= 0;
	bool movable = true;
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

//gets a new block for nextblock
void Game::getNewBlock()
{
	TetrisPiece newBlock;
	nextBlock = newBlock;
}

// ClearGrid - Erases all oncoming blocks from the grid.
void Game::ClearGrid()
{
	for (int r=0; r<ROWS; r++) {
		for (int c=0; c<COLS; c++) {
			colorGrid[r][c].isSet = false;
		}
	}
}

//restarts the game to initial values
void Game::Restart()
{
	ClearGrid();
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
	glutTimerFunc( gametimer, timer, gameID );
}

// Game - Constructor
Game::Game()
{
	gameID = 0;
	Restart();
}

Game game;

void timer(int id) //won't need this till it drops
{
	if( !game.paused )
	{
		if( game.BlockIsActive && id == game.gameID )
		{
			game.Drop();
			glutPostRedisplay();
			glutTimerFunc( game.gametimer, timer, game.gameID );
		}
		//block just set in place, check death, check rows, then get
		//a new block if necessary
		else if( !game.BlockIsActive && id == game.gameID && !game.killed)
		{
				game.checkDeath();
				game.CheckRows();
				//if we need to clear a row, do so
				if( game.RowsClearing )  {
					glutPostRedisplay();
					glutTimerFunc( 100, timer, -1 );
				}
				else
				{
					glutTimerFunc( game.gametimer, timer, game.gameID );
					game.currentBlock = game.nextBlock;
					game.getNewBlock( );
					game.blocksSeen++;
					game.BlockIsActive = true;
					glutPostRedisplay();
				}
		}
		//freshly killed call kill animation
		else if( game.killed && id > 0 )
				glutTimerFunc( KILLTIMER, timer, -1 );

		else if( game.killed && id < 0 && id >= -10 )
		{
			if( id > -10 )
			{
				game.killFlash = !game.killFlash;
				id--;
				glutTimerFunc( KILLTIMER, timer, id );
				glutPostRedisplay();
			}
			else
			{
				game.killFlash = true;
				glutPostRedisplay();
			}

		}
		else if( game.RowsClearing && id < 0 && id >= -1 * FLASHES )
		{
			if( id > -1 * FLASHES )
			{
				id--;
				PACX += double(COLS)/double(FLASHES);
				PACANGLE = fabs( sinf( PACX ) )*PACMAXANGLE;
				glutTimerFunc( PACTIMER, timer, id );
				glutPostRedisplay();
			}
			else
			{
				glutTimerFunc( PACTIMER, timer, 0 );
				glutPostRedisplay();
			}
		}
		//rows done blinking smash 'em
		else if( game.RowsClearing && id == 0 && !game.killed )
		{
				PACX = 0;
				game.SmashRows();
				game.RowsClearing = !game.RowsClearing;
				glutTimerFunc( game.gametimer, timer, game.gameID );
				glutPostRedisplay();
		}
	}
	else	//needed for pause loop
		glutTimerFunc( game.gametimer, timer, id );
}

//function to display text on the graphics window
void BitmapText(char *str, float wcx, float wcy)
{
	glRasterPos2d(wcx, wcy);
	for (int i=0; str[i] != '\0'; i++) {
		glutBitmapCharacter(GLUT_BITMAP_8_BY_13, str[i]);
	}
}

//function to draw a circle
void Circle(GLdouble x, GLdouble y, GLdouble r )
{
	glPushMatrix();
	glTranslated(x, y, 0);
	GLUquadric *q = gluNewQuadric();
	gluDisk( q, 0, r, 30, 10);
	gluDeleteQuadric( q );
	glPopMatrix();
}

//function to draw a filled arc (pac) stAngle is always < endAngle
void DrawFilledArc(GLdouble stAngle, GLdouble endAngle, GLdouble radius,
			 GLdouble stX, GLdouble stY)
{
	GLdouble x, y;
	glBegin(GL_POLYGON);
		glVertex2f( stX, stY );
        for(GLdouble theta = stAngle; theta <= endAngle; theta += 0.1f)
        {
           x = stX + radius*cos(theta);
           y = stY + radius*sin(theta);
           glVertex2f(x, y);
        }
		glVertex2f( stX, stY );
    glEnd();
}

//function to draw a arc (outline of pac) stAngle is always < endAngle
void DrawArc(GLdouble stAngle, GLdouble endAngle, GLdouble radius,
			 GLdouble stX, GLdouble stY)
{
	GLdouble x, y;
	glBegin(GL_LINE_STRIP);
		glVertex2f( stX, stY );
        for(GLdouble theta = stAngle; theta <= endAngle; theta += 0.1f)
        {
           x = stX + radius*cos(theta);
           y = stY + radius*sin(theta);
           glVertex2f(x, y);
        }
		glVertex2f( stX, stY );
    glEnd();
}

//function to draw pac
void drawPac( GLdouble y, GLdouble r )
{
	glColor3f( 1.0, 1.0, 0.0 );
	DrawFilledArc( PACANGLE, 2*PI-PACANGLE, r, PACX, y+0.5 );
	glColor3f( 0.0, 0.0, 0.0 );
	DrawArc( PACANGLE, 2*PI-PACANGLE, r, PACX, y+0.5 );
}

//function to draw the pacdots
void drawPacDot( GLdouble y, GLdouble r )
{
	glColor3f( 1.0, 1.0, 1.0 );
	for( double i = 1+r; i < double(COLS) - 1+r; i+=3*r )
		if( i > PACX ) //if pac hasn't passed - creates eating effect
			Circle( i, y+0.5, r );
}

//determines
void determinePacRows()
{
	int counter = 0;
	GLdouble pacRowsAvg[2];//avg y point of rows
	GLdouble pacNumRows[2];//radii of pacs

	for( int i = 0; i < game.numRowsToClear; i++ )
	{
		if( i == 0 )
		{
			pacRowsAvg[0] = game.rowsToClear[0];
			pacNumRows[0] = 1;
		}
		else
		{
			if( game.rowsToClear[i-1] - game.rowsToClear[i] == 1 )
			{
				pacNumRows[counter]++;
				pacRowsAvg[counter] = (pacRowsAvg[counter] + game.rowsToClear[i]);
			}
			else
			{
				counter++;
				pacRowsAvg[counter] = game.rowsToClear[i];
				pacNumRows[counter] = 1;
			}
		}
	}
	for( i = 0; i < counter+1; i++ )
	{
		drawPacDot( pacRowsAvg[i]/pacNumRows[i], pacNumRows[i]/4 );
		drawPac( pacRowsAvg[i]/pacNumRows[i], pacNumRows[i]/2 );
	}
}

//function to draw the outline of the game board
void drawGameBoard()
{
	glColor3f(.5, 0.5, 0.5);
	glLineWidth( 3.0 );
	glBegin(GL_LINES);
		glVertex2d( 0, ROWS - STAGING_ROWS);
		glVertex2d( 0, 0 );
		glVertex2d( 0, 0 );
		glVertex2d( COLS, 0);
		glVertex2d( COLS, 0);
		glVertex2d( COLS, ROWS - STAGING_ROWS);
	glEnd();
	glLineWidth( 1.0 );
}

void drawPiece( double lx, double by, double rx, double ty, GLdouble red,
				GLdouble green, GLdouble blue)
{
	glColor3f( red, green, blue );
	glRectd( lx, by, rx, ty );
	glColor3f( red - 0.5, green - 0.5, blue - 0.5);
	glBegin( GL_LINE_LOOP );
		glVertex2d( lx, by );
		glVertex2d( lx, ty );
		glVertex2d( rx, ty );
		glVertex2d( rx, by );
	glEnd();
	glLineWidth( 1.0 );
}

//display function
void display(void)
{
//	GLdouble tempr, tempg, tempb;
	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	glClear(GL_COLOR_BUFFER_BIT);
	glViewport(10, 10, VPWIDTH+(BLOCKSIZE*10)+10, VPHEIGHT+10);

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluOrtho2D(0, COLS+10, 0, ROWS);

	if( game.BlockIsActive && !game.killed && !game.paused )
		for( int b = 0; b < 4; b++ )
		{
			drawPiece( game.currentBlock.xvalues[b],
						game.currentBlock.yvalues[b],
						game.currentBlock.xvalues[b] + 1,
						game.currentBlock.yvalues[b] + 1,
						game.currentBlock.red, game.currentBlock.green,
						game.currentBlock.blue);
		}

	if( !game.paused )
		for (int r=0; r<ROWS-STAGING_ROWS; r++)
			for (int c=0; c<COLS; c++) {
				if (game.colorGrid[r][c].isSet && !game.fullRow(r))
				{
					if( !game.killFlash  )
					{
						drawPiece( c, r, c+1, r+1, game.colorGrid[r][c].r,
							game.colorGrid[r][c].g, game.colorGrid[r][c].b);
					}
					else
						drawPiece( c, r, c+1, r+1, 0.5, 0, 0);
				}
			}

	drawGameBoard();

	if( game.RowsClearing && !game.paused)
		determinePacRows();

	glLineWidth( 1.0 );

	//display death text
	if( game.killed )
	{
		glColor3f(0.8, 0.8, 0.8);
		glRectd(1, ROWS/2-2.5, COLS - 1, ROWS/2 + 1);
		glColor3f(.7, 0, 0);
		char msg[100];
		sprintf(msg, "DEATH");
		BitmapText(msg, double(COLS)/2.0f - 1.2f, double(ROWS)/2.0f - .5f);
		sprintf(msg, "Please Press 'r'");
		BitmapText(msg, double(COLS)/2.0f - 3.2f, double(ROWS)/2.0f - 1.5f);
		sprintf(msg, "To Play Again");
		BitmapText(msg, double(COLS)/2.0f - 3.0f, double(ROWS)/2.0f - 2.5f);
	}

	//display paused text
	if( game.paused )
	{
		glColor3f(0.8, 0.8, 0.8);
		glRectd(1, ROWS/2-1, COLS - 1, ROWS/2 + 1);
		glColor3f(.7, 0, 0);
		char msg[100];
		sprintf(msg, "PAUSED");
		BitmapText(msg, double(COLS)/2.0f-1.2f, double(ROWS)/2.0f - .6f);
	}


	//draw next border
	int vpsize = BLOCKSIZE*6;
	glColor3f( 0.5, 0.5, 0.5 );
	glBegin(GL_LINE_LOOP);
	glVertex2f( COLS + 1, ROWS-STAGING_ROWS-8 );
	glVertex2f( COLS + 1, ROWS-STAGING_ROWS-1 );
	glVertex2f( COLS + 7, ROWS-STAGING_ROWS-1 );
	glVertex2f( COLS+ 7, ROWS-STAGING_ROWS-8);
	glEnd();

	GLdouble xmiddle = (double(COLS)+1.0f+double(COLS)+7.0f)/2.0f,
			 ymiddle = (2.0f*double(ROWS-STAGING_ROWS)-7.0f-1.0f)/2.0f,
			 yheight = double(game.nextBlock.height)/2.0f,
			 xwidth = double(game.nextBlock.width)/2.0f;

	//draw next piece
	if( !game.killed && !game.paused )
		for( int i = 0; i < 4; i++ )
		{
			GLdouble xinitial = game.nextBlock.xvalues[i]-game.nextBlock.xposition,
				     yinitial = game.nextBlock.yvalues[i]-game.nextBlock.yposition;
			drawPiece( xinitial+xmiddle-xwidth, yinitial+ymiddle-yheight,
				xinitial+xmiddle+1-xwidth, yinitial+ymiddle+1-yheight,
				game.nextBlock.red, game.nextBlock.green, game.nextBlock.blue);
		}

	glViewport(VPWIDTH + 10, 0, VPWIDTH+110, VPHEIGHT);

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluOrtho2D(0, 100, 0, ROWS);

		// start displaying statistics
	char msg[100];
	glColor3f(1,1,0);
	sprintf(msg, "   NEXT    ");
	BitmapText(msg, 9, ROWS-STAGING_ROWS-6);
	sprintf(msg, "Blocks Seen: %d", game.blocksSeen);
	glColor3f(1, 1, 0);
	BitmapText(msg, 5, (ROWS-STAGING_ROWS) - 13.0);
	sprintf(msg, "Rows Cleared: %d", game.rowsCleared);
	BitmapText(msg, 5, (ROWS-STAGING_ROWS) - 12.0);
	sprintf(msg, "Score: %d", game.score);
	BitmapText(msg, 5, (ROWS-STAGING_ROWS) - 11.0);
	sprintf(msg, "Level: %d", game.level);
	BitmapText(msg, 5, (ROWS-STAGING_ROWS) - 10.0);


	glutSwapBuffers();
}

void keyboard(unsigned char key, int x, int y)
{
	switch (key) {
	case ' ':
		while( !game.paused && game.BlockIsActive )
		{
			game.Drop();
			if( game.BlockIsActive)
				game.score++;
		}
		glutPostRedisplay();
		break;
	case 'x': case 'X':
		exit(0);
		break;
	case 'r': case 'R':
		game.Restart();
		glutPostRedisplay();
		break;
	case 'p': case 'P':
		if( !game.killed )
			game.paused = !game.paused;
		glutPostRedisplay();
		break;
	}

}


void special(int key, int x, int y)
{
	if( !game.paused )
	{
		switch(key) {
			//left moves the current block left if possible
			case GLUT_KEY_LEFT:
				if( !game.killed )
				{
					game.Move( -1 );
					glutPostRedisplay();
				}
				break;
			//right moves the current block right if possible
			case GLUT_KEY_RIGHT:
				if( !game.killed )
				{
					game.Move( 1 );
					glutPostRedisplay();
				}
				break;
			//up rotates the block
			case GLUT_KEY_UP:
				if( !game.killed && game.BlockIsActive)
				{
					game.rotateBlock();
					glutPostRedisplay();
				}
				break;
			//down drops the block one spot
			case GLUT_KEY_DOWN:
				if( !game.killed && game.BlockIsActive)
				{
					game.Drop();
					glutPostRedisplay();
				}
				break;
		}
	}
}


void main(int argc, char *argv[])
{
	srand(time(0));
	glutInit(&argc, argv);

	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB);
	glEnable( GL_LINE_SMOOTH );

	glutInitWindowPosition(150, 50);
	glutInitWindowSize(VPWIDTH + 200, VPHEIGHT+25);

	glutCreateWindow("Bruce Hubbard - Tetris Sr.");

	cout << "x - exits the game\n";
	cout << "p - pause the game\n";
	cout << "SPACE - drops the block in it's place\n";
	cout << "r - resets the game\n" << "UP - rotates the block\n";
	cout << "DOWN - drops the block one space\n";
	cout << "LEFT - moves block left\n" << "RIGHT - moves block right\n";


	glutDisplayFunc(display);
	glutSpecialFunc(special);
	glutKeyboardFunc(keyboard);

	glutMainLoop();
}



