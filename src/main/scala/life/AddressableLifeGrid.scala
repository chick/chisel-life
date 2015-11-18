package life

import Chisel._

/**
 * Created by chick on 11/10/15.
 */
class AddressableLifeGrid(val rows: Int=10, val cols: Int = 20) extends Module {
  val io = new Bundle {
    val running = Bool(OUTPUT)
    val row_address = UInt(INPUT, width = 32)
    val col_address = UInt(INPUT, width = 32)
    val write_enable = Bool(INPUT)
    val set_alive = Bool(INPUT)
    val read_enable = Bool(INPUT)
    val alive_value = Bool(OUTPUT)
  }
  val grid = Array.fill(rows, cols)(Module(new LifeCell()))

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

  when(io.read_enable) {
    io.alive_value := MuxLookup(
      io.row_address,
      Bool(false),
      Array.tabulate(rows) { row =>
        UInt(row) -> MuxLookup(
          io.col_address,
          Bool(false),
          Array.tabulate(cols) { col =>
            UInt(col) -> grid(row)(col).io.is_alive
          }
        )
      }
    )
  } otherwise {
    io.alive_value := Bool(false)
  }
}

class AddressableLifeGridTests(c: AddressableLifeGrid) extends Tester(c) { self =>
  def clear(): Unit = {
    for {row_index <- c.grid.indices
         col_index <- c.grid(0).indices
    } {
      poke(c.grid(row_index)(col_index).is_alive, 0)
    }
  }

  def run(): Unit = { poke(c.running, 1) }
  def pause(): Unit = { poke(c.running, 0) }

  def read(row: Int, col: Int) : Unit = {
    poke(c.io.row_address, row)
    poke(c.io.col_address, col)
    poke(c.io.read_enable, 1)
    step(10)
  }

  def test_grid_reading(): Unit = {
    clear()
    pause()

    poke(c.grid(0)(0).is_alive, 1)
    poke(c.grid(1)(1).is_alive, 1)
    step(1)

    expect(c.running, 0)
    expect(c.grid(1)(1).io.is_alive, 1)

    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      read(i, j)
      expect(c.io.alive_value, if(i==j) 1 else 0)
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

  test_grid_reading()
//  test_blinker()
  // test_line()
}

object AddressableLifeGrid {
  def main(args: Array[String]): Unit = {
    chiselMainTest(
      Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
//            Array[String]("--backend", "dot"),
      () => Module(new AddressableLifeGrid(2, 2))
    ) {
      c => new AddressableLifeGridTests(c)
    }
  }
}
