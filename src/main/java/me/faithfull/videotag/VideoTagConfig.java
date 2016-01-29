package me.faithfull.videotag;

import java.io.File;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import com.infinitybas.slfx.SLFXConfig;

import me.faithfull.videotag.models.LabelFileModel;
import me.faithfull.videotag.models.VideoSeekModel;
import me.faithfull.videotag.opencv.SeekableVideoCapture;

@Configuration
@Import(SLFXConfig.class)
@ComponentScan({"me.faithfull.videotag.controllers", "me.faithfull.videotag"})
public class VideoTagConfig {

	@Bean
	@Scope("prototype")
	public VideoSeekModel videoSeekModel(SeekableVideoCapture vc) {
		return new VideoSeekModel(vc);
	}

	@Bean
	@Scope("prototype")
	SeekableVideoCapture seekableVideoCapture(String location) {
		SeekableVideoCapture vc = new SeekableVideoCapture();
		vc.open(location);
		return vc;
	}
	
	@Bean
	@Scope("prototype")
	LabelFileModel labelFileModel(File videoFile, int nFrames, String... classes) throws IOException {
		return new LabelFileModel(videoFile, nFrames, classes);
	}
}
