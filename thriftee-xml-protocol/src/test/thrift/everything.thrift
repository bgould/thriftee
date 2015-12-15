namespace java everything

namespace xsd everything

include "nothing_all_at_once.thrift"

typedef i32 dukk
typedef Sprat poig
typedef Spirfle plorp
typedef nothing_all_at_once.Blotto hammlegaff
typedef set<string> setdef
typedef list<string> listdef
typedef map<string, Spirfle> mapdef

enum Spinkle {
  HRRR, PPOL, REWT
}

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

struct Everything {
  1:  string str;
  2:  i64 int64;
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
}

exception EndOfTheUniverseException {
  1: string msg;
}

service Universe extends nothing_all_at_once.Metaverse {
  i32 grok(1: Everything everything) throws (1:EndOfTheUniverseException endOfIt),
  oneway void sendIt(),
  Everything bang(1: i32 fortyTwo);
}
