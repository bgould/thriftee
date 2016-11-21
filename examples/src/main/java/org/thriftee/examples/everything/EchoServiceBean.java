package org.thriftee.examples.everything;

import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local(EchoService.class)
public class EchoServiceBean implements EchoService {

  @Override
  public Everything echo(Everything everything) {
    return everything;
  }

}
