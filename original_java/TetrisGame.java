import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


/**
 *
 *  to add
 *
 *  1. Username Entry on start
 *	2. Directions
 *
 */


public class TetrisGame extends java.applet.Applet
		implements Runnable, GlobalVars
{
	/** The actual width of the graphics screen in pixels. */
	int appWidth;

	/** The actual heigth of the graphics screen in pixels. */
	int appHeight;

	/** The width of the graphics screen used to draw the game board in pixels. */
	int gameWidth;

	/** The heigth of the graphics screen used to draw the game board in pixels. */
	int gameHeight;

	/** Image used for double buffering. */
	Image offscreenImage;

	/** Graphics context used with offscreenImage for double buffering. */
	Graphics offscreen;

	/** temp. */
	Date date;

	HighScore hs;


	static int id = 0;
	double PACX = 0.0;	//holder for where pac is on the screen
	double PACANGLE = 0.0;	//current angle for pac's mouth
	final double PACMAXANGLE = 45;	//max angle that pac's mouth is open
	int BLOCKSIZE;
	Game game = new Game();
	Thread worker = new Thread(this, "myTimer");


	/**
	 *
	 * Constructer for when the game is run in application mode.
	 *
	 */
	public TetrisGame()
	{
		appWidth = 400;
		appHeight = 400;
		gameWidth = appWidth-100;
		gameHeight = appHeight-10;
		BLOCKSIZE = 15;
		id = game.gameID;
		date = new Date();
	}


	public static void main(String[] args)
	{
		Frame frame = new Frame("TetrisGAme"){
			public Dimension getPreferredSize()
			{
				return new Dimension(400, 430);
			}
		};
		TetrisGame testMe = new TetrisGame();
		testMe.hs = new HighScore("http://unixgen.muohio.edu/~hubbarrb/test/hs.php");
		TetrisKeyListener keyboard = testMe.new TetrisKeyListener();
		testMe.addKeyListener(keyboard);
		frame.add(testMe);
		testMe.worker.start();

		frame.pack();
		frame.setVisible(true);
	}

	/**
		 *
		 * Initialization function for when the game is run in applet mode.
		 *
	 */
	public void init()
	{
		appWidth  = (Integer.parseInt(getParameter("width" )));
		appHeight = (Integer.parseInt(getParameter("height")));

		gameWidth = appWidth-100;
		gameHeight= appHeight-10;

		hs = new HighScore("http://unixgen.muohio.edu/~hubbarrb/test/hs.php");

		BLOCKSIZE = 15;
		id = game.gameID;

		this.addKeyListener(new TetrisKeyListener());
		worker.start();
		repaint();
	}

	/**
	 *
	 * Function To Update The Game Board and statistics.
	 * Delegates itself to paint.
	 *
	 */
	public void update(Graphics a)
	{
	  paint(a);
	}

	/**
	 *
	 * Function To Draw The Game Board and statistics (double buffered)
	 *
	 */
	public void paint(Graphics screen)
	{

		if( offscreenImage == null )
		{
			offscreenImage = createImage(getSize().width, getSize().height);
		}

		offscreen = offscreenImage.getGraphics();


		offscreen.fillRect(0, 0, appWidth, appHeight);

		if( game.BlockIsActive && !game.killed && !game.paused )
			for( int b = 0; b < 4; b++ )
			{
				drawPiece( game.currentBlock.xvalues[b]*BLOCKSIZE+10,
							(gameHeight-((game.currentBlock.yvalues[b])*BLOCKSIZE)-BLOCKSIZE),
							game.currentBlock.red, game.currentBlock.green,
							game.currentBlock.blue, offscreen);
			}

		if( !game.paused )
			for (int r=0; r<ROWS-STAGING_ROWS; r++)
				for (int c=0; c<COLS; c++) {
					if (game.colorGrid[r][c].isSet && !game.fullRow(r))
					{
						if( !game.killFlash  )
						{
							drawPiece( c*BLOCKSIZE+10,
									   (gameHeight-((r)*BLOCKSIZE))-BLOCKSIZE,
									   game.colorGrid[r][c].r,
									   game.colorGrid[r][c].g,
							           game.colorGrid[r][c].b, offscreen);
						}
						else
							drawPiece( c*BLOCKSIZE+10,
									   (gameHeight-((r)*BLOCKSIZE))-BLOCKSIZE,
									   0.5f, 0.0f, 0.0f, offscreen);
					}
				}

		drawGameBoard(offscreen);

		if( game.RowsClearing && !game.paused)
			determinePacRows(offscreen);


		//display death text
		if( game.killed )
		{
			String deathMessage = "DEATH\nPLEASE PRESS 'R'\nTO PLAY AGAIN";
			offscreen.drawString("DEATH", (COLS/2)*BLOCKSIZE, (gameHeight-(ROWS/2)*BLOCKSIZE));
			/*
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
		*/}

		//display paused text
		if( game.paused )
		{
			offscreen.drawString("PAUSED", (COLS/2)*BLOCKSIZE, (gameHeight-(ROWS/2)*BLOCKSIZE));
		}


		//draw next border
		int vpsize = BLOCKSIZE*6;
		offscreen.setColor( new Color( 0.5f, 0.5f, 0.5f ));
		offscreen.drawRect( (COLS+1)*BLOCKSIZE, gameHeight-((ROWS-STAGING_ROWS-1)*BLOCKSIZE),
						 (6*BLOCKSIZE), 6*BLOCKSIZE);

		int    xmiddle = (COLS+4)*BLOCKSIZE,
			   ymiddle = (gameHeight-(ROWS-STAGING_ROWS-4)*BLOCKSIZE),
			   yheight = (int)(((double)(game.nextBlock.height)/2.0f)*BLOCKSIZE),
			   xwidth  = (int)(((double)(game.nextBlock.width)/2.0f)*BLOCKSIZE);


		//draw next piece
		if( !game.killed && !game.paused )
			for( int i = 0; i < 4; i++ )
			{
				int xinitial = (game.nextBlock.xvalues[i]-game.nextBlock.xposition)*BLOCKSIZE,
				    yinitial = (game.nextBlock.yvalues[i]-game.nextBlock.yposition)*BLOCKSIZE;
				drawPiece( xinitial+xmiddle-xwidth, ((ymiddle+yheight)-yinitial)-BLOCKSIZE,
					game.nextBlock.red, game.nextBlock.green, game.nextBlock.blue, offscreen);
			}


		// start displaying statistics
		offscreen.setColor(new Color( 1.0f, 1.0f, 0.0f ));
		offscreen.drawString("NEXT", (COLS+3)*BLOCKSIZE, (gameHeight-(ROWS-STAGING_ROWS-1)*BLOCKSIZE)-10);

		offscreen.drawString("Blocks Seen: " + game.blocksSeen,
						  (COLS+1)*BLOCKSIZE,
						  gameHeight-((ROWS-STAGING_ROWS-9)*BLOCKSIZE));

		offscreen.drawString("Rows Cleared: " + game.rowsCleared,
						  (COLS+1)*BLOCKSIZE,
						  gameHeight-((ROWS-STAGING_ROWS-10)*BLOCKSIZE));

		offscreen.drawString("Score: " + game.score,
						  (COLS+1)*BLOCKSIZE,
						  gameHeight-((ROWS-STAGING_ROWS-11)*BLOCKSIZE));

		offscreen.drawString("Level: " + game.level,
						  (COLS+1)*BLOCKSIZE,
						  gameHeight-((ROWS-STAGING_ROWS-12)*BLOCKSIZE));

		offscreen.drawString(hs.urlName,
							 (COLS+1)*BLOCKSIZE,
						  gameHeight-((ROWS-STAGING_ROWS-14)*BLOCKSIZE));
		Calendar calendar = Calendar.getInstance();

		offscreen.drawString("" + calendar.getTime(), (COLS+1)*BLOCKSIZE,
							gameHeight-((ROWS-STAGING_ROWS-16)*BLOCKSIZE));

		//if( !game.RowsClearing ) //if pacman is not running across the screen then double buffer
		//{
			screen.drawImage(offscreenImage, 0, 0, this);
			offscreen.dispose();
		//}

	}

	public void run()
	{
		boolean runMe = true;
		while( runMe )
		{

		try{
			if( !game.paused )
			{
				if( game.BlockIsActive && id == game.gameID )
				{
					game.Drop();
					repaint();
					id = game.gameID;
					worker.sleep(game.gametimer);
				}
				//block just set in place, check death, check rows, then get
				//a new block if necessary
				else if( !game.BlockIsActive && id == game.gameID && !game.killed)
				{
					game.checkDeath();
					game.CheckRows();
					//if we need to clear a row, do so
					if( game.RowsClearing )  {
						id = -1;
						//worker.sleep(100);
					}
					else
					{
						id = game.gameID;
						worker.sleep(game.gametimer);
						game.currentBlock = game.nextBlock;
						game.getNewBlock( );
						game.blocksSeen++;
						game.BlockIsActive = true;
						repaint();
					}
				}
				//freshly killed call kill animation
				else if( game.killed && id > 0 )
				{
					id = -1;
					//worker.sleep(KILLTIMER);
				}
				else if( game.killed && id < 0 && id >= -10 )
				{
					if( id > -10 )
					{
						game.killFlash = !game.killFlash;
						id--;
						worker.sleep(KILLTIMER);
						repaint();
					}
					else
					{
						game.killFlash = true;
						repaint();
						//set High Score
						System.out.println("Setting High Score");
						hs.setHighScore(game.score, "Bruce");
						runMe = false; //stop thread
					}

				}
				else if( game.RowsClearing && id < 0 && id >= -1 * FLASHES )
				{
					if( id > -1 * FLASHES )
					{
						id--;
						PACX += (double)COLS/(double)FLASHES;
						PACANGLE = Math.abs( Math.sin( PACX ) )*PACMAXANGLE;
						//	/*fabs( sinf( PACX ) )*PACMAXANGLE*/10;
						repaint();
						worker.sleep(PACTIMER);
					}
					else
					{
						id = 0;
						repaint();
						worker.sleep(PACTIMER);
					}
				}
				//rows done blinking smash 'em
				else if( game.RowsClearing && id == 0 && !game.killed )
				{
					PACX = 0;
					game.SmashRows();
					game.RowsClearing = !game.RowsClearing;
					repaint();
					id = game.gameID;
					worker.sleep(game.gametimer);
				}
			}
			else	//needed for pause loop
				worker.sleep(game.gametimer);
		}
		catch( InterruptedException e )
		{}
		}
	}


	//determines
	void determinePacRows(Graphics screen)
	{
		int counter = 0;
		double[] pacRowsAvg = new double[2];//avg y point of rows
		double[] pacNumRows = new double[2];//radii of pacs

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


		for( int i = 0; i < counter+1; i++ )
		{

			drawPacDot( (int)(((pacRowsAvg[i]/pacNumRows[i])+0.5f)*BLOCKSIZE),
						(int)(((pacNumRows[i]/4.0f))*BLOCKSIZE), screen );

			drawPac(    (int)(((pacRowsAvg[i]/pacNumRows[i])+0.5f)*BLOCKSIZE),
					    (int)(((pacNumRows[i]/2.0f))*BLOCKSIZE), screen );
		}
	}


	public void drawPiece( int lx, int ty, float red,
					float green, float blue, Graphics screen)
	{
		Color newColor  = new Color(red, green, blue);
		Color newBorder = new Color( (red > 0.5f) ? red - 0.5f : red,
									 (green > 0.5f) ? green - 0.5f : green,
									 (blue > 0.5f) ? blue - 0.5f : blue);

		screen.setColor(newColor);
		screen.fillRect(lx, ty, BLOCKSIZE, BLOCKSIZE);

		screen.setColor(newBorder);
		screen.drawRect(lx, ty, BLOCKSIZE, BLOCKSIZE);

	}



	//function to draw a circle
	void Circle(int x, int y, int r, Graphics screen )
	{
		screen.fillOval(x, y-(r/2), r, r);
	}

	//function to draw a filled arc (pac) stAngle is always < endAngle
	public void DrawFilledArc(int stAngle, int endAngle, int radius,
				 int stX, int stY, Graphics screen)
	{
		screen.fillArc(stX-(radius/2), stY-(radius/2), radius, radius, stAngle, endAngle-stAngle);
	}

	//function to draw a arc (outline of pac) stAngle is always < endAngle
	public void DrawArc(int stAngle, int endAngle, int radius,
				 int stX, int stY, Graphics screen)
	{
		screen.drawArc(stX-radius, stY-radius, radius, radius, stAngle, endAngle-stAngle);
	}

	//function to draw pac
	public void drawPac( int y, int r, Graphics screen )
	{
		screen.setColor(new Color(1.0f, 1.0f, 0.0f));
		DrawFilledArc( (int)PACANGLE, (int)(360-PACANGLE), 2*r, (int)(PACX*BLOCKSIZE), gameHeight-y, screen );

		screen.setColor(new Color(0.0f, 0.0f, 0.0f));
		DrawArc( (int)PACANGLE, (int)(360-PACANGLE), r, (int)PACX, y, screen );
	}

	//function to draw the pacdots
	public void drawPacDot( int y, int r, Graphics screen )
	{
		screen.setColor( new Color(1.0f, 1.0f, 1.0f) );
		for( double i = BLOCKSIZE+r; i < (double)((COLS*BLOCKSIZE) - 1+((double)r/2)); i+=3*r )
			if( i > (PACX*BLOCKSIZE) ) //if pac hasn't passed - creates eating effect
				Circle( (int)(i), (int)(gameHeight-y), (int)(r*2), screen );
	}

	//function to draw the outline of the game board
	void drawGameBoard(Graphics screen)
	{
		screen.setColor(new Color(0.5f, 0.5f, 0.5f));
		screen.drawRect(10, (gameHeight-((ROWS-STAGING_ROWS)*BLOCKSIZE)),
						COLS*BLOCKSIZE, (ROWS-STAGING_ROWS)*BLOCKSIZE);
	}



	class TetrisKeyListener extends KeyAdapter
	{
		public void keyPressed(KeyEvent e) //   Invoked when a key has been pressed.
		{
			int key = e.getKeyCode();

			if( !game.paused )
			{
				switch (key)
				{
					//left moves the current block left if possible
					case KeyEvent.VK_LEFT:
					case KeyEvent.VK_KP_LEFT:
						if( !game.killed )
						{
							game.Move( -1 );
							repaint();
						}
						break;
					//right moves the current block right if possible
					case KeyEvent.VK_RIGHT:
					case KeyEvent.VK_KP_RIGHT:
						if( !game.killed )
						{
							game.Move( 1 );
							repaint();
						}
						break;
					//up rotates the block
					case KeyEvent.VK_UP:
					case KeyEvent.VK_KP_UP:
						if( !game.killed && game.BlockIsActive)
						{
							game.rotateBlock();
							repaint();
						}
						break;
					//down drops the block one spot
					case KeyEvent.VK_DOWN:
					case KeyEvent.VK_KP_DOWN:
						if( !game.killed && game.BlockIsActive)
						{
							game.Drop();
							repaint();
						}
						break;
				}
			}

			switch (key)
			{
				case KeyEvent.VK_SPACE:
				 	while( !game.paused && game.BlockIsActive )
					{
						game.Drop();
						if( game.BlockIsActive)
							game.score++;
					}
					repaint();
					break;
				case KeyEvent.VK_X:
					System.exit(0);
					break;
				case KeyEvent.VK_R:
					game.Restart();
					//worker.stop();
					id=game.gameID;
					worker.start();
					//worker.start();
					repaint();
					break;
				case KeyEvent.VK_P:
					if( !game.killed )
						game.paused = !game.paused;
					repaint();
					break;
			}
		}

	}//end keyListener class

}



