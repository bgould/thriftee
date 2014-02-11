package org.thriftee.servlet;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.servlet.ServletRequest;

import org.apache.thrift.protocol.TProtocol;

import com.facebook.nifty.core.RequestContext;

public class ServletRequestContext implements RequestContext {

	private final ServletRequest servletRequest;
	
	private final TProtocol protocolIn;
	
	private final TProtocol protocolOut;
	
	public ServletRequestContext(ServletRequest servletRequest,
			TProtocol protocolIn, TProtocol protocolOut) {
		super();
		this.servletRequest = servletRequest;
		this.protocolIn = protocolIn;
		this.protocolOut = protocolOut;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return new InetSocketAddress(servletRequest.getRemoteAddr(), servletRequest.getRemotePort());
	}

	@Override
	public TProtocol getOutputProtocol() {
		return protocolOut;
	}

	@Override
	public TProtocol getInputProtocol() {
		return protocolIn;
	}
	
}
