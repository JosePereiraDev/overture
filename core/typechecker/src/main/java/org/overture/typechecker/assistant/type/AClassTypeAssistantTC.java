package org.overture.typechecker.assistant.type;

import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.intf.lex.ILexIdentifierToken;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.PType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.PrivateClassEnvironment;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.definition.SClassDefinitionAssistantTC;

public class AClassTypeAssistantTC {

	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public AClassTypeAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static LexNameToken getMemberName(AClassType cls,
			ILexIdentifierToken id) {
		// Note: not explicit
		return new LexNameToken(cls.getName().getName(), id.getName(), id.getLocation(), false, false);
	}

	public static PDefinition findName(AClassType cls, ILexNameToken tag, NameScope scope) {
		return  SClassDefinitionAssistantTC.findName(cls.getClassdef(),tag, scope);
	}

	public static boolean hasSupertype(AClassType sclass, PType other) {
		return SClassDefinitionAssistantTC.hasSupertype(sclass.getClassdef(),other);
	}

	public static PType typeResolve(AClassType type, ATypeDefinition root,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) {
		
		if (type.getResolved()) return type; else type.setResolved(true);

		try
		{
			// We have to add a private class environment here because the
			// one passed in may be from a class that contains a reference
			// to this class. We need the private environment to see all
			// the definitions that are available to us while resolving...

			Environment self = new PrivateClassEnvironment(question.assistantFactory,type.getClassdef(), question.env);

			for (PDefinition d: type.getClassdef().getDefinitions())
			{
				// There is a problem resolving ParameterTypes via a FunctionType
				// when this is not being done via ExplicitFunctionDefinition
				// which extends the environment with the type names that
				// are in scope. So we skip these here.

				if (d instanceof AExplicitFunctionDefinition)
				{
					AExplicitFunctionDefinition fd = (AExplicitFunctionDefinition)d;

					if (fd.getTypeParams() != null)
					{
						continue;	// Skip polymorphic functions
					}
				}
				question = new TypeCheckInfo(question.assistantFactory,self,question.scope,question.qualifiers);				
				af.createPTypeAssistant().typeResolve(question.assistantFactory.createPDefinitionAssistant().getType(d), root, rootVisitor, question);
			}

			return type;
		}
		catch (TypeCheckException e)
		{
			unResolve(type);
			throw e;
		}
		
	}

	public static void unResolve(AClassType type) {
		if (type.getResolved())
		{
    		type.setResolved(false);

    		for (PDefinition d: type.getClassdef().getDefinitions())
    		{
    			PTypeAssistantTC.unResolve(af.createPDefinitionAssistant().getType(d));
    		}
		}
		
	}

	public static String toDisplay(AClassType exptype) {
		return exptype.getClassdef().getName().getName();
	}

	public static boolean equals(AClassType type, Object other) {
		other = PTypeAssistantTC.deBracket(other);

		if (other instanceof AClassType)
		{
			AClassType oc = (AClassType)other;
			return type.getName().equals(oc.getName());		// NB. name only
		}

		return false;
	}

	public static boolean isClass(AClassType type) {
		return true;
	}

	public static SClassDefinition getClass(SClassDefinition type)
	{
		return type;
	}
	
}
