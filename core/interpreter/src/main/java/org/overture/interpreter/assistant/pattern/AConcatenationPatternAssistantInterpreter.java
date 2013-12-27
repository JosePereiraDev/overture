package org.overture.interpreter.assistant.pattern;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.overture.ast.patterns.AConcatenationPattern;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.PatternMatchException;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.runtime.VdmRuntimeError;
import org.overture.interpreter.traces.Permutor;
import org.overture.interpreter.values.NameValuePair;
import org.overture.interpreter.values.NameValuePairList;
import org.overture.interpreter.values.NameValuePairMap;
import org.overture.interpreter.values.SeqValue;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueList;
import org.overture.typechecker.assistant.pattern.AConcatenationPatternAssistantTC;

public class AConcatenationPatternAssistantInterpreter extends
		AConcatenationPatternAssistantTC
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public AConcatenationPatternAssistantInterpreter(
			IInterpreterAssistantFactory af)
	{
		super(af);
		this.af = af;
	}

	public static List<NameValuePairList> getAllNamedValues(
			AConcatenationPattern pattern, Value expval, Context ctxt)
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

		int llen = PPatternAssistantInterpreter.getLength(pattern.getLeft());
		int rlen = PPatternAssistantInterpreter.getLength(pattern.getRight());
		int size = values.size();

		if (llen == PPatternAssistantInterpreter.ANY && rlen > size
				|| rlen == PPatternAssistantInterpreter.ANY && llen > size
				|| rlen != PPatternAssistantInterpreter.ANY
				&& llen != PPatternAssistantInterpreter.ANY
				&& size != llen + rlen)
		{
			VdmRuntimeError.patternFail(4108, "Sequence concatenation pattern does not match expression", pattern.getLocation());
		}

		// If the left and right sizes are ANY (ie. flexible) then we have to
		// generate a set of splits of the values, and offer these to sub-matches
		// to see whether they fit. Otherwise, there is just one split at this level.

		List<Integer> leftSizes = new Vector<Integer>();

		if (llen == PPatternAssistantInterpreter.ANY)
		{
			if (rlen == PPatternAssistantInterpreter.ANY)
			{
				if (size == 0)
				{
					// Can't match a ^ b with []
				} else if (size % 2 == 1)
				{
					// Odd => add the middle, then those either side
					int half = size / 2 + 1;
					if (half > 0)
					{
						leftSizes.add(half);
					}

					for (int delta = 1; half - delta > 0; delta++)
					{
						leftSizes.add(half + delta);
						leftSizes.add(half - delta);
					}

					leftSizes.add(0);
				} else
				{
					// Even => add those either side of the middle
					int half = size / 2;
					if (half > 0)
					{
						leftSizes.add(half);
					}

					for (int delta = 1; half - delta > 0; delta++)
					{
						leftSizes.add(half + delta);
						leftSizes.add(half - delta);
					}

					leftSizes.add(size);
					leftSizes.add(0);
				}
			} else
			{
				leftSizes.add(size - rlen);
			}
		} else
		{
			leftSizes.add(llen);
		}

		// Now loop through the various splits and attempt to match the l/r
		// sub-patterns to the split sequence value.

		List<NameValuePairList> finalResults = new Vector<NameValuePairList>();

		for (Integer lsize : leftSizes)
		{
			Iterator<Value> iter = values.iterator();
			ValueList head = new ValueList();

			for (int i = 0; i < lsize; i++)
			{
				head.add(iter.next());
			}

			ValueList tail = new ValueList();

			while (iter.hasNext()) // Everything else in second
			{
				tail.add(iter.next());
			}

			List<List<NameValuePairList>> nvplists = new Vector<List<NameValuePairList>>();
			int psize = 2;
			int[] counts = new int[psize];

			try
			{
				List<NameValuePairList> lnvps = PPatternAssistantInterpreter.getAllNamedValues(pattern.getLeft(), new SeqValue(head), ctxt);
				nvplists.add(lnvps);
				counts[0] = lnvps.size();

				List<NameValuePairList> rnvps = PPatternAssistantInterpreter.getAllNamedValues(pattern.getRight(), new SeqValue(tail), ctxt);
				nvplists.add(rnvps);
				counts[1] = rnvps.size();
			} catch (PatternMatchException e)
			{
				continue;
			}

			Permutor permutor = new Permutor(counts);

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
									VdmRuntimeError.patternFail(4109, "Values do not match concatenation pattern", pattern.getLocation());
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
		}

		if (finalResults.isEmpty())
		{
			VdmRuntimeError.patternFail(4109, "Values do not match concatenation pattern", pattern.getLocation());
		}

		return finalResults;
	}

	public static boolean isConstrained(AConcatenationPattern pattern)
	{
		return PPatternAssistantInterpreter.isConstrained(pattern.getLeft())
				|| PPatternAssistantInterpreter.isConstrained(pattern.getRight());
	}

	public static int getLength(AConcatenationPattern pattern)
	{
		int llen = PPatternAssistantInterpreter.getLength(pattern.getLeft());
		int rlen = PPatternAssistantInterpreter.getLength(pattern.getRight());
		return llen == PPatternAssistantInterpreter.ANY
				|| rlen == PPatternAssistantInterpreter.ANY ? PPatternAssistantInterpreter.ANY
				: llen + rlen;
	}

	public static List<AIdentifierPattern> findIdentifiers(
			AConcatenationPattern p)
	{
		List<AIdentifierPattern> list = new Vector<AIdentifierPattern>();
		list.addAll(PPatternAssistantInterpreter.findIdentifiers(p.getLeft()));
		list.addAll(PPatternAssistantInterpreter.findIdentifiers(p.getRight()));
		return list;
	}

}
