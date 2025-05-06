import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe Simulador - classe principal que coordena a simulação da rede de filas
 */
public class Simulador {
    private final int MAX_ALEATORIOS;
    private final List<Fila> filas;
    private final GeradorAleatorio gerador;
    private final Escalonador escalonador;
    private final double[][] matrizRoteamento;
    private double tempoSimulacao;
    
    /**
     * Construtor
     * @param arquivoEntrada Nome do arquivo de configuração
     * @param maxAleatorios Número máximo de aleatórios a serem usados
     */
    public Simulador(String arquivoEntrada, int maxAleatorios) {
        this.MAX_ALEATORIOS = maxAleatorios;
        this.filas = new ArrayList<>();
        this.gerador = new GeradorAleatorio(123456789L); // Semente fixa para reprodutibilidade
        this.tempoSimulacao = 0.0;
        
        // Carrega a configuração do arquivo
        int numFilas = carregarEntrada(arquivoEntrada);
        this.matrizRoteamento = new double[numFilas + 1][numFilas + 1];
        carregarMatrizRoteamento(arquivoEntrada, numFilas);
        
        // Cria o escalonador
        this.escalonador = new Escalonador(filas, matrizRoteamento, gerador);
    }
    /**
 * Gera o relatório da simulação em um arquivo de texto
 * @param nomeArquivo Nome do arquivo de saída
 */
public void gerarRelatorioArquivo(String nomeArquivo) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivo))) {
        writer.println("=========================================================");
        writer.println("=================    END OF SIMULATION   ================");
        writer.println("=========================================================");
        writer.println("=========================================================");
        writer.println("======================    REPORT   ======================");
        writer.println("=========================================================");
        
        for (int i = 0; i < filas.size(); i++) {
            Fila fila = filas.get(i);
            writer.println("*********************************************************");
            
            // Nome da fila e caracterização usando notação de Kendall
            // Formato: G/G/m/K onde m é número de servidores e K é capacidade
            String nomeConfig = String.format("FILA%d (G/G/%d/%d)", 
                                             i+1, 
                                             fila.Servers(), 
                                             fila.Capacity());
            
            // Caso especial para fila sem capacidade finita (G/G/m)
            if (fila.Capacity() == Integer.MAX_VALUE) {
                nomeConfig = String.format("FILA%d (G/G/%d)", i+1, fila.Servers());
            }
            
            writer.println("Queue:   " + nomeConfig);
            
            // Só imprime os tempos de chegada para filas que têm chegadas externas
            if (fila.getMinChegada() > 0 || fila.getMaxChegada() > 0) {
                writer.printf("Arrival: %.1f ... %.1f\n", fila.getMinChegada(), fila.getMaxChegada());
            }
            
            writer.printf("Service: %.1f ... %.1f\n", fila.getMinAtendimento(), fila.getMaxAtendimento());
            writer.println("*********************************************************");
            
            // Tabela de estados
            writer.println("   State               Time               Probability");
            
            double[] temposEstado = fila.getTemposEstado();
            for (int j = 0; j < temposEstado.length; j++) {
                double probabilidade = (temposEstado[j] / tempoSimulacao) * 100;
                writer.printf("      %d           %10.4f                %5.2f%%\n", 
                             j, temposEstado[j], probabilidade);
            }
            
            // Número de perdas
            writer.println("Number of losses: " + fila.getPerdidos());
            
            // Calcular e mostrar índices de desempenho adicionais
            double utilizacao = calcularUtilizacao(fila, temposEstado);
            double populacaoMedia = calcularPopulacaoMedia(temposEstado);
            double tempoMedioSistema = estimarTempoMedioNoSistema(fila, populacaoMedia);
            
            writer.println("*********************************************************");
            writer.println("Performance Metrics:");
            writer.printf("Utilization: %5.2f%%\n", utilizacao * 100);
            writer.printf("Average Population: %5.2f clients\n", populacaoMedia);
            writer.printf("Estimated Average Response Time: %5.2f minutes\n", tempoMedioSistema);
            writer.println("*********************************************************");
        }
        
        writer.println("=========================================================");
        writer.printf("Simulation average time: %.4f\n", tempoSimulacao);
        writer.println("=========================================================");
        
        System.out.println("Relatório gerado com sucesso no arquivo: " + nomeArquivo);
    } catch (IOException e) {
        System.err.println("Erro ao gerar relatório: " + e.getMessage());
    }
}

/**
 * Calcula a utilização dos servidores com base nos tempos dos estados
 * @param fila A fila analisada
 * @param temposEstado Array com os tempos em cada estado
 * @return Taxa de utilização (0-1)
 */
private double calcularUtilizacao(Fila fila, double[] temposEstado) {
    double tempoTotalDesocupado = temposEstado[0];
    return 1.0 - (tempoTotalDesocupado / tempoSimulacao);
}

/**
 * Calcula a população média com base nos tempos dos estados
 * @param temposEstado Array com os tempos em cada estado
 * @return População média
 */
private double calcularPopulacaoMedia(double[] temposEstado) {
    double soma = 0.0;
    for (int i = 0; i < temposEstado.length; i++) {
        soma += i * temposEstado[i];
    }
    return soma / tempoSimulacao;
}

/**
 * Estima o tempo médio que um cliente passa no sistema (Lei de Little)
 * @param fila A fila analisada
 * @param populacaoMedia População média calculada
 * @return Tempo médio estimado no sistema
 */
