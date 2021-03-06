package fpinscala.monoids

import fpinscala.parallelism.Nonblocking._
import fpinscala.parallelism.Nonblocking.Par.toParOps // infix syntax for `Par.map`, `Par.flatMap`, etc

trait Monoid[A] {
  def op(a1: A, a2: A): A
  def zero: A
}

object Monoid {

  val stringMonoid = new Monoid[String] {
    def op(a1: String, a2: String) = a1 + a2

    val zero = ""
  }

  def listMonoid[A] = new Monoid[List[A]] {
    def op(a1: List[A], a2: List[A]) = a1 ++ a2

    val zero = Nil
  }

  val intAddition: Monoid[Int] = new Monoid[Int] {
    def op(a: Int, b: Int) = a + b

    def zero = 0
  }

  val intMultiplication: Monoid[Int] = new Monoid[Int] {
    def op(a: Int, b: Int) = a * b

    def zero = 1
  }

  val booleanOr: Monoid[Boolean] = new Monoid[Boolean] {
    def op(a: Boolean, b: Boolean) = a || b

    def zero = false
  }

  val booleanAnd: Monoid[Boolean] = new Monoid[Boolean] {
    def op(a: Boolean, b: Boolean) = a && b

    def zero = true
  }

  def optionMonoid[A]: Monoid[Option[A]] = new Monoid[Option[A]] {

    /**
     *
     * a: Some, b: Some, c: Some
     * op(op(a,b),c) = op(a,c) = a
     * op(a,op(b,c)) = op(a,b) = a
     *
     * a: Some, b: Some, c: None
     * op(op(a,b),c) = op(a,c) = a
     * op(a,op(b,c)) = op(a,b) = a
     *
     * a: Some, b: None, c: Some
     * op(op(a,b),c) = op(a,c) = a
     * op(a,op(b,c)) = op(a,c) = a
     *
     * a: Some, b: None, c: None
     * op(op(a,b),c) = op(a,None) = a
     * op(a,op(b,c)) = op(a,None) = a
     *
     * a: None, b: Some, c: Some
     * op(op(a,b),c) = op(b,c) = b
     * op(a,op(b,c)) = op(a,b) = b
     *
     * a: None, b: Some, c: None
     * op(op(a,b),c) = op(b,c) = b
     * op(a,op(b,c)) = op(a,b) = b
     *
     * a: None, b: None, c: Some
     * op(op(a,b),c) = op(None,c) = c
     * op(a,op(b,c)) = op(a,c) = c
     *
     * a: None, b: None, c: None
     * op(op(a,b),c) = op(None,c) = None
     * op(a,op(b,c)) = op(a,None) = None
     */
    def op(a: Option[A], b: Option[A]) = a match {
      case Some(a) => Some(a)
      case None => b
    }

    /**
     * op(None, None) = None
     * op(Some, None) = Some
     */
    def zero = None
  }

  /**
   * op(f, op(g,h))(x) = op(f, g(h))(x) = f(g(h(x)))
   * op(op(f,g),h))(x) = op(f(g), h)(x) = f(g(h(x)))
   *
   * op(f,zero)(x) = f(zero(x)) = f(x)
   *
   */
  def endoMonoid[A]: Monoid[A => A] = new Monoid[A => A] {
    def op(f: A => A, g: A => A): A => A = {
      a: A => f(g(a))
    }

    def zero = { a: A => a }
  }

  // TODO: Placeholder for `Prop`. Remove once you have implemented the `Prop`
  // data type from Part 2.
  trait Prop {}

  // TODO: Placeholder for `Gen`. Remove once you have implemented the `Gen`
  // data type from Part 2.

  import fpinscala.testing._
  import Prop._

  def monoidLaws[A](m: Monoid[A], gen: Gen[A]): Prop = sys.error("todo")

  def trimMonoid(s: String): Monoid[String] = sys.error("todo")

  def concatenate[A](as: List[A], m: Monoid[A]): A = as.foldLeft(m.zero)(m.op)

  def foldMap[A, B](as: List[A], m: Monoid[B])(f: A => B): B =
    as.foldLeft(m.zero)((b, a) => m.op(b, f(a)))

  def foldRight[A, B](as: List[A])(z: B)(f: (A, B) => B): B =
    sys.error("todo")

  def foldLeft[A, B](as: List[A])(z: B)(f: (B, A) => B): B =
    sys.error("todo")

  def foldMapV[A, B](as: IndexedSeq[A], m: Monoid[B])(f: A => B): B = {

    def loop(xs: IndexedSeq[A]): B = {
      xs match {
        case Seq(a)   => f(a)
        case Seq(a,b) => m.op(f(a),f(b))
        case ys => {
          val (l, r) = ys.splitAt(xs.length / 2)
          m.op(loop(l), loop(r))
        }
      }
    }

    loop(as)
  }


