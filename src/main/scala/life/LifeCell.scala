// See README.md for license details.

package life

import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util.MuxCase

/** Created by chick on 11/9/15.
  * Implements a single cell of a conway life space
  */
class LifeCell extends Module {
  val io = IO(new Bundle {
    val running = Input(Bool())
    val topLeft = Input(UInt(4.W))
    val topCenter = Input(UInt(4.W))
    val topRight = Input(UInt(4.W))
    val midLeft = Input(UInt(4.W))
    val midRight = Input(UInt(4.W))
    val botLeft = Input(UInt(4.W))
    val botCenter = Input(UInt(4.W))
    val botRight = Input(UInt(4.W))

    val setAlive = Input(Bool())
    val setDead = Input(Bool())

    val isAlive = Output(Bool())
  })

  def setNeighbor(neighborOpt: Option[LifeCell], deltaRow: Int, deltaCol: Int): Unit = {
    def maybeConnect(): Data = {
      neighborOpt match {
        case Some(neighbor) => neighbor.io.isAlive
        case _              => DontCare
      }
    }
    if (deltaRow < 0 && deltaCol < 0) io.botLeft := maybeConnect()
    if (deltaRow < 0 && deltaCol == 0) io.botCenter := maybeConnect()
    if (deltaRow < 0 && deltaCol > 0) io.botRight := maybeConnect()
    if (deltaRow == 0 && deltaCol < 0) io.midLeft := maybeConnect()
    if (deltaRow == 0 && deltaCol == 0) throw new Exception("bad connection")
    if (deltaRow == 0 && deltaCol > 0) io.midRight := maybeConnect()
    if (deltaRow > 0 && deltaCol < 0) io.topLeft := maybeConnect()
    if (deltaRow > 0 && deltaCol == 0) io.topCenter := maybeConnect()
    if (deltaRow > 0 && deltaCol > 0) io.topRight := maybeConnect()
  }

  val isAlive = RegInit(false.B)

  val sum0 = io.topLeft + io.topCenter
  val sum1 = io.topRight + io.midLeft
  val sum2 = io.midRight + io.botLeft
  val sum3 = io.botCenter + io.botRight

  val sum4 = sum0 + sum1
  val sum5 = sum2 + sum3

  val neighborSum = sum4 + sum5

  isAlive := MuxCase(
    isAlive,
    Seq(
      io.setAlive -> true.B,
      io.setDead -> false.B,
      (io.running && isAlive) -> (neighborSum === 2.U || neighborSum === 3.U),
      (io.running && !isAlive) -> (neighborSum === 3.U)
    )
  )

  io.isAlive := isAlive
}

class LifeCellTests(c: LifeCell) extends PeekPokeTester(c) { self =>
  def setNeighbors(
    ntl: Int,
    ntc: Int,
    ntr: Int,
    nml: Int,
    nmc: Int,
    nmr: Int,
    nbl: Int,
    nbc: Int,
    nbr: Int
  ): Unit = {
    poke(c.io.topLeft, ntl)
    poke(c.io.topCenter, ntc)
    poke(c.io.topRight, ntr)
    poke(c.io.midLeft, nml)
    // center "neighbor" is the value of the cell itself
    // poke(c.isAlive, nmc)
    poke(c.io.midRight, nmr)
    poke(c.io.botLeft, nbl)
    poke(c.io.botCenter, nbc)
    poke(c.io.botRight, nbr)
  }

  poke(c.io.running, 1)

  // dead cell with no neighbors stays dead
  setNeighbors(
    0, 0, 0, 0, 0, 0, 0, 0, 0
  )
  step(1)
  expect(c.io.isAlive, 0)

  // dead cell with > 3 neighbors stays dead
  setNeighbors(
    1, 1, 1, 1, 0, 1, 1, 1, 1
  )
  step(1)
  expect(c.io.isAlive, 0)

  // live cell with > 3 neighbors stays dead
  setNeighbors(
    1, 1, 1, 1, 1, 1, 1, 1, 1
  )
  step(1)
  expect(c.io.isAlive, 0)

  // dead cell with exactly three neighbors becomes alive
  setNeighbors(
    1, 0, 0, 1, 0, 0, 1, 0, 0
  )
  step(1)
  expect(c.io.isAlive, 1)
  setNeighbors(
    1, 0, 0, 0, 0, 1, 0, 1, 0
  )
  step(1)
  expect(c.io.isAlive, 1)

  // live cell with one neighbor dies
  setNeighbors(
    0, 0, 0, 0, 1, 1, 0, 0, 0
  )
  step(1)
  expect(c.io.isAlive, 0)

  // live cell with exactly three neighbors stays alive
  setNeighbors(
    1, 0, 0, 1, 1, 0, 1, 0, 0
  )
  step(1)
  expect(c.io.isAlive, 1)

  // live cell with exactly four neighbors dies
  setNeighbors(
    1, 0, 0, 1, 1, 1, 1, 0, 0
  )
  step(1)
  expect(c.io.isAlive, 0)

  // test setAlive
  setNeighbors(
    0, 0, 0, 0, 0, 0, 0, 0, 0
  )
  poke(c.io.setAlive, 1)
  poke(c.io.setDead, 0)
  poke(c.io.running, 1)
  step(1)
  expect(c.io.isAlive, 1)

  poke(c.io.setAlive, 1)
  poke(c.io.setDead, 0)
  poke(c.io.running, 0)
  step(1)
  expect(c.io.isAlive, 1)

  poke(c.io.setDead, 1)
  poke(c.io.setAlive, 0)
  poke(c.io.running, 1)
  step(1)
  expect(c.io.isAlive, 0)

  poke(c.io.setDead, 1)
  poke(c.io.setAlive, 0)
  poke(c.io.running, 0)
  step(1)
  expect(c.io.isAlive, 0)

}

object LifeCell {
  def main(args: Array[String]): Unit = {
    iotesters.Driver.execute(
      Array[String]("--backend-name", "firrtl"),
      //      Array[String]("--backend", "dot"),
      () => new LifeCell()
    ) { c =>
      new LifeCellTests(c)
    }
  }
}
