import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Classe que representa uma fila no sistema de simulação.
 * Cada fila tem uma capacidade, número de servidores, e tempos de serviço.
 * Versão atualizada para incluir estatísticas de estados por fila.
 */
public class Fila {
    private int id;                           // Identificador único da fila
    private int numClientes;                  // Número atual de clientes na fila
    private int capacidade;                   // Capacidade máxima da fila (-1 para infinita)
    private int numServidores;                // Número de servidores atendendo a fila
    private int servidoresOcupados;           // Número de servidores atualmente ocupados
    private double tempoServicoMin;           // Tempo mínimo de serviço
    private double tempoServicoMax;           // Tempo máximo de serviço
    private double chegadaMin;                // Tempo mínimo entre chegadas (se for fila de entrada)
    private double chegadaMax;                // Tempo máximo entre chegadas (se for fila de entrada)
    private boolean filaDeEntrada;            // Indica se é uma fila que recebe clientes do exterior
    
    // Estatísticas
    private int totalClientesAtendidos;       // Total de clientes atendidos
    private int totalClientesPerdidos;        // Total de clientes rejeitados por fila cheia
    private double tempoTotalEspera;          // Tempo total de espera dos clientes
    private double ocupacaoTotal;             // Tempo total de ocupação dos servidores
    private double ultimoTempoEvento;         // Tempo do último evento processado
    
    // Estatísticas de estado da fila
    private Map<Integer, Double> tempoEmEstado; // Mapeia o estado para o tempo acumulado
    private int estadoMaximoRegistrado;         // Maior estado encontrado durante a simulação
    
    // Estrutura para armazenar as rotas possíveis a partir desta fila
    private List<Rota> rotas;
    
    /**
     * Classe interna que representa uma rota possível a partir desta fila
     */
    public class Rota {
        private int filaDestinoId;            // ID da fila de destino (-1 para saída do sistema)
        private double probabilidade;         // Probabilidade de escolha desta rota
        
        public Rota(int filaDestinoId, double probabilidade) {
            this.filaDestinoId = filaDestinoId;
            this.probabilidade = probabilidade;
        }
        
        public int getFilaDestinoId() {
            return filaDestinoId;
        }
        
        public double getProbabilidade() {
            return probabilidade;
        }
    }
    
    /**
     * Construtor para fila de entrada (que recebe clientes do exterior)
     */
    public Fila(int id, int capacidade, int numServidores, 
                double tempoServicoMin, double tempoServicoMax,
                double chegadaMin, double chegadaMax) {
        this.id = id;
        this.capacidade = capacidade;
        this.numServidores = numServidores;
        this.tempoServicoMin = tempoServicoMin;
        this.tempoServicoMax = tempoServicoMax;
        this.chegadaMin = chegadaMin;
        this.chegadaMax = chegadaMax;
        this.filaDeEntrada = true;
        
        // Inicializa valores
        this.numClientes = 0;
        this.servidoresOcupados = 0;
        this.totalClientesAtendidos = 0;
        this.totalClientesPerdidos = 0;
        this.tempoTotalEspera = 0;
        this.ocupacaoTotal = 0;
        this.ultimoTempoEvento = 0;
        this.tempoEmEstado = new HashMap<>();
        this.estadoMaximoRegistrado = 0;
        
        this.rotas = new ArrayList<>();
    }
    
    /**
     * Construtor para fila intermediária (que não recebe clientes do exterior)
     */
    public Fila(int id, int capacidade, int numServidores, 
                double tempoServicoMin, double tempoServicoMax) {
        this(id, capacidade, numServidores, tempoServicoMin, tempoServicoMax, 0, 0);
        this.filaDeEntrada = false;
    }
    
    /**
     * Adiciona uma rota possível a partir desta fila
     * @param filaDestinoId ID da fila de destino (-1 para saída do sistema)
     * @param probabilidade Probabilidade de escolha desta rota
     */
    public void adicionarRota(int filaDestinoId, double probabilidade) {
        rotas.add(new Rota(filaDestinoId, probabilidade));
    }
    
    /**
     * Verifica se a fila tem capacidade para receber mais um cliente
     * @return true se a fila pode receber mais um cliente, false caso contrário
     */
    public boolean podeReceberCliente() {
        return capacidade == -1 || numClientes < capacidade;
    }
    
