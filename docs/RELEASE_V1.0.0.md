# Afinador 1.0.0

Primeira versão consolidada do afinador cromático pessoal para Android, voltado principalmente a guitarra e violão.

## Novidades

- Leitura em tempo real de nota, oitava, frequência em Hz e desvio de −50 a +50 cents, com última nota válida retida por até cinco segundos.
- Régua ampliada com marcas a cada 1 cent, referências em −50, −25, 0, +25 e +50 e ponteiro de posição contínua.
- Sensibilidade automática ou manual com limite em dBFS, valor digitável e marcador do nível captado.
- Switch Auto/Manual com toque em toda a cápsula, feedback arredondado e um único controle para acessibilidade.
- Referência A4 ajustável de 400 a 480 Hz, com 440 Hz como padrão e até três presets.
- Interface em português, inglês e francês. O processamento de áudio ocorre no aparelho; o app não salva gravações.

## Instalação

Baixe `afinador-v1.0.0-debug.apk` nos arquivos da release e instale no Android. Autorize o microfone na primeira abertura. O APK é uma compilação de desenvolvimento assinada com a chave de debug; atualizações devem usar a mesma assinatura para instalar sobre esta versão.

SHA-256 do APK: `0f52a774f3c1d4f4bac82cf2ad0ff3c126c1b993d79ec4023a077ca8f3e49490`.

## Validação e limites

A interface foi conferida no emulador em telas compacta e grande, inclusive a régua perto de −50, 0 e +50 cents e o toque nas três regiões do switch. O usuário também testou a correção do switch no aparelho. A precisão, latência e sensibilidade do detector ainda precisam de medição sistemática com instrumentos reais no Samsung S24 Ultra; o desempenho pode variar conforme o microfone e o processamento do Android. O foco de acessibilidade foi inspecionado com um serviço de teste, mas a fala do TalkBack ainda não foi verificada.
