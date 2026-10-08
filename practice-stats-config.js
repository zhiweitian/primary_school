// 熟练度：近 window 题线性加权（越近越重），空位当错；衰减看更久的对题分布；下游答对可补空位。
window.PS_CONFIG = {
  window: 50,
  halfLifeDays: 14,
  strengthOkDays: 5,
  strengthSpanDays: 30,
  strengthSpanCap: 60,
  downstreamDepth: 3,
  masteredAt: 90
};
