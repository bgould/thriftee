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
