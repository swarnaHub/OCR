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
public class ExtraCredit {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		List<String> smallWordsList = readFile(OCRGlobals.smallDataPath + "words.dat");
		List<String> smallImagesList = readFile(OCRGlobals.smallDataPath + "images.dat");
		
		for(int i=1; i<=10; i++) {
			System.out.println("Skip strength " + Integer.toString(i));
			OCRGlobals.skipStrength = i;
			storeIntoModels();
			//scaleTransitionFactors(2);
			//squareOCRFactors();
			calculateAccuracies(smallImagesList, smallWordsList);
			System.out.println();
		}
	}
	
	public static void scaleTransitionFactors(int scaleUpFactor) {
		for(int i=0; i<OCRGlobals.numberOfChars; i++) {
			for(int j=0; j<OCRGlobals.numberOfChars; j++) {
				TransitionModel.transitionFactors[i][j] = TransitionModel.transitionFactors[i][j]*scaleUpFactor;
				CombinedModel.transitionFactors[i][j] = CombinedModel.transitionFactors[i][j]*scaleUpFactor;
			}
		}
	}
	
	public static void squareOCRFactors() {
		for(int i=0; i<OCRGlobals.numberOfImages; i++) {
			for(int j=0; j<OCRGlobals.numberOfChars; j++) {
				OCRModel.OCRFactors[i][j] = (OCRModel.OCRFactors[i][j])*(OCRModel.OCRFactors[i][j]);
				TransitionModel.OCRFactors[i][j] = (TransitionModel.OCRFactors[i][j])*(TransitionModel.OCRFactors[i][j]);
				CombinedModel.OCRFactors[i][j] = (CombinedModel.OCRFactors[i][j])*(CombinedModel.OCRFactors[i][j]);
			}
		}
	}
	
	public static void storeIntoModels() throws IOException {
		List<String> ocrFactors = readFile(OCRGlobals.ocrFilePath);
		List<String> transFactors = readFile(OCRGlobals.transFilePath);
		
		OCRGlobals.initialize();
		
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
					CombinedModel.skipFactors[i][j] = OCRGlobals.skipStrength;
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
		
		
		CombinedModel.getPredictedWords(images, predictedWords, assignmentScoresSums);
		System.out.println("Character wise model accuracy is " + CombinedModel.charWiseAccuracy(words, predictedWords));
		System.out.println("Word wise model accuracy is " + CombinedModel.wordWiseAccuracy(images, words, predictedWords));
		System.out.println("Average dataset log-likelihood is " + CombinedModel.avgDatasetLogLikelihood(images, words, assignmentScoresSums));
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
