package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 日本語のテキスト。 */
object StringsJa : AppStrings {

    override val javaLocale: Locale = Locale.JAPAN

    // =================================================================
    // 共通
    // =================================================================

    override val back = "戻る"
    override val close = "閉じる"
    override val cancel = "キャンセル"
    override val accept = "OK"
    override val ok = "OK"
    override val done = "完了"
    override val delete = "削除"
    override val edit = "編集"
    override val save = "保存"
    override val next = "次へ"
    override val skip = "スキップ"
    override val selected = "選択済み"
    override val add = "追加"
    override val retry = "タップして再試行"
    override val acceptShort = "OK"

    override val connectionErrorTitle = "接続エラー！"
    override val connectionErrorMessage = "接続を確認して、後でもう一度試してください。"
    override val noActiveSessionMsg = "アクティブなセッションがありません"

    override val successCd = "成功"
    override val infoCd = "情報"
    override val warningCd = "警告"
    override val errorCd = "エラー / オフライン"
    override val closeCd = "閉じる"
    override val showPassword = "パスワードを表示"
    override val hidePassword = "パスワードを非表示"
    override val expandOptionsCd = "オプションを展開"

    override fun dayName(dayKey: String): String = when (dayKey) {
        "Lunes" -> "月曜日"
        "Martes" -> "火曜日"
        "Miércoles" -> "水曜日"
        "Jueves" -> "木曜日"
        "Viernes" -> "金曜日"
        "Sábado" -> "土曜日"
        "Domingo" -> "日曜日"
        else -> dayKey
    }

    override fun formatTime(hora24: String): String {
        val partes = hora24.trim().split(":")
        val horas = partes.getOrNull(0)?.toIntOrNull() ?: 0
        val minutos = partes.getOrNull(1)?.toIntOrNull() ?: 0
        val sufijo = if (horas < 12) "午前" else "午後"
        val hora12 = when {
            horas == 0 || horas == 12 -> 12
            horas > 12 -> horas - 12
            else -> horas
        }
        return "%s%d:%02d".format(sufijo, hora12, minutos)
    }

    override fun freeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> "${minutos / 60}時間の空き"

