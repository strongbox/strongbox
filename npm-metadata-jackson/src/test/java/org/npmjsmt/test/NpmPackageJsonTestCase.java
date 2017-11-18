package org.npmjsmt.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.npmjsmt.pojo.Package;

public class NpmPackageJsonTestCase {

	@Test
	public void testParseTypesNode() throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		Package packageDef = mapper.readValue(getClass().getResourceAsStream("/json/types/node/package.json"),
				Package.class);

		assertEquals("definitely-typed", packageDef.getName());
	}
}
