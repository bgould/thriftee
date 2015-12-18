package org.thriftee.thrift.xml;

import java.io.IOException;

import net.sf.saxon.s9api.XsltTransformer;

public class StreamingToSimpleTransformation extends Transformation {

  StreamingToSimpleTransformation(Transforms transforms) {
    super(transforms);
  }

  @Override
  protected XsltTransformer newTransformer() throws IOException {
    final XsltTransformer result = transforms.newStreamingToSimpleTransformer();
    result.setParameter(q("schema"), urlval(getModelFile()));
    result.setParameter(q("root_module"), strval(getModule()));
    switch (rootType) {
    case MESSAGE:
      result.setParameter(q("service_name"), strval(rootName));
      break;
    case STRUCT:
      result.setParameter(q("root_struct"), strval(rootName));
      break;
    default:
      throw new IllegalStateException("rootType must be set.");
    }
    return result;
  }

}
