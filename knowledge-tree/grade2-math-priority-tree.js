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
  const u79 = (topic) => t("上册", "7～9的表内乘、除法", topic);
  const uTable = (topic) => t("上册", "表内乘、除法", topic);
  const uMix = (topic) => t("下册", "混合运算", topic);
  const uDiv1 = (topic) => t("下册", "表内除法（一）", topic);
  const uDiv2 = (topic) => t("下册", "表内除法（二）", topic);
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
    L("div-know", "除法的认识", { ...u16div("除法的认识"), app: app("division-meaning.html") }),
    L("div-koujue", "口诀求商", { ...uTable("用口诀求商"), app: app("vertical-division.html") }, ["div-know", "mul-mental2"]),
    L("mul-div-rel", "乘除关系", { ...uTable("用口诀求商"), app: app("mul-div-relation.html") }, ["div-koujue"]),

    // 除法应用 → 混合运算
    L("one-step-div", "一步除法实际问题", u16div("解决问题")),
    L("avg-vs-contain", "辨析平均分与包含除", u16div("解决问题")),
    L("pic-div", "看图列除法算式", u16div("解决问题")),
    L("compare-diff", "比较多少（相差几）", uDiv2("解决问题")),
    L("choose-info", "选择有效信息", uDiv1("解决问题")),
    L("choose-mul-div", "选择乘法或除法", u79("解决问题")),
    N("div-app", "除法应用", { topic: "除法应用" }, ["choose-mul-div", "choose-info", "compare-diff", "pic-div", "avg-vs-contain", "one-step-div", "mul-div-rel"]),
    L("mul-add", "乘加运算", u79("乘加与乘减")),
    L("mul-sub", "乘减运算", u79("乘加与乘减")),
    L("order-mul-add", "运算顺序（先乘后加减）", u79("乘加与乘减")),
    N("mul-then-add", "先乘后加减", { topic: "先乘后加减" }, ["order-mul-add", "mul-sub", "mul-add", "div-app"]),
    L("only-addsub", "只有加减的混合运算", uMix("同级运算")),
    L("only-muldiv", "只有乘除的混合运算", uMix("同级运算")),
    L("left-to-right", "从左到右计算", uMix("同级运算")),
    N("same-level", "同级运算", { topic: "同级运算" }, ["left-to-right", "only-muldiv", "only-addsub", "mul-then-add"]),
    L("paren-change", "小括号改变运算顺序", uMix("含括号的运算")),
    L("paren-first", "先算括号里", uMix("含括号的运算")),
    L("two-step-mix", "两步混合运算", uMix("含括号的运算")),
    N("with-paren", "含括号的运算", { topic: "含括号的运算" }, ["two-step-mix", "paren-first", "paren-change", "same-level"]),
    L("analyze-qty", "分析数量关系", uMix("解决问题")),
    L("write-expr", "列综合算式", uMix("解决问题")),
    L("check-result", "检验计算结果", uMix("解决问题")),
    N("mix-app", "混合运算应用", uMix("解决问题"), ["check-result", "write-expr", "analyze-qty", "with-paren"]),
    N("muldiv", "表内乘除", ["mix-app"]),

    // 万以内数
    L("num-read", "数的认识", { ...uWan("读写亿以内的数"), app: app("read-write-within-yi.html") }),
    L("abacus", "用算盘表示数", uWan("算盘与数的大小")),
    L("compare-10000", "比较万以内数的大小", { ...uWan("算盘与数的大小"), app: app("compare-within-10000.html") }, ["abacus", "num-read"]),
    L("add-hundreds", "整百数加减", uWan("整百、整千数加减法"), ["compare-10000"]),
    L("add-thousands", "整千数加减", uWan("整百、整千数加减法"), ["add-hundreds"]),
    L("add-round", "口算整百整千加减", uWan("整百、整千数加减法"), ["add-thousands"]),
    L("approx-basics", "认识近似数", { ...uWan("近似数"), app: app("approx-number-basics.html") }, ["add-round"]),
    L("approx-desc", "用近似数描述数量", { ...uWan("近似数"), app: app("approx-describe-quantity.html") }, ["approx-basics"]),
    L("estimate", "估算", { ...uWan("近似数"), app: app("estimate-calc.html") }, ["approx-desc"]),
    N("wan", "万以内数", ["estimate"]),
    N("qty", "数量", ["muldiv", "wan"]),

    // 分类
    L("classify-one", "按某一标准分类", uSort("分类")),
    L("classify-multi", "按不同标准分类", uSort("分类"), ["classify-one"]),
    L("classify-result", "分类结果的整理", uSort("分类"), ["classify-multi"]),
    L("pic-data", "用图画整理数据", uSort("整理"), ["classify-result"]),
    L("table-data", "用简单表格整理", uSort("整理"), ["pic-data"]),
    L("read-stat", "读懂统计表", uSort("整理"), ["table-data"]),
    L("answer-class", "根据分类结果回答问题", uSort("解决问题")),
    L("compare-cat", "比较各类数量多少", uSort("解决问题")),
    N("data-app", "数据应用", { topic: "数据应用" }, ["compare-cat", "answer-class", "read-stat"]),

    // 长度
    L("len-know", "认识长度", { ...uLen("认识长度"), app: app("length-units.html") }),
    L("len-convert", "长度换算", { ...uLen("长度换算"), app: app("length-convert.html") }, ["len-know"]),
    L("line-know", "认识线段", uLen("线段"), ["len-convert"]),
    L("line-draw", "画线段", uLen("线段"), ["line-know"]),
    L("est-measure", "估计与测量", uLen("线段"), ["line-draw"]),
    L("choose-unit", "选择合适的长度单位", uLen("解决问题")),
    L("compare-len", "比较物体长短", uLen("解决问题")),
    L("len-word", "简单长度应用题", uLen("解决问题")),
    N("measure-app", "测量应用", { topic: "测量应用" }, ["len-word", "compare-len", "choose-unit", "est-measure"]),
    N("stat-measure", "统计与测量", ["data-app", "measure-app"]),
    N("grade2", "二年级数学", ["qty", "stat-measure"])
  ];

  const byId = new Map(nodes.map((n) => [n.id, n]));

  function compile(id) {
    const n = byId.get(id);
    const { id: _id, deps, ...rest } = n;
    if (deps.length) rest.children = deps.map(compile);
    return rest;
  }

  window.GRADE2_MATH_PRIORITY_TREE = {
    edition: "人教版（2024）",
    grade: 2,
    subject: "数学",
    tree: compile("grade2")
  };
})();
