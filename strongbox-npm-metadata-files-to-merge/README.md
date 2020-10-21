# Introduction

This project is a set of POJOs which are relevant for various `NPM` metadata files, such as `package.json`. 
It uses native [JSON Schema](http://json-schema.org/) to define and then generate Java classes.
It also uses [Jackson](https://github.com/FasterXML/jackson) as default the JSON parser. 

# How to use

	ObjectMapper mapper = new ObjectMapper();
	Package packageDef = mapper.readValue(jsonInputStream, Package.class);
