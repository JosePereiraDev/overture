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
package org.overture.codegen.tests;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.codegen.analysis.violations.InvalidNamesResult;
import org.overture.codegen.analysis.violations.UnsupportedModelingException;
import org.overture.codegen.utils.GeneratedData;
import org.overture.codegen.utils.GeneratedModule;
import org.overture.codegen.vdm2java.JavaCodeGenUtil;

public class SpecificationTestCase extends CodeGenBaseTestCase
{
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String MODULE_DELIMITER = LINE_SEPARATOR
			+ "##########" + LINE_SEPARATOR;
	private static final String NAME_VIOLATION_INDICATOR = "*Name Violations*";

	public SpecificationTestCase()
	{
		super();
	}

	public SpecificationTestCase(File file)
	{
		super(file);
	}

	@Override
	protected String generateActualOutput() throws AnalysisException
	{
		StringBuilder generatedCode = new StringBuilder();

		GeneratedData data = null;

		List<File> files = new LinkedList<File>();
		files.add(file);

		try
		{
			data = JavaCodeGenUtil.generateJavaFromFiles(files, getIrSettings(), getJavaSettings(), Dialect.VDM_PP);
		} catch (UnsupportedModelingException e)
		{
			return JavaCodeGenUtil.constructUnsupportedModelingString(e);
		}

		List<GeneratedModule> classes = data.getClasses();

		for (GeneratedModule classCg : classes)
		{
			generatedCode.append(classCg.getContent());
			generatedCode.append(MODULE_DELIMITER);
		}

		GeneratedModule quoteData = data.getQuoteValues();

		if (quoteData != null)
		{
			generatedCode.append(LINE_SEPARATOR + quoteData.getContent());
			generatedCode.append(MODULE_DELIMITER);
		}

		InvalidNamesResult invalidNames = data.getInvalidNamesResult();

		if (invalidNames != null && !invalidNames.isEmpty())
		{
			generatedCode.append(NAME_VIOLATION_INDICATOR + LINE_SEPARATOR);
			generatedCode.append(LINE_SEPARATOR
					+ JavaCodeGenUtil.constructNameViolationsString(invalidNames));
			generatedCode.append(MODULE_DELIMITER);
		}

		return generatedCode.toString();
	}
}
