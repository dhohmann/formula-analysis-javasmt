package org.spldev.formula.analysis.javasmt;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.spldev.formula.expression.Formula;
import org.spldev.formula.expression.atomic.predicate.Equals;
import org.spldev.formula.expression.atomic.predicate.LessEqual;
import org.spldev.formula.expression.atomic.predicate.LessThan;
import org.spldev.formula.expression.compound.And;
import org.spldev.formula.expression.io.XmlExtendedFeatureModelFormat;
import org.spldev.formula.expression.term.integer.IntConstant;
import org.spldev.formula.expression.term.integer.IntMultiply;
import org.spldev.formula.expression.term.integer.attribute.IntProduct;
import org.spldev.formula.expression.term.integer.attribute.IntSum;
import org.spldev.formula.expression.term.real.RealConstant;
import org.spldev.formula.expression.term.real.attribute.RealAverage;
import org.spldev.formula.expression.term.real.attribute.RealProduct;
import org.spldev.formula.solver.javasmt.JavaSmtSolver;
import org.spldev.util.data.Result;
import org.spldev.util.io.FileHandler;
import org.spldev.util.tree.Trees;
import org.spldev.util.tree.visitor.TreePrinter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfiguringConstraintsSimpleTest {

	XmlExtendedFeatureModelFormat format;
	Formula model;

	@BeforeEach
	public void setup() {
		format = new XmlExtendedFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/simple.xml"), format);
		Assumptions.assumeTrue(result.isPresent());
		model = result.get();

	}

	protected void assertSolutionCountSum(long threshold, String attribute, long expected) {
		IntConstant c = new IntConstant(threshold);
		// System.out.println(format.getAllSmtVariables());
		LessEqual condition = new LessEqual(new IntSum(attribute, format.getAllSmtVariables()), c);
		JavaSmtSolver solver = new JavaSmtSolver(new And(model, condition), SolverContextFactory.Solvers.Z3);
		long solutions = solver.countSolutions().longValue();
		assertEquals(expected, solutions);
	}

	@Test
	public void testSum() {
		assertSolutionCountSum(9, "Latenz", 0);
		assertSolutionCountSum(10, "Latenz", 7);
		assertSolutionCountSum(29, "Latenz", 7);
		assertSolutionCountSum(30, "Latenz", 21);
	}

	protected void assertSolutionCountProduct(double threshold, String attribute, long expected) {
		RealConstant c = new RealConstant(threshold);
		LessThan condition = new LessThan(new RealProduct(attribute, format.getAllSmtVariables()), c);
		JavaSmtSolver solver = new JavaSmtSolver(new And(model, condition), SolverContextFactory.Solvers.Z3);
		long solutions = solver.countSolutions().longValue();
		assertEquals(expected, solutions);
	}

	@Test
	public void testProduct() {
		assertSolutionCountProduct(0.009, "Ausfall", 0);
		assertSolutionCountProduct(0.01, "Ausfall", 0);
		assertSolutionCountProduct(1, "protocolDouble", 18); // TODO warum 18
		assertSolutionCountProduct(0.5, "protocolDouble", 12); // todo warum 12
	}

	@Test
	public void testAvg() {
		double threshold = 10;
		String attribute = "Latenz";
		RealConstant c = new RealConstant(threshold);
		LessThan condition = new LessThan(new RealAverage(attribute, format.getAllSmtVariables()), c);
		JavaSmtSolver solver = new JavaSmtSolver(new And(model, condition), SolverContextFactory.Solvers.Z3);
		long solutions = solver.countSolutions().longValue();

		// Trees.traverse(condition, new TreePrinter()).ifPresent(System.out::println);

		assertEquals(21, solutions);
	}

	@Test
	public void testMultiplyInterpol() {
		Formula formula = new Equals(new IntConstant(2L), new IntMultiply(new IntConstant(1L), new IntConstant(2L)));
		JavaSmtSolver solver = new JavaSmtSolver(formula, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		// System.out.println(solutions);
		assertTrue(true);
	}

}
