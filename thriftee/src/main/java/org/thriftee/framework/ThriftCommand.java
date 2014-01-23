package org.thriftee.framework;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.thriftee.framework.ThriftCommand.Generate.Flag;

public class ThriftCommand {
	
	public static enum Generate {
		
		PHP("PHP"),
		JS("Javascript"),
		;
		
		public final String option;
		
		public final String description;
				
		private Generate(String description) {
			this.description = description;
			this.option = name().toLowerCase();
		}

		public static final class Flag extends org.apache.commons.lang.enums.Enum {
			
			public static final Flag JS_JQUERY		=	new Flag( JS,	"jquery",		"Generate jQuery compatible code",						false );
			public static final Flag JS_NODE		=	new Flag( JS,	"node",			"Generate node.js compatible code",						false );
			
			public static final Flag PHP_INLINED 	=	new Flag( PHP,	"inlined", 		"Generate PHP inlined files", 							false );
			public static final Flag PHP_SERVER 	=	new Flag( PHP,	"server", 		"Generate PHP server stubs", 							false );
			public static final Flag PHP_AUTOLOAD	= 	new Flag( PHP,	"autoload",		"Generate PHP with autoload", 							false );
			public static final Flag PHP_OOP 		=	new Flag( PHP,	"oop", 			"Generate PHP with object oriented subclasses", 		false );
			public static final Flag PHP_REST 		=	new Flag( PHP,	"rest", 		"Generate PHP REST processors", 						false );
			public static final Flag PHP_NAMESPACE 	= 	new Flag( PHP,	"namespace", 	"Generate PHP namespaces as defined in PHP >= 5.3", 	false );
			
			private static final long serialVersionUID = -3504700206843791875L;

			public final String key;
			
			public final String description;
			
			public final Generate language;
			
			public final boolean requiresValue;
			
			public final String displayName; 
			
			private Flag(Generate lang, String key, String description, boolean requiresValue) {
				super(makeName(lang, key));
				this.language = lang;
				this.key = key;
				this.description = description;
				this.requiresValue = requiresValue;
				this.displayName = this.language.option + ":" + this.key;
			}
			
			private static final String makeName(Generate lang, String key) {
				return lang.name().toUpperCase() + "_" + key.toUpperCase();
			}
			
		}

	}

	public static String getDefaultExecutableName() {
		return System.getProperty("os.name").startsWith("Windows") ? WINDOWS_EXECUTABLE : DEFAULT_EXECUTABLE;
	}
	
	public static String searchPathForThrift() {
//		String path = System.getProperty("java.bin.path");
		String path = System.getenv("PATH");
		String[] parts = StringUtils.split(path, File.pathSeparatorChar);
		String executable = getDefaultExecutableName();
		for (String part : parts) {
			File possible = new File(part, executable);
			if (possible.exists() && possible.canExecute()) {
				return possible.getAbsolutePath();
			}
		}
		return executable;
	}
	
	public static String WINDOWS_EXECUTABLE = "thrift.exe";
	
	public static String DEFAULT_EXECUTABLE = "thrift";
		
	private String thriftCommand = searchPathForThrift();
	
	private String outputDirectory;
	
	private String outputLocation;
	
	private List<String> includeDirectories = new ArrayList<String>();
	
	private boolean noWarn;
	
	private boolean strict = false;
	
	private boolean verbose = false;
	
	private boolean recurse = false;
	
	private boolean debug = false;
	
	private boolean allowNegativeFieldKeys = false;
	
	private boolean allow64bitConsts = false;
	
	private String thriftFile = "\"<output file>\"";
	
	private final Generate language;
	
	private Map<Flag, String> generateFlags = new HashMap<Flag, String>();
	
	public ThriftCommand(Generate lang) {
		this.language = lang;
		setDefaultThriftCommand();
	}
	
	public ThriftCommand(Generate lang, String thriftFile) {
		this(lang);
		this.thriftFile = thriftFile;
	}
	
	protected void setDefaultThriftCommand() {
		if (System.getenv().containsKey("THRIFT_CMD")) {
			setThriftCommand(System.getenv("THRIFT_CMD"));
			return;
		}
		if (System.getProperties().containsKey("thriftee.command")) {
			setThriftCommand(System.getProperty("thriftee.command"));
			return;
		}
	}
	
	public void addFlag(Flag flag) {
		addFlag(flag, null);
	}

	public void addFlag(Flag flag, String value) {
		if (!flag.language.equals(this.language)) {
			throw new IllegalArgumentException("Flag `" + flag.displayName + "` is not applicable for " + this.language.description); 
		}
		if (flag.requiresValue && StringUtils.isBlank(value)) {
			throw new IllegalArgumentException("Value for `" + flag.displayName + "` requires a value");
		}
		if (!flag.requiresValue && StringUtils.isNotBlank(value)) {
			throw new IllegalArgumentException("Value for `" + flag.displayName + "` cannot have a value");
		}
		this.generateFlags.put(flag, value);
	}
	
