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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sosy_lab.common.*;
import org.sosy_lab.common.configuration.*;
import org.sosy_lab.common.log.*;
import org.sosy_lab.java_smt.*;
import org.sosy_lab.java_smt.SolverContextFactory.*;
import org.sosy_lab.java_smt.api.*;
import org.spldev.util.logging.*;

public class TestSolvers {

	private void solversWindows() {
		testAvailability(Solvers.MATHSAT5);
		testAvailability(Solvers.PRINCESS);
		testAvailability(Solvers.SMTINTERPOL);
		testAvailability(Solvers.Z3);
	}

	private void solversUnix() {
		testAvailability(Solvers.BOOLECTOR);
		testAvailability(Solvers.CVC4);
		testAvailability(Solvers.MATHSAT5);
		testAvailability(Solvers.PRINCESS);
		testAvailability(Solvers.SMTINTERPOL);
		testAvailability(Solvers.Z3);
	}

	private void solversMac(){
		testAvailability(Solvers.PRINCESS);
		testAvailability(Solvers.SMTINTERPOL);
		testAvailability(Solvers.Z3);
	}

	@Test
	public void solvers() {
		try {
			if(OSType.IS_UNIX){
				solversUnix();
			}
			if(OSType.IS_MAC){
				solversMac();
			}
			if(OSType.IS_WINDOWS){
				solversWindows();
			}
		} catch (final Exception e) {
			Logger.logError(e);
			fail();
		}
	}

	public void testAvailability(Solvers solver) {
		final Configuration config = Configuration.defaultConfiguration();
		final LogManager logger = LogManager.createNullLogManager();
		final ShutdownNotifier notifier = ShutdownNotifier.createDummy();

		try (SolverContext context = SolverContextFactory.createSolverContext(config, logger, notifier, solver)) {
			assertNotNull(context.getVersion());
		} catch (final InvalidConfigurationException e) {
			fail(solver + " not available!");
		}
	}

}
