/*
 * #%~
 * VDM Code Generator
 * %%
 * Copyright (C) 2008 - 2014 Overture
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #~%
 */
package org.overture.codegen.trans.iterator;

import java.util.List;

import org.overture.codegen.cgast.SExpCG;
import org.overture.codegen.cgast.SPatternCG;
import org.overture.codegen.cgast.STypeCG;
import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.declarations.AVarLocalDeclCG;
import org.overture.codegen.cgast.expressions.AIdentifierVarExpCG;
import org.overture.codegen.cgast.patterns.AIdentifierPatternCG;
import org.overture.codegen.cgast.statements.ALocalPatternAssignmentStmCG;
import org.overture.codegen.cgast.types.ABoolBasicTypeCG;
import org.overture.codegen.cgast.types.AClassTypeCG;
import org.overture.codegen.ir.ITempVarGen;
import org.overture.codegen.trans.TempVarPrefixes;
import org.overture.codegen.trans.assistants.TransformationAssistantCG;

public class JavaLanguageIterator extends AbstractLanguageIterator
{
	private static final String GET_ITERATOR = "iterator";
	private static final String NEXT_ELEMENT_ITERATOR = "next";
	private static final String HAS_NEXT_ELEMENT_ITERATOR = "hasNext";
	private static final String ITERATOR_TYPE = "Iterator";

	public JavaLanguageIterator(
			TransformationAssistantCG transformationAssistant,
			ITempVarGen tempGen, TempVarPrefixes varPrefixes)
	{
		super(transformationAssistant, tempGen, varPrefixes);
	}

	protected String iteratorName;

	@Override
	public AVarLocalDeclCG getForLoopInit(AIdentifierVarExpCG setVar,
			List<SPatternCG> patterns, SPatternCG pattern)
	{
		iteratorName = tempGen.nextVarName(varPrefixes.getIteratorNamePrefix());
		String setName = setVar.getOriginal();
		AClassTypeCG iteratorType = transformationAssistant.consClassType(ITERATOR_TYPE);
		STypeCG setType = setVar.getType().clone();
		SExpCG getIteratorCall = transformationAssistant.consInstanceCall(setType, setName, iteratorType.clone(), GET_ITERATOR, null);

		AVarLocalDeclCG iteratorDecl = new AVarLocalDeclCG();

		AIdentifierPatternCG idPattern = new AIdentifierPatternCG();
		idPattern.setName(iteratorName);

		iteratorDecl.setPattern(idPattern);
		iteratorDecl.setType(iteratorType);
		iteratorDecl.setExp(getIteratorCall);

		return iteratorDecl;
	}

	@Override
	public SExpCG getForLoopCond(AIdentifierVarExpCG setVar,
			List<SPatternCG> patterns, SPatternCG pattern)
			throws AnalysisException
	{
		AClassTypeCG iteratorType = transformationAssistant.consClassType(ITERATOR_TYPE);

		return transformationAssistant.consInstanceCall(iteratorType, iteratorName, new ABoolBasicTypeCG(), HAS_NEXT_ELEMENT_ITERATOR, null);
	}

	@Override
	public SExpCG getForLoopInc(AIdentifierVarExpCG setVar,
			List<SPatternCG> patterns, SPatternCG pattern)
	{
		return null;
	}

	@Override
	public AVarLocalDeclCG getNextElementDeclared(AIdentifierVarExpCG setVar,
			List<SPatternCG> patterns, SPatternCG pattern)
			throws AnalysisException
	{
		STypeCG elementType = transformationAssistant.getSetTypeCloned(setVar).getSetOf();

		return transformationAssistant.consNextElementDeclared(ITERATOR_TYPE, elementType, pattern, iteratorName, NEXT_ELEMENT_ITERATOR);
	}

	@Override
	public ALocalPatternAssignmentStmCG getNextElementAssigned(
			AIdentifierVarExpCG setVar, List<SPatternCG> patterns,
			SPatternCG pattern, AVarLocalDeclCG successVarDecl,
			AVarLocalDeclCG nextElementDecl) throws AnalysisException
	{
		STypeCG elementType = transformationAssistant.getSetTypeCloned(setVar).getSetOf();

		return transformationAssistant.consNextElementAssignment(ITERATOR_TYPE, elementType, pattern, iteratorName, NEXT_ELEMENT_ITERATOR, nextElementDecl);
	}
}
