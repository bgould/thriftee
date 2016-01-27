package org.thriftee.examples;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import another.Blotto;
import everything.EndOfTheUniverseException;
import everything.Everything;
import everything.Spinkle;
import everything.Spirfle;
import everything.Sprat;
import everything.Universe.grok_args;
import everything.Universe.grok_result;

public class Examples {

  public static Everything everythingStruct() {
    Everything everything = new Everything();
    everything.bite = 42;
    everything.int32 = 64000;
    everything.int16 = 1024;
    everything.int64 = 10000000000L;
    everything.str = "foobar";
    everything.dbl = 10.4;
    everything.bin = ByteBuffer.wrap("secret_password".getBytes());
    everything.onion = Sprat.wowzer(1337);
    everything.setReally(true);

    final Map<String, String> str_str_map = new HashMap<String, String>();
    str_str_map.put("foo", "bar");
    str_str_map.put("graffle", "florp");
    everything.str_str_map = str_str_map;

    final List<String> str_list = new ArrayList<String>();
    str_list.add("wibble");
    str_list.add("snork");
    str_list.add("spiffle");
    everything.str_list = str_list;

    final List<Spinkle> enum_list = new ArrayList<Spinkle>();
    enum_list.add(Spinkle.HRRR);
    enum_list.add(Spinkle.REWT);
    everything.enum_list = enum_list;

    final List<Spirfle> obj_list = new ArrayList<Spirfle>();
    obj_list.add(new Spirfle("blat", 17, Spinkle.HRRR, 1, null, null));
    obj_list.add(new Spirfle("yarp", 89, Spinkle.REWT, 2, null, null));
    obj_list.add(new Spirfle("trop", 9, null, 3, null, null));
    everything.obj_list = obj_list;

    final Map<Integer, Spirfle> int_obj_map = new LinkedHashMap<>();
    for (int i = 0, c = obj_list.size(); i < c; i++) {
      int_obj_map.put(i + 1, obj_list.get(i));
    }
    everything.int_obj_map = int_obj_map;

    everything.obj = obj_list.get(0);
    everything.obj_set = new LinkedHashSet<>(obj_list);
    everything.str_set = new LinkedHashSet<>(str_list);

    final List<List<Integer>> int_list_list = new ArrayList<>();
    int_list_list.add(Arrays.asList(new Integer[] { 1, 2, 3, 4, 5 }));
    int_list_list.add(Arrays.asList(new Integer[] { 1, 1, 3, 5 }));
    everything.int_list_list = int_list_list;

    everything.smork = new Blotto(42, "happelsmack");

    Map<Spinkle, List<Spirfle>> enum_list_map = new HashMap<>();
    List<Spirfle> spirfles = new ArrayList<>();
    spirfles.add(new Spirfle("fink", 2, null, 34, null, null));
    enum_list_map.put(Spinkle.HRRR, spirfles);
    everything.enum_list_map = enum_list_map;

    everything.empty = "";

    return everything;
  }


  public static grok_result grokResult() {
    return new grok_result(42, null);
  }

  public static grok_result grokError() {
    final grok_result result = new grok_result();
    result.setEndOfIt(new EndOfTheUniverseException("it's over!!!"));
    return result;
  }

  public static grok_args grokArgs() {
    return new grok_args(everythingStruct());
  }

  public static Blotto blotto() {
    return new Blotto(42, "fish");
  }

}
