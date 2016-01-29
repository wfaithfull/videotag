package me.faithfull.videotag;

import java.util.Optional;

import org.springframework.stereotype.Service;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

@Service
public class DialogService {

	public void exception(Throwable ex) {
		Alert alert = buildAlert(AlertType.ERROR, "Exception", ex.getMessage(), ex.toString());
		alert.show();
	}
	
	public void error(String message) {
		Alert alert = buildAlert(AlertType.ERROR, "Error", "An error occurred:", message);
		alert.show();
	}
	
	public void warning(String message) {
		Alert alert = buildAlert(AlertType.WARNING, "Warning", "Warning:", message);
		alert.show();
	}
	
	public boolean yesNo(AlertType type, String message) {
		Alert alert = new Alert(type);
		alert.setTitle("Decision");
		alert.setHeaderText(message);

		ButtonType btnYes = new ButtonType("Yes");
		ButtonType btnNo = new ButtonType("No");
		
		alert.getButtonTypes().setAll(btnYes, btnNo);
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == btnNo){
		    return false;
		}
		return true;
	}
	
	public Alert buildAlert(AlertType type, String title, String header, String message) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		return alert;
	}
}
