package org.thriftee.framework;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.thriftee.framework.ExportIDL;

import test1.domain.Group;
import test1.domain.User;
import test1.service.GroupService;
import test1.service.UserGroupException;
import test1.service.UserService;

public class ExportIDLTest {

	public static final Set<Class<?>> TEST_CLASSES;
	static {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(User.class);
		classes.add(Group.class);
		classes.add(UserGroupException.class);
		classes.add(UserService.class);
		classes.add(GroupService.class);
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
