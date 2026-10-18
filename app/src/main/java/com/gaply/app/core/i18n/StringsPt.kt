package com.gaply.app.core.i18n

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Textos em português do Brasil. */
object StringsPt : AppStrings {

    override val javaLocale: Locale = Locale("pt", "BR")

    // =================================================================
    // COMUM
    // =================================================================

    override val back = "Voltar"
    override val close = "Fechar"
    override val cancel = "Cancelar"
    override val accept = "Aceitar"
    override val ok = "OK"
    override val done = "Pronto"
    override val delete = "Excluir"
    override val edit = "Editar"
    override val save = "Salvar"
    override val next = "Próximo"
    override val skip = "Pular"
    override val selected = "Selecionado"
    override val add = "Adicionar"
    override val retry = "Toque para tentar novamente"
    override val acceptShort = "Aceitar"

    override val connectionErrorTitle = "Erro de conexão!"
    override val connectionErrorMessage = "Verifique sua conexão e tente novamente mais tarde."
    override val noActiveSessionMsg = "Não há uma sessão ativa"

    override val successCd = "Sucesso"
    override val infoCd = "Informações"
    override val warningCd = "Aviso"
    override val errorCd = "Erro / Sem conexão"
    override val closeCd = "Fechar"
    override val showPassword = "Mostrar senha"
    override val hidePassword = "Ocultar senha"
    override val expandOptionsCd = "Expandir opções"

    override fun dayName(dayKey: String): String = when (dayKey) {
        "Lunes" -> "segunda-feira"
        "Martes" -> "terça-feira"
        "Miércoles" -> "quarta-feira"
        "Jueves" -> "quinta-feira"
        "Viernes" -> "sexta-feira"
        "Sábado" -> "sábado"
        "Domingo" -> "domingo"
        else -> dayKey
    }

    override fun formatTime(hora24: String): String {
        val partes = hora24.trim().split(":")
        val horas = partes.getOrNull(0)?.toIntOrNull() ?: 0
        val minutos = partes.getOrNull(1)?.toIntOrNull() ?: 0
        val sufijo = if (horas < 12) "a.m." else "p.m."
        val hora12 = when {
            horas == 0 || horas == 12 -> 12
            horas > 12 -> horas - 12
            else -> horas
        }
        return "%d:%02d %s".format(hora12, minutos, sufijo)
    }

    override fun freeDuration(minutos: Int): String = when {
        minutos >= 60 && minutos % 60 == 0 -> {
            val horas = minutos / 60
            if (horas == 1) "1 hora livre" else "$horas horas livres"
        }

        minutos >= 60 -> "${minutos / 60} h ${minutos % 60} min livres"
        else -> "$minutos minutos livres"
    }

    override fun formatMonthDay(date: LocalDate): String =
        DateTimeFormatter.ofPattern("d 'de' MMMM", javaLocale).format(date)

