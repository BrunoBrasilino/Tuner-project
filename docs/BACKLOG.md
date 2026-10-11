# Afinador Android — visão e backlog inicial

Status: versão 1.0.0 do afinador Android organizada em 11 de outubro de 2026. A interface foi testada no emulador e pelo usuário no aparelho; a medição sistemática da precisão de áudio no Samsung S24 Ultra ainda está pendente.

## Andamento da fase 1

- **Feito:** projeto Kotlin/Compose, captação PCM com `AudioRecord`, detector YIN inicial, tela com sustenido elevado, indicador laranja acima de ±2 cents e última leitura retida por cinco segundos; referência A4 vertical com digitação e até três presets; português/inglês/francês; sensibilidade Auto/Manual com nível captado em dBFS; persistência e APK de desenvolvimento.
- **Verificado:** compilação Android, quatro testes locais com tons sintéticos, dois testes da retenção da última nota e interação da referência no emulador Android.
- **Pendente:** medir precisão, resposta, estabilidade, sensibilidade e desempenho de C2–E5 com guitarra/violão no S24 Ultra; confirmar o comportamento de diferentes fontes de microfone e ajustar o detector a partir desses resultados.

## Objetivo

Criar um afinador cromático para Android que use o microfone do celular e mostre, em tempo real, a nota detectada, sua oitava, a frequência medida e o desvio em cents em relação à afinação escolhida.

## Recorte da primeira versão

- Afinador cromático para **uma nota por vez**, com guitarra/violão como uso principal e folga para afinações alternativas. O usuário pode começar sem escolher um instrumento.
- Faixa inicial de detecção: **C2 a E5** (aproximadamente 65–659 Hz quando A4 = 440 Hz). C2 fica abaixo do Mi2 da sexta corda em afinação padrão. O desempenho nas extremidades será validado no aparelho real.
- Referência ajustável do **A4**, entre **400 e 480 Hz**, com 440 Hz como padrão e até três presets. O valor escolhido desloca a frequência-alvo de todas as notas; não altera a taxa de amostragem do microfone.
- Nota e oitava explícitas em notação **A–G**, como `C2`, `E4` e `A4`, adotando **C4 como dó central**.
- Indicador visual de grave/afinada/aguda, com desvio em cents e frequência em Hz.
- Controle de sensibilidade com modo automático e ajuste manual do limite mínimo de sinal. No modo manual, o slider mostra o valor numérico selecionado e permite digitá-lo diretamente.
- Funcionamento local, sem conta, rede ou gravação persistente de áudio.

### Fora do primeiro recorte

Afinações prontas por instrumento, detecção de acordes, histórico, metrônomo, geração de tom, microfone externo e suporte a iOS. Podem entrar depois que a detecção cromática estiver confiável.

## Como o afinador funcionará

`microfone → PCM → nível/qualidade do sinal → frequência fundamental → estabilização → nota + oitava + cents → interface`

1. **Captação:** pedir permissão do microfone e ler áudio mono em blocos. `AudioRecord` oferece acesso a amostras PCM para análise. Testar a fonte `UNPROCESSED` quando disponível, com alternativa compatível; o comportamento muda conforme o aparelho.
2. **Detecção:** estimar a frequência fundamental, não simplesmente escolher o pico mais alto de uma FFT. Uma primeira opção a avaliar é YIN, que foi proposto para voz e instrumentos musicais e ajuda a evitar erros de oitava.
3. **Validação:** descartar silêncio, ruído e estimativas sem confiança suficiente. Usar nível RMS em **dBFS** e uma medida de confiança do detector. dBFS é relativo ao sinal digital, **não** uma medição calibrada de dB SPL do ambiente.
4. **Estabilização:** suavizar pequenas variações e exigir evidência antes de trocar a nota exibida, sem esconder mudanças reais. Quando a leitura deixar de ser confiável, indicar “sem nota estável” em vez de manter uma nota antiga como se fosse atual.
5. **Mapeamento musical:** converter a frequência medida para a nota mais próxima com base no Lá4 configurado e calcular `cents = 1200 × log2(frequência medida / frequência-alvo)`.

O ajuste chamado “sensibilidade” atuará primeiro no **limite de detecção por software**. Não devemos prometer controle do ganho físico do microfone, que depende do hardware e do processamento do Android.

## Backlog priorizado

