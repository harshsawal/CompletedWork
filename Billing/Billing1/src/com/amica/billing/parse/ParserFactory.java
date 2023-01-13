package com.amica.billing.parse;

public class ParserFactory {

	public static Parser createParser(String fileName) {

		switch(getFileExtension(fileName)) {
		case ".csv" : return new CSVParser();
		case ".flat" : return new FlatParser();
		default: return null;
		}
		
//		if(getFileExtension(fileName).equals(".csv")) {
//			
//		}
//		else if() {
//			
//		}
	}

	public static String getFileExtension(String fileName) {

		// get the ending extension
		String extension = "";
		if (fileName.contains(".")) {
			extension = fileName.substring(fileName.lastIndexOf("."));
		}
		return extension;
	}
}
