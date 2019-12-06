###############
CS19MTECH01003
GAGANDEEP GOYAL
###############

###################
Problem Definiton - 
###################
1) We need to make an LLVM's Loop Reversal Transformation pass.


##################################################################
Approach for Solving above given Problem Definition -
##################################################################
- Using LoopInfoWrapperPass, Firstly I iterate over all the Loops.

- Fetch all the Basic blocks in the For loop.

- Then I run a Loop on all Instructions in basicblock named parentLoopHead.

- If the found instruction is Compare Instruction then we loop over all Instructions again in the same block.

- For all the PHI Instructions we compare it with the operands of Compare Instruction.

- Whichever PHI instruction matches becomes our Induction Variable.

- Get the lower bound and upper bounds in the loop.

- Check Comparison Istruction is SLT (Less Than), SGT (Greater Than), SLE (Less Than Equal) or SGE (Greater than Equal).

- Set New Upper & Lower bounds.

- Set the Predicate to transformation value corresponding to what Comparison Instruction has.

- Example -> if its SLT -> change to SGE

- Handle the Loop Latch.

- Change the Binary Operator on Induction variable accordingly.

- Example
- If Add operator -> change to Sub
- If Sub operator -> change to Add
- If Mul operator -> change to SDiv
- If SDiv operator -> change to Mul

- Handle Loop Body.

- Create an expression corresponding to what it phi node should change into to keep the semantics.

- Example 
- if CMP_INST is SLT -> final_expr = oldLowerBound + (oldUpperBound - 1) - i
- if CMP_INST is SGT -> final_expr = oldUpperBound + (oldLowerBound + 1) - i
- if CMP_INST is SLE -> final_expr = oldUpperBound - i
- if CMP_INST is SGE -> final_expr = oldLowerBound - i


##################################################################
Note - The Code is incomplete and may not pass the test cases.
##################################################################
1) The code runs on all the basic and trivial cases.
2) It can handle <,>,<=,>= operators properly.
3) It can handle the Add, Sub, Mul, Div properly.
##################################################################
Note - Cases not Handled
##################################################################
4) The case of Nested For loops is also not handled.
5) The case when the induction variable is accessed outside the loops is not handled.

##################################################################
References -
##################################################################
1) https://llvm.org/docs/SourceLevelDebugging.html
2) https://stackoverflow.com/questions/54193082/how-to-get-the-original-source-and-line-number-in-llvm (For understanding on how to find the line number , scope of a given instruction)
