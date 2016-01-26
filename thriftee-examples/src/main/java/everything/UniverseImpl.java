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
package everything;

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
