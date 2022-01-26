import io.qpointz.flow.ql.QlFunction

val mf = QlFunction.func(
  {(t1:String, t2:Int) => s"Hello $t1 $t2 times"},
  {a=> (a(0).toString,
        a(1).asInstanceOf[Int])}
)

mf(Seq("kxxx",121))
print("hallo")