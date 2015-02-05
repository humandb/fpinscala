import fpinscala.monoids.Monoid._

foldMapV(IndexedSeq("hello","world"), intAddition)( _.length)