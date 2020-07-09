package me.faithfull.videotag.controllers;

import com.infinitybas.slfx.Intent;
import com.infinitybas.slfx.SLFXController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import me.faithfull.videotag.DialogService;
import me.faithfull.videotag.models.LabelFileModel;
import me.faithfull.videotag.models.VideoSeekModel;
import me.faithfull.videotag.opencv.SeekableVideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class MenuController extends SLFXController implements Initializable {

	private final static Logger log = LoggerFactory.getLogger(MenuController.class);

	@FXML
	MenuItem closeMenuItem;

	@FXML
	MenuItem saveMenuItem;

	@Autowired
	VideoController videoController;

	@Autowired
	DialogService dialogs;

	@Autowired
	BeanFactory factory;

	private LabelFileModel labelsModel;

	File currentFile;

	@FXML
	public void showAbout() {
		Alert about = dialogs.buildAlert(AlertType.INFORMATION, "VideoTag", "VideoTag v0.1.1",
				"VideoTag by Will Faithfull (will@faithfull.me)\n" + "Created using JavaFX and OpenCV");
		about.show();
	}

	@FXML
	public void openVideo() {

		FileChooser videoChooser = new FileChooser();
		videoChooser.setTitle("Choose video");
		videoChooser.setSelectedExtensionFilter(new ExtensionFilter("MPEG4", ".mp4"));
		currentFile = videoChooser.showOpenDialog(slfx.getPrimaryStage());

		if (videoController.openFileProperty().get()) {
			boolean keepGoing = dialogs.yesNo(AlertType.CONFIRMATION, "Current file will be closed. Continue?");
			if(!keepGoing)
				return;
			videoController.destroy();
		}

		if (currentFile != null) {

			log.info("Loading {}", currentFile.getAbsolutePath());

			SeekableVideoCapture vc = factory.getBean(SeekableVideoCapture.class, currentFile.getAbsolutePath());

			if (!vc.isOpened()) {
				slfx.show(new Intent("static/fxml/default.fxml"));
				dialogs.error(String.format("There was a problem opening the video file (%s)", currentFile.getName()));
				return;
			}

			VideoSeekModel seekModel = factory.getBean(VideoSeekModel.class, vc);
			File labelsFile = new File(currentFile.getAbsolutePath() + ".vt");

			labelsModel = factory.getBean(LabelFileModel.class, labelsFile,
					seekModel.getTotalFrames(), new String[] { "Open", "Transition", "Closed" });

			Intent intent = new Intent("static/fxml/video.fxml")
					.withExtra("seekModel", seekModel)
					.withExtra("labelsModel", labelsModel);

			slfx.show(intent);
			closeMenuItem.setText("Close " + currentFile.getName());
		}
	}

	@FXML
	public void closeVideo() {
		videoController.destroy();
		currentFile = null;
		slfx.back();
		slfx.getPrimaryStage().sizeToScene();
		closeMenuItem.setText("Close");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		closeMenuItem.disableProperty().bind(videoController.openFileProperty().not());
	}

	@FXML
	public void saveLabels() {
		labelsModel.write();
	}
}
