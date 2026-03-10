# CareerCompass Dashboard 前端复刻技术文档

## 1. 范围说明

这份文档基于仓库当前实现整理，目标是复刻登录成功后的主工作台界面，重点覆盖两块：

1. 左侧菜单栏在桌面端鼠标悬停后向右展开的呼出效果。
2. Dashboard 展示页中的卡片 UI 外观、结构和动效。


## 2. 真实页面结构

当前登录成功后的 employee 主界面不是传统统计面板，而是一个 Community Feed 页面。页面链路如下：

- Dashboard 路由入口：src/app/(app)/dashboard/page.tsx
- 登录后统一布局：src/app/(app)/layout.tsx
- 左侧菜单和顶部头部：src/components/app-sidebar.tsx
- 桌面端与移动端侧边栏动画：src/components/ui/animated-sidebar.tsx
- Feed 容器：src/components/ui/feed.tsx
- 发帖卡片：src/components/ui/create-post.tsx
- 帖子卡片：src/components/ui/post-card.tsx
- 设计 token：src/app/globals.css
- Tailwind 主题映射：tailwind.config.ts

页面层级可以概括为：

```text
AppLayout
└─ AppSidebar
   ├─ DesktopSidebar / MobileSidebar
   └─ Right Content Shell
      ├─ Sticky Header
      └─ DashboardPage
         ├─ Title Block
         └─ Feed
            ├─ CreatePost Card
            └─ PostCard List
```

## 3. 视觉基础 Token

整个界面的外观并不是靠单独页面写死颜色，而是依赖全局 CSS 变量驱动。核心 token 在 src/app/globals.css：

```css
:root {
  --background: 0 0% 100%;
  --foreground: 222.2 84% 4.9%;
  --card: 0 0% 100%;
  --card-foreground: 222.2 84% 4.9%;
  --primary: 231 48% 48%;
  --muted: 210 40% 96.1%;
  --muted-foreground: 215.4 16.3% 46.9%;
  --border: 214.3 31.8% 91.4%;
  --sidebar-background: 0 0% 100%;
}

.dark {
  --background: 20 14.3% 4.1%;
  --card: 24 9.8% 10%;
  --border: 12 6.5% 15.1%;
  --sidebar-background: 20 14.3% 4.1%;
}
```

Tailwind 再把这些 token 映射成可复用语义类：

```ts
colors: {
  background: "hsl(var(--background))",
  foreground: "hsl(var(--foreground))",
  card: {
    DEFAULT: "hsl(var(--card))",
    foreground: "hsl(var(--card-foreground))",
  },
  primary: {
    DEFAULT: "hsl(var(--primary))",
    foreground: "hsl(var(--primary-foreground))",
  },
  muted: {
    DEFAULT: "hsl(var(--muted))",
    foreground: "hsl(var(--muted-foreground))",
  },
  border: "hsl(var(--border))",
}
```

这意味着复刻时不要直接写死一套颜色值，应该先建立一层 token，后续侧边栏、卡片、按钮、hover 和暗黑模式才能统一。

## 4. 页面骨架复刻

### 4.1 整体布局规律

AppSidebar 组件承担了登录后整个应用壳层：

- 根容器是横向 flex。
- 左侧为可展开侧边栏。
- 右侧内容区是圆角左上和左下切角的大白板。
- Header 吸顶，Main 区域单独滚动。

关键类名组合：

```tsx
<div className="flex flex-col md:flex-row bg-gray-100 dark:bg-neutral-800 w-full flex-1 mx-auto border border-neutral-200 dark:border-neutral-700 overflow-hidden h-screen">
  <Sidebar open={open} setOpen={setOpen}>
    <SidebarBody className="justify-between gap-10">...</SidebarBody>
  </Sidebar>

  <div className="flex flex-1 flex-col overflow-hidden rounded-l-3xl bg-white dark:bg-neutral-900">
    <header className="sticky top-0 z-10 flex h-10 shrink-0 items-center justify-between gap-4 px-4 sm:px-6 py-2">
      ...
    </header>
    <main className="flex-1 p-4 md:p-6 overflow-auto">...</main>
  </div>
</div>
```

### 4.2 可复刻的 HTML 结构

