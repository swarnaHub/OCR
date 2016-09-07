/**
 * 
 */
package runner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import domain.CombinedModel;
import domain.OCRGlobals;
import domain.OCRModel;
import domain.TransitionModel;

/**
 * @author swarna
 *
 */
public class OCRLargeModelAccuracy {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		getArguments(args);
		storeIntoModels();
		
		List<String> wordsList = readFile(OCRGlobals.largeDataPath + "allwords.dat");
		
		for(Integer i=1; i<=5; i++) {
			System.out.println("Running with allimages" + i.toString() + ".dat");
			List<String> imagesList = readFile(OCRGlobals.largeDataPath + "allimages" + i.toString() + ".dat");
			
			calculateAccuracies(imagesList, wordsList);
		}

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
	
	public static void calculateAccuracies(List<String> imagesList, List<String> wordsList) {
		List<List<Integer> > images = new ArrayList<List<Integer> >();
		List<List<Character> > words = new ArrayList<List<Character> >();
		
		for(String image : imagesList) {
			String[] imageIndices = image.split("\t");
			
			List<Integer> tempImage = new ArrayList<Integer>();
			for(String imageIndex : imageIndices) {
				tempImage.add(Integer.parseInt(imageIndex));
			}
			
			images.add(tempImage);
		}
		
		for(String word : wordsList) {
			List<Character> tempWord = new ArrayList<Character>();
			
			for(int i=0; i<word.length(); i++) {
				tempWord.add(word.charAt(i));
			}
			
			words.add(tempWord);
		}
		
		List<List<Character> > predictedWords = new ArrayList<List<Character> >();
		List<Double> assignmentScoresSums = new ArrayList<Double>();
		
		if(OCRGlobals.modelType.equals("ocr")) {
			OCRModel.getPredictedWords(images, predictedWords, assignmentScoresSums);
			System.out.println("Character wise model accuracy is " + OCRModel.charWiseAccuracy(words, predictedWords));
			System.out.println("Word wise model accuracy is " + OCRModel.wordWiseAccuracy(words, predictedWords));
			System.out.println("Average dataset log-likelihood is " + OCRModel.avgDatasetLogLikelihood(images, words, assignmentScoresSums));
		}
		else if(OCRGlobals.modelType.equals("transition")) {
			TransitionModel.getPredictedWords(images, predictedWords, assignmentScoresSums);
			System.out.println("Character wise model accuracy is " + TransitionModel.charWiseAccuracy(words, predictedWords));
			System.out.println("Word wise model accuracy is " + TransitionModel.wordWiseAccuracy(words, predictedWords));
			System.out.println("Average dataset log-likelihood is " + TransitionModel.avgDatasetLogLikelihood(images, words, assignmentScoresSums));
		}
		else if(OCRGlobals.modelType.equals("combined")) {
			CombinedModel.getPredictedWords(images, predictedWords, assignmentScoresSums);
			System.out.println("Character wise model accuracy is " + CombinedModel.charWiseAccuracy(words, predictedWords));
			System.out.println("Word wise model accuracy is " + CombinedModel.wordWiseAccuracy(images, words, predictedWords));
			System.out.println("Average dataset log-likelihood is " + CombinedModel.avgDatasetLogLikelihood(images, words, assignmentScoresSums));
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
