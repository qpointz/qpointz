val a = 2

val b = 3

a + b

println(a)


object Test {
  println("This does not print!")
  add(5, 6)

  println("This however prints!")
  add(5, 6)

  def add(a: Int, b: Int): Int = a + b
}
