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
package org.thriftee.thrift.xml;

import java.io.IOException;

import net.sf.saxon.s9api.XsltTransformer;

public class SimpleToStreamingTransformation extends Transformation {

  SimpleToStreamingTransformation(Transforms transforms) {
    super(transforms);
  }

  @Override
  protected XsltTransformer newTransformer() throws IOException {
    final XsltTransformer result = transforms.newSimpleToStreamingTransformer();
    result.setParameter(q("schema"), urlval(getModelFile()));
    result.setParameter(q("root_module"), strval(getModule()));
    return result;
  }

}
