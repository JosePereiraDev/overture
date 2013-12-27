package org.overture.typechecker.assistant.pattern;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.patterns.AExpressionPattern;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class AExpressionPatternAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public AExpressionPatternAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static void typeResolve(AExpressionPattern pattern,
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
			question.qualifiers = null;
			question.scope = NameScope.NAMESANDSTATE;
			pattern.getExp().apply(rootVisitor, question);
		} catch (TypeCheckException e)
		{
			PPatternAssistantTC.unResolve(pattern);
			throw e;
		}

	}

}