    override fun formatMonthAbbrev(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", javaLocale).format(date)

    // =================================================================
    // BOAS-VINDAS
    // =================================================================

    override val welcomeTagline = "Encontre algo para fazer entre as aulas"
    override val signIn = "Entrar"
    override val createAccount = "Criar Conta"
    override val accountDeletedTitle = "Conta excluída!"
    override val accountDeletedMsg = "Sua conta e seus dados foram excluídos com sucesso."
    override val accountDeactivatedTitle = "Conta desativada!"
    override val accountDeactivatedMsg =
        "Você pode reativá-la quando quiser, com os mesmos dados de acesso."

    // =================================================================
    // LOGIN
    // =================================================================

    override val loginTitle = "Entre com sua conta do Gaply"
    override val usernameLabel = "Usuário"
    override val passwordLabel = "Senha"
    override val forgotPassword = "Esqueceu sua senha?"
    override val loginButton = "Entrar"
    override val noAccountYet = "Ainda não tem conta? "
    override val registerLink = "Cadastre-se"
    override val marketingOptOut =
        "Se você não deseja receber comunicações de marketing sobre nossos produtos e serviços, marque esta caixa."
    override val userNotFoundTitle = "Conta não encontrada!"
    override val userNotFoundMsg = "Não existe uma conta com esse usuário. Verifique ou cadastre-se."
    override val invalidEmailFormat = "O formato do e-mail não é válido"
    override val wrongPasswordMsg = "A senha está incorreta"
    override val loginFailedMsg = "Não foi possível entrar. Tente novamente."
    override val noAccountWithEmailMsg = "Não existe uma conta com esse e-mail"

    // =================================================================
    // ERROS DE CAMPO
    // =================================================================

    override val fieldRequired = "Este campo é obrigatório"
    override val minChars3 = "Mínimo de 3 caracteres"
    override val minChars6 = "Mínimo de 6 caracteres"
    override val validEmailPrompt = "Digite um e-mail válido"
    override val passwordsMismatch = "As senhas não coincidem"
    override val usernameTaken = "Esse nome de usuário já está em uso"
    override val emailTaken = "Esse e-mail já está cadastrado"
    override val selectBirthdate = "Selecione sua data de nascimento"
    override val selectUniversity = "Selecione sua universidade"

    override fun minInterestsWarning(max: Int) = "Você pode escolher até $max interesses."

    // =================================================================
    // REGISTRO
    // =================================================================

    override fun stepOfTotal(step: Int, total: Int) = "Etapa $step de $total"
    override val stepAccountData = "Dados da conta"
    override val stepPersonalInfo = "Informações pessoais"
    override val stepPurpose = "Propósito"
    override val stepInterests = "Interesses"
    override val stepPhotoBio = "Foto e bio"

    override val createYourAccountTitle = "Crie sua conta"
    override val startWithLoginData = "Comece com seus dados de acesso."
    override val usernameAtHint = "Usuário (@username)"
    override val emailLabel = "E-mail"
    override val confirmPasswordLabel = "Confirmar Senha"

    override val personalInfoTitle = "Informações pessoais"
    override val tellUsAboutYou = "Conte um pouco sobre você."
    override val firstNameLabel = "Nome *"
    override val lastNameLabel = "Sobrenome"
    override val birthDateLabel = "Data de nascimento *"
    override val dayLabel = "Dia"
    override val monthLabel = "Mês"
    override val yearLabel = "Ano"
    override val whereStudyNow = "Onde você estuda atualmente?"
    override val universityLabel = "Universidade *"
    override val genderIdentityLabel = "Identidade de gênero"
    override val chooseYourGender = "Escolha seu gênero"

    override val whatBringsGaply = "O que te trouxe ao Gaply?"
    override val chooseGoalHint = "Escolha o objetivo que melhor te descreve. Você poderá mudá-lo depois."

    override val selectSomeInterests = "Selecione alguns dos seus interesses"
    override val addInterests = "Adicionar interesses"
    override fun maxInterestsInTotal(max: Int, selected: Int, total: Int) =
        "Máximo de $max interesses no total ($selected/$total)."

    override val uploadPhoto = "Enviar foto"
    override val takePhoto = "Tirar Foto"
    override fun helloName(name: String) = "Olá, $name"
    override val giveShortBio = "Dê uma breve descrição sobre você:"
    override val bioLabel = "Bio"
    override val saveProfile = "Salvar perfil"

    override val profileSavedTitle = "Seu perfil foi salvo!"
    override val profileSavedMsg =
        "Seu perfil foi salvo! As informações do seu perfil foram salvas com sucesso."
    override val galleryInfoTitle = "Galeria de fotos"
    override val galleryInfoMsg = "Na versão final, este botão abrirá sua galeria de fotos."
    override val cameraInfoTitle = "Câmera"
    override val cameraInfoMsg = "Na versão final, este botão abrirá a câmera do seu celular."
    override val saveErrorTitle = "Erro ao salvar!"
    override val saveErrorMsg = "Erro ao salvar o perfil! Desculpe, tente novamente mais tarde."

    override val months = listOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
    )

    override val genders = listOf("Feminino", "Masculino", "Não binário", "Prefiro não dizer")

