package org.thriftee.framework;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.thriftee.framework.ThriftCommand;
import org.thriftee.framework.ThriftCommand.Generate;

public class ThriftCommandTest {

	@Test
	public void testAddsAllow64bitConsts() {
		ThriftCommand cmd = fakeCommand();
		cmd.setAllow64bitConsts(true);
		List<String> command = cmd.command();
		
		System.out.println("testAddsAllow64bitConsts: " + cmd.commandString());
				
		assertTrue(cmd.isAllow64bitConsts());
		assertTrue("command contains --allow-64bit-consts", command.contains("--allow-64bit-consts"));
	}
	
	@Test
	public void testAddsAllowNegativeFieldKeys() {
		ThriftCommand cmd = fakeCommand();
		cmd.setAllowNegativeFieldKeys(true);
		List<String> command = cmd.command();
		
		System.out.println("testAddsAllowNegativeFieldKeys: " + cmd.commandString());
				
		assertTrue(cmd.isAllowNegativeFieldKeys());
		assertTrue("command contains --allow-neg-keys", command.contains("--allow-neg-keys"));
	}
	
	@Test
	public void testAddsDebug() {
		ThriftCommand cmd = fakeCommand();
		cmd.setDebug(true);
		List<String> command = cmd.command();
		
		System.out.println("testAddsDebug: " + cmd.commandString());
				
		assertTrue(cmd.isDebug());
		assertTrue("command should contain debug flag", command.contains("-debug"));
	}
	
	@Test
	public void testAddsNoWarnFlag() {
		ThriftCommand cmd = fakeCommand();
		cmd.setNoWarn(true);
		List<String> command = cmd.command();
		
		System.out.println("testAddsNoWarn: " + cmd.commandString());
				
		assertTrue(cmd.isNoWarn());
		assertTrue("command should contain nowarn flag", command.contains("-nowarn"));
	}
	
	@Test
	public void testAddsRecurse() {
		ThriftCommand cmd = fakeCommand();
		cmd.setRecurse(true);
		List<String> command = cmd.command();
		
		System.out.println("testAddsRecurse: " + cmd.commandString());
				
		assertTrue(cmd.isRecurse());
		assertTrue("command should contain recurse flag", command.contains("-recurse") || command.contains("-r"));
	}
	
	@Test
	public void testAddsStrict() {
		ThriftCommand cmd = fakeCommand();
		cmd.setStrict(true);
		List<String> command = cmd.command();
		
		System.out.println("testAddsStrict: " + cmd.commandString());
				
		assertTrue(cmd.isStrict());
		assertTrue("command should contain strict flag", command.contains("-strict"));
	}
	
	@Test
	public void testAddsVerbose() {
		ThriftCommand cmd = fakeCommand();
		cmd.setVerbose(true);
		List<String> command = cmd.command();
		
		System.out.println("testAddsVerbose: " + cmd.commandString());
				
		assertTrue(cmd.isVerbose());
		assertTrue("command contains verbose flag", command.contains("-verbose") || command.contains("-v"));
	}
	
	@Test
	public void testGeneratePhp() {
		ThriftCommand cmd = fakeCommand();
		cmd.addFlag(Generate.Flag.PHP_NAMESPACE);
		cmd.addFlag(Generate.Flag.PHP_OOP);
		
		System.out.println("testGeneratePhp: " + cmd.commandString());
		
		assertTrue("oop flag should be included", cmd.generateString().contains("oop"));
		assertTrue("namespace flag should be included", cmd.generateString().contains("namespace"));
	}
	
	protected ThriftCommand fakeCommand() {
		return new ThriftCommand(Generate.PHP, "fake.thrift");
	}
	
}
