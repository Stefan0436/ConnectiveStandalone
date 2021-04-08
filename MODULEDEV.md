# Module Developer's Handbook
This page explains the DOs and DON'Ts for module development.<br/>
<br/>
<b>Note:</b> To make this easier, we have a preset project ready in the examplemodule folder.<br/>
To use it, copy the folder and run the following commands:

### For linux:

```bash
chmod +x gradlew createlocalserver.sh
./createlocalserver.sh
```

### For windows:

```batch
createlocalserver.bat
```

After which, modify `settings.gradle` and `build.gradle` to use your project names and group.<br/>
Then run `./gradlew createEclipseLaunches readme` (gradlew.bat on windows) to create the eclipse files and README.md.

## Understanding the loading chain
<b>Please note that the following applies to ConnectiveHTTP, the RaTs! chain differs slightly.</b><br />
ConnectiveHTTP loads in the following order:

1. Load `coremodules`
2. Load own components:
2. - Instanciate configuration
3. - Load configuration from file
4. - Load contextfile(s)
5. Load normal `modules`
6. Call the `bootstrap.call` `Memory` entry (more about memory later)
7. Call the `bootstrap.exec.call` `Memory` entry (avoid usage)


## Making your module loadable
Like RaTs, the standalone server is based on `CyanComponents`, so all you need to do is specify
the `@CYAN_COMPONENT` annotation for your module class and it is automatically loaded.<br/>
<br/>
<b>Note:</b> RaTs! works differently; apart from having the annotation, you will also need to tell the end-user to add the module class type to the `components.ccfg` file.<br/>
<br/>
Apart from the annotation, you will also need a `protected static` function named `initComponent` with no parameters, that function is called to LOAD the module, do not execute heavy code there.


## initComponent DOs and DONTs
Here follows a short list of this you should and shouldn't do in initComponents:
2. DO register and/or append `Memory` entries
3. DO append to the `bootstrap.call` entry.
5. DO set up needed files and directories, as long as it does not touch the actual server code.
4. DONT directly register module configuration entries through memory.
1. DONT use configuration-sensitive information


## Memory entries
RaTs! (and by extension ConnectiveHTTP) provides a system referred to as `Memory`<br/>
The settings stored there are common values for all modules (and server) to use.<br/>
It is the preferred way to save information during runtime, here follows a short guide on how to use it.

#### Basic information
To use `Memory`, you will need to retrieve the main instance with `Memory.getInstance()`<br/>
Take a look at the following example explaining how to create a simple memory entry:

```java
// Retrieve the main instance
Memory mem = Memory.getInstance();

// Get (or create) our own memory node (org.example.module.memory.test, can be anything)
Memory node = mem.getOrCreate("org.example.module.memory.test");

// Append a new String to the memory entry
node.append("test");


// Retrieve it in one go, the following retrieves the first value it can find
String value = Memory.getInstance().getOrCreate("org.example.module.memory.test").getValue(String.class);

// The following retrieves all values currently present in the node
String values[] = Memory.getInstance().getOrCreate("org.example.module.memory.test").getValues(String.class);
```

#### When to use `get()` and `getOrCreate()`
You should use `getOrCreate()` in most cases,<br/>
Use `get()` if you are trying to access existing values. Do not use `get()` unless you are certain the entry exists.<br/>
<b>You have to use `getOrCreate()` for `bootstrap` entries because they might not exist.</b>


#### When to use `append()` and when to use `assign()`
It is best to use `append()` whenever possible.<br/>
But you should use `assign()` if you have entries that should not have multiple values.

#### Retrieving values
To retrieve a single-value entry, use `getValue(TheClass.class)`<br/>
For accessing multi-value entries, you should <b>always</b> use `getValues(TheClass.class)`
<br/>
<br/>
<b>Refer to the [javadoc](https://aerialworks.ddns.net/javadoc/RaTs/Memory/index.html) for more information.</b>


## ContextFile instructions
You can create your own contextfile instructions from `coremodules` by using the `ContextFileInstruction` inteface. (the instructions are automatically loaded when present in module jarfiles)<br/>
The `instructionName` should return the lower-case alphabetical instruction name used to call the instruction.<br/>
<br/>
The `maximalArguments` should return the maximal amount of arguments that can be given to the instruction and by overriding `minimalArguments`, you can control the minimal number of arguments needed to run the instruction (minimalArguments returns maximalArguments unless overridden, setting -1 for maximalArguments disables the max amount of arguments)<br/>
<br/>
from the `run` method, you can make modifications to the provided context factory.<br/>
<b>Please document your instructions to help people understand how to use them.</b>

#### Regex parsing
It is recommended to implement REGEX parsing instead of string equals, you can take a look at the [RestrictionInstruction Source Code](src/main/java/org/asf/connective/standalone/main/impl/RestrictionInstruction.java) to get an idea of how Connective does this.

## Writing configuration support for your own modules
This applies to both normal modules and coremodules, as developer, you should use the `memory.modules.shared.config` memory entry, it is an instance of the `ModuleBasedConfiguration` supertype which used by both ConnectiveHTTP and RaTs! Furthermore, you should only run this logic from the `bootstrap.call` memory runnable. (append to it)<br />
<br />
To properly implement a configuration entry, use the following steps in your runnable:
1. Check if the config entry is present in the modules map
2. If some values are not present, write your defaults and save the configuration (writeall)
3. Read you configuration properties. (does not matter if it has been written or not)