    override val goals = listOf(
        "\uD83D\uDCDA Preciso estudar",
        "\uD83C\uDFC0 O que tem para fazer",
        "\uD83D\uDE09 Estou com tempo livre",
        "\uD83E\uDD7A Ainda não tenho certeza",
    )

    override val universities = listOf(
        "Universidad Jorge Tadeo Lozano",
        "Universidad Central",
        "Universidad de los Andes",
        "Outra",
    )

    override fun interestLabel(key: String): String = when (key) {
        "Deportes" -> "Esportes"
        "Música" -> "Música"
        "Videojuegos" -> "Videogames"
        "Artes" -> "Artes"
        "Juegos de mesa" -> "Jogos de tabuleiro"
        "Películas" -> "Filmes"
        "Estudio" -> "Estudos"
        "Eventos" -> "Eventos"
        "Baile" -> "Dança"
        "Fotografía" -> "Fotografia"
        "Viajes" -> "Viagens"
        "Cocina" -> "Culinária"
        "Programación" -> "Programação"
        "Moda" -> "Moda"
        "Ciencia" -> "Ciência"
        else -> key
    }

    // =================================================================
    // HOME
    // =================================================================

    override val tabDiscover = "Descobrir"
    override val tabMatches = "Matches"
    override val tabChats = "Conversas"
    override val tabProfile = "Perfil"

    override val welcomeUser = "Bem-vindo"
    override fun youHaveFreeSlot(descripcion: String) = "Você tem $descripcion"
    override val addClassesToSeeSlots =
        "Adicione suas aulas em Meu horário para ver seus espaços livres"
    override val noFreeSlotsToday = "Hoje você não tem espaços livres"
    override val suggestionsToday = "Sugestões para hoje"
    override val createActivitySection = "Criar a atividade"
    override val addYourOwnActivity = "Adicionar sua própria atividade"
    override val inviteOthers = "Convide outros para entrar no seu espaço"
    override val culturalAgenda = "Agenda cultural"
    override val visitWebsite = "ir para o site"
    override val matchesEmptyMsg = "Seus matches aparecerão aqui."
    override val chatsEmptyMsg = "Suas conversas aparecerão aqui."
    override val eventSiteInfoMsg = "Na versão final, este link abrirá o site do evento."

    override val placeLibrary = "Biblioteca"
    override val placeCourt = "Quadra M7A"
    override val placeMultiRoom = "Salão Multifuncional"
    override val placeGreenZone = "Área Verde"
    override val placeAuditorium = "Auditório"

    override val profilePhotoCd = "Foto de perfil"
    override val notificationsCd = "Notificações"
    override val addCd = "Adicionar"

    // =================================================================
    // PERFIL
    // =================================================================

    override val profileFallbackUsername = "Usuário"
    override val completeYourProfile = "Complete seu perfil"
    override val descriptionTitle = "Descrição"
    override val noBioYet = "Você ainda não adicionou uma descrição."
    override val interestsTitle = "Interesses"
    override val chooseInterestsHint = "Escolha seus interesses pelo botão Editar."
    override val scheduleSectionTitle = "Horário"
    override val addScheduleAction = "Adicionar horário"
    override val addClassesHint = "Adicione suas aulas para descobrir seus espaços livres."
    override val upsTitle = "Ops!"
    override val settingsCd = "Configurações"
    override val changeProfilePhotoCd = "Mudar foto de perfil"
    override val accountUnknownError = "Não conseguimos identificar sua conta. Encerre a sessão e entre novamente."
    override val photoChangeError = "Não conseguimos mudar sua foto. Tente novamente mais tarde."

    // =================================================================
    // EDITAR PERFIL
    // =================================================================

