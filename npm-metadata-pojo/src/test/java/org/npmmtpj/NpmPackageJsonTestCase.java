package org.npmmtpj;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.npmmtpj.metadata.pojo.Package;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NpmPackageJsonTestCase {

	@Test
	public void testParseTypesNode() throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		Package packageDef = mapper.readValue(getClass().getResourceAsStream("/json/types/node/package.json"),
				Package.class);

		assertEquals("definitely-typed", packageDef.getName());
	}
}
