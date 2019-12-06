package cool;

public interface VisitorPattern {
	public void visit (AST.program prog);
	public void visit(AST.class_ cl);
	public void visit(AST.attr attr);
	public void visit(AST.expression expr);
	public void visit(AST.method method);
	public void visit(AST.assign asgn);
	public void visit(AST.dispatch disp);
	public void visit(AST.static_dispatch stdis);
	public void visit(AST.cond cond);
	public void visit(AST.loop lp);
	public void visit(AST.isvoid isvoid);
	public void visit(AST.plus pls);
	public void visit(AST.block blck);
	public void visit(AST.sub sub);
	public void visit(AST.let let);
	public void visit(AST.mul mul);
	public void visit(AST.typcase typcs);
	public void visit(AST.new_ nw_);
	public void visit(AST.divide divide);
	public void visit(AST.lt lt);
	public void visit(AST.leq leq);
	public void visit(AST.eq eq);
	public void visit(AST.neg neg);
	public void visit(AST.object obj);
	public void visit(AST.comp comp);
	public void visit(AST.no_expr expr);
	public void visit(AST.int_const int_const);
	public void visit(AST.string_const string_const);
	public void visit(AST.bool_const bool_const);
	public void visit(AST.branch br);

}