    override val editProfileTitle = "Editar Perfil"
    override val photoAndBasicsTitle = "Foto de Perfil e Básicos"
    override val photoBasicsSubtitle = "Esta é a foto que outros usuários verão no swipe."
    override val profilePhotoPreviewCd = "Pré-visualização da foto de perfil"
    override val currentPhotoCd = "Foto de perfil atual"
    override val gallery = "Galeria"
    override val camera = "Câmera"
    override val namesLabel = "Nomes"
    override val surnamesLabel = "Sobrenomes"
    override val personalDetailsTitle = "Detalhes Pessoais"
    override val selectAnOption = "Selecione uma opção"
    override val genderLabel = "Gênero"
    override val interestsAndBioTitle = "Interesses e bio"
    override val interestsBioSubtitle = "Selecione até 3 interesses (eles aparecem no seu perfil)"
    override val descriptionFieldLabel = "Descrição"
    override val bioPlaceholder = "Conte um pouco sobre você..."
    override val saveChanges = "Salvar alterações"
    override val profilePreviewTitle = "Pré-visualização do perfil"
    override val noUniversity = "Sem universidade"
    override val noInterestsSelected = "Você ainda não selecionou interesses."
    override val changesSavedTitle = "Alterações salvas!"
    override val changesSavedMsg = "Seu perfil foi atualizado com sucesso."
    override val editSaveErrorMsg = "Não conseguimos salvar as alterações. Tente novamente mais tarde."

    // =================================================================
    // HORÁRIO
    // =================================================================

    override val mySchedule = "Meu horário"
    override val prevDay = "Dia anterior"
    override val nextDay = "Próximo dia"
    override val addScheduleCd = "Adicionar horário"
    override fun notePrefix(nota: String) = "Nota: $nota"
    override val importFromGallery = "Importar da galeria"
    override val importGalleryMsg =
        "Escolha uma foto do seu horário. Na versão final, o Gaply lerá as aulas automaticamente da imagem."
    override val viewActivitiesForSlot = "Ver atividades para este espaço"
    override val addScheduleTitle = "Adicionar Horário"
    override val tapPlusToAdd = "Toque em \"+\" para adicionar o horário"
    override val dayFreeUseArrows = "Este dia está livre. Use ‹ › para ver os outros dias."
    override val tapToRetry = "Toque para tentar novamente"

    override val noConnectionSchedule = "Sem conexão. Verifique sua internet e tente de novo."
    override val firestoreReadSchedule = "O Firestore não permitiu ler seu horário (sessão ou regras)."
    override val loadScheduleFailed = "Não foi possível carregar o horário. Tente de novo."
    override val loadGenericFailed = "Não foi possível carregar. Tente de novo."
    override val saveGenericFailed = "Não foi possível salvar. Tente de novo."
    override val deleteGenericFailed = "Não foi possível excluir. Tente de novo."
    override val firestoreWriteRules = "O Firestore não permitiu salvar (sessão ou regras)."
    override val firestoreDeleteRules = "O Firestore não permitiu excluir (sessão ou regras)."

    // =================================================================
    // EDITAR HORÁRIO
    // =================================================================

    override val deleteClassTitle = "Excluir aula"
    override fun deleteClassMessage(nombre: String) =
        "Excluir \"$nombre\" do horário? Esta ação não pode ser desfeita."
    override val thisClass = "esta aula"
    override val scheduleFormTitle = "Horário"
    override val closeWithoutSaving = "Fechar sem salvar"
    override val titleLabel = "Título"
    override val titlePlaceholder = "Título da disciplina"
    override val startLabel = "Início"
    override val endLabel = "Fim"
    override val timeSectionLabel = "Hora"
    override val startsLabel = "Começa"
    override val endsLabel = "Termina"
    override val addNotesTitle = "Adicionar Notas"
    override val notesPlaceholder = "Escreva uma nota (opcional)."
    override val colorLabel = "Cor"
    override val selectDay = "Selecione um dia"
    override val selectTime = "Selecione um horário"
    override val selectColor = "Selecione uma cor"
    override val classNoLongerExists = "Esta aula não existe mais ou foi excluída."
    override val writeTitleError = "Escreva o título da disciplina."
    override val endMustBeAfterStart = "O horário final deve ser posterior ao inicial."

    // =================================================================
    // ESPAÇOS LIVRES
    // =================================================================

