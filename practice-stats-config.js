// 熟练度：近 window 题线性加权（越近越重），空位当错；衰减看更久的对题分布；下游答对可补空位。
// halfLifeDays：强度为 1 时，大约 95% 要 15 天才会掉到 90%（15 / ln(95/90) ≈ 277）。
// remasterSlow：隔一段时间再练回 90% 以上，下降时间乘一次。
window.PS_CONFIG = {
  window: 50,
  halfLifeDays: 277,
  remasterSlow: 2,
  strengthOkDays: 5,
  strengthSpanDays: 30,
  strengthSpanCap: 60,
  downstreamDepth: 3,
  masteredAt: 90
};
