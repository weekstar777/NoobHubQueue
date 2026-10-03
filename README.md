# NoobHubQueue

Minecraft Paper 1.21.1 单服排队插件，为 NoobHub 无政府服务器设计。

## 功能

- 100人游戏限制，超出人数自动排队（FIFO）
- VIP玩家优先插队
- `/play` 指令手动进入游戏
- 兼容 RandomSpawn、EssentialsX

## 快速开始

### 编译

双击 `compile.bat` 或运行：
```bash
mvn clean package
```

### 安装

1. 复制 `target/NoobHubQueue-1.0.0.jar` 到服务器 `plugins/` 目录
2. 重启服务器
3. 编辑 `plugins/NoobHubQueue/config.yml` 配置等待平台坐标

### VIP配置

```bash
lp group vip permission set queue.vip true
```

## 项目结构

```
NoobHubQueue/
├── src/                    # 源代码
│   ├── main/java/          # Java源文件
│   └── main/resources/     # 配置文件
├── release/                # 发布版本
├── pom.xml                 # Maven配置
├── compile.bat             # 编译脚本
└── README.md               # 本文件
```

**Maven文件说明**：
- `pom.xml` - 定义项目依赖（Paper API、LuckPerms API等）
- `mvnw.cmd` + `.mvn/` - Maven Wrapper，无需单独安装Maven
- `compile.bat` - 编译脚本，封装了Maven命令并提供中文提示

## 开源协议

MIT License

---

NoobHub 无政府服务器 | 端口 1723
