package org.thriftee.examples.presidents;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.thriftee.framework.ExportIDL;

public class PresidentServiceTest {

	public static final Set<Class<?>> TEST_CLASSES;
	static {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(President.class);
		classes.add(SortOrder.class);
		classes.add(Sort.class);
		classes.add(Filter.class);
		classes.add(Name.class);
		classes.add(PresidentFilter.class);
		classes.add(PresidentSort.class);
		classes.add(PresidentService.class);
		TEST_CLASSES = Collections.unmodifiableSet(classes);
	}
	
	public static File getTempDirForTest() {
		StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
		String className = stackTraceElement.getClassName();
		String simpleName = className.substring(className.lastIndexOf('.') + 1);
		File tempDir = new File("target/tests/" + simpleName);
		File retval = new File(tempDir, stackTraceElement.getMethodName());
		return retval;
	}
	
	@Test
	public void testExport() throws IOException {
		File temp = getTempDirForTest();
		ExportIDL exporter = new ExportIDL();
		exporter.export(temp, TEST_CLASSES);
	}
	
}
