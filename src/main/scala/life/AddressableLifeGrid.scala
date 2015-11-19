package life

import Chisel._

/**
 * Created by chick on 11/10/15.
 */
class AddressableLifeGrid(val rows: Int=10, val cols: Int = 20) extends Module {
  val io = new Bundle {
    val running      = Bool(OUTPUT)
    val row_address  = UInt(INPUT, width = 32)
    val col_address  = UInt(INPUT, width = 32)
    val write_enable = Bool(INPUT)
    val set_alive    = Bool(INPUT)
    val set_dead     = Bool(INPUT)
    val alive_value  = Bool(OUTPUT)
  }
  val grid = Array.fill(rows, cols)(Module(new LifeCell()))

  val running = Reg(init=Bool(true))
  io.running := running

  // Initialize the neighbor connection
  // by iterating over the moore neighborhood and connecting
  // each cells input to the alive of the neighbor
  for { row_index <- Range(0, rows)
        col_index <- Range(0, cols)
        cell = grid(row_index)(col_index)
  } {
    cell.io.running := io.running
    for {
      neighbor_row_delta <- Array(-1, 0, 1)
      neighbor_col_delta <- Array(-1, 0, 1)
      neighbor_row = row_index + neighbor_row_delta
      neighbor_col = col_index + neighbor_col_delta
      if !(neighbor_col_delta == 0 && neighbor_row_delta == 0) &&
        valid_neighbor(neighbor_row, neighbor_col)
    } {
      cell.set_neighbor(
        neighbor = grid(neighbor_row)(neighbor_col),
        delta_row = neighbor_row_delta,
        delta_col = neighbor_col_delta
      )
    }
  }

  // DEMUX set_alive into cells based on row and col address
  // the individual's cell is set to set_alive
  Array.tabulate(rows) { row =>
    val row_enabled = Mux(UInt(row) === io.row_address, Bool(true), Bool(false))
    Array.tabulate(cols) { col =>
      grid(row)(col).io.set_alive := Mux(
        (UInt(col) === io.col_address) && row_enabled && io.write_enable, io.set_alive, Bool(false))
      grid(row)(col).io.set_dead := Mux(
        (UInt(col) === io.col_address) && row_enabled && io.write_enable, io.set_dead, Bool(false))
    }
  }
  // set up a mux to access the alive state of any cell
  // mux all the columns in a row indexed by column
  // then mux those muxes together indexed by row
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

  def valid_neighbor(neighbor_row : Int, neighbor_col : Int) = {
    0 <= neighbor_row && neighbor_row < rows && 0 <= neighbor_col && neighbor_col < cols
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
  }

  def test_grid_reading(): Unit = {
    clear()
    pause()

    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      poke(c.grid(i)(j).is_alive, if ((i + j) % 5 == 0) 0 else 1)
      expect(c.grid(i)(j).is_alive, if ((i + j) % 5 == 0) 0 else 1)
    }

    val i = 0
    val j = 0
    expect(c.grid(i)(j).is_alive, if ((i + j) % 5 == 0) 0 else 1)
    step(1)
    expect(c.grid(i)(j).is_alive, if ((i + j) % 5 == 0) 0 else 1)

    expect(c.running, 0)
    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      expect(c.grid(i)(j).is_alive, if ((i + j) % 5 == 0) 0 else 1)
      expect(c.grid(i)(j).io.is_alive, if ((i + j) % 5 == 0) 0 else 1)
    }

    for {
      i <- 0 until c.rows
      j <- 0 until c.cols
    } {
      read(i, j)
      expect(c.io.alive_value, if ((i + j) % 5 == 0) 0 else 1)
    }
  }

  def test_grid_writing(): Unit = {
    clear()
    pause()

    def write_cell(row: Int, col: Int, set_alive: Boolean): Unit = {
      poke(c.io.row_address, row)
      poke(c.io.col_address, col)
      poke(c.io.set_alive, if(set_alive) 1 else 0)
      poke(c.io.set_dead, if(!set_alive) 1 else 0)
      poke(c.io.write_enable, 1)
      step(1)
      poke(c.io.write_enable, 0)
    }

    def test_read_write(): Unit = {
      for {
        i <- 0 until c.rows
        j <- 0 until c.cols
      } {
        write_cell(i, j, set_alive = true)
        expect(c.grid(i)(j).io.is_alive, 1)
        write_cell(i, j, set_alive = false)
        expect(c.grid(i)(j).io.is_alive, 0)
//        show()
      }
    }

    test_read_write()
  }

  def test_blinker() {
    clear()
    run()

    poke(c.grid(2)(2).is_alive, 1)

    step(1)

    expect(c.grid(2)(2).is_alive, BigInt(0))

    poke(c.grid(2)(1).is_alive, 1)
    poke(c.grid(2)(2).is_alive, 1)
    poke(c.grid(2)(3).is_alive, 1)

    show()

    expect(c.grid(2)(2).is_alive, 1)
    step(1)
    show()
    expect(c.grid(2)(2).is_alive, 1)

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

//    for (g <- 0 until 10) {
//      step(1)
//      show()
//    }
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
    isTrace = false
    System.out.println("+" + ("-" * c.grid.head.length) + "+")
    for {
      row <- c.grid
    } {
      System.out.println("|" + row.map { cell => if(peek(cell.is_alive)==BigInt(1)) "*" else " "}.mkString("") + "|")
    }
    System.out.println("+" + ("-" * c.grid.head.length) + "+")
    isTrace = true
  }

  test_grid_writing()
//  test_grid_reading()
//  test_blinker()
//  test_line()
}

object AddressableLifeGrid {
  def main(args: Array[String]): Unit = {
    chiselMainTest(
      Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
//            Array[String]("--backend", "dot"),
      () => Module(new AddressableLifeGrid(8, 8))
    ) {
      c => new AddressableLifeGridTests(c)
    }
  }
}
