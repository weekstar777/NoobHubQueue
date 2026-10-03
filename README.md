# NoobHubQueue - 单服排队插件

为 NoobHub 无政府服务器设计的单服务器排队系统。

## ✨ 功能特性

- ✅ **严格的FIFO排队** - 先进先出，公平排队
- ✅ **100人游戏限制** - 前100人进入游戏，后100人排队
- ✅ **VIP优先插队** - VIP玩家自动排到队列最前面
- ✅ **自动传送** - 排到时自动通知并传送进入游戏
- ✅ **新玩家随机出生** - 兼容RandomSpawn插件
- ✅ **老玩家回原位置** - 兼容Paper原生和EssentialsX
- ✅ **实时排队显示** - ActionBar实时显示排队位置
- ✅ **手动进入** - /play指令手动进入游戏

## 📋 要求

- **服务器**: Paper 1.21.1 或更高版本
- **Java**: 17 或更高版本
- **可选依赖**:
  - LuckPerms（VIP权限）
  - RandomSpawn（新玩家随机出生）
  - EssentialsX（老玩家位置管理）

## 🚀 快速开始

### 1. 编译插件

**方法A：双击编译脚本（推荐）**
```
双击 compile.bat
等待编译完成
```

**方法B：使用Maven命令**
```bash
mvn clean package
```

### 2. 安装插件

1. 找到编译后的文件：`target/NoobHubQueue-1.0.0.jar`
2. 复制到服务器的 `plugins/` 目录
3. 重启服务器

### 3. 配置插件

编辑 `plugins/NoobHubQueue/config.yml`：

```yaml
settings:
  max-game-players: 100  # 游戏区域最大人数
  lobby-location:        # 等待平台坐标
    world: world
    x: 10.5
    y: 197.0
    z: -9.5
```

### 4. 配置权限

使用 LuckPerms 给VIP玩家设置权限：

```bash
lp group vip permission set queue.vip true
```

## 🎮 玩家使用

### 普通玩家

1. 进入服务器
2. 如果人数≤100：直接进入游戏
3. 如果人数>100：传送到等待平台，加入排队
4. 等待轮到时输入 `/play` 或等待自动传送

### VIP玩家

1. 进入服务器自动插队到队列最前面
2. 优先进入游戏

### 指令

- `/play` - 进入游戏（排到时）
- 别名：`/p`, `/enter`

## ⚙️ 工作原理

```
玩家连接服务器
       ↓
   检测在线人数
       ↓
  ┌────┴────┐
  |         |
≤100人    >100人
  |         |
  ↓         ↓
进入游戏  进入等待平台
  |       加入队列
  |         ↓
  |    显示排队信息
  |         ↓
  |    等待空位
  |         ↓
  └─────┬───┘
        ↓
   自动/手动进入游戏
```

## 📊 核心特性详解

### 1. 严格的先进先出（FIFO）

使用 `LinkedList` 实现严格的排队顺序：
- 101号玩家先进入 → 排队位置 #1
- 102号玩家后进入 → 排队位置 #2
- 有人退出时，#1自动进入，#2变#1

### 2. VIP插队机制

```java
if (isVIP(player)) {
    queue.addFirst(uuid);  // 插入到队列最前面
} else {
    queue.addLast(uuid);   // 加到队列末尾
}
```

### 3. 自动检测

每秒检查：
- 当前游戏人数 < 100
- 且有排队玩家
- 自动传送队列第一位进入游戏

### 4. 兼容性设计

**新玩家**：
- 不干扰 RandomSpawn
- 让 RandomSpawn 处理随机出生

**老玩家**：
- 不干扰 Paper 原生重生机制
- 不干扰 EssentialsX 的位置管理

## 🔧 配置文件

### config.yml

```yaml
settings:
  max-game-players: 100      # 游戏人数上限
  check-interval: 20         # 检查间隔（tick）
  lobby-location:            # 等待平台坐标
    world: world
    x: 10.5
    y: 197.0
    z: -9.5
    yaw: 0.0
    pitch: 0.0

permissions:
  vip: "queue.vip"          # VIP权限节点

messages:
  join-queue: "&7你已加入排队 &8| &e位置: &6{position}&e/&6{total}"
  vip-priority: "&6[VIP] &e你已插队至第 &6{position} &e位"
  your-turn: "&a&l轮到你了！"
  # ... 更多消息配置
```

## 📝 权限节点

- `queue.vip` - VIP优先插队
- `queue.admin` - 管理员权限（预留）

## 🐛 故障排除

### 编译失败

**问题**: Maven 编译失败
**解决**: 
```bash
# 清理缓存
mvn clean
# 重新编译
mvn package
```

### 插件无法加载

**问题**: 服务器版本不匹配
**解决**: 确保使用 Paper 1.21.1+

### VIP功能不工作

**问题**: 未安装 LuckPerms
**解决**: 
1. 安装 LuckPerms 插件
2. 或使用原生权限：`/op <玩家>`

## 📦 项目结构

```
NoobHubQueue/
├── src/main/java/com/noobhub/queue/
│   ├── NoobHubQueue.java          # 主类
│   ├── managers/
│   │   └── QueueManager.java      # 队列管理器
│   ├── config/
│   │   └── ConfigManager.java     # 配置管理
│   ├── listeners/
│   │   ├── PlayerJoinListener.java
│   │   └── PlayerQuitListener.java
│   ├── commands/
│   │   └── PlayCommand.java       # /play指令
│   └── utils/
│       ├── TeleportUtil.java
│       └── MessageUtil.java
├── src/main/resources/
│   ├── plugin.yml
│   └── config.yml
├── pom.xml
├── compile.bat                     # 编译脚本
└── README.md
```

## 📄 开源协议

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 🔧 高级配置

### 双世界方案（可选）

使用两个独立世界实现更好的隔离：

**world_lobby（主城）** + **world（游戏世界）**

#### 快速配置

1. 安装 Multiverse-Core 插件
2. 创建主城虚空世界：
   ```
   /mv create world_lobby normal -g VoidGenerator
   /mv setspawn
   ```
3. 设置世界规则：
   ```
   /mv gamerule world_lobby doDaylightCycle false
   /mv modify set pvp false world_lobby
   ```

玩家在两个世界间切换时，Minecraft自动管理独立的物品栏和位置数据。

### 生产环境部署

**必要插件**：
- Multiverse-Core（多世界管理）
- WorldGuard（区域保护）
- LuckPerms（可选，VIP功能）

**数据库配置**（可选）：
```yaml
database:
  host: localhost
  port: 3306
  database: noobhub
  username: noobhub
  password: your_password
```

**保护主城等待区**：
```
/rg define lobby
/rg flag lobby pvp deny
/rg flag lobby damage deny
```

更多详细配置请参考项目 Wiki 或联系服务器管理员。

## 📮 联系方式

- 服务器：NoobHub 无政府服务器
- 端口：1723

---

**由 AI 助手开发 | 专为 NoobHub 设计**
