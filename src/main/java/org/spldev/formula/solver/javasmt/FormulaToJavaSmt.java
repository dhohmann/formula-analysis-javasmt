/* -----------------------------------------------------------------------------
 * Formula-Analysis-JavaSMT Lib - Library to analyze propositional formulas with JavaSMT.
 * Copyright (C) 2021  Sebastian Krieter
 * 
 * This file is part of Formula-Analysis-JavaSMT Lib.
 * 
 * Formula-Analysis-JavaSMT Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Formula-Analysis-JavaSMT Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Formula-Analysis-JavaSMT Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/formula-analysis-javasmt> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.formula.solver.javasmt;

import java.util.*;
import java.util.stream.*;

import org.sosy_lab.java_smt.SolverContextFactory.*;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.Formula;
import org.sosy_lab.java_smt.api.NumeralFormula.*;
import org.spldev.formula.expression.*;
import org.spldev.formula.expression.atomic.literal.*;
import org.spldev.formula.expression.atomic.predicate.*;
import org.spldev.formula.expression.compound.*;
import org.spldev.formula.expression.term.*;

/**
 * Class containing functions that are used to translate formulas to java smt.
 *
 * @author Joshua Sprey
 * @author Sebastian Krieter
 */
public class FormulaToJavaSmt {

	private FormulaManager currentFormulaManager;
	private BooleanFormulaManager currentBooleanFormulaManager;
	private IntegerFormulaManager currentIntegerFormulaManager;
	private RationalFormulaManager currentRationalFormulaManager;
	private boolean isPrincess = false;
	private boolean createVariables = true;

	private VariableMap variableMapping;
	private ArrayList<Formula> variables = new ArrayList<>();

	public FormulaToJavaSmt(SolverContext context, VariableMap variableMapping) {
		setContext(context);
		this.variableMapping = variableMapping;
		variables = new ArrayList<>(variableMapping.size() + 1);
		for (int i = 0; i < (variableMapping.size() + 1); i++) {
			variables.add(null);
		}
	}

	public void setContext(SolverContext context) {
		currentFormulaManager = context.getFormulaManager();
		currentBooleanFormulaManager = currentFormulaManager.getBooleanFormulaManager();
		currentIntegerFormulaManager = currentFormulaManager.getIntegerFormulaManager();
		if (context.getSolverName() != Solvers.PRINCESS) { // Princess does not support Rationals
			isPrincess = false;
			currentRationalFormulaManager = currentFormulaManager.getRationalFormulaManager();
		} else {
			isPrincess = true;
		}
	}

	public BooleanFormula nodeToFormula(Expression node) {
		if (node instanceof Not) {
			return createNot(nodeToFormula(node.getChildren().get(0)));
		} else if (node instanceof Or) {
			return createOr(getChildren(node));
		} else if (node instanceof And) {
			return createAnd(getChildren(node));
		} else if (node instanceof Biimplies) {
			return createBiimplies(nodeToFormula(node.getChildren().get(0)), nodeToFormula(node.getChildren().get(1)));
		} else if (node instanceof Implies) {
			return createImplies(nodeToFormula(node.getChildren().get(0)), nodeToFormula(node.getChildren().get(1)));
		} else if (node instanceof Literal) {
			return handleLiteralNode((Literal) node);
		} else if (node instanceof LessThan) {
			return handleLessThanNode((LessThan<?>) node);
		} else if (node instanceof GreaterThan) {
			return handleGreaterThanNode((GreaterThan<?>) node);
		} else if (node instanceof LessEqual) {
			return handleLessEqualNode((LessEqual<?>) node);
		} else if (node instanceof GreaterEqual) {
			return handleGreaterEqualNode((GreaterEqual<?>) node);
		} else if (node instanceof Equals) {
			return handleEqualNode((Equals<?>) node);
		} else {
			throw new RuntimeException("The nodes of type: " + node.getClass() + " are not supported by JavaSmt.");
		}
	}

	private List<BooleanFormula> getChildren(Expression node) {
		return node.getChildren().stream() //
			.map(this::nodeToFormula) //
			.collect(Collectors.toList());
	}

