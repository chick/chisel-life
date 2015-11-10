#include "LifeCell.h"

void LifeCell_t::init ( val_t rand_init ) {
  this->__srand(rand_init);
  LifeCell__neighbor_sum.randomize(&__rand_seed);
  LifeCell__is_alive.randomize(&__rand_seed);
  clk.len = 1;
  clk.cnt = clk.len;
  clk.values[0] = 0;
}


int LifeCell_t::clock ( dat_t<1> reset ) {
  uint32_t min = ((uint32_t)1<<31)-1;
  if (clk.cnt < min) min = clk.cnt;
  clk.cnt-=min;
  if (clk.cnt == 0) clock_lo( reset );
  if (clk.cnt == 0) clock_hi( reset );
  if (clk.cnt == 0) clk.cnt = clk.len;
  return min;
}


void LifeCell_t::print ( FILE* f ) {
}
void LifeCell_t::print ( std::ostream& s ) {
}


void LifeCell_t::dump_init ( FILE* f ) {
}


void LifeCell_t::dump ( FILE* f, int t ) {
}




void LifeCell_t::clock_lo ( dat_t<1> reset ) {
  val_t T0;
  { T0 = LifeCell__io_top_left.values[0]+LifeCell__io_top_center.values[0];}
  T0 = T0 & 0xfL;
  val_t T1;
  { T1 = T0+LifeCell__io_top_right.values[0];}
  T1 = T1 & 0xfL;
  val_t T2;
  { T2 = T1+LifeCell__io_mid_left.values[0];}
  T2 = T2 & 0xfL;
  val_t T3;
  { T3 = T2+LifeCell__io_mid_right.values[0];}
  T3 = T3 & 0xfL;
  val_t T4;
  { T4 = T3+LifeCell__io_bot_left.values[0];}
  T4 = T4 & 0xfL;
  val_t T5;
  { T5 = T4+LifeCell__io_bot_center.values[0];}
  T5 = T5 & 0xfL;
  val_t T6;
  { T6 = T5+LifeCell__io_bot_right.values[0];}
  T6 = T6 & 0xfL;
  { T7.values[0] = TERNARY(reset.values[0], 0x0L, T6);}
  val_t T8;
  T8 = LifeCell__neighbor_sum.values[0] == 0x3L;
  val_t T9;
  T9 = LifeCell__neighbor_sum.values[0] == 0x2L;
  val_t T10;
  { T10 = T9 | T8;}
  val_t T11;
  { T11 = TERNARY_1(LifeCell__is_alive.values[0], T10, LifeCell__is_alive.values[0]);}
  val_t T12;
  T12 = LifeCell__neighbor_sum.values[0] == 0x3L;
  val_t T13;
  { T13 = LifeCell__is_alive.values[0] ^ 0x1L;}
  val_t T14;
  { T14 = TERNARY_1(T13, T12, T11);}
  { T15.values[0] = TERNARY(reset.values[0], 0x0L, T14);}
  { LifeCell__io_is_alive.values[0] = LifeCell__is_alive.values[0];}
}


void LifeCell_t::clock_hi ( dat_t<1> reset ) {
  dat_t<4> LifeCell__neighbor_sum__shadow = T7;
  dat_t<1> LifeCell__is_alive__shadow = T15;
  LifeCell__neighbor_sum = T7;
  LifeCell__is_alive = T15;
}


void LifeCell_api_t::init_sim_data (  ) {
  sim_data.inputs.clear();
  sim_data.outputs.clear();
  sim_data.signals.clear();
  LifeCell_t* mod = dynamic_cast<LifeCell_t*>(module);
  assert(mod);
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_top_left));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_top_center));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_top_right));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_mid_left));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_mid_center));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_mid_right));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_bot_left));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_bot_center));
  sim_data.inputs.push_back(new dat_api<4>(&mod->LifeCell__io_bot_right));
  sim_data.outputs.push_back(new dat_api<1>(&mod->LifeCell__io_is_alive));
  sim_data.signals.push_back(new dat_api<4>(&mod->LifeCell__neighbor_sum));
  sim_data.signal_map["LifeCell.neighbor_sum"] = 0;
  sim_data.signals.push_back(new dat_api<1>(&mod->LifeCell__is_alive));
  sim_data.signal_map["LifeCell.is_alive"] = 1;
  sim_data.clk_map["clk"] = new clk_api(&mod->clk);
}
