# InstaDown 📥 – Instagram Video Downloader para Android

**InstaDown** é um aplicativo Android nativo, moderno e de alta performance desenvolvido em **Kotlin** e **Jetpack Compose**. Ele permite que os usuários façam o download de vídeos e Reels do Instagram através da [API FastSaver](https://api.fastsaver.io/docs), salvando-os diretamente no armazenamento público do dispositivo e permitindo a reprodução offline por meio de uma galeria integrada com o Jetpack Media3 ExoPlayer.

O aplicativo se destaca por uma experiência visual premium baseada no conceito **Dark Glassmorphism** (efeito translúcido e desfocado com tons vibrantes de neon).

---

## ✨ Principais Funcionalidades

- **📥 Downloads Seguros e Estáveis**: Integração direta com a API FastSaver para extração de mídias públicas do Instagram.
- **⚙️ Fila em Segundo Plano (WorkManager)**: Downloads persistentes e resilientes gerenciados pelo `WorkManager` do Android, que continuam mesmo se o aplicativo for fechado.
- **🔔 Notificações Ativas**: Notificações nativas exibindo a porcentagem de progresso em tempo real e avisando sobre a conclusão dos downloads.
- **🗂️ Galeria Offline Integrada**: Galeria nativa com grade de miniaturas para gerenciar todos os vídeos baixados.
- **🎬 Player de Vídeo Avançado**: Reprodutor integrado usando **Jetpack Media3 ExoPlayer** para assistir aos vídeos offline de maneira suave.
- **📋 Auto-Detecção do Clipboard**: O aplicativo detecta links válidos do Instagram na Área de Transferência assim que é aberto, agilizando o processo.
- **🎨 Design System Premium (Glassmorphism)**: Interface moderna com fundos translúcidos de vidro, bordas brilhantes, neon rosa/roxo e um tema escuro imersivo.

---

## 🎨 Visual e Identidade de Design (Glassmorphism)

O aplicativo foi desenhado utilizando componentes customizados para simular vidro fosco translúcido (*frosted glass*):
- **Cor de Fundo**: Escuro profundo (`#0F0C20`).
- **Superfícies de Vidro**: Tons roxos semitransparentes (`#1F1A3A` com opacidade controlada) com efeito de desfoque (*blur*).
- **Cores de Destaque**: Rosa Neon (`#FF007A`) e Roxo Vibrante (`#8A00FF`).

---

## 🛠️ Arquitetura e Tecnologias Utilizadas

- **Kotlin & Jetpack Compose**: Criação da UI de forma totalmente declarativa e performática.
- **Room Database**: Persistência local do histórico de downloads (links, títulos, miniaturas, caminhos locais e datas).
- **Preferences DataStore**: Armazenamento seguro de preferências de usuário, como a chave de API (`X-Api-Key`) e a pasta de downloads personalizada.
- **Retrofit & OkHttp**: Comunicação assíncrona robusta com a API FastSaver.
- **Coil**: Carregamento assíncrono inteligente de imagens e thumbnails.
- **MediaStore API**: Salvamento correto de arquivos na pasta pública `/Downloads/InstaDown/` respeitando as novas diretrizes do Android 10+ (sem requisições excessivas de permissão).
- **GitHub Actions (CI/CD)**: Pipeline pronto para construir o app e anexar os arquivos APK automaticamente aos Releases.

---

## 📂 Estrutura de Diretórios do Projeto

```
insta-down-app/
├── .github/workflows/
│   └── release.yml             # Workflow de CI/CD para compilação e release automático
├── app/
│   ├── build.gradle.kts        # Configurações de dependências e compilação do app
│   └── src/main/
│       ├── AndroidManifest.xml # Manifesto do aplicativo (permissões, activities e workers)
│       ├── java/com/instadown/app/
│       │   ├── MainActivity.kt # Ponto de entrada (Navegação Compose e permissão de Notificação)
│       │   ├── InstaDownApp.kt # Classe de Application e injeção de dependência manual (Singletons)
│       │   ├── data/           # Camada de Dados (Modelos, Retrofit API, Room DB e Repositórios)
│       │   ├── service/        # DownloadWorker (Trabalho assíncrono via WorkManager)
│       │   └── ui/             # Camada de UI (Telas, Componentes de Vidro e Temas Neon)
│       └── res/                # Recursos XML (strings em português e temas de barra de status)
├── settings.gradle.kts         # Declaração do projeto e Version Catalog
└── settings.gradle.kts         # Repositórios e carregador de dependências
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- **JDK 21** configurada.
- **Android SDK** instalado (compilação direcionada para API 34).
- Uma chave de acesso para a API FastSaver (obtenha na [documentação da FastSaver](https://api.fastsaver.io/docs)).

### Passo a Passo

1. **Clonar e Importar**:
   Abra o **Android Studio** e selecione *Open* -> Escolha a pasta `/workspaces/insta-down-app`.

2. **Sincronizar o Gradle**:
   Deixe o Android Studio sincronizar os arquivos de build. O gerenciador de dependências foi configurado usando a mais recente estrutura de catálogo de versões (`libs.versions.toml`).

3. **Construir o App**:
   Selecione o botão **Run** no Android Studio ou compile diretamente via terminal utilizando o Gradle Wrapper:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Configurar a API Key**:
   Ao abrir o aplicativo pela primeira vez, acesse a aba **Configurações** (ícone de engrenagem) e insira sua chave da API. O app validará e salvará o token de forma segura no DataStore.

5. **Utilizar**:
   Copie qualquer link de vídeo público do Instagram, abra a aba **Início** (o app já sugerirá colar o link) e clique em **Baixar**. Monitore na barra de notificações ou assista posteriormente offline na aba **Galeria**.

---

## 📦 Entrega e Deploy Automatizado (CI/CD)

Este projeto possui integração contínua (CI/CD) configurada com o **GitHub Actions**.

### Como gerar um novo APK automaticamente:
1. Faça a tag de nova versão no Git e envie ao servidor:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. O workflow do GitHub Actions será disparado, realizando as seguintes etapas:
   - Configuração do JDK 21.
   - Compilação dos APKs do projeto (`assembleDebug` e `assembleRelease`).
   - Criação automática de um **GitHub Release**.
   - Upload do arquivo `InstaDown-v1.0.0-debug.apk` pronto para download.

Você poderá encontrar e baixar as versões publicadas diretamente na seção **Releases** do seu repositório no GitHub.

---

## ⚖️ Licença

Este projeto está licenciado sob a licença MIT. Consulte o arquivo `LICENSE` para obter mais informações.
