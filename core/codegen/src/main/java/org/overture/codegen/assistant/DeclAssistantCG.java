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
package org.overture.codegen.assistant;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AClassClassDefinition;
import org.overture.ast.definitions.AEqualsDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.definitions.SFunctionDefinition;
import org.overture.ast.definitions.SOperationDefinition;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.node.INode;
import org.overture.codegen.cgast.SDeclCG;
import org.overture.codegen.cgast.SExpCG;
import org.overture.codegen.cgast.SPatternCG;
import org.overture.codegen.cgast.STypeCG;
import org.overture.codegen.cgast.declarations.AClassDeclCG;
import org.overture.codegen.cgast.declarations.AFieldDeclCG;
import org.overture.codegen.cgast.declarations.AMethodDeclCG;
import org.overture.codegen.cgast.declarations.ARecordDeclCG;
import org.overture.codegen.cgast.declarations.ATypeDeclCG;
import org.overture.codegen.cgast.declarations.AVarLocalDeclCG;
import org.overture.codegen.cgast.expressions.ANullExpCG;
import org.overture.codegen.cgast.name.ATypeNameCG;
import org.overture.codegen.cgast.types.ABoolBasicTypeCG;
import org.overture.codegen.cgast.types.ACharBasicTypeCG;
import org.overture.codegen.cgast.types.AIntNumericBasicTypeCG;
import org.overture.codegen.cgast.types.ANat1NumericBasicTypeCG;
import org.overture.codegen.cgast.types.ANatNumericBasicTypeCG;
import org.overture.codegen.cgast.types.ARealNumericBasicTypeCG;
import org.overture.codegen.cgast.types.ARecordTypeCG;
import org.overture.codegen.cgast.types.AStringTypeCG;
import org.overture.codegen.ir.IRConstants;
import org.overture.codegen.ir.IRInfo;
import org.overture.codegen.ir.SourceNode;
import org.overture.codegen.utils.LexNameTokenWrapper;

public class DeclAssistantCG extends AssistantBase
{
	public DeclAssistantCG(AssistantManager assistantManager)
	{
		super(assistantManager);
	}

	public boolean classIsLibrary(SClassDefinition classDef)
	{
		String className = classDef.getName().getName();

		return isLibraryName(className);
	}

	public boolean isLibraryName(String className)
	{
		for (int i = 0; i < IRConstants.CLASS_NAMES_USED_IN_VDM.length; i++)
		{
			if (IRConstants.CLASS_NAMES_USED_IN_VDM[i].equals(className))
			{
				return true;
			}
		}

		return false;
	}

	public <T extends SDeclCG> List<T> getAllDecls(AClassDeclCG classDecl,
			List<AClassDeclCG> classes, DeclStrategy<T> strategy)
	{
		List<T> allDecls = new LinkedList<T>();

		allDecls.addAll(strategy.getDecls(classDecl));

		String superName = classDecl.getSuperName();

		while (superName != null)
		{
			AClassDeclCG superClassDecl = findClass(classes, superName);

			for (T superDecl : strategy.getDecls(superClassDecl))
			{
				if (isInherited(strategy.getAccess(superDecl)))
				{
					allDecls.add(superDecl);
				}
			}

			superName = superClassDecl.getSuperName();
		}

		return allDecls;
	}

	public List<AMethodDeclCG> getAllMethods(AClassDeclCG classDecl,
			List<AClassDeclCG> classes)
	{
		DeclStrategy<AMethodDeclCG> methodDeclStrategy = new DeclStrategy<AMethodDeclCG>()
		{
			@Override
			public String getAccess(AMethodDeclCG decl)
			{
				return decl.getAccess();
			}

			@Override
			public List<AMethodDeclCG> getDecls(AClassDeclCG classDecl)
			{
				return classDecl.getMethods();
			}
		};

		return getAllDecls(classDecl, classes, methodDeclStrategy);
	}

	public List<AFieldDeclCG> getAllFields(AClassDeclCG classDecl,
			List<AClassDeclCG> classes)
	{
		DeclStrategy<AFieldDeclCG> fieldDeclStrategy = new DeclStrategy<AFieldDeclCG>()
		{
			@Override
			public String getAccess(AFieldDeclCG decl)
			{
				return decl.getAccess();
			}

			@Override
			public List<AFieldDeclCG> getDecls(AClassDeclCG classDecl)
			{
				return classDecl.getFields();
			}
		};

		return getAllDecls(classDecl, classes, fieldDeclStrategy);
	}

	public boolean isInherited(String access)
	{
		return access.equals(IRConstants.PROTECTED)
				|| access.equals(IRConstants.PUBLIC);
	}

