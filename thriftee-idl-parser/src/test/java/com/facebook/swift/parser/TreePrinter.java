/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.swift.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.tree.Tree;

public class TreePrinter
{
  public static String treeToString(Tree tree)
  {
    return treeToString(tree, 1);
  }

  private static String treeToString(Tree tree, int depth)
  {
    if (tree.getChildCount() == 0) {
      return tree.toString();
    }
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(tree.toString());
    for (Tree t : children(tree)) {
      if (leafCount(tree) > 2) {
        sb.append("\n");
        for (int i = 0; i < depth; i++) sb.append("   ");
      }
      else {
        sb.append(" ");
      }
      sb.append(treeToString(t, depth + 1));
    }
    sb.append(")");
    return sb.toString();
  }

  private static int leafCount(Tree tree)
  {
    if (tree.getChildCount() == 0) {
      return 1;
    }

    int n = 0;
    for (Tree t : children(tree)) {
      n += leafCount(t);
    }
    return n;
  }

  private static List<Tree> children(Tree tree)
  {
    ArrayList<Tree> list = new ArrayList<Tree>();
    for (int i = 0; i < tree.getChildCount(); i++) {
      list.add(tree.getChild(i));
    }
    return Collections.unmodifiableList(list);
  }
}
