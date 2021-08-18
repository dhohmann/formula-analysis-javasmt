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

import java.math.*;
import java.util.*;
import java.util.stream.*;

import org.sosy_lab.common.*;
import org.sosy_lab.common.configuration.*;
import org.sosy_lab.common.log.*;
import org.sosy_lab.common.rationals.*;
import org.sosy_lab.java_smt.*;
import org.sosy_lab.java_smt.SolverContextFactory.*;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.BasicProverEnvironment.*;
import org.sosy_lab.java_smt.api.Model.*;
import org.sosy_lab.java_smt.api.OptimizationProverEnvironment.*;
import org.sosy_lab.java_smt.api.SolverContext.*;
import org.spldev.formula.*;
import org.spldev.formula.solver.*;
import org.spldev.util.logging.*;

/**
 * SMT solver using JavaSMT.
 *
 * @author Joshua Sprey
 */
public class JavaSmtSolver implements Solver, SatSolver<Object[]>, OptSolver<Object, NumeralFormula>,
	MusSolver<BooleanFormula> {

	private AssumptionStack<BooleanFormula> assumptions;
	private JavaSmtFormula formula;

	/**
	 * The current contex of the solver. Used by the translator to translate prop4J
	 * nodes to JavaSMT formulas.
	 */
	public SolverContext context;

	public JavaSmtSolver(ModelRepresentation model, Solvers solver) {
		try {
			final Configuration config = Configuration.defaultConfiguration();
			final LogManager logManager = BasicLogManager.create(config);
			final ShutdownManager shutdownManager = ShutdownManager.create();
			context = SolverContextFactory.createSolverContext(config, logManager, shutdownManager.getNotifier(),
				solver);
			formula = new JavaSmtFormula(context, model.getFormula());
			assumptions = new AssumptionStack<>(formula.getVariableMap().size());
		} catch (final InvalidConfigurationException e) {
			Logger.logError(e);
		}
	}

	public SatResult isSatisfiable() {
		try (ProverEnvironment prover = context.newProverEnvironment()) {
			for (final BooleanFormula constraint : formula.getConstraints()) {
				prover.addConstraint(constraint);
			}
			for (final BooleanFormula constraint : assumptions.getAssumptions()) {
				prover.addConstraint(constraint);
			}
			return prover.isUnsat() ? SatResult.FALSE : SatResult.TRUE;
		} catch (final SolverException e) {
			return SatResult.TIMEOUT;
		} catch (final InterruptedException e) {
			return SatResult.TIMEOUT;
		}
	}

	public BigInteger countSolutions() {
		try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_ALL_SAT)) {
			for (final BooleanFormula constraint : formula.getConstraints()) {
				prover.addConstraint(constraint);
			}
			for (final BooleanFormula constraint : assumptions.getAssumptions()) {
				prover.addConstraint(constraint);
			}
			return prover.allSat(new AllSatCallback<>() {
				BigInteger count = BigInteger.ZERO;

				@Override
				public void apply(List<BooleanFormula> model) {
					count = count.add(BigInteger.ONE);
				}

				@Override
				public BigInteger getResult() throws InterruptedException {
					return count;
				}
			}, formula.getBooleanVariables());
		} catch (final Exception e) {
			Logger.logError(e);
			return BigInteger.valueOf(-1);
		}
	}

	@Override
	public Object[] getSolution() {
		try (OptimizationProverEnvironment prover = context.newOptimizationProverEnvironment()) {
			for (final BooleanFormula constraint : formula.getConstraints()) {
				prover.addConstraint(constraint);
			}

			if (!prover.isUnsatWithAssumptions(assumptions.getAssumptions())) {
				final Model model = prover.getModel();
				final Iterator<ValueAssignment> iterator = model.iterator();
				final Object[] solution = new Object[formula.getVariableMap().size() + 1];
				while (iterator.hasNext()) {
					final ValueAssignment assignment = iterator.next();
					final int index = formula.getVariableMap().getIndex(assignment.getName()).orElseThrow();
					solution[index] = assignment.getValue();
				}
				return solution;
			} else {
				return null;
			}
		} catch (final SolverException e) {
			return null;
		} catch (final InterruptedException e) {
			return null;
		}
	}

	@Override
	public Object[] findSolution() {
		return getSolution();
	}

	@Override
	public Object minimum(NumeralFormula formula) {
		try (OptimizationProverEnvironment prover = context.newOptimizationProverEnvironment()) {
			for (final BooleanFormula constraint : this.formula.getConstraints()) {
				prover.addConstraint(constraint);
			}
			for (final BooleanFormula assumption : assumptions.getAssumptions()) {
				prover.addConstraint(assumption);
			}
			final int handleY = prover.minimize(formula);
			final OptStatus status = prover.check();
			assert status == OptStatus.OPT;
			final Optional<Rational> lower = prover.lower(handleY,
				Rational.ofString("1/1000"));
			return lower.get();
		} catch (final Exception e) {
			Logger.logError(e);
			return null;
		}
	}

	@Override
	public Object maximum(NumeralFormula formula) {
		try (OptimizationProverEnvironment prover = context.newOptimizationProverEnvironment()) {
			for (final BooleanFormula constraint : this.formula.getConstraints()) {
				prover.addConstraint(constraint);
			}
			for (final BooleanFormula assumption : assumptions.getAssumptions()) {
				prover.addConstraint(assumption);
			}
			final int handleX = prover.maximize(formula);
			final OptStatus status = prover.check();
			assert status == OptStatus.OPT;
			final Optional<Rational> upper = prover.upper(handleX, Rational.ofString("1/1000"));
			return upper.get();
		} catch (final Exception e) {
			Logger.logError(e);
			return null;
		}
	}

	@Override
	public SatResult hasSolution() {
		try (ProverEnvironment prover = context.newProverEnvironment()) {
			for (final BooleanFormula constraint : formula.getConstraints()) {
				prover.addConstraint(constraint);
			}
			return prover.isUnsatWithAssumptions(assumptions.getAssumptions())
				? SatResult.FALSE
				: SatResult.TRUE;
		} catch (final SolverException e) {
			return SatResult.TIMEOUT;
		} catch (final InterruptedException e) {
			return SatResult.TIMEOUT;
		}
	}

	@Override
	public List<BooleanFormula> getMinimalUnsatisfiableSubset() throws IllegalStateException {
		try (ProverEnvironment prover = context.newProverEnvironment()) {
			for (final BooleanFormula constraint : formula.getConstraints()) {
				prover.addConstraint(constraint);
			}
			if (prover.isUnsatWithAssumptions(assumptions.getAssumptions())) {
				final List<BooleanFormula> formula = prover.getUnsatCore();
				return formula.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			}
			return Collections.emptyList();
		} catch (final Exception e) {
			Logger.logError(e);
			return null;
		}
	}

	@Override
	public List<List<BooleanFormula>> getAllMinimalUnsatisfiableSubsets() throws IllegalStateException {
		return Collections.singletonList(getMinimalUnsatisfiableSubset());
	}

}
