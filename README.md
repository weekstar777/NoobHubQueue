# NoobHubQueue

Minecraft Paper 1.21.1 单服排队插件，为 NoobHub 无政府服务器设计。

## 功能

- 游戏人数限制（可配置，默认100人），超出自动排队（FIFO）
- VIP玩家优先插队
- `/play` 指令进入游戏
- 兼容 RandomSpawn、EssentialsX

## 快速开始

### 编译

双击 `compile.bat` 或运行：
```bash
mvn clean package
```

### 安装

1. 复制 `target/NoobHubQueue-1.0.0.jar` 到服务器 `plugins/` 目录
2. 重启服务器，自动生成 `plugins/NoobHubQueue/config.yml`
3. 根据需要修改配置文件（见下方）

## 配置文件

`plugins/NoobHubQueue/config.yml`：

```yaml
settings:
  # 游戏区域最大人数（可配置）
  max-game-players: 100
  
  # 检查间隔（tick，20 tick = 1秒）
  check-interval: 20
  
  # 世界名称配置
  worlds:
    lobby: "world_lobby"  # 等待大厅世界名
    game: "world"         # 游戏主世界名
    # 注：下界和末地自动识别为 world_nether 和 world_the_end

permissions:
  vip: "queue.vip"  # VIP权限节点

messages:
  # 所有消息文本均可自定义
  join-queue: "&7你已加入排队 &8| &e位置: &6{position}&e/&6{total}"
  # ... 更多消息配置
```

### 配置说明

**可配置参数**：
- `max-game-players` - 游戏人数上限（默认100）
- `check-interval` - 队列检查间隔（默认20 tick = 1秒）
- `worlds.lobby` - 等待大厅世界名（默认world_lobby）
- `worlds.game` - 游戏主世界名（默认world）
  - 下界和末地自动识别：`world` → `world_nether` + `world_the_end`
- `permissions.vip` - VIP权限节点（默认queue.vip）
- `messages.*` - 所有消息文本

### VIP配置

使用 LuckPerms：
```bash
lp group vip permission set queue.vip true
```

或使用原生权限（Paper/Bukkit）。

## 注意事项

### 生产环境配置

**重要**: `config.yml`中的`max-game-players`必须与实际游戏容量匹配
- 生产环境: 默认100人
- 测试环境: 可临时改为1-5人
- **测试完成后务必改回生产值**

### 依赖插件

#### 必需依赖
- **AuthMe** (6.0.1+): 登录系统,排队功能依赖其登录事件触发
  - 如无AuthMe,插件将无法启动

#### 可选依赖
- **LuckPerms** (5.4+): VIP权限管理
  - 未安装时自动回退到Bukkit原生权限系统
  - VIP功能仍可通过`permissions.yml`或其他权限插件配置
  
- **Multiverse-Core** (5.8.1+): 多世界管理
  - 不影响排队逻辑,仅用于世界名自动识别
  - 配置`worlds.game`可自定义游戏世界名称
  
- **EssentialsX/RandomSpawn**: 传送相关
  - 不影响核心功能,仅影响玩家首次进入游戏的出生点

### 兼容性说明

#### Multiverse-Inventories
如果使用背包分离插件,建议将`world_lobby`和`world/nether/end`配置为同一inventory-group,避免玩家传送后背包丢失。

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

#### 其他插件
- ✅ 与EssentialsX/CMI等传送插件兼容
- ✅ 与GrimAC/Vulcan等反作弊插件兼容
- ❌ 不支持BungeeCord跨服排队(本插件为单服设计)

## 常见问题

**Q: 为什么我一直在排队,但前面明明没人?**  
A: 检查`config.yml`中的`max-game-players`是否配置正确(应为100,不是1)

**Q: VIP权限不生效?**  
A: 确认LuckPerms已安装并为VIP组设置`queue.vip`权限:`lp group vip permission set queue.vip true`

**Q: 玩家卡在大厅无法进入游戏?**  
A: 检查AuthMe是否正常工作,排队系统依赖AuthMe的登录完成事件

## 项目结构

```
NoobHubQueue/
├── src/                    # 源代码
│   ├── main/java/          # Java源文件
│   └── main/resources/     # 配置文件
├── release/                # 发布版本
├── pom.xml                 # Maven配置（定义依赖）
├── mvnw.cmd + .mvn/        # Maven Wrapper（无需单独安装Maven）
├── compile.bat             # 编译脚本（中文界面）
└── README.md               # 本文件
```

## 开源协议

MIT License

---

NoobHub 无政府服务器 | 端口 1723
