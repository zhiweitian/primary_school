// 倒树：叶子 = 更简单 / 先修；父节点 = 晋级。同层多个孩子 = 都要会（无序）。
window.GRADE2_MATH_PRIORITY_TREE = {
  edition: "人教版（2024）",
  grade: 2,
  subject: "数学",
  tree: {
    name: "二年级数学",
    children: [
      {
        name: "数量",
        children: [
          {
            name: "表内乘除",
            children: [
              {
                name: "混合运算应用",
                topic: "解决问题",
                volume: "下册",
                unit: "混合运算",
                children: [
                  { name: "检验计算结果", leaf: true, volume: "下册", unit: "混合运算", topic: "解决问题" },
                  { name: "列综合算式", leaf: true, volume: "下册", unit: "混合运算", topic: "解决问题" },
                  { name: "分析数量关系", leaf: true, volume: "下册", unit: "混合运算", topic: "解决问题" },
                  {
                    name: "含括号的运算",
                    topic: "含括号的运算",
                    children: [
                      { name: "两步混合运算", leaf: true, volume: "下册", unit: "混合运算", topic: "含括号的运算" },
                      { name: "先算括号里", leaf: true, volume: "下册", unit: "混合运算", topic: "含括号的运算" },
                      { name: "小括号改变运算顺序", leaf: true, volume: "下册", unit: "混合运算", topic: "含括号的运算" },
                      {
                        name: "同级运算",
                        topic: "同级运算",
                        children: [
                          { name: "从左到右计算", leaf: true, volume: "下册", unit: "混合运算", topic: "同级运算" },
                          { name: "只有乘除的混合运算", leaf: true, volume: "下册", unit: "混合运算", topic: "同级运算" },
                          { name: "只有加减的混合运算", leaf: true, volume: "下册", unit: "混合运算", topic: "同级运算" },
                          {
                            name: "先乘后加减",
                            topic: "先乘后加减",
                            children: [
                              { name: "运算顺序（先乘后加减）", leaf: true, volume: "上册", unit: "7～9的表内乘、除法", topic: "乘加与乘减" },
                              { name: "乘减运算", leaf: true, volume: "上册", unit: "7～9的表内乘、除法", topic: "乘加与乘减" },
                              { name: "乘加运算", leaf: true, volume: "上册", unit: "7～9的表内乘、除法", topic: "乘加与乘减" },
                              {
                                name: "除法应用",
                                topic: "除法应用",
                                children: [
                                  { name: "选择乘法或除法", leaf: true, volume: "上册", unit: "7～9的表内乘、除法", topic: "解决问题" },
                                  { name: "选择有效信息", leaf: true, volume: "下册", unit: "表内除法（一）", topic: "解决问题" },
                                  { name: "比较多少（相差几）", leaf: true, volume: "下册", unit: "表内除法（二）", topic: "解决问题" },
                                  { name: "看图列除法算式", leaf: true, volume: "上册", unit: "1～6的表内除法", topic: "解决问题" },
                                  { name: "辨析平均分与包含除", leaf: true, volume: "上册", unit: "1～6的表内除法", topic: "解决问题" },
                                  { name: "一步除法实际问题", leaf: true, volume: "上册", unit: "1～6的表内除法", topic: "解决问题" },
                                  {
                                    name: "乘除关系",
                                    topic: "用口诀求商",
                                    children: [
                                      { name: "一图两式（乘除）", leaf: true, volume: "下册", unit: "表内除法（一）", topic: "用2～6的乘法口诀求商" },
                                      { name: "乘除法关系巩固", leaf: true, volume: "上册", unit: "7～9的表内乘、除法", topic: "用7～9的口诀求商" },
                                      { name: "乘除法互逆关系", leaf: true, volume: "上册", unit: "1～6的表内除法", topic: "用2～6的口诀求商" },
                                      { name: "想乘法算除法", leaf: true, volume: "上册", unit: "1～6的表内除法", topic: "用2～6的口诀求商" },
                                      {
                                        name: "口诀求商",
                                        leaf: true,
                                        volume: "上册",
                                        unit: "表内乘、除法",
                                        topic: "用口诀求商",
                                        app: "../apps/grade2/vertical-division.html",
                                        children: [
                                          {
                                            name: "除法算式各部分名称（被除数、除数、商）",
                                            leaf: true,
                                            volume: "上册",
                                            unit: "1～6的表内除法",
                                            topic: "除法的认识",
                                            children: [
                                              { name: "理解平均分与除法的意义", leaf: true, volume: "上册", unit: "1～6的表内除法", topic: "除法的认识" }
                                            ]
                                          },
                                          {
                                            name: "口算进阶",
                                            leaf: true,
                                            volume: "上册",
                                            unit: "表内乘、除法",
                                            topic: "乘两位数",
                                            app: "../apps/grade2/vertical-multiplication.html?mode=mental2",
                                            children: [
                                              {
                                                name: "乘法应用",
                                                topic: "乘法应用",
                                                children: [
                                                  { name: "解决简单乘法实际问题", leaf: true, volume: "上册", unit: "1～6的表内乘法", topic: "用口诀求积" },
                                                  { name: "看图列乘法算式", leaf: true, volume: "上册", unit: "1～6的表内乘法", topic: "用口诀求积" },
                                                  {
                                                    name: "口算乘法",
                                                    leaf: true,
                                                    volume: "上册",
                                                    unit: "表内乘、除法",
                                                    topic: "两三位乘一位",
                                                    app: "../apps/grade2/vertical-multiplication.html?mode=mental1",
                                                    children: [
                                                      {
                                                        name: "竖式乘法",
                                                        leaf: true,
                                                        volume: "上册",
                                                        unit: "表内乘、除法",
                                                        topic: "用口诀求积",
                                                        app: "../apps/grade2/vertical-multiplication.html",
                                                        children: [
                                                          {
                                                            name: "逐步提示",
                                                            leaf: true,
                                                            volume: "上册",
                                                            unit: "表内乘、除法",
                                                            topic: "用口诀求积",
                                                            app: "../apps/grade2/vertical-multiplication.html?mode=guided",
                                                            children: [
                                                              {
                                                                name: "根据口诀计算乘法",
                                                                leaf: true,
                                                                volume: "上册",
                                                                unit: "表内乘、除法",
                                                                topic: "乘法口诀",
                                                                children: [
                                                                  {
                                                                    name: "乘法口诀表",
                                                                    leaf: true,
                                                                    volume: "上册",
                                                                    unit: "表内乘、除法",
                                                                    topic: "乘法口诀",
                                                                    app: "../apps/grade2/multiplication-table.html",
                                                                    children: [
                                                                      {
                                                                        name: "乘法算式各部分名称（因数、积）",
                                                                        leaf: true,
                                                                        volume: "上册",
                                                                        unit: "1～6的表内乘法",
                                                                        topic: "乘法的认识",
                                                                        children: [
                                                                          {
                                                                            name: "把加法算式改写成乘法算式",
                                                                            leaf: true,
                                                                            volume: "上册",
                                                                            unit: "1～6的表内乘法",
                                                                            topic: "乘法的认识",
                                                                            children: [
                                                                              { name: "理解乘法表示几个相同加数的和", leaf: true, volume: "上册", unit: "1～6的表内乘法", topic: "乘法的认识" }
                                                                            ]
                                                                          }
                                                                        ]
                                                                      }
                                                                    ]
                                                                  }
                                                                ]
                                                              }
                                                            ]
                                                          }
                                                        ]
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            name: "万以内数",
            children: [
              {
                name: "估算",
                leaf: true,
                volume: "下册",
                unit: "万以内数的认识",
                topic: "近似数",
                app: "../apps/grade2/estimate-calc.html",
                children: [
                  {
                    name: "用近似数描述数量",
                    leaf: true,
                    volume: "下册",
                    unit: "万以内数的认识",
                    topic: "近似数",
                    app: "../apps/grade2/approx-describe-quantity.html",
                    children: [
                      {
                        name: "认识近似数",
                        leaf: true,
                        volume: "下册",
                        unit: "万以内数的认识",
                        topic: "近似数",
                        app: "../apps/grade2/approx-number-basics.html",
                        children: [
                          {
                            name: "口算整百整千加减",
                            leaf: true,
                            volume: "下册",
                            unit: "万以内数的认识",
                            topic: "整百、整千数加减法",
                            children: [
                              {
                                name: "整千数加减",
                                leaf: true,
                                volume: "下册",
                                unit: "万以内数的认识",
                                topic: "整百、整千数加减法",
                                children: [
                                  {
                                    name: "整百数加减",
                                    leaf: true,
                                    volume: "下册",
                                    unit: "万以内数的认识",
                                    topic: "整百、整千数加减法",
                                    children: [
                                      {
                                        name: "比较万以内数的大小",
                                        leaf: true,
                                        volume: "下册",
                                        unit: "万以内数的认识",
                                        topic: "算盘与数的大小",
                                        app: "../apps/grade2/compare-within-10000.html",
                                        children: [
                                          { name: "用算盘表示数", leaf: true, volume: "下册", unit: "万以内数的认识", topic: "算盘与数的大小" },
                                          {
                                            name: "数的认识",
                                            leaf: true,
                                            volume: "下册",
                                            unit: "万以内数的认识",
                                            topic: "读写亿以内的数",
                                            app: "../apps/grade2/read-write-within-yi.html"
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      },
      {
        name: "统计与测量",
        children: [
          {
            name: "数据应用",
            topic: "数据应用",
            children: [
              { name: "比较各类数量多少", leaf: true, volume: "上册", unit: "分类与整理", topic: "解决问题" },
              { name: "根据分类结果回答问题", leaf: true, volume: "上册", unit: "分类与整理", topic: "解决问题" },
              {
                name: "读懂统计表",
                leaf: true,
                volume: "上册",
                unit: "分类与整理",
                topic: "整理",
                children: [
                  {
                    name: "用简单表格整理",
                    leaf: true,
                    volume: "上册",
                    unit: "分类与整理",
                    topic: "整理",
                    children: [
                      {
                        name: "用图画整理数据",
                        leaf: true,
                        volume: "上册",
                        unit: "分类与整理",
                        topic: "整理",
                        children: [
                          {
                            name: "分类结果的整理",
                            leaf: true,
                            volume: "上册",
                            unit: "分类与整理",
                            topic: "分类",
                            children: [
                              {
                                name: "按不同标准分类",
                                leaf: true,
                                volume: "上册",
                                unit: "分类与整理",
                                topic: "分类",
                                children: [
                                  { name: "按某一标准分类", leaf: true, volume: "上册", unit: "分类与整理", topic: "分类" }
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            name: "测量应用",
            topic: "测量应用",
            children: [
              { name: "简单长度应用题", leaf: true, volume: "上册", unit: "厘米和米", topic: "解决问题" },
              { name: "比较物体长短", leaf: true, volume: "上册", unit: "厘米和米", topic: "解决问题" },
              { name: "选择合适的长度单位", leaf: true, volume: "上册", unit: "厘米和米", topic: "解决问题" },
              {
                name: "估计与测量",
                leaf: true,
                volume: "上册",
                unit: "厘米和米",
                topic: "线段",
                children: [
                  {
                    name: "画线段",
                    leaf: true,
                    volume: "上册",
                    unit: "厘米和米",
                    topic: "线段",
                    children: [
                      {
                        name: "认识线段",
                        leaf: true,
                        volume: "上册",
                        unit: "厘米和米",
                        topic: "线段",
                        children: [
                          {
                            name: "长度换算",
                            leaf: true,
                            volume: "上册",
                            unit: "厘米和米",
                            topic: "长度换算",
                            app: "../apps/grade2/length-convert.html",
                            children: [
                              {
                                name: "认识长度",
                                leaf: true,
                                volume: "上册",
                                unit: "厘米和米",
                                topic: "认识长度",
                                app: "../apps/grade2/length-units.html"
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
};
