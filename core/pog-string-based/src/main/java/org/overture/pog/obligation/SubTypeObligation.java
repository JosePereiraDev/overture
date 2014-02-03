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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.CORBA.CTX_RESTRICT_SCOPE;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.expressions.ABooleanConstExp;
import org.overture.ast.expressions.ACharLiteralExp;
import org.overture.ast.expressions.AMapEnumMapExp;
import org.overture.ast.expressions.AMapletExp;
import org.overture.ast.expressions.AMkTypeExp;
import org.overture.ast.expressions.ANotYetSpecifiedExp;
import org.overture.ast.expressions.ASeqEnumSeqExp;
import org.overture.ast.expressions.ASetEnumSetExp;
import org.overture.ast.expressions.ASetRangeSetExp;
import org.overture.ast.expressions.ASubclassResponsibilityExp;
import org.overture.ast.expressions.ASubseqExp;
import org.overture.ast.expressions.ATupleExp;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.ATuplePattern;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.ACharBasicType;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ANatNumericBasicType;
import org.overture.ast.types.ANatOneNumericBasicType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.ASeq1SeqType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SBasicType;
import org.overture.ast.types.SInvariantType;
import org.overture.ast.types.SMapType;
import org.overture.ast.types.SNumericBasicType;
import org.overture.ast.types.SSeqType;
import org.overture.ast.util.PTypeSet;
import org.overture.pog.assistant.IPogAssistantFactory;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.pattern.PPatternAssistantTC;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;
import org.overture.typechecker.assistant.type.SNumericBasicTypeAssistantTC;

