# Simulador de Rede de Filas

## Descrição

Este projeto implementa um simulador de rede de filas baseado em eventos discretos. O simulador permite analisar o comportamento de uma rede de filas interconectadas, coletando estatísticas sobre o tempo em cada estado das filas, probabilidades estacionárias e número de perdas de clientes.

## Estrutura do Projeto

O projeto é composto pelas seguintes classes:

- **GeradorAleatorio**: Responsável por gerar números aleatórios para os tempos de chegada e atendimento.
- **Evento**: Representa os eventos da simulação (chegadas e saídas).
- **Fila**: Modela uma fila no sistema, com capacidade, número de servidores e tempos de atendimento.
- **Escalonador**: Gerencia a agenda de eventos e controla o avanço do tempo de simulação.
- **Simulador**: Coordena todo o processo de simulação e gera o relatório final.
- **Main**: Classe de inicialização que configura e executa o simulador.

## Configuração

O simulador utiliza um arquivo de configuração `config.txt` com o seguinte formato:

```
[número de filas]

[capacidade] [num_servidores] [min_chegada] [max_chegada] [min_atendimento] [max_atendimento]
...

[origem] [destino] [probabilidade]
...
```

Exemplo de configuração para uma rede com 3 filas:

```
3

4 1 2.0 4.0 1.0 2.0
5 2 0.0 0.0 4.0 8.0
10 2 0.0 0.0 5.0 15.0

0 1 1.0

1 2 0.8
1 3 0.2

2 2 0.5
2 1 0.3
2 0 0.2

3 3 0.7
3 0 0.3
```

Onde:
- A primeira linha indica o número de filas.
- Para cada fila, uma linha com: capacidade, número de servidores, tempo mínimo e máximo de chegada, tempo mínimo e máximo de atendimento.
- As linhas seguintes definem as probabilidades de roteamento entre as filas, onde 0 representa o mundo externo.

## Funcionamento da Simulação

A simulação segue a abordagem de eventos discretos:

1. O simulador lê a configuração do arquivo `config.txt`.
2. O escalonador agenda o primeiro evento (chegada do primeiro cliente no tempo 2.0).
3. A simulação avança evento por evento, processando chegadas e saídas nas filas.
4. As estatísticas são coletadas ao longo da simulação.
5. Após o processamento de 100.000 números aleatórios, a simulação termina e o relatório é gerado.

## Como Executar

Para executar o simulador:

1. Compile todas as classes:
   ```
   javac *.java
   ```

2. Execute a classe Main:
   ```
   java Main
   ```

O programa irá gerar um relatório com as estatísticas da simulação em um arquivo de texto com o nome no formato `relatorio_simulacao_YYYYMMDD_HHMMSS.txt`.

## Formato do Relatório

O relatório gerado tem o seguinte formato:

```
=========================================================
=================    END OF SIMULATION   ================
=========================================================
=========================================================
======================    REPORT   ======================
=========================================================
*********************************************************
Queue:   FILA1 (G/G/1)
Arrival: 2.0 ... 4.0
Service: 1.0 ... 2.0
*********************************************************
   State               Time               Probability
      0           [tempo]                [probabilidade]%
      ...
Number of losses: [perdas]
*********************************************************
...
=========================================================
Simulation average time: [tempo_total]
=========================================================
```

## Exemplo de Rede Simulada

O exemplo padrão implementa uma rede de filas com a seguinte estrutura:

- **Fila 1 (G/G/1)**: Um servidor, capacidade 4, chegadas entre 2-4 min, atendimento entre 1-2 min
- **Fila 2 (G/G/2/5)**: Dois servidores, capacidade 5, atendimento entre 4-8 min
- **Fila 3 (G/G/2/10)**: Dois servidores, capacidade 10, atendimento entre 5-15 min

Com as seguintes probabilidades de roteamento:
- Da Fila 1: 80% para Fila 2, 20% para Fila 3
- Da Fila 2: 50% para Fila2, 30% para Fila 1, 20% saem do sistema
- Da Fila 3: 70% para Fila 3, 30% saem do sistema

## Requisitos

- Java 8 ou superior