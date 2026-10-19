# NoobHubQueue

<div align="center">

**Minecraft Paper 1.21.1 单服排队插件**

专为高流量服务器设计的轻量级排队系统，支持游戏人数限制、VIP优先通道、自动FIFO队列管理

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Paper](https://img.shields.io/badge/Paper-1.21.1-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

</div>

---

## 📋 目录

- [核心功能](#-核心功能)
- [快速开始](#-快速开始)
- [配置指南](#-配置指南)
- [命令与权限](#-命令与权限)
- [依赖关系](#-依赖关系)
- [常见问题](#-常见问题)
- [开源协议](#-开源协议)

## ✨ 核心功能

### 🎮 智能排队系统
- **游戏区域人数限制** - 可配置最大游戏人数（默认100人）
- **自动FIFO队列** - 严格先进先出，公平排队
- **VIP优先通道** - 拥有权限的玩家可插队进入游戏
- **实时队列显示** - 显示当前排队位置和总人数

### 🚀 性能与兼容
- **轻量级设计** - 最小化服务器性能开销
- **多世界支持** - 自动识别主世界、下界、末地
- **热重载配置** - 无需重启即可应用配置更改
- **离线/在线模式均支持** - 兼容正版和盗版服务器

## 🚀 快速开始

### 环境要求
- **Java**: 17+
- **服务器**: Paper 1.21.1 或更高版本
- **必需依赖**: AuthMe 6.0.1+

### 编译插件

**Windows:**
```bash
compile.bat
```

**Linux/Mac:**
```bash
./mvnw clean package
```

编译成功后，JAR文件位于: `target/NoobHubQueue-1.0.0.jar`

### 安装部署

1. 将编译好的JAR文件复制到服务器的 `plugins/` 目录
2. 重启服务器，插件自动生成配置文件
3. 编辑 `plugins/NoobHubQueue/config.yml` 根据需要调整配置
4. 执行 `/queue reload` 重载配置（或重启服务器）

## ⚙️ 配置指南

### 基础配置

配置文件位置: `plugins/NoobHubQueue/config.yml`

```yaml
settings:
  # 游戏区域最大人数
  max-game-players: 100
  
  # 队列检查间隔（tick，20 tick = 1秒）
  check-interval: 20
  
  # 世界名称配置
  worlds:
    lobby: "world_lobby"  # 等待大厅世界名
    game: "world"         # 游戏主世界名
    # 注: 下界和末地自动识别为 world_nether 和 world_the_end

permissions:
  vip: "queue.vip"  # VIP优先权限节点

messages:
  join-queue: "&7你已加入排队 &8| &e位置: &6{position}&e/&6{total}"
  leave-queue: "&c已退出排队"
  enter-game: "&a传送至游戏世界！"
  # 更多消息配置...
```

### 核心参数说明

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `max-game-players` | 100 | 游戏区域最大人数，超出自动排队 |
| `check-interval` | 20 | 队列检查间隔（tick） |
| `worlds.lobby` | world_lobby | 等待大厅世界名 |
| `worlds.game` | world | 游戏主世界名 |
| `permissions.vip` | queue.vip | VIP优先权限节点 |

### VIP配置示例

使用LuckPerms设置VIP权限:
```bash
lp group vip permission set queue.vip true
```

使用原生权限系统（permissions.yml）:
```yaml
groups:
  vip:
    permissions:
      queue.vip: true
```

## 🎯 命令与权限

### 玩家命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/play` | 无需权限 | 进入游戏（若已满则自动排队） |
| `/queue status` | 无需权限 | 查看排队状态 |
| `/queue leave` | 无需权限 | 退出排队 |

### 管理员命令

| 命令 | 权限 | 说明 |
|------|------|------|
| `/queue reload` | `queue.admin` | 重载配置文件 |
| `/queue info` | `queue.admin` | 查看系统详细信息 |
| `/queue clear` | `queue.admin` | 清空当前队列 |

### 权限节点

| 权限 | 说明 |
|------|------|
| `queue.vip` | VIP优先权限，可插队 |
| `queue.admin` | 管理员权限，使用管理命令 |

## 📦 依赖关系

### 必需依赖
- **AuthMe** (6.0.1+)
  - 用途: 登录系统，排队触发依赖其登录完成事件
  - 下载: [AuthMe GitHub](https://github.com/AuthMe/AuthMeReloaded)

### 可选依赖（增强功能）
- **LuckPerms** (5.4+) - VIP权限管理
- **Multiverse-Core** (5.8.1+) - 多世界管理
- **EssentialsX** - 传送点管理（影响出生点）

### 兼容性

✅ **兼容插件**
- EssentialsX / CMI (传送插件)
- GrimAC / Vulcan (反作弊)
- PlaceholderAPI (变量支持)
- CoreProtect (日志插件)

❌ **不兼容**
- BungeeCord/Velocity跨服系统（本插件为单服设计）

### Multiverse-Inventories配置建议

如果使用背包分离，建议将大厅和游戏世界配置为同一组:

```yaml
# plugins/Multiverse-Inventories/groups.yml
groups:
  default:
    worlds:
      - world_lobby
      - world
      - world_nether
      - world_the_end
    shares:
      all: true
```

## ❓ 常见问题

<details>
<summary><b>Q: 为什么我一直在排队，但前面明明没人？</b></summary>

检查 `config.yml` 中的 `max-game-players` 是否配置正确。如果误设为1或其他小值，会导致游戏区域一直"满员"。

**解决方法:** 
```yaml
settings:
  max-game-players: 100  # 改为实际容量
```
然后执行 `/queue reload`
</details>

<details>
<summary><b>Q: VIP权限不生效，依然需要排队？</b></summary>

确认以下配置:
1. LuckPerms中已为VIP组设置权限: `lp group vip permission set queue.vip true`
2. 玩家已加入VIP组: `lp user 玩家名 parent add vip`
3. 配置文件中权限节点正确: `permissions.vip: "queue.vip"`
</details>

<details>
<summary><b>Q: 玩家卡在大厅无法进入游戏？</b></summary>

检查AuthMe是否正常工作:
1. 确认AuthMe插件已加载
2. 确认玩家已完成登录（`/login 密码`）
3. 查看服务器日志是否有AuthMe相关错误

排队系统依赖AuthMe的登录完成事件触发。
</details>

<details>
<summary><b>Q: 如何调整测试环境的人数限制？</b></summary>

测试时可临时改小人数:
```yaml
settings:
  max-game-players: 2  # 测试用
```

**重要**: 测试完成后务必改回生产值（如100），否则会导致正常玩家无法进入。
</details>

## 🏗️ 项目结构

```
NoobHubQueue/
├── src/
│   ├── main/
│   │   ├── java/com/noobhub/queue/
│   │   │   ├── NoobHubQueue.java           # 插件主类
│   │   │   ├── commands/                   # 命令处理
│   │   │   ├── config/                     # 配置管理
│   │   │   ├── listeners/                  # 事件监听
│   │   │   └── managers/                   # 队列管理核心
│   │   └── resources/
│   │       ├── config.yml                  # 默认配置
│   │       └── plugin.yml                  # 插件元数据
├── release/                                # 发布版本
├── pom.xml                                 # Maven依赖
├── compile.bat                             # Windows编译脚本
└── README.md
```

## 🛠️ 开发

### 构建要求
- JDK 17+
- Maven 3.6+（或使用内置的Maven Wrapper）

### 本地构建
```bash
git clone https://github.com/weekstar777/NoobHubQueue.git
cd NoobHubQueue
mvn clean package
```

### 调试模式
在服务器启动参数中添加:
```bash
java -Dqueue.debug=true -jar paper.jar
```

## 📝 更新日志

### v1.0.0 (2024-10-19)
- ✨ 初始版本发布
- 🎮 完整的排队系统实现
- 👑 VIP优先通道功能
- 🌍 多世界自动识别
- ⚙️ 可配置的人数限制和消息

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议

---

<div align="center">

**NoobHub 无政府服务器** | 端口 1723

Made with ❤️ by weekstar777

</div>
