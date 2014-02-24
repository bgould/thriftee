//package org.thriftee.servlet;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.thrift.protocol.TJSONProtocol;
//import org.apache.thrift.protocol.TProtocol;
//import org.apache.thrift.protocol.TProtocolFactory;
//import org.apache.thrift.transport.TMemoryBuffer;
//import org.junit.Assert;
//import org.junit.Test;
//import org.thriftee.compiler.ExportIDL;
//import org.thriftee.compiler.ProcessIDL;
//import org.thriftee.compiler.ThriftCommand;
//import org.thriftee.compiler.ThriftCommand.Generate;
//import org.thriftee.examples.usergroup.service.GroupServiceImpl;
//import org.thriftee.examples.usergroup.service.UserServiceImpl;
//import org.thriftee.util.ExportIDLTest;
//
//import com.facebook.nifty.core.RequestContext;
//import com.facebook.swift.codec.ThriftCodecManager;
//import com.facebook.swift.service.ThriftEventHandler;
//import com.facebook.swift.service.ThriftServiceProcessor;
//import com.google.common.util.concurrent.ListenableFuture;
//
//public class EndpointServletTest {
//
//	private final UserServiceImpl userService = new UserServiceImpl();
//	
//	private final GroupServiceImpl groupService = new GroupServiceImpl(userService);
//	
//	public static final String JSON_FIND_1 = "[1,\"find\",1,1,{\"1\":{\"str\":\"aaardvark\"}}]";
//	
//	//private UserGroupEndpointServlet servlet = new UserGroupEndpointServlet(userService, groupService);
//	
//	//@Test
//	public void testBinaryProtocol() throws Exception {
//		//File temp = ExportIDLTest.getTempDirForTest();
//		//processIDL(temp);
//		
//		/*
//		TProtocolFactory factory = new TJSONProtocol.Factory();
//		TMemoryBuffer transport = new TMemoryBuffer(1024 * 512);
//		TProtocol protocol = factory.getProtocol(transport);
//		
//		try {
//			UserService.Client client = new UserService.Client(protocol);
//			client.find("aaardvark");
//		} catch (TApplicationException e) {}
//		
//		System.out.println("message: " + new String(transport.getArray()));
//		*/
//		
//		
//		TProtocolFactory factory = new TJSONProtocol.Factory();
//		
//		TMemoryBuffer inTransport = new TMemoryBuffer(512);
//		inTransport.write(JSON_FIND_1.getBytes());
//		TMemoryBuffer outTransport = new TMemoryBuffer(512);
//		
//		TProtocol inProtocol = factory.getProtocol(inTransport);
//		TProtocol outProtocol = factory.getProtocol(outTransport);
//		RequestContext ctx = new TestRequestContext(inProtocol, outProtocol);
//
//		ThriftCodecManager codecManager = new ThriftCodecManager();
//		List<ThriftEventHandler> eventList = Collections.emptyList();
//		ThriftServiceProcessor proc = new ThriftServiceProcessor(codecManager, eventList, userService);
//		
//		ListenableFuture<Boolean> processResult = proc.process(inProtocol, outProtocol, ctx);
//		Boolean result = processResult.get(30, TimeUnit.SECONDS);
//		
//		Assert.assertTrue(result);
//		System.out.println("result: " + new String(outTransport.getArray()));
//		
//	}
//	
//	@Test
//	public void testJSONProtocol() {
//		
//	}
//	
//	public static void processIDL(File temp) throws IOException {
//		File[] idlFiles = exportIDL(temp);
//		ThriftCommand cmd = new ThriftCommand(Generate.JAVA);
//		//cmd.addFlag(Flag.JAVA_BEANS);
//		ProcessIDL processor = new ProcessIDL();
//		processor.process(idlFiles, temp, "java-library", cmd);
//	}
//	
//	private static File[] exportIDL(File temp) {
//		try {
//			ExportIDL exporter = new ExportIDL();
//			return exporter.export(temp, ExportIDLTest.TEST_CLASSES);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//}
