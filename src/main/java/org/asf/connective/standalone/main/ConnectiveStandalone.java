package org.asf.connective.standalone.main;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.ZipInputStream;

import org.asf.aos.util.service.extra.slib.util.ArrayUtil;
import org.asf.connective.standalone.ContextFileInstruction;
import org.asf.connective.standalone.main.impl.AliasInstruction;
import org.asf.connective.standalone.main.impl.DefaultIndexPageInstruction;
import org.asf.connective.standalone.main.impl.ExtensionInstruction;
import org.asf.connective.standalone.main.impl.IndexPageInstruction;
import org.asf.connective.standalone.main.impl.UploadHandlerInstruction;
import org.asf.connective.standalone.main.impl.RestrictionInstruction;
import org.asf.connective.standalone.main.impl.VirtualFileInstruction;
import org.asf.connective.standalone.main.impl.VirtualRootInstruction;
import org.asf.connective.standalone.main.impl.internal.ConnectiveAuthProvider;
import org.asf.cyan.api.common.CYAN_COMPONENT;
import org.asf.cyan.api.config.serializing.internal.Splitter;
import org.asf.cyan.fluid.Transformer.AnnotationInfo;
import org.asf.cyan.fluid.bytecode.FluidClassPool;
import org.asf.cyan.fluid.bytecode.sources.LoaderClassSourceProvider;
import org.asf.cyan.fluid.implementation.CyanTransformer;
import org.asf.rats.ConnectiveHTTPServer;
import org.asf.rats.Memory;
import org.asf.rats.http.BasicFileModule;
import org.asf.rats.http.FileProcessorContextFactory;
import org.asf.rats.http.ProviderContextFactory;
import org.asf.rats.processors.HttpGetProcessor;
import org.asf.rats.processors.HttpUploadProcessor;
import org.objectweb.asm.tree.ClassNode;

/**
 * 
 * ConnectiveHTTP Standalone Main Component.
 * 
 * @author Stefan0436 - AerialWorks Software Foundation
 *
 */
@CYAN_COMPONENT
public class ConnectiveStandalone extends ConnectiveHTTPServer implements Closeable {

	private static boolean simpleInit = false;
	private static boolean init = false;
	private static ConnectiveStandalone impl;

	private static URLClassLoader moduleLoader = null;
	private static Class<?>[] defaultClasses = new Class[] { ConnectiveHTTPServer.class, ConnectiveStandalone.class,
			BasicFileModule.class, VirtualRootInstruction.class, DefaultIndexPageInstruction.class,
			IndexPageInstruction.class, RestrictionInstruction.class, ExtensionInstruction.class,
			AliasInstruction.class, UploadHandlerInstruction.class, VirtualFileInstruction.class };

	private static FluidClassPool modulePool = FluidClassPool.createEmpty();
	private static ArrayList<ContextFileInstruction> instructions;

	public URLClassLoader getClassLoader() {
		return moduleLoader;
	}

