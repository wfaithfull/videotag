package me.faithfull.videotag.controllers;

import com.infinitybas.slfx.SLFXController;
import com.infinitybas.slfx.SLFXControllerFor;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@SLFXControllerFor("static/fxml/default.fxml")
public class DefaultController extends SLFXController {
	
	@Autowired
	MenuController menu;
	
	@FXML void loadVideo() {
		menu.openVideo();
	}

}
