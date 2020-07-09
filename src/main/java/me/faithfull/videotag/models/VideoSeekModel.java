package me.faithfull.videotag.models;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.util.Duration;
import me.faithfull.videotag.opencv.SeekableVideoCapture;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;

@Component
public class VideoSeekModel extends ScheduledService<Void> {

	private final static Logger log = LoggerFactory.getLogger(VideoSeekModel.class);

	private SimpleIntegerProperty currentFrameProperty = new SimpleIntegerProperty(0);
	private SimpleBooleanProperty playingProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
	private SimpleDoubleProperty playbackSpeed = new SimpleDoubleProperty(1d);
	private int totalFrames;
	private SeekableVideoCapture vc;
	private Image currentFrame;
	private MatOfByte buffer = new MatOfByte();

	public VideoSeekModel(SeekableVideoCapture vc) {
		this.setExecutor(Executors.newSingleThreadExecutor());
		this.vc = vc;
		totalFrames = vc.getTotalFrames();
		playbackSpeed.set(1.0d);
		changePlayback();
		playbackSpeed.addListener(x -> {
			changePlayback();
		});

		seek(0);
	}

	private void changePlayback() {
		int FPS = (int) Math.floor(vc.getFPS());
		Duration freq = Duration.seconds((1d / FPS) / playbackSpeed.get());
		setPeriod(freq);
		log.info(freq.toString());
	}

	@Override
	protected Task<Void> createTask() {
		Task<Void> playTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				if (vc.hasFrame()) {
					if(playingProperty.get()) {
						updateProgress(getCurrentFrameProperty().get(), vc.getTotalFrames());
						Mat mat = new Mat();
						vc.read(mat);
						currentFrame = encode(mat);
	
						Platform.runLater(() -> {
							currentFrameProperty.set(vc.getCurrentFrame());
							imageProperty.set(currentFrame);
						});
					}
				} else {
					cancel();
				}

				return null;
			}

		};

		return playTask;
	}

	@Override
	public void start() {
		playingProperty.set(true);
		super.start();
	}

	@Override
	public boolean cancel() {
		playingProperty.set(false);
		return super.cancel();
	}

	private Image encode(Mat image) {
		Imgcodecs.imencode(".bmp", image, buffer);
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}

	public Image getCurrentFrame() {
		return currentFrame;
	}

	public void forward() {
		if(!vc.hasFrame())
			return;
		Mat mat = new Mat();
		vc.read(mat);
		currentFrame = encode(mat);
		imageProperty.set(currentFrame);
		currentFrameProperty.set(vc.getCurrentFrame());
	}

	public void back() {
		Mat mat = new Mat();
		int currentFrameIndex = vc.getCurrentFrame();
		vc.readFrame(currentFrameIndex - 2, mat);
		currentFrame = encode(mat);
		imageProperty.set(currentFrame);
		currentFrameProperty.set(vc.getCurrentFrame());
	}
	
	public void seek(int frame) {
		vc.seekToFrame(frame);
		Mat mat = new Mat();
		int currentFrameIndex = vc.getCurrentFrame();
		vc.readFrame(currentFrameIndex, mat);
		currentFrame = encode(mat);
		imageProperty.set(currentFrame);
		currentFrameProperty.set(currentFrameIndex);
	}
	
	public void destroy() {
		vc.release();
		vc = null;
	}

	public ReadOnlyIntegerProperty getCurrentFrameProperty() {
		return currentFrameProperty;
	}

	public ReadOnlyBooleanProperty getPlayingProperty() {
		return playingProperty;
	}

	public ReadOnlyObjectProperty<Image> getImageProperty() {
		return imageProperty;
	}

	public DoubleProperty getPlaybackSpeedProperty() {
		return playbackSpeed;
	}

	public int getTotalFrames() {
		return totalFrames;
	}

}
