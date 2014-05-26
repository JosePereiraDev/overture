package org.overture.interpreter.assistant.expression;

import org.overture.ast.expressions.AIsOfBaseClassExp;
import org.overture.ast.expressions.PExp;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.ObjectContext;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.ValueList;

public class AIsOfBaseClassExpAssistantInterpreter// extends
// AIsOfBaseClassExpAssistantTC
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public AIsOfBaseClassExpAssistantInterpreter(IInterpreterAssistantFactory af)
	{
		this.af = af;
	}

//	public static ValueList getValues(AIsOfBaseClassExp exp, ObjectContext ctxt)
//	{
//		return PExpAssistantInterpreter.getValues(exp.getExp(), ctxt);
//	}

//	public static PExp findExpression(AIsOfBaseClassExp exp, int lineno)
//	{
//		PExp found = PExpAssistantInterpreter.findExpressionBaseCase(exp, lineno);
//		if (found != null)
//			return found;
//
//		return PExpAssistantInterpreter.findExpression(exp.getExp(), lineno);
//	}

	public static boolean search(AIsOfBaseClassExp node, ObjectValue from)
	{
		if (from.type.getName().getName().equals(node.getBaseClass().getName())
				&& from.superobjects.isEmpty())
		{
			return true;
		}

		for (ObjectValue svalue : from.superobjects)
		{
			if (search(node, svalue))
			{
				return true;
			}
		}

		return false;
	}

}
