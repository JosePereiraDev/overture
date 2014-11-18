/*
 * #%~
 * VDM Code Generator
 * %%
 * Copyright (C) 2008 - 2014 Overture
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #~%
 */
package org.overture.codegen.merging;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.overture.codegen.cgast.INode;
import org.overture.codegen.cgast.declarations.ACatchClauseDeclCG;
import org.overture.codegen.cgast.declarations.AClassDeclCG;
import org.overture.codegen.cgast.declarations.ACounterLocalDeclCG;
import org.overture.codegen.cgast.declarations.AFieldDeclCG;
import org.overture.codegen.cgast.declarations.AFormalParamLocalParamCG;
import org.overture.codegen.cgast.declarations.AInterfaceDeclCG;
import org.overture.codegen.cgast.declarations.AMethodDeclCG;
import org.overture.codegen.cgast.declarations.ARecordDeclCG;
import org.overture.codegen.cgast.declarations.AThreadDeclCG;
import org.overture.codegen.cgast.declarations.ATypeDeclCG;
import org.overture.codegen.cgast.declarations.AVarLocalDeclCG;
import org.overture.codegen.cgast.expressions.AAbsUnaryExpCG;
import org.overture.codegen.cgast.expressions.AAddrEqualsBinaryExpCG;
import org.overture.codegen.cgast.expressions.AAddrNotEqualsBinaryExpCG;
import org.overture.codegen.cgast.expressions.AAndBoolBinaryExpCG;
import org.overture.codegen.cgast.expressions.AAnonymousClassExpCG;
import org.overture.codegen.cgast.expressions.AApplyExpCG;
import org.overture.codegen.cgast.expressions.ABoolLiteralExpCG;
import org.overture.codegen.cgast.expressions.ACastUnaryExpCG;
import org.overture.codegen.cgast.expressions.ACharLiteralExpCG;
import org.overture.codegen.cgast.expressions.ACompMapExpCG;
import org.overture.codegen.cgast.expressions.ACompSeqExpCG;
import org.overture.codegen.cgast.expressions.ACompSetExpCG;
import org.overture.codegen.cgast.expressions.ADistConcatUnaryExpCG;
import org.overture.codegen.cgast.expressions.ADistIntersectUnaryExpCG;
import org.overture.codegen.cgast.expressions.ADistMergeUnaryExpCG;
import org.overture.codegen.cgast.expressions.ADistUnionUnaryExpCG;
import org.overture.codegen.cgast.expressions.ADivideNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.ADomainResByBinaryExpCG;
import org.overture.codegen.cgast.expressions.ADomainResToBinaryExpCG;
import org.overture.codegen.cgast.expressions.AElemsUnaryExpCG;
import org.overture.codegen.cgast.expressions.AEnumMapExpCG;
import org.overture.codegen.cgast.expressions.AEnumSeqExpCG;
import org.overture.codegen.cgast.expressions.AEnumSetExpCG;
import org.overture.codegen.cgast.expressions.AEqualsBinaryExpCG;
import org.overture.codegen.cgast.expressions.AExists1QuantifierExpCG;
import org.overture.codegen.cgast.expressions.AExistsQuantifierExpCG;
import org.overture.codegen.cgast.expressions.AExplicitVarExpCG;
import org.overture.codegen.cgast.expressions.AExternalExpCG;
import org.overture.codegen.cgast.expressions.AFieldExpCG;
import org.overture.codegen.cgast.expressions.AFieldNumberExpCG;
import org.overture.codegen.cgast.expressions.AFloorUnaryExpCG;
import org.overture.codegen.cgast.expressions.AForAllQuantifierExpCG;
import org.overture.codegen.cgast.expressions.AGreaterEqualNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.AGreaterNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.AHeadUnaryExpCG;
import org.overture.codegen.cgast.expressions.AHistoryExpCG;
import org.overture.codegen.cgast.expressions.AIdentifierVarExpCG;
import org.overture.codegen.cgast.expressions.AInSetBinaryExpCG;
import org.overture.codegen.cgast.expressions.AIndicesUnaryExpCG;
import org.overture.codegen.cgast.expressions.AInstanceofExpCG;
import org.overture.codegen.cgast.expressions.AIntLiteralExpCG;
import org.overture.codegen.cgast.expressions.AIsolationUnaryExpCG;
import org.overture.codegen.cgast.expressions.ALambdaExpCG;
import org.overture.codegen.cgast.expressions.ALessEqualNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.ALessNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.ALetBeStExpCG;
import org.overture.codegen.cgast.expressions.ALetBeStNoBindingRuntimeErrorExpCG;
import org.overture.codegen.cgast.expressions.ALetDefExpCG;
import org.overture.codegen.cgast.expressions.AMapDomainUnaryExpCG;
import org.overture.codegen.cgast.expressions.AMapInverseUnaryExpCG;
import org.overture.codegen.cgast.expressions.AMapOverrideBinaryExpCG;
import org.overture.codegen.cgast.expressions.AMapRangeUnaryExpCG;
import org.overture.codegen.cgast.expressions.AMapUnionBinaryExpCG;
import org.overture.codegen.cgast.expressions.AMapletExpCG;
import org.overture.codegen.cgast.expressions.AMethodInstantiationExpCG;
import org.overture.codegen.cgast.expressions.AMinusUnaryExpCG;
import org.overture.codegen.cgast.expressions.AMissingMemberRuntimeErrorExpCG;
import org.overture.codegen.cgast.expressions.AMkBasicExpCG;
import org.overture.codegen.cgast.expressions.ANewExpCG;
import org.overture.codegen.cgast.expressions.ANotEqualsBinaryExpCG;
import org.overture.codegen.cgast.expressions.ANotImplementedExpCG;
import org.overture.codegen.cgast.expressions.ANotUnaryExpCG;
import org.overture.codegen.cgast.expressions.ANullExpCG;
import org.overture.codegen.cgast.expressions.AOrBoolBinaryExpCG;
import org.overture.codegen.cgast.expressions.APatternMatchRuntimeErrorExpCG;
import org.overture.codegen.cgast.expressions.APlusNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.APlusUnaryExpCG;
import org.overture.codegen.cgast.expressions.APowerNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.APowerSetUnaryExpCG;
import org.overture.codegen.cgast.expressions.APreCondRuntimeErrorExpCG;
import org.overture.codegen.cgast.expressions.AQuoteLiteralExpCG;
import org.overture.codegen.cgast.expressions.ARangeResByBinaryExpCG;
import org.overture.codegen.cgast.expressions.ARangeResToBinaryExpCG;
import org.overture.codegen.cgast.expressions.ARangeSetExpCG;
import org.overture.codegen.cgast.expressions.ARealLiteralExpCG;
import org.overture.codegen.cgast.expressions.AReverseUnaryExpCG;
import org.overture.codegen.cgast.expressions.ASelfExpCG;
import org.overture.codegen.cgast.expressions.ASeqConcatBinaryExpCG;
import org.overture.codegen.cgast.expressions.ASeqModificationBinaryExpCG;
import org.overture.codegen.cgast.expressions.ASetDifferenceBinaryExpCG;
import org.overture.codegen.cgast.expressions.ASetIntersectBinaryExpCG;
import org.overture.codegen.cgast.expressions.ASetProperSubsetBinaryExpCG;
import org.overture.codegen.cgast.expressions.ASetSubsetBinaryExpCG;
import org.overture.codegen.cgast.expressions.ASetUnionBinaryExpCG;
import org.overture.codegen.cgast.expressions.AStringLiteralExpCG;
import org.overture.codegen.cgast.expressions.ASubSeqExpCG;
import org.overture.codegen.cgast.expressions.ASubtractNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.ATailUnaryExpCG;
import org.overture.codegen.cgast.expressions.ATernaryIfExpCG;
import org.overture.codegen.cgast.expressions.AThreadIdExpCG;
import org.overture.codegen.cgast.expressions.ATimesNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.ATupleCompatibilityExpCG;
import org.overture.codegen.cgast.expressions.ATupleExpCG;
import org.overture.codegen.cgast.expressions.ATupleSizeExpCG;
import org.overture.codegen.cgast.expressions.AUndefinedExpCG;
import org.overture.codegen.cgast.expressions.AXorBoolBinaryExpCG;
import org.overture.codegen.cgast.expressions.*;
import org.overture.codegen.cgast.patterns.AIdentifierPatternCG;
import org.overture.codegen.cgast.statements.AApplyObjectDesignatorCG;
import org.overture.codegen.cgast.statements.AAssignmentStmCG;
import org.overture.codegen.cgast.statements.ABlockStmCG;
import org.overture.codegen.cgast.statements.ABreakStmCG;
import org.overture.codegen.cgast.statements.ACallObjectExpStmCG;
import org.overture.codegen.cgast.statements.ACallObjectStmCG;
import org.overture.codegen.cgast.statements.AContinueStmCG;
import org.overture.codegen.cgast.statements.ADecrementStmCG;
import org.overture.codegen.cgast.statements.AErrorStmCG;
import org.overture.codegen.cgast.statements.AFieldObjectDesignatorCG;
import org.overture.codegen.cgast.statements.AFieldStateDesignatorCG;
import org.overture.codegen.cgast.statements.AForAllStmCG;
import org.overture.codegen.cgast.statements.AForIndexStmCG;
import org.overture.codegen.cgast.statements.AForLoopStmCG;
import org.overture.codegen.cgast.statements.AIdentifierObjectDesignatorCG;
import org.overture.codegen.cgast.statements.AIdentifierStateDesignatorCG;
import org.overture.codegen.cgast.statements.AIfStmCG;
import org.overture.codegen.cgast.statements.AIncrementStmCG;
import org.overture.codegen.cgast.statements.ALetDefStmCG;
import org.overture.codegen.cgast.statements.ALocalAssignmentStmCG;
import org.overture.codegen.cgast.statements.ALocalPatternAssignmentStmCG;
import org.overture.codegen.cgast.statements.AMapSeqStateDesignatorCG;
import org.overture.codegen.cgast.statements.ANewObjectDesignatorCG;
import org.overture.codegen.cgast.statements.ANotImplementedStmCG;
import org.overture.codegen.cgast.statements.APlainCallStmCG;
import org.overture.codegen.cgast.statements.ARaiseErrorStmCG;
import org.overture.codegen.cgast.statements.AReturnStmCG;
import org.overture.codegen.cgast.statements.ASelfObjectDesignatorCG;
import org.overture.codegen.cgast.statements.ASkipStmCG;
import org.overture.codegen.cgast.statements.AStartStmCG;
import org.overture.codegen.cgast.statements.ASuperCallStmCG;
import org.overture.codegen.cgast.statements.AThrowStmCG;
import org.overture.codegen.cgast.statements.ATryStmCG;
import org.overture.codegen.cgast.statements.AWhileStmCG;
import org.overture.codegen.cgast.types.ABoolBasicTypeCG;
import org.overture.codegen.cgast.types.ABoolBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.ACharBasicTypeCG;
import org.overture.codegen.cgast.types.ACharBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.AClassTypeCG;
import org.overture.codegen.cgast.types.AExternalTypeCG;
import org.overture.codegen.cgast.types.AIntBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.AIntNumericBasicTypeCG;
import org.overture.codegen.cgast.types.AInterfaceTypeCG;
import org.overture.codegen.cgast.types.AMapMapTypeCG;
import org.overture.codegen.cgast.types.AMethodTypeCG;
import org.overture.codegen.cgast.types.ANat1BasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.ANat1NumericBasicTypeCG;
import org.overture.codegen.cgast.types.ANatBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.ANatNumericBasicTypeCG;
import org.overture.codegen.cgast.types.AObjectTypeCG;
import org.overture.codegen.cgast.types.ARatBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.ARatNumericBasicTypeCG;
import org.overture.codegen.cgast.types.ARealBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.ARealNumericBasicTypeCG;
import org.overture.codegen.cgast.types.ARecordTypeCG;
import org.overture.codegen.cgast.types.ASeqSeqTypeCG;
import org.overture.codegen.cgast.types.ASetSetTypeCG;
import org.overture.codegen.cgast.types.AStringTypeCG;
import org.overture.codegen.cgast.types.ATemplateTypeCG;
import org.overture.codegen.cgast.types.ATokenBasicTypeCG;
import org.overture.codegen.cgast.types.ATupleTypeCG;
import org.overture.codegen.cgast.types.AUnionTypeCG;
import org.overture.codegen.cgast.types.AUnknownTypeCG;
import org.overture.codegen.cgast.types.AVoidTypeCG;
import org.overture.codegen.utils.GeneralUtils;

