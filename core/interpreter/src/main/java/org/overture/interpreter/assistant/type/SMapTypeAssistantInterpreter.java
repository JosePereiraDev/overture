package org.overture.interpreter.assistant.type;

import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.assistant.pattern.PTypeList;
import org.overture.ast.types.SMapType;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.MapValue;
import org.overture.interpreter.values.TupleValue;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueList;
import org.overture.interpreter.values.ValueMap;
import org.overture.interpreter.values.ValueSet;

public class SMapTypeAssistantInterpreter
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public SMapTypeAssistantInterpreter(IInterpreterAssistantFactory af)
	{
		this.af = af;
	}

//	public ValueList getAllValues(SMapType type, Context ctxt)
//			throws AnalysisException
//	{
//		PTypeList tuple = new PTypeList();
//		tuple.add(type.getFrom());
//		tuple.add(type.getTo());
//
//		ValueList results = new ValueList();
//		ValueList tuples = af.createPTypeListAssistant().getAllValues(tuple, ctxt);
//		ValueSet set = new ValueSet();
//		set.addAll(tuples);
//		List<ValueSet> psets = set.powerSet();
//
//		for (ValueSet map : psets)
//		{
//			ValueMap result = new ValueMap();
//
//			for (Value v : map)
//			{
//				TupleValue tv = (TupleValue) v;
//				result.put(tv.values.get(0), tv.values.get(1));
//			}
//
//			results.add(new MapValue(result));
//		}
//
//		return results;
//	}

}
