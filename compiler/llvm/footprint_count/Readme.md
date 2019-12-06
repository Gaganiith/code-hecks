###############
CS19MTECH01003
GAGANDEEP GOYAL
###############

###################
Problem Definiton - 
###################
1) We need to make an LLVM's analysis pass which should extract Various Information like - 
a) Printing Clang Version.
b) Printing Clang/LLVM source Repository.
c) Printing Commit hash for above Repository
d) Printing the Target machine for which we are generating the code.
e) Printing Variable Name
f) Printing Variable Scope
g) Printing Footprint
h) Printing Number of Read/Writes.


##################################################################
Approach for Solving above given Problem Definition -
##################################################################
1)The information for Part a), b) , c) was pretty straight forward.
We got the value from 'llvm.dbg.cu' debug information. In the metadata, 'producer' was holding the string which contains all these 3 information. Using Regex on string matching I extracted out all these 3 values.

d) I got the target information from the "getDefaultTargetTriple()" function.

##################################################################
e) For every call instruction I get its operand values. The second Operand contains the information 
regarding the variable used here. So I get its value using "getName()" function and if the name is 
equal to the name provided by the user in the terminal then we proceed with the next steps finding other values.
We also print this variable name here itself.

##################################################################
f) For finding the variable scope, there were 2 parts in solving it
i) Starting point of the variable scope, which was given by the instruction itself. i.e from its debugLoc where i was easily able to get the start Line information.
ii) Ending point of the variable scope was a little trickier part.
Approach here in solving this was get the debug information i.e Line() and scope() information for every Instruction.
Use a Datastructure map and add scope with line number of it.
The trick here is For every new entry  we check if scope value already exists and if so and the line number is greater than the existing scope in the map, then we update the value with the new Line information.

In the end we are left with the map containing every scope corresponding to which it has maximum line number the instruction which used this Scope.

But there is still some issue in this approach.
This approach fails for the for loop.

for(Lexical Block parent){
    Lexical Block child;
}

As the values declared in the Lexical Block parent have the scope in this block only, where as its value can be used inside the child block as well.
So our mapping technique fails here.

Work around -:
#############
Observation - The parents Lexical Block's scope will always have the maximum line number greater than the Lexical Block's scope of children. But this case is not true for the case in For loop.

Implementation - So we exploited this by checking this condition for every instruction's scope and hence update the max end line of the scope where we find the above condition as true.

##################################################################
g) The concept of use-def helped in getting the footprint. We even handled the pointer reference to the variable
using the approach of Alias Analysis. In the set provided by alias analysis, if there is a pointer refering to our 
variable then we find the use-def for that pointer as well and print its values terming as Footprint.

We get same value multiple times using alias set, so we store all the values in the vector.
Then I transformed this vector into set getting all the unique values in the increasing order.

I have also added the use-def functionality for Global variable as well, But the alias analysis is not working there.

##################################################################
h) The Read/Write functionality is not done. We tried to get this functionality in Non-SSA 
form using use-def and seeing whether the particular instruction is load/store instruction.
If the Instruction is Load Instruction then I considered it as a Read Instruction and
If the Instruction is Store Instruction then I considered it as a Write Instruction.

But this analysis fails with the incoming of a for loop.

Another approach we came up with is trip count using scev analysis. But the scev analysis can be applied only on
SSA form, where we were able to get the Trip count but all the information related to Load/Store got lost.

So our Basic approach failed and hence were unable to complete Finding the Read/Write problem.



##################################################################
Note - The Code is incomplete and may not pass the test cases.
##################################################################
1) Our logic of Finding the scope of a variable fails when the code is SSA form, and works only when the code is Non-SSA form.
2) The Code for finding number of Read/Write is not written.
3) Alias analysis failed on Global variables as Alias analysis was giving all its pointer reference in all different sets instead of one same set.

##################################################################
References -
##################################################################
1) https://llvm.org/docs/SourceLevelDebugging.html
2) https://stackoverflow.com/questions/54193082/how-to-get-the-original-source-and-line-number-in-llvm (For understanding on how to find the line number , scope of a given instruction)
3) https://www.youtube.com/watch?v=XTqokII5pVw (For understanding the concept of use-def Implementation)
4) https://stackoverflow.com/questions/51400856/incorrect-llvm-alias-analysis (For understanding the concept of Alias Analysis Implemention)

#####################################################################
Challenging Part - 
#####################################################################
The Non-familiarity with the coding style in the LLVM was challenging.
Finding the number of Read/Write was also a challenging problem.