    /**
     * Adiciona um cliente à fila
     * @param tempoAtual Tempo atual da simulação
     * @return true se o cliente foi adicionado com sucesso, false caso contrário
     */
    public boolean adicionarCliente(double tempoAtual) {
        if (!podeReceberCliente()) {
            totalClientesPerdidos++;
            return false;
        }
        
        // Registra o tempo no estado atual antes de mudar
        registrarTempoEmEstado(tempoAtual);
        
        numClientes++;
        
        // Atualiza o estado máximo registrado
        if (numClientes > estadoMaximoRegistrado) {
            estadoMaximoRegistrado = numClientes;
        }
        
        // Se houver servidor disponível, o cliente é atendido imediatamente
        if (servidoresOcupados < numServidores) {
            servidoresOcupados++;
        }
        
        atualizarEstatisticas(tempoAtual);
        return true;
    }
    
    
    /**
     * Remove um cliente da fila após atendimento
     * @param tempoAtual Tempo atual da simulação
     */
    public void removerCliente(double tempoAtual) {
        if (numClientes > 0) {
            // Registra o tempo no estado atual antes de mudar
            registrarTempoEmEstado(tempoAtual);
            
            numClientes--;
            totalClientesAtendidos++;
            
            // Se ainda houver clientes na fila e servidores disponíveis, atende o próximo
            if (numClientes >= servidoresOcupados && servidoresOcupados < numServidores) {
                servidoresOcupados++;
            } else if (numClientes < servidoresOcupados) {
                servidoresOcupados--;
            }
            
            atualizarEstatisticas(tempoAtual);
        }
    }
    
    /**
     * Registra o tempo gasto no estado atual
     * @param tempoAtual Tempo atual da simulação
     */
    private void registrarTempoEmEstado(double tempoAtual) {
        double tempoDuracao = tempoAtual - ultimoTempoEvento;
        tempoEmEstado.put(numClientes, tempoEmEstado.getOrDefault(numClientes, 0.0) + tempoDuracao);
    }
    
    /**
     * Atualiza as estatísticas da fila
     * @param tempoAtual Tempo atual da simulação
     */
    private void atualizarEstatisticas(double tempoAtual) {
        double tempoDecorrido = tempoAtual - ultimoTempoEvento;
        
        // Atualiza ocupação dos servidores
        ocupacaoTotal += servidoresOcupados * tempoDecorrido;
        
        // Atualiza tempo de espera dos clientes na fila (não sendo atendidos)
        int clientesEsperando = Math.max(0, numClientes - servidoresOcupados);
        tempoTotalEspera += clientesEsperando * tempoDecorrido;
        
        ultimoTempoEvento = tempoAtual;
    }
    
    /**
     * Finaliza a coleta de estatísticas
     * @param tempoFinal Tempo final da simulação
     */
    public void finalizarEstatisticas(double tempoFinal) {
        registrarTempoEmEstado(tempoFinal);
        ultimoTempoEvento = tempoFinal;
    }
    
    /**
     * Escolhe aleatoriamente a próxima rota para um cliente
     * @param gerador Gerador de números aleatórios
     * @return ID da próxima fila (-1 para saída do sistema)
     */
    public int escolherProximaRota(GeradorNumerosPseudoAleatorios gerador) {
        if (rotas.isEmpty()) {
            return -1; // Se não houver rotas definidas, cliente sai do sistema
        }
        
        double valorSorteado = gerador.proximoNumero();
        double somaAcumulada = 0.0;
        
        for (Rota rota : rotas) {
            somaAcumulada += rota.getProbabilidade();
            if (valorSorteado < somaAcumulada) {
                return rota.getFilaDestinoId();
            }
        }
        
        // Se chegar aqui, retorna a última rota (caso de arredondamento)
        return rotas.get(rotas.size() - 1).getFilaDestinoId();
    }
    
    /**
     * Gera um tempo de serviço aleatório para um cliente
     * @param gerador Gerador de números aleatórios
     * @return Tempo de serviço
     */
    public double gerarTempoServico(GeradorNumerosPseudoAleatorios gerador) {
        return gerador.proximoNumeroNoIntervalo(tempoServicoMin, tempoServicoMax);
    }
    
    /**
     * Gera um tempo de chegada aleatório para o próximo cliente
     * @param gerador Gerador de números aleatórios
     * @return Tempo de chegada
     */
    public double gerarTempoChegada(GeradorNumerosPseudoAleatorios gerador) {
        if (!filaDeEntrada) {
            throw new IllegalStateException("Esta fila não é uma fila de entrada");
        }
        return gerador.proximoNumeroNoIntervalo(chegadaMin, chegadaMax);
    }
    
