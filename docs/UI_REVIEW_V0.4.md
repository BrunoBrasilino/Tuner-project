Method: dual-agent (A: /root/design_assessment · B: /root/detector_assessment)

# Avaliação da interface V0.4

**Alvo:** `code/android/app/src/main/java/com/example/afinador/ui/TunerScreen.kt` e `TunerTheme.kt`. **Evidências:** capturas do celular em [`context/reviews/v0.4/`](../context/reviews/v0.4/), código Compose e decisões em `PRODUCT.md`/`DESIGN.md`. O celular costuma ficar apoiado durante a afinação. O diagnóstico abaixo registra o estado anterior à revisão; o andamento da implementação está no fim do documento.

## Diagnóstico

A interface comunica bem que se trata de um afinador: nota e oitava dominam, a escala de cents tem zero fixo, teal indica afinação e âmbar indica desvio. A tela escura tem pouco ruído visual. O principal risco está na leitura rápida à distância e na interpretação do estado do áudio, não na identidade visual.

| # | Heurística de Nielsen | Nota /4 | Evidência principal |
|---|---|---:|---|
| 1 | Estado do sistema visível | 2 | Leitura retida usa sobretudo transparência; silêncio mostra apenas traços. |
| 2 | Correspondência com o mundo real | 3 | Nota e cents são familiares; dBFS exige contexto. |
| 3 | Controle e liberdade | 3 | Voltar, cancelar, presets e digitação direta existem. |
| 4 | Consistência | 3 | Paleta e componentes coerentes; alguns controles são personalizados. |
| 5 | Prevenção de erros | 2 | Tocar no slider/campo troca Auto por Manual imediatamente. |
| 6 | Reconhecimento | 2 | Marcador de entrada e limite são difíceis de distinguir; estado retido é implícito. |
| 7 | Flexibilidade | 2 | Presets, rolagem, ±1 Hz e digitação ajudam, mas ajustes frequentes pedem alcance melhor. |
| 8 | Minimalismo | 3 | Foco na nota; 11 rótulos da escala e sensibilidade competem em menor grau. |
| 9 | Recuperação de erros | 2 | Há botão de recuperação, mas o erro do microfone é genérico. |
| 10 | Ajuda | 1 | Há instrução da referência A4; falta explicação contextual para sensibilidade. |
| **Total** | | **23/40** | Base boa, com problemas concretos de leitura e acessibilidade. |

**Carga cognitiva:** a afinação exige ler nota, frequência, alvo, cents e direção. Os 11 números pequenos da escala e os indicadores de sensibilidade ampliam a varredura sem ajudar na decisão principal. Não há conjunto com mais de quatro escolhas simultâneas que peça reestruturação do fluxo.

**Jornada:** a nota grande dá confiança quando há sinal estável. No silêncio, os traços não dizem claramente que o microfone está escutando. Quando a última nota fica retida, a transparência sugere mudança de estado, mas não explica qual; a frequência ainda parece uma medida atual.

## Pontos fortes a preservar

1. Nota grande com oitava menor, legível em uma olhada.
2. Número de cents com sinal, ponteiro e zero fixo oferecem pistas redundantes de direção.
3. Referência A4 e idiomas ficam em Ajustes; a tarefa principal permanece focada.

## Mudanças propostas, em ordem

| Prioridade | Problema observado | Proposta pontual | Critério de aceite no aparelho |
|---|---|---|---|
| **P1** | Na captura da nota B2, nota, Hz e medidor estão esmaecidos; o código usa alpha 0,62/0,65 para leitura retida e limpa a descrição falada nesse estado (`TunerScreen.kt`, regiões da nota e do medidor). A tela sem nota mostra só traços. | Mostrar um pequeno estado **“Escutando”** no cabeçalho quando não há nota e **“Última leitura”** durante os cinco segundos de retenção, sem adicionar instrução abaixo da nota. Manter a nota retida mais contrastada; anunciar esses estados no TalkBack. | No S24 Ultra apoiado, após tocar e parar uma nota, distinguir ao vivo/retida/silêncio em até 2 s; após ~5 s, a nota some. TalkBack anuncia a transição uma vez, sem repetir a cada bloco de áudio. |
| **P1** | A escala tem 11 rótulos de **10sp** e marcas a cada 2 cents (`TunerScreen.kt`, `CentsMeter`). À distância, números e marcas intermediárias ficam pequenos; o alpha da leitura retida reduz ainda mais as marcas. | Manter −50…+50, zero fixo, ponteiro e número de cents, mas destacar cinco rótulos (`−50`, `−25`, `0`, `+25`, `+50`) e marcas principais. Reduzir densidade ou contraste das marcas menores sem perder a leitura do ponteiro. | Com o telefone a 50–70 cm, identificar em até 2 s se uma nota está grave/aguda/afinada e localizar aproximadamente `−16`, `0` e `+16` cents, em luz normal e baixa. |
| **P1** | A linha da nota tem altura fixa de **132dp** com letra em **108sp**; o Auto/Manual tem **42dp** de altura; idiomas usam uma única linha (`TunerScreen.kt`). Fonte ampliada e largura menor podem cortar conteúdo. | Permitir que a área da nota cresça com a fonte; garantir alvo de toque de pelo menos **48dp** para o controle segmentado; fazer idiomas e cabeçalho da sensibilidade quebrarem/adaptarem à largura disponível. Preservar tamanhos visuais atuais na escala padrão. | Em 320dp de largura e escala de fonte 1,3×, 1,5× e 2,0×, nenhuma nota, oitava, rótulo ou controle corta ou sobrepõe conteúdo; tudo segue operável por toque e TalkBack. |
| **P2** | O nível `−94 dBFS` da captura fica fora da faixa do slider (`−80…−20`), mas o marcador é limitado visualmente à ponta de `−80` (`TunerScreen.kt`, `SensitivityCard`). O limiar, marcador e valor digitado não têm rótulos/semântica suficientemente distintos. | Diferenciar **limiar** e **nível captado** por texto curto e semântica acessível; indicar quando a entrada está abaixo da faixa sem fingir que vale `−80`. Dar mensagem textual a valor digitado inválido e estado selecionado explícito a Auto/Manual. | Com limiar `−52` e entrada abaixo de `−80`, usuário entende qual valor é cada um e que o som está abaixo da escala. TalkBack anuncia modo selecionado, limiar, entrada e erro de digitação. |
| **P2** | Em Ajustes, ±1 Hz ficam nas extremidades; a opção 440 salva deixa **Salvar preset** desabilitado sem motivo visível. A linha de idiomas pode apertar com fonte grande; a seleção da referência é sobretudo visual. | Agrupar ±1 Hz mais perto da referência, explicar o botão desabilitado quando o valor já está salvo e dar semântica de seleção/ação à referência e aos presets. Manter gesto vertical, toque para digitar, três presets e idiomas. | Com uma mão livre e telefone apoiado, ajustar 439→440→441, salvar/remover preset e voltar sem esticar a mão repetidamente até bordas opostas. TalkBack anuncia referência atual e ação de editar. |

