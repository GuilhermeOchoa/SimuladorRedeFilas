# Simulador de Redes de Filas com Roteamento Probabilístico

Este simulador permite modelar e simular sistemas de filas com múltiplas filas e roteamento probabilístico de clientes entre elas.

## Características Principais

- Suporte para múltiplas filas com capacidade finita ou infinita
- Múltiplos servidores por fila
- Tempos de serviço e chegada configuráveis (distribuição uniforme)
- Roteamento probabilístico de clientes entre filas
- Estatísticas completas de desempenho do sistema
- Configuração flexível via arquivos YAML

## Requisitos

- Java 8 ou superior
- Biblioteca SnakeYAML (para processamento de arquivos YAML)

## Como Compilar

1. Certifique-se de ter o JDK instalado
2. Adicione a biblioteca SnakeYAML ao seu classpath
3. Compile todas as classes:

```bash
javac *.java
```

## Como Executar

Execute o simulador fornecendo o caminho para um arquivo de configuração YAML:

```bash
java Simulador caminho/para/configuracao.yml
```

## Formato do Arquivo de Configuração

O arquivo de configuração YAML deve seguir o seguinte formato:

```yaml
tempo_simulacao: [tempo total da simulação]
semente: [semente para o gerador de números aleatórios]

filas:
  fila0:
    capacidade: [capacidade máxima da fila (-1 para infinita)]
    servidores: [número de servidores]
    tempo_servico_min: [tempo mínimo de serviço]
    tempo_servico_max: [tempo máximo de serviço]
    chegada_min: [tempo mínimo entre chegadas (somente para filas de entrada)]
    chegada_max: [tempo máximo entre chegadas (somente para filas de entrada)]
    rotas:
      fila1: [probabilidade de ir para fila1]
      fila2: [probabilidade de ir para fila2]
      saida: [probabilidade de sair do sistema]
  
  fila1:
    # ...configuração similar...
```

**Notas importantes:**
- A soma das probabilidades de roteamento para cada fila deve ser igual a 1.0
- Use `saida` como destino para clientes que saem do sistema
- Filas de entrada (que recebem clientes do exterior) devem ter os parâmetros `chegada_min` e `chegada_max`

## Exemplo de Uso

1. Crie um arquivo de configuração YAML (por exemplo, `config.yml`) com a definição do seu sistema
2. Execute o simulador:
   ```bash
   java Simulador config.yml
   ```
3. O simulador irá executar a simulação e exibir as estatísticas

## Resultados da Simulação

O simulador fornece as seguintes estatísticas:

- Tempo total simulado
- Total de clientes processados, rejeitados e concluídos
- Para cada fila:
  - Número de clientes atendidos
  - Tempo médio de espera
  - Taxa de utilização dos servidores

## Classes Principais

- **Simulador**: Classe principal que executa a simulação
- **Escalonador**: Gerencia a fila de eventos e executa a simulação
- **Fila**: Representa uma fila no sistema, com seus parâmetros e estatísticas
- **Evento**: Representa eventos de chegada, passagem ou saída de clientes
- **GeradorNumerosPseudoAleatorios**: Gera números aleatórios usando o método congruente linear
- **CarregadorConfiguracao**: Carrega a configuração do sistema a partir de um arquivo YAML

## Personalização

O simulador pode ser estendido de várias formas:

1. Implementar diferentes distribuições de probabilidade para tempos de serviço e chegada
2. Adicionar políticas de atendimento diferentes (além de FIFO)
3. Implementar prioridades para diferentes tipos de clientes
4. Adicionar coleta de métricas adicionais

## Exemplo de Modelo

O modelo de exemplo incluído simula um sistema com duas filas:
- Fila 0: Recebe clientes do exterior, tem 2 servidores e capacidade para 4 clientes
- Fila 1: Tem 1 servidor e capacidade infinita

No modelo, 70% dos clientes da Fila 0 vão para a Fila 1, e 30% saem do sistema.