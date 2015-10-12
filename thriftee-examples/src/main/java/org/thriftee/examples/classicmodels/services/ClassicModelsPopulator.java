package org.thriftee.examples.classicmodels.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class ClassicModelsPopulator {

  @PersistenceContext
  private EntityManager em;

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  private final URL sql = getClass().getClassLoader().getResource(
    "META-INF/load_classicmodels.sql"
  );

  @PostConstruct
  public void afterPropertiesSet() throws Exception {
    final Connection conn = em.unwrap(Connection.class);
    final Statement stmt = conn.createStatement();
    final String[] lines = getLines(sql);
    String line = null;
    try {
      final ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CUSTOMERS");
      if (rs.next() && rs.getInt(1) > 0) {
        LOG.info("ClassicModels database appears to be loaded already.");
      } else {
        LOG.info("ClassicModels database appears to be empty... loading.");
      }
      rs.close();
      for (int i = 0; i < lines.length; i++) {
        line = lines[i];
        LOG.debug("executing SQL: {}", line);
        stmt.execute(line);
      }
    } catch (SQLException e) {
      LOG.error("error executing SQL : {} - {}", e.getMessage(), line);
      throw e;
    } finally {
      try {
        stmt.close();
      } catch (Exception e2) {}
    }
  }

  public static String[] getLines(URL url) throws IOException {
    final InputStream is = url.openStream();
    final InputStreamReader reader = new InputStreamReader(is);
    try {
      final StringBuilder sb = new StringBuilder();
      final char[] buffer = new char[1024];
      for (int n; (n = reader.read(buffer)) > -1; ) {
        sb.append(buffer, 0, n);
      }
      return sb.toString().split("\b+;\b+", Pattern.MULTILINE);
    } finally {
      try {
        is.close();
      } catch (IOException e) {}
    }
  }

}
