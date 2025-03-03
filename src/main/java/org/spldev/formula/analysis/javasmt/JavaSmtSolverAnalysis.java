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
package org.spldev.formula.analysis.javasmt;

import org.sosy_lab.java_smt.SolverContextFactory.*;
import org.spldev.formula.analysis.*;
import org.spldev.formula.expression.*;
import org.spldev.formula.solver.javasmt.*;

/**
 * Base class for analyses using a {@link JavaSmtSolver}.
 *
 * @param <T> Type of the analysis result.
 *
 * @author Joshua Sprey
 * @author Sebastian Krieter
 */
public abstract class JavaSmtSolverAnalysis<T> extends AbstractAnalysis<T, JavaSmtSolver, Formula> {

	public JavaSmtSolverAnalysis() {
		super();
		solverInputProvider = FormulaProvider.empty();
	}

	@Override
	protected JavaSmtSolver createSolver(Formula input) {
		return new JavaSmtSolver(input, Solvers.SMTINTERPOL);
	}

}