    override val myFreeSlots = "Meus espaços livres"
    override val tellUsLikesFreeTime =
        "Diga o que você gosta e em que horário está livre. As sugestões se organizam sozinhas."
    override val yourInterests = "Seus interesses"
    override fun selectedCount(selected: Int, max: Int) = "$selected de $max selecionados"
    override val yourFreeSlotTitle = "Seu espaço livre"
    override val fromLabel = "De"
    override val toLabel = "Até"
    override val startShort = "Início"
    override val endShort = "Fim"
    override val suggestionsTitle = "Sugestões"
    override val fixTimeToSeeSuggestions = "Corrija o horário para ver as sugestões."
    override val noneFitsSlot = "Nenhuma atividade cabe nesse espaço. Tente outro dia ou horário."
    override fun doesNotFit(count: Int) = "Não cabe ($count)"
    override val utilityExplanation =
        "Utilidade = 65% interesses + 35% encaixe no horário. O que não cabe no seu espaço é descartado com o motivo."
    override fun yourSlotIs(rango: String) = "Seu espaço: $rango"

    override fun activityName(id: String, fallback: String): String = when (id) {
        "futbol" -> "Jogo de futebol"
        "repaso" -> "Café e revisão"
        "mesa" -> "Torneio de jogos de tabuleiro"
        "musica" -> "Sessão de música"
        "baile" -> "Aula de dança"
        "artes" -> "Oficina de artes"
        "cine" -> "Cinema no campus"
        "evento" -> "Evento cultural"
        else -> fallback
    }

    override fun activityDescription(id: String, fallback: String): String = when (id) {
        "futbol" -> "Quadra do campus."
        "repaso" -> "Revisão em grupo na cafeteria."
        "mesa" -> "Sala de atividades."
        "musica" -> "Auditório."
        "baile" -> "Piso multiuso."
        "artes" -> "Sala de artes."
        "cine" -> "Auditório."
        "evento" -> "Praça principal."
        else -> fallback
    }

    override fun reasonInterestsMatch(compartidos: String) =
        "Combina com seus interesses: $compartidos"
    override val reasonNoInterestsMatch = "Não combina com seus interesses atuais"
    override val reasonNoInterestsRegistered = "Você ainda não tem interesses registrados"
    override fun reasonFitsCompletely(hueco: String) = "Cabe inteiro no seu espaço de $hueco"
    override fun reasonPartiallyUses(minutos: Int, hueco: String) =
        "Usa $minutos min do seu espaço de $hueco"
    override fun reasonNoFreeSlot(franja: String) = "Não cabe em nenhum espaço livre ($franja)"

    // =================================================================
    // CONFIGURAÇÕES
    // =================================================================

    override val settingsTitle = "Configurações"
    override val accountSecurity = "Conta e Segurança"
    override val changePasswords = "Mudar senha"
    override val updateYourAccess = "Atualize seu acesso"
    override val deactivateDeleteAccount = "Desativar e Excluir Conta"
    override val deactivateDeleteAccountSubtitle = "Pausa temporária ou exclusão permanente"
    override val twoFactor = "Autenticação de dois fatores"
    override val preferences = "Preferências"
    override val theme = "Tema"
    override val enableDarkMode = "Ative o modo escuro"
    override val themeSoonMessage = "Estará na versão final, estamos trabalhando nisso."
    override val notifications = "Notificações"
    override val configureAlerts = "Configure seus alertas"
    override val language = "Idioma"
    override val saveLanguage = "Salvar idioma"
    override val aboutSection = "Sobre"
    override val rateTheApp = "Avaliar o app"
    override val aboutGaply = "Sobre o Gaply"
    override val copyrightText = "© Gaply 2026 - Version 1.0"
    override val logout = "Sair"

    // =================================================================
    // MUDAR SENHA
    // =================================================================

    override val changePasswordTitle = "Mudar senha"
    override val currentPasswordLabel = "Senha atual"
    override val newPasswordLabel = "Nova Senha"
    override val changePasswordButton = "Salvar senha"
    override val passwordUpdatedTitle = "Senha atualizada!"
    override val passwordUpdatedMsg = "Sua senha foi alterada com sucesso."
    override val changePasswordErrorTitle = "Não foi possível mudar"
    override val changePasswordReloginMsg =
        "Por segurança, saia e entre novamente para mudar sua senha"
    override val changePasswordErrorMsg = "Não foi possível mudar a senha. Tente de novo."

