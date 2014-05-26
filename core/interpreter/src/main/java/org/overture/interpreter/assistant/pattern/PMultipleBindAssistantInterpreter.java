package org.overture.interpreter.assistant.pattern;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.patterns.PMultipleBind;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ObjectContext;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.ValueList;
import org.overture.typechecker.assistant.pattern.PMultipleBindAssistantTC;

public class PMultipleBindAssistantInterpreter extends PMultipleBindAssistantTC
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public PMultipleBindAssistantInterpreter(IInterpreterAssistantFactory af)
	{
		super(af);
		this.af = af;
	}

	public ValueList getBindValues(PMultipleBind mb, Context ctxt)
			throws ValueException, AnalysisException
	{
//		try
//		{
			return mb.apply(af.getBindValuesCollector(), ctxt);
//		} catch (AnalysisException e)
//		{
//			return null;
//		}
//		if (mb instanceof ASetMultipleBind)
//		{
//			return ASetMultipleBindAssistantInterpreter.getBindValues((ASetMultipleBind) mb, ctxt);
//		} else if (mb instanceof ATypeMultipleBind)
//		{
//			return ATypeMultipleBindAssistantInterpreter.getBindValues((ATypeMultipleBind) mb, ctxt);
//		} else
//		{
//		}
//		return null;
	}

	public ValueList getValues(PMultipleBind mb, ObjectContext ctxt)
	{
		try
		{
			return mb.apply(af.getValueCollector(), ctxt);// FIXME: should we handle exceptions like this
		} catch (AnalysisException e)
		{
			return new ValueList();
		}
//		if (mb instanceof ASetMultipleBind)
//		{
//			return ASetMultipleBindAssistantInterpreter.getValues((ASetMultipleBind) mb, ctxt);
//		} else if (mb instanceof ATypeMultipleBind)
//		{
//			return ATypeMultipleBindAssistantInterpreter.getValues((ATypeMultipleBind) mb, ctxt);
//		} else
//		{
//			return new ValueList();
//		}
	}

}
