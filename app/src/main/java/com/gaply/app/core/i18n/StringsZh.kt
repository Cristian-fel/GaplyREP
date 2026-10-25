package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 简体中文文本。 */
object StringsZh : AppStrings {

    override val javaLocale: Locale = Locale.SIMPLIFIED_CHINESE

    // =================================================================
    // 通用
    // =================================================================

    override val back = "返回"
    override val close = "关闭"
    override val cancel = "取消"
    override val accept = "确定"
    override val ok = "OK"
    override val done = "完成"
    override val delete = "删除"
    override val edit = "编辑"
    override val save = "保存"
    override val next = "下一步"
    override val skip = "跳过"
    override val selected = "已选择"
    override val add = "添加"
    override val retry = "点按重试"
    override val acceptShort = "确定"

    override val connectionErrorTitle = "连接错误！"
    override val connectionErrorMessage = "请检查网络连接，稍后再试。"
    override val noActiveSessionMsg = "没有活跃的会话"
    override val firestoreReadDenied = "Firestore 拒绝读取这些数据（会话或安全规则）。"

    override val successCd = "成功"
    override val infoCd = "信息"
    override val warningCd = "警告"
    override val errorCd = "错误 / 离线"
    override val closeCd = "关闭"
    override val showPassword = "显示密码"
    override val hidePassword = "隐藏密码"
    override val expandOptionsCd = "展开选项"

    override fun dayName(dayKey: String): String = when (dayKey) {
        "Lunes" -> "星期一"
        "Martes" -> "星期二"
        "Miércoles" -> "星期三"
        "Jueves" -> "星期四"
        "Viernes" -> "星期五"
        "Sábado" -> "星期六"
        "Domingo" -> "星期日"
        else -> dayKey
    }

    override fun formatTime(hora24: String): String {
        val partes = hora24.trim().split(":")
        val horas = partes.getOrNull(0)?.toIntOrNull() ?: 0
        val minutos = partes.getOrNull(1)?.toIntOrNull() ?: 0
        val sufijo = if (horas < 12) "上午" else "下午"
        val hora12 = when {
            horas == 0 || horas == 12 -> 12
            horas > 12 -> horas - 12
            else -> horas
        }
        return "%s%d:%02d".format(sufijo, hora12, minutos)
    }

    override fun freeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> "${minutos / 60}小时空闲"