**Observações menores:** o ícone de Ajustes é o glifo de texto `⚙`, cujo desenho pode variar por aparelho; um ícone vetorial Material estabilizaria a aparência. O botão de remover preset tem altura visual de 36dp; medir a área real de toque antes de classificá-la como menor que 48dp.

**Perspectivas de uso:** o músico com telefone apoiado precisa ler nota e direção a alguma distância; a pessoa com baixa visão precisa de fonte ampliada sem corte; no ajuste ocasional com uma mão, os controles de ±1 Hz distantes entre si elevam o esforço. Nenhum desses casos pede mudar a navegação principal.

## Plano de validação no S24 Ultra

1. Em luz normal e baixa, apoiar o celular a 50–70 cm, tocar `B2`, `E4` e uma nota sustenida em estados grave, dentro de ±2 cents e agudo. Cronometrar a identificação da nota, oitava e direção; verificar o ponteiro e o número de cents sem aproximar o rosto.
2. Tocar uma nota, interromper o som e confirmar a leitura retida por até cinco segundos, a distinção visual para a leitura ao vivo e o retorno ao estado de escuta. Repetir com ruído abaixo do limiar.
3. Repetir telas principal e Ajustes com escala de fonte 1,3×, 1,5× e 2,0×; incluir orientação estreita de 320dp no emulador. Conferir corte, sobreposição, rolagem e abertura do teclado na edição de 400–480 Hz.
4. Ativar TalkBack: percorrer estado de escuta, nota/direção, retenção, permissão negada/erro, Auto/Manual, slider, valor digitado, referência A4 e presets. Confirmar seleção e erro por fala, sem anúncio contínuo a cada atualização de áudio.
5. Medir no Layout Inspector as áreas de toque dos controles personalizados e testar Auto/Manual, ±1 Hz, remover preset, botão de Ajustes e Voltar com uma mão. Confirmar alvos de pelo menos 48dp e ausência de toque acidental.
6. Em Manual, comparar limiar `−52 dBFS` e entrada `−94 dBFS`, então testar entrada dentro e acima da faixa, valor inválido e mudança deliberada para Auto. O indicador deve concordar com o texto.

## Limites da avaliação

As capturas iniciais representam três estados em inglês e não mostram interação, distância real, TalkBack ou fonte ampliada. As dimensões em dp e o comportamento de semântica foram inferidos do código. O detector da skill retornou `[]` porque não processa arquivos `.kt`; isso não significa que a interface Android passou na auditoria automática.

## Andamento em 11 de outubro de 2026

O usuário optou por **preservar o estado de leitura** da primeira proposta P1. Portanto, não foram adicionados rótulos “Escutando”/“Última leitura”, nem alterados o aspecto e o tempo de retenção da nota. As demais propostas foram implementadas:

- Escala de cents com cinco rótulos maiores e menos marcas intermediárias, mantendo zero e ponteiro.
- Área da nota flexível, controle Auto/Manual com alvo de 48dp e semântica de seleção, cabeçalho de sensibilidade e opções de idioma adaptáveis à largura e à fonte.
- Limiar e nível captado identificados separadamente; entrada abaixo/acima da faixa indicada em texto; erro explícito para valor manual inválido.
- Ajuste fino de A4 agrupado, referência com descrição acessível, presets e idiomas com quebra de linha, e motivo visível para “Salvar preset” desabilitado quando o valor já está salvo.

O APK `outputs/apk/afinador-debug-v0.4-ui.apk` compilou com o JBR do Android Studio. No emulador, foram conferidas as telas principal e Ajustes na escala padrão, em 1,5× e em 320dp/2,0×, incluindo a rolagem e a quebra das opções de idioma. A árvore de semântica do Android confirmou a seleção Auto/Manual e a descrição do limiar. Capturas estão em [`outputs/previews/`](../outputs/previews/). Permanecem pendentes a leitura a 50–70 cm, o uso com instrumento no S24 Ultra, a medição das áreas de toque e a validação com TalkBack ativo.
