// 倒树：deps = 先修（孩子）；同层多个 deps = 都要会（无序）。
(function () {
  const L = (id, name, meta, deps) => ({ id, name, leaf: true, ...meta, deps: deps || [] });
  const N = (id, name, meta, deps) => {
    if (Array.isArray(meta)) return { id, name, deps: meta };
    return { id, name, ...meta, deps: deps || [] };
  };
  const t = (volume, unit, topic) => ({ volume, unit, topic });
  const u15 = (topic) => t("上册", "5以内数的认识和加、减法", topic);
  const u610 = (topic) => t("上册", "6～10的认识和加、减法", topic);
  const u1120 = (topic) => t("上册", "11～20的认识", topic);
  const uCarry = (topic) => t("上册", "20以内的进位加法", topic);
  const uBorrow = (topic) => t("下册", "20以内的退位减法", topic);
  const u100n = (topic) => t("下册", "100以内数的认识", topic);
  const u100m = (topic) => t("下册", "100以内的口算加、减法", topic);
  const u100w = (topic) => t("下册", "100以内的笔算加、减法", topic);
  const uRel = (topic) => t("下册", "数量间的加减关系", topic);
  const uSolid = (topic) => t("上册", "认识立体图形", topic);
  const uPlane = (topic) => t("下册", "认识平面图形", topic);

  const nodes = [
    L("num10", "10以内认数", u15("认数与比大小")),
    L("add10", "10以内加减", u610("加减法"), ["num10"]),
    L("num20", "20以内认数", u1120("十几与数位"), ["add10"]),
    L("carry", "进位加法", uCarry("凑十法"), ["num20"]),
    L("borrow", "退位减法", uBorrow("破十与想加算减"), ["carry"]),
    L("word20", "加减问题", uBorrow("解决问题"), ["borrow"]),
    N("g20", "20以内加减", ["word20"]),

    L("num100", "100以内认数", u100n("读写与组成")),
    L("cmp100", "比较大小", u100n("比较大小"), ["num100"]),
    L("mental100", "口算加减", u100m("口算加减"), ["cmp100"]),
    L("written", "竖式加减", u100w("笔算加减"), ["mental100"]),
    L("word100", "实际问题", uRel("解决问题"), ["written"]),
    N("g100", "100以内", ["word100"]),
    N("qty", "数量", ["g20", "g100"]),

    L("solid", "立体图形", uSolid("辨认与分类")),
    L("plane", "平面图形", uPlane("认识与拼组"), ["solid"]),
    N("geo", "图形", ["plane"]),
    N("grade1", "一年级数学", ["qty", "geo"])
  ];

  const byId = new Map(nodes.map((n) => [n.id, n]));

  function compile(id) {
    const n = byId.get(id);
    const { deps, ...rest } = n;
    if (deps.length) rest.children = deps.map(compile);
    return rest;
  }

  window.GRADE1_MATH_PRIORITY_TREE = {
    edition: "人教版（2024）",
    grade: 1,
    subject: "数学",
    tree: compile("grade1"),
    nodes: Object.fromEntries(nodes.map((n) => [n.id, { name: n.name, app: n.app, deps: n.deps || [] }])),
    routes: [
      {
        name: "20以内加减",
        rows: [
          ["num10"],
          ["add10"],
          ["num20"],
          ["carry"],
          ["borrow"],
          ["word20"]
        ]
      },
      {
        name: "100以内",
        rows: [
          ["num100"],
          ["cmp100"],
          ["mental100"],
          ["written"],
          ["word100"]
        ]
      },
      {
        name: "图形",
        rows: [
          ["solid"],
          ["plane"]
        ]
      }
    ]
  };
})();
