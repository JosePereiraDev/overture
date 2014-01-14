package org.overture.typechecker.assistant.definition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.AOperationType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.type.AOperationTypeAssistantTC;

public class AImplicitOperationDefinitionAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public AImplicitOperationDefinitionAssistantTC(
			ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	@SuppressWarnings("unchecked")
	public static AExplicitFunctionDefinition getPostDefinition(
			AImplicitOperationDefinition d, Environment base)
	{

		List<List<PPattern>> parameters = new Vector<List<PPattern>>();
		List<PPattern> plist = new Vector<PPattern>();

		for (APatternListTypePair pl : (LinkedList<APatternListTypePair>) d.getParameterPatterns())
		{
			plist.addAll((Collection<PPattern>) pl.getPatterns().clone());
		}

		if (d.getResult() != null)
		{
			plist.add(d.getResult().getPattern().clone());
		}

		AStateDefinition state = d.getState();

		if (state != null)
		{
			plist.add(AstFactory.newAIdentifierPattern(state.getName().getOldName()));
			plist.add(AstFactory.newAIdentifierPattern(state.getName().clone()));
		}
		else if (base.isVDMPP())
		{
			plist.add(AstFactory.newAIdentifierPattern(d.getName().getSelfName().getOldName()));
			
			if (!PAccessSpecifierAssistantTC.isStatic(d.getAccess()))
			{
				plist.add(AstFactory.newAIdentifierPattern(d.getName().getSelfName()));
			}
		}

		parameters.add(plist);
		PExp postop = AstFactory.newAPostOpExp(d.getName().clone(), d.getPrecondition(), d.getPostcondition(), d.getErrors(), d.getState());

		AExplicitFunctionDefinition def = AstFactory.newAExplicitFunctionDefinition(d.getName().getPostName(d.getPostcondition().getLocation()), NameScope.GLOBAL, null, AOperationTypeAssistantTC.getPostType((AOperationType) d.getType(), state, d.getClassDefinition(), PAccessSpecifierAssistantTC.isStatic(d.getAccess())), parameters, postop, null, null, false, null);

		// Operation postcondition functions are effectively not static as
		// their expression can directly refer to instance variables, even
		// though at runtime these are passed via a "self" parameter.

		def.setAccess(PAccessSpecifierAssistantTC.getStatic(d, false));
		def.setClassDefinition(d.getClassDefinition());
		return def;
	}

	@SuppressWarnings("unchecked")
	public static AExplicitFunctionDefinition getPreDefinition(
			AImplicitOperationDefinition d, Environment base)
	{

		List<List<PPattern>> parameters = new Vector<List<PPattern>>();
		List<PPattern> plist = new Vector<PPattern>();

		for (APatternListTypePair pl : (LinkedList<APatternListTypePair>) d.getParameterPatterns())
		{
			plist.addAll((Collection<PPattern>) pl.getPatterns().clone());
		}

		AStateDefinition state = d.getState();

		if (state != null)
		{
			plist.add(AstFactory.newAIdentifierPattern(state.getName().clone()));
		} else if (base.isVDMPP()
				&& !PAccessSpecifierAssistantTC.isStatic(d.getAccess()))
		{
			plist.add(AstFactory.newAIdentifierPattern(d.getName().getSelfName()));
		}

		parameters.add(plist);
		PExp preop = AstFactory.newAPreOpExp(d.getName().clone(), d.getPrecondition(), d.getErrors(), d.getState());

		AExplicitFunctionDefinition def = AstFactory.newAExplicitFunctionDefinition(d.getName().getPreName(d.getPrecondition().getLocation()), NameScope.GLOBAL, null, AOperationTypeAssistantTC.getPreType((AOperationType) d.getType(), state, d.getClassDefinition(), PAccessSpecifierAssistantTC.isStatic(d.getAccess())), parameters, preop, null, null, false, null);

		// Operation precondition functions are effectively not static as
		// their expression can directly refer to instance variables, even
		// though at runtime these are passed via a "self" parameter.

		def.setAccess(PAccessSpecifierAssistantTC.getStatic(d, false));
		def.setClassDefinition(d.getClassDefinition());
		return def;
	}

	public static List<PPattern> getParamPatternList(
			AImplicitOperationDefinition definition)
	{
		List<PPattern> plist = new ArrayList<PPattern>();

		for (APatternListTypePair pl : definition.getParameterPatterns())
		{
			plist.addAll(pl.getPatterns());
		}

		return plist;
	}

	public static List<List<PPattern>> getListParamPatternList(
			AImplicitOperationDefinition func)
	{
		List<List<PPattern>> parameters = new ArrayList<List<PPattern>>();
		List<PPattern> plist = new ArrayList<PPattern>();

		for (APatternListTypePair pl : func.getParameterPatterns())
		{
			plist.addAll(pl.getPatterns());
		}

		parameters.add(plist);
		return parameters;
	}

}
