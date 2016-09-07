/**
 * 
 */
package runner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import domain.CombinedModel;
import domain.OCRGlobals;
import domain.OCRModel;
import domain.TransitionModel;

/**
 * @author swarna
 *
 */
public class OCRModelProbability {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		getArguments(args);
		storeIntoModels();
		
		List<Integer> images = new ArrayList<Integer>();
		List<Character> chars = new ArrayList<Character>();
		
		takeInput(images, chars);
		
		runModel(images, chars);
	}
	
	public static void getArguments(String[] args) {
		OCRGlobals.path = args[0];
		String modelType = args[1];
		if(modelType.equalsIgnoreCase("ocr") || modelType.equalsIgnoreCase("transition") || 
				modelType.equalsIgnoreCase("combined")) {
			OCRGlobals.modelType = modelType;
		}
		else {
			System.out.println("Model type does not match.Exiting...");
			System.exit(0);
		}
		
		OCRGlobals.initialize();
	}
	
	public static void storeIntoModels() throws IOException {
		List<String> ocrFactors = readFile(OCRGlobals.ocrFilePath);
		List<String> transFactors = readFile(OCRGlobals.transFilePath);
		
		for(String ocrFactor : ocrFactors) {
			String []parts = ocrFactor.split("\t");
			int imageId = Integer.parseInt(parts[0]);
			int charId = OCRGlobals.charsIndicesMap.get(parts[1].charAt(0));
			
			// OCR model
			OCRModel.OCRFactors[imageId][charId] = Double.parseDouble(parts[2]);
			
			// Transition Model
			TransitionModel.OCRFactors[imageId][charId] = Double.parseDouble(parts[2]);
			
			// Combined Model
			CombinedModel.OCRFactors[imageId][charId] = Double.parseDouble(parts[2]);
		}
		
		for(String transFactor : transFactors) {
			String []parts = transFactor.split("\t");
			int charId1 = OCRGlobals.charsIndicesMap.get(parts[0].charAt(0));
			int charId2 = OCRGlobals.charsIndicesMap.get(parts[1].charAt(0));
			
			// Transition Model
			TransitionModel.transitionFactors[charId1][charId2] = Double.parseDouble(parts[2]);
			
			// Combined Model
			CombinedModel.transitionFactors[charId1][charId2] = Double.parseDouble(parts[2]);
		}
		
		for(int i=0; i<OCRGlobals.numberOfChars; i++) {
			for(int j=0; j<OCRGlobals.numberOfChars; j++) {
				if(i==j) {
					CombinedModel.skipFactors[i][j] = 5.0;
				}
				else {
					CombinedModel.skipFactors[i][j] = 1.0;
				}
			}
		}
	}
	
	public static void takeInput(List<Integer> images, List<Character> chars) {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Enter space separated list of imageIds.");
		String imageIdsInput = scan.nextLine();
		
		System.out.println("Enter the character assignment as a string.");
		String charIds = scan.nextLine();
		
		String[] imageIds = imageIdsInput.split(" ");
		if(charIds.length() != imageIds.length) {
			System.out.println("The lengths must match.Exiting...");
			System.exit(0);
		}
		
		int index = 0;
		for(String imageId : imageIds) {
			Integer image = Integer.parseInt(imageId);
			images.add(image);
			chars.add(charIds.charAt(index));
			index++;
		}
	}
	
	public static void runModel(List<Integer> images, List<Character> chars) {
		if(OCRGlobals.modelType.equals("ocr")) {
			System.out.println("Model probability is " + OCRModel.getModelProbability(images, chars));
		}
		else if(OCRGlobals.modelType.equals("transition")) {
			System.out.println("Model probability is " + TransitionModel.getModelProbability(images, chars));
		}
		else if(OCRGlobals.modelType.equals("combined")) {
			System.out.println("Model probability is " + CombinedModel.getModelProbability(images, chars));
		}
		else {
			System.out.println("The model doesn't match.Exiting...");
			System.exit(0);
		}
	}
	
	public static List<String> readFile(String filePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		
		List<String> lines = new ArrayList<>();
		
		String line = br.readLine();
		while(line != null) {
			if(!line.trim().isEmpty()) lines.add(line); 
			line = br.readLine();
		}
		
		br.close();
		return lines;
	}

}
