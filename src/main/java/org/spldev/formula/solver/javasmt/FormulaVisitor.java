package org.spldev.formula.solver.javasmt;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FunctionDeclaration;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager;
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor;
import org.spldev.formula.expression.Formula;
import org.spldev.formula.expression.atomic.literal.ErrorLiteral;
import org.spldev.formula.expression.atomic.literal.VariableMap;

import java.util.List;

public abstract class FormulaVisitor implements BooleanFormulaVisitor<Formula> {
	protected final BooleanFormulaManager booleanFormulaManager;
	protected final VariableMap variableMap;

	public FormulaVisitor(BooleanFormulaManager booleanFormulaManager, VariableMap variableMap) {
		this.booleanFormulaManager = booleanFormulaManager;
		this.variableMap = variableMap;
	}

	@Override
	public Formula visitConstant(boolean value) {
		return new ErrorLiteral("unexpected constant");
	}

	@Override
	public Formula visitBoundVar(BooleanFormula var, int deBruijnIdx) {
		return new ErrorLiteral("unexpected bound var");
	}

	@Override
	public Formula visitNot(BooleanFormula operand) {
		return new ErrorLiteral("unexpected not");
	}

	@Override
	public Formula visitAnd(List<BooleanFormula> operands) {
		return new ErrorLiteral("unexpected and");
	}

	@Override
	public Formula visitOr(List<BooleanFormula> operands) {
		return new ErrorLiteral("unexpected or");
	}

	@Override
	public Formula visitXor(BooleanFormula operand1, BooleanFormula operand2) {
		return new ErrorLiteral("unexpected xor");
	}

	@Override
	public Formula visitEquivalence(BooleanFormula operand1, BooleanFormula operand2) {
		return new ErrorLiteral("unexpected equivalence");
	}

	@Override
	public Formula visitImplication(BooleanFormula operand1, BooleanFormula operand2) {
		return new ErrorLiteral("unexpected implication");
	}

	@Override
	public Formula visitIfThenElse(BooleanFormula condition, BooleanFormula thenFormula,
		BooleanFormula elseFormula) {
		return new ErrorLiteral("unexpected if-then-else");
	}

	@Override
	public Formula visitQuantifier(QuantifiedFormulaManager.Quantifier quantifier,
		BooleanFormula quantifiedAST, List<org.sosy_lab.java_smt.api.Formula> boundVars, BooleanFormula body) {
		return new ErrorLiteral("unexpected quantifier");
	}

	@Override
	public Formula visitAtom(BooleanFormula atom,
		FunctionDeclaration<BooleanFormula> funcDecl) {
		return new ErrorLiteral("unexpected atom");
	}
}
