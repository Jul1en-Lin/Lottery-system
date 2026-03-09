# CareerCompass Auth Pages Style Guide

## 1. 文档目标

本文件基于当前登录页风格体系，继续扩展出注册页与忘记密码页的统一认证界面规范，形成一套 Auth 系列页面的可复用标准。

覆盖页面：

- Login
- Signup
- Forgot Password

目标：

- 统一认证流程视觉语言
- 统一布局骨架与组件行为
- 明确不同认证页之间的差异化内容规范
- 便于后续实现 Reset Password、Verify Email、Magic Link、Invitation Accept 等页面

---

## 2. 统一设计原则

所有认证页必须共享以下底层原则：

- 同一品牌识别：Logo、标题语气、品牌色、圆角、按钮形式一致
- 同一布局逻辑：桌面端双栏，移动端单栏
- 同一表单节奏：标题区、表单区、辅助操作区、底部跳转区
- 同一交互语义：输入聚焦、错误反馈、按钮 hover、链接状态保持一致
- 同一情绪表达：专业、柔和、清晰，少量拟人动效作为品牌记忆点

不允许出现的风格偏差：

- 某个认证页突然切回传统后台卡片风
- 某个认证页按钮圆角、阴影、字号明显不同
- 某个认证页使用完全不同的标题文案语气
- 某个认证页的字段间距、表单宽度与其他页面不一致

---

## 3. 页面层级体系

Auth 页面建议统一为四层：

### 3.1 品牌层

- 顶部 Logo
- 品牌名称 CareerCompass
- 移动端居中显示
- 桌面端显示在左侧 Hero 顶部

### 3.2 叙事层

- Hero 插画与背景氛围
- 用于传达品牌个性
- 登录页和注册页应默认保留
- 忘记密码页可保留 Hero，也可使用压缩版 Hero

### 3.3 操作层

- 标题
- 说明文案
- 表单字段
- 主按钮
- 辅助按钮或链接

### 3.4 跳转层

- 切换到登录
- 切换到注册
- 返回登录
- 条款与隐私政策

---

## 4. 通用布局模板

### 4.1 桌面端模板

- 左侧：品牌展示 Hero
- 右侧：表单操作区
- 分栏比例：`1.15fr / 0.85fr`
- 右侧表单最大宽度：`420px`

### 4.2 移动端模板

- 隐藏左侧 Hero
- 仅保留右侧 Panel
- 顶部插入移动端品牌标识
- 表单控件保持满宽

### 4.3 统一页面骨架

```text
AuthPageShell
  HeroPanel
    Brand
    CharacterScene
    PolicyLinks
  FormPanel
    MobileBrand
    AuthHeader
    AuthForm
    SecondaryAction
    FooterPrompt
```

---

## 5. Login 页面规范

### 5.1 页面目标

- 快速进入产品
- 降低认知负担
- 支持常规登录与第三方登录

### 5.2 标题与说明

- Title: Welcome back!
- Description: Please enter your details

### 5.3 字段结构

- Email
- Password
- Remember for 30 days
- Forgot password?

### 5.4 主要行为

- Primary CTA: Log in
- Secondary CTA: Log in with Google
- Footer Prompt: Don’t have an account? Sign Up

### 5.5 交互重点

- 输入邮箱触发角色 typing
- 密码显示切换触发角色偷看状态
- 错误提示在按钮上方展示

---

## 6. Signup 页面规范

### 6.1 页面目标

- 在尽量少的字段下完成注册
- 保持与登录页一致的视觉语言
- 通过角色选择帮助用户快速进入正确身份路径

### 6.2 当前实现特征

