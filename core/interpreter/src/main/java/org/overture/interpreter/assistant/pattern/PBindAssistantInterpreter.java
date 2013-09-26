package org.overture.interpreter.assistant.pattern;

import org.overture.ast.patterns.ASetBind;
import org.overture.ast.patterns.ATypeBind;
import org.overture.ast.patterns.PBind;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ObjectContext;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.ValueList;
import org.overture.typechecker.assistant.pattern.PBindAssistantTC;

public class PBindAssistantInterpreter extends PBindAssistantTC
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public PBindAssistantInterpreter(IInterpreterAssistantFactory af)
	{
		super(af);
		this.af = af;
	}

	public static ValueList getBindValues(PBind bind, Context ctxt)
			throws ValueException
	{
		switch (bind.kindPBind())
		{
			case ASetBind.kindPBind:
				return ASetBindAssistantInterpreter.getBindValues((ASetBind) bind, ctxt);
			case ATypeBind.kindPBind:
				return ATypeBindAssistantInterpreter.getBindValues((ATypeBind) bind, ctxt);
			default:
				assert false : "Should not happen";
				return null;
		}
	}

	public static ValueList getValues(PBind bind, ObjectContext ctxt)
	{
		switch (bind.kindPBind())
		{
			case ASetBind.kindPBind:
				return ASetBindAssistantInterpreter.getValues((ASetBind) bind, ctxt);
			case ATypeBind.kindPBind:
				return ATypeBindAssistantInterpreter.getValues((ATypeBind) bind, ctxt);
			default:
				return new ValueList();
		}
	}

}
