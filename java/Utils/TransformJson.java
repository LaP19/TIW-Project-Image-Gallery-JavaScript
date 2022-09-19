package Utils;

import java.util.ArrayList;

public class TransformJson {
	
	/**
	 * Method that reads a jSon string of number and convert it into an arrayList of integer
	 * @param jSon is the String to be converted
	 * @return an arrayList containing the conversion of the jSon
	 */
	public static ArrayList<Integer> transformJson(String jSon){
		
		ArrayList<Integer> sortedArray = new ArrayList<Integer>();
		int num = 0;
		boolean wasNumber = false;
		boolean invalidNumber = false;
		char charLetto = ' ';
		
		//Convert the jSon into an array of integer
		for(int i = 1 ; i < jSon.length() ; i++) {
			//92 is the "\" character
			if(jSon.charAt(i) == '[' || jSon.charAt(i) == ']' || jSon.charAt(i) == 92 || jSon.charAt(i) == ',' 
									 || jSon.charAt(i) == '"' || jSon.charAt(i) == ' ') {
				if(invalidNumber) {
					//Don't add the number in the array and reset the variables
					num = 0;
					wasNumber = false;
					invalidNumber = false;
				}
				else if(wasNumber) {
					sortedArray.add(num);
					num = 0;
					wasNumber = false;
				}
				continue;
			}
			charLetto = jSon.charAt(i);
			//Check if the char read is a number
			if(charLetto < 48 || charLetto > 57) {
				invalidNumber = true;
			}
			num = num * 10 + (charLetto - 48);
			wasNumber = true;
		}
		return sortedArray;
	}
}
