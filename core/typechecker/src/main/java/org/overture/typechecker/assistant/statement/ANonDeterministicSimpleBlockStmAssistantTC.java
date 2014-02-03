package org.overture.typechecker.assistant.statement;

import org.overture.ast.factory.AstFactory;
import org.overture.ast.types.AVoidReturnType;
import org.overture.ast.types.AVoidType;
import org.overture.ast.types.PType;
import org.overture.ast.util.PTypeSet;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class ANonDeterministicSimpleBlockStmAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public ANonDeterministicSimpleBlockStmAssistantTC(
			ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public boolean addOne(PTypeSet rtypes, PType add)
	{
		if (add instanceof AVoidReturnType)
		{
			rtypes.add(AstFactory.newAVoidType(add.getLocation()));
			return true;
		} else if (!(add instanceof AVoidType))
		{
			rtypes.add(add);
			return true;
		} else
		{
			rtypes.add(add);
			return false;
		}
	}
}
