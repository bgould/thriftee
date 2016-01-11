/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.framework.client.ClientTypeAlias;
import org.thriftee.util.Strings;

public class ThriftCommand {
  
  public static enum Generate {
    /*
    Available generators (and options):
        as3 (AS3):
        bindable:      Add [bindable] metadata to all the struct classes.
        c_glib (C, using GLib):
        cocoa (Cocoa):
        log_unexpected:  Log every time an unexpected field ID or type is encountered.
        validate_required:
                 Throws exception if any required field is not set.
        cpp (C++):
        cob_style:     Generate "Continuation OBject"-style classes.
        no_client_completion:
                 Omit calls to completion__() in CobClient class.
        no_default_operators:
                 Omits generation of default operators ==, != and <
        templates:     Generate templatized reader/writer methods.
        pure_enums:    Generate pure enums instead of wrapper classes.
        dense:       Generate type specifications for the dense protocol.
        include_prefix:  Use full include paths in generated files.
        csharp (C#):
        async:       Adds Async support using Task.Run.
        asyncctp:    Adds Async CTP support using TaskEx.Run.
        wcf:       Adds bindings for WCF to generated classes.
        serial:      Add serialization support to generated classes.
        nullable:    Use nullable types for properties.
        hashcode:    Generate a hashcode and equals implementation for classes.
        union:       Use new union typing, which includes a static read function for union types.
        d (D):
        delphi (delphi):
        ansistr_binary:  Use AnsiString for binary datatype (default is TBytes).
        register_types:  Enable TypeRegistry, allows for creation of struct, union
                 and container instances by interface or TypeInfo()
        constprefix:   Name TConstants classes after IDL to reduce ambiguities
        events:      Enable and use processing events in the generated code.
        erl (Erlang):
        go (Go):
        package_prefix= Package prefix for generated files.
        thrift_import=  Override thrift package import path (default:git.apache.org/thrift.git/lib/go/thrift)
        gv (Graphviz):
        exceptions:    Whether to draw arrows from functions to exception.
        hs (Haskell):
        html (HTML):
        standalone:    Self-contained mode, includes all CSS in the HTML files.
                 Generates no style.css file, but HTML files will be larger.
        java (Java):
        beans:       Members will be private, and setter methods will return void.
        private-members: Members will be private, but setter methods will return 'this' like usual.
        nocamel:     Do not use CamelCase field accessors with beans.
        android_legacy:  Do not use java.io.IOException(throwable) (available for Android 2.3 and above).
        java5:       Generate Java 1.5 compliant code (includes android_legacy flag).
        sorted_containers:
                 Use TreeSet/TreeMap instead of HashSet/HashMap as a implementation of set/map.
        javame (Java ME):
        js (Javascript):
        jquery:      Generate jQuery compatible code.
        node:      Generate node.js compatible code.
        ocaml (OCaml):
        perl (Perl):
        php (PHP):
        inlined:     Generate PHP inlined files
        server:      Generate PHP server stubs
        oop:       Generate PHP with object oriented subclasses
        rest:      Generate PHP REST processors
        py (Python):
        new_style:     Generate new-style classes.
        twisted:     Generate Twisted-friendly RPC services.
        tornado:     Generate code for use with Tornado.
        utf8strings:   Encode/decode strings using utf8 in the generated code.
        slots:       Generate code using slots for instance members.
        dynamic:     Generate dynamic code, less code generated but slower.
        dynbase=CLS    Derive generated classes from class CLS instead of TBase.
        dynexc=CLS     Derive generated exceptions from CLS instead of TExceptionBase.
        dynimport='from foo.bar import CLS'
                 Add an import line to generated code to find the dynbase class.
        rb (Ruby):
        rubygems:    Add a "require 'rubygems'" line to the top of each generated file.
        st (Smalltalk):
        xsd (XSD):


     */
    AS3("AS3"),
    C_GLIB("C, using Glib"),
    COCOA("Cocoa"),
    CPP("C++"),
    CSHARP("C#"),
    D("D"),
    DELPHI("delphi"),
    ERL("Erlang"),
    GO("Go"),
    GV("Graphviz"),
    HS("Haskell"),
    HTML("HTML"),
    JAVA("Java"),
    JAVAME("Java ME"),
    JS("Javascript"),
    JSON("JSON"),
    OCAML("OCaml"),
    PERL("Perl"),
    PHP("PHP"),
    PY("Python"),
    RB("Ruby"),
    ST("Smalltalk"),
    XML("XML"),
    XSD("XSD")
    ;
    
    public final String option;
    
    public final String description;
        
    private Generate(String description) {
      this.description = description;
      this.option = name().toLowerCase();
    }

    public static final class Flag implements Comparable<Flag> {
      
      public static final Flag GV_EXCEPTIONS      =  new Flag( GV,  "exceptions",    "Whether to draw arrows from functions to exception.",                false );
      
      public static final Flag HTML_STANDALONE     =  new Flag( HTML,  "standalone",    "Self-contained mode, includes all CSS in the HTML files. Generates no style.css file, but HTML files will be larger.", false);
      
      public static final Flag JAVA_BEANS        =  new Flag( JAVA,  "beans",      "Members will be private, and setter methods will return void.",          false );
      public static final Flag JAVA_PRIVATE_MEMBERS  =  new Flag( JAVA,  "private-members",  "Members will be private, but setter methods will return 'this' like usual.",    false );
      public static final Flag JAVA_NOCAMEL      =  new Flag( JAVA,  "nocamel",      "Do not use CamelCase field accessors with beans.",                  false );
      public static final Flag JAVA_ANDROID_LEGACY  =  new Flag( JAVA,  "android_legacy",  "Do not use java.io.IOException(throwable) (available for Android 2.3 and above).",  false );
      public static final Flag JAVA_JAVA5        =  new Flag( JAVA,  "java5",      "Generate Java 1.5 compliant code (includes android_legacy flag).",          false );
      