        minutos >= 60 -> "${minutos / 60}時間${minutos % 60}分の空き"
        else -> "${minutos}分の空き"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("M月d日", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // ようこそ
    // =================================================================

    override val welcomeTagline = "授業の合間に何か見つけよう"
    override val signIn = "ログイン"
    override val createAccount = "アカウント作成"
    override val accountDeletedTitle = "アカウントを削除しました！"
    override val accountDeletedMsg = "アカウントとデータは正常に削除されました。"
    override val accountDeactivatedTitle = "アカウントを無効化しました！"
    override val accountDeactivatedMsg =
        "いつでも同じログイン情報で再有効化できます。"

    // =================================================================
    // ログイン
    // =================================================================

    override val loginTitle = "Gaplyアカウントでログイン"
    override val usernameLabel = "ユーザー名"
    override val passwordLabel = "パスワード"
    override val forgotPassword = "パスワードを忘れましたか？"
    override val loginButton = "ログイン"
    override val noAccountYet = "アカウントをお持ちでないですか？ "
    override val registerLink = "新規登録"
    override val marketingOptOut =
        "弊社の製品やサービスに関するマーケティング情報を受け取らない場合は、このチェックボックスを選択してください。"
    override val userNotFoundTitle = "アカウントが見つかりません！"
    override val userNotFoundMsg = "そのユーザー名のアカウントはありません。確認するか、新規登録してください。"
    override val invalidEmailFormat = "メールアドレスの形式が正しくありません"
    override val wrongPasswordMsg = "パスワードが正しくありません"
    override val loginFailedMsg = "ログインできませんでした。もう一度お試しください。"
    override val noAccountWithEmailMsg = "そのメールアドレスのアカウントはありません"

    // =================================================================
    // 項目エラー
    // =================================================================

    override val fieldRequired = "この項目は必須です"
    override val minChars3 = "3文字以上"
    override val minChars6 = "6文字以上"
    override val validEmailPrompt = "有効なメールアドレスを入力してください"
    override val passwordsMismatch = "パスワードが一致しません"
    override val usernameTaken = "そのユーザー名は既に使われています"
    override val emailTaken = "そのメールアドレスは既に登録されています"
    override val selectBirthdate = "生年月日を選択してください"
    override val selectUniversity = "大学を選択してください"

    override fun minInterestsWarning(max: Int) = "興味は最大${max}個まで選べます。"

    // =================================================================
    // 登録
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "ステップ $step / $total"
    override val stepAccountData = "アカウント情報"
    override val stepPersonalInfo = "個人情報"
    override val stepPurpose = "目的"
    override val stepInterests = "興味"
    override val stepPhotoBio = "写真と自己紹介"

    override val createYourAccountTitle = "アカウントを作成"
    override val startWithLoginData = "ログイン情報から始めましょう。"
    override val usernameAtHint = "ユーザー名（@username）"
    override val emailLabel = "メールアドレス"
    override val confirmPasswordLabel = "パスワード（確認）"

    override val personalInfoTitle = "個人情報"
    override val tellUsAboutYou = "あなたについて教えてください。"
    override val firstNameLabel = "名前 *"
    override val lastNameLabel = "苗字"
    override val birthDateLabel = "生年月日 *"
    override val dayLabel = "日"
    override val monthLabel = "月"
    override val yearLabel = "年"
    override val whereStudyNow = "今はどこで勉強していますか？"
    override val universityLabel = "大学 *"
    override val genderIdentityLabel = "ジェンダー"
    override val chooseYourGender = "あなたのジェンダーを選んでください"

    override val whatBringsGaply = "Gaplyに参加した理由は何ですか？"
    override val chooseGoalHint = "あなたに最も合う目標を選んでください。あとから変更できます。"

    override val selectSomeInterests = "興味をいくつか選んでください"
    override val addInterests = "興味を追加"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "合計で最大${max}個まで選べます（$selected/$total）。"

    override val uploadPhoto = "写真をアップロード"
    override val takePhoto = "写真を撮る"
    override fun helloName(name: String) = "こんにちは、${name}さん"
    override val giveShortBio = "短い自己紹介を書いてください:"
    override val bioLabel = "自己紹介"
    override val saveProfile = "プロフィールを保存"

    override val profileSavedTitle = "プロフィールを保存しました！"
    override val profileSavedMsg =
        "プロフィールを保存しました！プロフィール情報は正常に保存されました。"
    override val galleryInfoTitle = "フォトギャラリー"
    override val galleryInfoMsg = "正式版では、このボタンが写真ギャラリーを開きます。"
    override val cameraInfoTitle = "カメラ"
    override val cameraInfoMsg = "正式版では、このボタンがスマートフォンのカメラを開きます。"
    override val saveErrorTitle = "保存エラー！"
    override val saveErrorMsg = "プロフィールの保存に失敗しました！申し訳ありません、後でもう一度お試しください。"

    override val months = listOf(
        "1月", "2月", "3月", "4月", "5月", "6月",
        "7月", "8月", "9月", "10月", "11月", "12月",
    )

    override val genders = listOf("女性", "男性", "ノンバイナリー", "回答しない")

    override val goals = listOf(
        "\uD83D\uDCDA 勉強したい",
        "\uD83C\uDFC0 何かしたい",
        "\uD83D\uDE09 時間がある",
        "\uD83E\uDD7A まだ決めていない",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "その他",
    )

    override fun interestLabel(key: String): String = when (key) {
        "Deportes" -> "スポーツ"
        "Música" -> "音楽"
        "Videojuegos" -> "ゲーム"
        "Artes" -> "アート"
        "Juegos de mesa" -> "ボードゲーム"
        "Películas" -> "映画"
        "Estudio" -> "勉強"
        "Eventos" -> "イベント"
        "Baile" -> "ダンス"
        "Fotografía" -> "写真"
        "Viajes" -> "旅行"
        "Cocina" -> "料理"
        "Programación" -> "プログラミング"
        "Moda" -> "ファッション"
        "Ciencia" -> "科学"
        else -> key
    }

    // =================================================================
    // ホーム
    // =================================================================

    override val tabDiscover = "発見"
    override val tabMatches = "マッチ"
    override val tabChats = "チャット"
    override val tabProfile = "プロフィール"

    override val welcomeUser = "ようこそ"
    override fun youHaveFreeSlot(descripcion: String) = "$descripcion があります"
    override val addClassesToSeeSlots =
        "「マイ予定」に授業を追加すると、空き時間が見られます"
    override val noFreeSlotsToday = "今日は空き時間がありません"
    override val suggestionsToday = "今日のおすすめ"
    override val createActivitySection = "アクティビティを作成"
    override val addYourOwnActivity = "自分のアクティビティを追加"
    override val inviteOthers = "空き時間に一緒に参加を募集"
    override val culturalAgenda = "カルチャーイベント"
    override val visitWebsite = "サイトを見る"
    override val matchesEmptyMsg = "マッチはここに表示されます。"
    override val chatsEmptyMsg = "トークはここに表示されます。"
    override val matchesNoMoreMsg = "今はこれ以上の提案がありません。また後でチェックしてください！"
    override val matchTitle = "マッチです！"
    override val chatHint = "メッセージを入力..."
    override val joinToChat = "グループでチャットするには活動に参加してください。"
    override val swipePassCd = "スキップ"
    override val swipeStarCd = "気になる"
    override val swipeLikeCd = "いいね"
    override fun createdBy(nombre: String) = "${nombre}が作成"
    override val eventSiteInfoMsg = "正式版では、このリンクがイベントサイトを開きます。"

    override val placeLibrary = "図書館"
    override val placeCourt = "M7Aコート"
    override val placeMultiRoom = "多目的室"
    override val placeGreenZone = "グリーンゾーン"
    override val placeAuditorium = "講堂"

    override val profilePhotoCd = "プロフィール写真"
    override val notificationsCd = "通知"
    override val addCd = "追加"

    // =================================================================
    // プロフィール
    // =================================================================

    override val profileFallbackUsername = "ユーザー"
    override val completeYourProfile = "プロフィールを完成させましょう"
    override val descriptionTitle = "自己紹介"
    override val noBioYet = "まだ自己紹介を追加していません。"
    override val interestsTitle = "興味"
    override val chooseInterestsHint = "「編集」ボタンから興味を選べます。"
    override val scheduleSectionTitle = "予定"
    override val addScheduleAction = "予定を追加"
    override val addClassesHint = "授業を追加すると、空き時間がわかります。"
    override val upsTitle = "おっと！"
    override val settingsCd = "設定"
    override val changeProfilePhotoCd = "プロフィール写真を変更"
    override val accountUnknownError = "アカウントを確認できませんでした。ログアウトして、もう一度ログインしてください。"
    override val photoChangeError = "写真を変更できませんでした。後でもう一度お試しください。"

    // =================================================================
    // プロフィール編集
    // =================================================================

    override val editProfileTitle = "プロフィールを編集"
    override val photoAndBasicsTitle = "プロフィール写真と基本情報"
    override val photoBasicsSubtitle = "他のユーザーがスワイプで見る写真です。"
    override val profilePhotoPreviewCd = "プロフィール写真のプレビュー"
    override val currentPhotoCd = "現在のプロフィール写真"
    override val gallery = "ギャラリー"
    override val camera = "カメラ"
    override val namesLabel = "名前"
    override val surnamesLabel = "苗字"
    override val personalDetailsTitle = "個人情報"
    override val selectAnOption = "選択してください"
    override val genderLabel = "ジェンダー"
    override val interestsAndBioTitle = "興味と自己紹介"
    override val interestsBioSubtitle = "興味は3つまで選べます（プロフィールに表示されます）"
    override val descriptionFieldLabel = "自己紹介"
    override val bioPlaceholder = "あなたについて教えてください..."
    override val saveChanges = "変更を保存"
    override val profilePreviewTitle = "プロフィールのプレビュー"
    override val noUniversity = "大学未設定"
    override val noInterestsSelected = "まだ興味が選ばれていません。"
    override val changesSavedTitle = "変更を保存しました！"
    override val changesSavedMsg = "プロフィールが正常に更新されました。"
    override val editSaveErrorMsg = "変更を保存できませんでした。後でもう一度お試しください。"

    // =================================================================
    // 予定
    // =================================================================

    override val mySchedule = "マイ予定"
    override val prevDay = "前日"
    override val nextDay = "翌日"
    override val addScheduleCd = "予定を追加"
    override fun notePrefix(nota: String) = "メモ：$nota"
    override val importFromGallery = "ギャラリーから読み込む"
    override val importGalleryMsg =
        "予定の写真を選んでください。正式版では、Gaplyが画像から授業を自動で読み取ります。"
    override val viewActivitiesForSlot = "この空き時間のアクティビティを見る"
    override val addScheduleTitle = "予定を追加"
    override val tapPlusToAdd = "「+」をタップして予定を追加"
    override val dayFreeUseArrows = "この日は空いています。‹ › で他の日を見られます。"
    override val tapToRetry = "タップして再試行"

    override val noConnectionSchedule = "接続がありません。インターネットを確認してもう一度試してください。"
    override val firestoreReadSchedule = "Firestoreがあなたの予定の読み取りを許可しませんでした（セッションまたはルール）。"
    override val loadScheduleFailed = "予定を読み込めませんでした。もう一度お試しください。"
    override val loadGenericFailed = "読み込めませんでした。もう一度お試しください。"
    override val saveGenericFailed = "保存できませんでした。もう一度お試しください。"
    override val deleteGenericFailed = "削除できませんでした。もう一度お試しください。"
    override val firestoreWriteRules = "Firestoreがあなたの保存を許可しませんでした（セッションまたはルール）。"
    override val firestoreDeleteRules = "Firestoreがあなたの削除を許可しませんでした（セッションまたはルール）。"

    // =================================================================
    // 予定編集
    // =================================================================

    override val deleteClassTitle = "授業を削除"
    override fun deleteClassMessage(nombre: String) =
        "予定から「$nombre」を削除しますか？この操作は元に戻せません。"
    override val thisClass = "この授業"
    override val scheduleFormTitle = "予定"
    override val closeWithoutSaving = "保存せずに閉じる"
    override val titleLabel = "タイトル"
    override val titlePlaceholder = "授業名"
    override val startLabel = "開始"
    override val endLabel = "終了"
    override val timeSectionLabel = "時間"
    override val startsLabel = "開始"
    override val endsLabel = "終了"
    override val addNotesTitle = "メモを追加"
    override val notesPlaceholder = "メモを入力（任意）。"
    override val colorLabel = "色"
    override val selectDay = "曜日を選択してください"
    override val selectTime = "時間を選択してください"
    override val selectColor = "色を選択してください"
    override val classNoLongerExists = "この授業はもう存在しないか、削除されました。"
    override val writeTitleError = "授業名を入力してください。"
    override val endMustBeAfterStart = "終了時刻は開始時刻より後にしてください。"

    // =================================================================
    // 空き時間
    // =================================================================

    override val myFreeSlots = "私の空き時間"
    override val tellUsLikesFreeTime =
        "好きなことと空いてる時間を教えてください。おすすめは自動で並び替わります。"
    override val yourInterests = "あなたの興味"
    override fun selectedCount(selected: Int, max: Int) = "$selected / $max 選択中"
    override val yourFreeSlotTitle = "あなたの空き時間"
    override val fromLabel = "開始"
    override val toLabel = "終了"
    override val startShort = "開始"
    override val endShort = "終了"
    override val suggestionsTitle = "おすすめ"
    override val fixTimeToSeeSuggestions = "時間を修正するとおすすめが表示されます。"
    override val noneFitsSlot = "その時間に合うアクティビティがありません。別の日や時間をお試しください。"
    override fun doesNotFit(count: Int) = "合わない（$count）"
    override val utilityExplanation =
        "活用度 = 興味65% + 時間の合計35%。空き時間に収まらないものは理由付きで除外されます。"
    override fun yourSlotIs(rango: String) = "あなたの空き時間：$rango"

    override fun activityName(id: String, fallback: String): String = when (id) {
        "futbol" -> "サッカーの試合"
        "repaso" -> "コーヒーと勉強"
        "mesa" -> "ボードゲーム大会"
        "musica" -> "音楽セッション"
        "baile" -> "ダンスレッスン"
        "artes" -> "アートワークショップ"
        "cine" -> "キャンパスシネマ"
        "evento" -> "文化イベント"
        else -> fallback
    }

    override fun activityDescription(id: String, fallback: String): String = when (id) {
        "futbol" -> "キャンパスのコート。"
        "repaso" -> "カフェでグループ勉強。"
        "mesa" -> "活動室。"
        "musica" -> "講堂。"
        "baile" -> "多目的フロア。"
        "artes" -> "アート教室。"
        "cine" -> "講堂。"
        "evento" -> "メイン広場。"
        else -> fallback
    }

    override fun reasonInterestsMatch(compartidos: String) =
        "あなたの興味と一致：$compartidos"
    override val reasonNoInterestsMatch = "今の興味とは一致しません"
    override val reasonNoInterestsRegistered = "まだ興味が登録されていません"
    override fun reasonFitsCompletely(hueco: String) = "$hueco の空きに完全に収まります"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "$hueco の空きから ${minutos}分使えます"
    override fun reasonNoFreeSlot(franja: String) = "どの空き時間にも入りません（$franja）"

    // =================================================================
    // 設定
    // =================================================================

    override val settingsTitle = "設定"
    override val accountSecurity = "アカウントとセキュリティ"
    override val changePasswords = "パスワードを変更"
    override val updateYourAccess = "ログイン情報を更新"
    override val deactivateDeleteAccount = "アカウントの無効化と削除"
    override val deactivateDeleteAccountSubtitle = "一時停止または完全な削除"
    override val twoFactor = "二段階認証"
    override val preferences = "環境設定"
    override val theme = "テーマ"
    override val enableDarkMode = "ダークモードをオン"
    override val themeSoonMessage = "最終版に搭載予定です。開発中です。"
    override val notifications = "通知"
    override val configureAlerts = "通知を設定"
    override val language = "言語"
    override val saveLanguage = "言語を保存"
    override val languageSoonMessage = "最終版に搭載予定です。開発中です。"
    override val aboutSection = "情報"
    override val rateTheApp = "アプリを評価"
    override val aboutGaply = "Gaplyについて"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "ログアウト"

    // =================================================================
    // パスワード変更
    // =================================================================

    override val changePasswordTitle = "パスワードを変更"
    override val currentPasswordLabel = "現在のパスワード"
    override val newPasswordLabel = "新しいパスワード"
    override val changePasswordButton = "パスワードを保存"
    override val passwordUpdatedTitle = "パスワードを更新しました！"
    override val passwordUpdatedMsg = "パスワードは正常に変更されました。"
    override val changePasswordErrorTitle = "変更できませんでした"
    override val changePasswordReloginMsg =
        "セキュリティのため、ログアウトしてからもう一度ログインするとパスワードを変更できます"
    override val changePasswordErrorMsg = "パスワードを変更できませんでした。もう一度お試しください。"

    // =================================================================
    // アカウント: 無効化 / 削除
    // =================================================================

    override val deleteOrDeactivateTitle = "削除と無効化、どちらにしますか？"
    override val deleteOrDeactivateDesc =
        "Gaplyから一時的に離れる場合は、アカウントを無効化するだけにしてください。 " +
            "削除を選んだ場合、30日が経過すると元に戻せません。"
    override val deactivateOptionTitle = "アカウントを無効化"
    override val deactivateOptionDesc = "いつでも同じログイン情報で再有効化できます。"
    override val deleteOptionTitle = "アカウントを削除"
    override val deleteOptionDesc = "この操作で、アカウントとすべてのデータが完全に削除されます。"
    override val confirmDeactivateTitle = "このアカウントを無効化しますか？"
    override val confirmDeactivateDesc =
        "同じログイン情報でログインするまで、アカウントは無効のままになります。"
    override val confirmDeactivateButton = "無効化"
    override val confirmDeactivateListTitle = "アカウントを無効化すると:"
    override val confirmDeactivateBulletPrivacy = "誰もあなたのアカウントやその内容を見ることができなくなります。"
    override val confirmDeactivateBulletOutsideData =
        "アカウントに保存されていないデータ（ダイレクトメッセージなど）は" +
            "他のユーザーから見えるままです。"
    override val confirmDeactivateBulletDataKept =
        "Gaplyはデータを保持するため、アカウントを再有効化したとき" +
            "にいつでも取り戻せます。"
    override val confirmDeactivateBulletRecovery =
        "同じログイン情報を使用すれば、いつでもアカウントを再有効化して" +
            "すべての内容を取り戻せます。"
    override val deactivateErrorTitle = "無効化できませんでした"
    override val deactivateErrorMsg = "数分後にもう一度お試しください。"
    override val deactivateReloginMsg =
        "セキュリティのため、ログアウトしてからもう一度ログインするとアカウントを無効化できます"
    override val deleteAccountTitle = "アカウントを削除"
    override val deleteAccountDesc =
        "この操作は永続的で、アカウント・プロフィール・予定・データがすべて削除されます。"
    override val deleteAccountButton = "完全に削除"
    override val deleteAccountErrorMsg = "アカウントを削除できませんでした。もう一度お試しください。"
    override val deleteAccountConfirmTitle = "このアカウントを削除しますか？"
    override val deleteAccountErrorTitle = "削除できませんでした"
    override val deleteAccountReloginMsg =
        "セキュリティのため、ログアウトしてからもう一度ログインするとアカウントを削除できます"
    override val deleteWarningPermanent = "アカウントの削除は永続的です。"
    override val deleteWarningProfileData = "プロフィールのすべてのデータが消去されます。"
    override val deleteWarningActivity = "アクティビティ履歴と保存された内容は失われます。"
    override val deleteWarningIrreversible = "この操作は元に戻せません。"

    // =================================================================
    // パスワード再設定
    // =================================================================

    override val resetPasswordHeader = "パスワードを再取得"
    override val resetPasswordTitle = "パスワードを再設定"
    override val resetPasswordSubtitle = "新しいパスワードを作成するためのリンクをお送りします。"
    override val sendResetLink = "リンクを送信"
    override val resetLinkSentTitle = "リンクを送信しました！"
    override val resetLinkSentMsg = "メールを確認して、パスワードを再設定してください。"
    override val invalidEmailError = "有効なメールアドレスを入力してください"
    override val resetPasswordErrorTitle = "リンクを送信できませんでした"
    override val resetPasswordErrorMsg = "メールアドレスを確認してもう一度お試しください。"

    // =================================================================
    // 評価 / 情報
    // =================================================================

    override val rateAppTitle = "このアプリを評価"
    override val rateAppSubtitle = "あなたの意見がGaplyの改善に役立ちます"
    override val thanksTitle = "ご意見ありがとうございます！"
    override val thanksMsg = "あなたの評価がGaplyの改善に役立ちます。"
    override fun thanksRatingMsg(stars: Int) =
        "Gaplyを${stars}つ星で評価いただきありがとうございます。"
    override val starRatingCd = "星での評価"
    override fun starLabel(star: Int) = "${star}つ星"
    override val rateCommentLabel = "理由（任意）:"
    override val rateCommentPlaceholder = "気に入った点や改善してほしい\nことを教えてください..."
    override val rateSendButton = "送信"
    override val aboutLogoCd = "Gaplyのロゴ"
    override val logoCd = "Gaplyのロゴ"
    override val welcomeIllustrationCd = "メインイラスト"
    override val aboutBody =
        "Gaplyはコミュニケーションの壁を取りのぞき、" +
            "学生が授業の合間の時間をより有効に使えるようにするために生まれました。"

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "空き時間のアクティビティを見る"
    override val myActivitiesCardTitle = "自分のアクティビティ"
    override val myActivitiesCardSubtitle = "公開済みのものを編集またはキャンセル"
    override val activitiesListTitle = "空き時間のアクティビティ"
    override val activitiesDetailTitle = "アクティビティ"
    override val huecoBannerDemo = "2時間の空きがあります 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "あなたの興味に基づく"
    override val activitiesEmptyListMsg =
        "この空き時間のアクティビティはまだありません。最初に公開しましょう！"
    override val likeCd = "いいね"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "まだアクティビティを公開していません。「$createWith」で作成するとここに表示されます。"
    override val cancelActivityTitle = "アクティビティをキャンセル"
    override val cancelActivityConfirmMsg = "このアクティビティをキャンセルしますか？"
    override val confirmCancelActivity = "はい、キャンセルします"
    override val activityUnavailableMsg = "このアクティビティは利用できなくなりました。"
    override val organizerLabel = "主催者"
    override fun attendeesCount(count: Int) = "参加者: $count"
    override val joinActivity = "参加する"
    override val alreadyJoined = "参加済みです"
    override val leaveActivityTitle = "活動から退出する"
    override val leaveActivityConfirmMsg =
        "この活動から退出しますか？またいつでも参加できます。"
    override val confirmLeaveActivity = "はい、退出する"
    override val comingSoonTitle = "近日公開！"
    override val comingSoonMsg = "現在制作中で、近日追加されます"
    override val eventChat = "イベントチャット"
    override val locationLabel = "場所"
    override val howToGetThere = "行き方"
    override val beFirstToJoin = "最初に参加しましょう"
    override val manageYourActivity = "アクティビティを管理"
    override val newActivityTitle = "新しいアクティビティ"
    override val editActivityTitle = "アクティビティを編集"
    override val eventTitleLabel = "イベントタイトル"
    override val categoryLabel = "カテゴリー"
    override val activityPhotoLabel = "会場の写真"
    override val locationPlaceLabel = "場所 / ロケーション"
    override val locationPlacePlaceholder = "例: コートM7A、教室204、図書館…"
    override val durationLabel = "推定時間"
    override val dateLabel = "日付"
    override val descriptionOptionalLabel = "説明（任意）"
    override val activityDescriptionPlaceholder = "アクティビティの詳細を追加してください..."
    override val publishActivity = "アクティビティを公開"
    override val selectTimeTitle = "時間を選択"
    override val expandCategoriesCd = "カテゴリーを展開"
    override val placePhotoCd = "会場の写真"
    override val removePhotoCd = "写真を削除"
    override val tapToAddPlacePhoto = "タップして会場の写真を追加"
    override val defaultColorPhotoHint = "選択しない場合はデフォルトの緑色が使用されます"
    override val duration30 = "30分"
    override val duration1h = "1時間"
    override val duration2h = "2時間"
}
