import java.awt.*;
import java.io.*;
import java.net.*;

class HighScore
{
	String urlName;
	byte[] bytes = new byte[4096];

	static public void main(String[] args)
	{
		HighScore hs = new HighScore("http://unixgen.muohio.edu/~hubbarrb/test/hs.php");
		System.out.println("High Score:" + hs.getHighScore());
		System.out.println("Setting 'Bruce' + '1500'");
		hs.setHighScore(1500, "Bruce");
	}

	public HighScore(String pathName)
	{
		urlName = pathName;
	}

	public int getHighScore()
	{
		String path = urlName + "?mode=getHigh";
		InputStream in = null;

		try {
			URL url = new URL(path);
			in = url.openStream();
			int numRead = in.read(bytes);
			if( numRead != -1 )
				return Integer.parseInt(new String(bytes).trim());
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		finally {
			try {
				if( in != null )
					in.close();
			}
			catch (Exception e) {}
		}


		//on error we return the largest possible value
		return Integer.MAX_VALUE;
	}

	public boolean setHighScore(int newScore, String newName)
	{
		String path = urlName + "?mode=set&user=" + newName + "&score=" + newScore;
		System.out.println(path);
		InputStream in = null;

		try {
			in = new URL(path).openStream();
		}
		catch (Exception e)
		{
			return false;
		}
		finally
		{
			try {
				if( in != null )
					in.close();
			}
			catch (Exception e) {}
		}

		//if we didn't catch any errors assume it went well
		return true;
	}


}