    // Getters
    
    public int getId() {
        return id;
    }
    
    public int getNumClientes() {
        return numClientes;
    }
    
    public int getCapacidade() {
        return capacidade;
    }
    
    public int getNumServidores() {
        return numServidores;
    }
    
    public int getServidoresOcupados() {
        return servidoresOcupados;
    }
    
    public boolean isFilaDeEntrada() {
        return filaDeEntrada;
    }
    
    public List<Rota> getRotas() {
        return rotas;
    }
    
    // Métodos para estatísticas
    
    public int getTotalClientesAtendidos() {
        return totalClientesAtendidos;
    }
    
    public int getTotalClientesPerdidos() {
        return totalClientesPerdidos;
    }
    
    public double getTempoMedioEspera(double tempoTotal) {
        return tempoTotalEspera / Math.max(1, totalClientesAtendidos);
    }
    
    public double getTaxaUtilizacaoServidores(double tempoTotal) {
        return ocupacaoTotal / (numServidores * tempoTotal);
    }
    
    public double getTempoTotalEspera() {
        return tempoTotalEspera;
    }
    
    public double getOcupacaoTotal() {
        return ocupacaoTotal;
    }
    
    public int getEstadoMaximoRegistrado() {
        return estadoMaximoRegistrado;
    }
    
    /**
     * Retorna o tempo que a fila passou em um determinado estado
     * @param estado Estado
     * @param tempoTotal Tempo total da simulação
     * @return Tempo acumulado no estado
     */
    public double getTempoEmEstado(int estado, double tempoTotal) {
        return tempoEmEstado.getOrDefault(estado, 0.0);
    }
    
    /**
     * Retorna o percentual de tempo que a fila passou em um determinado estado
     * @param estado Estado
     * @param tempoTotal Tempo total da simulação
     * @return Percentual de tempo no estado
     */
    public double getPercentualEmEstado(int estado, double tempoTotal) {
        return (getTempoEmEstado(estado, tempoTotal) / tempoTotal) * 100;
    }
    
    /**
 * Gera uma string formatada com a distribuição de estados da fila
 * @param tempoTotal Tempo total da simulação
 * @return String formatada com a distribuição de estados
 */
public String gerarDistribuicaoEstados(double tempoTotal) {
    StringBuilder sb = new StringBuilder();
    
    // Descrição da fila
    String descFila = String.format("Resultado da Fila %d: G/G/%d", id + 1, numServidores);
    if (capacidade != -1) {
        descFila += "/" + capacidade;
    }
    descFila += String.format(", atendimento entre %.1f..%.1f:", tempoServicoMin, tempoServicoMax);
    sb.append(descFila).append("\n");
    
    // Estatísticas básicas
    sb.append(String.format("Clientes processados: %d\n", totalClientesAtendidos));
    sb.append(String.format("Clientes perdidos: %d\n", totalClientesPerdidos));
    
    // Distribuição de estados
    sb.append("Distribuição de probabilidades dos estados:\n");
    
    // Ordena os estados para apresentação
    Map<Integer, Double> estadosOrdenados = new TreeMap<>(tempoEmEstado);
    
    for (Map.Entry<Integer, Double> entry : estadosOrdenados.entrySet()) {
        int estado = entry.getKey();
        double tempo = entry.getValue();
        double percentual = (tempo / tempoTotal) * 100;
        
        sb.append(String.format("Estado %d: %.2f%% (Tempo: %.2f)\n", 
                estado, percentual, tempo));
    }
    
    // Adiciona o tempo total da simulação ao final
    sb.append(String.format("\nTempo Total de Simulação: %.2f", tempoTotal));
    
    return sb.toString();
}
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fila ").append(id).append(": ");
        sb.append("Clientes: ").append(numClientes).append("/");
        sb.append(capacidade == -1 ? "∞" : capacidade).append(", ");
        sb.append("Servidores: ").append(servidoresOcupados).append("/").append(numServidores);
        
        if (!rotas.isEmpty()) {
            sb.append(", Rotas: ");
            for (Rota rota : rotas) {
                String destino = rota.getFilaDestinoId() == -1 ? "Saída" : "Fila " + rota.getFilaDestinoId();
                sb.append(destino).append("(").append(String.format("%.2f", rota.getProbabilidade() * 100)).append("%) ");
            }
        }
        
        return sb.toString();
    }
}