package org.spldev.formula.analysis.javasmt;

import org.junit.jupiter.api.Test;
import org.spldev.formula.ModelRepresentation;
import org.spldev.formula.expression.Formula;
import org.spldev.formula.expression.Formulas;
import org.spldev.formula.expression.atomic.literal.Literal;
import org.spldev.formula.expression.atomic.literal.LiteralPredicate;
import org.spldev.formula.expression.atomic.literal.VariableMap;
import org.spldev.formula.expression.atomic.predicate.Equals;
import org.spldev.formula.expression.atomic.predicate.GreaterThan;
import org.spldev.formula.expression.atomic.predicate.LessThan;
import org.spldev.formula.expression.compound.And;
import org.spldev.formula.expression.compound.Implies;
import org.spldev.formula.expression.compound.Not;
import org.spldev.formula.expression.compound.Or;
import org.spldev.formula.expression.term.Term;
import org.spldev.formula.expression.term.bool.BoolVariable;
import org.spldev.formula.expression.term.integer.IntAdd;
import org.spldev.formula.expression.term.integer.IntConstant;
import org.spldev.formula.expression.term.integer.IntVariable;
import org.spldev.util.data.Result;
import org.spldev.util.logging.Logger;
import org.spldev.util.tree.Trees;
import org.spldev.util.tree.visitor.TreePrinter;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamicConstraintTest {

	@Test
	public void testDynamicAttributeSupport() {
		final VariableMap variables = VariableMap.fromNames(Arrays.asList("a", "b", "c"));
		variables.addIntegerVariable("a.cost");
		variables.addIntegerVariable("b.cost");
		variables.addIntegerVariable("c.cost");

		final Literal a = new LiteralPredicate((BoolVariable) variables.getVariable("a").get(), true);
		final Literal b = new LiteralPredicate((BoolVariable) variables.getVariable("b").get(), true);
		final Literal c = new LiteralPredicate((BoolVariable) variables.getVariable("c").get(), true);

		// Normal formula
		final Or or = new Or(a, b);
		final Or formula = new Or(or, c);

		// Attribute values
		final List<And> attributes = new ArrayList<>();
		buildAttributeAssignment(attributes, variables, "a", "cost", 12);
		buildAttributeAssignment(attributes, variables, "b", "cost", 1);
		buildAttributeAssignment(attributes, variables, "c", "cost", 10);
		final And assignments = new And(attributes);

		// Attribute aggregation and constraint
		Term<Long> sum = buildIntSum(variables, "cost");
		final LessThan<Long> lessThan = new LessThan<>(sum, new IntConstant(13L));

		// Formula extension
		final And ext = new And(assignments, lessThan);

		Result<Formula> cnf = Formulas.toCNF(new And(formula, ext));
		if (cnf.isPresent()) {
			System.out.println("converted to cnf");
			Trees.traverse(cnf.get(), new TreePrinter()).ifPresent(System.out::println);
		}
		// Build model
		final ModelRepresentation rep = new ModelRepresentation(new And(formula, ext));
		Trees.traverse(rep.getFormula(), new TreePrinter()).ifPresent(System.out::println);

		final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
		final Result<?> result = analysis.getResult(rep);
		result.orElse(Logger::logProblems);
		assertTrue(result.isPresent());
		assertEquals(BigInteger.valueOf(3), result.get());
	}

	private void buildAttributeAssignment(List<And> assignments, VariableMap variables, String feature, String attrName,
		long value) {
		if (!variables.getVariable(feature + "." + attrName).isPresent()) {
			return;
		}
		BoolVariable var = (BoolVariable) variables.getVariable(feature).get();
		LiteralPredicate selected = new LiteralPredicate(var, true);
		LiteralPredicate deselected = new LiteralPredicate(var, false);
		IntVariable attr = (IntVariable) variables.getVariable(feature + "." + attrName).get();
		final Implies implies = new Implies(selected, new Equals<>(attr, new IntConstant(12L)));
		final Implies notImplies = new Implies(deselected, new Equals<>(attr, new IntConstant(0L)));
		assignments.add(new And(implies, notImplies));
	}

	private Term<Long> buildIntSum(VariableMap variables, String attr) {
		List<Term<Long>> sumElements = new ArrayList<>();
		variables.getNames().stream().filter((name) -> {
			return name.contains("." + attr);
		}).forEach((name) -> {
			sumElements.add((IntVariable) variables.getVariable(name).get());
		});

		Term<Long> a = null;
		Iterator<Term<Long>> iter = sumElements.iterator();
		while (iter.hasNext()) {
			Term<Long> l = iter.next();
			Term<Long> r = null;
			if (a != null) {
				r = a;
			} else if (iter.hasNext()) {
				r = iter.next();
			}
			if (r == null) {
				a = l;
			} else {
				a = new IntAdd(l, r);
			}
		}
		return a;
	}

	// Do not continue here
	@Test
	public void testMinAggregation() {
		final VariableMap variables = VariableMap.fromNames(Arrays.asList("a", "b", "c"));
		variables.addIntegerVariable("a.cost");
		variables.addIntegerVariable("b.cost");
		variables.addIntegerVariable("c.cost");

		final Literal a = new LiteralPredicate((BoolVariable) variables.getVariable("a").get(), true);
		final Literal b = new LiteralPredicate((BoolVariable) variables.getVariable("b").get(), true);
		final Literal c = new LiteralPredicate((BoolVariable) variables.getVariable("c").get(), true);

		// Normal formula
		final Or or = new Or(a, b);
		final Or formula = new Or(or, c);

		// Attribute values
		final List<And> attributes = new ArrayList<>();
		buildAttributeAssignment(attributes, variables, "a", "cost", 12);
		buildAttributeAssignment(attributes, variables, "b", "cost", 1);
		buildAttributeAssignment(attributes, variables, "c", "cost", 10);
		final And assignments = new And(attributes);

		// Attribute aggregation and constraint
		List<Formula> formulaList = new ArrayList<>();
		Term<Long> min = buildIntMin(variables, "cost", formulaList);
		final GreaterThan<Long> lessThan = new GreaterThan<>(min, new IntConstant(2L));

		// Formula extension
		formulaList.add(lessThan);
		final And ext_min = new And(formulaList);
		final And ext = new And(assignments, ext_min);
		System.out.println("Provided formula");

		// Build model
		final ModelRepresentation rep = new ModelRepresentation(new And(formula, ext));
		Trees.traverse(rep.getFormula(), new TreePrinter()).ifPresent(System.out::println);

		final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
		final Result<?> result = analysis.getResult(rep);
		result.orElse(Logger::logProblems);
		assertTrue(result.isPresent());
		assertEquals(BigInteger.valueOf(3), result.get());
	}

	public static Term<Long> buildIntMin(VariableMap variables, String attr, List<Formula> formulas) {
		Queue<IntVariable> queue = new LinkedBlockingQueue<>();
		variables.getNames().stream().filter((name) -> {
			return name.contains("." + attr);
		}).forEach((name) -> {
			queue.add((IntVariable) variables.getVariable(name).get());
		});
		IntVariable min = buildMin(variables, formulas, queue);
		return min;
	}

	public static IntVariable buildMin(VariableMap variableMap, List<Formula> terms, IntVariable a1, IntVariable a2) {
		LessThan<Long> lessThan = new LessThan<>(a1, a2);
		String name = "min(" + a1.getName() + "," + a2.getName() + ")";
		IntVariable min = variableMap.addIntegerVariable(name).get();
		Implies a1_greater = new Implies(lessThan, new Equals<>(min, a1));
		Implies a2_greater = new Implies(new Not(lessThan), new Equals<>(min, a2));
		terms.add(a1_greater);
		terms.add(a2_greater);
		return min;
	}

	public static IntVariable buildMin(VariableMap variableMap, List<Formula> terms, Queue<IntVariable> values) {
		if (values.isEmpty()) {
			IntVariable min = variableMap.addIntegerVariable("min").get();
			terms.add(new Equals<>(min, new IntConstant(Long.MAX_VALUE)));
			return min;
		}
		if (values.size() == 1) {
			return values.poll();
		}
		IntVariable a1 = values.poll();
		IntVariable a2 = values.poll();
		values.add(buildMin(variableMap, terms, a1, a2));
		return buildMin(variableMap, terms, values);
	}

	public static int min(int a, int b) {
		return Math.min(a, b);
	}

	public static int min(Queue<Integer> values) {
		if (values.isEmpty()) {
			return Integer.MAX_VALUE;
		}
		if (values.size() == 1) {
			return values.poll();
		}
		int a1 = values.poll();
		int a2 = !values.isEmpty() ? values.poll() : Integer.MAX_VALUE;
		values.add(min(a1, a2));
		return min(values);
	}
}
