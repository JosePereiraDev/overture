package org.overture.typechecker.assistant.pattern;

import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.patterns.AUnionPattern;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;

public class AUnionPatternAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public AUnionPatternAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static void typeResolve(AUnionPattern pattern,
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
			PPatternAssistantTC.typeResolve(pattern.getLeft(), rootVisitor, question);
			PPatternAssistantTC.typeResolve(pattern.getRight(), rootVisitor, question);
		} catch (TypeCheckException e)
		{
			unResolve(pattern);
			throw e;
		}

	}

	public static void unResolve(AUnionPattern pattern)
	{
		PPatternAssistantTC.unResolve(pattern.getLeft());
		PPatternAssistantTC.unResolve(pattern.getRight());
		pattern.setResolved(false);

	}

	public static List<PDefinition> getAllDefinitions(AUnionPattern rp,
			PType type, NameScope scope)
	{

		List<PDefinition> defs = new Vector<PDefinition>();

		if (!PTypeAssistantTC.isSet(type))
		{
			TypeCheckerErrors.report(3206, "Matching expression is not a set type", rp.getLocation(), rp);
		}

		defs.addAll(PPatternAssistantTC.getDefinitions(rp.getLeft(), type, scope));
		defs.addAll(PPatternAssistantTC.getDefinitions(rp.getRight(), type, scope));

		return defs;
	}

}