    // =================================================================
    // CONTA: DESATIVAR / EXCLUIR
    // =================================================================

    override val deleteOrDeactivateTitle = "Excluir ou desativar?"
    override val deleteOrDeactivateDesc =
        "Se você quiser sair do Gaply temporariamente, apenas desative sua conta. " +
            "Se escolher excluí-la, não poderá recuperá-la depois de 30 dias."
    override val deactivateOptionTitle = "Desativar conta"
    override val deactivateOptionDesc = "Você pode reativá-la quando quiser, com os mesmos dados de acesso."
    override val deleteOptionTitle = "Excluir conta"
    override val deleteOptionDesc = "Esta ação apaga sua conta e todos os seus dados de forma permanente."
    override val confirmDeactivateTitle = "Desativar esta conta?"
    override val confirmDeactivateDesc =
        "Sua conta ficará inativa até que você entre novamente com os mesmos dados de acesso."
    override val confirmDeactivateButton = "Desativar"
    override val confirmDeactivateListTitle = "Se você desativar a conta:"
    override val confirmDeactivateBulletPrivacy = "Ninguém poderá ver sua conta nem seu conteúdo."
    override val confirmDeactivateBulletOutsideData =
        "Os dados que não ficam armazenados na sua conta (como as mensagens " +
            "diretas) ainda poderão ser visíveis para outros usuários."
    override val confirmDeactivateBulletDataKept =
        "O Gaply manterá seus dados para que você possa recuperá-los quando " +
            "reativar sua conta."
    override val confirmDeactivateBulletRecovery =
        "Você poderá reativar sua conta e recuperar todo o conteúdo a " +
            "qualquer momento usando os mesmos dados de acesso."
    override val deactivateErrorTitle = "Não foi possível desativar"
    override val deactivateErrorMsg = "Tente novamente em alguns minutos."
    override val deactivateReloginMsg =
        "Por segurança, saia e entre novamente para desativar sua conta"
    override val deleteAccountTitle = "Excluir conta"
    override val deleteAccountDesc =
        "Esta ação é permanente: sua conta, perfil, horário e dados serão excluídos."
    override val deleteAccountButton = "Excluir definitivamente"
    override val deleteAccountErrorMsg = "Não foi possível excluir a conta. Tente de novo."
    override val deleteAccountConfirmTitle = "Excluir esta conta?"
    override val deleteAccountErrorTitle = "Não foi possível excluir"
    override val deleteAccountReloginMsg =
        "Por segurança, saia e entre novamente para excluir sua conta"
    override val deleteWarningPermanent = "Excluir sua conta é permanente."
    override val deleteWarningProfileData = "Todos os dados do seu perfil serão apagados."
    override val deleteWarningActivity = "Seu histórico de atividades e o conteúdo salvo serão perdidos."
    override val deleteWarningIrreversible = "Você não pode desfazer esta ação."

    // =================================================================
    // REDEFINIR SENHA
    // =================================================================

    override val resetPasswordHeader = "Recuperar senha"
    override val resetPasswordTitle = "Redefinir senha"
    override val resetPasswordSubtitle = "Enviaremos um link para você criar uma nova senha."
    override val sendResetLink = "Enviar link"
    override val resetLinkSentTitle = "Link enviado!"
    override val resetLinkSentMsg = "Verifique seu e-mail para redefinir sua senha."
    override val invalidEmailError = "Digite um e-mail válido"
    override val resetPasswordErrorTitle = "Não foi possível enviar o link"
    override val resetPasswordErrorMsg = "Verifique o e-mail e tente de novo."

    // =================================================================
    // AVALIAR / SOBRE
    // =================================================================

