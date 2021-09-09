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

import java.math.*;
import java.nio.file.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.spldev.formula.*;
import org.spldev.formula.expression.*;
import org.spldev.formula.expression.atomic.literal.*;
import org.spldev.formula.expression.compound.*;
import org.spldev.formula.expression.term.bool.*;
import org.spldev.util.*;
import org.spldev.util.extension.*;
import org.spldev.util.logging.*;

public class CountSolutionAnalysisTest {

	private static final Path modelDirectory = Paths.get("src/test/resources/testFeatureModels");

	private static final List<String> modelNames = Arrays.asList( //
		"basic", //
		"simple", //
		"car", //
		"gpl_medium_model", //
		"500-100");

	private static ModelRepresentation load(final Path modelFile) {
		return ModelRepresentation.load(modelFile) //
			.orElseThrow(p -> new IllegalArgumentException(p.isEmpty() ? null : p.get(0).getError().get()));
	}

	static {
		ExtensionLoader.load();
	}

	@Test
	public void count() {
		final VariableMap variables = VariableMap.fromNames(Arrays.asList("a", "b", "c"));
		final Literal a = new LiteralPredicate((BoolVariable) variables.getVariable("a").get(), true);
		final Literal b = new LiteralPredicate((BoolVariable) variables.getVariable("b").get(), true);
		final Literal c = new LiteralPredicate((BoolVariable) variables.getVariable("c").get(), true);

		final Implies implies1 = new Implies(a, b);
		final Or or = new Or(implies1, c);
		final Biimplies equals = new Biimplies(a, b);
		final And and = new And(equals, c);
		final Implies formula = new Implies(or, and);

		final Formula cnfFormula = Formulas.toCNF(formula).get();
		final ModelRepresentation rep = new ModelRepresentation(cnfFormula);

		final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
		final Result<?> result = analysis.getResult(rep);
		result.orElse(Logger::logProblems);
		assertTrue(result.isPresent());
		assertEquals(BigInteger.valueOf(3), result.get());
	}

	@Test
	public void count2() {
		final ModelRepresentation rep = load(modelDirectory.resolve(modelNames.get(3) + ".xml"));

		final CountSolutionsAnalysis analysis = new CountSolutionsAnalysis();
		final Result<?> result = analysis.getResult(rep);
		result.orElse(Logger::logProblems);
		assertTrue(result.isPresent());
		assertEquals(BigInteger.valueOf(960), result.get());
	}

}
