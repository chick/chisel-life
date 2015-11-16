package life

import Chisel._

/**
 * Created by chick on 11/10/15.
 */
class LifeGrid(val rows: Int=20, val cols: Int = 40) extends Module {
  val io = new Bundle {val running = Bool(OUTPUT)}
  val grid = Array.fill(rows, cols) { Module(new LifeCell()) }

  val running = Reg(init=Bool(true))
  io.running := running

  for { row_index <- Range(0, rows)
        col_index <- Range(0, cols)
        cell = grid(row_index)(col_index)
  } {
    cell.io.running := io.running
    for {
      neighbor_row_delta <- Array(-1, 0, 1)
      neighbor_col_delta <- Array(-1, 0, 1)
      dr = row_index + neighbor_row_delta
      dc = col_index + neighbor_col_delta
      if !(neighbor_col_delta == 0 && neighbor_row_delta == 0) &&
         0 <= dr && dr < rows && 0 <= dc && dc < cols
    } {
      val neighbor_cell = grid(row_index + neighbor_row_delta)(col_index + neighbor_col_delta)
      cell.set_neighbor(neighbor_cell, neighbor_row_delta, neighbor_col_delta)
      neighbor_cell.set_neighbor(cell, -neighbor_row_delta, -neighbor_col_delta)
    }
  }
}

class LifeGridTests(c: LifeGrid) extends Tester(c, false) { self =>
  def clear(): Unit = {
    for {row_index <- c.grid.indices
         col_index <- c.grid(0).indices
    } {
      poke(c.grid(row_index)(col_index).is_alive, 0)
    }
  }

  def test_blinker() {
    clear()

    poke(c.grid(2)(2).is_alive, 1)

    step(1)

    expect(c.grid(2)(2).is_alive, BigInt(0))

    poke(c.grid(2)(1).is_alive, 1)
    poke(c.grid(2)(2).is_alive, 1)
    poke(c.grid(2)(3).is_alive, 1)

    show()

    //  expect(c.grid(2)(1).is_alive, BigInt(1))
    //  expect(c.grid(2)(2).is_alive, BigInt(1))
    //  expect(c.grid(2)(3).is_alive, BigInt(1))

    expect(c.grid(2)(2).is_alive, 1)
    step(1)
    show()
    expect(c.grid(2)(2).is_alive, 1)
    //  expect(c.grid(2)(2).neighbor_sum, 2) show()

    expect(c.grid(2)(1).is_alive, BigInt(0))
    expect(c.grid(2)(2).is_alive, BigInt(1))
    expect(c.grid(2)(3).is_alive, BigInt(0))

    // stop machine running, despite step, things should stay the same
    poke(c.running, 0)
    step(1)
    expect(c.grid(2)(1).is_alive, BigInt(0))
    expect(c.grid(2)(2).is_alive, BigInt(1))
    expect(c.grid(2)(3).is_alive, BigInt(0))
    show()

    // start machine back up
    poke(c.running, 1)
    step(1)
    expect(c.grid(2)(1).is_alive, BigInt(1))
    expect(c.grid(2)(2).is_alive, BigInt(1))
    expect(c.grid(2)(3).is_alive, BigInt(1))
    show()

    for (g <- 0 until 10) {
      step(1)
      show()
    }
  }

  def test_line() {
    clear()

    for(row <- c.grid.indices) {
      poke(c.grid(row)(2).is_alive, 1)
    }

    for (g <- 0 until 10) {
      step(1)
      show()
    }
  }

  def show(): Unit = {
    System.out.println("+" + ("-" * c.grid.head.length) + "+")
    for {
      row <- c.grid
    } {
      System.out.println("|" + row.map { cell => if(peek(cell.is_alive)==BigInt(1)) "*" else " "}.mkString("") + "|")
    }
    System.out.println("+" + ("-" * c.grid.head.length) + "+")
  }

  test_blinker()
  test_line()
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
