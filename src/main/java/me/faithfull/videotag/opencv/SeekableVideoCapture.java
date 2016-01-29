package me.faithfull.videotag.opencv;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class SeekableVideoCapture extends VideoCapture {
	
	private final static int CAP_PROP_POS_MSEC = 0;
	private final static int CAP_PROP_POS_FRAMES = 1;
	private final static int CAP_PROP_FPS = 5;
	private final static int CAP_PROP_FRAME_COUNT = 7;
	
	public boolean readFrame(int frame, Mat image) {
		seekToFrame(frame);
		boolean grab = this.grab();
		boolean retrieve = this.retrieve(image);
		
		return grab && retrieve;
	}
	
	public void seekToFrame(int frame) {
		this.set(CAP_PROP_POS_FRAMES, frame);
	}
	
	public void seekToTime(int ms) {
		this.set(CAP_PROP_POS_MSEC, ms);
	}
	
	public int getCurrentFrame() {
		return (int) this.get(CAP_PROP_POS_FRAMES);
	}
	
	public int getTotalFrames() {
		return (int) this.get(CAP_PROP_FRAME_COUNT);
	}
	
	public Double getFPS() {
		return this.get(CAP_PROP_FPS);
	}
	
	public boolean hasFrame() {
		return getCurrentFrame() < getTotalFrames();
	}

}
