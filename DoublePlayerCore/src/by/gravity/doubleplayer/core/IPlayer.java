package by.gravity.doubleplayer.core;

public interface IPlayer {

	public void playPause();

	public String getMediaUriString();

	public void nextFrame();

	public void prevFrame();
	
	public void setRate(double rate);

}
