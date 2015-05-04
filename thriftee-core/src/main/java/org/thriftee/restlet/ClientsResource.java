package org.thriftee.restlet;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.thriftee.framework.ClientTypeAlias;
import org.thriftee.util.New;

public class ClientsResource extends FrameworkResource {

  @Get
  public Representation get() {

    final String title = "Available Thrift Clients";
    final DirectoryListingModel directory = new DirectoryListingModel();
    directory.setTitle(title);
    directory.setBaseRef(".");
    for (final ClientTypeAlias alias : thrift().clientTypeAliases().values()) {
      directory.getFiles().put(alias.getName() + "/", alias.getName() + "/");
    }
 
    final Map<String, Object> model = New.map();
    model.put("title", title);
    model.put("aliases", thrift().clientTypeAliases());
    model.put("directory", directory);
    return getTemplate("clients", model, MediaType.TEXT_HTML);

  }

/*
  @Override
  protected void doInit() throws ResourceException {
    LOG.debug("entering doInit()");
    super.doInit();
    final Router clientRouter = new Router();
    for (final ClientTypeAlias alias : thrift().clientTypeAliases().values()) {
      final String name = alias.getName();
      final String uri = thrift().clientLibraryDir(name).toURI().toString();
      clientRouter.attach("/" + name + "/", new Directory(getContext(), uri));
    }
    
    LOG.debug("leaving doInit()");
  }
  public void generateClientLibrary(ClientTypeAlias alias) {
    final String name = alias.getName();
    LOG.info("Generating library for client type alias: {}", name);
    try {
        ThriftCommand cmd = new ThriftCommand(alias);
        cmd.setRecurse(true);
        cmd.setVerbose(true);
        if (thrift().thriftExecutable() != null) {
          cmd.setThriftCommand(thrift().thriftExecutable().getAbsolutePath());
        }
        final File[] extraDirs;
        /* TODO: add facility for adding in extra lib dirs */
        /*
        if (thrift().thriftLibDir() != null) {
            File jsLib = new File(thrift().thriftLibDir(), "js/src");
            extraDirs = new File[] { jsLib };
        } else {
            extraDirs = new File[0];
        }
        */
/*
        extraDirs = new File[0];
        final File clientLibrary = new ProcessIDL().process(
            new File[] { thrift().globalIdlFile() }, 
            thrift().tempDir(), 
            "client-" + alias.getName(), 
            cmd, 
            extraDirs
        );
        final String path = clientLibrary.getAbsolutePath();
        LOG.info("{} client library created at: {}", name, path);
    } catch (IOException e) {
        throw new RuntimeException(String.format(
          "Problem generating %s library: %s", alias.getName(), e.getMessage()
        ), e);
    }
  }
*/

}
