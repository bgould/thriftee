/** this is just a test IDL file for trying out things with the XML protocol */

namespace java everything
namespace xml evrything // (targetNamespace = "http://example.com/ns", test = "any")

include "nothing_all_at_once.thrift"

const byte     I8_CONST  = 42
const i64    I64_CONST = 42000000000
const i32    INT_CONST = 42
const string STR_CONST = "test constant string"
const double DBL_CONST = 42.0

/** this is a set const */
const set<string> SET_CONST  = [ 'test1', 'test2', 'test3' ]

/** an enum doc */
enum Spinkle {
  HRRR, /** this is the second member */ PPOL /*(order = "second")*/, REWT
}

/** this is a list const */
const list<string> LIST_CONST = [ 'test3', 'test5', 'test6' ]
const map<string, string> MAP_CONST  = { 'test7': 'test8', 'test9' : 'test10' }
const map<i32, map<string, string>> MAP_MAP_CONST  = { 
  42 : { 'test7': 'test8', 'test9' : 'test10' },
  43 : { 'test7': 'test8', 'test9' : 'test10' }
}
const map<Spinkle, string> ENUM_MAP_CONST = { Spinkle.PPOL : "test" }

typedef i32 dukk
typedef i32 int32
typedef Sprat poig
typedef Spirfle plorp
/**
 * this goes with a typedef.
 * also it has line breaks.
 */
typedef nothing_all_at_once.Blotto hammlegaff //(it.also = 'has.annotations')
typedef set<string> setdef
typedef list<string> listdef
typedef map<string, Spirfle> mapdef
typedef list<map<int32, string>> biglist

/** some union doc & "stuff" */
union Sprat {
  1: string woobie;
  2: i32 wowzer;
  3: Spinkle wheee;
}

struct Spirfle {
  1: string giffle;
  2: i32 flar;
  3: Spinkle spinkle;
  4: dukk spoot;
  5: poig sprat;
  6: hammlegaff blotto;
}

/**
  This struct has a bunch of different fields
 */
struct Everything {
  1:  required string str = "default" (field.annot = 'true');
  2:  optional i64 int64;
  3:  i32 int32;
  4:  i16 int16;
  5:  byte bite;
  6:  double dbl;
  7:  binary bin;
  8:  Spinkle enu;
  9:  Sprat onion;
  10: list<string> str_list;
  11: list<Spinkle> enum_list;
  12: list<Spirfle> obj_list;
  13: list<list<i32>> int_list_list;
  14: map<string, string> str_str_map;
  15: map<i32, string> int_str_map;
  16: map<i32, Spirfle> int_obj_map;
  17: Spirfle obj;
  18: set<string> str_set;
  19: set<Spirfle> obj_set;
  20: nothing_all_at_once.Blotto smork;
  21: map<Spinkle, list<Spirfle>> enum_list_map;
  22: optional bool really = true;
  23: string empty;
  24: int32 someint;
  25: poig someobj;
  26: hammlegaff someobj2;
  /*
  27: setdef someset;
  28: mapdef somemap;
  29: listdef somelist;
  30: biglist listtype;
  */
} (struct.annot = 'true')

/** trying out an exception */
exception EndOfTheUniverseException {
  1: string msg;
}

exception SomeOtherException {
  1: string msg;
}

exception AThirdException {
  1: string msg;
}

/** this service has some documentation */
service Universe extends nothing_all_at_once.Metaverse {
  i32 grok(1: Everything everything) throws (0: EndOfTheUniverseException endOfIt, -6: SomeOtherException another, -4: AThirdException craziness),
  /** this is oneway so should have a void result */
  oneway void sendIt(),
  Everything bang(1: i32 fortyTwo);
}