    override val rateAppTitle = "Avalie este aplicativo"
    override val rateAppSubtitle = "Sua opinião nos ajuda a melhorar"
    override val thanksTitle = "Obrigado pelo seu feedback!"
    override val thanksMsg = "Sua avaliação nos ajuda a melhorar o Gaply."
    override fun thanksRatingMsg(stars: Int) =
        "Ficamos felizes que você tenha avaliado o Gaply com $stars ${if (stars == 1) "estrela" else "estrelas"}."
    override val starRatingCd = "Avaliação com estrelas"
    override fun starLabel(star: Int) = "Estrela $star"
    override val rateCommentLabel = "Motivos (opcional):"
    override val rateCommentPlaceholder = "Conte o que você gostou ou o que\npodemos melhorar..."
    override val rateSendButton = "Enviar"
    override val aboutLogoCd = "Logo do Gaply"
    override val logoCd = "Logo do Gaply"
    override val welcomeIllustrationCd = "Ilustração principal"
    override val aboutBody =
        "O Gaply nasceu com o propósito de remover as barreiras de comunicação e ajudar " +
            "os estudantes a aproveitar melhor seu tempo entre as aulas."

    // =================================================================
    // MÓDULO ACTIVIDADES
    // =================================================================

    override val activitiesForYourSlot = "Ver atividades para o seu intervalo"
    override val myActivitiesCardTitle = "Suas atividades"
    override val myActivitiesCardSubtitle = "Edite ou cancele as que você já publicou"
    override val activitiesListTitle = "Atividades para o seu intervalo"
    override val activitiesDetailTitle = "Atividades"
    override val huecoBannerDemo = "Você tem 2 horas livres 12:00 - 2:00 p.m."
    override val accordingToYourInterests = "Com base nos seus interesses"
    override val activitiesEmptyListMsg =
        "Ainda não há atividades publicadas para este intervalo. Seja o primeiro a publicar uma!"
    override val likeCd = "Curtir"
    override fun myActivitiesEmptyMsg(createWith: String) =
        "Você ainda não publicou atividades. Crie uma com \"$createWith\" e ela aparecerá aqui."
    override val cancelActivityTitle = "Cancelar atividade"
    override val cancelActivityConfirmMsg = "Tem certeza de que deseja cancelar esta atividade?"
    override val confirmCancelActivity = "Sim, cancelar atividade"
    override val activityUnavailableMsg = "Esta atividade não está mais disponível."
    override val organizerLabel = "Organizador"
    override fun attendeesCount(count: Int) = "Participantes: $count"
    override val joinActivity = "Participar"
    override val alreadyJoined = "Você já está dentro"
    override val comingSoonTitle = "Em breve!"
    override val comingSoonMsg = "Estamos trabalhando nisso e será adicionado em breve"
    override val eventChat = "Chat do evento"
    override val locationLabel = "Localização"
    override val howToGetThere = "Como chegar lá"
    override val beFirstToJoin = "Seja o primeiro a participar"
    override val manageYourActivity = "Gerencie sua atividade"
    override val newActivityTitle = "Nova atividade"
    override val editActivityTitle = "Editar atividade"
    override val eventTitleLabel = "Título do evento"
    override val categoryLabel = "Categoria"
    override val activityPhotoLabel = "Foto do local da atividade"
    override val locationPlaceLabel = "Localização / Lugar"
    override val locationPlacePlaceholder = "ex. Quadra M7A, Sala 204, Biblioteca…"
    override val durationLabel = "Duração estimada"
    override val dateLabel = "Data"
    override val descriptionOptionalLabel = "Descrição (opcional)"
    override val activityDescriptionPlaceholder = "Adicione mais detalhes sobre a sua atividade..."
    override val publishActivity = "Publicar atividade"
    override val selectTimeTitle = "Selecione um horário"
    override val expandCategoriesCd = "Expandir categorias"
    override val placePhotoCd = "Foto do local"
    override val removePhotoCd = "Remover foto"
    override val tapToAddPlacePhoto = "Toque para adicionar uma foto do local"
    override val defaultColorPhotoHint = "Se você não escolher nenhuma, é usada uma cor verde padrão"
    override val duration30 = "30 min"
    override val duration1h = "1 hora"
    override val duration2h = "2 horas"
}
