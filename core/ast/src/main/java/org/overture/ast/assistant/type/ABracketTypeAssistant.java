package org.overture.ast.assistant.type;

import org.overture.ast.assistant.IAstAssistantFactory;

public class ABracketTypeAssistant
{

	protected static IAstAssistantFactory af;

	@SuppressWarnings("static-access")
	public ABracketTypeAssistant(IAstAssistantFactory af)
	{
		this.af = af;
	}

//	public static boolean isNumeric(ABracketType type)
//	{
//		return PTypeAssistant.isNumeric(type.getType());
//	}

//	public static SNumericBasicType getNumeric(ABracketType type)
//	{
//		return PTypeAssistant.getNumeric(type.getType());
//	}

}