  def ordered(ints: IndexedSeq[Int]): Boolean =
    sys.error("todo")

  sealed trait WC
  case class Stub(chars: String) extends WC
  case class Part(lStub: String, words: Int, rStub: String) extends WC

  def par[A](m: Monoid[A]): Monoid[Par[A]] = 
    sys.error("todo")

  def parFoldMap[A,B](v: IndexedSeq[A], m: Monoid[B])(f: A => B): Par[B] = 
    sys.error("todo") 

  val wcMonoid: Monoid[WC] = sys.error("todo")

  def count(s: String): Int = sys.error("todo")

  def productMonoid[A,B](A: Monoid[A], B: Monoid[B]): Monoid[(A, B)] =
    sys.error("todo")


  /**
   * op(f,op(g,h)) === op(op(f,g),h)
   *
   * op(f, op(g,h)) == op(f, { a => mon.op(g(a),h(a)) })
   *                == { a => mon.op(f(a), mon.op(g(a),h(a)) }
   *                == { a => mon.op(mon.op(f(a),g(a)),h(a)) }
   *                == op( { a => mon.op(f(a),g(a))}, h)
   *                == op(op(f,g),h)
   *
   * op(f, zero) == { a => mon.op(b1,mon.zero) } == { a => b1 } == { a => f(a) } == f
   *
   */
  def functionMonoid[A,B](mon: Monoid[B]): Monoid[A => B] = new Monoid[A => B] {
    def op(f: A => B, g: A => B): A => B = { a: A => mon.op(f(a),g(a)) }
    def zero = { a: A => mon.zero }
  }

  def mapMergeMonoid[K,V](V: Monoid[V]): Monoid[Map[K, V]] =
    sys.error("todo")

  def bag[A](as: IndexedSeq[A]): Map[A, Int] =
    sys.error("todo")
}

trait Foldable[F[_]] {
  import Monoid._

  def foldRight[A, B](as: F[A])(z: B)(f: (A, B) => B): B =
    sys.error("todo")

  def foldLeft[A, B](as: F[A])(z: B)(f: (B, A) => B): B =
    sys.error("todo")

  def foldMap[A, B](as: F[A])(f: A => B)(mb: Monoid[B]): B =
    sys.error("todo")

  def concatenate[A](as: F[A])(m: Monoid[A]): A =
    sys.error("todo")

  def toList[A](as: F[A]): List[A] =
    sys.error("todo")
}

object ListFoldable extends Foldable[List] {
  override def foldRight[A, B](as: List[A])(z: B)(f: (A, B) => B) =
    sys.error("todo")
  override def foldLeft[A, B](as: List[A])(z: B)(f: (B, A) => B) =
    sys.error("todo")
  override def foldMap[A, B](as: List[A])(f: A => B)(mb: Monoid[B]): B =
    sys.error("todo")
}

object IndexedSeqFoldable extends Foldable[IndexedSeq] {
  override def foldRight[A, B](as: IndexedSeq[A])(z: B)(f: (A, B) => B) =
    sys.error("todo")
  override def foldLeft[A, B](as: IndexedSeq[A])(z: B)(f: (B, A) => B) =
    sys.error("todo")
  override def foldMap[A, B](as: IndexedSeq[A])(f: A => B)(mb: Monoid[B]): B =
    sys.error("todo")
}

object StreamFoldable extends Foldable[Stream] {
  override def foldRight[A, B](as: Stream[A])(z: B)(f: (A, B) => B) =
    sys.error("todo")
  override def foldLeft[A, B](as: Stream[A])(z: B)(f: (B, A) => B) =
    sys.error("todo")
}

sealed trait Tree[+A]
case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

object TreeFoldable extends Foldable[Tree] {
  override def foldMap[A, B](as: Tree[A])(f: A => B)(mb: Monoid[B]): B =
    sys.error("todo")
  override def foldLeft[A, B](as: Tree[A])(z: B)(f: (B, A) => B) =
    sys.error("todo")
  override def foldRight[A, B](as: Tree[A])(z: B)(f: (A, B) => B) =
    sys.error("todo")
}

object OptionFoldable extends Foldable[Option] {
  override def foldMap[A, B](as: Option[A])(f: A => B)(mb: Monoid[B]): B =
    sys.error("todo")
  override def foldLeft[A, B](as: Option[A])(z: B)(f: (B, A) => B) =
    sys.error("todo")
  override def foldRight[A, B](as: Option[A])(z: B)(f: (A, B) => B) =
    sys.error("todo")
}

