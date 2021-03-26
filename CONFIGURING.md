# Configuring the server
This guide assumes you have the server installed/build. <br />
All configurations are written in Cyan <b>CCFG</b> (except mime.types), remember that you <b>CANNOT</b> use tabs in CCFG as it breaks parsing.

<br />

# Context configuration
The Connective Standalone Server uses the 'contextfile' configuration system inside CCFG.<br />
Each (virtual) folder is created by using the following statements:

```
# File: server.ccfg
#
# HTTP Context Configuration
# Format: context-root> 'contextfile'
context> {

	# Default entry
	root> '
	
	# The following instruction assigns the '/' server folder as the virtual root.
	# Requests to, say, http://localhost:8080/ are directed here
    virtualroot "/"
    
    '

	# Custom entry
	[real folder path]> '	
	
	# All ContextFiles are simple sets of instructions
	virtualroot [virtual folder name or path]
	
	# Insert more instructions here, the following line disables index pages
	# defaultindexpage null	
	
	'	
	
}
```

By default, a simple context file is added pointing to the folder named 'root' inside the server directory.

### ContextFile Instrucitions
You can find a list of all contextfile instructions [here](CONTEXTFILE-INSTRUCTIONS.md)

<br />

# Server MIME types (NON-CCFG)
All mime types are loaded from java, but you can also add you own. <br/>
You can create a `mime.types` file in the server root and configure it as following:

```
# Remove this line and the 2 following
# The format goes as follows: [mime path] <tab> [extension] <tab> [optionally more]
# Example: "application/yaml   yml   yaml", another example: "application/json   json"

mime/type	extension	another
```

By default, XML, JSON and YAML are already added.<br/>
The server also reads from `.mime.types` in the user home directory.

<br />

# Server modules

