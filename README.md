###目标
设计实现一个安卓垃圾文件删除的app

###背景
很多人使用安卓手机默认打开了通话录音，时间长了，通话录音占用大量的存储空间，但是很多通话录音是用户不希望保存或希望过一段时间就删除掉的；时间久了，没有用处的通话录音占据了大量的存储空间，让手机运行越来越慢。
现在需要设计并实现一款便捷删除通话录音的app。

###UI
1. 使用扁平化风格（参照微信或者ios风格）
2. 颜色搭配柔和，美观
3. 可以参考【美颜相机】、【轻颜】相机的功能

###功能
1.过滤出已经删除的通话记录但仍存在的通话录音文件，单独一个分类给到用户选择是否全部删除。
2.支持对通话记录中的用户进行分类，根据通话频次占用空间大小进行分类。
3.支持用户把通话联系人放入到安全区，对安全区联系人的通话记录、录音不进行删除。
4.支持用户把部分通话联系人放入临时区，对临时区联系人的通话记录、录音进行定时删除。
5.支持本地网自动备份功能（高阶功能，暂时不提供）。
6.支持根据联系人、通话时间、通话时长列表展示通话记录、录音记录，并提供类似华为手机通话记录类似的录音播放功能。

###数据
1. 构建数据库，通过同步录音文件、联系人、通话记录，来加速访问。
2. 录音记录（RecordingEntity）：
    - 录音文件名
    - 录音文件路径
    - 录音文件大小
    - 录音文件创建时间
    - 录音文件时长
2. 通话记录（CallEntity）：
    - 通话电话号码
    - 通话联系人ID
    - 通话时间
    - 通话时长
    - 通话录音文件名
    - 通话录音文件路径
    - 通话录音文件大小
    - 通话录音文件创建时间
3. 联系人记录（ContactEntity）
    - 联系人ID
    - 联系人姓名
    - 联系人电话号码（多个）
    - 联系人是否是安全区
    - 联系人是否是临时区
    - 联系人是否是黑名单
    - 联系人是否是已删除
    - 创建时间
    - 更新时间

###实现
1. 编写安卓app（请在现在工程基础上）
2. 使用java语言（请在现在工程基础上）
3. 路径包名：com.blackharry.androidcleaner（请在现在工程基础上）
4. gradle构建。（请在现在工程基础上）
5. Java 21的jdk（请在现在工程基础上）
6. 能够正常运行
7. 代码能够正常运行
8. 构建的完整项目，可以在android studio中直接运行
9.  支持安卓7以上

###功能
1. APP包含几个部分：
    - 上方是APP的标题和功能菜单
    - 中间是APP的界面
    - 下方是APP的导航菜单
2. 功能菜单包含：
  - 调试
  - 设置
  - 帮助
  - 退出
3. 调试包含：
  - 日志
  - 性能
  - 崩溃
  - 异常
  - 数据库记录
4. 导航菜单包含：
  - 概览
  - 录音
  - 通话
  - 联系人
5. 概览包含：
  - 录音、通话、联系人的数据统计展示和图标
6. 录音包含：
  - 录音记录列表
  - 录音播放
7. 通话包含：
  - 通话记录列表
  - 通话播放
8. 联系人包含：
  - 联系人列表
  - 联系人分类属性设置



###角色
1. 你是一个安卓专家、精通app的设计实现, 同时你对UI有高深的造诣，能够画出让大多数满意舒服的UI
2. 可以自主根据需求设计实现精美的app的能力
3. 你可以对功能需求进行分析，并构建数据库对系统数据定时进行同步后，通过数据库来加速访问，不要求对用户实时通话的记录进行操作，可以延后同步加载最新的通话记录即可。

###注意事项：
1. 由于项目结构的特殊性，会出现包名不匹配的lint警告，这些警告是预期的，不需要修复。具体包括：
   - `The declared package "com.blackharry.androidcleaner" does not match the expected package "app.src.main.java.com.blackharry.androidcleaner"`
   - 类似的其他包名不匹配警告
2. 请不要尝试修复这些lint警告，因为：
   - 这些警告是由于Cursor和Android Studio的项目结构差异导致的
   - 修复这些警告可能会破坏项目的正常编译和运行
   - 这些警告不会影响应用的功能和性能
3. 如果你在使用Cursor，请忽略"Iterate on Lints"的提示
4. 正确的包名结构已在build.gradle.kts中配置，无需更改

###其他特别的环境依赖，如下代码是正确的，需要注意（由于在Cursor环境中无法正确编译Android项目，所以需要使用Android Studio来编译，对于下面部分的内容请不要重复尝试更改）：
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDirs(listOf("src/main/java"))
            res.srcDirs(listOf("src/main/res"))
        }
    }

###健壮性：
1.应用应该具有完备的日志系统，能够记录应用的运行状态，以及错误信息；日志应该用中文记录，并且应该包含时间、线程、日志级别、日志内容（业务运行上下文）。
2.应用应该具有完备的错误处理机制，能够处理应用运行过程中出现的各种错误。
3.应用应该具有完备的异常处理机制，能够处理应用运行过程中出现的各种异常。
4.应用应该具有完备的崩溃处理机制，能够处理应用运行过程中出现的各种崩溃。
5.应用应该具有完备的性能监控机制，能够监控应用的性能，以及性能瓶颈。
6.应用应该具有完备的性能优化机制，能够优化应用的性能，以及性能瓶颈。
7.应用应该具有完备的性能测试机制，能够测试应用的性能，以及性能瓶颈。


