package org.thriftee.servlet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.framework.ServiceLocator;
import org.thriftee.framework.ServiceLocatorException;
import org.thriftee.framework.ServiceLocatorException.ServiceLocatorMessage;
import org.thriftee.util.New;
import org.thriftee.util.Strings;

public class DefaultEJBServiceLocator implements ServiceLocator {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private boolean searchAllModules;

  private Set<String> modulesToSearch = Collections.emptySet();

  public DefaultEJBServiceLocator() {
  }

  public void setSearchAllModules(boolean searchAllModules) {
    this.searchAllModules = searchAllModules;
  }

  public boolean isSearchAllModules() {
    return this.searchAllModules;
  }

  private static String normalizeModuleName(String moduleName) {
    moduleName = Strings.trimToNull(moduleName);
    if (moduleName == null) {
      // TODO: maybe there is a use case for this... waiting for complaints
      throw new IllegalArgumentException("moduleName cannot be empty");
    }
    if (!moduleName.endsWith("/")) {
      moduleName += "/";
    }
    if (!moduleName.startsWith("/")) {
      moduleName = "/" + moduleName;
    }
    return moduleName;
  }

  public void setModuleToSearch(String moduleName) {
    final Set<String> names = new HashSet<String>();
    names.add(moduleName);
    setModulesToSearch(names);
  }

  public void setModulesToSearch(final Set<String> modulesToSearch) {
    if (modulesToSearch == null) {
      this.modulesToSearch = Collections.emptySet();
    } else {
      final Set<String> names = new HashSet<String>();
      for (final String name : modulesToSearch) {
        names.add(normalizeModuleName(name));
      }
      this.modulesToSearch = names;
    }
  }

  public Set<String> getModulesToSearch() {
    return Collections.unmodifiableSet(this.modulesToSearch);
  }

  private Set<String> findNamesForInterface(
      final InitialContext ctx, 
      final String moduleName, 
      final Class<?> svcIntf 
    ) throws NamingException {
    final String prefix = "java:global" + moduleName;
    final NamingEnumeration<NameClassPair> ne = ctx.list(prefix);
    final Set<String> matches = new HashSet<String>();
    while (ne.hasMoreElements()) {
      final NameClassPair ncp = ne.nextElement();
      LOG.trace("  jndi entry: {}", ncp);
      final String name = ncp.getName();
      if (name.endsWith("!" + svcIntf.getName())) {
        matches.add(prefix + name);
      }
    }
    return matches;
  }

  private Set<String> getGlobalModuleNames(
      InitialContext ctx
    ) throws NamingException {
    final String prefix = "java:global";
    final NamingEnumeration<NameClassPair> ne = ctx.list(prefix);
    final Set<String> matches = new HashSet<String>();
    while (ne.hasMoreElements()) {
      final NameClassPair ncp = ne.nextElement();
      final String name = ncp.getName();
      LOG.trace("  jndi module: {}", ncp);
      matches.add(normalizeModuleName(name));
    }
    return matches;
  }

  private Map<String, Set<String>> implMap(
      InitialContext ctx, 
      Class<?> intf
    ) throws NamingException {
    final Set<String> modulesToSearch;
    if (isSearchAllModules()) {
      modulesToSearch = getGlobalModuleNames(ctx);
    } else {
      modulesToSearch = getModulesToSearch();
    }
    final Map<String, Set<String>> matchesInModules = New.sortedMap(); 
    for (final String name : modulesToSearch) {
      final Set<String> matches = findNamesForInterface(ctx, name, intf);
      matchesInModules.put(name, matches);
    }
    return matchesInModules; 
  }

  @Override
  public <I> I locate(Class<I> svcIntf) throws ServiceLocatorException {
    if (!isSearchAllModules() && getModulesToSearch().isEmpty()) {
      throw new IllegalStateException(
        "Either searchAllModules must be set to true, " +
        "or modulesToSearch must be specified."
      );
    }
    final InitialContext ic;
    final Map<String, Set<String>> matches;
    try {
      ic = new InitialContext();
      matches = implMap(ic, svcIntf);
    } catch (NamingException e) {
      throw new ServiceLocatorException(e, ServiceLocatorMessage.SVCLOC_000);
    }
    final Set<String> allMatches = new HashSet<>();
    for (String moduleName : matches.keySet()) {
      allMatches.addAll(matches.get(moduleName));
    }
    if (allMatches.isEmpty()) {
      return null;
    }
    if (allMatches.size() == 1) {
      final String jndiName = allMatches.iterator().next();
      final I result;
      try {
        @SuppressWarnings("unchecked")
        final I bean = (I) ic.lookup(jndiName);
        result = bean;
      } catch (NamingException e) {
        throw new ServiceLocatorException(e, ServiceLocatorMessage.SVCLOC_000);
      }
      return result;
    }
    // TODO: make this more intelligent
    throw new IllegalStateException(
      "More than one implementation found for interface: " + svcIntf);
    /*
    final ThriftService annotation = svcIntf.getAnnotation(ThriftService.class);
    if (annotation == null) {
      throw new IllegalArgumentException("Service interface not annotated.");
    }
    final String prefix = "java:global/" + moduleName;
    debugJNDI(prefix);
    final String name = annotation.value();
    final String svcName = (StringUtils.trimToNull(name) == null) ? svcIntf.getSimpleName() : name;
    final String jndiName = prefix + svcName + "Bean";
    final I result;
    try {
      @SuppressWarnings("unchecked")
      final I bean = (I) ic.lookup(jndiName);
      result = bean;
    } catch (NamingException e) {
      throw new ServiceLocatorException(e, ServiceLocatorMessage.SVCLOC_000);
    }
    return result;
  }

  private void debugJNDI(String prefix) {
    //if (!LOG.isDebugEnabled()) {
    //  return;
    //}
    try {
      LOG.info("Showing JNDI prefix: {}", prefix);
      final InitialContext ic = new InitialContext();
      final NamingEnumeration<NameClassPair> ne = ic.list(prefix);
      while (ne.hasMoreElements()) {
        final NameClassPair ncp = ne.nextElement();
        LOG.info("  NameClassPair: {}", ncp);
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    */
  }

}
