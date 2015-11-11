package life

import Chisel._

/**
 * Created by chick on 11/10/15.
 */
class LifeGrid(val rows: Int=5, val cols: Int = 5) extends Module {
  val io = new Bundle {val running = Bool(OUTPUT)}
  val grid = Array.fill[LifeCell](rows, cols) { Module(new LifeCell()) }

  for { row_index <- Range(0, rows)
        col_index <- Range(0, cols)
        cell = grid(row_index)(col_index)
  } {
    for {
      neighbor_row_delta <- Array(-1, 0, 1)
      neighbor_col_delta <- Array(-1, 0, 1)
      dr = row_index + neighbor_row_delta
      dc = col_index + neighbor_col_delta
      if(!(neighbor_col_delta == 0 && neighbor_row_delta == 0) &&
          (0 <= dr && dr < rows && 0 <= dc && dc < cols)
        )
    } {
      val neighbor_cell = grid(row_index + neighbor_row_delta)(col_index + neighbor_col_delta)
      cell.set_neighbor(neighbor_cell, neighbor_row_delta, neighbor_col_delta)
      neighbor_cell.set_neighbor(cell, -neighbor_row_delta, -neighbor_col_delta)
    }
  }
}

class LifeGridTests(c: LifeGrid) extends Tester(c, false) { self =>
  poke(c.grid(2)(2).is_alive, 1)

  step(2)

  expect(c.grid(2)(2).is_alive, BigInt(0))

  poke(c.grid(2)(1).is_alive, 1)
  poke(c.grid(2)(2).is_alive, 1)
  poke(c.grid(2)(3).is_alive, 1)

  show()

  expect(c.grid(2)(1).is_alive, BigInt(1))
  expect(c.grid(2)(2).is_alive, BigInt(1))
  expect(c.grid(2)(3).is_alive, BigInt(1))

  step(1)
  show()
  step(1)
  show()

  expect(c.grid(2)(1).is_alive, BigInt(0))
  expect(c.grid(2)(2).is_alive, BigInt(1))
  expect(c.grid(2)(3).is_alive, BigInt(0))

  def show(): Unit = {
    System.out.println("+" + ("-" * c.grid.size) + "|")
    for {
      row <- c.grid
    } {
      System.out.println("|" + row.map { cell => if(peek(cell.is_alive)==1) "*" else " "}.mkString("") + "|")
    }
    System.out.println("+" + ("-" * c.grid.size) + "|")
  }
}




object LifeGrid {
  def main(args: Array[String]): Unit = {
    chiselMainTest(
            Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
//      Array[String]("--backend", "dot"),
      () => Module(new LifeGrid())
    ) {
      c => new LifeGridTests(c)
    }
  }
}
