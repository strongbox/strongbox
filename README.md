# Introducion

This project is set of POJOs which is relevant for various `NPM` metadata files, like `package.json`. 
It use native [JSON Schema](http://json-schema.org/) to define and then generate Java classes.
It also use [Jackson](https://github.com/FasterXML/jackson) as default JSON parser. 

# How to use

	ObjectMapper mapper = new ObjectMapper();
	Package packageDef = mapper.readValue(jsonInputStream, Package.class);