	public void setLocalDefs(List<PDefinition> localDefs,
			List<AVarLocalDeclCG> localDecls, IRInfo question)
			throws AnalysisException
	{
		for (PDefinition def : localDefs)
		{
			if (def instanceof AValueDefinition)
			{
				localDecls.add(consLocalVarDecl((AValueDefinition) def, question));
			} else if (def instanceof AEqualsDefinition)
			{
				localDecls.add(consLocalVarDecl((AEqualsDefinition) def, question));
			}
		}
	}

	public AClassDeclCG findClass(List<AClassDeclCG> classes, String moduleName)
	{
		for (AClassDeclCG classDecl : classes)
		{
			if (classDecl.getName().equals(moduleName))
			{
				return classDecl;
			}
		}

		return null;
	}

	// This method assumes that the record is defined in definingClass and not a super class
	public ARecordDeclCG findRecord(AClassDeclCG definingClass,
			String recordName)
	{
		for (ATypeDeclCG typeDecl : definingClass.getTypeDecls())
		{
			SDeclCG decl = typeDecl.getDecl();
			
			if(!(decl instanceof ARecordDeclCG))
			{
				continue;
			}
			
			ARecordDeclCG recordDecl = (ARecordDeclCG) decl;
			
			if (recordDecl.getName().equals(recordName))
			{
				return recordDecl;
			}
		}

		return null;
	}
	
	// This method assumes that the record is defined in definingClass and not a super class
	public List<ARecordDeclCG> getRecords(AClassDeclCG definingClass)
	{
		List<ARecordDeclCG> records = new LinkedList<ARecordDeclCG>();
		
		for (ATypeDeclCG typeDecl : definingClass.getTypeDecls())
		{
			SDeclCG decl = typeDecl.getDecl();
			
			if(!(decl instanceof ARecordDeclCG))
			{
				continue;
			}
			
			ARecordDeclCG recordDecl = (ARecordDeclCG) decl;
			
			records.add(recordDecl);
		}
		
		return records;
	}

	public ARecordDeclCG findRecord(List<AClassDeclCG> classes,
			ARecordTypeCG recordType)
	{
		AClassDeclCG definingClass = findClass(classes, recordType.getName().getDefiningClass());
		ARecordDeclCG record = findRecord(definingClass, recordType.getName().getName());

		return record;
	}

	private AVarLocalDeclCG consLocalVarDecl(AValueDefinition valueDef,
			IRInfo question) throws AnalysisException
	{
		STypeCG type = valueDef.getType().apply(question.getTypeVisitor(), question);
		SPatternCG pattern = valueDef.getPattern().apply(question.getPatternVisitor(), question);
		SExpCG exp = valueDef.getExpression().apply(question.getExpVisitor(), question);

		return consLocalVarDecl(valueDef, type, pattern, exp);

	}

	private AVarLocalDeclCG consLocalVarDecl(AEqualsDefinition equalsDef,
			IRInfo question) throws AnalysisException
	{
		STypeCG type = equalsDef.getExpType().apply(question.getTypeVisitor(), question);
		SPatternCG pattern = equalsDef.getPattern().apply(question.getPatternVisitor(), question);
		SExpCG exp = equalsDef.getTest().apply(question.getExpVisitor(), question);

		return consLocalVarDecl(equalsDef, type, pattern, exp);

	}

	private AVarLocalDeclCG consLocalVarDecl(INode node, STypeCG type,
			SPatternCG pattern, SExpCG exp)
	{
		AVarLocalDeclCG localVarDecl = new AVarLocalDeclCG();
		localVarDecl.setSourceNode(new SourceNode(node));
		localVarDecl.setType(type);
		localVarDecl.setPattern(pattern);
		localVarDecl.setExp(exp);

		return localVarDecl;
	}

	public AFieldDeclCG constructField(String access, String name,
			boolean isStatic, boolean isFinal, STypeCG type, SExpCG exp)
	{

		AFieldDeclCG field = new AFieldDeclCG();
		field.setAccess(access);
		field.setName(name);
		field.setVolatile(false);
		field.setStatic(isStatic);
		field.setFinal(isFinal);
		field.setType(type);
		field.setInitial(exp);

		return field;
	}

	public Set<ILexNameToken> getOverloadedMethodNames(
			AClassClassDefinition classDef)
	{
		List<LexNameTokenWrapper> methodNames = getMethodNames(classDef);
		Set<LexNameTokenWrapper> duplicates = findDuplicates(methodNames);

		Set<ILexNameToken> overloadedMethodNames = new HashSet<ILexNameToken>();

		for (LexNameTokenWrapper wrapper : methodNames)
		{
			if (duplicates.contains(wrapper))
			{
				overloadedMethodNames.add(wrapper.getName());
			}
		}

		return overloadedMethodNames;
	}

