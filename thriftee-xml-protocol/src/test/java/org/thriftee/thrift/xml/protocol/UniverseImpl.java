package org.thriftee.thrift.xml.protocol;

import org.apache.thrift.TException;

import another.Blotto;
import everything.Everything;
import everything.Universe;

public class UniverseImpl implements Universe.Iface {

  @Override
  public int grok(Everything arg0) throws TException {
    return 42;
  }

  @Override
  public void sendIt() throws TException {
    System.out.println("received oneway message!");
  }

  @Override
  public Blotto woah(int fortyTwo) throws TException {
    if (fortyTwo == 42) {
      Blotto result = new Blotto();
      result.rimple = fortyTwo;
      result.sparticle = "hammelgaff";
    return result;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public Everything bang(int fortyTwo) throws TException {
    return new Everything();
  }

}
