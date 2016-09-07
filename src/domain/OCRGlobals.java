/**
 * 
 */
package domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author swarna
 *
 */
public class OCRGlobals {
	public static int numberOfImages = 1000;
	public static int numberOfChars = 10;
	public static double skipStrength = 5.0;
	
	public static String modelType = null;
	public static String charSet = new String("etaoinshrd");
	public static Map<Character, Integer> charsIndicesMap = new HashMap<Character, Integer>();
	
	public static String path = null; // set from command line argument
	public static String ocrFilePath = null;
	public static String transFilePath = null;
	public static String smallDataPath = null;
	public static String largeDataPath = null;
	
	public static void initialize() {
		for(int i=0; i<charSet.length(); i++) {
			charsIndicesMap.put(charSet.charAt(i), i);
		}
		
		ocrFilePath = path + "/potentials/ocr.dat";
		transFilePath = path + "/potentials/trans.dat";
		smallDataPath = path + "/data/small/";
		largeDataPath = path + "/data/large/";
	}
}
