package org.overture.typechecker.visitor;

import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.modules.AAllImport;
import org.overture.ast.modules.AFunctionValueImport;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.modules.AOperationValueImport;
import org.overture.ast.modules.ATypeImport;
import org.overture.ast.modules.SValueImport;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;
import org.overture.ast.types.SInvariantType;
import org.overture.typechecker.FlatCheckedEnvironment;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.definition.PDefinitionAssistantTC;
import org.overture.typechecker.assistant.definition.PDefinitionListAssistantTC;

public class TypeCheckerImportsVisitor extends AbstractTypeCheckVisitor
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6883311293059829368L;

	public TypeCheckerImportsVisitor(
			QuestionAnswerAdaptor<TypeCheckInfo, PType> typeCheckVisitor)
	{
		super(null,typeCheckVisitor);// FIXME
	}

	@Override
	public PType caseAAllImport(AAllImport node, TypeCheckInfo question)
	{
		return null; // Implicitly OK.
	}

	@Override
	public PType caseATypeImport(ATypeImport node, TypeCheckInfo question)
	{
		if (node.getDef() != null && node.getFrom() != null)
		{
			PDefinition def = node.getDef();
			ILexNameToken name = node.getName();
			AModuleModules from = node.getFrom();
			def.setType((SInvariantType) question.assistantFactory.createPTypeAssistant().typeResolve(question.assistantFactory.createPDefinitionAssistant().getType(def), null, rootVisitor, question));
			PDefinition expdef = PDefinitionListAssistantTC.findType(from.getExportdefs(), name, null);

			if (expdef != null)
			{
				PType exptype = question.assistantFactory.createPTypeAssistant().typeResolve(expdef.getType(), null, rootVisitor, question);

				if (!TypeComparator.compatible(def.getType(), exptype))
				{
					TypeCheckerErrors.report(3192, "Type import of " + name
							+ " does not match export from " + from.getName(), node.getLocation(), node);
					TypeCheckerErrors.detail2("Import", def.getType().toString() // TODO: .toDetailedString()
					, "Export", exptype.toString()); // TODO:
														// .toDetailedString());
				}
			}
		}
		return null;
	}

	@Override
	public PType defaultSValueImport(SValueImport node, TypeCheckInfo question)
	{
		PType type = node.getImportType();
		AModuleModules from = node.getFrom();
		ILexNameToken name = node.getName();

		if (type != null && from != null)
		{
			type = question.assistantFactory.createPTypeAssistant().typeResolve(type, null, rootVisitor, question);
			PDefinition expdef = PDefinitionListAssistantTC.findName(from.getExportdefs(), name, NameScope.NAMES);

			if (expdef != null)
			{
				PType exptype = question.assistantFactory.createPTypeAssistant().typeResolve(expdef.getType(), null, rootVisitor, question);

				if (!TypeComparator.compatible(type, exptype))
				{
					TypeCheckerErrors.report(3194, "Type of value import "
							+ name + " does not match export from "
							+ from.getName(), node.getLocation(), node);
					TypeCheckerErrors.detail2("Import", type.toString(), // TODO:
																			// .toDetailedString(),
							"Export", exptype.toString()); // TODO:
															// .toDetailedString());
				}
			}
		}
		return null;
	}

	@Override
	public PType caseAFunctionValueImport(AFunctionValueImport node,
			TypeCheckInfo question)
	{
		// TODO: This might need to be made in another way
		if (node.getTypeParams().size() == 0)
		{
			defaultSValueImport(node, question);
		} else
		{
			List<PDefinition> defs = new Vector<PDefinition>();

			for (ILexNameToken pname : node.getTypeParams())
			{
				ILexNameToken pnameClone = pname.clone();
				PDefinition p = AstFactory.newALocalDefinition(pname.getLocation(), pnameClone, NameScope.NAMES, AstFactory.newAParameterType(pnameClone));

				PDefinitionAssistantTC.markUsed(p);
				defs.add(p);
			}

			FlatCheckedEnvironment params = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, NameScope.NAMES);

			defaultSValueImport(node, new TypeCheckInfo(question.assistantFactory, params, question.scope, question.qualifiers));
		}
		return null;
	}

	@Override
	public PType caseAOperationValueImport(AOperationValueImport node,
			TypeCheckInfo question)
	{
		return defaultSValueImport(node, question);
	}
}
