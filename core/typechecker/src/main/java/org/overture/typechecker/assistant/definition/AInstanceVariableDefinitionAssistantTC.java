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
package org.overture.typechecker.assistant.definition;

import org.overture.ast.assistant.IAstAssistant;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class AInstanceVariableDefinitionAssistantTC implements IAstAssistant
{
	protected ITypeCheckerAssistantFactory af;

	public AInstanceVariableDefinitionAssistantTC(
			ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	//FIXME only used in 1 place. move it
	public void initializedCheck(AInstanceVariableDefinition ivd)
	{
		if (!ivd.getInitialized()
				&& !af.createPAccessSpecifierAssistant().isStatic(ivd.getAccess()))
		{
			TypeCheckerErrors.warning(5001, "Instance variable '"
					+ ivd.getName() + "' is not initialized", ivd.getLocation(), ivd);
		}

	}

}
