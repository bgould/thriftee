package org.thriftee.compiler;

import static org.thriftee.compiler.ThriftCommandException.ThriftCommandMessage.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.util.FileUtil;

public class ThriftCommandRunner {

    private final ThriftCommand thriftCommand;
    
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    
    public static ThriftCommandRunner instanceFor(ThriftCommand cmd) {
        return new ThriftCommandRunner(cmd);
    }
    
    ThriftCommandRunner(ThriftCommand thriftCommand) {
        this.thriftCommand = thriftCommand;
    }
    
    public String executeVersion() throws ThriftCommandException {
        ExecutionResult result = null;
        try {
            result = execute(thriftCommand.versionCommand());
        } catch (IOException e) {
            throw new ThriftCommandException(e, COMMAND_101, result);
        }
        if (result.interrupted) {
            throw new ThriftCommandException(COMMAND_103, result);
        } else if (result.exitCode != 0) {
            throw new ThriftCommandException(COMMAND_102, result);
        }
        if (StringUtils.isNotBlank(result.errString())) {
            LOG.warn("output on stderr for Thrift 'version' command: {}", result.errString());
        }
        return result.outString();
    }
    
    public String executeHelp() throws ThriftCommandException {
        ExecutionResult result = null;
        try {
            result = execute(thriftCommand.helpCommand());
        } catch (IOException e) {
            throw new ThriftCommandException(e, COMMAND_201, result);
        }
        if (result.interrupted) {
            throw new ThriftCommandException(COMMAND_203, result);
        } else if (result.exitCode != 0) {
            throw new ThriftCommandException(COMMAND_202, result);
        }
        if (StringUtils.isNotBlank(result.errString())) {
            LOG.warn("output on stderr for Thrift 'help' command: {}", result.errString());
        }
        return result.outString();
    }
    
    ExecutionResult execute(final List<String> command) throws IOException {
        
        LOG.debug("executing: " + command);
        
        final File stdout = File.createTempFile("thriftee_", ".stdout");
        final File stderr = File.createTempFile("thriftee_", ".stderr");
        final ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(stdout);
        pb.redirectError(stderr);
        
        LOG.debug(
            "standard stream files: [stdout] {} (exists: {}), [stderr] {} (exists: {})", 
            stdout, stdout.exists(), 
            stderr, stderr.exists()
        );
        
        String outString = null;
        String errString = null;
        int exit = Integer.MIN_VALUE;
        boolean interrupted = false;
        boolean processDestroyed = false;
        
        final Process process = pb.start();
        LOG.debug("command started");
        try {
            exit = process.waitFor();
            process.destroy();
            processDestroyed = true;
            LOG.debug("command completed: {}", exit);
            outString = FileUtil.readAsString(stdout);
            errString = FileUtil.readAsString(stderr);
        } catch (InterruptedException e) {
            interrupted = true;
        } finally {
            if (!processDestroyed) {
                process.destroy();
            }
            stdout.delete();
            stderr.delete();
        }
        LOG.debug("returning execution result");
        return new ExecutionResult(exit, interrupted, outString, errString);
    }
    
    static final class ExecutionResult {

        private final String outString;
        private final String errString;
        private final int exitCode;
        private final boolean interrupted;
        
        ExecutionResult(int exitCode, boolean interrupted, String outString, String errString) {
            this.outString = outString;
            this.errString = errString;
            this.exitCode = exitCode;
            this.interrupted = interrupted;
        }
        
        String outString() {
            return this.outString;
        }
        
        String errString() {
            return this.errString;
        }
        
        int exitCode() {
            return this.exitCode;
        }
        
        boolean interrupted() {
            return this.interrupted;
        }
        
        boolean successful() {
            return this.exitCode == 0 && !this.interrupted;
        }
        
    }
    
}
