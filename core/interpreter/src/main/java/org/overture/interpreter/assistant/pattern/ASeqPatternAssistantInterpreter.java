package org.overture.interpreter.assistant.pattern;

import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.ASeqPattern;
import org.overture.ast.patterns.PPattern;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.PatternMatchException;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.runtime.VdmRuntimeError;
import org.overture.interpreter.traces.Permutor;
import org.overture.interpreter.values.NameValuePair;
import org.overture.interpreter.values.NameValuePairList;
import org.overture.interpreter.values.NameValuePairMap;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueList;
import org.overture.typechecker.assistant.pattern.ASeqPatternAssistantTC;

public class ASeqPatternAssistantInterpreter extends ASeqPatternAssistantTC
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public ASeqPatternAssistantInterpreter(IInterpreterAssistantFactory af)
	{
		super(af);
		this.af = af;
	}

	public static List<NameValuePairList> getAllNamedValues(
			ASeqPattern pattern, Value expval, Context ctxt)
			throws PatternMatchException
	{
		ValueList values = null;

		try
		{
			values = expval.seqValue(ctxt);
		} catch (ValueException e)
		{
			VdmRuntimeError.patternFail(e, pattern.getLocation());
		}

		if (values.size() != pattern.getPlist().size())
		{
			VdmRuntimeError.patternFail(4117, "Wrong number of elements for sequence pattern", pattern.getLocation());
		}

		ListIterator<Value> iter = values.listIterator();
		List<List<NameValuePairList>> nvplists = new Vector<List<NameValuePairList>>();
		int psize = pattern.getPlist().size();
		int[] counts = new int[psize];
		int i = 0;

		for (PPattern p : pattern.getPlist())
		{
			List<NameValuePairList> pnvps = PPatternAssistantInterpreter.getAllNamedValues(p, iter.next(), ctxt);
			nvplists.add(pnvps);
			counts[i++] = pnvps.size();
		}

		Permutor permutor = new Permutor(counts);
		List<NameValuePairList> finalResults = new Vector<NameValuePairList>();

		if (pattern.getPlist().isEmpty())
		{
			finalResults.add(new NameValuePairList());
			return finalResults;
		}

		while (permutor.hasNext())
		{
			try
			{
				NameValuePairMap results = new NameValuePairMap();
				int[] selection = permutor.next();

				for (int p = 0; p < psize; p++)
				{
					for (NameValuePair nvp : nvplists.get(p).get(selection[p]))
					{
						Value v = results.get(nvp.name);

						if (v == null)
						{
							results.put(nvp);
						} else
						// Names match, so values must also
						{
							if (!v.equals(nvp.value))
							{
								VdmRuntimeError.patternFail(4118, "Values do not match sequence pattern", pattern.getLocation());
							}
						}
					}
				}

				finalResults.add(results.asList()); // Consistent set of nvps
			} catch (PatternMatchException pme)
			{
				// try next perm
			}
		}

		if (finalResults.isEmpty())
		{
			VdmRuntimeError.patternFail(4118, "Values do not match sequence pattern", pattern.getLocation());
		}

		return finalResults;
	}

	public static boolean isConstrained(ASeqPattern pattern)
	{
		return PPatternListAssistantInterpreter.isConstrained(pattern.getPlist());
	}

	public static int getLength(ASeqPattern pattern)
	{
		return pattern.getPlist().size();
	}

	public static List<AIdentifierPattern> findIdentifiers(ASeqPattern pattern)
	{
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();

		for (PPattern p : pattern.getPlist())
		{
			list.addAll(PPatternAssistantInterpreter.findIdentifiers(p));
		}

		return list;
	}

}
