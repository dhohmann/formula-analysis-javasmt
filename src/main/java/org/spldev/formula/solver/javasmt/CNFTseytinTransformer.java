package org.spldev.formula.solver.javasmt;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.*;
import org.spldev.formula.expression.Formula;
import org.spldev.formula.expression.atomic.literal.LiteralPredicate;
import org.spldev.formula.expression.atomic.literal.VariableMap;
import org.spldev.formula.expression.compound.And;
import org.spldev.formula.expression.compound.Or;
import org.spldev.formula.expression.term.Variable;
import org.spldev.formula.expression.term.bool.BoolVariable;
import org.spldev.formula.expression.transform.Transformer;
import org.spldev.util.job.InternalMonitor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforms a formula into CNF using the Tseytin transformation implemented in
 * Z3. Requires Z3 to be installed and libz3/libz3java to be in Java's dynamic
 * linking path.
 */
public class CNFTseytinTransformer implements Transformer {
	private static Configuration config;
	private static LogManager logManager;
	private static SolverContext context;
	private static ShutdownManager shutdownManager;
	private static FormulaManager formulaManager;
	private static BooleanFormulaManager booleanFormulaManager;

	static {
		try {
			config = Configuration.defaultConfiguration();
			logManager = BasicLogManager.create(config);
			shutdownManager = ShutdownManager.create();
			context = SolverContextFactory.createSolverContext(config, logManager, shutdownManager
				.getNotifier(), SolverContextFactory.Solvers.Z3);
			formulaManager = context.getFormulaManager();
			booleanFormulaManager = formulaManager.getBooleanFormulaManager();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Formula execute(Formula formula, InternalMonitor monitor) throws Exception {
		VariableMap variableMap = VariableMap.fromExpression(formula);
		BooleanFormula booleanFormula = formulaManager.applyTactic(new FormulaToJavaSmt(context,
			variableMap).nodeToFormula(formula), Tactic.TSEITIN_CNF);
		return booleanFormulaManager.visit(booleanFormula, new CNFVisitor(booleanFormulaManager, variableMap));
	}

	public static class CNFVisitor extends FormulaVisitor {
		public CNFVisitor(BooleanFormulaManager booleanFormulaManager, VariableMap variableMap) {
			super(booleanFormulaManager, variableMap);
		}

		@Override
		public Formula visitAnd(List<BooleanFormula> operands) {
			return new And(operands.stream().map(operand -> booleanFormulaManager.visit(operand, new ClauseVisitor(
				booleanFormulaManager, variableMap))).collect(Collectors.toList()));
		}

		@Override
		public Formula visitOr(List<BooleanFormula> operands) {
			return new And(new ClauseVisitor(booleanFormulaManager, variableMap).visitOr(operands));
		}

		@Override
		public Formula visitNot(BooleanFormula operand) {
			return new And(new Or(new LiteralVisitor(booleanFormulaManager, variableMap).visitNot(operand)));
		}

		@Override
		public Formula visitAtom(BooleanFormula atom, FunctionDeclaration<BooleanFormula> funcDecl) {
			return new And(new Or(new LiteralVisitor(booleanFormulaManager, variableMap).visitAtom(atom, funcDecl)));
		}
	}

	public static class ClauseVisitor extends FormulaVisitor {
		public ClauseVisitor(BooleanFormulaManager booleanFormulaManager, VariableMap variableMap) {
			super(booleanFormulaManager, variableMap);
		}

		@Override
		public Formula visitOr(List<BooleanFormula> operands) {
			return new Or(operands.stream().map(operand -> booleanFormulaManager.visit(operand, new LiteralVisitor(
				booleanFormulaManager, variableMap))).collect(Collectors.toList()));
		}

		@Override
		public Formula visitNot(BooleanFormula operand) {
			return new Or(new LiteralVisitor(booleanFormulaManager, variableMap).visitNot(operand));
		}

		@Override
		public Formula visitAtom(BooleanFormula atom, FunctionDeclaration<BooleanFormula> funcDecl) {
			return new Or(new LiteralVisitor(booleanFormulaManager, variableMap).visitAtom(atom, funcDecl));
		}
	}

	public static class LiteralVisitor extends FormulaVisitor {
		public LiteralVisitor(BooleanFormulaManager booleanFormulaManager, VariableMap variableMap) {
			super(booleanFormulaManager, variableMap);
		}

		@Override
		public Formula visitNot(BooleanFormula operand) {
			LiteralPredicate literalPredicate = (LiteralPredicate) booleanFormulaManager.visit(operand, this);
			return literalPredicate.flip();
		}

		@Override
		public Formula visitAtom(BooleanFormula atom, FunctionDeclaration<BooleanFormula> funcDecl) {
			Variable<?> variable = variableMap.getVariable(atom.toString()).orElseGet(() -> variableMap
				.addBooleanVariable(atom.toString()).get());
			return new LiteralPredicate((BoolVariable) variable, true);
		}
	}
}
