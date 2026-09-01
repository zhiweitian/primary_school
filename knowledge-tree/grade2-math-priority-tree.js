// 倒树：deps = 先修（孩子）；同层多个 deps = 都要会（无序）。
(function () {
  const L = (id, name, meta, deps) => ({ id, name, leaf: true, ...meta, deps: deps || [] });
  const N = (id, name, meta, deps) => {
    if (Array.isArray(meta)) return { id, name, deps: meta };
    return { id, name, ...meta, deps: deps || [] };
  };
  const t = (volume, unit, topic) => ({ volume, unit, topic });
  const u16mul = (topic) => t("上册", "1～6的表内乘法", topic);
  const u16div = (topic) => t("上册", "1～6的表内除法", topic);
  const uTable = (topic) => t("上册", "表内乘、除法", topic);
  const uMix = (topic) => t("下册", "混合运算", topic);
  const uWan = (topic) => t("下册", "万以内数的认识", topic);
  const uSort = (topic) => t("上册", "分类与整理", topic);
  const uLen = (topic) => t("上册", "厘米和米", topic);
  const app = (file) => "../apps/grade2/" + file;

  const nodes = [
    // 乘法先修
    L("mul-know", "乘法的认识", { ...u16mul("乘法的认识"), app: app("multiplication-meaning.html") }),
    L("mul-table", "乘法口诀表", { ...uTable("乘法口诀"), app: app("multiplication-table.html") }, ["mul-know"]),
    L("mul-word", "解决简单乘法实际问题", { ...u16mul("用口诀求积"), app: app("multiplication-word.html") }, ["mul-table"]),
    L("mul-guided", "逐步提示", { ...uTable("用口诀求积"), app: app("vertical-multiplication.html?mode=guided") }, ["mul-word"]),
    L("mul-vertical", "竖式乘法", { ...uTable("用口诀求积"), app: app("vertical-multiplication.html") }, ["mul-guided"]),
    L("mul-mental1", "口算乘法", { ...uTable("两三位乘一位"), app: app("vertical-multiplication.html?mode=mental1") }, ["mul-vertical"]),
    L("mul-mental2", "口算进阶", { ...uTable("乘两位数"), app: app("vertical-multiplication.html?mode=mental2") }, ["mul-mental1"]),

    // 除法与乘除关系
    L("mul-div-rel", "乘除关系", { ...uTable("用口诀求商"), app: app("mul-div-relation.html") }),
    L("div-koujue", "口诀求商", { ...uTable("用口诀求商"), app: app("vertical-division.html") }, ["mul-div-rel", "mul-mental2"]),
    L("div-word", "除法实际问题", { ...u16div("解决问题"), app: app("division-word.html") }, ["div-koujue"]),
    L("mix-op", "混合运算", { ...uMix("混合运算"), app: app("mixed-ops.html") }, ["div-word"]),
    N("muldiv", "表内乘除", ["mix-op"]),

    // 万以内数
    L("num-read", "数的认识", { ...uWan("读写亿以内的数"), app: app("read-write-within-yi.html") }),
    L("compare-10000", "比较万以内数的大小", { ...uWan("算盘与数的大小"), app: app("compare-within-10000.html") }, ["num-read"]),
    L("round-add", "整百整千口算", { ...uWan("整百、整千数加减法"), app: app("round-addsub.html") }, ["compare-10000"]),
    L("approx-basics", "认识近似数", { ...uWan("近似数"), app: app("approx-number-basics.html") }, ["round-add"]),
    L("approx-desc", "用近似数描述数量", { ...uWan("近似数"), app: app("approx-describe-quantity.html") }, ["approx-basics"]),
    L("estimate", "估算", { ...uWan("近似数"), app: app("estimate-calc.html") }, ["approx-desc"]),
    N("wan", "万以内数", ["estimate"]),
    N("qty", "数量", ["muldiv", "wan"]),

    // 分类与长度
    L("classify", "分类与统计", { ...uSort("分类与整理"), app: app("classify-stats.html") }),
    L("len-know", "认识长度", { ...uLen("认识长度"), app: app("length-units.html") }),
    L("len-convert", "长度换算", { ...uLen("长度换算"), app: app("length-convert.html") }, ["len-know"]),
    L("len-word", "长度问题", { ...uLen("解决问题"), app: app("length-word.html") }, ["len-convert"]),
    N("stat-measure", "统计与测量", ["classify", "len-word"]),
    N("grade2", "二年级数学", ["qty", "stat-measure"])
  ];

  const byId = new Map(nodes.map((n) => [n.id, n]));

  function compile(id) {
    const n = byId.get(id);
    const { deps, ...rest } = n;
    if (deps.length) rest.children = deps.map(compile);
    return rest;
  }

  window.GRADE2_MATH_PRIORITY_TREE = {
    edition: "人教版（2024）",
    grade: 2,
    subject: "数学",
    tree: compile("grade2"),
    nodes: Object.fromEntries(nodes.map((n) => [n.id, { name: n.name, app: n.app, deps: n.deps || [] }])),
    routes: [
      {
        name: "表内乘除",
        rows: [
          ["mul-know"],
          ["mul-table"],
          ["mul-word"],
          ["mul-guided"],
          ["mul-vertical"],
          ["mul-mental1"],
          ["mul-mental2", "mul-div-rel"],
          ["div-koujue"],
          ["div-word"],
          ["mix-op"]
        ]
      },
      {
        name: "万以内数",
        rows: [
          ["num-read"],
          ["compare-10000"],
          ["round-add"],
          ["approx-basics"],
          ["approx-desc"],
          ["estimate"]
        ]
      },
      {
        name: "长度",
        rows: [
          ["len-know"],
          ["len-convert"],
          ["len-word"]
        ]
      },
      { name: "分类", rows: [["classify"]] }
    ]
  };
})();