```tsx
export function DashboardShell() {
  const [open, setOpen] = useState(false);

  return (
    <div className="flex h-screen w-full overflow-hidden border border-neutral-200 bg-gray-100 dark:border-neutral-700 dark:bg-neutral-800">
      <Sidebar open={open} setOpen={setOpen}>
        <SidebarBody className="justify-between gap-10">
          <div className="flex flex-1 flex-col overflow-y-auto overflow-x-hidden">
            <Logo />
            <nav className="mt-8 flex flex-col gap-2">
              {links.map((link) => (
                <SidebarLink key={link.href} link={link} />
              ))}
            </nav>
          </div>
        </SidebarBody>
      </Sidebar>

      <section className="flex flex-1 flex-col overflow-hidden rounded-l-3xl bg-white dark:bg-neutral-900">
        <header className="sticky top-0 z-10 flex items-center justify-between px-6 py-3">
          <div className="flex-1" />
          <div className="flex-1 flex justify-center" />
          <div className="flex-1 flex justify-end gap-4" />
        </header>
        <main className="flex-1 overflow-auto p-6">...</main>
      </section>
    </div>
  );
}
```

## 5. 左侧菜单栏 hover 向右展开效果

这是当前界面最关键的交互点，源码在 src/components/ui/animated-sidebar.tsx。

### 5.1 交互原理

桌面端侧边栏的呼出效果不是 Drawer，也不是 absolute 浮层，而是一个宽度可动画变化的固定栏：

1. 默认宽度为 70px，只展示 logo 图标和菜单 icon。
2. 鼠标移入侧栏时，`open` 变成 `true`。
3. Framer Motion 将宽度从 70px 动画过渡到 250px。
4. 文本标签的 `opacity` 从 0 切换到 1，同时 `display` 从 `none` 切到 `inline-block`。
5. 文本本身还叠加一个 `group-hover/sidebar:translate-x-1`，形成轻微向右滑动的二级反馈。

### 5.2 核心源码

```tsx
export const DesktopSidebar = ({ className, children, ...props }) => {
  const { open, setOpen, animate } = useSidebar();

  return (
    <motion.div
      className={cn(
        "h-full px-4 py-4 hidden md:flex md:flex-col bg-neutral-100 dark:bg-neutral-800 flex-shrink-0",
        className
      )}
      animate={{
        width: animate ? (open ? "250px" : "70px") : "250px",
      }}
      onMouseEnter={() => setOpen(true)}
      onMouseLeave={() => setOpen(false)}
      {...props}
    >
      {children}
    </motion.div>
  );
};
```

```tsx
export const SidebarLink = ({ link, className, ...props }) => {
  const { open, animate } = useSidebar();

  return (
    <Link
      href={link.href}
      className={cn(
        "flex items-center justify-start gap-2 group/sidebar py-2",
        className
      )}
      {...props}
    >
      {link.icon}
      <motion.span
        animate={{
          display: animate ? (open ? "inline-block" : "none") : "inline-block",
          opacity: animate ? (open ? 1 : 0) : 1,
        }}
        className="text-neutral-700 dark:text-neutral-200 text-sm group-hover/sidebar:translate-x-1 transition duration-150 whitespace-pre inline-block !p-0 !m-0"
      >
        {link.label}
      </motion.span>
    </Link>
  );
};
```

### 5.3 复刻时必须保留的细节

#### 1. 宽度动画而不是 `transform: scaleX`

这里使用 `width` 变化是为了让内部布局真实重排，文本出现后不会被压缩变形。如果用 `scaleX`，图标、文字、点击区域都容易被拉伸。

#### 2. `overflow-x-hidden`

AppSidebar 里包裹菜单列表的容器使用了：

```tsx
<div className="flex flex-col flex-1 overflow-y-auto overflow-x-hidden">
```

这一点很关键。侧边栏收起时，文字虽然仍在 DOM 内，但横向溢出会被裁掉，避免出现收起状态下的残影。

#### 3. active 态通过背景块强化

当前选中的菜单项不是单靠文字高亮，而是增加一层浅灰背景：

```tsx
className={cn(
  pathname === link.href && "bg-neutral-200 dark:bg-neutral-700 rounded-lg px-2"
)}
```

