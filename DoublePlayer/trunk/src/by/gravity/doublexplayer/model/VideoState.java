package by.gravity.doublexplayer.model;

import java.io.Serializable;

public class VideoState implements Serializable {

	private static final long serialVersionUID = 5292623026996483257L;

	private final String mediaUri;

	private final boolean isPlayed;

	private final int position;

	private final Rate rate;

	public VideoState(String mediaUri, int position, Rate rate, boolean isPlayed) {

		this.mediaUri = mediaUri;
		this.position = position;
		this.rate = rate;
		this.isPlayed = isPlayed;
	}

	public String getMediaUri() {

		return mediaUri;
	}

	public boolean isPlayed() {

		return isPlayed;
	}

	public int getPosition() {

		return position;
	}

	public Rate getRate() {

		return rate;
	}

}
