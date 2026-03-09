# CareerCompass Login Style Guide

## 1. 概述

本风格指南基于当前登录页界面提炼而成，目标是沉淀出一套可复用、可扩展、可交付给设计与前端协作的视觉规范。该界面的核心气质可以概括为：

- 专业但不冷淡
- 现代但不过度科技化
- 轻盈、圆润、带有少量趣味感
- 用克制的品牌色和插画动效提升识别度

整体采用左右分栏结构：

- 左侧为品牌展示和角色插画区域，强调记忆点和情绪表达
- 右侧为登录操作区域，强调清晰、专注、低认知负担

适用场景：

- 登录页
- 注册页
- 忘记密码页
- 邀请登录页
- 身份认证、账号绑定、重置密码等轻流程页面

---

## 2. 设计关键词

- Calm Professional
- Friendly Motion
- Soft Geometry
- Airy Layout
- Rounded Interactive Surfaces
- Muted Neutrals with Focused Brand Accent

---

## 3. 配色方案

### 3.1 核心颜色

#### 基础中性色

- Background: `#FFFFFF`
- Foreground: `#111827`
- Muted Text: `#6B7280`
- Secondary Text: `#334155`
- Icon Neutral: `#64748B`
- Border: `rgba(148, 163, 184, 0.35)`

#### 品牌色

- Primary: `#4C57C5`
- Primary Foreground: `#F8FAFC`
- Focus Ring: `rgba(76, 87, 197, 0.12)`
- Focus Border: `rgba(76, 87, 197, 0.55)`

#### 状态色

- Error: `#DC2626`
- Error Background: `rgba(220, 38, 38, 0.10)`
- Error Border: `rgba(220, 38, 38, 0.24)`
- Success Text: `#166534`
- Success Background: `rgba(22, 101, 52, 0.10)`
- Success Border: `rgba(22, 101, 52, 0.18)`

### 3.2 背景渐变

登录页左侧主视觉背景使用中性灰渐变，不走高饱和科技风，而是偏品牌海报式氛围：

- Gradient Start: `#9AA0A7`
- Gradient Middle: `#707783`
- Gradient End: `#565D68`

辅助叠加：

- 右上柔光：`rgba(255, 255, 255, 0.14)`
- 左下柔光：`rgba(255, 255, 255, 0.12)`
- 网格线：`rgba(255, 255, 255, 0.06)`
- 大面积模糊光晕：`rgba(255, 255, 255, 0.08)`

### 3.3 插画角色配色

- Purple Character: `#6C3FF5`
- Black Character: `#2D2D2D`
- Orange Character: `#FF9B6B`
- Yellow Character: `#E8D754`
- Eye White: `#FFFFFF`
- Eye Pupil: `#2D2D2D`

### 3.4 配色使用原则

- 主体页面背景保持纯白，确保表单操作区干净稳定
- 品牌色只用于聚焦点，不大面积泛滥
- 左侧视觉区允许更强的色彩存在，右侧表单区要节制
- 所有错误和成功反馈必须有文字、底色、边框三层表达

---

## 4. 字体与排版

### 4.1 字体家族

- Primary Font: Manrope
- Fallback: sans-serif

使用原因：

- 几何感强，适合现代产品界面
- 粗细变化自然，适合标题和按钮
- 不会像极简无衬线那样过冷

### 4.2 字重规范

- 400: 辅助正文
- 500: 常规说明文字
- 600: 表单标签
- 700: 按钮、品牌名称、强调链接
- 800: 页面主标题

### 4.3 字号层级

- H1: `clamp(2rem, 4vw, 2.45rem)`
- Brand Text: `1.1rem`
- Body / Input / Button: `0.92rem` 到 `1rem`
- Secondary Text: `0.94rem` 到 `0.95rem`
- Fine Text / Error Text: `0.85rem`

### 4.4 字距与行高

- 主标题字距：`-0.05em`
- 品牌字距：`-0.02em`
- 正文字距：默认
- 标题行高：`1`
- 消息提示行高：`1.5`

### 4.5 排版风格原则

- 标题紧凑，增强视觉抓力
- 正文留白足，减少表单区域压迫感
- 次要说明统一用低对比度中灰文字
- 按钮和关键链接使用更高字重体现可点击性

---

## 5. 间距系统

### 5.1 基础间距单位

建议以 4px 为基础栅格单位，主界面常用步进如下：

- 4px
- 8px
- 10px
- 12px
- 14px
- 16px
- 20px
- 24px
- 28px
- 32px
- 40px
- 48px

### 5.2 页面级间距

- Left Hero Padding: `48px`
- Right Panel Padding: `32px`
- 移动端右侧面板 Padding: `28px 20px`
- 表单容器最大宽度: `420px`

### 5.3 模块间距

- 品牌与标题区分隔: `48px`
- 标题与表单分隔: `40px`
- 表单字段垂直间距: `20px`
- Label 与 Input 间距: `8px`
- 社交按钮与表单间距: `24px`
- 底部注册链接与按钮间距: `32px`