这能保证侧边栏展开和收起状态下都能稳定识别当前位置。

#### 4. Logo 双态切换

展开时显示图标加品牌名，收起时只显示图标：

```tsx
{open ? <Logo href={dashboardHref} /> : <LogoIcon href={dashboardHref} />}
```

品牌名本身也有淡入：

```tsx
<motion.span initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
  CareerCompass
</motion.span>
```

### 5.4 可直接复用的简化实现

```tsx
import { motion } from "framer-motion";
import { Home, Briefcase, User } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

const links = [
  { href: "/dashboard", label: "Dashboard", icon: <Home className="h-5 w-5" /> },
  { href: "/jobs", label: "Jobs", icon: <Briefcase className="h-5 w-5" /> },
  { href: "/profile", label: "Profile", icon: <User className="h-5 w-5" /> },
];

export function HoverExpandSidebar() {
  const [open, setOpen] = useState(false);

  return (
    <motion.aside
      className="hidden h-screen flex-shrink-0 flex-col bg-neutral-100 px-4 py-4 md:flex"
      animate={{ width: open ? 250 : 70 }}
      transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
      onMouseEnter={() => setOpen(true)}
      onMouseLeave={() => setOpen(false)}
    >
      <div className="flex flex-1 flex-col overflow-y-auto overflow-x-hidden">
        <div className="mb-8 flex items-center gap-2">
          <div className="h-6 w-6 rounded-lg bg-primary" />
          {open && <span className="whitespace-pre text-sm font-medium">CareerCompass</span>}
        </div>

        <nav className="flex flex-col gap-2">
          {links.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              className="group flex items-center gap-2 rounded-lg py-2"
            >
              {link.icon}
              <motion.span
                animate={{ opacity: open ? 1 : 0, display: open ? "inline-block" : "none" }}
                className="whitespace-pre text-sm text-neutral-700 transition duration-150 group-hover:translate-x-1"
              >
                {link.label}
              </motion.span>
            </Link>
          ))}
        </nav>
      </div>
    </motion.aside>
  );
}
```

### 5.5 移动端对应方案

当前项目在移动端不是 hover 展开，而是全屏抽屉：

```tsx
<motion.div
  initial={{ x: "-100%", opacity: 0 }}
  animate={{ x: 0, opacity: 1 }}
  exit={{ x: "-100%", opacity: 0 }}
  transition={{ duration: 0.3, ease: "easeInOut" }}
  className="fixed inset-0 z-[100] flex h-full w-full flex-col justify-between bg-white p-10 dark:bg-neutral-900"
/>
```

所以如果要完全复刻，需要保留桌面端 hover-expand 和移动端 slide-in 两套行为，而不是试图用一个交互同时覆盖所有设备。

## 6. Dashboard 展示页卡片 UI 拆解

### 6.1 当前 dashboard 的卡片类型

当前 dashboard 页面由两种卡片组成：

1. CreatePost 卡片：输入框、媒体按钮、发布操作。
2. PostCard 卡片：头像、作者信息、正文、图片、互动条、评论列表。

Feed 容器把整体内容限制在中等阅读宽度：

```tsx
<div className="space-y-4 max-w-2xl mx-auto">
  <CreatePost onPost={handleCreatePost} />
  <div className="space-y-4">
    {posts.map((post) => (
      <PostCard key={post.id} ... />
    ))}
  </div>
</div>
```

也就是说这套界面不是满屏平铺，而是中轴单列信息流，核心视觉策略是：

- 中等宽度容器提升可读性。
- 卡片之间通过 `space-y-4` 留出稳定节奏。
- 外层背景偏灰，卡片本体保持纯白或深色 panel，形成层次对比。

### 6.2 卡片壳层样式

CreatePost 和 PostCard 都沿用了同一类卡片壳层：

```tsx
<Card className="w-full rounded-2xl border border-border/50 bg-card p-4 shadow-sm">
```

这里面最关键的是四个视觉参数：

- `rounded-2xl`：大圆角，界面更现代。
- `border-border/50`：边框不重，只做结构分隔。
- `bg-card`：跟随主题 token 自动切换浅色和深色。
- `shadow-sm`：轻投影，避免信息流卡片太浮。

