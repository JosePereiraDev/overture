package org.overture.ast.assistant.type;

import org.overture.ast.assistant.IAstAssistantFactory;

public class AOptionalTypeAssistant
{

	protected static IAstAssistantFactory af;

	@SuppressWarnings("static-access")
	public AOptionalTypeAssistant(IAstAssistantFactory af)
	{
		this.af = af;
	}

//	public static boolean isNumeric(AOptionalType type)
//	{
//		return PTypeAssistant.isNumeric(type.getType());
//	}

//	public static SNumericBasicType getNumeric(AOptionalType type)
//	{
//		return PTypeAssistant.getNumeric(type.getType());
//	}

}
