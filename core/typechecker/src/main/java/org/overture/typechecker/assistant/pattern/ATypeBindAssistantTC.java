package org.overture.typechecker.assistant.pattern;

import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.lex.LexNameList;
import org.overture.ast.patterns.ATypeBind;
import org.overture.ast.patterns.PMultipleBind;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class ATypeBindAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public ATypeBindAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static void typeResolve(ATypeBind typebind,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question)
	{

		typebind.setType(af.createPTypeAssistant().typeResolve(typebind.getType(), null, rootVisitor, question));

	}

	public static List<PMultipleBind> getMultipleBindList(ATypeBind bind)
	{
		List<PPattern> plist = new Vector<PPattern>();
		plist.add(bind.getPattern().clone());
		List<PMultipleBind> mblist = new Vector<PMultipleBind>();
		mblist.add(AstFactory.newATypeMultipleBind(plist, bind.getType().clone()));
		return mblist;
	}

	public static LexNameList getOldNames(ATypeBind bind)
	{
		return new LexNameList();
	}

}