如果你想精准还原观感，不要把阴影做重。当前这套设计偏克制，更接近 Slack / LinkedIn 的轻量工作台卡片，而不是 Dribbble 式强浮层。

### 6.3 发帖卡片的交互质感

CreatePost 的第一层不是直接展开编辑器，而是用一个圆角胶囊按钮诱导点击：

```tsx
<button
  onClick={() => setIsExpanded(true)}
  className="w-full text-left px-4 py-3 rounded-full border border-border hover:bg-muted/50 transition-colors text-muted-foreground"
>
  Start a post...
</button>
```

这个设计有三个作用：

1. 降低输入框首次出现时的视觉噪声。
2. 通过 `rounded-full` 建立轻量、可点击的社交产品语义。
3. 通过 `hover:bg-muted/50 transition-colors` 提供轻反馈，而不打断整体页面节奏。

展开后，正文输入框反而是极简的：

```tsx
<Textarea
  className="min-h-[100px] resize-none rounded-xl border-0 bg-transparent p-0 focus-visible:ring-0 text-base"
/>
```

这类做法适合复刻，因为真正的“卡片感”由外层容器提供，输入区本身尽量不再套一层重边框。

### 6.4 帖子卡片结构

PostCard 的结构顺序非常标准，适合直接复刻：

1. Header：头像、用户名、身份说明、时间、更多菜单。
2. Content：文字正文，可选配图。
3. Stats：点赞数、评论数、分享数。
4. Actions：Like / Comment / Share 三段式操作栏。
5. Comments：输入框和评论列表折叠展开。

推荐保持这种顺序，因为它天然符合社交 feed 的阅读流。

### 6.5 卡片外观关键样式

#### 头像与标题区

```tsx
<Avatar className="h-12 w-12 cursor-pointer hover:opacity-80 transition-opacity" />

<Link className="font-semibold text-foreground hover:underline hover:text-primary transition-colors">
  {post.userName}
</Link>
```

视觉特征：

- 头像尺寸 48px。
- 头像和用户名都提供轻 hover。
- 用户名使用 `font-semibold`，不是极粗标题，避免和页面主标题抢层级。

#### 图片区

```tsx
<div className="mt-4 rounded-xl overflow-hidden">
  <img className="w-full object-cover max-h-[400px]" />
</div>
```

关键点：

- 图片容器自己带圆角并裁切。
- 高度限制在 400px 以内，避免长图破坏信息流节奏。

#### 数据区与操作区

```tsx
<div className="mt-4 flex items-center justify-between text-sm text-muted-foreground border-b border-border/50 pb-3">
  ...
</div>

<div className="mt-3 flex items-center justify-between border-b border-border/50 pb-3">
  <Button variant="ghost" size="sm" className="flex-1 gap-2 rounded-xl" />
</div>
```

这里的设计意图是：

- 用两条较淡的底边分隔不同功能层。
- 互动按钮采用 `ghost`，避免形成抢眼 CTA。
- 每个按钮都 `flex-1`，保证三列宽度一致，形成稳定节奏。

#### 评论区

```tsx
<div className="flex-1 bg-muted/50 rounded-xl p-3">
  <Link className="text-sm font-semibold hover:underline hover:text-primary transition-colors" />
  <p className="text-sm text-foreground">...</p>
</div>
```

评论气泡没有纯白背景，而是浅 muted 面板，这样能跟主卡片正文区做层级区分。

## 7. 卡片动画与微交互

### 7.1 当前 dashboard 卡片的真实动效强度

需要特别说明：当前 dashboard 的 Feed 卡片本身没有大幅 Framer Motion 入场动画，主要采用的是轻量 CSS transition。也就是说，这一页的“动感”来自微交互，而不是卡片整体飞入。

当前已有的动画包括：

- 头像 hover 透明度变化。
- 用户名 hover 变色和下划线。
- 胶囊输入入口 hover 背景变浅。
- 点赞后图标填充色变化。
- 发布按钮 loading 时 `Loader2` 的 `animate-spin`。

这种做法对工作台很合适，因为它避免信息流页面过于活跃。

