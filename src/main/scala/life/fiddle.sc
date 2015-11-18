Range(0, 3) ++ Range(4, 7)

import Chisel._

if(true) "dog" else "cat"

val x = Option(null)
val y = Some(null)

class SimpleLink extends Bundle {
  val data = UInt(OUTPUT, 16)
  val valid = Bool(OUTPUT)
}

val aa = Array.tabulate(10) { i => i+i }

val s = new SimpleLink()


class Plink extends SimpleLink {
  val parity = UInt(OUTPUT, 5)
}

class FilterIO extends Bundle {
  val x = new Plink().flip()
  val y = new Plink()
}

def f[T](ot: Option[T]) : Unit = {
  ot match {
    case Some(t) => System.out.println(s"some t $t")
    case None => System.out.println("got none")
  }
  print(s"ot $ot")
}

def f[T](t : T) : Unit = {
  f(Option(t))
}

f("s")
