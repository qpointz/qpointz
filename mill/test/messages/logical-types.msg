schema {
  fields {
    name: "BigInt"
    type {
      type {
        typeId: BIG_INT
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Binary"
    fieldIdx: 1
    type {
      type {
        typeId: BINARY
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Boolean"
    fieldIdx: 2
    type {
      type {
        typeId: BOOL
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Date"
    fieldIdx: 3
    type {
      type {
        typeId: DATE
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Double"
    fieldIdx: 4
    type {
      type {
        typeId: DOUBLE
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Float"
    fieldIdx: 5
    type {
      type {
        typeId: FLOAT
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Int"
    fieldIdx: 6
    type {
      type {
        typeId: INT
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "SmallInt"
    fieldIdx: 7
    type {
      type {
        typeId: SMALL_INT
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "String"
    fieldIdx: 8
    type {
      type {
        typeId: STRING
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Time"
    fieldIdx: 9
    type {
      type {
        typeId: TIME
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "Timestamp"
    fieldIdx: 10
    type {
      type {
        typeId: TIMESTAMP
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "TimestampTZ"
    fieldIdx: 11
    type {
      type {
        typeId: TIMESTAMP_TZ
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "TinyInt"
    fieldIdx: 12
    type {
      type {
        typeId: TINY_INT
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
  fields {
    name: "GUId"
    fieldIdx: 13
    type {
      type {
        typeId: UUID
        precision: -1
        scale: -1
      }
      nullability: NULL
    }
  }
}
vectorSize: 5
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: true
  }
  i64Vector {
    values: 1
    values: 0
    values: -9223372036854775808
    values: 9223372036854775807
    values: 0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: true
  }
  byteVector {
    values: "d\f\234\f\352\026"
    values: ""
    values: ""
    values: "\337\005\211U\335\006\023P\204,\305\272z Ekh\253`M\334-\222\024\321\235\212\307\354\215r\333\325A\241\\l\362\341%uF\2031\317\355\217\367\315f\346\221\343\024\000\353\342\t4\211\247\240\317b<\004\036\336\034^(S\360\243Y\263k\035\257\322\252\263I\355\001\331\322[\301\367J\215\375\236n\301\177\227\276\2523\203Ia+m\226A\325\303 4\376\033\223\311m\371\3250\214\347\217@\360F\002v\374\361cf\277\311\220\272\272\222\253\377\323>7\304\341$\273R\347\255\b\034\221s\246b\265\215\035\247\265\277\257\345-\316\26202\2277\306\306\275-\210\244\23262\223\305\343\257\335\236,\t\312\273\211a\003\333\235B\350p[:B\365\222\312S\017\372\r\310\354\302\235E\265\273\366e\005\3741\246\025+\360\365\247\273l\002\214\035+\\kt\036\t\357;\365\256\204\243\325\231Sp\214\'\377\037\'\304\243;12\267\263\272]\367\t/GB+\234O\200\336\036\367\363m\247\022&\337\276\224\310\363C\251\265\331{\037\377\300,\360[\251\a\321{\375]\275\227,\vS\265\307\321Z\n\324\000\350\336j\242J\b\267\210\221?\332\325\306|\366B\362\327\215\340:\200\260\247\2770\021a+\305x\336j\027\264}{\270\3537\303J\275_\002\320\262[f\317\375\016\023\342J=\372\263\347\315R\314\257\260\353J\024\206\352\341\264AA\251\247\376\201\n\264\357\305c\002$\310.\003\246\320\025\b\315$&\321q\b\340\331\225\200\n\344\340\263\006\356UF\272\271\245W\037\027\240\375\357t\327\367\361\023PG\\?<xQ\360\233\206\251@\320\21002\001\v\232lP\340\362\226\261\226\254\245(\021\334\256w=\313\347\267S\273\243\027\200\275\364\304\317\243\347BZ\201\245\252\"\340h7n\250|:>\227eb\372N\030SP\340x\254\225u\364\315\334E&i0<\a\t\257m\243\005\244\223\361F\272/\f\020\321\004\004h\234]-\034\327\221\253\234`\375K\316p\"\371\337\246 \237\2565<\243\032\227`~\222\336U\"\024\345QF1RK,\251\376EUo@g\314\306\361\021\262\347?N\\\215\266\355\333\"\353+T\004t\'\244oZ\233R\031JC\001\021\336k:\315O\032\241\032\r\003\266_ \353\342\232\222\264\027\360\322\rF\207)\352\212t\342\343\305\202\030K8.\345rM\362\377h\022*\364\271\023\374\217@\016\2206\263\3426V\324g\370\f\027\250\302\337\rLu\0212X\373\272\373\330\251\324\311\332\2676em>\364L\225`\026\362\217|\227$\223\315\367;\212\033N\365\324\216\203\277\300\024bk\373\330\227\326\302\233\263\251\030\213j;\335\250T\351$}\351L\207\276~\351\020\334\217\304\2102\277^O7=\343\371\275\373\220\256\315\313\341\327\334^\371\351)\360j\'\r\021n\344\367\023\311\335\373\241\230\200G\033\n\221\276&4\030J\372\224[+\305\365-\237\311\374\275\373\322L\300c7r\212\036\334W\306\362Z\303C\306\374\3026\322\365\037\347\000\201\017\221\241jrk\000[5ho\362\031\b\326\210>E\204uF\025#s\033\254[\226\234\247\307)\2770_C\337\r\302m/C\277E\225\\\260\334\006\204\266\225%\036|\366\351\271f\302:\352\261b\341\023\033w2A\006\016$\354?\350\272\021\235\006\267`\354$\026\2618H\322l\343\"\236\006}\n\237f\254\214\232\231\030\217\367\246Y\304\3370\233pi\375\371\321Bh \253\321\361\327\3417~f\v\n\273\341C[J\3543E\353{\342pR\326|\315\255\315\365\304\345\024\017E\215\355\276U\227\341\362cY0S\020&\026&\2131\234\263:\254\227\371wE\032\377\265/\220e(\241\314J\207\345>"
    values: ""
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: true
    nulls: true
  }
  boolVector {
    values: true
    values: false
    values: false
    values: false
    values: false
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: true
  }
  i64Vector {
    values: 0
    values: 0
    values: -719162
    values: 2932896
    values: 0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: false
    nulls: false
    nulls: true
    nulls: true
  }
  fp64Vector {
    values: -2.3423423234E7
    values: 4.9E-324
    values: 1.7976931348623157E308
    values: 0.0
    values: 0.0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: false
    nulls: false
    nulls: true
    nulls: true
  }
  fp32Vector {
    values: -3.4E38
    values: 3.4E38
    values: -23423.234
    values: 0.0
    values: 0.0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: false
    nulls: false
    nulls: true
    nulls: true
  }
  i32Vector {
    values: 23
    values: -2147483648
    values: 2147483647
    values: 0
    values: 0
  }
}
vectors {
  nulls {
    nulls: true
    nulls: false
    nulls: false
    nulls: false
    nulls: true
  }
  i32Vector {
    values: 0
    values: -2147483648
    values: 2147483647
    values: -34234
    values: 0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: false
  }
  stringVector {
    values: "Hello world"
    values: ""
    values: ""
    values: "NULL"
    values: "StrangeString"
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: true
  }
  i64Vector {
    values: 47408238049834
    values: 0
    values: 0
    values: 86399999999999
    values: 0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: true
  }
  i64Vector {
    values: 1349273565345
    values: 0
    values: -62135596800000
    values: 253402300799999
    values: 0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: true
  }
  i64Vector {
    values: 1349266365345
    values: 0
    values: -62135596800000
    values: 253402300799999
    values: 0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: false
    nulls: false
    nulls: true
    nulls: true
  }
  i32Vector {
    values: -32768
    values: 32767
    values: 234
    values: 0
    values: 0
  }
}
vectors {
  nulls {
    nulls: false
    nulls: true
    nulls: false
    nulls: false
    nulls: true
  }
  byteVector {
    values: "\372\031\332sp\364E\236\201!<6/\257\250\340"
    values: ""
    values: "\a3lXk\262H\364\242p;3\361\264$\350"
    values: "Atv4U\222D.\226\261\312\276\020>8m"
    values: ""
  }
}
