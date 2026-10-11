# Interface do afinador

Referência aprovada para a composição atual da tela principal: [layout C](../context/design/layout-c-approved.png). As [referências V3](../context/main-screen-reference-v3.png) e do [controle Auto/Manual](../context/segmented-switch-reference-v3.png) permanecem como histórico de funções e estados. A implementação é nativa em Compose e segue controles Material 3.

## Estrutura

- A tela principal concentra nota, frequência, escala de cents e sensibilidade. A oitava é menor que a letra; o `#` fica acima da letra.
- O ícone no canto superior direito abre uma página de Ajustes.
- A referência A4 fica em Ajustes: rolagem vertical de 400–480 Hz com inércia curta, toque no valor para digitar, ajuste fino de 1 Hz e até três presets salvos. 440 Hz é o preset fixo e valor padrão.
- A sensibilidade fica na parte inferior da tela principal, com switch segmentado Auto/Manual. O slider mostra o nível captado como marcador sobre a escala; arrastar ou editar o número ativa o modo manual.
- Cabeçalho, leitura/escala e sensibilidade ocupam a altura segura disponível. A leitura fica visualmente centralizada, e uma linha discreta separa os controles inferiores. A tela não rola quando tudo cabe; em altura reduzida, fonte ampliada ou com o teclado aberto, o conteúdo pode rolar.
- O idioma é selecionável entre português, inglês e francês nos Ajustes.

## Linguagem visual

- Superfície escura de baixa distração, texto claro, teal para seleção e afinação, laranja saturado para desvio acima de ±2 cents na nota e no medidor.
- Escala horizontal de -50 a +50 cents com zero fixo, ponteiro na posição medida e cinco rótulos principais (-50, -25, 0, +25, +50).
- Tipografia Material com a nota em maior destaque; estados sem leitura mostram traços, sem instrução textual abaixo da nota. Não há frase de direção abaixo da escala.
- Barras de sistema escuras com ícones claros. Áreas de toque usam componentes Material de tamanho adequado.
- A área da nota cresce com fontes ampliadas. Os controles da sensibilidade e os idiomas se adaptam à largura disponível; telas estreitas permitem rolagem.
- O limiar de detecção e o nível captado têm rótulos separados. O marcador pode ficar fora das extremidades da faixa do slider; não aparece uma mensagem adicional abaixo de “Sinal captado”.
- Os botões de ajuste fino de ±1 Hz ficam agrupados abaixo da referência A4. A interface explica quando o preset atual já está salvo.
- Escala e sensibilidade aparecem diretamente sobre o fundo principal, sem cartões. Auto/Manual usa uma cápsula binária de 48dp de altura: qualquer toque alterna o modo uma vez. A metade selecionada tem fundo verde escuro e contorno ciano inteiramente visível; a largura acompanha os rótulos traduzidos e a escala de fonte.

## Validação do layout C

Em 11 de outubro de 2026, o APK debug compilou e foi conferido em dois emuladores: 1080×2400 a 420 dpi e 900×1800 a 420 dpi. No maior, toda a tela principal ficou visível sem deslocamento após um gesto de rolagem. No compacto, os controles foram alcançados por rolagem; com fonte 1,5×, nota e controles não se sobrepuseram. Ao editar o limiar com o teclado aberto, campo, unidade, rótulo e sinal captado ficaram visíveis acima do teclado. As [capturas de revisão](../outputs/previews/layout-c/) incluem estados sem nota, com B2 detectada, leitura retida, Auto e Manual. As notas e níveis dessas capturas foram fornecidos por uma atividade de demonstração presente somente no build debug; elas verificam a interface, não a precisão da detecção. Nenhum aparelho físico estava conectado durante a validação.

Na revisão seguinte, os [prints do usuário](../context/reviews/layout-c-states/) motivaram a remoção da linha “Abaixo da escala”, a reserva de uma altura fixa para a nota e o [switch segmentado](../context/design/auto-manual-segmented-approved.png). No emulador maior, alternar duas vezes um sinal simulado abaixo/acima de −75 dBFS manteve idênticas as coordenadas da nota (`y=419…751`), frequência (`y=751…842`), cents (`y=1048…1122`), sensibilidade (`y=1752…1815`) e sinal captado (`y=2189…2242`). Os toques mudaram a seleção Auto/Manual; a árvore de acessibilidade marcou a opção ativa como selecionada. As [capturas e hierarquias](../outputs/previews/layout-c-stable/) registram essa comparação. O emulador compacto com fonte 1,5× preservou a operação por rolagem.

O switch foi refinado conforme a [referência atual](../context/design/auto-manual-single-toggle-approved.png). O contorno externo é desenhado por último e o contorno da seleção fica recuado 2dp das extremidades; nenhum dos dois é cortado. A cápsula inteira forma um único controle acessível, com estado Auto/Manual e ação traduzida para alternar. No emulador 1080×2400 a 420 dpi, os toques à esquerda, no centro, à direita e no lado já selecionado produziram uma única alternância cada. O modo permaneceu salvo após reiniciar o app. As [capturas em português, inglês e francês](../outputs/previews/auto-manual-toggle/) registram Auto e Manual; no emulador compacto 900×1800 com fonte 1,5×, o controle de 48dp continuou inteiro e alcançável por rolagem.

## Logo escolhida

A versão aprovada pelo usuário é [afinador-logo-approved.png](../outputs/design/afinador-logo-approved.png). Ela mostra uma palheta vazada em teal, uma senoide com o cruzamento do zero no centro, ondas menores acompanhando a crista e o vale, e um ponto âmbar central. A imagem editada pelo usuário está preservada em [context/design/logo-reference-user-v2.png](../context/design/logo-reference-user-v2.png); o histórico de geração está em [ICON_PROMPTS.md](ICON_PROMPTS.md).

## Estados previstos

Aguardando nota estável, nota detectada, última nota atenuada por até cinco segundos, permissão negada e erro de captação. Uma nova nota válida substitui imediatamente a anterior. Para permissão ou erro, a ação de recuperação aparece abaixo da escala.

Mudanças estáveis de nota e direção de afinação usam um anúncio discreto para TalkBack, após breve atraso, para evitar fala contínua a cada bloco de áudio. Esse comportamento ainda precisa de teste com TalkBack ativo.