      public static final Flag JS_JQUERY        =  new Flag( JS,  "jquery",      "Generate jQuery compatible code",                          false );
      public static final Flag JS_NODE        =  new Flag( JS,  "node",        "Generate node.js compatible code",                          false );
      
      public static final Flag PHP_INLINED       =  new Flag( PHP,  "inlined",       "Generate PHP inlined files",                             false );
      public static final Flag PHP_SERVER       =  new Flag( PHP,  "server",       "Generate PHP server stubs",                             false );
      public static final Flag PHP_AUTOLOAD      =   new Flag( PHP,  "autoload",      "Generate PHP with autoload",                             false );
      public static final Flag PHP_OOP         =  new Flag( PHP,  "oop",         "Generate PHP with object oriented subclasses",                   false );
      public static final Flag PHP_REST         =  new Flag( PHP,  "rest",       "Generate PHP REST processors",                           false );
      public static final Flag PHP_NAMESPACE       =   new Flag( PHP,  "namespace",     "Generate PHP namespaces as defined in PHP >= 5.3",                 false );
      
      public static final Flag RB_RUBYGEMS       =   new Flag( RB,  "rubygems",     "Add a \"require 'rubygems'\" line to the top of each generated file.",       false );

      public static final Flag XML_MERGE = new Flag(XML, "merge", "Generate output with included files merged", false);

      public final String name;

      public final String key;
      
      public final String description;
      
      public final Generate language;
      
      public final boolean requiresValue;
      
      public final String displayName; 
      
      private Flag(
          Generate lang, 
          String key, 
          String description, 
          boolean requiresValue
        ) {
        this.name = makeName(lang, key);
        this.language = lang;
        this.key = key;
        this.description = description;
        this.requiresValue = requiresValue;
        this.displayName = this.language.option + ":" + this.key;
      }

      @Override
      public int compareTo(Flag o) {
        return name.compareTo(o.name);
      }

      @Override
      public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj)
          return true;
        if (obj == null)
          return false;
        if (getClass() != obj.getClass())
          return false;
        Flag other = (Flag) obj;
        if (name == null) {
          if (other.name != null)
            return false;
        } else if (!name.equals(other.name))
          return false;
        return true;
      }

      private static final String makeName(Generate lang, String key) {
        return lang.name().toUpperCase() + "_" + key.toUpperCase();
      }

    }

  }

  public static String getDefaultExecutableName() {
    return System.getProperty("os.name").startsWith("Windows") 
      ? WINDOWS_EXECUTABLE 
      : DEFAULT_EXECUTABLE;
  }
  
  public static String searchPathForThrift() {
    final String path = System.getenv("PATH");
    final String[] parts = Strings.split(path, File.pathSeparatorChar);
    final String executable = getDefaultExecutableName();
    for (String part : parts) {
      final File possible = new File(part, executable);
      if (possible.exists() && possible.canExecute()) {
        return possible.getAbsolutePath();
      }
    }
    return executable;
  }
  
  public static final String WINDOWS_EXECUTABLE = "thrift.exe";
  
  public static final String DEFAULT_EXECUTABLE = "thrift";
    
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
 
  public ThriftCommand(ClientTypeAlias alias) {
    this.language = alias.getLanguage();
    for (final Flag flag : alias.getFlags()) {
      addFlag(flag);
    }
  }

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

  // TODO: convert exceptions to ThriftEE exception heirarchy
  public void addFlag(Flag flag, String value) {
    if (!flag.language.equals(this.language)) {
      throw new IllegalArgumentException(
        "Flag `" + flag.displayName + "` is not applicable for " + 
        this.language.description
      ); 
    }
    if (flag.requiresValue && Strings.isBlank(value)) {
      throw new IllegalArgumentException(
        "Value for `" + flag.displayName + "` requires a value");
    }
    if (!flag.requiresValue && Strings.isNotBlank(value)) {
      throw new IllegalArgumentException(
        "Value for `" + flag.displayName + "` cannot have a value");
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
    if (Strings.isBlank(thriftCommand)) {
      throw new IllegalArgumentException("thriftCommand cannot be blank");
    }
    this.thriftCommand = thriftCommand;
  }
  
  public String getThriftFile() {
    return thriftFile;
  }

  public void setThriftFile(String thriftFile) {
    if (Strings.isBlank(thriftCommand)) {
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
      final Iterator<Entry<Flag, String>> i;
      for (i = generateFlags.entrySet().iterator(); i.hasNext(); ) {
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
    command.add(
      Strings.isBlank(thriftFile) 
        ? "\"<output file>\"" 
        : escape(thriftFile)
    );
    return command;
  }
  
  public String commandString() {
    return Strings.join(command(), ' ');
  }
  
  public List<String> versionCommand() {
    List<String> command = new ArrayList<String>(2);
    command.add(escape(this.thriftCommand));
    command.add("-version");
    return command;
  }
  
  public String versionCommandString() {
    return Strings.join(versionCommand(), ' ');
  }
  
  public List<String> helpCommand() {
    List<String> command = new ArrayList<String>(2);
    command.add(escape(this.thriftCommand));
    command.add("-help");
    return command;
  }
  
  public String helpCommandString() {
    return Strings.join(helpCommand(), ' ');
  }
  
  public String toString() {
    return "ThriftCommand[" + commandString() + "]";
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

}
