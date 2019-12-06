package cool;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.*;
import cool.ClassStructure;
import cool.AST.*;

public class ExpressionTraversal{

	TypeParse tP;
	static int VariableCount = -1;
	static int strCount;
	static int loopCount = -1;
	static int ifCount = -1;
	static String globalStrings = "";
	static String mainReturnType = "i32";

    ExpressionTraversal(){

		tP = new TypeParse();

	}
	String getMainReturnType(){

		return mainReturnType;
	
	}
	String getGlobalString(){

		return globalStrings;

	}
	// This method print if conditional
	public void addIfConditionBlock(String pred,PrintWriter out,int ifcnt){
		out.println("\tbr i1 "+pred.substring(4)+", label %if.then"+ifcnt+", label %if.else"+ifcnt+"\n");
		out.println("if.then"+ifcnt+":");
	}
	// This returns body label
	public String addIfBodyBlock(List<String> blocks,TypeParse tP,PrintWriter out,int tempCount){
		String ifbodylabel = blocks.get(blocks.size()-1);
		out.println("\tbr label %if.end"+tempCount+"\n");
		out.println("if.else"+tempCount+":");
		return ifbodylabel;
	}

	void checkDispatchToVoid(String caller,List<String> blocks,PrintWriter out){
		out.println("if.then"+ifCount+":");
		// Add a block if condition is true for dispatch to void
		blocks.add("if.then"+ifCount);
		// Cast Global string for dispatchtovoid  to String
		out.println("\t%"+(++VariableCount)+" = bitcast [35 x i8]* @DispatchToVoid to i8*");
		out.println("\t%"+(++VariableCount)+" = call %class.IO* @_ZN2IO10out_string( %class.IO* null, i8* %"+(VariableCount-1)+")");
		out.println("\tcall void @exit(i32 1)");
		out.println("\tbr label %if.else"+ifCount);
	}

	// This method checks if a method is in parent class
	boolean isParentMethod(ClassData cls,AST.method met,String class_name){
		
		String parentClass = cls.classes_list.get(class_name).parent;
		HashMap<String,AST.method> objectMethods = cls.classes_list.get(parentClass).methodlist;
		
		for (Map.Entry<String, AST.method> entry : objectMethods.entrySet()) {
			if(entry.getValue().equals(met)){
				return true;
			}
		}
		return false;
	}

	public int checkFormalAssignment(AST.method met,String name,int attri){
		// Check if method is not null
		if (met != null) {
            for (AST.formal f : met.formals) {
                if (f.name.equals(name)) {
                    attri = -1;
                    break;
                }
            }
		}
		return attri;
	}
	// Check Formal
	public int checkFormal(String objectname,int attr,AST.method m){
		for (AST.formal f : m.formals) {
            if (f.name.equals(objectname)) {
                attr = -1;
                break;
            }
		}
		return attr;
	}

	// This method converts class name to type name
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
    
