# Produto

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Android nativo em Kotlin com Jetpack Compose, escolha confirmada pelo usuário.

## Users

Uso pessoal de um músico afinando principalmente guitarra e violão com o microfone de um celular Android.

## Product Purpose

Mostrar em tempo real a nota e a oitava de um som isolado, a frequência medida e o desvio até a afinação desejada.

## Operating Context

O primeiro aparelho de teste é um Samsung S24 Ultra. O usuário tem acesso a esse aparelho e talvez ao Android Studio no computador, mas precisará de um passo a passo quando a etapa de instalação e teste chegar.

## Capabilities and Constraints

- Afinador cromático de uma nota por vez; uso principal com guitarra e violão.
- Faixa inicial C2–E5, sujeita a validação no aparelho real.
- Notas na notação A–G com número da oitava, como `E2` e `A4`.
- Referência A4 ajustável entre 400 e 480 Hz em Ajustes; 440 Hz como padrão e até três presets salvos.
- Mostrar nota, sustenido elevado, oitava menor que a letra, frequência e desvio em cents. Nota e medidor ficam laranja acima de ±2 cents. A direção aparece pelo indicador da escala e nos anúncios de acessibilidade, sem frase extra sob o medidor.
- Reter a última leitura por até cinco segundos, com aparência atenuada quando o sinal deixa de ser válido.
- Sensibilidade automática e manual em controle segmentado; slider com marcador do nível captado em dBFS e entrada direta do valor.
- Interface em português, inglês ou francês, escolhida em Ajustes.
- Processamento local do áudio, sem gravação persistente.
- Desempenho real, limites do controle de sensibilidade e metas de precisão ainda dependem de testes.

## Product Principles

- A leitura deve ser confiável o suficiente para afinar; silêncio e ruído não devem parecer notas válidas.
- A informação principal deve ser legível rapidamente durante a afinação.
- Ajustes avançados devem estar disponíveis sem atrapalhar o fluxo principal.
