package by.gravity.doubleplayer.core;

public interface IPlayer {

	public void play();

	public void pause();

	public String getMediaUriString();
	
	public void nextFrame();
	
	public void prevFrame();

}