    void printClassMethods(String cl_queue_name,ClassData clsData,HashMap<String,AST.attr> attrList,ArrayList<String> attrL,TypeParse typeP,PrintWriter out){
		
		// Print methods for Object class
		PrintBaseClasses pbc = new PrintBaseClasses();
		if (cl_queue_name.equals("Object")) {
			pbc.printObjectMethods(out);
			return;
		}else if (cl_queue_name.equals("IO")) {
			// Print methods for IO class
			pbc.printIOMethods(out);
			return;
		}

		// Print Constructor
		// get formals for the constructor
		String formals = parse_string(cl_queue_name)+" %self";

		// Print the definition
		out.println("define "+parse_string(cl_queue_name)+" @_ZN"+cl_queue_name.length()+cl_queue_name+"C2EV( "+formals+" ) {");
		out.println("entry:");
		// Allocate object for class
		out.print("\t" + "%self.addr" + " = alloca " + parse_string(cl_queue_name) + "\n");
		out.print("\tstore " + parse_string(cl_queue_name) + " " + "%self" + ", " + parse_string(cl_queue_name) + "* " + "%self.addr" + "\n");
		out.print("\t%" +"self1"+ " = load " + parse_string(cl_queue_name) + ", " + parse_string(cl_queue_name) + "* " + "%self.addr" + "\n");
		List<String> blocks = new ArrayList<>();
		String returnValue = "";
		// iterate through Attributes
		VariableCount = -1;
		for(Entry<String,AST.attr > entry : attrList.entrySet()){	
			// Get attribute of the class
			AST.attr a = entry.getValue(); 
			if (a.typeid.equals("Bool")) {
				// Get the index of the attribute
				int attri = attrL.indexOf(a.name);
				String ctype = parse_string(cl_queue_name);
				ctype = ctype.substring(0, ctype.length()-1);
				String temp = "self1";
				out.println("\t%"+a.name+" = getelementptr inbounds "+ctype+", "+ctype+"* %"+temp+", i32 0, i32 "+attri);
				returnValue = parse_string(cl_queue_name) + " %"+temp;
                if (a.value.getClass() != AST.no_expr.class && a.value.getClass() != AST.new_.class) {
					visit(a.value,cl_queue_name, null,new ArrayList<>(),attrL,blocks,out,clsData);
					out.print("\tstore " + parse_string(a.typeid) + " %" + (VariableCount) + ", " + parse_string(a.typeid) + "* %" +a.name + "\n");
                } else {
					
					out.println("\tstore "+parse_string(a.typeid)+" false, "+parse_string(a.typeid)+"* %"+a.name+", align 4");
                }
			
			} else if (a.typeid.equals("String")) {
				// Get the index of the attribute
				int attri = attrL.indexOf(a.name);
				String ctype = parse_string(cl_queue_name);
				ctype = ctype.substring(0, ctype.length()-1);
				String temp = "self1";
				out.println("\t%"+a.name+" = getelementptr inbounds "+ctype+", "+ctype+"* %"+temp+", i32 0, i32 "+attri);
				String length = null;
				returnValue = parse_string(cl_queue_name) + " %"+temp;
                if (a.value.getClass() != AST.no_expr.class && a.value.getClass() != AST.new_.class) {
					visit(a.value,cl_queue_name, null,new ArrayList<>(),attrL,blocks,out,clsData);
					out.print("\tstore " + "i8*" + " %" + (VariableCount) + ", " + "i8*" + "* %" +a.name + "\n");
                } else {
					length = "[" + 1 + " x i8]";
					out.println("\tstore i8* getelementptr inbounds (" + length + ", " + length + "* @.str.empty , i32 0, i32 0), i8** %" + a.name);
                }
			
			} else if(a.typeid.equals("Int")){
				// Get the index of the attribute
				int attri = attrL.indexOf(a.name);
				String ctype = parse_string(cl_queue_name);
				ctype = ctype.substring(0, ctype.length()-1);
				String temp = "self1";
				returnValue = parse_string(cl_queue_name) + " %"+temp;
				out.println("\t%"+a.name+" = getelementptr inbounds "+ctype+", "+ctype+"* %"+temp+", i32 0, i32 "+attri);
				
                if (a.value.getClass() != AST.no_expr.class && a.value.getClass() != AST.new_.class) {
					visit(a.value,cl_queue_name, null,new ArrayList<>(),attrL,blocks,out,clsData);
					out.print("\tstore " + parse_string(a.typeid) + " %" + (VariableCount) + ", " + parse_string(a.typeid) + "* %" +a.name + "\n");
                } else {
					out.println("\tstore "+parse_string(a.typeid)+" 0, "+parse_string(a.typeid)+"* %"+a.name+", align 4");
                }
			}else{
				// Get the index of the attribute
				int attri = attrL.indexOf(a.name);
				String ctype = parse_string(cl_queue_name);
				ctype = ctype.substring(0, ctype.length()-1);
				String temp = "self1";
				returnValue = parse_string(cl_queue_name) + " %"+temp;
				out.println("\t%"+(a.name)+" = getelementptr inbounds "+ctype+", "+ctype+"* %"+temp+", i32 0, i32 "+attri);
				if ((a.value.getClass() != AST.no_expr.class)) {
					visit(a.value,cl_queue_name, null,new ArrayList<>(),attrL,blocks,out,clsData);
					out.print("\tstore " + parse_string(a.typeid) + " %" + (VariableCount) + ", " + parse_string(a.typeid) + "* %" +a.name + "\n");
                } else {
                    out.println("\tstore "+parse_string(a.typeid)+"null, "+parse_string(a.typeid)+"* %"+a.name+", align 4");
                }
			}
		}
	
		ClassStructure clStr = clsData.classes_list.get(cl_queue_name);
		
		String ty = "["+(cl_queue_name.length()+1)+" x i8]";
		
		globalStrings += "@.str"+(strCount++)+" = private unnamed_addr constant "+ty+" c\""+cl_queue_name+"\\00\", align 1\n";
		
		if(cl_queue_name.equals("Main")){
			returnValue = "%class.Main* %self1";
		}
		out.println("\tret "+returnValue);
		out.println("}\n");
		
		printOtherMethods(typeP,cl_queue_name,clStr,attrL,out,clsData);
	}

	
	// This method prints all remaining methods of the class
	void printOtherMethods(TypeParse typeP,String c,ClassStructure clStr,ArrayList<String> attrL,PrintWriter out,ClassData clsData){
		
		// Print methods defined by user
		// Placeholder for method formals
		String formals = " ";

		// Create list of basic blocks in the method
		ArrayList<String> blocks = new ArrayList<>();
		
		// For each method in the class Do following
	    for (Map.Entry<String, AST.method> entry : clsData.classes_list.get(c).methodlist.entrySet()) {
			
			AST.method m = entry.getValue();
			
			// Skip printing methods of the parent class
			if(isParentMethod(clsData,m,c)){
				continue;
			}
			// Get the formals of the method
	    	formals = "%class."+c+"* %self";
	    	for (AST.formal f : m.formals) {
	    		formals += ", " + parse_string(f.typeid)+" %"+f.name;
	    	}
	    	blocks.clear();
	    	// Set return type for main function in Main class
	    	if (c.equals("Main") && entry.getKey().equals("main"))
	    		mainReturnType = parse_string(m.typeid);

			// start defining the function
			out.println("define "+parse_string(m.typeid)+" "+clsData.classes_list.get(c).methodToMethodName.get(entry.getKey())+"( "+formals+" )" + "{\n"
						+ "entry:");
			// Create a new basic block named entry
			blocks.add("entry");
			// Re initialize the counters
	        VariableCount = -1;
	        loopCount = -1;
	        ifCount = -1;
	        List<String> newFormals = new ArrayList<>();
	        // Allot all formals in the stack
			for (AST.formal f : m.formals) {
				String type = parse_string(f.typeid);
				out.println("%"+f.name+".addr = alloca "+type+", align 4\n" 
							+ "\tstore "+type+" %"+f.name+", "+type+"* %"+f.name+".addr, align 4");
				newFormals.add(f.name);
			}
			// Print the IR of the Method body
			ExpressionTraversal trav = new ExpressionTraversal();
	        String ret = trav.visit(m.body,c, m,newFormals,attrL,blocks,out,clsData);
			String rettype = typeP.ParseTypeValue(ret);
			
	        // Return Statement Printing 
	        if (!rettype.equals(parse_string(m.typeid))) {
	        	if (rettype.equals("i32")) {
					out.println("\t%"+(++VariableCount)+" = call noalias i8* @malloc(i64 8)\n"
								+ "\t%"+(++VariableCount)+" = bitcast i8* %"+(VariableCount-1)+" to "+parse_string(m.typeid));
	        	} else {
	        		out.println("\t%"+(++VariableCount)+" = bitcast "+ret+" to "+parse_string(m.typeid));	        		
	        	}
	        	ret = parse_string(m.typeid)+" %"+VariableCount;
	        }
			out.println("\tret "+ret+"\n"
						+"}\n");
	    }
    }
    