参考 [src/app/(auth)/signup/page.tsx](src/app/(auth)/signup/page.tsx#L117)：

- 使用与登录页相同的双栏布局
- 保留相同 Hero 插画与背景
- 表单宽度、输入风格、按钮风格与登录页保持一致
- 新增角色选择与条款同意模块

### 6.3 标题与说明

- Title: Create an account
- Description: Join CareerCompass to find your next opportunity

文案语气要求：

- 比登录页更积极，但不要过度营销化
- 传达进入机会平台的价值
- 保持短句，避免说明文字过长

### 6.4 字段结构

建议字段顺序：

- Role Selector
- Full Name or Company Name
- Email
- Password
- Privacy / Terms Consent

### 6.5 Signup 特有组件规范

#### 角色选择器 Role Selector

形式：

- 双选项横向分布
- 使用等宽卡片式单选项
- 每项支持整块点击

视觉规则：

- 默认边框：常规 Border Token
- 默认背景：透明或极浅中性色
- Hover：`bg-muted/50` 级别的轻背景变化
- Selected Border: Primary
- Selected Background: `Primary / 5%`
- 圆角建议：`12px` 到 `16px`
- 内边距建议：`px-4 py-3`

内容建议：

- Job Seeker
- Employer

#### 条款同意模块 Consent Row

结构：

- Checkbox + 一行或两行说明文本
- 中间可嵌入 Privacy Policy 和 Terms of Service 链接

规范：

- 文本应为常规说明色
- 链接应使用品牌色并带下划线
- Checkbox 与文本要垂直对齐
- 长文案时文本允许换行，但整体左对齐不能乱

### 6.6 主要行为

- Primary CTA: Create Account
- Footer Prompt: Already have an account? Sign in

### 6.7 交互重点

- Full Name / Company Name 输入聚焦时可继续使用 typing 动画
- 密码显隐交互沿用登录页规则
- 若角色卡片被选中，必须有清晰选中态，不可仅依赖微弱边框

### 6.8 注册页与登录页的差异边界

允许变化：

- 说明文案
- 字段数量
- 多一个角色选择器
- 多一个条款同意模块

不允许变化：

- 主要按钮风格
- 输入框高度和圆角
- 标题排版系统
- 主体布局骨架

---

## 7. Forgot Password 页面规范

### 7.1 页面目标

- 快速完成密码重置请求
- 明确告知后续动作发生在邮箱中
- 降低表单复杂度，只保留必要信息

### 7.2 当前实现特征

参考 [src/app/(auth)/forgot-password/page.tsx](src/app/(auth)/forgot-password/page.tsx#L1)：

- 当前为基础 Card 结构
- 未使用与登录页一致的 Hero + Panel 模板
- 风格上明显偏旧，不属于当前 Auth 视觉体系

### 7.3 统一改造建议

忘记密码页应纳入与登录页、注册页一致的 Auth Shell，推荐做法：

- 桌面端保留左侧 Hero
- 右侧使用与登录页相同宽度和表单样式
- 弱化字段数量，只保留单输入框流程
- 可保留更轻的说明性文案，强化“查看邮箱”预期

### 7.4 标题与说明

推荐文案：

- Title: Forgot password?
- Description: Enter your email and we’ll send you a link to reset your password.

成功后的提示文案建议：

- If an account with that email exists, we’ve sent a reset link.

### 7.5 字段结构

- Email
- Primary CTA: Send Reset Link
- Footer Prompt: Remembered your password? Sign in

### 7.6 视觉规范

忘记密码页相比登录页应更简洁，但不是更简陋：

- 继续保留相同标题字体系统
- 继续使用 `52px` 高度输入框
- 继续使用胶囊主按钮
- 可取消社交登录按钮
- 可减少页面中的辅助元素数量

### 7.7 成功反馈模式

忘记密码页建议支持两种反馈方式之一：

- 提交成功后原地显示 Success Alert
- 提交成功后跳转回登录页并展示 toast

如果采用原地反馈，建议：

- 输入框区仍保留
- 按钮文案变为 Resend Link
- 附加提示：Check your spam folder if you don’t see it soon

### 7.8 不建议继续使用的旧模式

- 独立小卡片悬浮在空白页中
- 使用与登录页完全不同的按钮和间距
- 链接通过 Button variant 假装文本链接

---

## 8. 认证页组件映射规范

### 8.1 必备通用组件

- AuthPageShell
- AuthHeroPanel
- AuthFormPanel
- AuthHeader
- BrandMark
- InteractiveHoverButton
- PasswordField
- StatusAlert
- FooterPrompt

### 8.2 按页面启用的组件

#### Login

- TextInput
- PasswordField
- RememberCheckbox
- SocialLoginButton
- ForgotPasswordLink

#### Signup

- RoleSelector
- TextInput
- PasswordField
- ConsentCheckbox

#### Forgot Password

- TextInput
- StatusAlert
- FooterPrompt

---

## 9. 页面间一致性矩阵

| 维度 | Login | Signup | Forgot Password |
|---|---|---|---|
| 双栏布局 | 是 | 是 | 应该是 |
| Hero 插画 | 是 | 是 | 建议保留 |
| 表单最大宽度 420px | 是 | 是 | 是 |
| 胶囊主按钮 | 是 | 是 | 是 |
| 输入框高度 52px | 是 | 是 | 是 |
| 品牌 Logo 位置 | 一致 | 一致 | 一致 |
| 主标题排版 | 一致 | 一致 | 一致 |
| 社交登录 | 有 | 可选 | 无 |
| 状态提示样式 | 一致 | 一致 | 一致 |

---

## 10. 推荐页面文案系统

### 10.1 Login

- Welcome back!
- Please enter your details
- Log in
- Log in with Google
- Forgot password?
- Don’t have an account? Sign Up

### 10.2 Signup

- Create an account
- Join CareerCompass to find your next opportunity
- I am a...
- Job Seeker
- Employer
- Create Account
- Already have an account? Sign in

### 10.3 Forgot Password

- Forgot password?
- Enter your email and we’ll send you a link to reset your password.
- Send Reset Link
- Remembered your password? Sign in

---

## 11. 实现建议

### 11.1 优先级

建议后续实现顺序：

1. 先把 Forgot Password 页切换到统一 Auth Shell
2. 抽出 Login / Signup 共用的 AuthLayout 组件
3. 抽出 RoleSelector、ConsentRow、FooterPrompt 等认证专用子组件

### 11.2 代码结构建议

建议抽象为：

```text
src/components/auth/
  auth-page-shell.tsx
  auth-hero-panel.tsx
  auth-form-panel.tsx
  auth-header.tsx
  auth-footer-prompt.tsx
  password-field.tsx
  role-selector.tsx
  consent-row.tsx
  status-alert.tsx
```

### 11.3 风格治理建议

- 登录页作为 Auth 视觉基准页
- 注册页允许结构增加，不允许风格漂移
- 忘记密码页必须从旧 Card 模式迁移出去

---

## 12. 结论

统一认证页的重点不是让所有页面完全一样，而是让它们在同一个系统里被识别为同一产品的一组连续流程。

这组页面应满足：

- 看起来属于同一品牌
- 用起来像同一套交互体系
- 维护时能共享同一批组件和样式 token

当前结论很明确：

- Login 是基准页
- Signup 已基本对齐
- Forgot Password 需要升级到同一视觉系统