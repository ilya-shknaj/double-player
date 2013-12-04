package by.gravity.doublexplayer.core;

import by.gravity.doublexplayer.model.Rate;

public interface IPlayer {

	public void playPause(boolean isPlaying);

	public String getMediaUriString();

	public void nextFrame();

	public void prevFrame();

	public void setRate(Rate rate);

}
