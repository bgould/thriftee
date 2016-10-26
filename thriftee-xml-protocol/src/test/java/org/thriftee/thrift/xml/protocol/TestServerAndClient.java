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
package org.thriftee.thrift.xml.protocol;

import java.io.UnsupportedEncodingException;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.thriftee.examples.Examples;

import another.Blotto;
import everything.AThirdException;
import everything.EndOfTheUniverseException;
import everything.Everything;
import everything.SomeOtherException;
import everything.Universe;

public class TestServerAndClient {

  private static final TProtocolFactory fctry = new TXMLProtocol.Factory();

  public static void main(String[] args) throws Exception {

    final String cmd = args.length > 0 ? args[0] : "";
    if ("server".equals(cmd)) {
      final Server server = new Server();
      final Thread serverThread = new Thread(server);
      serverThread.start();
    } else if ("BangClient".equals(cmd)) {
      final BangClient client = new BangClient();
      final Thread clientThread = new Thread(client);
      clientThread.start();
    } else if ("GrokClient".equals(cmd)) {
      final GrokClient client = new GrokClient();
      final Thread clientThread = new Thread(client);
      clientThread.run();
    } else if ("".equals(cmd)){

      final Server server = new Server();
      final Thread serverThread = new Thread(server);
      serverThread.setDaemon(true);
      serverThread.start();

      final GrokClient grokClient = new GrokClient(); {
        final Thread clientThread = new Thread(grokClient);
        clientThread.start();
      }

      final BangClient client = new BangClient(); {
        final Thread clientThread = new Thread(client);
        clientThread.start();
      }

      while (!client.closed() && client.err() == null) {
        Thread.sleep(1);
      }

      while (!grokClient.closed() && grokClient.err() == null) {
        Thread.sleep(1);
      }

      server.stop();
    }

  }

  public static abstract class Client implements Runnable {
    private volatile int count;
    private volatile Throwable err;
    private volatile boolean closed;
    @Override
    public void run() {
      final String prefix = "[" + getClass().getSimpleName() + "] ";
      TTransport transport = null;
      try {
        System.out.println(prefix + "starting client...");
        transport = new TSocket("localhost", 9090);
        System.out.println(prefix + "opening socket...");
        transport.open();
        TProtocol protocol = fctry.getProtocol(transport);
        Universe.Client client = new Universe.Client(protocol);
        for (int i = 0; i < 10; i++) {
          System.err.println(prefix + "sending request " + (count+1) );
          final String response = perform(client);
          System.err.println(prefix +
              "response number " + (++count) + " received: " + response);
//          Thread.sleep((int)(Math.random()*200));
        }
      } catch (Exception x) {
        synchronized(System.err) {
          System.err.println("-------- Error in " + prefix + " client ------------");
          x.printStackTrace();
          System.err.println("-------- End " + prefix + " client error -----------");
        }
        this.err = x;
      } finally {
        if (transport != null) {
          System.out.println(prefix + "closing transport");
          transport.close();
        }
        this.closed = true;
      }
    }
    public int count() {
      return count;
    }
    public Throwable err() {
      return err;
    }
    public boolean closed() {
      return closed;
    }
    protected abstract String perform(final Universe.Client client)
        throws Exception;
    protected static String toJson(TBase<?,?> obj) throws TException {
      final TMemoryBuffer buf = new TMemoryBuffer(0);
      final TSimpleJSONProtocol proto = new TSimpleJSONProtocol(buf);
      obj.write(proto);
      try {
        return buf.toString("UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new TApplicationException(
          TApplicationException.INTERNAL_ERROR, e.getMessage());
      }
    }
  }

  public static class BangClient extends Client {
    @Override
    protected String perform(final Universe.Client client) throws Exception {
      final Everything ev = client.bang(42);
      return toJson(ev);
    }
  }

  public static class GrokClient extends Client {
    @Override
    protected String perform(final Universe.Client client) throws Exception {
      final Everything ev = Examples.everythingStruct();
      try {
        int fortyTwo = client.grok(ev);
        return ""+fortyTwo;
      } catch (EndOfTheUniverseException|SomeOtherException|AThirdException e) {
        System.err.println("[GrokClient] received: " + e);
        return toJson(e);
      }
    }
  }

  public static class UniverseServer implements Universe.Iface {

    @Override
    public Blotto woah(int fortyTwo) throws TException {
      System.err.println("[processor] received blotto(): " + fortyTwo);
      return Examples.blotto();
    }

    @Override
    public void sendIt() throws TException {
      System.err.println("[processor] received sendIt()");
    }

    @Override
    public int grok(Everything everything) throws
        EndOfTheUniverseException, SomeOtherException,
        AThirdException, TException {
      System.err.println("[processor] received grok()");
      throw new EndOfTheUniverseException("its over!!!");
    }

    @Override
    public Everything bang(int fortyTwo) throws TException {
      System.err.println("[processor] received bang(): " + fortyTwo);
      return Examples.everythingStruct();
    }
  }

  public static class Server implements Runnable {

    private TServer server;

    public Server() throws TException {
      final UniverseServer universe = new UniverseServer();
      final TProcessor processor = new Universe.Processor<>(universe);
      TServerTransport serverTransport = new TServerSocket(9090);
      server = new TThreadPoolServer(
        new Args(serverTransport).
//            transportFactory(new TFramedTransport.Factory()).
            processor(processor).protocolFactory(fctry)
      );
    }

    public boolean isStarted() {
      return server.isServing();
    }

    @Override
    public void run() {
      try {
        System.out.println("Starting the simple server...");
        server.serve();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public void stop() {
      System.out.println("Stopping server...");
      server.stop();
    }
  }

}