	private Set<LexNameTokenWrapper> findDuplicates(
			List<LexNameTokenWrapper> nameWrappers)
	{
		Set<LexNameTokenWrapper> duplicates = new HashSet<LexNameTokenWrapper>();
		Set<LexNameTokenWrapper> temp = new HashSet<LexNameTokenWrapper>();

		for (LexNameTokenWrapper wrapper : nameWrappers)
		{
			if (!temp.add(wrapper))
			{
				duplicates.add(wrapper);
			}
		}

		return duplicates;
	}

	private List<LexNameTokenWrapper> getMethodNames(
			AClassClassDefinition classDef)
	{
		List<LexNameTokenWrapper> methodNames = new LinkedList<LexNameTokenWrapper>();

		List<PDefinition> allDefs = new LinkedList<PDefinition>();

		LinkedList<PDefinition> defs = classDef.getDefinitions();
		LinkedList<PDefinition> inheritedDefs = classDef.getAllInheritedDefinitions();

		allDefs.addAll(defs);
		allDefs.addAll(inheritedDefs);

		for (PDefinition def : allDefs)
		{
			if (def instanceof SOperationDefinition
					|| def instanceof SFunctionDefinition)
			{
				methodNames.add(new LexNameTokenWrapper(def.getName()));
			}
		}

		return methodNames;
	}

	public void setDefaultValue(AVarLocalDeclCG localDecl, STypeCG typeCg)
			throws AnalysisException
	{
		ExpAssistantCG expAssistant = assistantManager.getExpAssistant();

		if (typeCg instanceof AStringTypeCG)
		{
			localDecl.setExp(expAssistant.getDefaultStringlValue());
		} else if (typeCg instanceof ACharBasicTypeCG)
		{
			localDecl.setExp(expAssistant.getDefaultCharlValue());
		} else if (typeCg instanceof AIntNumericBasicTypeCG)
		{
			localDecl.setExp(expAssistant.getDefaultIntValue());
		} else if(typeCg instanceof ANat1NumericBasicTypeCG)
		{
			localDecl.setExp(expAssistant.getDefaultNat1Value());
		} else if(typeCg instanceof ANatNumericBasicTypeCG)
		{
			localDecl.setExp(expAssistant.getDefaultNatValue());
		}
		else if (typeCg instanceof ARealNumericBasicTypeCG)
		{
			localDecl.setExp(expAssistant.getDefaultRealValue());
		} else if (typeCg instanceof ABoolBasicTypeCG)
		{
			localDecl.setExp(expAssistant.getDefaultBoolValue());
		} else
		{
			localDecl.setExp(new ANullExpCG());
		}
	}

	public AFieldDeclCG getFieldDecl(List<AClassDeclCG> classes,
			ARecordTypeCG recordType, int number)
	{
		ARecordDeclCG record = findRecord(classes, recordType);

		return record.getFields().get(number);
	}

	public AFieldDeclCG getFieldDecl(List<AClassDeclCG> classes,
			ARecordTypeCG recordType, String memberName)
	{
		ATypeNameCG name = recordType.getName();

		if (name == null)
		{
			throw new IllegalArgumentException("Could not find type name for record type: "
					+ recordType);
		}

		String definingClassName = name.getDefiningClass();

		if (definingClassName == null)
		{
			throw new IllegalArgumentException("Could not find defining class for record type: "
					+ recordType);
		}

		String recName = name.getName();

		if (recName == null)
		{
			throw new IllegalArgumentException("Could not find record name for record type: "
					+ recordType);
		}

		AClassDeclCG definingClass = null;
		for (AClassDeclCG currentClass : classes)
		{
			if (currentClass.getName().equals(definingClassName))
			{
				definingClass = currentClass;
				break;
			}
		}

		if (definingClass == null)
		{
			throw new IllegalArgumentException("Could not find defining class with name: "
					+ definingClassName);
		}

		List<ARecordDeclCG> records = getRecords(definingClass);

		ARecordDeclCG recordDecl = null;
		for (ARecordDeclCG currentRec : records)
		{
			if (currentRec.getName().equals(recName))
			{
				recordDecl = currentRec;
				break;
			}
		}

		if (recordDecl == null)
		{
			throw new IllegalArgumentException("Could not find record with name '"
					+ recName + "' in class '" + definingClassName + "'");
		}

		List<AFieldDeclCG> fields = recordDecl.getFields();

		AFieldDeclCG field = null;
		for (AFieldDeclCG currentField : fields)
		{
			if (currentField.getName().equals(memberName))
			{
				field = currentField;
			}
		}

		if (field == null)
		{
			throw new IllegalArgumentException("Could not find field '"
					+ memberName + "' in record '" + recName + "' in class '"
					+ definingClassName + "'");
		}

		return field;
	}
}
