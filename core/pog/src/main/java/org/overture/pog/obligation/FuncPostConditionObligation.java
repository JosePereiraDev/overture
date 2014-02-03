/*******************************************************************************
 *
 *	Copyright (C) 2008 Fujitsu Services Ltd.
 *
 *	Author: Nick Battle
 *
 *	This file is part of VDMJ.
 *
 *	VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package org.overture.pog.obligation;

import java.util.LinkedList;
import java.util.List;

import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.expressions.AApplyExp;
import org.overture.ast.expressions.ANotYetSpecifiedExp;
import org.overture.ast.expressions.ASubclassResponsibilityExp;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstExpressionFactory;
import org.overture.ast.patterns.PPattern;
import org.overture.pog.pub.IPOContextStack;
import org.overture.pog.pub.IPogAssistantFactory;
import org.overture.pog.pub.POType;
import org.overture.typechecker.assistant.definition.AImplicitFunctionDefinitionAssistantTC;
import org.overture.typechecker.assistant.pattern.PPatternListAssistantTC;


public class FuncPostConditionObligation extends ProofObligation
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FuncPostConditionObligation(AExplicitFunctionDefinition func,
			IPOContextStack ctxt, IPogAssistantFactory assistantFactory)
	{
		super(func, POType.FUNC_POST_CONDITION, ctxt, func.getLocation());

		List<PExp> params = new LinkedList<PExp>();
		for (List<PPattern> pl : func.getParamPatternList())
		{
			params.addAll(cloneListPExp(assistantFactory.createPPatternListAssistant().getMatchingExpressionList(pl)));
		}

		PExp body = null;
		// String body = null;

		if (func.getBody() instanceof ANotYetSpecifiedExp
				|| func.getBody() instanceof ASubclassResponsibilityExp)
		{
			AApplyExp applyExp = new AApplyExp();
			applyExp.setArgs(params);
			AVariableExp varExp = new AVariableExp();
			varExp.setName(func.getName());
			applyExp.setRoot(varExp);
			// We have to say "f(a)" because we have no expression yet
			// I suppose this still still holds true with ast pos
			body = applyExp;
		} else

		{
			body = func.getBody();
		}

	//	valuetree.setContext(ctxt.getContextNodeList());
		valuetree.setPredicate(ctxt.getPredWithContext(generatePredicate(func.getPredef(), func.getPostdef().clone(), params, body)));
	}

	public FuncPostConditionObligation(AImplicitFunctionDefinition func,
			IPOContextStack ctxt, IPogAssistantFactory assistantFactory)
	{
		super(func, POType.FUNC_POST_CONDITION, ctxt, func.getLocation());

		List<PExp> params = new LinkedList<PExp>();

		
		for (List<PPattern> pl : AImplicitFunctionDefinitionAssistantTC.getParamPatternList(func))
		{
			params.addAll(assistantFactory.createPPatternListAssistant().getMatchingExpressionList(pl));
		}

		
		PExp body = null;

		// implicit body is apparently allowed
		if (func.getBody() == null)
		{
			List<PPattern> aux = new LinkedList<PPattern>();
			aux.add(func.getResult().getPattern());
			List<PExp> aux2 = assistantFactory.createPPatternListAssistant().getMatchingExpressionList(aux);
			body = aux2.get(0);

		} else if (func.getBody() instanceof ANotYetSpecifiedExp
				|| func.getBody() instanceof ASubclassResponsibilityExp)
		{
			AApplyExp applyExp = new AApplyExp();
			applyExp.setArgs(params);
			AVariableExp varExp = new AVariableExp();
			varExp.setName(func.getName().clone());
			applyExp.setRoot(varExp);
			body = applyExp;
		} else
		{
			body = func.getBody().clone();
		}

//		valuetree.setContext(ctxt.getContextNodeList());
		valuetree.setPredicate(ctxt.getPredWithContext(generatePredicate(func.getPredef(), func.getPostdef(), cloneListPExp(params), body)));

	}

	private PExp generatePredicate(AExplicitFunctionDefinition predef,
			AExplicitFunctionDefinition postdef, List<PExp> params, PExp body)
	{
		
		if (predef != null)
		{
			// pre(params) =>
			AApplyExp applyExp = new AApplyExp();
			applyExp.setArgs(cloneListPExp(params));
			AVariableExp varExp = getVarExp(predef.getName().clone());
			applyExp.setRoot(varExp);
			
			return AstExpressionFactory.newAImpliesBooleanBinaryExp(applyExp, generateBody(postdef, params, body));
			
	
		}
		return generateBody(postdef, params, body);

	}

	private PExp generateBody(AExplicitFunctionDefinition postdef,
			List<PExp> params, PExp body)
	{
		//post(params, body)
		AApplyExp applyExp = new AApplyExp();
		AVariableExp varExp = getVarExp(postdef.getName());
		applyExp.setRoot(varExp);
		List<PExp> args = params;
		args.add(body.clone());
		applyExp.setArgs(args);
		return applyExp;
	}

	
	

}
