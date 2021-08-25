package life

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util.MuxLookup
import chiseltest.{experimental, RawTester}

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

//object AddressableLifeGrid {
//  def main(args: Array[String]): Unit = {
//    println(ChiselStage.emitFirrtl(new AddressableLifeGrid()))
//    RawTester.test(new AddressableLifeGrid(20, 20)) { c =>
//      new Ex(c)
//    }
//  }
//}
