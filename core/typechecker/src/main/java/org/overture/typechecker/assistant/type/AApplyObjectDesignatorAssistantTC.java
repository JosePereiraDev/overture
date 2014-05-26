package org.overture.typechecker.assistant.type;

import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.statements.AApplyObjectDesignator;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SMapType;
import org.overture.ast.types.SSeqType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class AApplyObjectDesignatorAssistantTC
{

	protected ITypeCheckerAssistantFactory af;

	public AApplyObjectDesignatorAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public PType mapApply(AApplyObjectDesignator node, SMapType map,
			Environment env, NameScope scope, boolean unique,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor)
			throws AnalysisException
	{

		if (node.getArgs().size() != 1)
		{
			TypeCheckerErrors.concern(unique, 3250, "Map application must have one argument", node.getLocation(), node);
			return AstFactory.newAUnknownType(node.getLocation());
		}

		PType argtype = node.getArgs().get(0).apply(rootVisitor, new TypeCheckInfo(af, env, scope));

		if (!TypeComparator.compatible(map.getFrom(), argtype))
		{
			TypeCheckerErrors.concern(unique, 3251, "Map application argument is incompatible type", node.getLocation(), node);
			TypeCheckerErrors.detail2(unique, "Map domain", map.getFrom(), "Argument", argtype);
		}

		return map.getTo();
	}

	public PType seqApply(AApplyObjectDesignator node, SSeqType seq,
			Environment env, NameScope scope, boolean unique,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor)
			throws AnalysisException
	{

		if (node.getArgs().size() != 1)
		{
			TypeCheckerErrors.concern(unique, 3252, "Sequence application must have one argument", node.getLocation(), node);
			return AstFactory.newAUnknownType(node.getLocation());
		}

		PType argtype = node.getArgs().get(0).apply(rootVisitor, new TypeCheckInfo(af, env, scope));

		if (!env.af.createPTypeAssistant().isNumeric(argtype))
		{
			TypeCheckerErrors.concern(unique, 3253, "Sequence argument is not numeric", node.getLocation(), node);
			TypeCheckerErrors.detail(unique, "Type", argtype);
		}

		return seq.getSeqof();
	}

	public PType functionApply(AApplyObjectDesignator node,
			AFunctionType ftype, Environment env, NameScope scope,
			boolean unique, IQuestionAnswer<TypeCheckInfo, PType> rootVisitor)
			throws AnalysisException
	{

		LinkedList<PType> ptypes = ftype.getParameters();

		if (node.getArgs().size() > ptypes.size())
		{
			TypeCheckerErrors.concern(unique, 3254, "Too many arguments", node.getLocation(), node);
			TypeCheckerErrors.detail2(unique, "Args", node.getArgs(), "Params", ptypes);
			return ftype.getResult();
		} else if (node.getArgs().size() < ptypes.size())
		{
			TypeCheckerErrors.concern(unique, 3255, "Too few arguments", node.getLocation(), node);
			TypeCheckerErrors.detail2(unique, "Args", node.getArgs(), "Params", ptypes);
			return ftype.getResult();
		}

		int i = 0;

		for (PExp a : node.getArgs())
		{
			PType at = a.apply(rootVisitor, new TypeCheckInfo(af, env, scope));
			PType pt = ptypes.get(i++);

			if (!TypeComparator.compatible(pt, at))
			{

				// TypeCheckerErrors.concern(unique, 3256, "Inappropriate type for argument " + i
				// +". (Expected: "+pt+" Actual: "+at+")" ,node.getLocation(),node);
				TypeCheckerErrors.concern(unique, 3256, "Inappropriate type for argument "
						+ i, node.getLocation(), node);
				TypeCheckerErrors.detail2(unique, "Expect", pt, "Actual", at);
			}
		}

		return ftype.getResult();
	}

	public PType operationApply(AApplyObjectDesignator node,
			AOperationType optype, Environment env, NameScope scope,
			boolean unique, IQuestionAnswer<TypeCheckInfo, PType> rootVisitor)
			throws AnalysisException
	{
		LinkedList<PType> ptypes = optype.getParameters();

		if (node.getArgs().size() > ptypes.size())
		{
			TypeCheckerErrors.concern(unique, 3257, "Too many arguments", node.getLocation(), node);
			TypeCheckerErrors.detail2(unique, "Args", node.getArgs(), "Params", ptypes);
			return optype.getResult();
		} else if (node.getArgs().size() < ptypes.size())
		{
			TypeCheckerErrors.concern(unique, 3258, "Too few arguments", node.getLocation(), node);
			TypeCheckerErrors.detail2(unique, "Args", node.getArgs(), "Params", ptypes);
			return optype.getResult();
		}

		int i = 0;

		for (PExp a : node.getArgs())
		{
			PType at = a.apply(rootVisitor, new TypeCheckInfo(af, env, scope));
			PType pt = ptypes.get(i++);

			if (!TypeComparator.compatible(pt, at))
			{ // + ". (Expected: "+pt+" Actual: "+at+")"
				TypeCheckerErrors.concern(unique, 3259, "Inappropriate type for argument "
						+ i, node.getLocation(), node);
				TypeCheckerErrors.detail2(unique, "Expect", pt, "Actual", at);
			}
		}

		return optype.getResult();
	}

}
