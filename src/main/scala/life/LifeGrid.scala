package life

import Chisel._

/**
 * Created by chick on 11/10/15.
 */
class LifeGrid(val rows: Int=2, val cols: Int = 2) extends Module {
  val io = new Bundle {val running = Bool(OUTPUT)}
  val grid = Array.fill[LifeCell](rows, cols) { Module(new LifeCell()) }

  for(row_index <- Range(0, rows)) {
    for(col_index <- Range(0, cols)) {
      val cell = grid(row_index)(col_index)
      System.out.println(s"(row,col) $row_index, $col_index")

      for(neighbor_row_delta <- Array(-1, 0, 1)) {
        for(neighbor_col_delta <- Array(-1, 0, 1)) {
          val dr = row_index + neighbor_row_delta
          val dc = col_index + neighbor_col_delta
          System.out.println(s"(row,col) ($row_index,$col_index) (dr,dc) ($dr,$dc) ")
          if(
            !(neighbor_col_delta == 0 && neighbor_row_delta == 0) &&
              (0 <= dr && dr < rows && 0 <= dc && dc < cols)
          ) {
            System.out.println(s"(row,col) ($row_index,$col_index) (dr,dc) ($dr,$dc) IN")
            val neighbor_cell = grid(row_index + neighbor_row_delta)(col_index + neighbor_col_delta)
            cell.set_neighbor(neighbor_cell, neighbor_row_delta, neighbor_col_delta)
            neighbor_cell.set_neighbor(cell, -neighbor_row_delta, -neighbor_col_delta)
          }
        }
      }
    }
  }
}

class LifeGridTests(c: LifeGrid) extends Tester(c) { self =>
  poke(c.grid(2)(2).is_alive, 1)

  step(2)

  expect(c.grid(2)(2).is_alive, BigInt(0))
}




object LifeGrid {
  def main(args: Array[String]): Unit = {
    chiselMainTest(
      //      Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
      Array[String]("--backend", "dot"),
      () => Module(new LifeGrid())
    ) {
      c => new LifeGridTests(c)
    }
  }
}
