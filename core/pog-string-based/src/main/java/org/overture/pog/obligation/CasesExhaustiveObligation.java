/*******************************************************************************
 *
 *	Copyright (C) 2008 Fujitsu Services Ltd.
 *
 *	Author: Nick Battle
 *
 *	This file is part of VDMJ.
 *
 *	VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package org.overture.pog.obligation;

import org.overture.ast.expressions.ACaseAlternative;
import org.overture.ast.expressions.ACasesExp;
import org.overture.ast.expressions.PExp;
import org.overture.typechecker.assistant.pattern.PPatternAssistantTC;

public class CasesExhaustiveObligation extends ProofObligation
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2266396606434510800L;

	public CasesExhaustiveObligation(ACasesExp exp, POContextStack ctxt)
	{
		super(exp.getLocation(), POType.CASES_EXHAUSTIVE, ctxt);
		StringBuilder sb = new StringBuilder();
		String prefix = "";

		for (ACaseAlternative alt : exp.getCases())
		{
			sb.append(prefix);

			if (ctxt.assistantFactory.createPPatternAssistant().isSimple(alt.getPattern()))
			{
				sb.append(exp.getExpression());
				sb.append(" = ");
				sb.append(alt.getPattern());
			} else
			{

				PExp matching = ctxt.assistantFactory.createPPatternAssistant().getMatchingExpression(alt.getPattern());

				sb.append("(exists ");
				sb.append(matching);
				sb.append(":");
				sb.append(exp.getExpression().getType());
				sb.append(" & ");
				sb.append(exp.getExpression());
				sb.append(" = ");
				sb.append(matching);
				sb.append(")");
			}

			prefix = " or ";
		}

		value = ctxt.getObligation(sb.toString());
	}
};
