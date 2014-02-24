package org.thriftee.servlet;

import java.net.SocketAddress;

import org.apache.thrift.protocol.TProtocol;
import org.jboss.netty.channel.local.LocalAddress;

import com.facebook.nifty.core.RequestContext;

public class TestRequestContext implements RequestContext {

	private final TProtocol inProtocol;
	
	private final TProtocol outProtocol;
	
	private final SocketAddress remoteAddress = new LocalAddress(1);
	
	public TestRequestContext(TProtocol in, TProtocol out) {
		this.inProtocol = in;
		this.outProtocol = out;
	}
	
	@Override
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public TProtocol getOutputProtocol() {
		return outProtocol;
	}

	@Override
	public TProtocol getInputProtocol() {
		return inProtocol;
	}

}
