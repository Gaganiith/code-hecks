package cool;
import java.util.*;
public class Semantic{
	private boolean errorFlag = false;
	public void reportError(String filename, int lineNo, String error){
		errorFlag = true;
		System.err.println(filename+":"+lineNo+": "+error);
		
	}
	public boolean getErrorFlag(){
		return errorFlag;
	}

/*
	Don't change code above this line
*/
public Semantic(AST.program program){
	
	
	//Write Semantic analyzer code here

	// ASTTraversal object to visit the program using Visitor Pattern
	ASTTraversal traverse = new ASTTraversal();
	
	// traverse the program
	program.accept(traverse);
	
}
	

public Semantic(){

}

}
