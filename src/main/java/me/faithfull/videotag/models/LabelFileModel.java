package me.faithfull.videotag.models;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelFileModel {
	
	private static final Logger log = LoggerFactory.getLogger(LabelFileModel.class);

	CSVPrinter printer;
	private Map<Integer, String> classes;
	private List<Integer> labels;
	private File labelFile;
	private int defaultClassIndex;
	
	public LabelFileModel(File labelFile, int nFrames, String... classes) throws IOException {
		this(labelFile, nFrames, 0, classes);
	}
	
	public LabelFileModel(File labelFile, int nFrames, int defaultClassIndex, String... classes) throws IOException {
		
		this.labels = new ArrayList<Integer>(Collections.nCopies(nFrames, defaultClassIndex));
		if(labelFile.exists()) {
			
			
			CSVParser parser = new CSVParser(new FileReader(labelFile), CSVFormat.DEFAULT);
			List<CSVRecord> lines = parser.getRecords();
			log.info("Loading {} labels from {}", lines.size(), labelFile.getName());
			for(int i=0;i<parser.getRecordNumber();i++) {
				CSVRecord record = lines.get(i);
				labels.set(i, Integer.parseInt(record.get(0)));
			}
			parser.close();
		}
		
		this.labelFile = labelFile;
		this.setDefaultClassIndex(defaultClassIndex);
		this.classes = new HashMap<Integer, String>();
		for(int i=0;i<classes.length;i++) {
			this.classes.put(i, classes[i]);
		}
	}
	
	public void addClass(String description) {
		getClassDescriptions().put(getClassDescriptions().size()+1, description);
	}
	
	public void setLabel(int index, int classIndex) {
		labels.set(index,classIndex);
	}
	
	public void write() {
		try {
			printer = new CSVPrinter(new FileWriter(labelFile), CSVFormat.DEFAULT);
			printer.printComment("VideoTag by Will Faithfull (will@faithfull.me)");
			for(int i=0;i<getClassDescriptions().size();i++) {
				printer.printComment(String.format("%s = %d", getClassDescriptions().get(i), i));
			}
			printer.printRecords(this.labels);
			printer.flush();
		} catch (IOException e) {
			log.error("IOException writing to file", e);
		}
	}
	
	public int getNumClasses() {
		return getClassDescriptions().size();
	}

	public int getDefaultClassIndex() {
		return defaultClassIndex;
	}

	public void setDefaultClassIndex(int defaultClassIndex) {
		this.defaultClassIndex = defaultClassIndex;
	}

	public List<Integer> getLabels() {
		return labels;
	}
	
	public int getLabelFor(int index) {
		return labels.get(index);
	}

	public Map<Integer, String> getClassDescriptions() {
		return classes;
	}

	public void setClassDescriptions(Map<Integer, String> classDescriptions) {
		this.classes = classDescriptions;
	}
	

}
