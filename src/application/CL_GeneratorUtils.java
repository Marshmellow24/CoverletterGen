package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class CL_GeneratorUtils {
	
	// method that initiates cmd.exe to run bat file with path file as argument
	public static String parse_cv(String path) throws IOException {	
		File pFile = new File(path);
		String batFile = ".\\.\\data\\buffer\\parse.bat";
		
		// instantiate process builder to start cmd
		ProcessBuilder builder = new ProcessBuilder("cmd.exe");
		Process process = builder.start();
		
		// write into output stream from process builder
		BufferedWriter out = new BufferedWriter(new	OutputStreamWriter(process.getOutputStream()));
		
		// retrieve output from python script
		BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));

		// start batchfile and close outputstream - quotation marks to enable path files with spaces
		out.write("start /b "+ batFile + " " + "\"" + pFile.getAbsolutePath() +"\"");
		out.write("\n");
		out.flush();
		out.close();
		
		// read input and show console output from cmd
		String line = "";
		String parsedFile = "";
		
		while((line = bfr.readLine()) != null) {
			if (line.contains("cv_parsed")) {
				parsedFile = line;
			}
		}
		
		System.out.println(parsedFile);
		
		// return file name of parsed CV in folder .\.\data\buffer
		return parsedFile;
	}
	
	public static String paraphraseCoverLetter(String path, boolean cuda) throws IOException {	
		File pFile = new File(path);
		String batFile = ".\\.\\data\\coverletters\\paraphrase.bat";
		
		// instantiate process builder to start cmd
		ProcessBuilder builder = new ProcessBuilder("cmd.exe");
		
		// sequentialize errorstream and inputstream from cmd
		builder.redirectErrorStream(true);
		
		// start process builder
		Process process = builder.start();
		
		// write into output stream from process builder
		BufferedWriter out = new BufferedWriter(new	OutputStreamWriter(process.getOutputStream()));
		
		// retrieve both streams
		BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream(), "ISO-8859-1"));
		
		// start batchfile and close outputstream - quotation marks to enable path files with spaces and boolean for cuda
		out.write("start /b "+ batFile + " " + "\"" + pFile.getAbsolutePath() +"\"" + " " + cuda);
		out.write("\n");
		out.flush();
		out.close();
		
		// read input and show console output from cmd
		String line = "";
		StringBuilder paraphrasedFile = new StringBuilder();
		
		while((line = bfr.readLine()) != null) {
			if (line.contains("coverletter20") | line.contains("Diversified") | line.contains("ValueError:")) {
				paraphrasedFile.append(line + "\n");
			}
		}
		System.out.println(paraphrasedFile.toString());
		
		// return file name of paraphrased coverletter in folder .\.\data\coverletters
		return paraphrasedFile.toString();
	}
	

	public static HashMap<String,ArrayList<String>> JSONtoMap(String jsonFile) throws IOException, ParseException {
		// initialize output hashmap, json parser, filreader for json file
		HashMap<String,ArrayList<String>> output = new HashMap<String,ArrayList<String>>();
		final String BUFFER_PATH = ".\\.\\data\\buffer\\";
		JSONParser parser = new JSONParser();
		FileReader reader = new FileReader(BUFFER_PATH + jsonFile);
		
		// parse json file into obj variable and put everything from "content" key to JSON object
		JSONObject obj = (JSONObject) parser.parse(reader);
		JSONObject content = (JSONObject) obj.get("content");
		
		// assign values from keys inside "content" object into json array objects
		JSONArray jobArray = (JSONArray) content.get("Current Job");
		JSONArray expArray = (JSONArray) content.get("Experience");
		JSONArray branchArray = (JSONArray) content.get("Branch");
		JSONArray skillsArray = (JSONArray) content.get("Skills");
		JSONArray langArray = (JSONArray) content.get("Languages");
		JSONArray titleArray = (JSONArray) content.get("Titles");
		JSONArray degArray = (JSONArray) content.get("Degrees");
		String charValue = (String) content.get("Charity");
		String driversValue = (String) content.get("Drivers License");
		
		// transfer values from json arrays to array list
		ArrayList<String> jobArrayList = new ArrayList<String>(jobArray);
		ArrayList<String> expArrayList = new ArrayList<String>(expArray);
		ArrayList<String> branchArrayList = new ArrayList<String>(branchArray);
		ArrayList<String> skillsArrayList = new ArrayList<String>(skillsArray);
		ArrayList<String> langArrayList = new ArrayList<String>(langArray);
		ArrayList<String> titleArrayList = new ArrayList<String>(titleArray);
		ArrayList<String> degArrayList = new ArrayList<String>(degArray);
		ArrayList<String> charArrayList = new ArrayList<String>();
		charArrayList.add(charValue);
		ArrayList<String> driversArrayList = new ArrayList<String>();
		driversArrayList.add(driversValue);
		
		// put array list with correct keys into hash map
		output.put("Current Job", jobArrayList);
		output.put("Experience", expArrayList);
		output.put("Branch", branchArrayList);
		output.put("Skills", skillsArrayList);
		output.put("Languages", langArrayList);
		output.put("Titles", titleArrayList);
		output.put("Degrees", degArrayList);
		output.put("Charity", charArrayList);
		output.put("Drivers License", driversArrayList);
		
		System.out.println(output);
		return output;
	}
	
	public static void main(String[] args) {
//		String path = "";
//		try {
//			CL_GeneratorUtils.paraphraseCoverLetter(path, false);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
