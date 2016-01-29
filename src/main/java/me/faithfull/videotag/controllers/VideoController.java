package me.faithfull.videotag.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.infinitybas.slfx.Intent;
import com.infinitybas.slfx.SLFXController;
import com.infinitybas.slfx.SLFXControllerFor;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import me.faithfull.videotag.DialogService;
import me.faithfull.videotag.models.LabelFileModel;
import me.faithfull.videotag.models.VideoSeekModel;
import me.faithfull.videotag.opencv.SeekableVideoCapture;

@SLFXControllerFor("static/fxml/video.fxml")
public class VideoController extends SLFXController implements Initializable {

	private final static Logger log = LoggerFactory.getLogger(VideoController.class);

	SeekableVideoCapture vc;
	VideoSeekModel seeker;
	LabelFileModel labels;
	List<ToggleButton> radioButtons;
	ToggleGroup group;

	private int totalFrames;

	@Autowired
	DialogService dialogs;

	@Autowired
	BeanFactory factory;

	@FXML
	ImageView currentFrame;

	@FXML
	Button playStopBtn;

	@FXML
	Label currentFrameLabel;

	@FXML
	Text frameNumber;

	@FXML
	ProgressBar progressBar;

	@FXML
	Slider playbackSpeedSlider;

	@FXML
	Label playbackSpeedLabel;

	private SimpleBooleanProperty openFileProperty = new SimpleBooleanProperty(false);

	@FXML
	FlowPane labelPane;

	@FXML
	TextField goToFrameField;

	@FXML
	Button goToFrameBtn;

	public ReadOnlyBooleanProperty openFileProperty() {
		return openFileProperty;
	}

	@PostConstruct
	public void init() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	}

	@PreDestroy
	public void destroy() {
		seeker.destroy();
		labelPane.getChildren().clear();
		this.radioButtons.clear();
		openFileProperty.set(false);
	}

	@Override
	public void onShow(Intent intent) {

		Optional<VideoSeekModel> seekModel = intent.<VideoSeekModel> getExtra(VideoSeekModel.class, "seekModel");
		Optional<LabelFileModel> labelsModel = intent.<LabelFileModel> getExtra(LabelFileModel.class, "labelsModel");

		seeker = seekModel.get();
		labels = labelsModel.get();
		openFileProperty.set(true);

		this.radioButtons = new ArrayList<>();
		this.group = new ToggleGroup();

		for (int i = 0; i < labels.getNumClasses(); i++) {
			String desc = labels.getClassDescriptions().get(i);
			ToggleButton rb = new ToggleButton(desc);
			rb.setToggleGroup(group);
			rb.setUserData(i);
			if (i == labels.getDefaultClassIndex()) {
				rb.setSelected(true);
			}

			rb.setOnAction(x -> {
				labels.setLabel(seeker.getCurrentFrameProperty().get(), (int) rb.getUserData());
				log.info("Set {} -> {}", seeker.getCurrentFrameProperty().get(),
						labels.getClassDescriptions().get((int) rb.getUserData()));
			});

			this.radioButtons.add(rb);
		}

		this.labelPane.getChildren().setAll(radioButtons);

		totalFrames = seeker.getTotalFrames();
		log.info("total frames: {}", totalFrames);

		Image img = seeker.getCurrentFrame();

		seeker.getCurrentFrameProperty().addListener((a, b, c) -> {
			int currentLabel = labels.getLabelFor(c.intValue()-1);
			this.radioButtons.get(currentLabel).setSelected(true);
		});

		currentFrame.imageProperty().bind(seeker.getImageProperty());
		frameNumber.textProperty().bind(seeker.getCurrentFrameProperty().asString());
		playbackSpeedSlider.valueProperty().bindBidirectional(seeker.getPlaybackSpeedProperty());
		playbackSpeedLabel.textProperty().bind(seeker.getPlaybackSpeedProperty().asString("%.2fx"));
		progressBar.progressProperty().bind(seeker.getCurrentFrameProperty().divide((double) totalFrames));

		log.info("Loaded @ {}x{}", img.getWidth(), img.getHeight());

		slfx.getPrimaryStage().setWidth(img.getWidth() + 50);
		slfx.getPrimaryStage().setHeight(img.getHeight() + 250);
		progressBar.setPrefWidth(img.getWidth());

		super.onShow(intent);
	}

	@FXML
	public void togglePlay() {
		if (seeker.getPlayingProperty().get()) {
			seeker.cancel();
			playStopBtn.setText("Play");
		} else {
			seeker.restart();
			playStopBtn.setText("Stop");
		}
	}

	@FXML
	public void back() {
		group.selectToggle(radioButtons.get(labels.getDefaultClassIndex()));
		seeker.back();
	}

	@FXML
	public void forward() {
		group.selectToggle(radioButtons.get(labels.getDefaultClassIndex()));
		seeker.forward();
	}

	@FXML
	public void closeVideo() {
		if (vc != null) {
			destroy();
			slfx.back();
			slfx.getPrimaryStage().sizeToScene();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		goToFrameField.setText("0");
		goToFrameField.textProperty().addListener((a, b, c) -> {
			if (!isNumeric(c)) {
				dialogs.warning(String.format("Invalid value %s", c));
				goToFrameField.setText(b);
				return;
			}
			
			if(c.isEmpty())
				return;

			int value = Integer.parseInt(c);
			if (value > totalFrames-1 || value < 0) {
				dialogs.warning(String.format("%d is outside the acceptable range (0-%d)", value, totalFrames-1));
				goToFrameField.setText(b);
			} else {
				goToFrameField.setText(c);
			}
		});

		// Setup key bindings
		slfx.getPrimaryStage().addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
			if (ke.getCode() == KeyCode.SPACE && ke.isControlDown()) {
				togglePlay();
			}

			if (ke.getCode() == KeyCode.LEFT && ke.isControlDown() && !seeker.getPlayingProperty().get()) {
				back();
			}

			if (ke.getCode() == KeyCode.RIGHT && ke.isControlDown() && !seeker.getPlayingProperty().get()) {
				forward();
			}
		});
	}

	private boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	@FXML
	public void goToFrame() {
		if(!goToFrameField.getText().isEmpty())
			seeker.seek(Integer.parseInt(goToFrameField.getText()));
	}

}