public class TemplateManager
{
	private HashMap<Class<? extends INode>, String> nodeTemplateFileNames;

	private TemplateStructure templateStructure;

	public TemplateManager(TemplateStructure templateStructure)
	{
		this.templateStructure = templateStructure;
		initNodeTemplateFileNames();
	}

	private void initNodeTemplateFileNames()
	{
		nodeTemplateFileNames = new HashMap<Class<? extends INode>, String>();

		// Declarations
		nodeTemplateFileNames.put(AClassDeclCG.class, templateStructure.DECL_PATH
				+ "Class");

		nodeTemplateFileNames.put(ARecordDeclCG.class, templateStructure.DECL_PATH
				+ "Record");

		nodeTemplateFileNames.put(AFieldDeclCG.class, templateStructure.DECL_PATH
				+ "Field");

		nodeTemplateFileNames.put(AMethodDeclCG.class, templateStructure.DECL_PATH
				+ "Method");

		nodeTemplateFileNames.put(AVarLocalDeclCG.class, templateStructure.DECL_PATH
				+ "LocalVar");

		nodeTemplateFileNames.put(ACounterLocalDeclCG.class, templateStructure.DECL_PATH
				+ "Counter");
		
		nodeTemplateFileNames.put(AThreadDeclCG.class, templateStructure.DECL_PATH 
				+ "Thread");
		
		nodeTemplateFileNames.put(ATypeDeclCG.class, templateStructure.DECL_PATH
				+ "Type");

		nodeTemplateFileNames.put(ACatchClauseDeclCG.class, templateStructure.DECL_PATH
				+ "CatchClause");
		
		// Local declarations

		nodeTemplateFileNames.put(AFormalParamLocalParamCG.class, templateStructure.LOCAL_DECLS_PATH
				+ "FormalParam");

		// Type
		nodeTemplateFileNames.put(AClassTypeCG.class, templateStructure.TYPE_PATH
				+ "Class");

		nodeTemplateFileNames.put(AExternalTypeCG.class, templateStructure.TYPE_PATH
				+ "External");

		nodeTemplateFileNames.put(ARecordTypeCG.class, templateStructure.TYPE_PATH
				+ "Record");

		nodeTemplateFileNames.put(AObjectTypeCG.class, templateStructure.TYPE_PATH
				+ "Object");

		nodeTemplateFileNames.put(AVoidTypeCG.class, templateStructure.TYPE_PATH
				+ "Void");

		nodeTemplateFileNames.put(AStringTypeCG.class, templateStructure.TYPE_PATH
				+ "String");

		nodeTemplateFileNames.put(ATemplateTypeCG.class, templateStructure.TYPE_PATH
				+ "Template");

		nodeTemplateFileNames.put(ATupleTypeCG.class, templateStructure.TYPE_PATH
				+ "Tuple");

		nodeTemplateFileNames.put(AMethodTypeCG.class, templateStructure.TYPE_PATH
				+ "Method");

		nodeTemplateFileNames.put(AInterfaceTypeCG.class, templateStructure.TYPE_PATH
				+ "Interface");

		nodeTemplateFileNames.put(AUnionTypeCG.class, templateStructure.TYPE_PATH
				+ "Union");

		nodeTemplateFileNames.put(AUnknownTypeCG.class, templateStructure.TYPE_PATH
				+ "Unknown");

		// Basic type wrappers

		nodeTemplateFileNames.put(AIntBasicTypeWrappersTypeCG.class, templateStructure.BASIC_TYPE_WRAPPERS_PATH
				+ "Integer");

		nodeTemplateFileNames.put(ANat1BasicTypeWrappersTypeCG.class, templateStructure.BASIC_TYPE_WRAPPERS_PATH
				+ "Nat1");
		
		nodeTemplateFileNames.put(ANatBasicTypeWrappersTypeCG.class, templateStructure.BASIC_TYPE_WRAPPERS_PATH
				+ "Nat");		

		nodeTemplateFileNames.put(ARatBasicTypeWrappersTypeCG.class, templateStructure.BASIC_TYPE_WRAPPERS_PATH
				+ "Rat");
		
		nodeTemplateFileNames.put(ARealBasicTypeWrappersTypeCG.class, templateStructure.BASIC_TYPE_WRAPPERS_PATH
				+ "Real");

		nodeTemplateFileNames.put(ABoolBasicTypeWrappersTypeCG.class, templateStructure.BASIC_TYPE_WRAPPERS_PATH
				+ "Bool");

		nodeTemplateFileNames.put(ACharBasicTypeWrappersTypeCG.class, templateStructure.BASIC_TYPE_WRAPPERS_PATH
				+ "Char");

		// Collection types

		nodeTemplateFileNames.put(ASetSetTypeCG.class, templateStructure.SET_TYPE_PATH
				+ "Set");

		nodeTemplateFileNames.put(ASeqSeqTypeCG.class, templateStructure.SEQ_TYPE_PATH
				+ "Seq");

		nodeTemplateFileNames.put(AMapMapTypeCG.class, templateStructure.MAP_TYPE_PATH
				+ "Map");

		// Basic types

		nodeTemplateFileNames.put(ABoolBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Bool");

		nodeTemplateFileNames.put(ACharBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Char");

		nodeTemplateFileNames.put(ATokenBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Token");

		// Basic numeric types
		
		nodeTemplateFileNames.put(AIntNumericBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Integer");

		nodeTemplateFileNames.put(ANat1NumericBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Nat1");

		nodeTemplateFileNames.put(ANatNumericBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Nat");

		nodeTemplateFileNames.put(ARatNumericBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Rat");
		
		nodeTemplateFileNames.put(ARealNumericBasicTypeCG.class, templateStructure.BASIC_TYPE_PATH
				+ "Real");

		// Statements
		nodeTemplateFileNames.put(AIfStmCG.class, templateStructure.STM_PATH
				+ "If");

		nodeTemplateFileNames.put(AReturnStmCG.class, templateStructure.STM_PATH
				+ "Return");

		nodeTemplateFileNames.put(ASkipStmCG.class, templateStructure.STM_PATH
				+ "Skip");

		nodeTemplateFileNames.put(ALetDefStmCG.class, templateStructure.STM_PATH
				+ "LetDef");

		nodeTemplateFileNames.put(ALocalAssignmentStmCG.class, templateStructure.STM_PATH
				+ "LocalAssignment");

		nodeTemplateFileNames.put(ALocalPatternAssignmentStmCG.class, templateStructure.STM_PATH
				+ "LocalPatternAssignment");

		nodeTemplateFileNames.put(AAssignmentStmCG.class, templateStructure.STM_PATH
				+ "Assignment");

		nodeTemplateFileNames.put(ABlockStmCG.class, templateStructure.STM_PATH
				+ "Block");

		nodeTemplateFileNames.put(ACallObjectStmCG.class, templateStructure.STM_PATH
				+ "CallObject");

		nodeTemplateFileNames.put(ACallObjectExpStmCG.class, templateStructure.STM_PATH
				+ "CallObjectExp");

		nodeTemplateFileNames.put(APlainCallStmCG.class, templateStructure.STM_PATH
				+ "Call");

		nodeTemplateFileNames.put(ASuperCallStmCG.class, templateStructure.STM_PATH
				+ "SuperCall");
		
		nodeTemplateFileNames.put(ANotImplementedStmCG.class, templateStructure.STM_PATH
				+ "NotImplemented");

		nodeTemplateFileNames.put(AForIndexStmCG.class, templateStructure.STM_PATH
				+ "ForIndex");

		nodeTemplateFileNames.put(AForAllStmCG.class, templateStructure.STM_PATH
				+ "ForAll");

		nodeTemplateFileNames.put(AWhileStmCG.class, templateStructure.STM_PATH
				+ "While");

		nodeTemplateFileNames.put(AThrowStmCG.class, templateStructure.STM_PATH
				+ "Throw");

		nodeTemplateFileNames.put(AForLoopStmCG.class, templateStructure.STM_PATH
				+ "ForLoop");

		nodeTemplateFileNames.put(AIncrementStmCG.class, templateStructure.STM_PATH
				+ "Increment");

		nodeTemplateFileNames.put(ADecrementStmCG.class, templateStructure.STM_PATH
				+ "Decrement");

		nodeTemplateFileNames.put(ARaiseErrorStmCG.class, templateStructure.STM_PATH
				+ "RaiseError");

		nodeTemplateFileNames.put(AErrorStmCG.class, templateStructure.STM_PATH
				+ "Error");

		nodeTemplateFileNames.put(AContinueStmCG.class, templateStructure.STM_PATH
				+ "Continue");

		nodeTemplateFileNames.put(ABreakStmCG.class, templateStructure.STM_PATH
				+ "Break");

		nodeTemplateFileNames.put(ATryStmCG.class, templateStructure.STM_PATH
				+ "Try");
		
		nodeTemplateFileNames.put(AStartStmCG.class, templateStructure.STM_PATH 
				+ "Start");
		
		// Expressions

		nodeTemplateFileNames.put(AApplyExpCG.class, templateStructure.EXP_PATH
				+ "Apply");

		nodeTemplateFileNames.put(AFieldExpCG.class, templateStructure.EXP_PATH
				+ "Field");

		nodeTemplateFileNames.put(ANewExpCG.class, templateStructure.EXP_PATH
				+ "New");

		nodeTemplateFileNames.put(AIdentifierVarExpCG.class, templateStructure.EXP_PATH
				+ "Variable");

		nodeTemplateFileNames.put(AExplicitVarExpCG.class, templateStructure.EXP_PATH
				+ "ExplicitVariable");

		nodeTemplateFileNames.put(ASuperVarExpCG.class, templateStructure.EXP_PATH
				+ "SuperVariable");
		
		nodeTemplateFileNames.put(AInstanceofExpCG.class, templateStructure.EXP_PATH
				+ "InstanceOf");

		nodeTemplateFileNames.put(ASelfExpCG.class, templateStructure.EXP_PATH
				+ "Self");

		nodeTemplateFileNames.put(ANullExpCG.class, templateStructure.EXP_PATH
				+ "Null");

		nodeTemplateFileNames.put(ALetDefExpCG.class, templateStructure.EXP_PATH
				+ "LetDef");

		nodeTemplateFileNames.put(AMethodInstantiationExpCG.class, templateStructure.EXP_PATH
				+ "MethodInstantiation");

		nodeTemplateFileNames.put(ATupleExpCG.class, templateStructure.EXP_PATH
				+ "Tuple");

		nodeTemplateFileNames.put(AFieldNumberExpCG.class, templateStructure.EXP_PATH
				+ "FieldNumber");

		nodeTemplateFileNames.put(ATupleSizeExpCG.class, templateStructure.EXP_PATH
				+ "TupleSize");

		nodeTemplateFileNames.put(ATernaryIfExpCG.class, templateStructure.EXP_PATH
				+ "TernaryIf");

		nodeTemplateFileNames.put(AMapletExpCG.class, templateStructure.EXP_PATH
				+ "Maplet");

		nodeTemplateFileNames.put(ALetBeStExpCG.class, templateStructure.EXP_PATH
				+ "LetBeSt");

		nodeTemplateFileNames.put(AMkBasicExpCG.class, templateStructure.EXP_PATH
				+ "MkBasic");

		nodeTemplateFileNames.put(AExternalExpCG.class, templateStructure.EXP_PATH
				+ "External");

		nodeTemplateFileNames.put(ALambdaExpCG.class, templateStructure.EXP_PATH
				+ "Lambda");

		nodeTemplateFileNames.put(AAnonymousClassExpCG.class, templateStructure.EXP_PATH
				+ "AnonymousClass");

		nodeTemplateFileNames.put(ANotImplementedExpCG.class, templateStructure.EXP_PATH
				+ "NotImplemented");

		nodeTemplateFileNames.put(AUndefinedExpCG.class, templateStructure.EXP_PATH
				+ "Undefined");

		nodeTemplateFileNames.put(ATupleCompatibilityExpCG.class, templateStructure.EXP_PATH
				+ "TupleCompatibility");

		nodeTemplateFileNames.put(AThreadIdExpCG.class, templateStructure.EXP_PATH
				+ "ThreadId");

		nodeTemplateFileNames.put(ASubSeqExpCG.class, templateStructure.EXP_PATH
				+ "SubSeq");
		
		nodeTemplateFileNames.put(AHistoryExpCG.class, templateStructure.EXP_PATH 
				+ "hisCounter");

		// Is expressions
		
		nodeTemplateFileNames.put(ABoolIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Bool");

		nodeTemplateFileNames.put(ANatIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Nat");

		nodeTemplateFileNames.put(ANat1IsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Nat1");
		
		nodeTemplateFileNames.put(AIntIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Int");
		
		nodeTemplateFileNames.put(ARatIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Rat");
		
		nodeTemplateFileNames.put(ARealIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Real");
		
		nodeTemplateFileNames.put(ACharIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Char");
		
		nodeTemplateFileNames.put(ATokenIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Token");
		
		nodeTemplateFileNames.put(ATupleIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "Tuple");
		
		nodeTemplateFileNames.put(AGeneralIsExpCG.class, templateStructure.IS_EXP_PATH
				+ "General");
		
		// Quantifier expressions

		nodeTemplateFileNames.put(AForAllQuantifierExpCG.class, templateStructure.QUANTIFIER_EXP_PATH
				+ "ForAll");

		nodeTemplateFileNames.put(AExistsQuantifierExpCG.class, templateStructure.QUANTIFIER_EXP_PATH
				+ "Exists");

		nodeTemplateFileNames.put(AExists1QuantifierExpCG.class, templateStructure.QUANTIFIER_EXP_PATH
				+ "Exists1");

		// Runtime error expressions

		nodeTemplateFileNames.put(ALetBeStNoBindingRuntimeErrorExpCG.class, templateStructure.RUNTIME_ERROR_EXP_PATH
				+ "LetBeStNoBinding");

		nodeTemplateFileNames.put(APatternMatchRuntimeErrorExpCG.class, templateStructure.RUNTIME_ERROR_EXP_PATH
				+ "PatternMatch");

		nodeTemplateFileNames.put(AMissingMemberRuntimeErrorExpCG.class, templateStructure.RUNTIME_ERROR_EXP_PATH
				+ "MissingMember");

		nodeTemplateFileNames.put(APreCondRuntimeErrorExpCG.class, templateStructure.RUNTIME_ERROR_EXP_PATH
				+ "PreCond");
		
		// Unary expressions

		nodeTemplateFileNames.put(APlusUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Plus");
		nodeTemplateFileNames.put(AMinusUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Minus");

		nodeTemplateFileNames.put(ACastUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Cast");

		nodeTemplateFileNames.put(AIsolationUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Isolation");

		nodeTemplateFileNames.put(ALenUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Len_Card");

		nodeTemplateFileNames.put(ACardUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Len_Card");
		
		nodeTemplateFileNames.put(AElemsUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Elems");

		nodeTemplateFileNames.put(AIndicesUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Indices");

		nodeTemplateFileNames.put(AHeadUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Head");

		nodeTemplateFileNames.put(ATailUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Tail");

		nodeTemplateFileNames.put(AReverseUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Reverse");

		nodeTemplateFileNames.put(AFloorUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Floor");

		nodeTemplateFileNames.put(AAbsUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Abs");

		nodeTemplateFileNames.put(ANotUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "Not");

		nodeTemplateFileNames.put(ADistConcatUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "DistConcat");

		nodeTemplateFileNames.put(ADistUnionUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "DistUnion");

		nodeTemplateFileNames.put(ADistIntersectUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "DistInter");

		nodeTemplateFileNames.put(APowerSetUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "PowerSet");

		nodeTemplateFileNames.put(AMapDomainUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "MapDom");

		nodeTemplateFileNames.put(AMapRangeUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "MapRange");

		nodeTemplateFileNames.put(ADistMergeUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "DistMerge");

		nodeTemplateFileNames.put(AMapInverseUnaryExpCG.class, templateStructure.UNARY_EXP_PATH
				+ "MapInverse");

		// Binary expressions

		nodeTemplateFileNames.put(AAddrEqualsBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "AddrEquals");

		nodeTemplateFileNames.put(AAddrNotEqualsBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "AddrNotEquals");

		nodeTemplateFileNames.put(AEqualsBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "Equals");

		nodeTemplateFileNames.put(ANotEqualsBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "NotEquals");

		nodeTemplateFileNames.put(ASeqConcatBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "SeqConcat");

		nodeTemplateFileNames.put(ASeqModificationBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "SeqModification");

		nodeTemplateFileNames.put(AInSetBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "InSet");

		nodeTemplateFileNames.put(ASetUnionBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "SetUnion");

		nodeTemplateFileNames.put(ASetIntersectBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "SetIntersect");

		nodeTemplateFileNames.put(ASetDifferenceBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "SetDifference");

		nodeTemplateFileNames.put(ASetSubsetBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "SetSubset");

		nodeTemplateFileNames.put(ASetProperSubsetBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "SetProperSubset");

		nodeTemplateFileNames.put(AMapUnionBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "MapUnion");

		nodeTemplateFileNames.put(AMapOverrideBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "MapOverride");

		nodeTemplateFileNames.put(ADomainResToBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "DomResTo");

		nodeTemplateFileNames.put(ADomainResByBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "DomResBy");

		nodeTemplateFileNames.put(ARangeResToBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "RngResTo");

		nodeTemplateFileNames.put(ARangeResByBinaryExpCG.class, templateStructure.BINARY_EXP_PATH
				+ "RngResBy");

		// Numeric binary expressions

		nodeTemplateFileNames.put(ATimesNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "Mul");
		nodeTemplateFileNames.put(APlusNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "Plus");
		nodeTemplateFileNames.put(ASubtractNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "Minus");

		nodeTemplateFileNames.put(ADivideNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "Divide");

		nodeTemplateFileNames.put(AGreaterEqualNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "GreaterEqual");

		nodeTemplateFileNames.put(AGreaterNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "Greater");

		nodeTemplateFileNames.put(ALessEqualNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "LessEqual");

		nodeTemplateFileNames.put(ALessNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "Less");

		nodeTemplateFileNames.put(APowerNumericBinaryExpCG.class, templateStructure.NUMERIC_BINARY_EXP_PATH
				+ "Power");

		// Connective binary expressions

		nodeTemplateFileNames.put(AOrBoolBinaryExpCG.class, templateStructure.BOOL_BINARY_EXP_PATH
				+ "Or");

		nodeTemplateFileNames.put(AAndBoolBinaryExpCG.class, templateStructure.BOOL_BINARY_EXP_PATH
				+ "And");

		nodeTemplateFileNames.put(AXorBoolBinaryExpCG.class, templateStructure.BOOL_BINARY_EXP_PATH
				+ "Xor");

		// Literal expressions

		nodeTemplateFileNames.put(AIntLiteralExpCG.class, templateStructure.EXP_PATH
				+ "IntLiteral");
		nodeTemplateFileNames.put(ARealLiteralExpCG.class, templateStructure.EXP_PATH
				+ "RealLiteral");

		nodeTemplateFileNames.put(ABoolLiteralExpCG.class, templateStructure.EXP_PATH
				+ "BoolLiteral");

		nodeTemplateFileNames.put(ACharLiteralExpCG.class, templateStructure.EXP_PATH
				+ "CharLiteral");

		nodeTemplateFileNames.put(AStringLiteralExpCG.class, templateStructure.EXP_PATH
				+ "StringLiteral");

		nodeTemplateFileNames.put(AQuoteLiteralExpCG.class, templateStructure.EXP_PATH
				+ "QuoteLiteral");

		// Seq expressions
		nodeTemplateFileNames.put(AEnumSeqExpCG.class, templateStructure.SEQ_EXP_PATH
				+ "Enum");

		nodeTemplateFileNames.put(ACompSeqExpCG.class, templateStructure.SEQ_EXP_PATH
				+ "Comp");

		// Set expressions
		nodeTemplateFileNames.put(AEnumSetExpCG.class, templateStructure.SET_EXP_PATH
				+ "Enum");

		nodeTemplateFileNames.put(ACompSetExpCG.class, templateStructure.SET_EXP_PATH
				+ "Comp");

		nodeTemplateFileNames.put(ARangeSetExpCG.class, templateStructure.SET_EXP_PATH
				+ "Range");

		// Map expressions

		nodeTemplateFileNames.put(AEnumMapExpCG.class, templateStructure.MAP_EXP_PATH
				+ "Enum");

		nodeTemplateFileNames.put(ACompMapExpCG.class, templateStructure.MAP_EXP_PATH
				+ "Comp");

		// State designators
		nodeTemplateFileNames.put(AFieldStateDesignatorCG.class, templateStructure.STATE_DESIGNATOR_PATH
				+ "Field");
		nodeTemplateFileNames.put(AIdentifierStateDesignatorCG.class, templateStructure.STATE_DESIGNATOR_PATH
				+ "Identifier");
		nodeTemplateFileNames.put(AMapSeqStateDesignatorCG.class, templateStructure.STATE_DESIGNATOR_PATH
				+ "MapSeq");

		// Object designators
		nodeTemplateFileNames.put(AApplyObjectDesignatorCG.class, templateStructure.OBJECT_DESIGNATOR_PATH
				+ "Apply");
		nodeTemplateFileNames.put(AFieldObjectDesignatorCG.class, templateStructure.OBJECT_DESIGNATOR_PATH
				+ "Field");
		nodeTemplateFileNames.put(AIdentifierObjectDesignatorCG.class, templateStructure.OBJECT_DESIGNATOR_PATH
				+ "Identifier");
		nodeTemplateFileNames.put(ANewObjectDesignatorCG.class, templateStructure.OBJECT_DESIGNATOR_PATH
				+ "New");
		nodeTemplateFileNames.put(ASelfObjectDesignatorCG.class, templateStructure.OBJECT_DESIGNATOR_PATH
				+ "Self");

		// Patterns
		nodeTemplateFileNames.put(AIdentifierPatternCG.class, templateStructure.PATTERN_PATH
				+ "Identifier");

		// Interface
		nodeTemplateFileNames.put(AInterfaceDeclCG.class, templateStructure.DECL_PATH
				+ "Interface");
	}

	public Template getTemplate(Class<? extends INode> nodeClass)
	{
		try
		{
			StringBuffer buffer = GeneralUtils.readFromFile(getTemplateFileRelativePath(nodeClass));

			if (buffer == null)
			{
				return null;
			}

			return constructTemplate(buffer);

		} catch (IOException e)
		{
			return null;
		}
	}

	private Template constructTemplate(StringBuffer buffer)
	{
		Template template = new Template();
		RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
		StringReader reader = new StringReader(buffer.toString());

		try
		{
			SimpleNode simpleNode = runtimeServices.parse(reader, "Template name");
			template.setRuntimeServices(runtimeServices);
			template.setData(simpleNode);
			template.initDocument();

			return template;

		} catch (ParseException e)
		{
			return null;
		}
	}

	private String getTemplateFileRelativePath(Class<? extends INode> nodeClass)
	{
		return nodeTemplateFileNames.get(nodeClass)
				+ TemplateStructure.TEMPLATE_FILE_EXTENSION;
	}
}
