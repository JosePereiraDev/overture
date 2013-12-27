package org.overture.typechecker.assistant.pattern;

import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.patterns.ASeqPattern;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;

public class ASeqPatternAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public ASeqPatternAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static void typeResolve(ASeqPattern pattern,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) throws AnalysisException
	{

		if (pattern.getResolved())
		{
			return;
		} else
		{
			pattern.setResolved(true);
		}

		try
		{
			PPatternListAssistantTC.typeResolve(pattern.getPlist(), rootVisitor, question);
		} catch (TypeCheckException e)
		{
			unResolve(pattern);
			throw e;
		}

	}

	public static void unResolve(ASeqPattern pattern)
	{
		PPatternListAssistantTC.unResolve(pattern.getPlist());
		pattern.setResolved(false);

	}

	public static List<PDefinition> getAllDefinitions(ASeqPattern rp,
			PType type, NameScope scope)
	{

		List<PDefinition> defs = new Vector<PDefinition>();

		if (!PTypeAssistantTC.isSeq(type))
		{
			TypeCheckerErrors.report(3203, "Sequence pattern is matched against "
					+ type, rp.getLocation(), rp);
		} else
		{
			PType elem = PTypeAssistantTC.getSeq(type).getSeqof();

			for (PPattern p : rp.getPlist())
			{
				defs.addAll(PPatternAssistantTC.getDefinitions(p, elem, scope));
			}
		}

		return defs;
	}

}
