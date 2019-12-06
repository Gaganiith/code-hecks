package cool;

import java.util.*;
import cool.AST.*;

public class TypeParse{


    // Return type in cool given return type in LLVM-IR
	String ParseType(String type){
		if (type.equals("i32")) {
			return "Int";
		} else if(type.equals("i1")){
			return "Bool";
		}
		else if (type.equals("i8*")) {
			return "String";
		} else {
			return type.substring(7, type.length()-1);
		}
	}

	// Return type with %
	String ParseTypeValue(String type) {
		
		if(type==null){
			return "";
		}
		if (type.length() > 2 && type.substring(0, 2).equals("i8*")) {
			return "i8*";
		} else {
			return type.split(" ")[0];
		}
	}

	// return variable
	String ParseTypeValueVar(String type) {

		String[] value = type.split(" ");
		return value[value.length-1];

	}


}