private double estimarTempoMedioNoSistema(Fila fila, double populacaoMedia) {
    // Vazão estimada (clientes por unidade de tempo)
    double taxaChegadaEfetiva = populacaoMedia / 
        ((fila.getMinAtendimento() + fila.getMaxAtendimento()) / 2.0);
    
    // Aplicando a Lei de Little: E[N] = λ * E[T]
    if (taxaChegadaEfetiva > 0) {
        return populacaoMedia / taxaChegadaEfetiva;
    } else {
        return 0.0;
    }
}
    /**
     * Carrega a configuração das filas do arquivo
     * @param arquivo Nome do arquivo de configuração
     * @return Número de filas
     */
    private int carregarEntrada(String arquivo) {
        int numeroFilas = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int filaAtual = 0;
            
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue; // Ignora linhas em branco ou comentários
                }
                
                String[] partes = linha.split("\\s+");
                if (numeroFilas == 0) {
                    numeroFilas = Integer.parseInt(partes[0]);
                    continue;
                }
                
                if (filaAtual < numeroFilas) {
                    int capacidade = Integer.parseInt(partes[0]);
                    int numServidores = Integer.parseInt(partes[1]);
                    double minChegada = Double.parseDouble(partes[2]);
                    double maxChegada = Double.parseDouble(partes[3]);
                    double minAtendimento = Double.parseDouble(partes[4]);
                    double maxAtendimento = Double.parseDouble(partes[5]);
                    
                    filas.add(new Fila(capacidade, numServidores, 
                               minChegada, maxChegada, 
                               minAtendimento, maxAtendimento));
                    filaAtual++;
                }
                
                // Se já leu todas as filas, sai do loop
                if (filaAtual >= numeroFilas) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo de entrada: " + e.getMessage());
            System.exit(1);
        }
        return numeroFilas;
    }
    
    /**
     * Carrega a matriz de roteamento do arquivo
     * @param arquivo Nome do arquivo de configuração
     * @param numFilas Número de filas
     */
    private void carregarMatrizRoteamento(String arquivo, int numFilas) {
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int linhasLidas = 0;
            boolean lerMatriz = false;
            
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue; // Ignora linhas em branco ou comentários
                }
                
                linhasLidas++;
                
                // Pula a primeira linha (número de filas)
                if (linhasLidas == 1) {
                    continue;
                }
                
                // Pula as linhas de configuração das filas
                if (linhasLidas <= numFilas + 1) {
                    continue;
                }
                
                // Começamos a ler a matriz de roteamento
                lerMatriz = true;
                
                String[] partes = linha.split("\\s+");
                if (partes.length >= 3) {
                    int origem = Integer.parseInt(partes[0]);
                    int destino = Integer.parseInt(partes[1]);
                    double probabilidade = Double.parseDouble(partes[2]);
                    
                    // Adiciona as probabilidades à matriz de roteamento
                    matrizRoteamento[origem][destino] = probabilidade;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler matriz de roteamento: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Executa a simulação
     */
    public void executar() {
        // Inicializa o escalonador
        escalonador.inicializar(2.0); // Primeiro cliente chega no tempo 2.0
        
        // Executa a simulação
        tempoSimulacao = escalonador.executar(MAX_ALEATORIOS);
        
        // Gera o relatório
        gerarRelatorio();
    }
    
    /**
     * Gera o relatório da simulação
     */
    public void gerarRelatorio() {
        System.out.println("=========================================================");
        System.out.println("=================    END OF SIMULATION   ================");
        System.out.println("=========================================================");
        System.out.println("=========================================================");
        System.out.println("======================    REPORT   ======================");
        System.out.println("=========================================================");
        
        for (int i = 0; i < filas.size(); i++) {
            Fila fila = filas.get(i);
            System.out.println("*********************************************************");
            
            // Nome da fila e caracterização
            String nomeConfig;
            if (i == 0) {
                nomeConfig = String.format("FILA1 (G/G/1)");
                System.out.println("Queue:   " + nomeConfig);
                System.out.printf("Arrival: %.1f ... %.1f\n", fila.getMinChegada(), fila.getMaxChegada());
            } else if (i == 1) {
                nomeConfig = String.format("FILA2 (G/G/2/5)");
                System.out.println("Queue:   " + nomeConfig);
            } else {
                nomeConfig = String.format("FILA3 (G/G/2/10)");
                System.out.println("Queue:   " + nomeConfig);
            }
            
            System.out.printf("Service: %.1f ... %.1f\n", fila.getMinAtendimento(), fila.getMaxAtendimento());
            System.out.println("*********************************************************");
            
            // Tabela de estados
            System.out.println("   State               Time               Probability");
            
            double[] temposEstado = fila.getTemposEstado();
            for (int j = 0; j < temposEstado.length; j++) {
                double probabilidade = (temposEstado[j] / tempoSimulacao) * 100;
                System.out.printf("      %d           %10.4f                %5.2f%%\n", 
                                 j, temposEstado[j], probabilidade);
            }
            
            // Número de perdas
            System.out.println("Number of losses: " + fila.getPerdidos());
        }
        
        System.out.println("=========================================================");
        System.out.printf("Simulation average time: %.4f\n", tempoSimulacao);
        System.out.println("=========================================================");
    }
    
    /**
     * Método principal
     * @param args Argumentos da linha de comando (arquivo_entrada, num_aleatorios)
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java Simulador <arquivo_entrada> <num_aleatorios>");
            System.exit(1);
        }
        
        String arquivoEntrada = args[0];
        int numAleatorios = Integer.parseInt(args[1]);
        
        Simulador simulador = new Simulador(arquivoEntrada, numAleatorios);
        simulador.executar();
    }
}