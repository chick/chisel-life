package life

import chisel3._
import chisel3.iotesters.{chiselMainTest, PeekPokeTester}
import chisel3.stage.ChiselStage
import chisel3.util.MuxLookup

/** Created by chick on 11/10/15.
  */

/** Implements a circuit for Conway's "Game of Life", which every cell is updated every clock cycle.
  * This class implements the grid along with read and write methods for every cell.
  * The grid can be paused or running
  * Each grid element is an LifeCell
  *
  * @param rows  number of rows in the grid
  * @param cols  number of columns in the grid
  */
class AddressableLifeGrid(val rows: Int = 10, val cols: Int = 20) extends Module {
  val io = IO(new Bundle {
    val running = Output(Bool())
    val rowAddress = Input(UInt(32.W))
    val colAddress = Input(UInt(32.W))
    val writeEnable = Input(Bool())
    val setAlive = Input(Bool())
    val setDead = Input(Bool())
    val aliveValue = Output(Bool())
  })
  val grid = Array.fill(rows, cols)(Module(new LifeCell()))

  val running = RegInit(true.B)
  io.running := running

  // Initialize the neighbor connection
  // by iterating over the moore neighborhood and connecting
  // each cells input to the io.isAlive of the neighbor
  for {
    rowIndex <- 0 until rows
    colIndex <- 0 until cols
    cell = grid(rowIndex)(colIndex)
  } {
    cell.io.running := io.running
    for {
      neighborRowDelta <- Array(-1, 0, 1)
      neighborColDelta <- Array(-1, 0, 1)
      neighborRow = rowIndex + neighborRowDelta
      neighborCol = colIndex + neighborColDelta
    } {
      if (!(neighborColDelta == 0 && neighborRowDelta == 0)) {
        cell.setNeighbor(
          neighborOpt = validNeighbor(neighborRow, neighborCol),
          deltaRow = neighborRowDelta,
          deltaCol = neighborColDelta
        )
      } else {}
    }
  }

  // DEMUX the setAlive and setDead write instructions into cells based on row and col address
  // the individual's cell is set according to which line is set
  Array.tabulate(rows) { row =>
    val rowEnabled = Mux(row.U === io.rowAddress, true.B, false.B)
    Array.tabulate(cols) { col =>
      grid(row)(col).io.setAlive := Mux(
        (col.U === io.colAddress) && rowEnabled && io.writeEnable,
        io.setAlive,
        false.B
      )
      grid(row)(col).io.setDead := Mux(
        (col.U === io.colAddress) && rowEnabled && io.writeEnable,
        io.setDead,
        false.B
      )
    }
  }
  // set up a mux to access the alive state of any cell
  // mux all the columns in a row indexed by column
  // then mux those muxes together indexed by row
  io.aliveValue := MuxLookup(
    io.rowAddress,
    false.B,
    Array.tabulate(rows) { row =>
      row.U -> MuxLookup(
        io.colAddress,
        false.B,
        Array.tabulate(cols) { col =>
          col.U -> grid(row)(col).io.isAlive
        }
      )
    }
  )

  def validNeighbor(neighborRow: Int, neighborCol: Int): Option[LifeCell] = {
    if (0 <= neighborRow && neighborRow < rows && 0 <= neighborCol && neighborCol < cols) {
      Some(grid(neighborRow)(neighborCol))
    } else {
      None
    }
  }
}

class AddressableLifeGridTests(c: AddressableLifeGrid) extends PeekPokeTester(c) {

  def clear(): Unit = {
    for {
      rowIndex <- c.grid.indices
      colIndex <- c.grid(0).indices
    } {
      poke(c.grid(rowIndex)(colIndex).isAlive, 0)
    }
  }

  def run(): Unit = {
    poke(c.running, 1)
  }

  def pause(): Unit = {
    poke(c.running, 0)
  }

  def read(row: Int, col: Int): Unit = {
    poke(c.io.rowAddress, row)
    poke(c.io.colAddress, col)
  }

  def testGridReading(): Unit = {
    clear()
    pause()

    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      poke(c.grid(i)(j).isAlive, if ((i + j) % 5 == 0) 0 else 1)
      expect(c.grid(i)(j).isAlive, if ((i + j) % 5 == 0) 0 else 1)
    }

