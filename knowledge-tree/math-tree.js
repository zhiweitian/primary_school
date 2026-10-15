// deps = 先修，可以跨年级；grade/volume/unit = 人教版（2024）课本位置。
(function () {
  const g1 = (file) => "../apps/grade1/" + file;
  const g2 = (file) => "../apps/grade2/" + file;
  const N = (id, name, grade, volume, unit, app, deps) => ({ id, name, grade, volume, unit, app, deps: deps || [] });

  const nodes = [
    N("num10", "10以内认数", 1, "上册", "5以内数的认识和加、减法", g1("num10.html")),
    N("add10", "10以内加减", 1, "上册", "6～10的认识和加、减法", g1("add10.html"), ["num10"]),
    N("num20", "20以内认数", 1, "上册", "11～20的认识", g1("num20.html"), ["add10"]),
    N("carry", "进位加法", 1, "上册", "20以内的进位加法", g1("carry.html"), ["num20"]),
    N("borrow", "退位减法", 1, "下册", "20以内的退位减法", g1("borrow.html"), ["carry"]),
    N("word20", "加减问题", 1, "下册", "20以内的退位减法", g1("word20.html"), ["borrow"]),
    N("num100", "100以内认数", 1, "下册", "100以内数的认识", g1("num100.html")),
    N("cmp100", "比较大小", 1, "下册", "100以内数的认识", g1("cmp100.html"), ["num100"]),
    N("mental100", "口算加减", 1, "下册", "100以内的口算加、减法", g1("mental100.html"), ["cmp100"]),
    N("written", "竖式加减", 1, "下册", "100以内的笔算加、减法", g1("written.html"), ["mental100"]),
    N("word100", "实际问题", 1, "下册", "数量间的加减关系", g1("word100.html"), ["written"]),
    N("solid", "立体图形", 1, "上册", "认识立体图形", g1("solid.html")),
    N("plane", "平面图形", 1, "下册", "认识平面图形", g1("plane.html"), ["solid"]),

    N("mul-know", "乘法的认识", 2, "上册", "1～6的表内乘法", g2("multiplication-meaning.html")),
    N("mul-table", "乘法口诀表", 2, "上册", "表内乘、除法", g2("multiplication-table.html"), ["mul-know"]),
    N("mul-word", "解决简单乘法实际问题", 2, "上册", "1～6的表内乘法", g2("multiplication-word.html"), ["mul-table"]),
    N("mul-div-rel", "乘除关系", 2, "上册", "表内乘、除法", g2("mul-div-relation.html")),
    N("div-koujue", "口算求商", 2, "上册", "表内乘、除法", g2("table-division.html"), ["mul-div-rel", "mul-word"]),
    N("div-word", "除法实际问题", 2, "上册", "1～6的表内除法", g2("division-word.html"), ["div-koujue"]),
    N("compare-10000", "比较万以内数的大小", 2, "下册", "万以内数的认识", g2("compare-within-10000.html")),
    N("round-add", "整百整千口算", 2, "下册", "万以内数的认识", g2("round-addsub.html"), ["compare-10000"]),
    N("approx-basics", "认识近似数", 2, "下册", "万以内数的认识", g2("approx-number-basics.html"), ["round-add"]),
    N("approx-desc", "用近似数描述数量", 2, "下册", "万以内数的认识", g2("approx-describe-quantity.html"), ["approx-basics"]),
    N("estimate", "估算", 2, "下册", "万以内数的认识", g2("estimate-calc.html"), ["approx-desc"]),
    N("classify", "分类与统计", 2, "上册", "分类与整理", g2("classify-stats.html")),
    N("len-know", "认识长度", 2, "上册", "厘米和米", g2("length-units.html")),
    N("len-convert", "长度换算", 2, "上册", "厘米和米", g2("length-convert.html"), ["len-know"]),
    N("len-word", "长度问题", 2, "上册", "厘米和米", g2("length-word.html"), ["len-convert"]),

    N("mul-guided", "竖式乘法逐步提示", 3, "上册", "多位数乘一位数", g2("vertical-multiplication.html?mode=guided"), ["mul-table", "written"]),
    N("mul-vertical", "竖式乘一位数", 3, "上册", "多位数乘一位数", g2("vertical-multiplication.html"), ["mul-guided"]),
    N("mul-mental1", "只写积乘一位数", 3, "上册", "多位数乘一位数", g2("vertical-multiplication.html?mode=mental1"), ["mul-vertical"]),
    N("mix-op", "混合运算", 3, "上册", "混合运算", g2("mixed-ops.html"), ["div-word"]),

    N("mul-vertical2", "竖式乘两位数", 4, "上册", "多位数乘两位数", g2("vertical-multiplication.html?mode=two"), ["mul-mental1"]),
    N("mul-mental2", "只写积乘两位数", 4, "上册", "多位数乘两位数", g2("vertical-multiplication.html?mode=mental2"), ["mul-vertical2"]),
    N("num-read", "读写亿以内的数", 4, "上册", "万以上数的认识", g2("read-write-within-yi.html"), ["compare-10000"])
  ];

  window.MATH_TREE = {
    edition: "人教版（2024）",
    grades: { 1: "一年级", 2: "二年级", 3: "三年级", 4: "四年级" },
    nodes: Object.fromEntries(nodes.map((n) => [n.id, n])),
    routes: [
      { grade: 1, name: "20以内加减", rows: [["num10"], ["add10"], ["num20"], ["carry"], ["borrow"], ["word20"]] },
      { grade: 1, name: "100以内", rows: [["num100"], ["cmp100"], ["mental100"], ["written"], ["word100"]] },
      { grade: 1, name: "图形", rows: [["solid"], ["plane"]] },

      { grade: 2, name: "表内乘除", rows: [["mul-know"], ["mul-table"], ["mul-word", "mul-div-rel"], ["div-koujue"], ["div-word"]] },
      { grade: 2, name: "万以内数", rows: [["compare-10000"], ["round-add"], ["approx-basics"], ["approx-desc"], ["estimate"]] },
      { grade: 2, name: "长度", rows: [["len-know"], ["len-convert"], ["len-word"]] },
      { grade: 2, name: "分类", rows: [["classify"]] },

      { grade: 3, name: "多位数乘一位数", rows: [["mul-table", "written"], ["mul-guided"], ["mul-vertical"], ["mul-mental1"]] },
      { grade: 3, name: "混合运算", rows: [["div-word"], ["mix-op"]] },

      { grade: 4, name: "多位数乘两位数", rows: [["mul-mental1"], ["mul-vertical2"], ["mul-mental2"]] },
      { grade: 4, name: "大数的认识", rows: [["compare-10000"], ["num-read"]] }
    ]
  };
})();