### 7.2 如果要增强卡片入场动画，可复用项目中的现成方案

仓库中更强的卡片动效样例在 src/components/ui/opportunity-card.tsx：

```tsx
const cardVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.4, ease: "easeOut" },
  },
};

<motion.div
  variants={cardVariants}
  initial="hidden"
  animate="visible"
  whileHover={{ y: -4, transition: { duration: 0.2 } }}
/>
```

如果你想在复刻版 dashboard 中让 Feed 更“产品展示化”，可以把这一段嫁接到 PostCard 外层：

```tsx
import { motion } from "framer-motion";

const feedCardMotion = {
  hidden: { opacity: 0, y: 16 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.35, ease: [0.22, 1, 0.36, 1] },
  },
};

export function AnimatedFeedCard({ children }: { children: React.ReactNode }) {
  return (
    <motion.article
      variants={feedCardMotion}
      initial="hidden"
      animate="visible"
      whileHover={{ y: -3 }}
      transition={{ duration: 0.2 }}
      className="w-full rounded-2xl border border-border/50 bg-card p-4 shadow-sm"
    >
      {children}
    </motion.article>
  );
}
```

建议控制在 `y: -3` 到 `y: -4` 的范围，不要做 scale 放大，否则社交 feed 会显得太浮。

### 7.3 推荐的可复刻动画参数

如果目标是“看起来像 CareerCompass 当前版本，但再精致一点”，建议参数如下：

- 侧边栏宽度动画：`0.22s` 到 `0.28s`。
- 侧边栏文字透明度动画：`0.15s` 到 `0.2s`。
- 卡片 hover 上浮：`translateY(-3px)`。
- 按钮颜色过渡：`150ms` 到 `200ms`。
- 输入框和评论展开：尽量只用高度或透明度过渡，不要叠加缩放。

## 8. 一套可落地的复刻方案

### 8.1 技术栈

推荐保持与原项目一致：

- Next.js App Router
- React Client Components
- Tailwind CSS
- Framer Motion
- Lucide React

### 8.2 复刻步骤

1. 先建立全局 token，至少包含 `background`、`foreground`、`card`、`primary`、`muted`、`border`、`sidebar-background`。
2. 实现 `DashboardShell`，保证左侧栏和右侧内容区是同一层 flex 容器。
3. 实现 `SidebarProvider`，把 `open` 状态集中管理。
4. 用 Framer Motion 驱动桌面端宽度变化，用 `onMouseEnter/onMouseLeave` 切换状态。
5. 用 `overflow-x-hidden` 和 label 的 `opacity + display` 控制文字显隐。
6. 内容区使用 `max-w-2xl mx-auto` 单列信息流。
7. 卡片壳层统一使用大圆角、轻边框、轻阴影。
8. 交互优先做轻量 hover 和状态切换，再决定是否补充入场动画。

## 9. 可直接参考的源码位置

- 侧边栏状态和外层壳结构：src/components/app-sidebar.tsx
- 桌面端 hover 展开和移动端 slide-in：src/components/ui/animated-sidebar.tsx
- Dashboard 页面入口：src/app/(app)/dashboard/page.tsx
- Feed 布局和内容宽度：src/components/ui/feed.tsx
- 发帖卡片：src/components/ui/create-post.tsx
- 帖子卡片：src/components/ui/post-card.tsx
- 可增强的卡片 hover 动画参考：src/components/ui/opportunity-card.tsx
- 全局色板和主题 token：src/app/globals.css
- Tailwind token 映射：tailwind.config.ts

## 10. 结论

CareerCompass 登录后的 dashboard 界面本质上是一套“轻工作台 + 社交 feed”组合：

- 左侧导航通过宽度动画实现 hover 呼出，而不是浮层弹出。
- 右侧内容区通过大圆角白板和吸顶 header 建立应用壳层。
- 卡片外观遵循轻边框、轻阴影、大圆角、单列阅读宽度的策略。
- 动画克制，重点放在导航展开、按钮反馈和 hover 细节，而不是全页面强动效。

如果目标是 1:1 复刻，优先保证布局比例、圆角、边框透明度、hover 节奏和动画时长一致；这些比单纯拷贝颜色更决定最终观感。