	public BooleanFormula createAnd(List<BooleanFormula> collect) {
		return currentBooleanFormulaManager.and(collect);
	}

	public BooleanFormula createImplies(final BooleanFormula leftChild, final BooleanFormula rightChild) {
		return currentBooleanFormulaManager.implication(leftChild, rightChild);
	}

	public BooleanFormula createBiimplies(final BooleanFormula leftChild, final BooleanFormula rightChild) {
		return currentBooleanFormulaManager.equivalence(leftChild, rightChild);
	}

	public BooleanFormula createOr(List<BooleanFormula> collect) {
		return currentBooleanFormulaManager.or(collect);
	}

	public BooleanFormula createNot(final BooleanFormula childFormula) {
		return currentBooleanFormulaManager.not(childFormula);
	}

	private BooleanFormula handleEqualNode(Equals<?> node) {
		final NumeralFormula leftTerm = termToFormula(node.getChildren().get(0));
		final NumeralFormula rightTerm = termToFormula(node.getChildren().get(1));
		return createEqual(leftTerm, rightTerm);
	}

	public BooleanFormula createEqual(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
		if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
			return currentRationalFormulaManager.equal(leftTerm, rightTerm);
		} else {
			return currentIntegerFormulaManager.equal((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
		}
	}

	private BooleanFormula handleGreaterEqualNode(GreaterEqual<?> node) {
		final NumeralFormula leftTerm = termToFormula(node.getChildren().get(0));
		final NumeralFormula rightTerm = termToFormula(node.getChildren().get(1));
		return createGreaterEqual(leftTerm, rightTerm);
	}

	public BooleanFormula createGreaterEqual(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
		if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
			return currentRationalFormulaManager.greaterOrEquals(leftTerm, rightTerm);
		} else {
			return currentIntegerFormulaManager.greaterOrEquals((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
		}
	}

	private BooleanFormula handleLessEqualNode(LessEqual<?> node) {
		final NumeralFormula leftTerm = termToFormula(node.getChildren().get(0));
		final NumeralFormula rightTerm = termToFormula(node.getChildren().get(1));
		return createLessEqual(leftTerm, rightTerm);
	}

	public BooleanFormula createLessEqual(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
		if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
			return currentRationalFormulaManager.lessOrEquals(leftTerm, rightTerm);
		} else {
			return currentIntegerFormulaManager.lessOrEquals((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
		}
	}

	private BooleanFormula handleGreaterThanNode(GreaterThan<?> node) {
		final NumeralFormula leftTerm = termToFormula(node.getChildren().get(0));
		final NumeralFormula rightTerm = termToFormula(node.getChildren().get(1));
		return createGreaterThan(leftTerm, rightTerm);
	}

	public BooleanFormula createGreaterThan(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
		if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
			return currentRationalFormulaManager.greaterThan(leftTerm, rightTerm);
		} else {
			return currentIntegerFormulaManager.greaterThan((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
		}
	}

	private BooleanFormula handleLessThanNode(LessThan<?> node) {
		final NumeralFormula leftTerm = termToFormula(node.getChildren().get(0));
		final NumeralFormula rightTerm = termToFormula(node.getChildren().get(1));
		return createLessThan(leftTerm, rightTerm);
	}

	public BooleanFormula createLessThan(final NumeralFormula leftTerm, final NumeralFormula rightTerm) {
		if (((leftTerm instanceof RationalFormula) || (rightTerm instanceof RationalFormula)) && !isPrincess) {
			return currentRationalFormulaManager.lessThan(leftTerm, rightTerm);
		} else {
			return currentIntegerFormulaManager.lessThan((IntegerFormula) leftTerm, (IntegerFormula) rightTerm);
		}
	}

	private NumeralFormula termToFormula(Term<?> term) {
		if (term instanceof Constant<?>) {
			return createConstant(((Constant<?>) term).getValue());
		} else if (term instanceof Variable<?>) {
			final Variable<?> variable = (Variable<?>) term;
			return handleVariable(variable);
		} else if (term instanceof Function) {
			return handleFunction((Function<?, ?>) term);
		} else {
			throw new RuntimeException("The given term is not supported by JavaSMT: " + term.getClass());
		}

	}

	private NumeralFormula handleFunction(Function<?, ?> function) {
		final NumeralFormula[] childs = new NumeralFormula[function.getChildren().size()];
		int index = 0;
		for (final Term<?> term : function.getChildren()) {
			childs[index++] = termToFormula(term);
		}
		if (function.getType() == Double.class) {
			if (isPrincess) {
				throw new UnsupportedOperationException("Princess does not support variables from type: Double");
			}
			if (function instanceof Add) {
				return currentRationalFormulaManager.add(childs[0], childs[1]);
			} else if (function instanceof Multiply) {
				return currentRationalFormulaManager.multiply(childs[0], childs[1]);
			} else {
				throw new RuntimeException(
					"The given function is not supported by JavaSMT Rational Numbers: " + function.getClass());
			}
		} else if (function.getType() == Long.class) {
			if (function instanceof Add) {
				return currentIntegerFormulaManager.add((IntegerFormula) childs[0], (IntegerFormula) childs[1]);
			} else if (function instanceof Multiply) {
				return currentIntegerFormulaManager.multiply((IntegerFormula) childs[0], (IntegerFormula) childs[1]);
			} else {
				throw new RuntimeException(
					"The given function is not supported by JavaSMT Rational Numbers: " + function.getClass());
			}
		} else {
			throw new UnsupportedOperationException("Unkown function type: " + function.getType());
		}
	}

	public NumeralFormula createConstant(Object value) {
		if (value instanceof Long) {
			return currentIntegerFormulaManager.makeNumber((long) value);
		} else if (value instanceof Double) {
			if (isPrincess) {
				throw new UnsupportedOperationException("Princess does not support constants from type: Double");
			}
			return currentRationalFormulaManager.makeNumber((double) value);
		} else {
			throw new UnsupportedOperationException("Unkown constant type: " + value.getClass());
		}
	}

	private NumeralFormula handleVariable(Variable<?> variable) {
		final String name = variable.getName();
		final Optional<Formula> map = variableMapping.getIndex(name).map(variables::get);
		if (variable.getType() == Double.class) {
			if (isPrincess) {
				throw new UnsupportedOperationException("Princess does not support variables from type: Double");
			}
			return (NumeralFormula) map.orElseGet(() -> newVariable(name, currentRationalFormulaManager::makeVariable));
		} else if (variable.getType() == Long.class) {
			return (NumeralFormula) map.orElseGet(() -> newVariable(name, currentIntegerFormulaManager::makeVariable));
		} else {
			throw new UnsupportedOperationException("Unkown variable type: " + variable.getType());
		}
	}

	private BooleanFormula handleLiteralNode(Literal literal) {
		if (literal == Literal.True) {
			return currentBooleanFormulaManager.makeTrue();
		} else if (literal == Literal.False) {
			return currentBooleanFormulaManager.makeFalse();
		} else {
			final String name = literal.getName();
			final BooleanFormula variable = (BooleanFormula) variableMapping.getIndex(name).map(variables::get)
				.orElseGet(() -> newVariable(name, currentBooleanFormulaManager::makeVariable));
			return literal.isPositive() ? variable : createNot(variable);
		}
	}

	private <T extends Formula> T newVariable(final String name,
		java.util.function.Function<String, T> variableCreator) {
		if (createVariables) {
			final Integer index = variableMapping.getIndex(name).orElseThrow(RuntimeException::new);
			final T newVariable = variableCreator.apply(name);
			while (variables.size() <= index) {
				variables.add(null);
			}
			variables.set(index, newVariable);
			return newVariable;
		} else {
			throw new RuntimeException(name);
		}
	}

	public VariableMap getVariableMapping() {
		return variableMapping;
	}

	public void setVariableMapping(VariableMap variableMapping) {
		this.variableMapping = variableMapping;
	}

	public ArrayList<Formula> getVariables() {
		return variables;
	}

}
