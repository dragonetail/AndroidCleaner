# Android Cleaner（安卓垃圾文件清理应用）

## 目录
1. [快速开始](#快速开始)
2. [核心功能](#核心功能)
3. [技术实现](#技术实现)
4. [界面规范](#界面规范)
5. [开发规范](#开发规范)
6. [项目协作](#项目协作)
7. [运维支持](#运维支持)
8. [附录](#附录)
9. [Cursor+AI相关](#cursorai相关)
10. [更新日志](#更新日志)

## 术语说明

- **录音文件**：指用户通过应用直接录制的音频文件，主要用于个人备忘或其他用途的音频管理。
- **通话录音**：指通过应用记录的电话通话音频文件，主要用于管理和分析通话记录。

## 一、快速开始
### 1.1 项目简介
Android Cleaner是一款专注于通话录音文件管理的安卓应用，帮助用户高效管理和清理通话录音，优化手机存储空间。

### 1.2 环境要求
- Android SDK: minSdk 24, targetSdk 33
- Java版本: Java 21
- 构建工具: Gradle
- 开发工具: Android Studio

### 1.3 安装步骤
1. 克隆项目
2. 使用Android Studio打开项目
3. 执行`./gradlew installDebug`安装到设备
4. 运行`./run-app.sh`启动应用

### 1.4 基本使用
- 浏览并管理录音
- 浏览并管理通话录音
- 设置联系人分类
- 配置自动清理规则
- 查看存储统计信息

## 二、核心功能
### 2.1 功能概览
1. 管理概览
   - 录音文件统计
   - 通话记录统计
   - 联系人分类统计
   - 存储空间分析
   - 清理建议提供
   - 开发测试支持【恢复初始数据状态】

2. 录音管理
   - 录音文件浏览
   - 批量选择操作
   - 播放和进度控制
   - 时长和大小过滤

3. 通话录音管理
   - 通话录音文件浏览
   - 批量选择操作
   - 播放和进度控制
   - 时长和大小过滤

4. 联系人分类
   - 安全区联系人保护
   - 临时区定时清理
   - 黑名单管理
   - 联系人分组

5. 智能清理
   - 已删除通话记录关联文件清理
   - 基于通话频次的空间分析
   - 定时清理策略
   - 清理结果统计

6. 数据统计
   - 存储空间分析
   - 通话记录统计
   - 联系人分类统计
   - 清理效果统计
   - 
7. 本地网自动备份（暂不实现）
   - 自动检测本地网络环境
   - 定期备份录音文件到指定的本地服务器
   - 支持增量备份，减少网络流量
   - 提供备份状态和历史记录查看
   - 支持手动触发备份操作
   - 备份文件加密，确保数据安全
   - 备份失败时提供错误提示和重试机制

### 2.2 使用场景
1. 日常管理
   - 浏览最近通话录音
   - 试听重要录音内容
   - 删除无用录音文件
   - 整理联系人分类

2. 批量清理
   - 清理已删除通话相关录音
   - 清理临时区联系人录音
   - 清理指定时间段录音
   - 清理超大文件录音
  
3. 数据备份（暂不实现）
   - 自动备份重要录音文件
   - 查看备份历史记录
   - 恢复误删的录音文件
   - 手动触发备份以确保数据安全
 
### 2.3 数据模型
1. 录音记录（RecordingEntity）
   - 基本信息（ID、创建时间、更新时间）
   - 文件基本信息（名称、路径、大小）
   - 录音属性（时长）
   - 关联信息（无）

2. 通话记录（CallEntity）
   - 基本信息（ID、创建时间、更新时间）
   - 通话基本信息（号码、通话时间）
   - 通话联系人信息（联系人ID）
   - 文件基本信息（名称、路径、大小）
   - 录音属性（时长）

3. 联系人记录（ContactEntity）
   - 基本信息（ID、姓名、号码、创建时间、更新时间）
   - 分类属性（安全区、临时区、黑名单）
   - 状态信息（是否删除、更新时间）

4. 通用约束
   - 通用字段命名约束：id、createTime、updateTime
   - 通用字段类型约定：id为Long类型，createTime、updateTime为Long类型（时间戳，适配ROOM映射和性能）
   - 同样含义的业务最大可能保持同样的命名及规则

### 2.4 业务流程
1. 数据同步流程
   - 扫描本地录音文件
   - 同步通话记录数据
   - 更新联系人信息
   - 构建数据索引

2. 清理流程
   - 分析清理目标
   - 确认安全区保护
   - 执行文件删除
   - 更新数据记录

## 三、技术实现
### 3.1 项目架构
1. 应用架构
   - MVVM架构模式
   - 模块化设计
   - 数据驱动UI
   - 响应式编程

2. 核心模块
   - 概览模块
   - 录音模块
   - 通话模块
   - 联系人模块

### 3.2 模块说明
1. 概览模块
   - 导航菜单：概览
   - 标题： 录音空间清理
   - 数据统计展示
   - 快捷操作入口
   - 存储空间分析
   - 清理建议提供
   - 开发测试支持【恢复初始数据状态】

9. 导航与标题
  - 整体APP下面导航栏分为【概览、录音、通话、联系人】
  - 对应不同导航选中时顶端标题栏文字分别为【录音空间清理、录音文件、通话录音、联系人信息】
  - 标题栏（APP或各画面标题和功能菜单）
   - 内容区（主要界面内容）
   - 导航栏（底部导航菜单）

2. 录音模块
   - 导航栏：录音
   - 标题栏： 录音文件
   - 文件列表管理，录音文件浏览(浏览状态)，过滤排序功能，播放器控制
      - 内容区：录音文件列表
      - 列表项：录音文件名、时间、大小、时长和播放按钮
        - 左面分为上下显示文件名、时间，右边先上下显示大小和时长
        - 然后最右边显示播放按钮，播放按钮为黑色圆圈中间三角形播放图标
        - 点击播放按钮的时候在录音项下面显示播放进度条，可以手动拖动进度条控制播放位置
        - 播放进度条下面显示播放时间，播放时间分为上下两行，上面显示当前播放时间，下面显示录音总时长
        - 播放状态时，播放按钮变化为暂停按钮，点击暂停按钮后，停止播放，并切换为播放按钮
        - 播放按钮和暂停按钮的图标变化
        - 播放按钮和暂停按钮的点击事件
      - 功能菜单：
        - 【更多】菜单按钮，点击弹出一级菜单，一级菜单及其下属二级菜单内容如下：
          - 时间： 全部（默认选项）、一年以前、90天以前、最近90天内
          - 时长： 全部（默认选项）、2小时以上、10分钟以上、10分钟以内
          - 排序： 时间降序（默认选项）、时间升序、大小降序、大小升序
        - 每个一级菜单对应的二级菜单，都是单选，使用单选框来表示当前选择状态
        - 二级菜单，点击后，选中当前选项，并更新标题栏对应的过滤条件，刷新当前列表数据
      - 状态切换： 
        - 长按录音记录项，切换到选择状态（录音文件列表），并且缺省当前记录为选中状态
    -  批量操作处理，录音文件选择列表（选择状态）
      - 内容区：录音文件列表
      - 列表项：录音文件名、时间、大小、时长和选择按钮
        - 最右边浏览状态的播放按钮切换显示为选择状态的选择按钮
        - 选择框内显示当前选择状态
        - 用户可以通过滑动多条记录快速控制选择状态
      - 标题栏： 选择状态下标题栏分为两部分
        - 关闭按钮：在最左边，一个【×】图标，点击退出选择状态（系统回退动作时同样操作）
        - 选择数量：在关闭按钮的右边，中间有一定的空间隔，显示总体选择状态【已选择xx项】
      - 功能菜单：
        - 删除：弹出确认对话框，确认后删除当前选择的记录
        - 全选：选中当前列表所有记录，并更新选择数量；选择状态下，点击全选按钮，则取消全选状态
        - 更多：弹出一级菜单，一级菜单及其下属二级菜单内容如下：
          - 分享：点击后，弹出分享对话框，选择分享方式，支持分享到微信、QQ、短信、邮件等（暂不实现）
          - 转文字：点击后，弹出转文字对话框，选择转文字方式，支持语音转文字、图片转文字等（暂不实现）
          - 重命名：点击后，弹出重命名对话框，输入新的文件名，点击确定后，更新文件名（暂不实现）
   - 特别处理：
     - 删除： 文件存在且删除失败，则不删除DB记录，否则都应该把DB记录删除了
     - 测试数据：请根据以上过滤规则，构造对应的测试数据协助测试

3. 通话模块
   - 通话记录管理
   - 录音关联处理
   - 通话统计分析
   - 清理策略执行

4. 联系人模块
   - 联系人管理
   - 分类属性设置
   - 关联数据处理
   - 保护策略执行

### 3.3 关键实现
1. 数据库设计
   - Room持久化
   - 关系映射
   - 索引优化
   - 事务处理

2. 文件管理
   - 异步操作
   - 批量处理
   - 错误恢复
   - 进度监控

3. UI和后端数据分离处理逻辑和原则
   - 数据处理不在UI主线程进行，避免阻塞UI响应。
   - 使用异步任务或协程进行数据处理，保证高效执行。
   - 在ViewModel中进行数据处理，确保UI和数据逻辑分离。
   - 使用Repository模式管理数据来源，统一数据访问接口。
   - 数据处理完成后，通过LiveData或其他观察者模式将结果通知UI层。
   - 保证数据处理结果能够第一时间反映到UI前端，提升用户体验。
   - 在数据处理过程中，UI层应显示加载状态或进度条，提示用户当前操作状态。
   - 数据处理失败时，及时向用户展示错误信息，并提供重试或其他解决方案。

4. 备份功能设计（暂不实现）
   - 网络检测：使用系统API检测当前网络状态，确保在Wi-Fi环境下进行备份
   - 文件加密：使用AES加密算法对备份文件进行加密，确保数据传输和存储安全
   - 增量备份：通过比较文件的修改时间和大小，仅备份有变化的文件
   - 备份调度：使用WorkManager定期调度备份任务，支持灵活的备份策略
   - 错误处理：在备份过程中捕获异常，记录错误日志，并提供重试机制

### 3.4 数据存储
1. 数据库存储
   - 录音信息表
   - 通话记录表
   - 联系人信息表
   - 配置信息表(暂无)

2. 文件存储
   - 录音文件管理
   - 缓存策略
   - 备份恢复
   - 存储优化

## 四、界面规范
### 4.1 设计语言
- Material Design 3规范
- 扁平化风格
- 简洁直观
- 统一体验

### 4.2 布局规则
1. 基础布局
   - 标题栏（APP或各画面标题和功能菜单）
   - 内容区（主要界面内容）
   - 导航栏（底部导航菜单）

2. 列表布局
   - 标准间距（16dp）
   - 统一圆角（8dp）
   - 清晰分割
   - 合理留白

### 4.3 组件样式
1. 按钮样式
   - 主要按钮（强调色）
   - 次要按钮（线框样式）
   - 文本按钮
   - 图标按钮

2. 列表项样式
   - 标准列表项
   - 选择状态样式
   - 播放控制样式
   - 展开详情样式

### 4.4 交互规范
1. 手势操作
   - 点击选择
   - 长按进入选择模式
   - 滑动播放进度
   - 返回退出选择

2. 状态反馈
   - 加载状态
   - 操作结果
   - 错误提示
   - 空状态展示

## 五、开发规范
### 5.1 代码规范
1. 命名规范
   - 类名：PascalCase
   - 方法名：camelCase
   - 变量名：camelCase
   - 常量名：UPPER_SNAKE_CASE
   - 相似含义的处理或配置应该最大可能保持同样的命名、顺序及规则

2. 代码风格
   - 缩进：4空格
   - 行长度：100字符
   - 大括号：同行
   - 注释规范
     - 注释设计作者一律使用： blackharry
     - 类注释：每个类的定义上方应包含类的功能描述、处理摘要、作者信息和创建日期、更新日期。例如：
       ```
       /**
        * 类功能描述：此类用于处理用户登录逻辑。
        * 处理摘要：处理用户登录逻辑，包括用户名和密码的验证。
        * 作者：blackharry
        * 创建日期：2025年2月3日
        * 更新日期：2025年2月3日
        */
       ```
     - 方法注释：每个方法的定义上方应包含方法的功能描述、处理摘要、参数说明、返回值说明和异常说明。例如：
       ```
       /**
        * 方法功能描述：验证用户输入的登录信息。
        * 处理摘要：验证用户输入的登录信息，包括用户名和密码的验证。
        * 参数说明：
        * @param username 用户名
        * @param password 密码
        * 返回值说明：验证结果，true表示成功，false表示失败
        * @throws IllegalArgumentException 如果用户名或密码为空
        */
       ```
     - 代码块注释：对于复杂的代码逻辑，应在代码块上方添加注释，解释代码的功能和实现思路。
     - 行内注释：对于关键代码行，应在行尾添加简短注释，解释代码的作用。
     - 文档注释：使用Javadoc格式编写文档注释，生成API文档，便于他人理解和使用。
     - 注释风格：注释应简洁明了，避免冗长，确保注释内容与代码保持一致。
     - 其他文件注释：对xml、yaml、json、shell等文件，也都应该追加类似文件级别的注释（需要针对不同文件类型生成对应注释）。例如xml文件头应该追加：
       ```
       <!--
        * 文件功能描述：概览画面的布局配置文件。
        * 作者：blackharry
        * 创建日期：2025年2月3日
        * 更新日期：2025年2月3日
        -->
       ```

3. 注解规范
   - ROOM注解：数据Entity上应该追加完善的ROOM注解，确保数据Entity的完整性。
   - Validation注解：数据Entity上应该追加完善的Validation注解（基于androidx.annotation），并在Repository进行校验，确保数据Entity的完整性。
   - 接口注解：接口上应该追加完善的接口注解（Swagger注解），确保接口文档的完整性（暂不考虑）。

### 5.2 资源规范
1. 资源命名
   - 布局文件：`activity_*`, `fragment_*`, `item_*`
   - 图标资源：`ic_*`
   - 背景资源：`bg_*`
   - 选择器：`selector_*`
   - 相似含义的处理或配置应该最大可能保持同样的命名、顺序及规则

2. 资源组织
   - 模块化资源
   - 主题样式
   - 字符串资源
   - 尺寸规范

### 5.3 版本控制
1. 分支管理
   - 主分支：main
   - 开发分支：develop
   - 功能分支：feature/*
   - 修复分支：bugfix/*

2. 提交规范
   - 明确的提交信息
   - 相关任务关联
   - 代码审查
   - 合并策略

### 5.4 构建发布
1. 构建配置
   - Debug构建
   - Release构建
   - 混淆规则
   - 签名配置

2. 发布流程
   - 版本号管理
   - 清单配置
   - 打包发布
   - 更新说明

## 六、项目协作
### 6.1 AI辅助开发
1. 代码生成
   - 遵循项目规范
   - 保持代码质量
   - 注重代码健壮性
   - 完善注释文档

2. 质量控制
   - 代码审查标准
   - 性能优化建议
   - 最佳实践推荐
   - 问题修复指导
  
  TODO 原来的相关规则

### 6.2 代码审查
1. 审查重点
   - 代码规范
   - 业务逻辑
   - 性能影响
   - 安全隐患

2. 审查流程
   - 提交前自查
   - 团队审查
   - 问题修复
   - 确认合并

### 6.3 文档维护
1. 文档范围
   - 技术文档
   - 接口文档
   - 测试文档
   - 发布说明

2. 更新规则
   - 及时更新
   - 版本对应
   - 变更说明
   - 审查确认

3. README文档维护
   - 每个目录都应该维护一个README.md文件，以固化当前目录的实现设计
   - 根目录的README.md文件（当前文件），维护整个项目的设计及开发原则、流程、规范等，不需要AI自动维护
   - 除根目录外其他目录的README.md文件，都需要AI自动维护
   - 对应目录内容文件变更后，必须更新README.md文件，以固化当前代码实现的设计
   - 目录下多个文件和内容时，根据内容自动分类并总结设计要点：
     * 功能说明
     * 设计原则
     * 开发规范
     * 测试规范
     * 运维支持
     * 附录

### 6.4 测试规范
1. 测试类型
   - 单元测试
   - 集成测试
   - UI测试
   - 性能测试

2. 测试要求
   - 测试覆盖率
   - 测试用例
   - 测试报告
   - 问题跟踪

## 七、运维支持
### 7.1 日志系统
1. 日志规范
   - 日志级别：v（详细）、d（调试）、i（信息）、w（警告）、e（错误）
   - 日志格式：时间戳、线程、级别、上下文、中文描述
   - 日志内容：应包含必要的上下文信息，确保问题可追踪
   - 日志存储：设备本地文件存储，定期上传服务器，保留周期为60天，支持压缩和加密

2. 日志使用
   - 业务日志：记录业务流程和重要操作
   - 错误日志：记录应用运行中的错误和异常
   - 性能日志：记录性能指标和瓶颈
   - 安全日志：记录安全相关事件和操作
   - 方法进入：使用logMethodEnter记录方法进入
   - 方法退出：使用logMethodExit记录方法退出
   - 错误记录：使用logError记录错误信息
   - 性能监控：使用logPerformance记录性能数据

### 7.2 异常处理
1. 异常分类
   - 业务异常：应用逻辑错误或数据处理错误，如数据格式不正确、业务规则冲突等。
   - 系统异常：系统级别的错误，如内存溢出、文件读写失败等。
   - 网络异常：网络连接失败、请求超时、服务器错误等。
   - 权限异常：缺少必要的权限，如读取存储权限、访问联系人权限等。

2. 处理策略
   - 异常捕获：使用try-catch块捕获异常，避免程序崩溃。
   - 错误恢复：提供合理的错误恢复机制，如重试操作、回滚事务等。
   - 用户提示：通过Toast、Dialog等方式向用户提示错误信息，并提供解决方案。
   - 日志记录：详细记录异常信息，包括异常类型、发生时间、堆栈信息等，便于后续分析和排查。

### 7.3 性能监控
1. 监控指标
   - 启动时间：应用从启动到完全加载所需的时间。
   - 内存使用：应用运行过程中占用的内存量。
   - 响应时间：用户操作到应用响应的时间间隔。
   - 电池消耗：应用运行对设备电池的消耗情况。

2. 优化策略
   - 启动优化：减少启动时间，优化启动流程，延迟加载非关键组件。
   - 内存优化：优化内存分配，避免内存泄漏，使用内存缓存技术。
   - 布局优化：简化布局层级，使用高效布局，减少过度绘制。
   - 电池优化：减少后台任务，优化电池消耗，使用节能模式。

### 7.4 安全规范
1. 数据安全
   - 存储加密：对本地存储的数据进行加密，防止数据泄露。
   - 传输安全：使用HTTPS等安全协议，确保数据传输过程中的安全性。
   - 权限控制：严格控制应用权限，确保只有必要的权限被授予。
   - 敏感信息保护：对敏感信息进行脱敏处理，防止信息泄露。

2. 应用安全
   - 代码混淆：使用ProGuard等工具对代码进行混淆，增加反编译难度。
   - 签名验证：对应用进行签名验证，确保应用的完整性和来源可信。
   - 漏洞防护：定期进行安全扫描，及时修复已知漏洞。
   - 安全更新：定期发布安全更新，修复安全漏洞，提升应用安全性。
   - 备份安全：确保备份文件的加密和传输安全，防止数据泄露。
   - 备份恢复：提供安全的备份恢复机制，确保数据完整性。

## 八、附录
### 8.1 常见问题
1. 环境配置
   - 开发环境配置
     - 安装Android Studio
     - 配置JDK路径
     - 设置Gradle构建工具
     - 配置Android SDK和AVD
   - 运行环境要求
     - Android设备或模拟器，Android版本不低于7.0（API 24）
     - 至少2GB的可用存储空间
     - 4GB以上的内存
   - 常见错误解决
     - Gradle构建失败：检查Gradle版本和依赖库配置
     - 模拟器启动失败：确保AVD配置正确，检查HAXM安装
     - 运行时崩溃：查看日志，检查权限配置和依赖库版本
   - 性能优化建议
     - 使用ProGuard进行代码混淆和优化
     - 启用R8进行代码压缩
     - 优化布局层级，减少过度绘制
     - 使用内存分析工具检测和修复内存泄漏

2. 功能使用
   - 基本功能说明
     - 浏览通话录音：在主界面查看所有录音文件
     - 批量选择操作：长按录音文件进入批量选择模式
     - 播放和进度控制：点击录音文件进行播放，拖动进度条控制播放进度
     - 设置联系人分类：在联系人管理界面设置联系人分类
   - 高级功能指南
     - 配置自动清理规则：在设置界面配置自动清理规则
     - 查看存储统计信息：在存储统计界面查看存储使用情况
     - 备份和恢复：在设置界面进行数据备份和恢复操作
     - 自定义清理策略：根据个人需求自定义清理策略
   - 使用技巧
     - 使用搜索功能快速查找录音文件
     - 利用分类标签管理联系人
     - 定期备份重要数据，防止数据丢失
     - 设置定时清理规则，保持存储空间充足
   - 注意事项
     - 确保应用拥有必要的权限，如存储权限、录音权限等
     - 定期更新应用，获取最新功能和修复
     - 使用过程中遇到问题，可通过帮助中心或联系客服获取支持
     - 清理操作不可逆，请谨慎操作

### 8.2 最佳实践
1. 开发建议
   - 代码组织
     - 模块化设计，确保代码结构清晰，易于维护和扩展
     - 遵循MVVM架构模式，分离视图、业务逻辑和数据层
     - 使用Repository模式管理数据来源，统一数据访问接口
     - 合理使用依赖注入，提升代码的可测试性和可维护性
   - 性能优化
     - 避免在主线程进行耗时操作，使用异步任务或协程
     - 优化布局层级，减少过度绘制
     - 使用内存缓存和磁盘缓存，减少不必要的网络请求
     - 定期进行性能分析，找出并优化性能瓶颈
   - 内存管理
     - 避免内存泄漏，及时释放不再使用的资源
     - 使用内存分析工具检测和修复内存泄漏
     - 合理使用弱引用，避免持有长生命周期对象的强引用
     - 控制Bitmap等大对象的使用，避免内存溢出
   - 电池优化
     - 减少后台任务的频率和耗时，降低电池消耗
     - 使用JobScheduler或WorkManager管理后台任务
     - 优化网络请求，减少不必要的数据传输
     - 使用电池分析工具检测和优化电池消耗

2. 设计建议
   - 界面设计
     - 遵循Material Design 3规范，确保界面一致性和美观性
     - 使用响应式布局，适配不同屏幕尺寸和分辨率
     - 合理使用颜色和字体，提升界面可读性和用户体验
     - 保持界面简洁，避免过多的视觉元素干扰用户
   - 交互设计
     - 提供直观的交互方式，确保用户能够快速上手
     - 使用动画和过渡效果，提升交互的流畅性和自然感
     - 提供及时的反馈，确保用户了解当前操作状态
     - 考虑不同用户的使用习惯，提供个性化的交互选项
   - 用户体验
     - 关注用户需求，提供符合用户期望的功能和服务
     - 收集用户反馈，持续改进应用的功能和体验
     - 提供详细的帮助文档和使用指南，帮助用户解决问题
     - 定期进行用户测试，找出并解决用户体验中的问题
   - 无障碍设计（暂时不开发）
     - 提供语音辅助功能，帮助视力障碍用户使用应用
     - 确保界面元素的可访问性，提供足够的触控目标和间距
     - 提供高对比度模式，帮助色盲用户识别界面元素
     - 支持屏幕阅读器，确保应用内容能够被朗读

### 8.3 更新日志
- 版本更新记录
- 功能变更说明
- 问题修复列表
- 优化改进项目

### 8.4 参考资料
1. 技术文档
   - Android开发文档（需要在手工在Cursor配置中追加为Doc： https://developer.android.com/）
   - Material Design指南（需要在手工在Cursor配置中追加为Doc： https://m3.material.io）
   - 开源库文档（暂无）
   - API参考（暂无）

2. 设计资源
   - 设计规范（https://m3.material.io/）
   - 图标资源（https://m3.material.io/styles/icons/overview）
   - 色彩系统（https://m3.material.io/styles）
   - 组件库（https://m3.material.io/components）

### 8.5 提示词示例参考
- 黄金提示词： 请学会【## 九、Cursor+AI相关】中相关命令和管理自动化的内容。并请开启【履历管理】和【TODO管理】的自动化动作。
- 补充履历记录： 请整理本次compser回话上下文内容追加到当天的履历记录中。
- 更新Rules： 请根据当前文件内容更新.cursorrules文件。（根据需要对具体内容在补充转化）
- 整体文档AI评审和辅助提示词： 请整体Review当前文件内容，并提出优化和改进意见；请一步一步提出具体的修改建议，我确认之后再一步一步修改。
- 代码生成辅助提示词： 请根据用户需求，生成对应的代码，并给出详细的代码说明和使用方法。
- 代码优化辅助提示词： 请根据用户需求，优化和改进代码，并给出详细的代码说明和使用方法。
- 代码评审辅助提示词： 请根据用户需求，评审代码，并给出详细的代码说明和使用方法。

## 九、Cursor+AI相关
### 9.1 Doc配置
1. Android开发文档： https://developer.android.com/
2. Material Design指南： https://m3.material.io

### 9.2 Yolo模式下Composer的用法
1. 交互原则
   - 简洁，不要过多解释问题，快速修改代码为主要目的
   - Composer的上下文要永远选择[README.md]
   - 生成Android代码的时候，永远参照知识库【developer.android.com】使用最新的代码实现方法
   - 生成UI代码的时候，永远参照知识库【m3.material.io和developer.android.com】使用最新的规范和配置方法
   - 
2. 常用命令类提示词
   - 提交：自动提交所有修改的代码并推送到GIT
   - 编译：自动执行[./gradlew installDebug]
   - 清空编译：自动执行[./gradlew clean installDebug]
   - 运行：自动执行[./run-app.sh]
   - 文档：自动校验和更新所有目录下的README文档，没有的自动生成
   - 注释：自动补全代码注释，并每更新10个文件自动提交一次GIT
   - 检查：自动检查代码中存在的问题，整理问题并提交给用户进行确认，根据反馈执行修复：
     * 检查时间：用户发起检查的时间，格式为yyyyMMdd-HHmmss
     * 检查：
       * 开始检查前，先生成检查记录文件，文件名：[./qreports/check-<检查时间>.md]
       * 在文件中记录如下内容，如果文件不存在，自动创建，追加内容到当前文件：
         * 问题序号： 序号（从1开始，自动增加）
         * 问题记录时间： 当前时间
         * 文件路径： 检查的文件路径，当前项目相对路径
         * 版本号： 检查的文件GIT版本号
         * 行号： 问题发生的行号
         * 问题摘要
         * 问题类型
         * 问题严重程度
         * 问题详细描述
         * 问题建议
       * 请使用Markdown分级标题方式记录内容，不要使用表格记录，每条记录之间使用空行隔开
       * 检查过程中，每次整理超过10个问题，马上停止检查，等待用户一个一个确认
       * 用户确认并修改后，提交GIT，然后继续检查
     * 报告：
       * 待检查问题结束后，自动生成问题报告文件，文件名：[./qreports/report-<检查时间>.md]，如果文件不存在，自动创建，追加内容到当前文件
       * 报告内容：
         * 检查时间： 检查时间
         * 报告时间： 当前时间
         * 问题序号范围
         * 关联文件
         * 问题数量
         * 问题分类数量
           * 问题分类
           * 统计数量
           * 问题摘要
         * 问题严重程度分类及统计数量
         * 简要报告
         * 待办事项
       * 检查记录文件和问题报告文件，需要自动追加到GIT中
       * 请使用Markdown分级标题方式记录内容，不要使用表格记录，每条记录之间使用空行隔开
     * 检查的范围：
       * 项目所有下级目录内容（源码、配置文件、资源文件）
     * 检查的要点内容：
       * 命名规范
       * 代码规范
       * 日志规范
       * 注释规范及完善
       * 潜在缺陷
       * 性能隐患
       * 安全问题
     * 涉及重大变更的问题，记录到检查和报告记录中，并提示用户另行处理
     * 自动Apply并更新文件，不需要用户确认，并同步提交到GIT
   - 履历搜索：
     * 根据用户输入，搜索关联度最高的前10条记录，分别显示：序号、日志文件名、履历时间、履历摘要
     * 并提示用户选择要查看的履历，用户选择后，自动显示履历文件内容并定位且高亮显示对应行
   - TODO搜索：
     * 根据用户输入，直接打开TODO.md文件，定位到标注为未完成的第一条TODO事项，并高亮显示
   - TODO完成：
     * 标记TODO为完成，并根据用户输入更新TODO的备注（时间、状态更新、用户输入内容）
     * 自动Apply并更新文件，不需要用户确认，并同步提交到GIT

3. 代码生成范围限定原则
   - 功能追加：不允许直接追加任何新的功能；如果有好的想法，请向用户提出建议和需求想法，然后根据用户反馈进行代码生成
   - 功能变更，在保证既有功能的前提下，允许对现有功能进行：
     * 修复
     * 健壮性强化
     * 日志补充
     * 注释完善
     * 代码重构
  
4. 功能变更流程
   - 新功能必须由用户发起
   - 或在获得用户确认后才能追加
   - 特别注意：已完成功能不得擅自改变设计初衷，避免后续需要重新Rework或删除
   - 修改主动查找关联修正，要尽量一次修改完整，不要遗漏
   - 代码变更后，必须对应Package或目录下，更新README.md文件（根目录除外），以固化当前代码实现的设计

5. 变更关联修正
   - 每次新增或更新资源配置要检查相关资源引用是否存在，不存在的自动创建，避免多次编译交互
   - 每次变更或删除资源配置后，要分析关联配置和代码中是否有遗留废弃的引用，自动删除，避免多次编译交互

6. 自动编译及修正错误
   - 每次修改代码和配置文件之后，请自动运行[./gradlew installDebug]命令进行编译；当遇到多次错误无法解决时，请运行[./gradlew clean installDebug]来通过清空工作空间，减少环境干扰
   - 如果编译出现错误，请自动分析错误原因并修复，包括但不限于：
     * 缺少导入语句的自动添加
     * 缺少方法的自动实现
     * 类型不匹配的自动转换
     * 资源引用错误的自动修复
   - 修复编译错误时需要遵循以下原则：
     * 保持代码风格一致性
     * 确保修复不会引入新的问题
     * 添加必要的日志记录
     * 保持代码的健壮性
   - 每次修改后要确保：
     * 代码能够正常编译
     * 功能逻辑正确
     * 不破坏现有功能
     * 符合项目规范
   - 用户确认保障：
     * 自动修改的内容，涉及重要的在不中断当前工作的前提下，要提醒用户确认后再执行
     * 涉及重大变更的问题，直接终端自动修改，并提示用户另行处理
  
7. 履历管理
   - 在目录[./history]下记录同用户的所有交互记录，包括用户的输入以及输出内容
   - 履历按照天来记录，文件名：[./history/history-<日期>.md]，如果文件不存在，自动创建，当天内容自动追加到当天文件中
   - 请使用Markdown分级标题方式记录内容，不要使用表格记录，每条记录之间使用空行隔开
   - 履历记录内容：
     * 序号（从1开始，自动增加）
     * 请求时间
     * 请求耗时
     * 请求内容： 用户输入的原始内容
     * 请求结果： COMPOSER输出的原始内容
   - 自动Apply并更新文件，不需要用户确认，并同步提交到GIT

8. TODO管理
   - 在根目录下记录TODO清单，文件名：[./TODO.md]，如果文件不存在，自动创建
   - 每次同用户交互过程中，用户对暂不处理的问题，请自动记录到TODO清单中，内容自动追加到当前文件中
   - 请使用Markdown分级标题方式记录内容，不要使用表格记录，每条记录之间使用空行隔开
   - TODO记录内容：
     * 序号（从1开始，自动增加）
     * 标题
     * 记录时间
     * 状态（未完成、进行中、已完成）
     * 摘要
     * 详细描述（如果可以请包含文件路径、版本号、行号，以及必要的上下文等详细信息）
     * 备注
   - 自动Apply并更新文件，不需要用户确认，并同步提交到GIT

## 更新日志

### 版本 1.0.1
- 修复了录音文件管理中的一个小错误。
- 优化了通话录音的存储性能。

### 版本 1.0.0
- 初始版本发布，包含录音文件和通话录音的基本管理功能。

