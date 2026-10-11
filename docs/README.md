# Afinador Android

Protótipo pessoal de afinador cromático para guitarra e violão, feito em Kotlin e Jetpack Compose.

## O que já funciona

- Captação do microfone ao abrir a tela, após conceder a permissão; a captação para quando o app sai de primeiro plano.
- Estimativa inicial da nota fundamental com YIN, para uma nota por vez.
- Nota em A–G com oitava menor e `#` acima da letra, frequência medida, frequência-alvo e desvio em cents. Acima de ±2 cents, nota e medidor ficam laranja.
- A última leitura válida fica visível, atenuada, por até cinco segundos após o som parar ou cair abaixo do limite.
- Referência A4 de 400–480 Hz escolhida por rolagem vertical com inércia curta ou digitação direta em Ajustes, com até três presets salvos (440 Hz incluso e padrão).
- Sensibilidade automática ou limite manual em dBFS (-80 a -20), com switch segmentado Auto/Manual, slider, valor numérico e marcador do nível captado. O nível exibido é um pico curto do RMS em dBFS, suavizado para leitura.
- Português, inglês e francês selecionáveis em Ajustes. Ajustes salvos no próprio aparelho; nenhum áudio é salvo.
- Mensagens próprias para permissão negada e falha do microfone, com ação de recuperação; anúncios seletivos de nota para TalkBack.

## Abrir no Android Studio

1. Abra o Android Studio e escolha **Open**.
2. Selecione a pasta `code/android` deste repositório.
3. Aguarde o **Gradle Sync**. Use o JDK incluído no Android Studio se ele solicitar um JDK para o Gradle.
4. Escolha um emulador ou um aparelho Android conectado e clique em **Run**.
5. No app, conceda a permissão do microfone quando solicitado. O afinador começa a escutar ao abrir.

O APK de teste atual fica localmente em `outputs/apk/afinador-debug-v0.4-single-toggle.apk` e não é versionado. A tela principal segue o [layout C aprovado](../context/design/layout-c-approved.png): leitura e escala sem cartão, sensibilidade após uma divisória e controles ancorados acima da navegação. A leitura e os controles mantêm posições estáveis ao entrar ou sair uma nota; o switch Auto/Manual segue a [referência atual](../context/design/auto-manual-single-toggle-approved.png). Tocar em qualquer ponto da cápsula alterna uma vez entre Auto e Manual. Nota, escala, sensibilidade, leitura retida e configurações preservam as funções da V3. O Gradle também gera `code/android/app/build/outputs/apk/debug/app-debug.apk`. É uma compilação de desenvolvimento, não uma versão de publicação.

## Estrutura

- `code/android/`: projeto Android completo (Gradle, app, fontes e testes).
- `docs/`: documentação Markdown, incluindo [backlog](BACKLOG.md), [produto](PRODUCT.md) e [decisões de interface](DESIGN.md).
- `context/`: arquivos de referência enviados pelo usuário, incluindo [a tela de referência atual](../context/main-screen-reference-v3.png) e o [controle Auto/Manual](../context/segmented-switch-reference-v3.png).
- `outputs/apk/`: APKs de teste ou entrega.
- `outputs/design/`: imagens de referência; inclui [o primeiro mock](../outputs/design/tuner-reference.png).
- `outputs/previews/`: capturas de tela de revisão do emulador ou aparelho; a revisão do layout C está em [`outputs/previews/layout-c/`](../outputs/previews/layout-c/), a comparação de estados em [`outputs/previews/layout-c-stable/`](../outputs/previews/layout-c-stable/) e o switch atualizado em [`outputs/previews/auto-manual-toggle/`](../outputs/previews/auto-manual-toggle/).

## Limite atual

Os testes automatizados usam áudio sintético e o emulador apenas confirmou abertura, permissões e estado de escuta. A precisão real, o atraso e a sensibilidade ainda precisam ser calibrados no Samsung S24 Ultra com guitarra/violão. O controle em dBFS filtra o sinal digital; ele não altera o ganho físico do microfone. Os anúncios do TalkBack foram implementados, mas ainda não foram validados com um leitor de tela real.
