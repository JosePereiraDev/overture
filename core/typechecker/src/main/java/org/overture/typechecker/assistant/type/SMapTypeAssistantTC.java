package org.overture.typechecker.assistant.type;

import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.types.PType;
import org.overture.ast.types.SMapType;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;


public class SMapTypeAssistantTC {
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public SMapTypeAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}
	public static void unResolve(SMapType type) {
		if (!type.getResolved()) return; else { type.setResolved(false); }

		if (!type.getEmpty())
		{
			PTypeAssistantTC.unResolve(type.getFrom());
			PTypeAssistantTC.unResolve(type.getTo());
		}
		
	}

	public static PType typeResolve(SMapType type, ATypeDefinition root,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) {
		if (type.getResolved()) return type; else { type.setResolved(true); }

		try
		{
			if (!type.getEmpty())
			{
				type.setFrom(af.createPTypeAssistant().typeResolve(type.getFrom(), root, rootVisitor, question));
				type.setTo(af.createPTypeAssistant().typeResolve(type.getTo(), root, rootVisitor, question));
			}

			return type;
		}
		catch (TypeCheckException e)
		{
			unResolve(type);
			throw e;
		}
	}

	public static boolean equals(SMapType type, Object other) {
		other = PTypeAssistantTC.deBracket(other);

		if (other.getClass() == type.getClass())	// inmaps too
		{
			SMapType mt = (SMapType)other;
			return PTypeAssistantTC.equals(type.getFrom(),mt.getFrom()) && PTypeAssistantTC.equals(type.getTo(), mt.getTo());
		}

		return false;
	}

	public static boolean isMap(SMapType type) {		
		return true;
	}
	
	public static SMapType getMap(SMapType type) {		
		return type;
	}

	public static PType polymorph(SMapType type, ILexNameToken pname,
			PType actualType) {
		
		return AstFactory.newAMapMapType(type.getLocation(), 
				PTypeAssistantTC.polymorph(type.getFrom(), pname, actualType), 
				PTypeAssistantTC.polymorph(type.getTo(), pname, actualType));
	}

}
