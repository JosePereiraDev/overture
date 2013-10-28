package org.overture.typechecker.assistant.pattern;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.patterns.AMapletPatternMaplet;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;
import org.overture.ast.types.SMapType;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class AMapletPatternMapletAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public AMapletPatternMapletAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static void unResolve(AMapletPatternMaplet mp)
	{
		PPatternAssistantTC.unResolve(mp.getFrom());
		PPatternAssistantTC.unResolve(mp.getTo());
		mp.setResolved(false);

	}

	public static void typeResolve(AMapletPatternMaplet mp,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) throws AnalysisException
	{
		if (mp.getResolved())
			return;
		else
		{
			mp.setResolved(true);
		}

		try
		{
			PPatternAssistantTC.typeResolve(mp.getFrom(), rootVisitor, question);
			PPatternAssistantTC.typeResolve(mp.getTo(), rootVisitor, question);
		} catch (TypeCheckException e)
		{
			unResolve(mp);
			throw e;
		}
	}

	public static Collection<? extends PDefinition> getDefinitions(
			AMapletPatternMaplet p, SMapType map, NameScope scope)
	{

		List<PDefinition> list = new Vector<PDefinition>();
		list.addAll(PPatternAssistantTC.getDefinitions(p.getFrom(), map.getFrom(), scope));
		list.addAll(PPatternAssistantTC.getDefinitions(p.getTo(), map.getTo(), scope));
		return list;
	}

	public static boolean isSimple(AMapletPatternMaplet p)
	{
		return PPatternAssistantTC.isSimple(p.getFrom())
				&& PPatternAssistantTC.isSimple(p.getTo());
	}

}