### 5.4 组件内边距

- Input Horizontal Padding: `16px`
- Alert Padding: `14px 16px`
- 按钮文字左右呼吸感应保留 10px 以上图文间隔

---

## 6. 布局规范

### 6.1 整体结构

- 桌面端使用双栏布局
- 宽度比例约为 `1.15fr / 0.85fr`
- 视觉区与操作区明确分离

### 6.2 左侧 Hero 区

组成：

- 顶部品牌 Logo
- 中部插画舞台
- 底部政策链接
- 背景网格和模糊光斑

布局原则：

- 插画视觉重心置中略偏下
- 顶部与底部信息轻量，不能抢占主视觉
- 背景纹理只能作为氛围，不应影响文字可读性

### 6.3 右侧 Panel 区

组成：

- 移动端 Logo
- 标题与说明
- 登录表单
- 第三方登录入口
- 注册引导

布局原则：

- 强调单列流程感
- 控件宽度统一为容器满宽
- 行为优先级从上至下递减

### 6.4 响应式策略

- `1024px` 以下隐藏左侧 Hero 区
- 右侧 Panel 区独占屏幕
- `640px` 以下辅助行转为纵向堆叠
- 移动端优先保障输入框和按钮的可点按面积

---

## 7. 圆角半径体系

该界面强调柔和与亲和力，整体圆角明显大于传统企业后台。

### 7.1 推荐圆角层级

- Micro Radius: `10px`
- Small Radius: `16px`
- Medium Radius: `24px`
- Pill Radius: `999px`
- Character Special Radius:
  - Purple Top: `10px 10px 0 0`
  - Black Top: `8px 8px 0 0`
  - Orange Arch: `120px 120px 0 0`
  - Yellow Arch: `70px 70px 0 0`

### 7.2 使用规范

- 输入框使用 `16px`
- 消息框使用 `16px`
- 品牌图标底座使用 `10px`
- 胶囊按钮一律使用 `999px`
- 大卡片或未来扩展模块可使用 `24px`

---

## 8. 阴影系统

阴影偏柔和，不使用厚重卡片感，重点模拟悬浮和可交互性。

### 8.1 现有阴影值

- Page Shadow Token: `0 30px 70px rgba(15, 23, 42, 0.14)`
- Button Default Shadow: `0 10px 30px rgba(15, 23, 42, 0.08)`
- Button Hover Shadow: `0 16px 34px rgba(15, 23, 42, 0.12)`
- Input Hairline Depth: `0 1px 0 rgba(15, 23, 42, 0.02)`

### 8.2 阴影原则

- 默认状态弱阴影
- Hover 状态略增强，但不要形成强烈悬浮错觉
- 输入框聚焦优先用边框和 focus ring，而非重阴影

---

## 9. 动效规范

### 9.1 动效气质

- 轻微
- 平滑
- 富有趣味但不喧宾夺主
- 服务于反馈，而不是炫技

### 9.2 动效时长

- Hover / Focus: `0.2s`
- Button Text Transition: `0.3s`
- Face Motion: `0.4s`
- Character Transform / Height: `0.7s`
- Eye Tracking: `0.1s`
- Blink Duration: `0.15s`

### 9.3 关键动画类型

#### 输入聚焦

- Input 轻微上浮 `translateY(-1px)`
- 边框颜色提升到品牌色透明态
- 外圈出现轻微蓝紫色光环

#### 按钮悬停

- 默认文字淡出并右移
- 悬停层滑入并显示品牌主色背景
- 箭头或图标伴随文字出现

#### 插画角色交互

- 鼠标移动时眼球跟随
- 输入邮箱时角色进入 typing 状态
- 输入密码但隐藏时，角色身体产生偏斜和探身感
- 密码可见时，角色进入“偷看”状态
- 角色随机眨眼维持生命感

### 9.4 动效原则

- 所有动画都必须可中断
- 不应阻塞表单操作
- 页面上不应同时出现过多高频运动元素

---

## 10. 组件样式规范

### 10.1 品牌 Logo 组件

组成：

- 32px 图标
- 文字品牌名

风格：

- `inline-flex`
- 图标与文字间距 `12px`
- 图标底座使用半透明白底和轻模糊
- 文字为 700 字重

适用：

- 页头
- 登录页
- 引导页
- 表单确认页

### 10.2 输入框 Input

视觉特征：

- 高度 `52px`
- 宽度 100%
- 背景近白，略带透明感
- 边框细且低对比
- 聚焦态使用品牌色边框与外环
- 圆角 `16px`

规范：

- Placeholder 使用低对比度灰
- 密码输入框预留右侧图标区
- 输入框内容区保持足够水平留白

### 10.3 Label

- 字号 `0.92rem`
- 字重 `600`
- 与输入框间距 `8px`
- 不使用全大写

