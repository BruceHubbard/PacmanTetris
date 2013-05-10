import java.awt.*;
import java.awt.event.*;

public class TetrisTimer
{
	public static int abc = 0;
	public String myName;

	public static void main(String[] args)
	{
			Timer testie = new Timer("myTimer");
	}

}

class Timer extends Thread
{
	public Timer(String name)
	{
		super(name);
		start();
	}

	public void run()
	{
		while( true )
		{
			System.out.println(TetrisTimer.abc);
			TetrisTimer.abc++;
			try {
				sleep(1000);
			}
			catch (InterruptedException e) {
				System.out.println(getName() + " interrupted.");
			}
		}
	}
}