| Ordem | Entrega | Critério de aceite |
| --- | --- | --- |
| P0.1 | Definir cenário de teste | Usar C2–E5 e notação A–G; registrar como avaliar precisão e rapidez no Samsung S24 Ultra. |
| P0.2 | Criar projeto Android | App em Kotlin com Jetpack Compose já compila e abre no emulador. Ainda falta abrir no Samsung S24 Ultra; preparar o passo a passo de Android Studio e instalação para essa etapa. |
| P0.3 | Captar áudio do microfone | Permissão clara; iniciar/parar captação junto à tela; tratar permissão negada, microfone indisponível e mudança de rota de áudio. |
| P0.4 | Fazer prova de conceito do detector | Tons sintéticos em C2–E5 e um caso de segundo harmônico mais forte passaram nos testes. Falta guitarra/violão real; medir erros de frequência, saltos de oitava, atraso e CPU antes de fechar o algoritmo e os tamanhos de janela. |
| P0.5 | Mostrar afinador cromático | Nota, oitava, Hz, cents e indicador de direção atualizam ao tocar uma nota estável; silêncio não mostra resultado enganoso. |
| P0.6 | Ajustar referência A4 | Ajuste em passos de 1 Hz de 400–480 Hz; até três presets salvos com 440 Hz fixo; todas as frequências-alvo seguem a referência escolhida. |
| P0.7 | Filtrar ruído e ajustar sensibilidade | Modo automático utilizável; no manual, slider exibe o limite numérico em dBFS e um campo permite digitá-lo. A faixa e o valor padrão serão calibrados nos testes. A interface explica que o ajuste filtra leituras, não amplifica o microfone. |
| P0.8 | Validar no aparelho real | Testar no Samsung S24 Ultra com guitarra/violão, incluindo notas próximas a C2 e E5; registrar limitações e corrigir falhas relevantes. Outros modelos entram quando estiverem disponíveis. |
| P1.1 | Melhorar acesso e robustez | Feedback legível em ambientes claros/escuros, acessibilidade, rotação, pausa/retomada e consumo de bateria aceitável. |
| P2.1 | Perfis de instrumentos | Cordas e afinações comuns como atalhos, após o modo cromático funcionar bem. |

## Primeira sequência de trabalho sugerida

1. Preparar uma pequena lista de instrumentos disponíveis para teste e os critérios de precisão e rapidez.
2. Implementar a captação e um detector isolado da interface.
3. Validar o detector com arquivos de teste conhecidos e com instrumentos reais; ajustar janela, confiança e estabilidade.
4. Montar a tela do afinador e persistir a referência A4 e a sensibilidade.
5. Fazer teste de uso em aparelhos físicos e corrigir os problemas encontrados.

## Pontos para validar na prova de conceito

- **Notas graves:** confirmar que C2 responde bem no S24 Ultra. Expandir abaixo de C2, como para um baixo em E1 ou C1, fica para uma etapa posterior e exigirá testes específicos.
- **Sensibilidade:** definir faixa, valor padrão e passos do controle numérico em dBFS após medir o sinal do aparelho e de instrumentos reais.
- **Precisão e resposta:** fixar metas mensuráveis para cents, tempo de atualização e estabilidade depois dos primeiros testes com áudio conhecido e guitarra/violão.
- **Aparelhos adicionais:** testar outros modelos Android quando houver acesso a eles para avaliar diferenças de microfone e processamento.

## Decisões técnicas

- **Android nativo com Kotlin + Jetpack Compose** para o aplicativo e a interface.
- **AudioRecord** para a primeira implementação de captação, com a análise em uma camada própria fora da thread da interface.
- **YIN** como primeiro algoritmo de detecção a experimentar. A prova de conceito determinará se ele atende aos requisitos de precisão e resposta.
- **Oboe/AAudio** como opção para depois, caso os testes mostrem necessidade de melhorar latência ou estabilidade da captação.

### Referências

- [Android: AudioRecord](https://developer.android.com/reference/android/media/AudioRecord)
- [Android: fontes de áudio e UNPROCESSED](https://developer.android.com/reference/android/media/MediaRecorder.AudioSource)
- [Android: Jetpack Compose](https://developer.android.com/compose)
- [Android: Oboe e áudio de baixa latência](https://developer.android.com/games/sdk/oboe/low-latency-audio)
- [de Cheveigné e Kawahara: YIN, a fundamental frequency estimator for speech and music](https://pubmed.ncbi.nlm.nih.gov/12002874/)
