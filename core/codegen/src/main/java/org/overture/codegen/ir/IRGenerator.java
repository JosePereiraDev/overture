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
package org.overture.codegen.ir;

import java.util.HashSet;
import java.util.Set;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.codegen.cgast.SExpCG;
import org.overture.codegen.cgast.declarations.AClassDeclCG;
import org.overture.codegen.cgast.declarations.AInterfaceDeclCG;
import org.overture.codegen.logging.ILogger;
import org.overture.codegen.logging.Logger;

public class IRGenerator
{
	private IRInfo codeGenInfo;

	public IRGenerator(ILogger log, String objectInitCallPrefix)
	{
		this.codeGenInfo = new IRInfo(objectInitCallPrefix);
		Logger.setLog(log);
	}

	public IRClassDeclStatus generateFrom(SClassDefinition classDef)
			throws AnalysisException
	{
		codeGenInfo.clearNodes();

		AClassDeclCG classCg = classDef.apply(codeGenInfo.getClassVisitor(), codeGenInfo);
		Set<NodeInfo> unsupportedNodes = copyGetUnsupportedNodes();

		return new IRClassDeclStatus(classDef.getName().getName(), classCg, unsupportedNodes);
	}

	public IRExpStatus generateFrom(PExp exp) throws AnalysisException
	{
		codeGenInfo.clearNodes();

		SExpCG expCg = exp.apply(codeGenInfo.getExpVisitor(), codeGenInfo);
		Set<NodeInfo> unsupportedNodes = copyGetUnsupportedNodes();

		return new IRExpStatus(expCg, unsupportedNodes);
	}

	private Set<NodeInfo> copyGetUnsupportedNodes()
	{
		return new HashSet<NodeInfo>(codeGenInfo.getUnsupportedNodes());
	}

	public AInterfaceDeclCG getQuotes()
	{
		return codeGenInfo.getQuotes();
	}

	public IRInfo getIRInfo()
	{
		return codeGenInfo;
	}
}
