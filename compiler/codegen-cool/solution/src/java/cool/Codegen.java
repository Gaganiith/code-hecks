package cool;

import java.util.*;
import java.util.Map.Entry;

import cool.AST.object;

import java.io.PrintWriter;

public class Codegen{
	String globalStrings = "";
	
	public Codegen(AST.program program, PrintWriter out){
		//Write Code generator code here
		out.println("; ModuleID = '"+program.classes.get(0).filename+"'");
		out.println("source_filename = \""+program.classes.get(0).filename+"\"");
		out.println("target datalayout = \"e-m:e-i64:64-f80:128-n8:16:32:64-S128\"\n");
		out.println("@DivByZeroException = private unnamed_addr constant [32 x i8] c\"Error: Division by 0 Exception\\0A\\00\", align 1\n"
			+ "@DispatchToVoid = private unnamed_addr constant [35 x i8] c\"Error: Dispatch to void Exception\\0A\\00\", align 1\n");
		out.println("@strformatstr = private unnamed_addr constant [3 x i8] c\"%s\\00\", align 1\n"
			+ "@intformatstr = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1\n");
		out.println("declare i32 @printf(i8*, ...)\n"
			+ "declare i32 @scanf(i8*, ...)\n"
			+ "declare i8* @malloc(i64)\n"
			+ "declare i8* @strcpy(i8*, i8*)\n"
			+ "declare i8* @strncpy(i8*, i8*, i32)\n"
			+ "declare i32 @strcmp(i8*, i8*)\n"
			+ "declare i8* @strcat(i8*, i8*)\n"
			+ "declare i32 @strlen(i8*)\n"
			+ "declare void @exit(i32)");
		
		ASTTraversal traverse = new ASTTraversal();
		TypeParse typeP = new TypeParse();
	    // traverse the program
		program.accept(traverse);
		ClassData clsData = traverse.clsData;
		InheritanceGraph ig = traverse.ig;
		ExpressionTraversal exp = new ExpressionTraversal();
		PrintBaseClasses p = new PrintBaseClasses();
		Queue<String> que = new LinkedList<String>();
		que.add("Object");
	
		for(;!que.isEmpty();){
			String temp_str = "";
			String cl_queue_name = que.poll();
			
			HashMap<String,AST.attr> attrList = clsData.classes_list.get(cl_queue_name).attrlist;
			int inc =  attrList.size();
			
			for(Entry<String,AST.attr > entry : attrList.entrySet())
			{
				if(--inc>0)
					temp_str = temp_str + parse_string(entry.getValue().typeid) + ", ";
				else
					temp_str = temp_str + parse_string(entry.getValue().typeid ) + " ";

			}

			if(!cl_queue_name.equals("Object")&&!cl_queue_name.equals("IO"))
				out.println("%class."+cl_queue_name+" = type { "+ temp_str +"}");
			if(cl_queue_name.equals("Object"))
				out.println("%class.Object = type { }");
			if(cl_queue_name.equals("IO")){
				out.println("%class.IO = type { }");
			}

			int index = ig.classNameToIndexMap.get(cl_queue_name);

			ArrayList<Integer> adjChild = ig.graph.get(index);
			for(Integer cl : adjChild){
				for (Entry<String,Integer> entry : traverse.ig.classNameToIndexMap.entrySet()) {
					if (Objects.equals(cl, entry.getValue())) {
						que.add(entry.getKey());
					}
				}
				
			}

		}

		out.println();
		// Clear the queue
		que.clear(); 

		// Print Class Methods
		que.add("Object"); 
		for(;!que.isEmpty();){
			String cl_queue_name = que.poll();
			
			ArrayList<String> attrL = new ArrayList<String>();
			for(Entry<String,AST.attr > entry : clsData.classes_list.get(cl_queue_name).attrlist.entrySet())
			{
				attrL.add(entry.getKey());
				
			}

			HashMap<String,AST.attr> attrList = clsData.classes_list.get(cl_queue_name).attrlist;
			exp.printClassMethods(cl_queue_name,clsData,attrList,attrL,typeP,out);
			int index = ig.classNameToIndexMap.get(cl_queue_name);
			
			ArrayList<Integer> adjChild = ig.graph.get(index);
			
			for(Integer cl : adjChild){
				for (Entry<String,Integer> entry : traverse.ig.classNameToIndexMap.entrySet()) {
					if (Objects.equals(cl, entry.getValue())) {
						que.add(entry.getKey());
					}
				}
			}
		}
		que.clear();

		// Print the Main method
		printMain(out,exp);

		// Print String class Methods
		p.printStringMethods(out);

		// Print all global Strings
		out.println("@.str.empty = private unnamed_addr constant [1 x i8] c\"" + "\\00\"\n");
		out.println(exp.getGlobalString());

	}

	String parse_string(String t){
		if (t.equals("String"))
			return "i8*";
		else if (t.equals("Int"))
			return "i32";
		else if(t.equals("Bool"))
			return "i1";
		else {
			return "%class."+t+"*";
		}
    }
	private void printMain(PrintWriter out,ExpressionTraversal exp) {

		// Print main method
		out.println("define i32 @main() {\n"
			+"entry:\n"
			+"\t%0 = alloca %class.Main, align 4\n"
			+"\tcall %class.Main* @_ZN4MainC2EV(%class.Main* %0)\n"
			+"\tcall "+exp.getMainReturnType()+" @_ZN4Main4main(%class.Main* %0)\n"
			+"\tret i32 0\n"
			+"}");
	}
}

