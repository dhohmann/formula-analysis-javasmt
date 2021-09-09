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

import org.spldev.formula.solver.SatSolver.*;
import org.spldev.formula.solver.javasmt.*;
import org.spldev.util.data.*;
import org.spldev.util.job.*;

/**
 * Counts the number of valid solutions to a formula.
 * 
 * @author Sebastian Krieter
 */
public class HasSolutionsAnalysis extends JavaSmtSolverAnalysis<SatResult> {

	public static final Identifier<SatResult> identifier = new Identifier<>();

	@Override
	public Identifier<SatResult> getIdentifier() {
		return identifier;
	}

	@Override
	protected SatResult analyze(JavaSmtSolver solver, InternalMonitor monitor) throws Exception {
		return solver.hasSolution();
	}

}
