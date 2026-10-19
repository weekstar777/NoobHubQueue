# NoobHubQueue

Minecraft Paper 1.21.1 单服排队插件

## ✨ 功能特性

- 游戏区域人数限制（默认100人），超出自动排队
- 严格FIFO队列，公平排队
- VIP优先通道（需权限 `queue.vip`）
- 实时显示排队位置
- 离线/在线模式均支持

## 🚀 快速开始

### 编译

**Windows:**
```bash
compile.bat
```

**Linux/Mac:**
```bash
./mvnw clean package
```

JAR文件: `release/NoobHubQueue-1.0.0.jar`

### 安装

1. 复制JAR到服务器 `plugins/` 目录
2. 重启服务器，自动生成配置文件
3. 编辑 `plugins/NoobHubQueue/config.yml`
4. 执行 `/queue reload` 或重启

## ⚙️ 配置

`plugins/NoobHubQueue/config.yml`:

```yaml
settings:
  max-game-players: 100    # 游戏区最大人数
  check-interval: 20       # 检查间隔(tick)
  worlds:
    lobby: "world_lobby"   # 大厅世界名
    game: "world"          # 游戏世界名

permissions:
  vip: "queue.vip"         # VIP权限节点

messages:
  # 所有消息可自定义
  join-queue: "&7你已加入排队 &8| &e位置: &6{position}&e/&6{total}"
```

## 🎯 命令

| 命令 | 说明 |
|------|------|
| `/play` | 进入游戏（若满员则排队） |
| `/queue status` | 查看排队状态 |
| `/queue reload` | 重载配置（管理员） |

## 🔑 权限

| 权限 | 说明 |
|------|------|
| `queue.vip` | VIP优先权限 |
| `queue.admin` | 管理员权限 |

**LuckPerms设置VIP:**
```bash
lp group vip permission set queue.vip true
lp user <玩家名> parent add vip
```

## 📦 依赖

**必需:**
- Paper 1.21.1+
- Java 17+
- AuthMe 6.0.1+ （登录系统）

**可选:**
- LuckPerms 5.4+ （权限管理）
- Multiverse-Core 5.8.1+ （多世界）

## ⚠️ 注意事项

- `max-game-players` 必须与实际容量匹配
- 测试环境可改小人数，**生产环境务必改回**
- 排队依赖AuthMe的登录完成事件

## 📝 许可证

MIT License

---

NoobHub 无政府服务器 | 端口 1723
