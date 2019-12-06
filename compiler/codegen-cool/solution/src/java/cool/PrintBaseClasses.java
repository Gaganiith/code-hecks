package cool;

import java.io.PrintWriter;
import java.util.*;
import cool.AST.*;

public class PrintBaseClasses{
    
    // Print The Constructor and methods of Object Class
	void printObjectMethods(PrintWriter out) {

		// Print Object class constructor
		out.println("define %class.Object* @_ZN6ObjectC2EV( %class.Object* %self ) noreturn {\n"
			+ "entry:\n"
			+ "\t%self.addr = alloca %class.Object*\n"
			+ "\tstore %class.Object* %self, %class.Object** %self.addr\n"
			+ "\t%self1 = load %class.Object*, %class.Object** %self.addr\n"
			+ "\tret %class.Object* %self1\n"
			+"}\n");

		out.println("define %class.Object* @_ZN6Object5abort( %class.Object* %self ) noreturn {\n"
			+ "entry:\n"
			+ "\tcall void @exit( i32 1 )\n"
			+ "\tret %class.Object* null\n"
			+ "}\n");
	}

	// Print all the methods of String class
	void printStringMethods(PrintWriter out) {

		out.println("define i32 @_ZN6String6length( i8* %self ) {\n"
			+ "\tentry:\n"
			+ "\t%retval = call i32 @strlen( i8* %self )\n"
			+ "\tret i32 %retval\n"
			+ "}\n");

		out.println("define i8* @_ZN6String6substr( i8* %self, i32 %start, i32 %len ) {\n"
			+ "entry:\n"
			+ "\t%0 = call i8* @malloc( i64 1024 )\n"
			+ "\t%1 = bitcast i8* %0 to i8*\n"
			+ "\t%2 = getelementptr inbounds i8, i8* %self, i32 %start\n"
			+ "\t%3 = call i8* @strncpy( i8* %0, i8* %2, i32 %len )\n"
			+ "\t%4 = getelementptr inbounds [1 x i8], [1 x i8]* @.str.empty, i32 0, i32 0\n"
			+ "\t%retval = call i8* @strcat( i8* %3, i8* %4 )\n"
			+ "\tret i8* %retval\n"
			+ "}\n");

		out.println("define i8* @_ZN6String6concat( i8* %self, i8* %that ) {\n"
		+ "entry:\n"
		+ "\t%mnew = call i8* @malloc( i64 1024 )\n"
		+ "\t%0 = bitcast i8* %mnew to i8*\n"
		+ "\t%copystr = call i8* @strcpy( i8* %0, i8* %self )\n"
		+ "\t%retval = call i8* @strcat( i8* %copystr, i8* %that )\n"
		+ "\tret i8* %retval\n"
		+ "}\n");
		
	}

	// Print all the methods of IO Class
	void printIOMethods(PrintWriter out) {
		// Print constructor of IO class
		out.println("define %class.IO* @_ZN2IOC2EV( %class.IO* %self ) noreturn {\n"
			+ "entry:\n"
			+ "\t%self.addr = alloca %class.IO*\n"
			+ "\tstore %class.IO* %self, %class.IO** %self.addr\n"
			+ "\t%self1 = load %class.IO*, %class.IO** %self.addr\n"
			+"\tret %class.IO* %self1\n"
			+"}\n");

		out.println("define %class.IO* @_ZN2IO10out_string( %class.IO* %self, i8* %str ) {\n"
			+ "entry:\n"
			+ "\t%0 = call i32 (i8*, ...) @printf( i8* bitcast ( [3 x i8]* @strformatstr to i8* ), i8* %str )\n"
			+ "\tret %class.IO* %self\n"
			+ "}\n");
	
		out.println("define %class.IO* @_ZN2IO7out_int( %class.IO* %self, i32 %int ) {\n"
			+ "entry:\n"
			+ "\t%0 = call i32 (i8*, ...) @printf( i8* bitcast ( [3 x i8]* @intformatstr to i8* ), i32 %int )\n"
			+ "\tret %class.IO* %self\n"
			+ "}\n");
	
		out.println("define i8* @_ZN2IO9in_string( %class.IO* %self ) {\n"
			+ "entry:\n"
			+ "\t%0 = call i8* @malloc( i64 1024 )\n"
			+ "\t%retval = bitcast i8* %0 to i8*\n"
			+ "\t%1 = call i32 (i8*, ...) @scanf( i8* bitcast ( [3 x i8]* @strformatstr to i8* ), i8* %retval )\n"
			+ "\tret i8* %retval\n"
			+ "}\n");
	
		out.println("define i32 @_ZN2IO6in_int( %class.IO* %self ) {\n"
			+ "entry:\n"
			+ "\t%0 = call i8* @malloc( i64 4 )\n"
			+ "\t%1 = bitcast i8* %0 to i32*\n"
			+ "\t%2 = call i32 (i8*, ...) @scanf( i8* bitcast ( [3 x i8]* @intformatstr to i8* ), i32* %1 )\n"
			+ "\t%retval = load i32, i32* %1\n"
			+ "\tret i32 %retval\n"
			+ "}\n");
	}
}