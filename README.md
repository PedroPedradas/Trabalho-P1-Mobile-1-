# Minhas Tarefas

Projeto individual de Programação Mobile I. O aplicativo cadastra tarefas com título e descrição opcional. O fluxo é: lista → cadastro → salvar no aparelho → lista → detalhes. As tarefas permanecem após fechar e abrir o aplicativo.

## Executar no Android Studio

1. Extraia o ZIP em uma nova pasta. No Android Studio, use **File → Open** e selecione **MinhasTarefas**, a pasta que contém `settings.gradle.kts`.
2. Confirme **Trust Project**. Aguarde a sincronização do Gradle. É necessária internet para baixar as dependências na primeira abertura.
3. Se o Studio oferecer **Use JVM 21**, aceite. JDK 17 ou 21 é compatível com este projeto. Configure em **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.
4. Se solicitado, instale o SDK Android API 36 em **Tools → SDK Manager**. O emulador pode usar API 26 ou superior.
5. Selecione **app** e um emulador no topo. Clique no triângulo verde **Run**.

Versões fixadas: Gradle 8.13, Android Gradle Plugin 8.13.2, Kotlin 2.2.21, Compose BOM 2025.12.00, Navigation 3 1.0.1 e Activity Compose 1.12.0. compileSdk 36, targetSdk 35 e minSdk 26. O Gradle Wrapper oficial está incluído.

## Demonstrar as etapas

**Parcial XML:** toque em **Ver demonstração XML** na tela principal. Essa lista tem três tarefas simuladas. Toque em uma para abrir a segunda Activity por Intent explícita. A tarefa sem descrição exibe “Sem descrição”. Toque em Voltar. As duas telas usam ViewBinding, TextView, LinearLayout, RecyclerView e ScrollView; o item da lista é um XML reutilizado pelo adapter.

**Etapa Compose:** toque em **Nova tarefa**. Tente salvar com título vazio: o formulário apresenta erro. Preencha o título, deixe a descrição vazia ou preencha, toque em Salvar e abra a tarefa na lista. Feche e reabra para verificar que foi gravada. O cadastro pode ser repetido.

## Como entender o código

Existem apenas seis arquivos Kotlin, todos no mesmo pacote, sem camadas e subpastas adicionais:

| Arquivo | Responsabilidade |
|---|---|
| `Task.kt` | Modelo com `val`: ID, título e descrição opcional. |
| `TaskRepository.kt` | Ler e salvar JSON na pasta interna do app; publicar a lista com StateFlow. |
| `TaskViewModel.kt` | Coordenar carregamento e cadastro e fornecer UiState. |
| `MainActivity.kt` | Hospedar as três telas Compose e a navegação. |
| `XmlListActivity.kt` | Mostrar mocks com RecyclerView e abrir detalhes. |
| `XmlDetailActivity.kt` | Receber extras da Intent, mostrar dados e voltar. |

O Repository faz operações de arquivo em `Dispatchers.IO`. O ViewModel usa `viewModelScope`: as coroutines são canceladas quando ele é encerrado, e `CancellationException` é repassada. Um Mutex impede gravações simultâneas. A gravação usa arquivo temporário e substitui o anterior só depois que o texto foi escrito.

O fluxo de dados é unidirecional: clique → ViewModel → Repository → StateFlow → tela. As telas observam com `collectAsStateWithLifecycle`. A navegação usa rotas serializáveis, `entryProvider`, `NavDisplay` e `rememberNavBackStack`; detalhes recebem o ID da tarefa. Os campos do formulário usam `rememberSaveable`, preservando o texto durante rotação e recriação; dados salvos ficam no arquivo.

## Estados e acessibilidade

Loading mostra indicador e texto; Empty mostra instrução para cadastrar; Content mostra LazyColumn; Error mostra uma mensagem e botão de tentar novamente. Falha de gravação mostra feedback no formulário, que permite tentar de novo. Não são introduzidos atrasos falsos: no aparelho, o carregamento de um arquivo pequeno pode ser rápido.

O app usa cores do Material 3, textos em tamanho escalável, botões com área mínima de 48dp, rótulos nos campos e formulário/detalhes com rolagem. Não há ícones informativos sem texto; os controles são identificados por seus rótulos. Teste também fonte maior e rotação.

## Bibliotecas usadas

- Compose UI/Foundation/Material 3: componentes, layouts e lista LazyColumn.
- Activity Compose: coloca Compose dentro da Activity Android.
- Lifecycle ViewModel e Runtime Compose: mantém estado e observa StateFlow conforme o ciclo de vida.
- Navigation 3 Runtime/UI: rotas, entradas e pilha de navegação.
- Kotlinx Serialization Core e plugin: serialização das rotas para restaurar a navegação.
- RecyclerView: lista XML da parcial.
- Coroutines (fornecidas pelas dependências Jetpack): operações assíncronas, StateFlow e Mutex.
- `org.json`: JSON da plataforma Android; não é dependência externa.

Não há API, banco remoto, chave ou senha. Não é necessário arquivo `.env`.

## GitHub e entrega

Crie um repositório e envie o conteúdo desta pasta, incluindo `app`, `gradle`, scripts `gradlew`/`gradlew.bat`, arquivos Gradle e este README. O `.gitignore` exclui arquivos locais, caches e builds. Entregue ao professor o **link do repositório GitHub**, conforme o enunciado. O ZIP é para abrir o projeto, não substitui a entrega por GitHub.

## Checklist antes de entregar

- Sincronizar e executar no Android Studio.
- Salvar vazio: aparece erro de título obrigatório.
- Salvar com e sem descrição; abrir detalhes.
- Fechar e reabrir: tarefas continuam presentes.
- Girar no cadastro: texto permanece.
- Conferir fonte maior e rolagem.
- Abrir a demonstração XML e navegar entre as duas telas.

Validação realizada na preparação: conferência dos arquivos XML e da estrutura do ZIP, revisão de referências e das versões. A compilação Android e a execução em emulador ainda devem ser confirmadas no Android Studio; não foi possível executá-las no ambiente de preparação.
