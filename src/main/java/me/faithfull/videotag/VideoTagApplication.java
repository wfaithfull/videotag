package me.faithfull.videotag;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.infinitybas.slfx.Intent;
import com.infinitybas.slfx.SLFX;

import javafx.application.Application;
import javafx.stage.Stage;
import me.faithfull.videotag.controllers.DefaultController;

/**
 * Hello world!
 *
 */
public class VideoTagApplication extends Application
{
    public static void main( String[] args )
    {
    	String os = System.getProperty("os.name");
    	if(os.startsWith("Windows")) {
    		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
    		String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

    		String realArch = arch.endsWith("64")
    		                  || wow64Arch != null && wow64Arch.endsWith("64")
    		                      ? "x64" : "x86";
    		
    		setPath("windows", realArch);
    	};
    	System.out.println(System.getProperty("java.library.path"));
        launch(args);
    }
    
    private static void setPath(String nativeSubFolder, String arch) {
    	String sep = System.getProperty("file.separator");
    	System.setProperty("java.library.path", System.getProperty("user.dir") + sep + "lib" + sep + "native" + sep + nativeSubFolder + sep + arch);
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(VideoTagConfig.class);
		SLFX slfx = context.getBean(SLFX.class);
		
		slfx.setPrimaryStage(primaryStage);
		slfx.show(new Intent(DefaultController.class));
		primaryStage.show();
	}
}