	public boolean isNoWarn() {
		return noWarn;
	}

	public void setNoWarn(boolean nowarn) {
		this.noWarn = nowarn;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isRecurse() {
		return recurse;
	}

	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isAllowNegativeFieldKeys() {
		return allowNegativeFieldKeys;
	}

	public void setAllowNegativeFieldKeys(boolean allowNegativeFieldKeys) {
		this.allowNegativeFieldKeys = allowNegativeFieldKeys;
	}

	public boolean isAllow64bitConsts() {
		return allow64bitConsts;
	}

	public void setAllow64bitConsts(boolean allow64bitConsts) {
		this.allow64bitConsts = allow64bitConsts;
	}
	
	public void setOutputDirectory(File file) {
		if (file == null) {
			setOutputDirectory((String) null);
		} else {
			setOutputDirectory(file.getAbsolutePath());
		}
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	public void setOutputLocation(File file) {
		if (file == null) {
			setOutputLocation((String) null);
		} else {
			setOutputLocation(file.getAbsolutePath());
		}
	}

	public String getOutputLocation() {
		return outputLocation;
	}

	public void setOutputLocation(String outputLocation) {
		this.outputLocation = outputLocation;
	}
	
	public String getThriftCommand() {
		return thriftCommand;
	}

	public void setThriftCommand(String thriftCommand) {
		if (StringUtils.isBlank(thriftCommand)) {
			throw new IllegalArgumentException("thriftCommand cannot be blank");
		}
		this.thriftCommand = thriftCommand;
	}
	
	public String getThriftFile() {
		return thriftFile;
	}

	public void setThriftFile(String thriftFile) {
		if (StringUtils.isBlank(thriftCommand)) {
			throw new IllegalArgumentException("thriftFile cannot be blank");
		}
		this.thriftFile = thriftFile;
	}
	
	public void setThriftFile(File thriftFile) {
		if (thriftFile == null) {
			throw new IllegalArgumentException("thriftFile cannot be blank");
		}
		this.thriftFile = thriftFile.getAbsolutePath();
	}

	public String generateString() {
		StringBuilder gstr = new StringBuilder();
		gstr.append(this.language.option);
		if (!this.generateFlags.isEmpty()) {
			gstr.append(':');
			for (Iterator<Entry<Flag, String>> i = generateFlags.entrySet().iterator(); i.hasNext(); ) {
				Entry<Flag, String> entry = i.next();
				gstr.append(entry.getKey().key);
				if (entry.getKey().requiresValue) {
					gstr.append("=").append(escape(entry.getValue()));
				}
				if (i.hasNext()) {
					gstr.append(',');
				}
			}
		}
		return gstr.toString();
	}
	
	public List<String> extraOptions() {
		List<String> opts = new LinkedList<String>();
		if (this.outputDirectory != null) {
			opts.add("-o");
			opts.add(escape(this.outputDirectory));
		}
		if (this.outputLocation != null) {
			opts.add("-out");
			opts.add(escape(this.outputLocation));
		}
		for (String includeDirectory : includeDirectories) {
			opts.add("-I");
			opts.add(escape(includeDirectory));
		}
		if (noWarn) {
			opts.add("-nowarn");
		}
		if (strict) {
			opts.add("-strict");
		}
		if (verbose) {
			opts.add("-verbose");
		}
		if (recurse) {
			opts.add("-recurse");
		}
		if (debug) {
			opts.add("-debug");
		}
		if (allowNegativeFieldKeys) {
			opts.add("--allow-neg-keys");
		}
		if (allow64bitConsts) {
			opts.add("--allow-64bit-consts");
		}
		return opts;
	}
	
	public List<String> command() {
		List<String> extraOptions = extraOptions();
		List<String> command = new ArrayList<String>(extraOptions.size() + 5);
		command.add(escape(this.thriftCommand));
		command.add("-gen");
		command.add(this.generateString());
		command.addAll(extraOptions);
		command.add(StringUtils.isBlank(thriftFile) ? "\"<output file>\"" : escape(thriftFile));
		return command;
	}
		
	protected String escape(String value) {
		if (value.contains(" ")) {
			return new StringBuilder()
						.append('"')
						.append(value)
						.append('"')
						.toString();	
		} else {
			return value;
		}
	}
	
	public String commandString() {
		return StringUtils.join(command(), ' ');
	}
	
	public String toString() {
		return "ThriftCommand[" + commandString() + "]";
	}
	
//	public void setThriftExecutable(File file) {
//		this.thriftExecutable = file;
//	}
	
}
