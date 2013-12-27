package org.overture.interpreter.assistant.pattern;

import java.util.List;
import java.util.Vector;

import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.values.NameValuePair;
import org.overture.interpreter.values.NameValuePairList;
import org.overture.interpreter.values.Value;

public class AIdentifierPatternAssistantInterpreter

{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public AIdentifierPatternAssistantInterpreter(
			IInterpreterAssistantFactory af)
	{
		this.af = af;
	}

	public static List<NameValuePairList> getAllNamedValues(
			AIdentifierPattern p, Value expval, Context ctxt)
	{
		List<NameValuePairList> result = new Vector<NameValuePairList>();
		NameValuePairList list = new NameValuePairList();
		list.add(new NameValuePair(p.getName(), expval));
		result.add(list);
		return result;
	}

	public static boolean isConstrained(AIdentifierPattern pattern)
	{
		return pattern.getConstrained(); // The variable may be constrained to be the same as another occurrence
	}

	public static int getLength(AIdentifierPattern pattern)
	{
		return PPatternAssistantInterpreter.ANY; // Special value meaning "any length"
	}

	public static List<AIdentifierPattern> findIdentifiers(
			AIdentifierPattern pattern)
	{
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();
		list.add(pattern);
		return list;
	}

}
