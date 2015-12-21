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
package org.thriftee.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.thriftee.compiler.ExportIDL;
import org.thriftee.examples.usergroup.domain.Group;
import org.thriftee.examples.usergroup.domain.User;
import org.thriftee.examples.usergroup.service.GroupService;
import org.thriftee.examples.usergroup.service.UserGroupException;
import org.thriftee.examples.usergroup.service.UserService;

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
        String prefix = System.getProperty("thriftee.build.dir", "target");
        File tempDir = new File(prefix + "/tests/" + simpleName);
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