	/**
	 * Main init method, called by java
	 * 
	 * @throws IOException           If loading fails
	 * @throws IllegalStateException If loading fails
	 */
	public static void main(String[] args) throws IllegalStateException, IOException {
		if (args.length != 0 && args[0].equals("credtool")) {
			CredentialTool.main(Arrays.copyOfRange(args, 1, args.length));
			return;
		}

		if (System.getProperty("ideMode") != null) {
			System.setProperty("log4j2.configurationFile",
					ConnectiveStandalone.class.getResource("/log4j2-ide.xml").toString());
		} else {
			System.setProperty("log4j2.configurationFile",
					ConnectiveStandalone.class.getResource("/log4j2.xml").toString());
		}

		if (System.getProperty("addCpModules") != null) {
			for (String mod : System.getProperty("addCpModules").split(":")) {
				try {
					defaultClasses = ArrayUtil.append(defaultClasses, new Class[] { Class.forName(mod) });
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
		}

		ConnectiveStandalone.initializeComponents();

		info("Starting components post-load (bootstrap.call)...");
		Memory mem = Memory.getInstance().getOrCreate("bootstrap.call");
		for (Runnable runnable : mem.getValues(Runnable.class)) {
			runnable.run();
		}

		info("Starting server...");
		mem = Memory.getInstance().getOrCreate("bootstrap.exec.call");
		for (Runnable runnable : mem.getValues(Runnable.class)) {
			runnable.run();
		}

		info("Stopping...");
		impl.close();
	}

	/**
	 * Simple init, only assigns main implementation
	 */
	public static void simpleInit() {
		if (simpleInit)
			return;

		simpleInit = true;
		impl = new ConnectiveStandalone();
		impl.assignImplementation();
	}

	/**
	 * Initializes all components
	 * 
	 * @throws IllegalStateException If loading fails
	 * @throws IOException           If loading modules fails.
	 */
	public static void initializeComponents() throws IllegalStateException, IOException {
		if (init)
			throw new IllegalStateException("Components have already been initialized.");

		simpleInit();

		File modules = new File("modules");
		File coremodules = new File("coremodules");

		if (!coremodules.exists())
			coremodules.mkdirs();
		if (!modules.exists())
			modules.mkdirs();

		ArrayList<URL> sources = new ArrayList<URL>();

		for (File jar : coremodules.listFiles((arg0) -> {
			return !arg0.isDirectory();
		})) {
			ZipInputStream strm = new ZipInputStream(new FileInputStream(jar));
			modulePool.importArchive(strm);
			strm.close();
			sources.add(jar.toURI().toURL());
		}

		modulePool.addSource(new LoaderClassSourceProvider(ClassLoader.getSystemClassLoader()));
		modulePool.addSource(new LoaderClassSourceProvider(Thread.currentThread().getContextClassLoader()));

		if (System.getProperty("debugCoremodules") != null && System.getProperty("debugCredentials") != null) {

			String cred = System.getProperty("debugCredentials");
			try {
				cred = new String(Base64.getDecoder().decode(cred));
			} catch (Exception ex) {
				fatal("Failed to load CredString from command line! Unable to load!");
				fatal("");
				fatal("Please specify -DdebugCredentials=\"CredString\" to load coremodules from the command line.");
				fatal("The 'CredString' is the base64-encoded value of 'username:password' which needs to be specified that way.");
				fatal("The 'debug' credential group is used for this, use the 'credtool' program argument for more information.");
				System.exit(-1);
				return;
			}
			if (cred.contains(":")) {
				String username = cred.substring(0, cred.indexOf(":"));
				String password = cred.substring(cred.indexOf(":") + 1);

				if (!username.matches("^[A-Za-z0-9@. ']+$")) {
					fatal("Failed to load CredString from command line! Unable to load!");
					fatal("ERROR: Username not alphanumeric. (note: the '.', '@', ' ' and \"'\" are also allowed)");
					fatal("");
					fatal("Please specify -DdebugCredentials=\"CredString\" to load coremodules from the command line.");
					fatal("The 'CredString' is the base64-encoded value of 'username:password' which needs to be specified that way.");
					fatal("The 'debug' credential group is used for this, use the 'credtool' program argument for more information.");
					System.exit(-1);
					password = null;
					return;
				}

				boolean invalid = false;
				File userFile = new File("credentials", "gr.debug." + username + ".cred");
				if (!userFile.exists()) {
					invalid = true;
				} else {
					try {
						String userPass = new String(Base64.getDecoder().decode(Files.readAllBytes(userFile.toPath())));
						if (!userPass.equals(password))
							invalid = true;

						userPass = null;
					} catch (Exception e) {
						invalid = true;
					}
				}
				password = null;

				if (invalid) {
					fatal("Failed to load CredString from command line! Unable to load!");
					fatal("ERROR: Username or password incorrect!");
					fatal("");
					fatal("Please specify -DdebugCredentials=\"CredString\" to load coremodules from the command line.");
					fatal("The 'CredString' is the base64-encoded value of 'username:password' which needs to be specified that way.");
					fatal("The 'debug' credential group is used for this, use the 'credtool' program argument for more information.");
					System.exit(-1);
					return;
				}

				for (String mod : System.getProperty("debugCoremodules").split(":")) {
					try {
						modulePool.getClassNode(mod);
					} catch (ClassNotFoundException e) {
						error("Failed to import coremodule class: " + mod, e);
					}
				}
			} else {
				fatal("CredString syntax invalid!");
				fatal("Expected: username:password");
				fatal("");
				fatal("Please specify -DdebugCredentials=\"CredString\" to load coremodules from the command line.");
				fatal("The 'CredString' is the base64-encoded value of 'username:password' which needs to be specified that way.");
				fatal("The 'debug' credential group is used for this, use the 'credtool' program argument for more information.");
				System.exit(-1);
				return;
			}
		} else if (System.getProperty("debugCoremodules") != null) {
			fatal("");
			fatal("");
			fatal("The argument debugCoremodules was specified without debugCredentials, cannot load!");
			fatal("To secure the coremodule system, you will need to supply valid credentials to debug coremodules!");
			fatal("");
			fatal("Please specify -DdebugCredentials=\"CredString\" to load coremodules from the command line.");
			fatal("The 'CredString' is the base64-encoded value of 'username:password' which needs to be specified that way.");
			fatal("The 'debug' credential group is used for this, use the 'credtool' program argument for more information.");
			System.exit(-1);
			return;
		}

		for (String path : Splitter.split(System.getProperty("java.class.path"), ':')) {
			if (path.equals("."))
				continue;

			File f = new File(path);
			sources.add(f.toURI().toURL());
			try {
				modulePool.addSource(f.toURI().toURL());
			} catch (MalformedURLException e) {
				error("Failed to load class path entry " + path, e);
			}
		}

		for (Class<?> cls : defaultClasses) {
			try {
				modulePool.getClassNode(cls.getTypeName());
			} catch (ClassNotFoundException e) {
				error("Failed to import class: " + cls.getTypeName(), e);
			}
		}

		for (File jar : modules.listFiles((arg0) -> {
			return !arg0.isDirectory();
		})) {
			ZipInputStream strm = new ZipInputStream(new FileInputStream(jar));
			modulePool.importArchive(strm);
			strm.close();
			sources.add(jar.toURI().toURL());
		}

		Class<?> cts = CyanTransformer.class;
		try {
			Method m = cts.getDeclaredMethod("initComponent");
			m.setAccessible(true);
			m.invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
				| NoSuchMethodException e) {
			error("Failed to initialize component, class name: " + cts.getSimpleName(), e);
		}

		moduleLoader = new URLClassLoader(sources.toArray(t -> new URL[t]),
				ConnectiveStandalone.class.getClassLoader());
		impl.initializeComponentClasses();
	}

	/**
	 * Finds classes of a given supertype or interface
	 * 
	 * @param <T>                  Class type.
	 * @param supertypeOrInterface Supertype or interface.
	 * @return Array of classes.
	 */
	public static <T> Class<T>[] findClasses(Class<T> supertypeOrInterface) {
		return findClasses(getMainImplementation(), supertypeOrInterface);
	}

	protected static void initComponent() throws IOException {
		init = true;
		ConnectiveAuthProvider.assign();

		info("Intanciating configuration...");
		if (implementation != null)
			ConnectiveConfiguration.getInstance().httpPort = implementation.getPort();

		info("Loading configuration...");
		ConnectiveConfiguration.getInstance().readAll();

		info("Loading contextfile(s)...");
		ConnectiveConfiguration.getInstance().context.forEach((key, contextFile) -> {
			info("Creating context: " + key + "...");

			ProviderContextFactory context = new ProviderContextFactory();
			context.setExecLocation(key);
			if (!key.startsWith("/")) {
				key = (System.getProperty("rats.config.dir") == null ? "." : System.getProperty("rats.config.dir"))
						+ "/" + key;
			}
			if (!new File(key).exists()) {
				new File(key).mkdirs();
			}

			for (String line : contextFile.replaceAll("\r", "").split("\n")) {
				if (line.isEmpty() || line.startsWith("#"))
					continue;

				ArrayList<String> arguments = parseCommand(line);
				if (arguments.size() != 0) {
					String cmd = arguments.get(0);
					arguments.remove(0);

					boolean found = false;
					if (instructions == null) {
						instructions = new ArrayList<ContextFileInstruction>();
						for (Class<ContextFileInstruction> instr : findClasses(ContextFileInstruction.class)) {
							try {
								Constructor<ContextFileInstruction> ctor = instr.getDeclaredConstructor();
								ctor.setAccessible(true);
								ContextFileInstruction ins = ctor.newInstance();
								instructions.add(ins);
							} catch (Exception e) {
								error("Context instruction failed to register: " + instr.getTypeName(), e);
							}
						}
					}

					ContextFileInstruction instruction = null;
					for (ContextFileInstruction instr : instructions) {
						if (instr.instructionName().equals(cmd)) {
							found = true;
							instruction = instr;
							break;
						}
					}
					if (!found) {
						warn("Failed to execute context instruction as it was not found. Instruction: " + cmd);
					} else {
						if (arguments.size() < instruction.minimalArguments() || (instruction.maximalArguments() != -1
								&& arguments.size() > instruction.maximalArguments())) {
							error("Invalid arguments for instruction '" + line + "'");
						} else {
							try {
								instruction.run(arguments.toArray(t -> new String[t]), context);
							} catch (Exception e) {
								error("Failed to run instruction: " + cmd, e);
							}
						}
					}
				}
			}

			FileProcessorContextFactory.getDefault().addProviderContext(context.build());
		});

		info("Assigning server port... Port: " + ConnectiveConfiguration.getInstance().httpPort);
		Memory.getInstance().getOrCreate("connective.http.props.port")
				.assign(ConnectiveConfiguration.getInstance().httpPort);

		Memory.getInstance().getOrCreate("bootstrap.exec.call").append(new Runnable() {

			@Override
			public void run() {
				info("Loading processors...");
				for (String line : ConnectiveConfiguration.getInstance().processors.replaceAll("\r", "").split("\n")) {
					if (line.isEmpty() || line.startsWith("#"))
						continue;

					info("Registering processor: " + line);
					try {
						@SuppressWarnings("unchecked")
						Class<? extends HttpGetProcessor> cls = (Class<? extends HttpGetProcessor>) Class.forName(line,
								false, moduleLoader);

						HttpGetProcessor proc = cls.getConstructor().newInstance();
						if (proc instanceof HttpUploadProcessor) {
							ConnectiveHTTPServer.getMainServer().registerProcessor((HttpUploadProcessor) proc);
						} else {
							ConnectiveHTTPServer.getMainServer().registerProcessor(proc);
						}
					} catch (Exception e) {
						error("Could not register processor " + line, e);
					}
				}

				info("Server is running.");
				ConnectiveHTTPServer.getMainServer().waitExit();
			}

		});
	}

	protected static ArrayList<String> parseCommand(String args) {
		ArrayList<String> args3 = new ArrayList<String>();
		char[] argarray = args.toCharArray();
		boolean ignorespaces = false;
		String last = "";
		int i = 0;
		for (char c : args.toCharArray()) {
			if (c == '"' && (i == 0 || argarray[i - 1] != '\\')) {
				if (ignorespaces)
					ignorespaces = false;
				else
					ignorespaces = true;
			} else if (c == ' ' && !ignorespaces && (i == 0 || argarray[i - 1] != '\\')) {
				args3.add(last);
				last = "";
			} else if (c != '\\' || (i + 1 < argarray.length && argarray[i + 1] != '"'
					&& (argarray[i + 1] != ' ' || ignorespaces))) {
				last += c;
			}

			i++;
		}

		if (last == "" == false)
			args3.add(last);

		return args3;
	}

	/**
	 * Internal marker getter for CyanComponents
	 */
	public static String getMarker() {
		return "COMPONENTS";
	}

	@Override
	public String getLoggerName() {
		return "CONNECTIVE";
	}

	@Override
	protected void setupComponents() {
		if (LOG == null)
			initLogger();
	}

	@Override
	protected void preInitAllComponents() {
		trace("INITIALIZE all components, caller: " + CallTrace.traceCallName());
	}

	@Override
	protected void finalizeComponents() {
	}

	@Override
	protected Class<?>[] getComponentClasses() {
		info("Searching for classes annotated with @CYAN_COMPONENT...");
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		for (ClassNode cls : modulePool.getLoadedClasses()) {
			if (checkClass(cls, true, CYAN_COMPONENT.class)) {
				try {
					classes.add(moduleLoader.loadClass(cls.name.replace("/", ".")));
					debug("Found class: " + cls.name.replace("/", "."));
				} catch (ClassNotFoundException e) {
				}
			}
		}

		return classes.toArray(t -> new Class[t]);
	}

	@SuppressWarnings("unchecked")
	private boolean checkClass(ClassNode cls, boolean annotationMatch, Class<?> matcher) {
		boolean match = false;
		if (annotationMatch)
			match = AnnotationInfo.isAnnotationPresent((Class<? extends Annotation>) matcher, cls);
		else {
			match = cls.name.equals(matcher.getTypeName().replace(".", "/"));
			if (!match && cls.superName != null)
				match = cls.superName.equals(matcher.getTypeName().replace(".", "/"));
		}
		if (match)
			return true;
		else {
			if (annotationMatch)
				return false;

			if (cls.interfaces != null) {
				for (String interfaceCls : cls.interfaces) {
					try {
						return checkClass(modulePool.getClassNode(interfaceCls), annotationMatch, matcher);
					} catch (ClassNotFoundException e) {
					}
				}
			}

			if (cls.superName != null) {
				try {
					return checkClass(modulePool.getClassNode(cls.superName), annotationMatch, matcher);
				} catch (ClassNotFoundException e) {
				}
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> Class<T>[] findClassesInternal(Class<T> interfaceOrSupertype) {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		debug("Searching for classes of type: " + interfaceOrSupertype.getTypeName());
		for (ClassNode cls : modulePool.getLoadedClasses()) {
			if (checkClass(cls, false, interfaceOrSupertype)
					&& !interfaceOrSupertype.getTypeName().equals(cls.name.replace("/", "."))) {
				try {
					classes.add(moduleLoader.loadClass(cls.name.replace("/", ".")));
					debug("Found class: " + cls.name.replace("/", "."));
				} catch (ClassNotFoundException e) {
				}
			}
		}

		return classes.toArray(t -> new Class[t]);
	}

	@Override
	public void close() throws IOException {
		modulePool.close();
	}

	/**
	 * Retrieves the running instance of this class
	 */
	public static ConnectiveStandalone getInstance() {
		return impl;
	}

}