	public String visit(AST.int_const int_const,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData) {
		
		// if expression has int type
		out.print("\t%" + (++VariableCount) + " = alloca " + "i32" + "\n");
		out.print("\tstore " + "i32" + " " + int_const.value + ", " + "i32" + "* %" + VariableCount + "\n");
		int temp = VariableCount;
		out.print("\t%" + (++VariableCount) + " = load " + "i32" + ", " + "i32" + "* %" + temp + "\n");

		String tempN = "i32 " + int_const.value;
		return tempN;
	}

	public String visit(AST.string_const string_const,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
		
		// if expression has string type
		String ty = "["+(string_const.value.length()+1)+" x i8]";
		String value = "";
		if(string_const.value.equals("\n")){
			value = "\n";
		}else{
			value = string_const.value;
		}
        globalStrings += "@.str"+(strCount++)+" = private unnamed_addr constant "+ty+" c\""+value+"\\00\", align 1\n";
		
		out.println("\t%" + (++VariableCount) + " = alloca " + "i8*");
		out.println("\tstore i8* getelementptr inbounds ([" + String.valueOf(string_const.value.length() + 1) + " x i8], [" + String.valueOf(string_const.value.length() + 1) + " x i8]* @.str" + (strCount-1) + ", i32 0, i32 0), i8** %" + String.valueOf(VariableCount));
		int temp = VariableCount;
		out.println("\t%" + (++VariableCount) + " = load " + "i8*" + ", " + "i8" + "** %" + temp);
        
        return "i8* %"+VariableCount;
	}


