package org.overture.core.npp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the extensibility of InsTable and VdmNsTable
 * @author ldc
 *
 */
public class InsExtText {

	class ExtTable extends VdmNsTable implements InsTable {

		public ExtTable() {
			super();
			this.insertAttribute("extkey", "extattrib");
			this.insertAttribute("head", "exthd");
		}
	}

	ExtTable eTable;

	@Before
	public void setup() {
		eTable = new ExtTable();
	}

	@Test
	public void testExtTable_Constructor_NoParams() {
		assertNotNull(eTable);
	}

	@Test
	public void testGetAttribute_Inherit() {
		String actual = eTable.getAttribute("TAIL");
		String expected = "tl";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetAttribute_Override() {
		String actual = eTable.getAttribute("head");
		String expected = "exthd";

		assertEquals(expected, actual);
	}

	@Test
	public void testGetAttribute_NewAttrib() {
		String actual = eTable.getAttribute("extkey");
		String expected = "extattrib";

		assertEquals(expected, actual);
	}

}
