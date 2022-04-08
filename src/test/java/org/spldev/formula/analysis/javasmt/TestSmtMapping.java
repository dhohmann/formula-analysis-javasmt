package org.spldev.formula.analysis.javasmt;

import org.junit.jupiter.api.Test;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.spldev.formula.expression.Formula;
import org.spldev.formula.expression.Formulas;
import org.spldev.formula.expression.compound.And;
import org.spldev.formula.expression.io.XmlExtendedFeatureModelFormat;
import org.spldev.formula.expression.io.XmlFeatureModelFormat;
import org.spldev.formula.expression.transform.CNFDistributiveLawTransformer;
import org.spldev.formula.expression.transform.DistributiveLawTransformer;
import org.spldev.formula.expression.transform.NormalForms;
import org.spldev.formula.solver.javasmt.JavaSmtSolver;
import org.spldev.util.data.Result;
import org.spldev.util.io.FileHandler;
import org.spldev.util.job.NullMonitor;
import org.spldev.util.tree.Trees;
import org.spldev.util.tree.visitor.TreePrinter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSmtMapping {

	@Test
	public void testSmtMappingSimple() {
		XmlExtendedFeatureModelFormat format = new XmlExtendedFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/simple.xml"), format);
		Formula formula = result.get();
		JavaSmtSolver solver = new JavaSmtSolver(formula, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		// Trees.traverse(formula, new TreePrinter()).ifPresent(System.out::println);
		assertEquals(21, solutions);

	}

	@Test
	public void testSmtMappingExample() {
		XmlExtendedFeatureModelFormat format = new XmlExtendedFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/extended.xml"), format);
		Formula formula = result.get();
		JavaSmtSolver solver = new JavaSmtSolver(formula, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		assertEquals(1104, solutions);
	}

	@Test
	public void testSmtMappingExampleNormalFeatureModel() {
		XmlExtendedFeatureModelFormat format = new XmlExtendedFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/normal.xml"), format);
		Formula formula = result.get();
		JavaSmtSolver solver = new JavaSmtSolver(formula, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		assertEquals(1104, solutions);
	}

	@Test
	public void testSmtMappingAlternative() {
		XmlExtendedFeatureModelFormat format = new XmlExtendedFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/alternative.xml"), format);
		Formula formula = result.get();
		JavaSmtSolver solver = new JavaSmtSolver(formula, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		assertEquals(2, solutions);
	}

	@Test
	public void testSmtMappingAlternativeMore() {
		XmlExtendedFeatureModelFormat format = new XmlExtendedFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/alternativeMore.xml"),
			format);
		Formula formula = result.get();
		JavaSmtSolver solver = new JavaSmtSolver(formula, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		assertEquals(4, solutions);
	}

	@Test
	public void testSmtMappingAlternativeParent() {
		XmlExtendedFeatureModelFormat format = new XmlExtendedFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/alternativeParent.xml"),
			format);
		Formula formula = result.get();
		JavaSmtSolver solver = new JavaSmtSolver(formula, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		assertEquals(3, solutions);
	}

	@Test
	public void testAlternative() {
		XmlFeatureModelFormat format = new XmlFeatureModelFormat();
		Result<Formula> result = FileHandler.load(TestSmtMapping.class.getResourceAsStream("/xor.xml"), format);
		Formula f = result.get();
		// Trees.traverse(f, new TreePrinter()).ifPresent(System.out::println);

		f = Formulas.toCNF(f).get();

		// Trees.traverse(f, new TreePrinter()).ifPresent(System.out::println);

		JavaSmtSolver solver = new JavaSmtSolver(f, SolverContextFactory.Solvers.SMTINTERPOL);
		long solutions = solver.countSolutions().longValue();
		assertEquals(4, solutions);
	}

}