public class SubTypeObligation extends ProofObligation
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1108478780469068741L;
	//private IPogAssistantFactory assistantFactory;

	public SubTypeObligation(PExp exp, PType etype, PType atype, POContextStack ctxt,
			ITypeCheckerAssistantFactory assistantFactory)
	{
		super(exp.getLocation(), POType.SUB_TYPE, ctxt);
		value = ctxt.getObligation(oneType(false, exp, etype, atype, assistantFactory));
		return;
	}

	public SubTypeObligation(AExplicitFunctionDefinition func, PType etype,
			PType atype, POContextStack ctxt, ITypeCheckerAssistantFactory assistantFactory)
	{
		super(func.getLocation(), POType.SUB_TYPE, ctxt);

		PExp body = null;

		if (func.getBody() instanceof ANotYetSpecifiedExp
				|| func.getBody() instanceof ASubclassResponsibilityExp)
		{
			// We have to say "f(a)" because we have no body

			PExp root = AstFactory.newAVariableExp(func.getName());
			List<PExp> args = new ArrayList<PExp>();

			for (PPattern p : func.getParamPatternList().get(0))
			{
				args.add(assistantFactory.createPPatternAssistant().getMatchingExpression(p));
			}
			body = AstFactory.newAApplyExp(root, args);

		} else
		{
			body = func.getBody();
		}

		value = ctxt.getObligation(oneType(false, body, etype, atype, assistantFactory));
	}

	public SubTypeObligation(AImplicitFunctionDefinition func, PType etype,
			PType atype, POContextStack ctxt, ITypeCheckerAssistantFactory assistantFactory)
	{
		super(func.getLocation(), POType.SUB_TYPE, ctxt);

		PExp body = null;

		if (func.getBody() instanceof ANotYetSpecifiedExp
				|| func.getBody() instanceof ASubclassResponsibilityExp)
		{
			// We have to say "f(a)" because we have no body

			PExp root = AstFactory.newAVariableExp(func.getName());
			List<PExp> args = new ArrayList<PExp>();

			for (APatternListTypePair pltp : func.getParamPatterns())
			{
				for (PPattern p : pltp.getPatterns())
				{
					args.add(assistantFactory.createPPatternAssistant().getMatchingExpression(p));
				}
			}

			body = AstFactory.newAApplyExp(root, args);
		} else
		{
			body = func.getBody();
		}

		value = ctxt.getObligation(oneType(false, body, etype, atype, assistantFactory));
	}

	public SubTypeObligation(AExplicitOperationDefinition def,
			PType actualResult, POContextStack ctxt, ITypeCheckerAssistantFactory assistantFactory)
	{
		super(def.getLocation(), POType.SUB_TYPE, ctxt);

		AVariableExp result = AstFactory.newAVariableExp(new LexNameToken(def.getName().getModule(), "RESULT", def.getLocation()));

		value = ctxt.getObligation(oneType(false, result, ((AOperationType) def.getType()).getResult(), actualResult, assistantFactory));
	}

	public SubTypeObligation(AImplicitOperationDefinition def,
			PType actualResult, POContextStack ctxt, ITypeCheckerAssistantFactory assistantFactory)
	{
		super(def.getLocation(), POType.SUB_TYPE, ctxt);
		PExp result = null;

		if (def.getResult().getPattern() instanceof AIdentifierPattern)
		{
			AIdentifierPattern ip = (AIdentifierPattern) def.getResult().getPattern();
			result = AstFactory.newAVariableExp(ip.getName());
		} else
		{
			ATuplePattern tp = (ATuplePattern) def.getResult().getPattern();
			List<PExp> args = new ArrayList<PExp>();

			for (PPattern p : tp.getPlist())
			{
				AIdentifierPattern ip = (AIdentifierPattern) p;
				args.add(AstFactory.newAVariableExp(ip.getName()));
			}

			result = AstFactory.newATupleExp(def.getLocation(), args);
		}

		value = ctxt.getObligation(oneType(false, result, ((AOperationType) def.getType()).getResult(), actualResult, assistantFactory));
	}

	private String oneType(boolean rec, PExp exp, PType etype, PType atype, ITypeCheckerAssistantFactory ctxt)
	{		
		if (atype != null && rec)
		{
			if (TypeComparator.isSubType(atype, etype, ctxt))
			{
				return ""; // A sub comparison is OK without checks
			}
		}

		StringBuilder sb = new StringBuilder();
		String prefix = "";

		etype = PTypeAssistantTC.deBracket(etype);

		if (etype instanceof AUnionType)
		{
			AUnionType ut = (AUnionType) etype;
			PTypeSet possibles = new PTypeSet();

			for (PType pos : ut.getTypes())
			{
				if (atype == null || TypeComparator.compatible(pos, atype))
				{
					possibles.add(pos);
				}
			}

			prefix = "";

			for (PType poss : possibles)
			{
				String s = oneType(true, exp, poss, null, ctxt);

				sb.append(prefix);
				sb.append("(");
				addIs(sb, exp, poss);

				if (s.length() > 0 && !s.startsWith("is_(")
						&& !s.startsWith("(is_("))
				{
					sb.append(" and ");
					sb.append(s);
				}

				sb.append(")");
				prefix = " or\n";
			}
		} else if (etype instanceof SInvariantType)
		{
			SInvariantType et = (SInvariantType) etype;
			prefix = "";

			if (et.getInvDef() != null)
			{
				sb.append(et.getInvDef().getName().getName());
				sb.append("(");

				// This needs to be put back if/when we change the inv_R
				// signature to take
				// the record fields as arguments, rather than one R value.
				// if (exp instanceof MkTypeExpression)
				// {
				// MkTypeExpression mk = (MkTypeExpression)exp;
				// sb.append(Utils.listToString(mk.args));
				// }
				// else
				{
					sb.append(exp);
				}

				sb.append(")");
				prefix = " and ";
			}

			if (etype instanceof ANamedInvariantType)
			{
				ANamedInvariantType nt = (ANamedInvariantType) etype;

				if (atype instanceof ANamedInvariantType)
				{
					atype = ((ANamedInvariantType) atype).getType();
				} else
				{
					atype = null;
				}

				String s = oneType(true, exp, nt.getType(), atype, ctxt);

				if (s.length() > 0)
				{
					sb.append(prefix);
					sb.append("(");
					sb.append(s);
					sb.append(")");
				}
			} else if (etype instanceof ARecordInvariantType)
			{
				if (exp instanceof AMkTypeExp)
				{
					ARecordInvariantType rt = (ARecordInvariantType) etype;
					AMkTypeExp mk = (AMkTypeExp) exp;

					if (rt.getFields().size() == mk.getArgs().size())
					{
						Iterator<AFieldField> fit = rt.getFields().iterator();
						Iterator<PType> ait = mk.getArgTypes().iterator();

						for (PExp e : mk.getArgs())
						{
							String s = oneType(true, e, fit.next().getType(), ait.next(), ctxt);

							if (s.length() > 0)
							{
								sb.append(prefix);
								sb.append("(");
								sb.append(s);
								sb.append(")");
								prefix = "\nand ";
							}
						}
					}
				} else
				{
					sb.append(prefix);
					addIs(sb, exp, etype);
				}
			} else
			{
				sb.append(prefix);
				addIs(sb, exp, etype);
			}
		} else if (etype instanceof SSeqType)
		{
			prefix = "";

			if (etype instanceof ASeq1SeqType)
			{
				sb.append(exp);
				sb.append(" <> []");
				prefix = " and ";
			}

			if (exp instanceof ASeqEnumSeqExp)
			{
				SSeqType stype = (SSeqType) etype;
				ASeqEnumSeqExp seq = (ASeqEnumSeqExp) exp;
				Iterator<PType> it = seq.getTypes().iterator();

				for (PExp m : seq.getMembers())
				{
					String s = oneType(true, m, stype.getSeqof(), it.next(), ctxt);

					if (s.length() > 0)
					{
						sb.append(prefix);
						sb.append("(");
						sb.append(s);
						sb.append(")");
						prefix = "\nand ";
					}
				}
			} else if (exp instanceof ASubseqExp)
			{
				ASubseqExp subseq = (ASubseqExp) exp;
				PType itype = AstFactory.newANatOneNumericBasicType(exp.getLocation());
				String s = oneType(true, subseq.getFrom(), itype, subseq.getFtype(), ctxt);

				if (s.length() > 0)
				{
					sb.append("(");
					sb.append(s);
					sb.append(")");
					sb.append(" and ");
				}

				s = oneType(true, subseq.getTo(), itype, subseq.getTtype(), ctxt);

				if (s.length() > 0)
				{
					sb.append("(");
					sb.append(s);
					sb.append(")");
					sb.append(" and ");
				}

				sb.append(subseq.getTo());
				sb.append(" <= len ");
				sb.append(subseq.getSeq());

				sb.append(" and ");
				addIs(sb, exp, etype); // Like set range does
			} else
			{
				sb = new StringBuilder(); // remove any "x <> []"
				addIs(sb, exp, etype);
			}
		} else if (etype instanceof SMapType)
		{
			if (exp instanceof AMapEnumMapExp)
			{
				SMapType mtype = (SMapType) etype;
				AMapEnumMapExp seq = (AMapEnumMapExp) exp;
				Iterator<PType> dit = seq.getDomTypes().iterator();
				Iterator<PType> rit = seq.getRngTypes().iterator();
				prefix = "";

				for (AMapletExp m : seq.getMembers())
				{
					String s = oneType(true, m.getLeft(), mtype.getFrom(), dit.next(), ctxt);

					if (s.length() > 0)
					{
						sb.append(prefix);
						sb.append("(");
						sb.append(s);
						sb.append(")");
						prefix = "\nand ";
					}

					s = oneType(true, m.getRight(), mtype.getTo(), rit.next(), ctxt);

					if (s.length() > 0)
					{
						sb.append(prefix);
						sb.append("(");
						sb.append(s);
						sb.append(")");
						prefix = "\nand ";
					}
				}
			} else
			{
				addIs(sb, exp, etype);
			}
		} else if (etype instanceof ASetType)
		{
			if (exp instanceof ASetEnumSetExp)
			{
				ASetType stype = (ASetType) etype;
				ASetEnumSetExp set = (ASetEnumSetExp) exp;
				Iterator<PType> it = set.getTypes().iterator();
				prefix = "";

				for (PExp m : set.getMembers())
				{
					String s = oneType(true, m, stype.getSetof(), it.next(), ctxt);

					if (s.length() > 0)
					{
						sb.append(prefix);
						sb.append("(");
						sb.append(s);
						sb.append(")");
						prefix = "\nand ";
					}
				}

				sb.append("\nand ");
			} else if (exp instanceof ASetRangeSetExp)
			{
				ASetType stype = (ASetType) etype;
				ASetRangeSetExp range = (ASetRangeSetExp) exp;
				PType itype = AstFactory.newAIntNumericBasicType(exp.getLocation());
				prefix = "";

				String s = oneType(true, range.getFirst(), itype, range.getFtype(), ctxt);

				if (s.length() > 0)
				{
					sb.append(prefix);
					sb.append("(");
					sb.append(s);
					sb.append(")");
					prefix = "\nand ";
				}

				s = oneType(true, range.getFirst(), stype.getSetof(), range.getFtype(), ctxt);

				if (s.length() > 0)
				{
					sb.append(prefix);
					sb.append("(");
					sb.append(s);
					sb.append(")");
					prefix = "\nand ";
				}

				s = oneType(true, range.getLast(), itype, range.getLtype(), ctxt);

				if (s.length() > 0)
				{
					sb.append(prefix);
					sb.append("(");
					sb.append(s);
					sb.append(")");
					prefix = "\nand ";
				}

				s = oneType(true, range.getLast(), stype.getSetof(), range.getLtype(), ctxt);

				if (s.length() > 0)
				{
					sb.append(prefix);
					sb.append("(");
					sb.append(s);
					sb.append(")");
					prefix = "\nand ";
				}
			}

			sb.append(prefix);
			addIs(sb, exp, etype);
		} else if (etype instanceof AProductType)
		{
			if (exp instanceof ATupleExp)
			{
				AProductType pt = (AProductType) etype;
				ATupleExp te = (ATupleExp) exp;
				Iterator<PType> eit = pt.getTypes().iterator();
				Iterator<PType> ait = te.getTypes().iterator();
				prefix = "";

				for (PExp e : te.getArgs())
				{
					String s = oneType(true, e, eit.next(), ait.next(), ctxt);

					if (s.length() > 0)
					{
						sb.append(prefix);
						sb.append("(");
						sb.append(s);
						sb.append(")");
						prefix = " and ";
					}
				}
			} else
			{
				addIs(sb, exp, etype);
			}
		} else if (etype instanceof SBasicType)
		{
			if (etype instanceof SNumericBasicType)
			{
				SNumericBasicType nt = (SNumericBasicType) etype;

				if (atype instanceof SNumericBasicType)
				{
					SNumericBasicType ant = (SNumericBasicType) atype;

					if (SNumericBasicTypeAssistantTC.getWeight(ant) > SNumericBasicTypeAssistantTC.getWeight(nt))
					{

						boolean isWhole = SNumericBasicTypeAssistantTC.getWeight(ant) < 3;

						if (isWhole && nt instanceof ANatOneNumericBasicType)
						{
							sb.append(exp);
							sb.append(" > 0");
						} else if (isWhole
								&& nt instanceof ANatNumericBasicType)
						{
							sb.append(exp);
							sb.append(" >= 0");
						} else
						{
							sb.append("is_");
							sb.append(nt);
							sb.append("(");
							sb.append(exp);
							sb.append(")");
						}
					}
				} else
				{
					sb.append("is_");
					sb.append(nt);
					sb.append("(");
					sb.append(exp);
					sb.append(")");
				}
			} else if (etype instanceof ABooleanBasicType)
			{
				if (!(exp instanceof ABooleanConstExp))
				{
					addIs(sb, exp, etype);
				}
			} else if (etype instanceof ACharBasicType)
			{
				if (!(exp instanceof ACharLiteralExp))
				{
					addIs(sb, exp, etype);
				}
			} else
			{
				addIs(sb, exp, etype);
			}
		} else
		{
			addIs(sb, exp, etype);
		}

		return sb.toString();
	}

	private void addIs(StringBuilder sb, PExp exp, PType type)
	{
		sb.append("is_(");
		sb.append(exp);
		sb.append(", ");
		sb.append(type);
		sb.append(")");
	}
}
