# Afinador

Aplicativo Android de afinador cromático para instrumentos musicais, desenvolvido em Kotlin e Jetpack Compose. Detecta uma nota por vez pelo microfone e mostra frequência, nota, oitava e desvio em cents.

## Recursos

- Referência A4 ajustável de 400 a 480 Hz, com até três valores salvos.
- Sensibilidade automática ou limiar manual em dBFS, com indicação do nível captado.
- Última leitura exibida de forma atenuada por até cinco segundos.
- Interface em português, inglês e francês.
- Processamento local: o áudio não é salvo nem enviado a um servidor.

## Executar

1. Abra a pasta [`code/android`](code/android) no Android Studio.
2. Aguarde a sincronização do Gradle e execute o aplicativo em um dispositivo ou emulador Android.
3. Conceda permissão de microfone ao abrir o app.

O projeto requer JDK 17 e Android SDK 35. Também é possível compilar com o wrapper do Gradle na pasta `code/android`. APKs de desenvolvimento são gerados localmente e não fazem parte do repositório.

Mais detalhes estão em [docs/README.md](docs/README.md). O [backlog](docs/BACKLOG.md) registra as limitações e próximas validações.
