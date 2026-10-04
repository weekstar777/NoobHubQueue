# NoobHubQueue 权限查询指南

## 查看插件定义的所有权限

插件定义的权限节点在 `plugin.yml` 中：

```yaml
permissions:
  queue.vip:
    description: VIP priority in queue
    default: false
  queue.admin:
    description: Admin commands (预留)
    default: op
```

**当前可用权限**：
- `queue.vip` - VIP排队优先权
- `queue.admin` - 管理员权限（预留，暂无功能）

---

## 查看服务器中玩家权限状态

### 方法一：使用 LuckPerms 查询（推荐）

#### 1. 查看特定玩家的所有权限
```bash
/lp user <玩家名> info
```
显示玩家的：
- 所属权限组
- 直接授予的权限
- 继承的权限

#### 2. 查看玩家是否有VIP权限
```bash
/lp user <玩家名> permission check queue.vip
```
返回结果：
- `true` - 有VIP权限
- `false` - 无VIP权限

#### 3. 查看玩家的所有权限列表
```bash
/lp user <玩家名> permission info
```

#### 4. 查看VIP组的所有成员
```bash
/lp group vip listmembers
```
列出所有拥有VIP权限的玩家。

#### 5. 查看所有权限组
```bash
/lp listgroups
```

#### 6. 查看在线玩家的权限
```bash
/lp user <玩家名> meta info
```

---

### 方法二：使用 Bukkit/Paper 原生命令

#### 1. 测试玩家权限
```bash
/testfor @a[tag=queue.vip]
```
**注意**: 原生命令功能有限，推荐使用LuckPerms。

#### 2. 查看OP列表
```bash
/ops
```
显示所有OP玩家（OP玩家默认拥有所有权限）。

---

### 方法三：游戏内插件命令（部分权限插件支持）

如果使用其他权限插件：

#### PermissionsEx
```bash
/pex user <玩家名> check queue.vip
/pex user <玩家名> list
```

#### GroupManager
```bash
/manuaddp <玩家名> queue.vip
/manucheckp <玩家名> queue.vip
```

---

## 批量查询所有玩家权限

### 使用 LuckPerms 导出

#### 1. 导出所有玩家数据
```bash
/lp export <文件名>
```
例如：`/lp export backup`

生成文件位置：`plugins/LuckPerms/export/<文件名>.json.gz`

#### 2. 查看VIP组成员（在线+离线）
```bash
/lp group vip listmembers
```

#### 3. 搜索拥有特定权限的玩家
```bash
/lp search permission queue.vip
```
列出所有直接或间接拥有 `queue.vip` 权限的玩家。

---

## 游戏内可视化查询

### 使用 LuckPerms Web Editor（最直观）

#### 1. 生成Web编辑器链接
```bash
/lp editor
```

#### 2. 点击链接打开网页
在网页中可以：
- 查看所有玩家的权限
- 查看所有权限组
- 可视化编辑权限
- 批量操作

#### 3. 查看特定玩家
```bash
/lp user <玩家名> editor
```
直接打开该玩家的权限编辑页面。

---

## 查看当前排队状态（插件内部）

虽然插件没有提供管理命令，但可以从日志查看：

### 1. 查看服务器日志
```bash
tail -f logs/latest.log
```

日志会显示：
```
[NoobHubQueue] [VIP] 玩家A 加入VIP通道，位置: 1
[NoobHubQueue] [普通] 玩家B 加入普通通道，位置: 1
```

### 2. 玩家自己查看排队状态
- 玩家进入排队时会收到消息提示当前位置
- ActionBar 会实时显示排队进度

---

## 实用查询脚本

### 批量检查所有在线玩家的VIP状态

创建文件 `check-vip.sh`：
```bash
#!/bin/bash
for player in $(rcon-cli list | grep -oP '(?<=: ).*' | tr ', ' '\n'); do
    result=$(rcon-cli "lp user $player permission check queue.vip")
    if [[ $result == *"true"* ]]; then
        echo "✅ $player - VIP"
    else
        echo "❌ $player - 普通玩家"
    fi
done
```

运行：
```bash
bash check-vip.sh
```

---

## 常见查询场景

### 场景1：新玩家说VIP没生效
```bash
# 步骤1：检查权限是否授予
/lp user <玩家名> permission check queue.vip

# 步骤2：查看玩家所属组
/lp user <玩家名> info

# 步骤3：查看玩家所有权限
/lp user <玩家名> permission info

# 步骤4：重新授予权限
/lp user <玩家名> permission set queue.vip true
```

### 场景2：检查VIP是否过期
```bash
# 查看玩家权限详情（包含过期时间）
/lp user <玩家名> info
```

显示类似：
```
> queue.vip (expires in 6 days, 23 hours)
```

### 场景3：导出所有VIP名单
```bash
# 方法1：使用LuckPerms
/lp group vip listmembers

# 方法2：导出到文件
/lp export vip-backup
```

### 场景4：对比两个玩家的权限差异
```bash
/lp user 玩家A info
/lp user 玩家B info
```

---

## 数据库直接查询（高级）

如果LuckPerms使用MySQL存储，可以直接查询数据库：

### 查询所有拥有VIP权限的玩家
```sql
-- 连接到LuckPerms数据库
USE luckperms;

-- 查询VIP玩家
SELECT 
    u.username,
    p.permission,
    p.expiry
FROM luckperms_user_permissions p
JOIN luckperms_players u ON p.uuid = u.uuid
WHERE p.permission = 'queue.vip'
AND (p.expiry = 0 OR p.expiry > UNIX_TIMESTAMP());
```

### 查询VIP组成员
```sql
SELECT 
    u.username,
    p.group_name,
    p.expiry
FROM luckperms_group_permissions p
JOIN luckperms_players u ON p.uuid = u.uuid
WHERE p.group_name = 'vip'
AND (p.expiry = 0 OR p.expiry > UNIX_TIMESTAMP());
```

---

## 权限查询命令速查表

| 操作 | 命令 |
|------|------|
| 查看玩家所有权限 | `/lp user <玩家名> info` |
| 检查VIP权限 | `/lp user <玩家名> permission check queue.vip` |
| 查看VIP组成员 | `/lp group vip listmembers` |
| 搜索拥有VIP权限的人 | `/lp search permission queue.vip` |
| 打开Web编辑器 | `/lp editor` |
| 查看所有权限组 | `/lp listgroups` |
| 导出权限数据 | `/lp export <文件名>` |

---

## 故障排查

### 问题1：命令显示"权限不足"
**原因**: 需要管理员权限执行LuckPerms命令

**解决**:
```bash
/op 你的游戏名
```
或在后台控制台执行命令。

### 问题2：LuckPerms命令不存在
**原因**: LuckPerms插件未安装或未启动

**解决**:
1. 检查 `plugins/` 目录是否有 `LuckPerms.jar`
2. 查看启动日志是否有错误
3. 重启服务器

### 问题3：玩家权限查询为空
**原因**: 玩家从未登录服务器

**解决**:
- 玩家必须至少登录一次，LuckPerms才会创建用户记录
- 或使用UUID查询：`/lp user <UUID> info`

---

**最后更新**: 2026-10-03  
**相关文档**: PERMISSIONS.md
