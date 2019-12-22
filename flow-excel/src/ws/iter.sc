class a extends Iterable[Int] {

  override def iterator: Iterator[Int] = {
    Seq(1,2,3).iterator
  }

}

val b = new a()

b