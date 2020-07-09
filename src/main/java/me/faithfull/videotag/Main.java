package me.faithfull.videotag;

import org.opencv.core.Core;

import static nu.pattern.OpenCV.loadShared;

public class Main {
    public static void main( String[] args ) {
        loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoTagApplication.run(args);
    }
}
