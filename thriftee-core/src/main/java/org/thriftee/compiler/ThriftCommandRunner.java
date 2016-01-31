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

import static org.thriftee.compiler.ThriftCommandException.ThriftCommandMessage.*;

import java.io.IOException;
import java.util.List;

import org.thriftee.thrift.compiler.ExecutionResult;
import org.thriftee.thrift.compiler.ThriftCompiler;

public class ThriftCommandRunner {

  private static final ThriftCompiler compiler = ThriftCompiler.newCompiler();

  private final ThriftCommand thriftCommand;

  public static ThriftCommandRunner instanceFor(ThriftCommand cmd) {
    return new ThriftCommandRunner(cmd);
  }

  ThriftCommandRunner(ThriftCommand thriftCommand) {
    this.thriftCommand = thriftCommand;
  }

  public ExecutionResult executeCommand() throws ThriftCommandException {
    final ExecutionResult result;
    try {
      result = execute(thriftCommand.command());
    } catch (IOException e) {
      throw new ThriftCommandException(e, COMMAND_301, e.getMessage());
    }
    if (result.interrupted) {
      throw new ThriftCommandException(COMMAND_303, result);
    } else if (result.exitCode != 0) {
      throw new ThriftCommandException(COMMAND_302, result);
    }
    return result;
  }

  public String executeVersion() throws ThriftCommandException {
    return compiler.version();
  }

  public String executeHelp() throws ThriftCommandException {
    return compiler.help();
  }

  ExecutionResult execute(final List<String> command) throws IOException {
    command.remove(0);
    return compiler.execute(command.toArray(new String[command.size()]));
  }

}
