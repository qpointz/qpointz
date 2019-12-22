trait Matcher[A] {
  def isMatching(a:A):Boolean
}

object Matcher {

  def include[A](m:Matcher[A]):Matcher[A] = m

  def exclude[A](m:Matcher[A]):Matcher[A] = (a: A) => {
    !m.isMatching(a)
  }

}