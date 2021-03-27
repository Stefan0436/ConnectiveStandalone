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
    virtualroot "[virtual folder name or path]"
    
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

mime/type    extension    another
```

By default, XML, JSON and YAML are already added.<br/>
The server also reads from `.mime.types` in the user home directory.

<br />

# Server modules
ConnectiveHTTP is modular, it supports both core and normal modules, if written correctly, they
can also run on the RaTs! build-in http server.

#### Installing modules
If you haven't run the server before, start it at least once. <br />
After which, you will have a `modules` directory, simple drop the module in it and restart the server.

#### Installing coremodules
If you haven't run the server before, start it at least once. <br />
After which, you will have a `coremodules` directory, simple drop the module in it and restart the server, please know that coremodules might be incompatible with one another.


#### Module configuration
The standard we (AerialWorks) provide is that modules use the HTTP server configuration,
if written correctly, the module properties should appear in the `modules` block of server.ccfg.

#### File extensions from modules
To use module extensions (such as php), <b>you will need to refer to the module webpage</b> to find out which `ExtensionProvider` type name they use. Once you have it, add the following entries:

```
# File: server.ccfg
#
# HTTP Context Configuration, already exists.
# Format: context-root> 'contextfile'
context> {

    # Default entry, just an example, you can use any of your own contexts
    root> '
    
    # ...
   
    # The following line matters, replace [extension-class] with the ExtensionProvider type name.
    extension class:[extension-class]
    
    # Example: PHP Support Module (needs to be installed)
    # extension class:org.asf.connective.php.PHPExtensionProvider
    
    # ...
    
    '
}
```

#### Module developer's guide
Refer to [MODULEDEV.md](MODULEDEV.md) for the full list of tips for module development.