### 10.4 密码显隐按钮

- 尺寸 `28px x 28px`
- 位置：输入框右侧垂直居中
- 默认透明背景
- Hover 出现浅灰背景和更高对比图标色
- 需要明确 aria-label

### 10.5 Checkbox

- 尺寸 `16px`
- 品牌色勾选高亮
- 与文本间距 `10px`
- 文本颜色偏深灰，弱于主标题强于说明文字

### 10.6 文本链接

#### 忘记密码链接

- 颜色使用 Primary
- 字重 `700`
- Hover 使用下划线

#### 页脚政策链接

- 使用深灰半透明
- Hover 颜色加深
- 保持轻量，不要抢主视觉

### 10.7 主要按钮 Primary Pill Button

视觉特征：

- 高度 `52px`
- 宽度 100%
- 圆角 `999px`
- 默认白底
- Hover 后切换为品牌色底
- 阴影轻柔

交互行为：

- 默认层与 Hover 层双层叠加
- 默认层文字在 Hover 时右移并淡出
- Hover 层淡入并归位
- Disabled 状态降低透明度并取消位移

### 10.8 次级按钮 Secondary Pill Button

适用于：

- Google 登录
- Apple 登录
- GitHub 登录

风格要求：

- 继承主按钮结构
- 默认状态保留边框
- 背景为半透明白
- Hover 行为保持一致

### 10.9 状态消息 Message / Alert

共有两类：

- Error Message
- Success Message

规范：

- 圆角 `16px`
- Padding `14px 16px`
- 文字字号 `0.92rem`
- 必须有边框和背景色区分状态
- 默认隐藏，通过 `.show` 切换显示

### 10.10 底部注册引导

风格：

- 居中排列
- 使用低对比度说明文字
- CTA 链接使用高对比文本和较高字重

---

## 11. 插画系统规范

该登录页的独特识别主要来自左侧角色插画。它不是传统插图，而是几何块面角色系统。

### 11.1 造型语言

- 基本形由矩形、半圆、圆角拱形组成
- 无复杂描边
- 通过眼睛和少量嘴部结构建立情绪
- 体块彼此前后叠放，构建立体层次

### 11.2 情绪表达方式

- 眨眼
- 眼球跟随
- 身体倾斜
- 面部微位移

### 11.3 插画应用建议

- 可复用到注册页与忘记密码页
- 不建议在密集业务页复用，以免干扰任务完成
- 可作为品牌 IP 的轻量化方向延展

---

## 12. 常用组件清单

基于此界面建议沉淀如下通用组件：

- AuthPageShell
- AuthHeroPanel
- BrandMark
- AuthFormHeader
- TextInput
- PasswordInput
- CheckboxField
- TextLink
- InteractiveHoverButton
- SocialLoginButton
- StatusAlert
- AuthFooterPrompt
- CharacterScene

---

## 13. 推荐 CSS Token

建议将该界面的核心样式沉淀为以下 Design Tokens：

```css
:root {
  --background: #ffffff;
  --foreground: #111827;
  --muted: #6b7280;
  --border: rgba(148, 163, 184, 0.35);
  --primary: #4c57c5;
  --primary-foreground: #f8fafc;
  --destructive: #dc2626;
  --destructive-soft: rgba(220, 38, 38, 0.1);
  --success: #166534;
  --success-soft: rgba(22, 101, 52, 0.1);
  --focus-ring: rgba(76, 87, 197, 0.12);
  --shadow-sm: 0 10px 30px rgba(15, 23, 42, 0.08);
  --shadow-md: 0 16px 34px rgba(15, 23, 42, 0.12);
  --shadow-lg: 0 30px 70px rgba(15, 23, 42, 0.14);
  --radius-sm: 10px;
  --radius-md: 16px;
  --radius-lg: 24px;
  --radius-pill: 999px;
}
```

---

## 14. 页面实现建议

### 14.1 适合保留的风格特征

- 白色表单区 + 中性灰主视觉区的分离结构
- 大圆角输入和胶囊按钮
- 较强标题字重与克制正文颜色
- 低频、拟人化的角色动效

### 14.2 后续扩展建议

- 为注册页补充更多字段时，保持同样的输入高度与栅格
- 第三方登录按钮增加图标统一规范
- 增加暗色模式时，不建议直接反转整个 Hero 区，而应重做背景层级
- 为无动画偏好用户增加 `prefers-reduced-motion` 降级策略

---

## 15. 总结

这套界面的本质不是“炫技登录页”，而是一个带品牌情绪的认证界面系统。它的价值在于：

- 用克制的品牌色建立识别
- 用圆润和留白提升友好度
- 用插画动效制造记忆点
- 用清晰的表单结构确保转化效率

如果要继续扩展成完整设计系统，建议优先沉淀：

- 颜色 Token
- 表单规范
- 按钮状态规范
- 消息反馈规范
- Auth 系列页面布局模板