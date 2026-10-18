// deps = 先修，可以跨年级；grade/volume/unit = 人教版（2024）课本位置。
// 四下、五下、六下新版还没出，这几册按旧版位置标。app 为空的节点显示「练习稍后」。
(function () {
  const g1 = (file) => "../apps/grade1/" + file;
  const g2 = (file) => "../apps/grade2/" + file;
  const g3 = (file) => "../apps/grade3/" + file;
  const g4 = (file) => "../apps/grade4/" + file;
  const g5 = (file) => "../apps/grade5/" + file;
  const g6 = (file) => "../apps/grade6/" + file;
  const N = (id, name, grade, volume, unit, app, deps) => ({ id, name, grade, volume, unit, app, deps: deps || [] });

  const nodes = [
    N("num10", "10以内认数", 1, "上册", "5以内数的认识和加、减法", g1("num10.html")),
    N("add10", "10以内加减", 1, "上册", "6～10的认识和加、减法", g1("add10.html"), ["num10"]),
    N("num20", "20以内认数", 1, "上册", "11～20的认识", g1("num20.html"), ["add10"]),
    N("carry", "进位加法", 1, "上册", "20以内的进位加法", g1("carry.html"), ["num20"]),
    N("borrow", "退位减法", 1, "下册", "20以内的退位减法", g1("borrow.html"), ["carry"]),
    N("word20", "加减问题", 1, "下册", "20以内的退位减法", g1("word20.html"), ["borrow"]),
    N("num100", "100以内认数", 1, "下册", "100以内数的认识", g1("num100.html"), ["num20"]),
    N("cmp100", "比较大小", 1, "下册", "100以内数的认识", g1("cmp100.html"), ["num100"]),
    N("mental100", "口算加减", 1, "下册", "100以内的口算加、减法", g1("mental100.html"), ["cmp100"]),
    N("written", "竖式加减", 1, "下册", "100以内的笔算加、减法", g1("written.html"), ["mental100"]),
    N("word100", "实际问题", 1, "下册", "数量间的加减关系", g1("word100.html"), ["written"]),
    N("solid", "立体图形", 1, "上册", "认识立体图形", g1("solid.html")),
    N("plane", "平面图形", 1, "下册", "认识平面图形", g1("plane.html"), ["solid"]),

    N("mul-know", "乘法的认识", 2, "上册", "1～6的表内乘法", g2("multiplication-meaning.html"), ["add10"]),
    N("mul-table", "乘法口诀表", 2, "上册", "表内乘、除法", g2("multiplication-table.html"), ["mul-know"]),
    N("mul-word", "解决简单乘法实际问题", 2, "上册", "1～6的表内乘法", g2("multiplication-word.html"), ["mul-table"]),
    N("div-know", "除法的认识", 2, "上册", "1～6的表内除法", g2("division-meaning.html"), ["mul-know"]),
    N("mul-div-rel", "乘除关系", 2, "上册", "表内乘、除法", g2("mul-div-relation.html"), ["mul-table", "div-know"]),
    N("div-koujue", "口算求商", 2, "上册", "表内乘、除法", g2("table-division.html"), ["mul-div-rel", "mul-word"]),
    N("div-word", "除法实际问题", 2, "上册", "1～6的表内除法", g2("division-word.html"), ["div-koujue"]),
    N("div-remainder", "有余数的除法", 2, "下册", "有余数的除法", g2("remainder-division.html"), ["div-koujue"]),
    N("clock", "认识时间", 2, "下册", "时间在哪里", g2("clock.html")),
    N("compare-10000", "比较万以内数的大小", 2, "下册", "万以内数的认识", g2("compare-within-10000.html"), ["cmp100"]),
    N("round-add", "整百整千口算", 2, "下册", "万以内数的认识", g2("round-addsub.html"), ["compare-10000"]),
    N("approx-basics", "认识近似数", 2, "下册", "万以内数的认识", g2("approx-number-basics.html"), ["round-add"]),
    N("approx-desc", "用近似数描述数量", 2, "下册", "万以内数的认识", g2("approx-describe-quantity.html"), ["approx-basics"]),
    N("estimate", "估算", 2, "下册", "万以内数的认识", g2("estimate-calc.html"), ["approx-desc"]),
    N("classify", "分类与统计", 2, "上册", "分类与整理", g2("classify-stats.html")),
    N("len-know", "认识长度", 2, "上册", "厘米和米", g2("length-units.html")),
    N("len-convert", "长度换算", 2, "上册", "厘米和米", g2("length-convert.html"), ["len-know"]),
    N("len-word", "长度问题", 2, "上册", "厘米和米", g2("length-word.html"), ["len-convert"]),

    N("mul-oral10", "口算乘一位数", 3, "上册", "多位数乘一位数", g3("mul-oral.html"), ["mul-table"]),
    N("mul-guided", "竖式乘法逐步提示", 3, "上册", "多位数乘一位数", g2("vertical-multiplication.html?mode=guided"), ["mul-oral10", "written"]),
    N("mul-vertical", "竖式乘一位数", 3, "上册", "多位数乘一位数", g2("vertical-multiplication.html"), ["mul-guided"]),
    N("mul-mental1", "只写积乘一位数", 3, "上册", "多位数乘一位数", g2("vertical-multiplication.html?mode=mental1"), ["mul-vertical"]),
    N("mix-op", "混合运算", 3, "上册", "混合运算", g2("mixed-ops.html"), ["div-word"]),
    N("div-oral", "口算除法", 3, "下册", "除数是一位数的除法", g3("div-oral.html"), ["div-koujue"]),
    N("div-vertical", "竖式除以一位数", 3, "下册", "除数是一位数的除法", g2("vertical-division.html?d=1"), ["div-oral", "div-remainder"]),
    N("frac-intro", "认识分数", 3, "上册", "分数的初步认识", g3("frac-intro.html"), ["div-know"]),
    N("frac-compare", "分数比大小", 3, "上册", "分数的初步认识", g3("frac-compare.html"), ["frac-intro"]),
    N("frac-addsub-same", "同分母分数加减", 3, "上册", "分数的初步认识", g3("frac-addsub.html"), ["frac-compare"]),
    N("frac-of-qty", "一些东西的几分之几", 3, "上册", "分数的初步认识", g3("frac-of.html"), ["frac-intro", "div-koujue"]),
    N("dec-intro", "认识小数", 3, "下册", "小数的初步认识", g3("decimal-intro.html"), ["frac-intro"]),
    N("dec-compare", "小数比大小", 3, "下册", "小数的初步认识", g3("decimal-compare.html"), ["dec-intro"]),
    N("dec-addsub1", "简单小数加减", 3, "下册", "小数的初步认识", g3("decimal-addsub.html"), ["dec-compare", "written"]),
    N("len-mm-km", "毫米、分米和千米", 3, "上册", "毫米、分米和千米", g3("length-mm-km.html"), ["len-convert"]),
    N("mass", "克、千克和吨", 3, "上册", "质量", g3("mass-units.html")),
    N("time-24", "年月日和24时计时", 3, "下册", "年、月、日", g3("time-calc.html"), ["clock"]),
    N("perimeter", "周长", 3, "下册", "长方形和正方形", g3("perimeter.html"), ["len-word", "mul-table"]),
    N("area-rect", "面积", 3, "下册", "图形的面积", g3("area.html"), ["perimeter"]),
    N("area-units", "面积单位", 3, "下册", "图形的面积", g3("area-units.html"), ["area-rect"]),
    N("angle-know", "认识角", 3, "上册", "线和角", g3("angles.html"), ["plane"]),

    N("mul-vertical2", "竖式乘两位数", 4, "上册", "多位数乘两位数", g2("vertical-multiplication.html?mode=two"), ["mul-mental1"]),
    N("mul-mental2", "只写积乘两位数", 4, "上册", "多位数乘两位数", g2("vertical-multiplication.html?mode=mental2"), ["mul-vertical2"]),
    N("div-2digit", "除数是两位数的除法", 4, "上册", "除数是两位数的除法", g2("vertical-division.html?d=2"), ["div-vertical", "mul-vertical2"]),
    N("num-read", "读写亿以内的数", 4, "上册", "万以上数的认识", g2("read-write-within-yi.html"), ["compare-10000"]),
    N("approx-big", "改写和近似数", 4, "上册", "万以上数的认识", g4("approx-big.html"), ["num-read", "approx-basics"]),
    N("qty-model", "单价·数量·总价", 4, "上册", "加法模型和乘法模型", g4("qty-model.html"), ["mul-word", "div-word"]),
    N("order-ops", "四则运算顺序", 4, "下册", "四则运算", g4("order-ops.html"), ["mix-op"]),
    N("laws", "运算定律", 4, "下册", "运算定律", g4("laws.html"), ["order-ops"]),
    N("laws-simplify", "简便计算", 4, "下册", "运算定律", g4("laws-simplify.html"), ["laws"]),
    N("dec-meaning", "小数的意义", 4, "下册", "小数的意义和性质", g4("dec-meaning.html"), ["dec-compare"]),
    N("dec-shift", "小数点移动", 4, "下册", "小数的意义和性质", g4("dec-shift.html"), ["dec-meaning"]),
    N("dec-addsub", "小数加减法", 4, "下册", "小数的加法和减法", g4("dec-addsub.html"), ["dec-meaning", "dec-addsub1"]),
    N("avg", "平均数", 4, "下册", "平均数与条形统计图", g4("avg.html"), ["div-word"]),
    N("angle-measure", "量角", 4, "上册", "角的度量", g4("angle-measure.html"), ["angle-know"]),
    N("parallel", "平行与垂直", 4, "上册", "平行四边形和梯形", g4("parallel.html"), ["angle-know"]),
    N("triangle", "三角形", 4, "下册", "三角形", g4("triangle.html"), ["angle-measure", "parallel"]),

    N("dec-mul", "小数乘法", 5, "上册", "小数乘法", g5("dec-mul.html"), ["dec-shift", "mul-vertical2"]),
    N("dec-div", "小数除法", 5, "上册", "小数除法", g5("dec-div.html"), ["dec-mul", "div-2digit"]),
    N("letters", "用字母表示数", 5, "上册", "用字母表示数和数量关系", g5("letters.html"), ["laws"]),
    N("equation", "解简易方程", 5, "上册", "用字母表示数和数量关系", g5("equation.html"), ["letters"]),
    N("area-para", "平行四边形面积", 5, "上册", "多边形的面积", g5("area-para.html"), ["area-rect", "parallel"]),
    N("area-tri", "三角形面积", 5, "上册", "多边形的面积", g5("area-tri.html"), ["area-para", "triangle"]),
    N("area-trap", "梯形面积", 5, "上册", "多边形的面积", g5("area-trap.html"), ["area-tri"]),
    N("symmetry", "轴对称、平移和旋转", 5, "上册", "图形的运动", g5("symmetry.html"), ["plane"]),
    N("factors", "因数和倍数", 5, "下册", "因数和倍数", g5("factors.html"), ["div-remainder"]),
    N("div-rules", "2、3、5的倍数特征", 5, "下册", "因数和倍数", g5("div-rules.html"), ["factors"]),
    N("prime", "质数和合数", 5, "下册", "因数和倍数", g5("prime.html"), ["factors"]),
    N("gcd-lcm", "公因数和公倍数", 5, "下册", "因数和倍数", g5("gcd-lcm.html"), ["factors"]),
    N("frac-meaning", "分数的意义", 5, "下册", "分数的意义和性质", g5("frac-meaning.html"), ["frac-of-qty"]),
    N("frac-improper", "假分数和带分数", 5, "下册", "分数的意义和性质", g5("frac-improper.html"), ["frac-meaning"]),
    N("frac-basic", "分数的基本性质", 5, "下册", "分数的意义和性质", g5("frac-basic.html"), ["frac-meaning"]),
    N("frac-reduce", "约分和通分", 5, "下册", "分数的意义和性质", g5("frac-reduce.html"), ["frac-basic", "gcd-lcm"]),
    N("frac-dec", "分数和小数互化", 5, "下册", "分数的意义和性质", g5("frac-dec.html"), ["frac-reduce", "dec-meaning"]),
    N("frac-addsub", "异分母分数加减", 5, "下册", "分数的加法和减法", g5("frac-addsub.html"), ["frac-reduce", "frac-addsub-same"]),
    N("vol-cuboid", "长方体的体积", 5, "下册", "长方体和正方体", g5("vol-cuboid.html"), ["area-rect"]),
    N("surf-cuboid", "长方体的表面积", 5, "下册", "长方体和正方体", g5("surf-cuboid.html"), ["area-rect"]),

    N("frac-mul", "分数乘法", 6, "上册", "分数乘法", g6("frac-mul.html"), ["frac-reduce", "frac-improper"]),
    N("reciprocal", "倒数", 6, "上册", "分数除法", g6("reciprocal.html"), ["frac-mul"]),
    N("frac-div", "分数除法", 6, "上册", "分数除法", g6("frac-div.html"), ["reciprocal"]),
    N("frac-word", "分数乘除应用题", 6, "上册", "分数除法", g6("frac-word.html"), ["frac-div"]),
    N("percent", "认识百分数", 6, "上册", "百分数", g6("percent.html"), ["frac-dec"]),
    N("percent-app", "百分数应用", 6, "上册", "百分数", g6("percent-app.html"), ["percent", "frac-word"]),
    N("negative", "负数", 6, "上册", "负数", g6("negative.html"), ["dec-compare"]),
    N("position", "数对和方向", 6, "上册", "确定位置", g6("position.html")),
    N("circle-c", "圆的周长", 6, "上册", "圆", g6("circle-c.html"), ["perimeter", "dec-mul"]),
    N("circle-a", "圆的面积", 6, "上册", "圆", g6("circle-a.html"), ["circle-c", "area-para"]),
    N("ratio", "比", 6, "下册", "比和比例关系", g6("ratio.html"), ["frac-div"]),
    N("ratio-share", "按比分配", 6, "下册", "比和比例关系", g6("ratio-share.html"), ["ratio"]),
    N("proportion", "比例", 6, "下册", "比和比例关系", g6("proportion.html"), ["ratio", "equation"]),
    N("direct-inverse", "正比例和反比例", 6, "下册", "比和比例关系", g6("direct-inverse.html"), ["proportion"]),
    N("scale", "比例尺", 6, "下册", "比和比例关系", g6("scale.html"), ["proportion"]),
    N("cylinder", "圆柱", 6, "下册", "圆柱与圆锥", g6("cylinder.html"), ["circle-a", "vol-cuboid", "surf-cuboid"]),
    N("cone", "圆锥", 6, "下册", "圆柱与圆锥", g6("cone.html"), ["cylinder"])
  ];

  window.MATH_TREE = {
    edition: "人教版（2024）",
    grades: { 1: "一年级", 2: "二年级", 3: "三年级", 4: "四年级", 5: "五年级", 6: "六年级" },
    nodes: Object.fromEntries(nodes.map((n) => [n.id, n])),
    routes: [
      { name: "整数", rows: [
        ["num10"], ["add10"], ["num20"],
        ["carry", "num100"], ["borrow", "cmp100"], ["word20", "mental100"],
        [null, "written"], [null, "word100"], [null, "compare-10000"],
        ["round-add", "num-read"], ["approx-basics", "approx-big"], ["approx-desc", null], ["estimate"]
      ] },
      { name: "乘除", rows: [
        ["mul-know"], ["mul-table", "div-know"], ["mul-word", "mul-div-rel"],
        ["mul-oral10", "div-koujue"], ["mul-guided", "div-remainder"], ["mul-vertical", "div-word"],
        ["mul-mental1", "div-oral"], ["mul-vertical2", "div-vertical"], ["mul-mental2", "div-2digit"]
      ] },
      { name: "运算和方程", rows: [["mix-op"], ["order-ops"], ["laws"], ["laws-simplify", "letters"], [null, "equation"]] },
      { name: "数量关系", rows: [["qty-model"], ["avg"]] },
      { name: "因数和倍数", rows: [["factors"], ["gcd-lcm", "div-rules"], [null, "prime"]] },
      { name: "分数、百分数和比", rows: [
        ["frac-intro"], ["frac-compare"], ["frac-addsub-same"], ["frac-of-qty"], ["frac-meaning"],
        ["frac-improper", "frac-basic"], ["frac-mul", "frac-reduce"], ["reciprocal", "frac-addsub"],
        ["frac-div", "frac-dec"], ["frac-word", "percent"], ["ratio", "percent-app"],
        ["ratio-share", null], ["proportion"], ["direct-inverse", "scale"]
      ] },
      { name: "小数", rows: [["dec-intro"], ["dec-compare"], ["dec-addsub1", "negative"], ["dec-meaning", null], ["dec-shift", "dec-addsub"], ["dec-mul", null], ["dec-div"]] },
      { name: "图形", rows: [["solid"], ["plane"], ["angle-know", "symmetry"], ["angle-measure", null], ["parallel"], ["triangle"]] },
      { name: "长度和面积", rows: [["len-know"], ["len-convert"], ["len-word", "len-mm-km"], ["perimeter", null], ["area-rect"], ["area-units"], ["area-para"], ["area-tri"], ["area-trap"]] },
      { name: "长方体", rows: [["surf-cuboid", "vol-cuboid"]] },
      { name: "圆和圆柱", rows: [["circle-c"], ["circle-a"], ["cylinder"], ["cone"]] },
      { name: "质量", rows: [["mass"]] },
      { name: "时间", rows: [["clock"], ["time-24"]] },
      { name: "分类", rows: [["classify"]] },
      { name: "位置", rows: [["position"]] }
    ]
  };
})();