        minutos >= 60 -> "${minutos / 60}小时${minutos % 60}分钟空闲"
        else -> "${minutos}分钟空闲"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("M月d日", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // 欢迎
    // =================================================================

    override val welcomeTagline = "在课间找点事情做"
    override val signIn = "登录"
    override val createAccount = "注册账号"
    override val accountDeletedTitle = "账号已删除！"
    override val accountDeletedMsg = "你的账号和数据已成功删除。"
    override val accountDeactivatedTitle = "账号已停用！"
    override val accountDeactivatedMsg =
        "你可以随时用原来的登录信息重新激活。"

    // =================================================================
    // 登录
    // =================================================================

    override val loginTitle = "使用 Gaply 账号登录"
    override val usernameLabel = "用户名"
    override val passwordLabel = "密码"
    override val forgotPassword = "忘记密码了？"
    override val loginButton = "登录"
    override val noAccountYet = "还没有账号？ "
    override val registerLink = "立即注册"
    override val marketingOptOut =
        "如果你不想接收我们产品和服务的营销信息，请勾选此选项。"
    override val userNotFoundTitle = "找不到账号！"
    override val userNotFoundMsg = "没有使用该用户名的账号。请检查一下或去注册。"
    override val invalidEmailFormat = "邮箱格式无效"
    override val wrongPasswordMsg = "密码不正确"
    override val loginFailedMsg = "登录失败，请重试。"
    override val noAccountWithEmailMsg = "没有使用该邮箱的账号"

    // =================================================================
    // 字段错误
    // =================================================================

    override val fieldRequired = "此项为必填"
    override val minChars3 = "至少 3 个字符"
    override val minChars6 = "至少 6 个字符"
    override val validEmailPrompt = "请输入有效的邮箱"
    override val passwordsMismatch = "两次输入的密码不一致"
    override val usernameTaken = "该用户名已被使用"
    override val emailTaken = "该邮箱已被注册"
    override val selectBirthdate = "请选择你的出生日期"
    override val selectUniversity = "请选择你的大学"

    override fun minInterestsWarning(max: Int) = "最多可以选择 $max 个兴趣。"

    // =================================================================
    // 注册
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "第 $step 步，共 $total 步"
    override val stepAccountData = "账号信息"
    override val stepPersonalInfo = "个人信息"
    override val stepPurpose = "目的"
    override val stepInterests = "兴趣"
    override val stepPhotoBio = "照片和简介"

    override val createYourAccountTitle = "创建你的账号"
    override val startWithLoginData = "先填写登录信息。"
    override val usernameAtHint = "用户名（@username）"
    override val emailLabel = "邮箱"
    override val confirmPasswordLabel = "确认密码"

    override val personalInfoTitle = "个人信息"
    override val tellUsAboutYou = "介绍一下你自己吧。"
    override val firstNameLabel = "名 *"
    override val lastNameLabel = "姓氏"
    override val birthDateLabel = "出生日期 *"
    override val dayLabel = "日"
    override val monthLabel = "月"
    override val yearLabel = "年"
    override val whereStudyNow = "你现在在哪里读书？"
    override val universityLabel = "大学 *"
    override val genderIdentityLabel = "性别认同"
    override val chooseYourGender = "选择你的性别"

    override val whatBringsGaply = "你为什么想用 Gaply？"
    override val chooseGoalHint = "选择最符合你的目标，之后可以随时更改。"

    override val selectSomeInterests = "选择一些你的兴趣"
    override val addInterests = "添加兴趣"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "最多可选 $max 个兴趣（$selected/$total）。"

    override val uploadPhoto = "上传照片"
    override val takePhoto = "拍照"
    override fun helloName(name: String) = "你好，$name"
    override val giveShortBio = "写一句简单的自我介绍："
    override val bioLabel = "简介"
    override val saveProfile = "保存资料"

    override val profileSavedTitle = "你的资料已保存！"
    override val profileSavedMsg =
        "你的资料已保存！个人信息已成功保存。"
    override val galleryInfoTitle = "相册"
    override val galleryInfoMsg = "正式版中，这个按钮会打开你的相册。"
    override val cameraInfoTitle = "相机"
    override val cameraInfoMsg = "正式版中，这个按钮会打开手机相机。"
    override val saveErrorTitle = "保存失败！"
    override val saveErrorMsg = "资料保存失败！抱歉，请稍后再试。"

    override val months = listOf(
        "1月", "2月", "3月", "4月", "5月", "6月",
        "7月", "8月", "9月", "10月", "11月", "12月",
    )

    override val genders = listOf("女性", "男性", "非二元", "不愿透露")

    override val goals = listOf(
        "\uD83D\uDCDA 我需要学习",
        "\uD83C\uDFC0 有什么好玩的",
        "\uD83D\uDE09 我有空",
        "\uD83E\uDD7A 我还没想好",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "其他",
    )

    override fun interestLabel(key: String): String = when (key) {
        "Deportes" -> "运动"
        "Música" -> "音乐"
        "Videojuegos" -> "电子游戏"
        "Artes" -> "艺术"
        "Juegos de mesa" -> "桌游"
        "Películas" -> "电影"
        "Estudio" -> "学习"
        "Eventos" -> "活动"
        "Baile" -> "舞蹈"
        "Fotografía" -> "摄影"
        "Viajes" -> "旅行"
        "Cocina" -> "烹饪"
        "Programación" -> "编程"
        "Moda" -> "时尚"
        "Ciencia" -> "科学"
        else -> key
    }

    // =================================================================
    // 首页
    // =================================================================

    override val tabDiscover = "发现"
    override val tabMatches = "匹配"
    override val tabChats = "聊天"
    override val tabProfile = "我的"

    override val welcomeUser = "欢迎"
    override fun youHaveFreeSlot(descripcion: String) = "你有 $descripcion"
    override val addClassesToSeeSlots =
        "在“我的课表”里添加课程，就能看到你的空闲时间"
    override val noFreeSlotsToday = "今天你没有空闲时间"
    override val suggestionsToday = "今天的推荐"
    override val createActivitySection = "创建活动"
    override val addYourOwnActivity = "添加你自己的活动"
    override val inviteOthers = "邀请别人加入你的空闲时段"
    override val culturalAgenda = "文化日程"
    override val visitWebsite = "前往网站"
    override val matchesEmptyMsg = "你的匹配会显示在这里。"
    override val chatsEmptyMsg = "你的对话会显示在这里。"
    override val eventSiteInfoMsg = "正式版中，这个链接会打开活动网站。"

    override val placeLibrary = "图书馆"
    override val placeCourt = "M7A 球场"
    override val placeMultiRoom = "多功能厅"
    override val placeGreenZone = "绿化区"
    override val placeAuditorium = "礼堂"

    override val profilePhotoCd = "头像"
    override val notificationsCd = "通知"
    override val addCd = "添加"

    // =================================================================
    // 资料
    // =================================================================

    override val profileFallbackUsername = "用户"
    override val completeYourProfile = "完善你的资料"
    override val descriptionTitle = "简介"
    override val noBioYet = "你还没有添加简介。"
    override val interestsTitle = "兴趣"
    override val chooseInterestsHint = "点击“编辑”按钮选择你的兴趣。"
    override val scheduleSectionTitle = "课表"
    override val addScheduleAction = "添加课表"
    override val addClassesHint = "添加课程，发现你的空闲时间。"
    override val upsTitle = "哎呀！"
    override val settingsCd = "设置"
    override val changeProfilePhotoCd = "更换头像"
    override val accountUnknownError = "无法识别你的账号。请退出后重新登录。"
    override val photoChangeError = "无法更换你的照片，请稍后再试。"

    // =================================================================
    // 编辑资料
    // =================================================================

    override val editProfileTitle = "编辑资料"
    override val photoAndBasicsTitle = "头像与基本信息"
    override val photoBasicsSubtitle = "这是其他用户在滑动时看到的照片。"
    override val profilePhotoPreviewCd = "头像预览"
    override val currentPhotoCd = "当前头像"
    override val gallery = "相册"
    override val camera = "相机"
    override val namesLabel = "名"
    override val surnamesLabel = "姓氏"
    override val personalDetailsTitle = "个人详情"
    override val selectAnOption = "请选择一项"
    override val genderLabel = "性别"
    override val interestsAndBioTitle = "兴趣与简介"
    override val interestsBioSubtitle = "最多选择 3 个兴趣（会显示在你的资料里）"
    override val descriptionFieldLabel = "简介"
    override val bioPlaceholder = "介绍一下你自己吧..."
    override val saveChanges = "保存更改"
    override val profilePreviewTitle = "资料预览"
    override val noUniversity = "未填写大学"
    override val noInterestsSelected = "你还没有选择兴趣。"
    override val changesSavedTitle = "更改已保存！"
    override val changesSavedMsg = "你的资料已成功更新。"
    override val editSaveErrorMsg = "无法保存更改，请稍后再试。"

    // =================================================================
    // 课表
    // =================================================================

    override val mySchedule = "我的课表"
    override val prevDay = "前一天"
    override val nextDay = "后一天"
    override val addScheduleCd = "添加课表"
    override fun notePrefix(nota: String) = "备注：$nota"
    override val importFromGallery = "从相册导入"
    override val importGalleryMsg =
        "选择一张课表照片。正式版中，Gaply 会自动从图片识别课程。"
    override val viewActivitiesForSlot = "查看这个时段的活动"
    override val addScheduleTitle = "添加课表"
    override val tapPlusToAdd = "点按“+”添加课表"
    override val dayFreeUseArrows = "这天空着。用 ‹ › 查看其他日期。"
    override val tapToRetry = "点按重试"

    override val noConnectionSchedule = "没有网络连接。请检查网络后重试。"
    override val firestoreReadSchedule = "Firestore 不允许读取你的课表（会话或权限规则）。"
    override val loadScheduleFailed = "课表加载失败，请重试。"
    override val loadGenericFailed = "加载失败，请重试。"
    override val saveGenericFailed = "保存失败，请重试。"
    override val deleteGenericFailed = "删除失败，请重试。"
    override val firestoreWriteRules = "Firestore 不允许保存（会话或权限规则）。"
    override val firestoreDeleteRules = "Firestore 不允许删除（会话或权限规则）。"

    // =================================================================
    // 编辑课表
    // =================================================================

    override val deleteClassTitle = "删除课程"
    override fun deleteClassMessage(nombre: String) =
        "要从课表中删除“$nombre”吗？此操作无法撤销。"
    override val thisClass = "这门课"
    override val scheduleFormTitle = "课表"
    override val closeWithoutSaving = "不保存并关闭"
    override val titleLabel = "标题"
    override val titlePlaceholder = "课程名称"
    override val startLabel = "开始"
    override val endLabel = "结束"
    override val timeSectionLabel = "时间"
    override val startsLabel = "开始"
    override val endsLabel = "结束"
    override val addNotesTitle = "添加备注"
    override val notesPlaceholder = "写条备注（可选）。"
    override val colorLabel = "颜色"
    override val selectDay = "请选择星期几"
    override val selectTime = "请选择时间"
    override val selectColor = "请选择颜色"
    override val classNoLongerExists = "该课程已不存在或已被删除。"
    override val writeTitleError = "请输入课程名称。"
    override val endMustBeAfterStart = "结束时间必须晚于开始时间。"

    // =================================================================
    // 空闲时段
    // =================================================================

    override val myFreeSlots = "我的空闲时段"
    override val tellUsLikesFreeTime =
        "告诉我们你喜欢什么、什么时候有空。推荐会自动排序。"
    override val yourInterests = "你的兴趣"
    override fun selectedCount(selected: Int, max: Int) = "已选 $selected / $max"
    override val yourFreeSlotTitle = "你的空闲时段"
    override val fromLabel = "从"
    override val toLabel = "到"
    override val startShort = "开始"
    override val endShort = "结束"
    override val suggestionsTitle = "推荐"
    override val fixTimeToSeeSuggestions = "调整时间才能看到推荐。"
    override val noneFitsSlot = "没有活动适合这个时段。换个日期或时间试试。"
    override fun doesNotFit(count: Int) = "放不下（$count）"
    override val utilityExplanation =
        "实用性 = 兴趣 65% + 时间匹配 35%。放不进你时段的活动会连同原因一起排除。"
    override fun yourSlotIs(rango: String) = "你的时段：$rango"

    override fun activityName(id: String, fallback: String): String = when (id) {
        "futbol" -> "足球比赛"
        "repaso" -> "咖啡与复习"
        "mesa" -> "桌游锦标赛"
        "musica" -> "音乐活动"
        "baile" -> "舞蹈课"
        "artes" -> "艺术工作坊"
        "cine" -> "校园电影"
        "evento" -> "文化活动"
        else -> fallback
    }

    override fun activityDescription(id: String, fallback: String): String = when (id) {
        "futbol" -> "校园球场。"
        "repaso" -> "在咖啡厅小组复习。"
        "mesa" -> "活动室。"
        "musica" -> "礼堂。"
        "baile" -> "多功能厅。"
        "artes" -> "美术教室。"
        "cine" -> "礼堂。"
        "evento" -> "主广场。"
        else -> fallback
    }

    override fun reasonInterestsMatch(compartidos: String) =
        "符合你的兴趣：$compartidos"
    override val reasonNoInterestsMatch = "与你目前的兴趣不符"
    override val reasonNoInterestsRegistered = "你还没有登记兴趣"
    override fun reasonFitsCompletely(hueco: String) = "完全适合你的空闲时段：$hueco"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "用了你 $hueco 时段里的 $minutos 分钟"
    override fun reasonNoFreeSlot(franja: String) = "不适合任何空闲时段（$franja）"

    // =================================================================
    // 设置
    // =================================================================

    override val settingsTitle = "设置"
    override val accountSecurity = "账号与安全"
    override val changePasswords = "修改密码"
    override val updateYourAccess = "更新你的登录信息"
    override val deactivateDeleteAccount = "停用与删除账号"
    override val deactivateDeleteAccountSubtitle = "临时暂停或永久删除"
    override val twoFactor = "双重身份验证"
    override val preferences = "偏好设置"
    override val theme = "主题"
    override val enableDarkMode = "开启深色模式"
    override val themeSoonMessage = "将在正式版本中推出，我们正在开发中。"
    override val notifications = "通知"
    override val configureAlerts = "配置提醒"
    override val language = "语言"
    override val saveLanguage = "保存语言"
    override val languageSoonMessage = "将在正式版本中推出，我们正在开发中。"
    override val aboutSection = "关于"
    override val rateTheApp = "给应用评分"
    override val aboutGaply = "关于 Gaply"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "退出登录"

    // =================================================================
    // 修改密码
    // =================================================================

    override val changePasswordTitle = "修改密码"
    override val currentPasswordLabel = "当前密码"
    override val newPasswordLabel = "新密码"
    override val changePasswordButton = "保存密码"
    override val passwordUpdatedTitle = "密码已更新！"
    override val passwordUpdatedMsg = "你的密码已成功修改。"
    override val changePasswordErrorTitle = "无法修改"
    override val changePasswordReloginMsg =
        "为安全起见，请退出后重新登录再修改密码"
    override val changePasswordErrorMsg = "无法修改密码，请重试。"

    // =================================================================
    // 账号: 停用 / 删除
    // =================================================================

    override val deleteOrDeactivateTitle = "删除还是停用？"
    override val deleteOrDeactivateDesc =
        "如果你想暂时离开 Gaply，只需停用账号。" +
            "如果选择删除，30 天后将无法恢复。"
    override val deactivateOptionTitle = "停用账号"
    override val deactivateOptionDesc = "你可以随时用原来的登录信息重新激活。"
    override val deleteOptionTitle = "删除账号"
    override val deleteOptionDesc = "此操作会永久删除你的账号和所有数据。"
    override val confirmDeactivateTitle = "要停用这个账号吗？"
    override val confirmDeactivateDesc =
        "在你用原来的登录信息重新登录之前，你的账号将保持停用状态。"
    override val confirmDeactivateButton = "停用"
    override val confirmDeactivateListTitle = "停用账号后："
    override val confirmDeactivateBulletPrivacy = "任何人都无法看到你的账号和内容。"
    override val confirmDeactivateBulletOutsideData =
        "未存储在你账号里的数据（比如私信）" +
            "仍可能对其他用户可见。"
    override val confirmDeactivateBulletDataKept =
        "Gaply 会保留你的数据，" +
            "等你重新激活账号后即可找回。"
    override val confirmDeactivateBulletRecovery =
        "你可以随时用原来的登录信息重新激活账号，" +
            "并找回全部内容。"
    override val deactivateErrorTitle = "无法停用"
    override val deactivateErrorMsg = "请几分钟后再试。"
    override val deactivateReloginMsg =
        "为安全起见，请退出后重新登录再停用账号"
    override val deleteAccountTitle = "删除账号"
    override val deleteAccountDesc =
        "此操作是永久性的：你的账号、资料、课表和数据都将被删除。"
    override val deleteAccountButton = "永久删除"
    override val deleteAccountErrorMsg = "无法删除账号，请重试。"
    override val deleteAccountConfirmTitle = "要删除这个账号吗？"
    override val deleteAccountErrorTitle = "无法删除"
    override val deleteAccountReloginMsg =
        "为安全起见，请退出后重新登录再删除账号"
    override val deleteWarningPermanent = "删除账号是永久性的。"
    override val deleteWarningProfileData = "你资料中的所有数据都会被清除。"
    override val deleteWarningActivity = "你的活动记录和已保存的内容将会丢失。"
    override val deleteWarningIrreversible = "此操作无法撤销。"

    // =================================================================
    // 重置密码
    // =================================================================

    override val resetPasswordHeader = "找回密码"
    override val resetPasswordTitle = "重置密码"
    override val resetPasswordSubtitle = "我们会给你发送一个创建新密码的链接。"
    override val sendResetLink = "发送链接"
    override val resetLinkSentTitle = "链接已发送！"
    override val resetLinkSentMsg = "请查收邮件以重置密码。"
    override val invalidEmailError = "请输入有效的邮箱"
    override val resetPasswordErrorTitle = "无法发送链接"
    override val resetPasswordErrorMsg = "请检查邮箱后重试。"

    // =================================================================
    // 评分 / 关于
    // =================================================================

    override val rateAppTitle = "给这个应用评分"
    override val rateAppSubtitle = "你的评价帮助我们变得更好"
    override val thanksTitle = "感谢你的反馈！"
    override val thanksMsg = "你的评分帮助 Gaply 不断改进。"
    override fun thanksRatingMsg(stars: Int) =
        "很高兴你给 Gaply 打了 $stars 星。"
    override val starRatingCd = "星级评分"
    override fun starLabel(star: Int) = "$star 星"
    override val rateCommentLabel = "原因（可选）:"
    override val rateCommentPlaceholder = "告诉我们你喜欢什么，或者哪里\n可以改进..."
    override val rateSendButton = "发送"
    override val aboutLogoCd = "Gaply 的标志"
    override val logoCd = "Gaply 的标志"
    override val welcomeIllustrationCd = "主插图"
    override val aboutBody =
        "Gaply 旨在消除沟通障碍，帮助" +
            "学生更好地利用课间的时光。"

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "查看适合你空档的活动"
    override val myActivitiesCardTitle = "我的活动"
    override val myActivitiesCardSubtitle = "编辑或取消你已发布的活动"
    override val activitiesListTitle = "适合你空档的活动"
    override val activitiesDetailTitle = "活动"
    override val huecoBannerDemo = "你有2小时空闲 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "根据你的兴趣"
    override val activitiesEmptyListMsg = "此空档还没有发布活动。来发布第一个吧！"
    override val likeCd = "点赞"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "你还没有发布活动。用「$createWith」创建一个，它会显示在这里。"
    override val cancelActivityTitle = "取消活动"
    override val cancelActivityConfirmMsg = "你确定要取消此活动吗？"
    override val confirmCancelActivity = "是，取消活动"
    override val activityUnavailableMsg = "该活动已不可用。"
    override val organizerLabel = "组织者"
    override fun attendeesCount(count: Int) = "参与者：$count"
    override val joinActivity = "加入"
    override val alreadyJoined = "你已加入"
    override val leaveActivityTitle = "退出活动"
    override val leaveActivityConfirmMsg =
        "您要退出此活动吗？您可以随时重新加入。"
    override val confirmLeaveActivity = "是，退出"
    override val comingSoonTitle = "即将推出！"
    override val comingSoonMsg = "我们正在开发中，即将添加"
    override val eventChat = "活动聊天"
    override val locationLabel = "位置"
    override val howToGetThere = "如何到达"
    override val beFirstToJoin = "成为第一个加入的人"
    override val manageYourActivity = "管理你的活动"
    override val newActivityTitle = "新活动"
    override val editActivityTitle = "编辑活动"
    override val eventTitleLabel = "活动标题"
    override val categoryLabel = "类别"
    override val activityPhotoLabel = "活动地点照片"
    override val locationPlaceLabel = "位置 / 地点"
    override val locationPlacePlaceholder = "例如：M7A球场、204教室、图书馆…"
    override val durationLabel = "预计时长"
    override val dateLabel = "日期"
    override val descriptionOptionalLabel = "描述（可选）"
    override val activityDescriptionPlaceholder = "添加更多关于活动的详情..."
    override val publishActivity = "发布活动"
    override val selectTimeTitle = "选择时间"
    override val expandCategoriesCd = "展开类别"
    override val placePhotoCd = "地点照片"
    override val removePhotoCd = "移除照片"
    override val tapToAddPlacePhoto = "点按添加地点照片"
    override val defaultColorPhotoHint = "如果不选择，将使用默认的绿色"
    override val duration30 = "30分钟"
    override val duration1h = "1小时"
    override val duration2h = "2小时"

    // =================================================================
    // MODULE 5 · MATCHES
    // =================================================================

    override val matchesNoMoreSuggestions = "暂时没有更多推荐了"
    override val matchesUtilityExplanation =
        "效用 = 60% 兴趣 + 40% 共同空闲时间。" +
            "只有拥有共同空闲时间的资料才会出现。"
    override val loadingData = "加载中…"

    override val swipeLike = "喜欢"
    override val swipePass = "跳过"
    override val swipeSuper = "超级喜欢"
    override val superLikeCd = "超级喜欢"

    override val matchTitle = "配对成功！"
    override fun matchMessage(name: String) =
        "你和 $name 互相喜欢！现在可以聊天了。"
    override val openChat = "打开聊天"
    override val activityJoinedTitle = "你已加入该活动！"
    override fun activityJoinedMsg(titulo: String) = "「$titulo」的聊天已向参与者开放。"

    override fun ageYears(age: Int) = "$age 岁"
    override fun commonFreeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "共同 1 小时" else "共同 $horas 小时"
        }

        minutos >= 60 -> "共同 ${minutos / 60} 小时 ${minutos % 60} 分钟"
        else -> "共同 $minutos 分钟"
    }

    override val reasonNoCommonFreeSlot = "你们没有共同的空闲时间"
    override fun reasonSharedFreeSlot(rango: String) = "共同空闲时间：$rango"

    // =================================================================
    // MODULE 5 · CHATS
    // =================================================================

    override val chatGroupTag = "群组"
    override val chatNoMessages = "还没有消息。发出第一条吧！"
    override val messageInputHint = "输入消息..."
    override val sendMessageCd = "发送消息"
    override val chatActivityActive = "今天进行中"
    override fun chatActivityScheduled(fecha: String) = "安排在：$fecha"

    override val timeJustNow = "刚刚"
    override fun timeAgoMinutes(minutes: Int) = "$minutes 分钟前"
    override fun timeAgoHours(hours: Int) = "$hours 小时前"
    override fun timeAgoDays(days: Int) = if (days <= 1) "昨天" else "$days 天前"

    override fun unreadMessages(count: Int) = "$count 条未读消息"
}
