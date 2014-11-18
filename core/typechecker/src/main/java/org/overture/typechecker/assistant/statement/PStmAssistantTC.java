/*
 * #%~
 * The VDM Type Checker
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
package org.overture.typechecker.assistant.statement;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.assistant.IAstAssistant;
import org.overture.ast.statements.PStm;
import org.overture.ast.util.PTypeSet;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class PStmAssistantTC implements IAstAssistant
{
	protected ITypeCheckerAssistantFactory af;

	public PStmAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	//FIXME: only used in 1 class. move it
	public PTypeSet exitCheck(PStm statement)
	{
		try
		{
			return statement.apply(af.getExitTypeCollector());// FIXME: should we handle exceptions like this
		} catch (AnalysisException e)
		{
			return new PTypeSet(af);
		}

	}

}
