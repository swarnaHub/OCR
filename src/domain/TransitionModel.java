/**
 * 
 */
package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author swarna
 *
 */
public class TransitionModel {
	public static Double[][] OCRFactors = new Double[OCRGlobals.numberOfImages][OCRGlobals.numberOfChars];
	public static Double[][] transitionFactors = new Double[OCRGlobals.numberOfChars][OCRGlobals.numberOfChars];
	
	public static Double getModelScore(List<Integer> imageIds, List<Character> chars) {
		if(imageIds.size() != chars.size()) {
			System.out.println("Assignment must have same size. Exiting...");
			System.exit(0);
		}
		
		Double score = 0.0;
		for(int i=0; i<imageIds.size(); i++) {
			int charid = OCRGlobals.charsIndicesMap.get(chars.get(i));
			Double factor = OCRFactors[imageIds.get(i)][charid];
			score += Math.log(factor);
		}
		
		for(int i=0; i<imageIds.size()-1; i++) {
			int charId1 = OCRGlobals.charsIndicesMap.get(chars.get(i));
			int charId2 = OCRGlobals.charsIndicesMap.get(chars.get(i+1));
			Double factor = transitionFactors[charId1][charId2];
			score += Math.log(factor);
		}
		
		return Math.exp(score);
	}
	
	public static Double getModelProbability(List<Integer> imageIds, List<Character> chars) {
		return getModelScore(imageIds, chars)/getNormalizationConstant(imageIds);
	}
	
	public static void getPredictedWords(List<List<Integer> > images, List<List<Character> > predictedWords,
			List<Double> assignmentScoresSums) {
		for(int i=0; i<images.size(); i++) {
			List<Character> tempWord = new ArrayList<Character>();
			
			List<Character> predictedWord = new ArrayList<Character>();
			
			List<Double> bestScore = new ArrayList<Double>();
			bestScore.add(-1.0);
			List<Double> assignmentScoresSum = new ArrayList<Double>();
			assignmentScoresSum.add(0.0);
			
			getPredictedWordUtil(images.get(i), tempWord, 0, predictedWord, assignmentScoresSum, bestScore);
			
			assignmentScoresSums.add(assignmentScoresSum.get(0));
			predictedWords.add(predictedWord);
			
		}
	}
	
	private static void getPredictedWordUtil(List<Integer> imageIds, List<Character> word, int index, List<Character> predictedWord,
			List<Double> assignmentScoresSum, List<Double> bestScore) {
		
		if(index == imageIds.size()) {
			Double modelScore = getModelScore(imageIds, word);
			
			if(modelScore > bestScore.get(0)) {
				bestScore.set(0, modelScore);
				predictedWord.clear();
				for(Character ch : word) {
					predictedWord.add(ch);
				}
			}
			Double sum = assignmentScoresSum.get(0);
			assignmentScoresSum.set(0, sum+modelScore);
		}
		else {
			for(int i=0; i<OCRGlobals.numberOfChars; i++) {
				word.add(OCRGlobals.charSet.charAt(i));
				getPredictedWordUtil(imageIds, word, index+1, predictedWord, assignmentScoresSum, bestScore);
				word.remove(word.size()-1);
			}
		}
	}
	
	public static Double charWiseAccuracy(List<List<Character> > words, List<List<Character> > predictedWords) {
		int correctlyPredicted = 0;
		int totalChars = 0;
		
		for(int i=0; i<words.size(); i++) {
			
			List<Character> predictedWord = predictedWords.get(i);
			
			List<Character> actualWord = words.get(i);
			
			totalChars += actualWord.size();
			
			for(int j=0; j<predictedWord.size(); j++) {
				if(actualWord.get(j) == predictedWord.get(j)) {
					correctlyPredicted++;
				}
			}
		}
		
		return (double)correctlyPredicted/totalChars;
	}
	
	public static Double wordWiseAccuracy(List<List<Character> > words, List<List<Character> > predictedWords) {
		int correctlyPredicted = 0;
		int totalWords = words.size();
		
		for(int i=0; i<words.size(); i++) {
			
			List<Character> predictedWord = predictedWords.get(i);
			
			List<Character> actualWord = words.get(i);
			
			boolean areEqual = true;
			
			for(int j=0; j<predictedWord.size(); j++) {
				if(predictedWord.get(j) != actualWord.get(j)) {
					areEqual = false;
					break;
				}
			}
			
			if(areEqual) {
				correctlyPredicted++;
			}
		}
		
		return (double)correctlyPredicted/totalWords;
	}
	
	public static Double avgDatasetLogLikelihood(List<List<Integer> > images, List<List<Character> > words,
			List<Double> assignmentScoresSums) {
		Double logLikelihood = 0.0;
		
		for(int i=0; i<images.size(); i++) {
			Double modelProbability = getModelScore(images.get(i), words.get(i))/assignmentScoresSums.get(i);
			logLikelihood += Math.log(modelProbability);
		}
		
		return logLikelihood/images.size();
	}
	
	private static Double getNormalizationConstant(List<Integer> imageIds) {
		List<Character> word = new ArrayList<Character>();
		List<Double> totalScore = new ArrayList<Double>();
		totalScore.add(0.0);
		getTotalScore(imageIds, word, 0, totalScore);
		return totalScore.get(0);
	}
	
	private static void getTotalScore(List<Integer> imageIds, List<Character> word, int index, List<Double> totalScore) {
		if(index == imageIds.size()) {
			Double score = totalScore.get(0);
			score += getModelScore(imageIds, word);
			totalScore.set(0, score);
		}
		else {
			for(int i=0; i<OCRGlobals.numberOfChars; i++) {
				word.add(OCRGlobals.charSet.charAt(i));
				getTotalScore(imageIds, word, index+1, totalScore);
				word.remove(word.size()-1);
			}
		}
	}
}
