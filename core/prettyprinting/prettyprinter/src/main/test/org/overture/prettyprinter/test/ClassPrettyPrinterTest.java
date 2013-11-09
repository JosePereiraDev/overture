package org.overture.prettyprinter.test;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.prettyprinter.PrettyPrinterEnv;
import org.overture.prettyprinter.PrettyPrinterVisitor;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

import static org.junit.Assert.*;

public class ClassPrettyPrinterTest {

	@Before
	public void Setup()
	{
		Settings.dialect = Dialect.VDM_SL;
		Settings.release = Release.VDM_10;
	}
	
	@Test
	public void Test() throws AnalysisException
	{
		File f = new File("src/test/resources/values");
		
		pogSL(f);
	}
	
	
	private String pogSL(File file) throws AnalysisException
	{
		System.out.println("Processing " + file);
		
		TypeCheckResult<List<AModuleModules>> TC = TypeCheckerUtil.typeCheckSl(file);
		
		
		
		assertTrue("Specification has syntax errors", TC.parserResult.errors.isEmpty());
		assertTrue("Specification has type errors", TC.errors.isEmpty());
		
		for (AModuleModules module : TC.parserResult.result) {
			String result = module.apply(new PrettyPrinterVisitor(), new PrettyPrinterEnv());
			
			System.out.println(result);
		}
		

		return "";
	}
}
			