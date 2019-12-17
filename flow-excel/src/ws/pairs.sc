val a = List(1,2,3)
val b = List("a","b","c")

val c = for (
  i <- a;
  j <- b
) yield (i, j)

