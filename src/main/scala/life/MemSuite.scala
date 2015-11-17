package life

import java.io.File

import Chisel._

class CreateSeqMem(size: Integer) extends Module {
  val io = new Bundle {
    val wEnable = Bool(INPUT)
    val rEnable = Bool(INPUT)
    val addr    = UInt(INPUT, log2Ceil(size))
    val value   = UInt(INPUT, 32)
    val out     = UInt(OUTPUT, 32)
  }
  val mem = SeqMem(size, UInt(width = 32))
  when(io.wEnable) {
    mem.write(io.addr, io.value)
  }
  val rdata = mem.read(io.addr, io.rEnable)
  io.out := rdata

//  val mem = Mem(size, UInt(width = 32))
//  when(io.wEnable) {
//    mem(io.addr) := io.value
//  }
//  when(io.rEnable) {
//    io.out := mem(io.addr)
//  }.otherwise {
//    io.out := UInt(0)
//  }

  val mem_sum0 = mem.read(UInt(0), Bool(true)) + mem.read(UInt(1), Bool(true))
  val mem_sum1 = mem.read(UInt(2), Bool(true)) + mem.read(UInt(3), Bool(true))

  mem.write(UInt(4), mem_sum0)
  mem.write(UInt(5), mem_sum1)
  mem.write(UInt(6), mem_sum0 + mem_sum1)
}

class CreateSeqMemTester(c: CreateSeqMem, size: Int) extends Tester(c) { self =>
  for (t <- 0 until 4) {
    val test_addr = rnd.nextInt(size)
    val test_value = rnd.nextInt(log2Ceil(size))
    poke(c.io.addr, test_addr)
    poke(c.io.value, test_value)
    poke(c.io.wEnable, 1)
    poke(c.io.rEnable, 1)
    step(2)
    poke(c.io.wEnable, 0)
    poke(c.io.rEnable, 0)
    expect(c.io.out, test_value)
  }

  println("======= Starting sequential writes ============")
  for(i <- 0 until 4) {
    poke(c.io.addr, i)
    poke(c.io.value, i)
    poke(c.io.wEnable, 1)
    poke(c.io.rEnable, 1)
    step(2)
    poke(c.io.wEnable, 0)
    poke(c.io.rEnable, 0)
    expect(c.io.out, i)
  }
  println("======= Testing sequential writes ============")
  for(i <- 0 until 4) {
    poke(c.io.addr, i)
    poke(c.io.rEnable, 1)
    step(2)
    expect(c.io.out, i)
  }
  println("======= Finished testing sequential writes ============")




  def check_value(address: Int, value: Int): Unit = {
    println(s"\n\nTesting for value $value at add $address")
    poke(c.io.addr, address)
    poke(c.io.rEnable, 1)
    step(2)
    expect(c.io.out, value)
  }

  check_value(0, 0)
  check_value(1, 1)
  check_value(2, 2)
  check_value(3, 3)
  check_value(4, 1)
  check_value(5, 5)
  check_value(6, 6)
}

object MemSuite {
  val dir = new File("test-outputs")

  def main (args: Array[String]){
    val size = 1024
    val testArgs = chiselEnvironmentArguments() ++ Array("--compile", "--genHarness", "--test", "--targetDir", dir.getPath)
//    val testArgs = chiselEnvironmentArguments() ++ Array("--backend", "dot")
    chiselMainTest(testArgs, () => Module(new CreateSeqMem(size))){
      c => new CreateSeqMemTester(c, size)}
  }
}
