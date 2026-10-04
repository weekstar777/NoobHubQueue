# NoobHubQueue 权限系统说明

## 权限分组

目前插件有 **2个权限节点**，对应 **2个分组**：

### 1. 普通玩家（默认）
- **权限节点**: 无
- **权限**: 
  - 可以进入服务器
  - 可以排队（普通通道）
  - 可以使用 `/play` 指令
- **排队优先级**: 普通
- **说明**: 所有玩家默认拥有此权限，无需配置

### 2. VIP玩家
- **权限节点**: `queue.vip`
- **权限**:
  - 可以进入服务器
  - 可以排队（**VIP通道**，优先于普通通道）
  - 可以使用 `/play` 指令
- **排队优先级**: **高**（自动插队到VIP通道队首）
- **说明**: 需要手动授予权限

### 3. 管理员（预留）
- **权限节点**: `queue.admin`
- **权限**: 
  - 拥有所有权限
  - 预留给未来的管理命令
- **默认**: OP玩家自动拥有
- **说明**: 目前未实现管理命令，仅作预留

---

## 可用命令

| 命令 | 权限要求 | 说明 | 别名 |
|------|---------|------|------|
| `/play` | 无 | 进入游戏世界（排到时） | `/p`, `/enter` |

**注意**: `/play` 命令所有玩家都能使用，但效果取决于排队状态：
- 在队列中：提示"还没轮到你"
- 轮到你了：传送进入游戏世界
- 已在游戏世界：提示"你已经在游戏世界了"

---

## 如何给玩家设置VIP权限

### 方法一：使用 LuckPerms（推荐）

#### 1. 创建VIP权限组（首次）
```bash
/lp creategroup vip
/lp group vip permission set queue.vip true
```

#### 2. 给玩家分配VIP组
```bash
# 永久VIP
/lp user <玩家名> parent add vip

# 临时VIP（7天）
/lp user <玩家名> parent add vip 7d

# 移除VIP
/lp user <玩家名> parent remove vip
```

#### 3. 检查玩家权限
```bash
/lp user <玩家名> permission check queue.vip
```

---

### 方法二：使用 Paper/Bukkit 原生权限

#### 1. 使用OP权限（不推荐）
```bash
/op <玩家名>
```
**缺点**: OP玩家拥有所有权限，包括服务器管理权限

#### 2. 使用权限插件（PermissionsEx、GroupManager等）
每个插件语法不同，请查阅对应插件文档。

---

### 方法三：直接授予单个权限（临时）

如果安装了 LuckPerms：
```bash
# 永久授予
/lp user <玩家名> permission set queue.vip true

# 临时授予（7天）
/lp user <玩家名> permission set queue.vip true 7d
```

---

## 权限配置文件

在 `config.yml` 中可以修改VIP权限节点名称：

```yaml
permissions:
  vip: "queue.vip"  # 修改为自定义权限节点
```

修改后重启服务器生效。

---

## 排队机制说明

### 双通道系统

```
玩家进入服务器
       ↓
   检查权限
       ↓
  ┌────┴────┐
  |         |
VIP权限   无VIP权限
  |         |
  ↓         ↓
VIP通道   普通通道
  |         |
  └─────┬───┘
        ↓
   按通道优先级
   依次进入游戏
```

### 优先级规则

1. **VIP通道优先**: VIP通道的玩家总是优先进入游戏
2. **通道内FIFO**: 同一通道内严格按先来后到顺序
3. **动态插队**: 新VIP玩家加入时插入VIP通道队尾（仍优先于所有普通玩家）

### 示例

假设当前排队情况：
- VIP通道：玩家A（1号）、玩家B（2号）
- 普通通道：玩家C（1号）、玩家D（2号）、玩家E（3号）

**进入顺序**: A → B → C → D → E

新VIP玩家F加入：
- VIP通道：A（1号）、B（2号）、**F（3号）**
- 普通通道：C（1号）、D（2号）、E（3号）

**新进入顺序**: A → B → **F** → C → D → E

---

## 常见问题

### Q1: 如何批量设置VIP？
**A**: 使用LuckPerms批量命令：
```bash
/lp bulkupdate users parent add vip
```
或写脚本循环执行。

### Q2: VIP权限可以继承吗？
**A**: 可以。在LuckPerms中创建继承关系：
```bash
# 创建SVIP组，继承VIP组
/lp creategroup svip
/lp group svip parent add vip
```

### Q3: 如何查看所有VIP玩家？
**A**: 
```bash
/lp group vip listmembers
```

### Q4: VIP权限失效怎么办？
**A**: 检查步骤：
1. 检查权限是否正确授予：`/lp user <玩家名> permission check queue.vip`
2. 检查LuckPerms是否加载：`/lp info`
3. 检查服务器日志是否有错误
4. 重启服务器

### Q5: 可以设置不同等级的VIP吗？
**A**: 目前插件只有一个VIP通道。如需多级VIP，需要修改插件代码添加更多权限节点（如 `queue.vip.premium`）。

---

## 技术说明

### 权限检查优先级

1. **LuckPerms API** - 如果安装了LuckPerms，优先使用其API
2. **Bukkit Permission** - 否则使用Bukkit原生权限系统

### 代码实现位置

- 权限检查: `QueueManager.java` 的 `isVIP()` 方法
- 权限节点配置: `config.yml` 的 `permissions.vip`
- 权限定义: `plugin.yml` 的 `permissions` 部分

---

**最后更新**: 2026-10-03  
**插件版本**: 1.0.0
