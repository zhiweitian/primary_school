// 手工维护 · 表内乘除 / 万以内数 双线；能力阶梯：意义→口诀→关系→顺序→应用
window.GRADE2_MATH_PRIORITY_TREE = {
  "edition": "人教版（2024）",
  "grade": 2,
  "subject": "数学",
  "tree": {
    "name": "二年级数学",
    "children": [
      {
        "name": "数量",
        "children": [
          {
            "name": "表内乘除",
            "children": [
              {
                "name": "运算意义",
                "children": [
                  {
                    "name": "乘法的意义",
                    "topic": "乘法的意义",
                    "children": [
                      { "name": "理解乘法表示几个相同加数的和", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "乘法的认识" },
                      { "name": "把加法算式改写成乘法算式", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "乘法的认识" },
                      { "name": "乘法算式各部分名称（因数、积）", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "乘法的认识" }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "除法的意义",
                    "topic": "除法的意义",
                    "children": [
                      { "name": "理解平均分与除法的意义", "leaf": true, "volume": "上册", "unit": "1～6的表内除法", "topic": "除法的认识" },
                      { "name": "除法算式各部分名称（被除数、除数、商）", "leaf": true, "volume": "上册", "unit": "1～6的表内除法", "topic": "除法的认识" }
                    ],
                    "ordered": true
                  }
                ],
                "ordered": true
              },
              {
                "name": "口诀与口算",
                "children": [
                  {
                    "name": "2～6",
                    "children": [
                      {
                        "name": "2～6的乘法口诀",
                        "topic": "2～6的乘法口诀",
                        "children": [
                          { "name": "2的乘法口诀", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "2～6的乘法口诀" },
                          { "name": "3的乘法口诀", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "2～6的乘法口诀" },
                          { "name": "4的乘法口诀", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "2～6的乘法口诀" },
                          { "name": "5的乘法口诀", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "2～6的乘法口诀" },
                          { "name": "6的乘法口诀", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "2～6的乘法口诀" }
                        ],
                        "ordered": true
                      },
                      {
                        "name": "根据口诀计算乘法",
                        "leaf": true,
                        "volume": "上册",
                        "unit": "1～6的表内乘法",
                        "topic": "用口诀求积",
                        "app": "../apps/grade2/vertical-multiplication.html"
                      },
                      {
                        "name": "2～6的口诀求商",
                        "leaf": true,
                        "volume": "上册",
                        "unit": "1～6的表内除法",
                        "topic": "用2～6的口诀求商",
                        "app": "../apps/grade2/vertical-division.html"
                      }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "7～9",
                    "children": [
                      {
                        "name": "7～9的乘法口诀",
                        "topic": "7～9的乘法口诀",
                        "children": [
                          { "name": "7的乘法口诀", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "7～9的乘法口诀" },
                          { "name": "8的乘法口诀", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "7～9的乘法口诀" },
                          { "name": "9的乘法口诀", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "7～9的乘法口诀" },
                          { "name": "乘法口诀表", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "7～9的乘法口诀" }
                        ],
                        "ordered": true
                      },
                      {
                        "name": "7～9的口诀求商",
                        "leaf": true,
                        "volume": "上册",
                        "unit": "7～9的表内乘、除法",
                        "topic": "用7～9的口诀求商",
                        "app": "../apps/grade2/vertical-division.html"
                      },
                      {
                        "name": "熟练表内除法口算",
                        "leaf": true,
                        "volume": "下册",
                        "unit": "表内除法（二）",
                        "topic": "用7～9的乘法口诀求商",
                        "app": "../apps/grade2/vertical-division.html"
                      }
                    ],
                    "ordered": true
                  }
                ],
                "ordered": true
              },
              {
                "name": "乘除关系",
                "children": [
                  {
                    "name": "2～6",
                    "children": [
                      {
                        "name": "想乘法算除法",
                        "leaf": true,
                        "volume": "上册",
                        "unit": "1～6的表内除法",
                        "topic": "用2～6的口诀求商"
                      },
                      {
                        "name": "乘除法互逆关系",
                        "leaf": true,
                        "volume": "上册",
                        "unit": "1～6的表内除法",
                        "topic": "用2～6的口诀求商"
                      },
                      {
                        "name": "一图两式（乘除）",
                        "leaf": true,
                        "volume": "下册",
                        "unit": "表内除法（一）",
                        "topic": "用2～6的乘法口诀求商"
                      }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "7～9",
                    "children": [
                      {
                        "name": "乘除法关系巩固",
                        "leaf": true,
                        "volume": "上册",
                        "unit": "7～9的表内乘、除法",
                        "topic": "用7～9的口诀求商"
                      }
                    ],
                    "ordered": true
                  }
                ],
                "ordered": true
              },
              {
                "name": "运算顺序",
                "children": [
                  {
                    "name": "先乘后加减",
                    "topic": "先乘后加减",
                    "children": [
                      { "name": "乘加运算", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "乘加与乘减" },
                      { "name": "乘减运算", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "乘加与乘减" },
                      { "name": "运算顺序（先乘后加减）", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "乘加与乘减" }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "同级运算",
                    "topic": "同级运算",
                    "children": [
                      { "name": "只有加减的混合运算", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "同级运算" },
                      { "name": "只有乘除的混合运算", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "同级运算" },
                      { "name": "从左到右计算", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "同级运算" }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "含括号的运算",
                    "topic": "含括号的运算",
                    "children": [
                      { "name": "小括号改变运算顺序", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "含括号的运算" },
                      { "name": "先算括号里", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "含括号的运算" },
                      { "name": "两步混合运算", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "含括号的运算" }
                    ],
                    "ordered": true
                  }
                ],
                "ordered": true
              },
              {
                "name": "数量关系与应用",
                "children": [
                  {
                    "name": "乘法应用",
                    "topic": "乘法应用",
                    "children": [
                      { "name": "看图列乘法算式", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "用口诀求积" },
                      { "name": "解决简单乘法实际问题", "leaf": true, "volume": "上册", "unit": "1～6的表内乘法", "topic": "用口诀求积" }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "除法应用",
                    "topic": "除法应用",
                    "children": [
                      { "name": "一步除法实际问题", "leaf": true, "volume": "上册", "unit": "1～6的表内除法", "topic": "解决问题" },
                      { "name": "辨析平均分与包含除", "leaf": true, "volume": "上册", "unit": "1～6的表内除法", "topic": "解决问题" },
                      { "name": "看图列除法算式", "leaf": true, "volume": "上册", "unit": "1～6的表内除法", "topic": "解决问题" },
                      { "name": "比较多少（相差几）", "leaf": true, "volume": "下册", "unit": "表内除法（二）", "topic": "解决问题" },
                      { "name": "选择有效信息", "leaf": true, "volume": "下册", "unit": "表内除法（一）", "topic": "解决问题" },
                      { "name": "选择乘法或除法", "leaf": true, "volume": "上册", "unit": "7～9的表内乘、除法", "topic": "解决问题" }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "混合运算应用",
                    "topic": "混合运算应用",
                    "children": [
                      { "name": "分析数量关系", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "解决问题" },
                      { "name": "列综合算式", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "解决问题" },
                      { "name": "检验计算结果", "leaf": true, "volume": "下册", "unit": "混合运算", "topic": "解决问题" }
                    ],
                    "ordered": true
                  }
                ],
                "ordered": true
              }
            ],
            "ordered": true
          },
          {
            "name": "万以内数",
            "children": [
              {
                "name": "数的认识",
                "children": [
                  {
                    "name": "1000以内",
                    "topic": "1000以内数的认识",
                    "children": [
                      { "name": "数数（一千以内）", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "1000以内数的认识" },
                      { "name": "数的组成（几个百、几个十、几个一）", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "1000以内数的认识" },
                      { "name": "读写1000以内的数", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "1000以内数的认识" }
                    ],
                    "ordered": true
                  },
                  {
                    "name": "10000以内",
                    "topic": "10000以内数的认识",
                    "children": [
                      { "name": "认识计数单位“千”“万”", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "10000以内数的认识" },
                      { "name": "10个一千是一万", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "10000以内数的认识" },
                      { "name": "读写10000以内的数", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "10000以内数的认识" }
                    ],
                    "ordered": true
                  }
                ],
                "ordered": true
              },
              {
                "name": "数的表示与大小",
                "topic": "数的表示与大小",
                "children": [
                  { "name": "用算盘表示数", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "算盘与数的大小" },
                  { "name": "比较万以内数的大小", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "算盘与数的大小", "app": "../apps/grade2/compare-within-10000.html" }
                ],
                "ordered": true
              },
              {
                "name": "数的运算",
                "topic": "整百、整千数加减法",
                "children": [
                  { "name": "整百数加减", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "整百、整千数加减法" },
                  { "name": "整千数加减", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "整百、整千数加减法" },
                  { "name": "口算整百整千加减", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "整百、整千数加减法" }
                ],
                "ordered": true
              },
              {
                "name": "估计",
                "topic": "近似数与估算",
                "children": [
                  { "name": "认识近似数", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "近似数", "app": "../apps/grade2/approx-number-basics.html" },
                  { "name": "用近似数描述数量", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "近似数", "app": "../apps/grade2/approx-describe-quantity.html" },
                  { "name": "估算", "leaf": true, "volume": "下册", "unit": "万以内数的认识", "topic": "近似数", "app": "../apps/grade2/estimate-calc.html" }
                ],
                "ordered": true
              }
            ],
            "ordered": true
          }
        ],
        "ordered": true
      },
      {
        "name": "统计与测量",
        "children": [
          {
            "name": "分类与整理",
            "children": [
              {
                "name": "收集与表示数据",
                "topic": "收集与表示数据",
                "children": [
                  { "name": "按某一标准分类", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "分类" },
                  { "name": "按不同标准分类", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "分类" },
                  { "name": "分类结果的整理", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "分类" },
                  { "name": "用图画整理数据", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "整理" },
                  { "name": "用简单表格整理", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "整理" },
                  { "name": "读懂统计表", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "整理" }
                ],
                "ordered": true
              },
              {
                "name": "数据应用",
                "topic": "数据应用",
                "children": [
                  { "name": "根据分类结果回答问题", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "解决问题" },
                  { "name": "比较各类数量多少", "leaf": true, "volume": "上册", "unit": "分类与整理", "topic": "解决问题" }
                ],
                "ordered": true
              }
            ],
            "ordered": true
          },
          {
            "name": "长度测量",
            "children": [
              {
                "name": "认识厘米",
                "topic": "认识厘米",
                "children": [
                  { "name": "认识1厘米", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "认识厘米" },
                  { "name": "用直尺测量长度", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "认识厘米" },
                  { "name": "以厘米为单位记录长度", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "认识厘米" }
                ],
                "ordered": true
              },
              {
                "name": "认识米",
                "topic": "认识米",
                "children": [
                  { "name": "认识1米", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "认识米" },
                  { "name": "1米=100厘米", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "认识米" },
                  { "name": "以米为单位测量较长物体", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "认识米" }
                ],
                "ordered": true
              },
              {
                "name": "线段",
                "topic": "线段",
                "children": [
                  { "name": "认识线段", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "线段" },
                  { "name": "画线段", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "线段" },
                  { "name": "估计与测量", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "线段" }
                ],
                "ordered": true
              },
              {
                "name": "测量应用",
                "topic": "测量应用",
                "children": [
                  { "name": "选择合适的长度单位", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "解决问题" },
                  { "name": "比较物体长短", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "解决问题" },
                  { "name": "简单长度应用题", "leaf": true, "volume": "上册", "unit": "厘米和米", "topic": "解决问题" }
                ],
                "ordered": true
              }
            ],
            "ordered": true
          }
        ],
        "ordered": true
      }
    ],
    "ordered": false
  }
};
