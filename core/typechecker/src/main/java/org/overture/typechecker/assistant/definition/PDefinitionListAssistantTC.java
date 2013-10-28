package org.overture.typechecker.assistant.definition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.assistant.definition.PDefinitionAssistant;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexNameList;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.AAccessSpecifierAccessSpecifier;
import org.overture.ast.types.PType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class PDefinitionListAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public PDefinitionListAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static void implicitDefinitions(List<PDefinition> paramDefinitions,
			Environment env)
	{
		for (PDefinition d : paramDefinitions)
		{
			PDefinitionAssistantTC.implicitDefinitions(d, env);
			// System.out.println();
		}

	}

	public static PDefinition findName(List<PDefinition> definitions,
			ILexNameToken name, NameScope scope)
	{
		for (PDefinition d : definitions)
		{
			PDefinition def = PDefinitionAssistantTC.findName(d, name, scope);

			if (def != null)
			{
				return def;
			}
		}

		return null;
	}

	public static AStateDefinition findStateDefinition(
			List<PDefinition> definitions)
	{
		for (PDefinition d : definitions)
		{
			if (d instanceof AStateDefinition)
			{
				return (AStateDefinition) d;
			}
		}

		return null;
	}

	public static void unusedCheck(List<PDefinition> definitions)
	{
		for (PDefinition d : definitions)
		{
			PDefinitionAssistantTC.unusedCheck(d);
		}

	}

	public  Set<PDefinition> findMatches(List<PDefinition> definitions,
			ILexNameToken name)
	{

		Set<PDefinition> set = new HashSet<PDefinition>();

		for (PDefinition d : singleDefinitions(definitions))
		{
			if (PDefinitionAssistantTC.isFunctionOrOperation(d)
					&& d.getName().matches(name))
			{
				set.add(d);
			}
		}

		return set;
	}

	public static List<PDefinition> singleDefinitions(
			List<PDefinition> definitions)
	{
		List<PDefinition> all = new ArrayList<PDefinition>();

		for (PDefinition d : definitions)
		{
			all.addAll(af.createPDefinitionAssistant().getDefinitions(d));
		}

		return all;
	}

	public static void markUsed(List<PDefinition> definitions)
	{
		for (PDefinition d : definitions)
		{
			PDefinitionAssistantTC.markUsed(d);
		}

	}

	public static void typeCheck(List<PDefinition> defs,
			QuestionAnswerAdaptor<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) throws AnalysisException
	{
		for (PDefinition d : defs)
		{
			d.apply(rootVisitor, question);
		}
	}

	public static LexNameList getVariableNames(List<PDefinition> list)
	{

		LexNameList variableNames = new LexNameList();

		for (PDefinition d : list)
		{
			variableNames.addAll(PDefinitionAssistantTC.getVariableNames(d));
		}

		return variableNames;
	}

	public static void setAccessibility(List<PDefinition> defs,
			AAccessSpecifierAccessSpecifier access)
	{
		for (PDefinition d : defs)
		{
			d.setAccess(access.clone());
		}

	}

	public static void typeResolve(List<PDefinition> definitions,
			QuestionAnswerAdaptor<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) throws AnalysisException
	{
		for (PDefinition definition : definitions)
		{
			PDefinitionAssistantTC.typeResolve(definition, rootVisitor, question);
		}

	}

	public static PDefinition findType(LinkedList<PDefinition> actualDefs,
			ILexNameToken name, String fromModule)
	{
		for (PDefinition d : actualDefs)
		{
			PDefinition def = PDefinitionAssistantTC.findType(d, name, fromModule);

			if (def != null)
			{
				return def;
			}
		}

		return null;
	}

	public static void initializedCheck(LinkedList<PDefinition> definitions)
	{
		for (PDefinition d : definitions)
		{
			if (d instanceof AInstanceVariableDefinition)
			{
				AInstanceVariableDefinition ivd = (AInstanceVariableDefinition) d;
				AInstanceVariableDefinitionAssistantTC.initializedCheck(ivd);
			}
		}
	}



	public static void setClassDefinition(List<PDefinition> defs,
			SClassDefinition classDefinition)
	{
		PDefinitionAssistant.setClassDefinition(defs, classDefinition);

	}
}
