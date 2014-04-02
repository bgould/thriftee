//
// Autogenerated by Thrift Compiler (1.0.0-dev)
//
// DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
//
var Thrift = require('thrift').Thrift;

var ttypes = module.exports = {};
User = module.exports.User = function(args) {
  this.uid = null;
  this.firstName = null;
  this.lastName = null;
  this.displayName = null;
  this.email = null;
  if (args) {
    if (args.uid !== undefined) {
      this.uid = args.uid;
    }
    if (args.firstName !== undefined) {
      this.firstName = args.firstName;
    }
    if (args.lastName !== undefined) {
      this.lastName = args.lastName;
    }
    if (args.displayName !== undefined) {
      this.displayName = args.displayName;
    }
    if (args.email !== undefined) {
      this.email = args.email;
    }
  }
};
User.prototype = {};
User.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.uid = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.STRING) {
        this.firstName = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 3:
      if (ftype == Thrift.Type.STRING) {
        this.lastName = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 4:
      if (ftype == Thrift.Type.STRING) {
        this.displayName = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 5:
      if (ftype == Thrift.Type.STRING) {
        this.email = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

User.prototype.write = function(output) {
  output.writeStructBegin('User');
  if (this.uid !== null && this.uid !== undefined) {
    output.writeFieldBegin('uid', Thrift.Type.STRING, 1);
    output.writeString(this.uid);
    output.writeFieldEnd();
  }
  if (this.firstName !== null && this.firstName !== undefined) {
    output.writeFieldBegin('firstName', Thrift.Type.STRING, 2);
    output.writeString(this.firstName);
    output.writeFieldEnd();
  }
  if (this.lastName !== null && this.lastName !== undefined) {
    output.writeFieldBegin('lastName', Thrift.Type.STRING, 3);
    output.writeString(this.lastName);
    output.writeFieldEnd();
  }
  if (this.displayName !== null && this.displayName !== undefined) {
    output.writeFieldBegin('displayName', Thrift.Type.STRING, 4);
    output.writeString(this.displayName);
    output.writeFieldEnd();
  }
  if (this.email !== null && this.email !== undefined) {
    output.writeFieldBegin('email', Thrift.Type.STRING, 5);
    output.writeString(this.email);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

Group = module.exports.Group = function(args) {
  this.name = null;
  this.members = null;
  if (args) {
    if (args.name !== undefined) {
      this.name = args.name;
    }
    if (args.members !== undefined) {
      this.members = args.members;
    }
  }
};
Group.prototype = {};
Group.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.name = input.readString();
      } else {
        input.skip(ftype);
      }
      break;
      case 2:
      if (ftype == Thrift.Type.LIST) {
        var _size0 = 0;
        var _rtmp34;
        this.members = [];
        var _etype3 = 0;
        _rtmp34 = input.readListBegin();
        _etype3 = _rtmp34.etype;
        _size0 = _rtmp34.size;
        for (var _i5 = 0; _i5 < _size0; ++_i5)
        {
          var elem6 = null;
          elem6 = new ttypes.User();
          elem6.read(input);
          this.members.push(elem6);
        }
        input.readListEnd();
      } else {
        input.skip(ftype);
      }
      break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

Group.prototype.write = function(output) {
  output.writeStructBegin('Group');
  if (this.name !== null && this.name !== undefined) {
    output.writeFieldBegin('name', Thrift.Type.STRING, 1);
    output.writeString(this.name);
    output.writeFieldEnd();
  }
  if (this.members !== null && this.members !== undefined) {
    output.writeFieldBegin('members', Thrift.Type.LIST, 2);
    output.writeListBegin(Thrift.Type.STRUCT, this.members.length);
    for (var iter7 in this.members)
    {
      if (this.members.hasOwnProperty(iter7))
      {
        iter7 = this.members[iter7];
        iter7.write(output);
      }
    }
    output.writeListEnd();
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

