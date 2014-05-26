package org.overture.pog.tests.old;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.node.INode;
import org.overture.config.Release;
import org.overture.config.Settings;
import org.overture.pog.pub.IProofObligationList;
import org.overture.pog.pub.ProofObligationGenerator;
import org.overture.pog.tests.InputsProvider;
import org.overture.pog.tests.PoResult;
import org.overture.pog.tests.TestHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RunWith(Parameterized.class)
public class OldStringPogTest
{
	private String modelPath;
	private String resultPath;

	@Before
	public void setup(){
		Settings.release = Release.DEFAULT;
	}
	
	@Parameters(name = "{index} : {0}")
	public static Collection<Object[]> testData() {
		return InputsProvider.old();
	}

	public OldStringPogTest(String modelPath, String resultPath)
	{
		super();
		this.modelPath = modelPath;
		this.resultPath = resultPath;
	}
	
	@Test
	public void testWithCompare() throws AnalysisException, IOException,
			URISyntaxException {

		
		List<INode> ast = TestHelper.getAstFromName(modelPath);
		IProofObligationList ipol = ProofObligationGenerator
				.generateProofObligations(ast);
		
		Gson gson = new Gson();
		String json = IOUtils.toString(new FileReader(resultPath));
		Type datasetListType = new TypeToken<Collection<PoResult>>() {
		}.getType();
		List<PoResult> results = gson.fromJson(json, datasetListType);

		assertTrue("Generated and stored POs differ",
				TestHelper.sameElements(results, ipol));

	}

	
	
}