	public String visit(AST.bool_const bool_const,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
		
		// if expression has bool type
		out.print("\t%" + (++VariableCount) + " = alloca " + "i1" + "\n");
		out.print("\tstore " + "i1" + " " + bool_const.value + ", " + "i1" + "* %" + VariableCount + "\n");
		int temp = VariableCount;
		out.print("\t%" + (++VariableCount) + " = load " + "i1" + ", " + "i1" + "* %" + temp + "\n");
		return "i1 " + (bool_const.value ? 1 : 0);
	}

	public String visit(AST.expression expr,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData) {
	 
		// visit the expression
		if(expr.getClass() == AST.assign.class) {
            return visit((AST.assign)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.static_dispatch.class) {
            return visit((AST.static_dispatch)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.dispatch.class) {
            return visit((AST.dispatch)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.cond.class) {
            return visit((AST.cond)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.loop.class) {
            return visit((AST.loop)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.block.class) {
            return visit((AST.block)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.let.class) {
            return visit((AST.let)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.typcase.class) {
            return visit((AST.typcase)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.new_.class) {
            return visit((AST.new_)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.isvoid.class) {
            return visit((AST.isvoid)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.plus.class) {
            return visit((AST.plus)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.sub.class) {
            return visit((AST.sub)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.mul.class) {
            return visit((AST.mul)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.divide.class) {
            return visit((AST.divide)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.comp.class) {
            return visit((AST.comp)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.lt.class) {
            return visit((AST.lt)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.leq.class) {
            return visit((AST.leq)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.eq.class) {
            return visit((AST.eq)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.neg.class) {
            visit((AST.neg)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.object.class) {
            return visit((AST.object)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.int_const.class) {
            return visit((AST.int_const)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.string_const.class) {
            return visit((AST.string_const)expr,cname, method,formal,attrL,blocks,out,clsData);
        } else if(expr.getClass() == AST.bool_const.class) {
            return visit((AST.bool_const)expr,cname, method,formal,attrL,blocks,out,clsData);
        }
        return "";
	}
	
	public String visit(AST.assign asgn,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData) {

		// Visit the assignment expression and print its IR
		String e1 = visit(asgn.e1,cname, method,formal,attrL,blocks,out,clsData);
		
		// Get the index of attribute
		int attri = attrL.indexOf(asgn.name);
		
		// Check if assignment is to formal 
		attri = checkFormalAssignment(method,asgn.name,attri);
        
		String type = parse_string(asgn.type);
		
        String stype = parse_string(cname);
        stype = stype.substring(0, stype.length()-1);
		String e1type = tP.ParseTypeValue(e1);
		
        // Bitcast of types not same
        if (!e1type.equals(type)) {
            if (e1type.equals("i32")) {
				out.println("\t%"+(++VariableCount)+" = call noalias i8* @malloc(i64 8)"+"\n"
							+ "\t%"+(++VariableCount)+" = bitcast i8* %"+(VariableCount-1)+" to "+type);
            } else {
                out.println("\t%"+(++VariableCount)+" = bitcast "+e1+" to "+type);	        		
            }
            e1 = type+" %"+VariableCount;
        }
		// Check if formal is again assigned
		switch(attri){
			case -1: if (formal.indexOf(asgn.name) == -1) {
                		 out.println("%"+asgn.name+".addr = alloca "+type+", align 4");
               			 formal.add(asgn.name);
            			}
             			out.println("\tstore "+e1+", "+type+"* %"+asgn.name+".addr, align 4");
						return e1;
			default : out.println("\t%"+(++VariableCount)+" = getelementptr inbounds "+stype+", "+stype+"* %self, i32 0, i32 "+attri);
           			  out.println("\tstore "+e1+", "+type+"* %"+VariableCount+", align 4");
                      return e1;
		}
	}
	
	public String visit(AST.static_dispatch staticdis,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
			
			// Emit the IR for the caller
			String caller = visit(staticdis.caller,cname, method,formal,attrL,blocks,out,clsData);
			// For each parameter in the static dispatch
			List<String> actuals = new ArrayList<>();

			// Visiting Each of the actuals
			for (AST.expression actual : staticdis.actuals) {
				String act = visit(actual,cname, method,formal,attrL,blocks,out,clsData);
				actuals.add(act);
			}
			// Check if dispatch to void
			ifCount++;
			out.println("\t%"+(++VariableCount)+" = icmp eq "+caller+", null");
			out.println("\tbr i1 %"+VariableCount+", label %if.then"+ifCount+", label %if.else"+ifCount+"\n");
			checkDispatchToVoid(caller,blocks,out);
			out.println("\nif.else"+ifCount+":");

			blocks.add("if.else"+ifCount);

			// Call the required function
			// Mangle function name
			String funcname = "@_ZN"+staticdis.typeid.length()+staticdis.typeid+staticdis.name.length()+staticdis.name;
			
			ClassStructure cStr = clsData.classes_list.get(parse_string(tP.ParseTypeValue(caller)));
			
			while (!tP.ParseTypeValue(caller).equals(parse_string(staticdis.typeid))) {
				String par = parse_string(cStr.parent);
				par = par.substring(0, par.length()-1);
				String ty = tP.ParseTypeValue(caller);
				ty = ty.substring(0, ty.length()-1);
				out.println("\t%"+(++VariableCount)+" = getelementptr inbounds "+ty+", "+ty+"* "+tP.ParseTypeValueVar(caller)+", i32 0, i32 0");
				caller = par+"* %"+VariableCount;
				cStr = clsData.classes_list.get(cStr.parent);
			}
			// Get the string for actuals of the method call
			String actualsStr = caller;
			int i = 0;
			while(i<actuals.size()){
				actualsStr += ", " + actuals.get(i);
				i++;
			}

			out.println("\t%"+(++VariableCount)+" = call "+parse_string(staticdis.type)+" "+funcname+"("+actualsStr+")");
			
			return parse_string(staticdis.type)+" %"+VariableCount;

    }
	
	public String visit(AST.cond cond,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData) {

		// Add Conditional blocks
		ifCount++;
		int tempCount = ifCount;

		// Print If Condition
		String pred = visit(cond.predicate,cname, method,formal,attrL,blocks,out,clsData);
		addIfConditionBlock(pred,out,tempCount);
		
		blocks.add("if.then"+tempCount);

		// Print If Body Expression
		String bodyExp = visit(cond.ifbody,cname, method,formal,attrL,blocks,out,clsData);
		String ifBody = addIfBodyBlock(blocks,tP,out,tempCount);

		bodyExp = tP.ParseTypeValueVar(bodyExp);

		blocks.add("if.else"+tempCount);

		// Print Else Body
		String elsebody = visit(cond.elsebody,cname, method,formal,attrL,blocks,out,clsData);
		
		String elsebodylabel = blocks.get(blocks.size()-1);
		elsebody = tP.ParseTypeValueVar(elsebody);
		out.println("\tbr label %if.end"+tempCount+"\n");

		out.println("if.end"+tempCount+":");
		blocks.add("if.end"+tempCount);

		// Add a PHI NODE
		out.println("\t%"+(++VariableCount)+" = phi "+parse_string(cond.type)
			+" ["+bodyExp+", %"+ifBody+"], ["+elsebody+", %"+elsebodylabel+"]");
		return parse_string(cond.type)+" %"+VariableCount;
	}
	
	public String visit(AST.loop lp,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData) {

		// Blocks for loop condition body and end
		loopCount++;
        int lCount = loopCount;
		out.println("\tbr label %loop.cond"+lCount+"\n\n"
					+ "loop.cond"+lCount+":" );

		// Add Loop Condition
        blocks.add("loop.cond"+lCount);
        String pred = visit(lp.predicate,cname, method,formal,attrL,blocks,out,clsData);
		out.println("\tbr i1 "+pred.substring(4)+", label %loop.body"+lCount+" , label %loop.end"+lCount+"\n\n"
					+"loop.body"+lCount+":" );

		// Add Loop Body Block
        blocks.add("loop.body"+lCount);
        String body = visit(lp.body,cname, method,formal,attrL,blocks,out,clsData);
		out.println("\tbr label %loop.cond"+lCount+"\n\n"
					+ "loop.end"+lCount+":");
		
		// Loop End block
        blocks.add("loop.end"+lCount);
        return body;
	}

	public String visit(AST.block blck,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData) {

		String returnValue = "";
		// For each expression the the block emit the IR
        for (AST.expression ex : blck.l1) {
            returnValue = visit(ex,cname, method,formal,attrL,blocks,out,clsData);
        }
        return returnValue;
	}


	public String visit(AST.new_ nw_,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData){

		String type = parse_string(nw_.typeid);
		// Get the size of the object of class
		int size = clsData.classes_list.get(nw_.typeid).size;
		
		out.println("\t%"+(++VariableCount)+" = call noalias i8* @malloc(i64 "+size+")"+"\n"
					+ "\t%"+(++VariableCount)+" = bitcast i8* %"+(VariableCount-1)+" to "+type+"\n"
					+ "\t%"+(++VariableCount)+" = call "+parse_string(nw_.typeid)+" @_ZN"+nw_.typeid.length()+nw_.typeid+"C2EV( "+type+" %"+(VariableCount-1)+" )");
        
        return type+" %"+(VariableCount-1);
	}

	public String visit(AST.isvoid isvoid,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData){

        String e1 = visit(isvoid.e1,cname, method,formal,attrL,blocks,out,clsData);
	    // Print IR for null condition
		out.println("\t%"+(++VariableCount)+" = icmp eq "+e1+", null");
        return "i32 %"+VariableCount;
	}

	public String visit(AST.plus pls,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData){

        String e1 = visit(pls.e1,cname, method,formal,attrL,blocks,out,clsData);
        String e2 = visit(pls.e2,cname, method,formal,attrL,blocks,out,clsData);
		// Print IR for add binary operation
		out.println("\t%"+(++VariableCount)+" = add nsw i32 " + e1.substring(4) + ", " + e2.substring(4));
        return "i32 %"+VariableCount;
	}

	public String visit(AST.sub sub,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
		String e1 = visit(sub.e1,cname, method,formal,attrL,blocks,out,clsData);
		String e2 = visit(sub.e2,cname, method,formal,attrL,blocks,out,clsData);
		// Print IR for subtraction binary operation
		out.println("\t%"+(++VariableCount)+" = sub nsw i32 " + e1.substring(4) + ", " + e2.substring(4));
		return "i32 %"+VariableCount;
	}

	public String visit(AST.mul mul,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
		String e1 = visit(mul.e1,cname, method,formal,attrL,blocks,out,clsData);
		String e2 = visit(mul.e2,cname, method,formal,attrL,blocks,out,clsData);
		// Print the IR for Multiplication operation
		out.println("\t%"+(++VariableCount)+" = mul nsw i32 " + e1.substring(4) + ", " + e2.substring(4));
		return "i32 %"+VariableCount;
	}

	public String visit(AST.divide divide,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
		
		String e1 = visit(divide.e1,cname, method,formal,attrL,blocks,out,clsData);
        String e2 = visit(divide.e2,cname, method,formal,attrL,blocks,out,clsData);
		
		// Print the IR for Divide operation

		out.println("\t%"+(++VariableCount)+" = icmp eq i32 0, "+e2.substring(4));
		
		ifCount++;
        // check for divide by zero runtime exception
		out.println("\tbr i1 %"+VariableCount+", label %if.then"+ifCount+", label %if.else"+ifCount+"\n\n"
					+ "if.then"+ifCount+":"+"\n"
					+ "\t%"+(++VariableCount)+" = bitcast [32 x i8]* @DivByZeroException to i8*"+"\n"
					+ "\t%"+(++VariableCount)+" = call %class.IO* @_ZN2IO10out_string( %class.IO* null, i8* %"+(VariableCount-1)+")"+"\n"
					+ "\tcall void @exit(i32 1)"+"\n"
					+ "\tbr label %if.else"+ifCount+"\n\n"
					+ "if.else"+ifCount+":\n" );
		blocks.add("if.then"+ifCount);
        blocks.add("if.else"+ifCount);
        out.println("\t%"+(++VariableCount)+" = sdiv i32 " + e1.substring(4) + ", " + e2.substring(4));
        return "i32 %"+VariableCount;      
	}

	public String visit(AST.lt lt,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
        String e1 = visit(lt.e1,cname, method,formal,attrL,blocks,out,clsData);
		String e2 = visit(lt.e2,cname, method,formal,attrL,blocks,out,clsData);
		// Print IR for less than condition
        out.println("\t%"+(++VariableCount)+" = icmp slt i32 " + e1.substring(4) + ", " + e2.substring(4));
        return "i32 %"+VariableCount;
	}

	public String visit(AST.leq leq,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
		String e1 = visit(leq.e1,cname, method,formal,attrL,blocks,out,clsData);
        String e2 = visit(leq.e2,cname, method,formal,attrL,blocks,out,clsData);
		// Print IR for Greater than condition
		out.println("\t%"+(++VariableCount)+" = icmp sle i32 " + e1.substring(4) + ", " + e2.substring(4));
        return "i32 %"+VariableCount;
	}

	public String visit(AST.eq eq,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
       
        String e1 = visit(eq.e1,cname, method,formal,attrL,blocks,out,clsData);
        String e2 = visit(eq.e2,cname, method,formal,attrL,blocks,out,clsData);
		// Print IR for equals to condition
		out.println("\t%"+(++VariableCount)+" = icmp eq i32 " + e1.substring(4) + ", " + e2.substring(4));
        return "i32 %"+VariableCount;
        
	}

	public String visit(AST.comp comp,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {

		String e1 = visit(comp.e1,cname, method,formal,attrL,blocks,out,clsData);
	    // Print IR for complement operation
		out.println("\t%"+(++VariableCount)+" = sub nsw i32 1, " + e1.substring(4));
        return "i32 %"+VariableCount;
        
	}

	public void visit(AST.neg neg,String cname, AST.method method,List<String> formal, ArrayList<String> attrL,List<String> blocks, PrintWriter out,ClassData clsData) {
		String e1 = visit(neg.e1,cname, method,formal,attrL,blocks,out,clsData);
		// Print IR for negation operation
		out.println("\t%"+(++VariableCount)+" = sub nsw i32 0, " + e1.substring(4));
	}
	
	public String visit(AST.object obj,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData) {
		// get attribute index
		int attri = attrL.indexOf(obj.name);
		
		// check if it is a formal
		attri = checkFormal(obj.name,attri,method);
		
		switch(attri){
			case -1: if (formal.indexOf(obj.name) == -1)
						return parse_string(obj.type) + " %"+obj.name;
					else {
						String ty = parse_string(obj.type);
						out.println("\t%"+(++VariableCount)+" = load "+ty+", "+ty+"* %"+obj.name+".addr, align 4");
						return ty+" %"+VariableCount;
					}
		}

        String parseTypeName = parse_string(cname);
        parseTypeName = parseTypeName.substring(0, parseTypeName.length()-1);
		out.println("\t%"+(++VariableCount)+" = getelementptr inbounds "+parseTypeName+", "+parseTypeName+"* %self, i32 0, i32 "+attri+"\n"
					+ "\t%"+(++VariableCount)+" = load "+parse_string(obj.type)+", "+parse_string(obj.type)+"* %"+(VariableCount-1)+", align 4");
        return parse_string(obj.type)+" %"+VariableCount;
    }

	public String visit(AST.dispatch disp,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData){
		return null;
	}
	public String visit(AST.let let,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData){
		return null;
	}
	public String visit(AST.typcase typ,String cname, AST.method method,List<String> formal,ArrayList<String> attrL, List<String> blocks, PrintWriter out,ClassData clsData){
		return null;
	}
}