    val i = 0
    val j = 0
    expect(c.grid(i)(j).isAlive, if ((i + j) % 5 == 0) 0 else 1)
    step(1)
    expect(c.grid(i)(j).isAlive, if ((i + j) % 5 == 0) 0 else 1)

    expect(c.running, 0)
    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      expect(c.grid(i)(j).isAlive, if ((i + j) % 5 == 0) 0 else 1)
      expect(c.grid(i)(j).io.isAlive, if ((i + j) % 5 == 0) 0 else 1)
    }

    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      read(i, j)
      expect(c.io.aliveValue, if ((i + j) % 5 == 0) 0 else 1)
    }
  }

  def testGridWriting(): Unit = {
    clear()
    pause()

    def writeCell(row: Int, col: Int, setAlive: Boolean): Unit = {
      poke(c.io.rowAddress, row)
      poke(c.io.colAddress, col)
      poke(c.io.setAlive, if (setAlive) 1 else 0)
      poke(c.io.setDead, if (!setAlive) 1 else 0)
      poke(c.io.writeEnable, 1)
      step(1)
      poke(c.io.writeEnable, 0)
    }

    def testReadWrite(): Unit = {
      for {
        i <- 0 until c.rows
        j <- 0 until c.cols
      } {
        writeCell(i, j, setAlive = true)
        expect(c.grid(i)(j).io.isAlive, 1)
        writeCell(i, j, setAlive = false)
        expect(c.grid(i)(j).io.isAlive, 0)
//        show()
      }
    }

    testReadWrite()
  }

  def testBlinker() {
    clear()
    run()

    poke(c.grid(2)(2).isAlive, 1)

    step(1)

    expect(c.grid(2)(2).isAlive, BigInt(0))

    poke(c.grid(2)(1).isAlive, 1)
    poke(c.grid(2)(2).isAlive, 1)
    poke(c.grid(2)(3).isAlive, 1)

    show()

    expect(c.grid(2)(2).isAlive, 1)
    step(1)
    show()
    expect(c.grid(2)(2).isAlive, 1)

    expect(c.grid(2)(1).isAlive, BigInt(0))
    expect(c.grid(2)(2).isAlive, BigInt(1))
    expect(c.grid(2)(3).isAlive, BigInt(0))

    // stop machine running, despite step, things should stay the same
    poke(c.running, 0)
    step(1)
    expect(c.grid(2)(1).isAlive, BigInt(0))
    expect(c.grid(2)(2).isAlive, BigInt(1))
    expect(c.grid(2)(3).isAlive, BigInt(0))
    show()

    // start machine back up
    poke(c.running, 1)
    step(1)
    expect(c.grid(2)(1).isAlive, BigInt(1))
    expect(c.grid(2)(2).isAlive, BigInt(1))
    expect(c.grid(2)(3).isAlive, BigInt(1))
    show()

//    for (g <- 0 until 10) {
//      step(1)
//      show()
//    }
  }

  def testLine() {
    clear()

    for (row <- c.grid.indices) {
      poke(c.grid(row)(2).isAlive, 1)
    }

    for (g <- 0 until 100) {
      step(1)
      show()
    }
  }

  def testRandom() {
    clear()

    for {
      row <- c.grid.indices
      col <- c.grid(row).indices
    } {
      poke(c.grid(row)(col).isAlive, rnd.nextInt(2))
    }

    for (g <- 0 until 100) {
      step(1)
      show()
    }
  }

  def show(): Unit = {
    System.out.println("+" + ("-" * c.grid.head.length) + "+")
    for {
      row <- c.grid
    } {
      System.out.println(
        "|" + row.map { cell => if (peek(cell.isAlive) == BigInt(1)) "*" else " " }.mkString("") + "|"
      )
    }
    System.out.println("+" + ("-" * c.grid.head.length) + "+")
  }

  testGridWriting()
  testGridReading()
  testBlinker()
  testLine()
  testRandom()
}

object AddressableLifeGrid {
  def main(args: Array[String]): Unit = {
    println(ChiselStage.emitFirrtl(new AddressableLifeGrid()))
    chiselMainTest(Array[String](), () => Module(new AddressableLifeGrid(20, 20))) { c =>
      new AddressableLifeGridTests(c)
    }
  }
}
