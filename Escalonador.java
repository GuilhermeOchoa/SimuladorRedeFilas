import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Classe responsável pelo escalonamento de eventos na simulação.
 * Mantém uma fila de prioridade de eventos ordenados pelo tempo.
 * Versão atualizada para incluir estatísticas de estados por fila.
 */
public class Escalonador {
    private PriorityQueue<Evento> filaEventos;    // Fila de prioridade para eventos
    private double tempoAtual;                    // Tempo atual da simulação
    private double tempoFinalSimulacao;           // Tempo final da simulação
    private List<Fila> filas;                     // Lista de filas no sistema
    private GeradorNumerosPseudoAleatorios gerador; // Gerador de números aleatórios
    
    // Estatísticas globais
    private int totalClientesProcessados;         // Total de clientes que passaram pelo sistema
    private int totalClientesRejeitados;          // Total de clientes que não entraram no sistema
    private int totalClientesConcluidos;          // Total de clientes que saíram do sistema
    
    /**
     * Construtor do escalonador
     * @param tempoFinalSimulacao Tempo limite da simulação
     * @param semente Semente para o gerador de números aleatórios
     */
    public Escalonador(double tempoFinalSimulacao, long semente) {
        this.filaEventos = new PriorityQueue<>();
        this.tempoAtual = 0.0;
        this.tempoFinalSimulacao = tempoFinalSimulacao;
        this.filas = new ArrayList<>();
        this.gerador = new GeradorNumerosPseudoAleatorios();
        this.gerador.reiniciar(semente);
        
        this.totalClientesProcessados = 0;
        this.totalClientesRejeitados = 0;
        this.totalClientesConcluidos = 0;
    }
    
    /**
     * Adiciona uma fila ao sistema
     * @param fila Fila a ser adicionada
     */
    public void adicionarFila(Fila fila) {
        filas.add(fila);
    }
    
    /**
     * Inicializa a simulação, agendando os eventos iniciais de chegada
     */
    public void inicializar() {
        // Agenda chegadas iniciais para todas as filas de entrada
        for (Fila fila : filas) {
            if (fila.isFilaDeEntrada()) {
                double tempoProximaChegada = fila.gerarTempoChegada(gerador);
                agendarEvento(new Evento(Evento.TipoEvento.CHEGADA, tempoProximaChegada, fila.getId()));
            }
        }
    }
    
    /**
     * Agenda um evento na fila de prioridade
     * @param evento Evento a ser agendado
     */
    public void agendarEvento(Evento evento) {
        if (evento.getTempo() <= tempoFinalSimulacao) {
            filaEventos.add(evento);
        }
    }
    
    /**
     * Executa a simulação até o tempo final
     */
    public void executarSimulacao() {
        System.out.println("Iniciando simulação...");
        
        while (!filaEventos.isEmpty()) {
            Evento evento = filaEventos.poll();
            tempoAtual = evento.getTempo();
            
            if (tempoAtual > tempoFinalSimulacao) {
                break;
            }
            
            processarEvento(evento);
            
            // Log periódico (opcional)
            if (totalClientesProcessados % 1000 == 0 && totalClientesProcessados > 0) {
                System.out.printf("Tempo: %.2f, Clientes processados: %d%n", 
                        tempoAtual, totalClientesProcessados);
            }
        }
        
        // Finaliza a coleta de estatísticas para todas as filas
        for (Fila fila : filas) {
            fila.finalizarEstatisticas(tempoAtual);
        }
        
        System.out.println("Simulação concluída!");
        imprimirResultados();
    }
    
    /**
     * Processa um evento da simulação
     * @param evento Evento a ser processado
     */
    private void processarEvento(Evento evento) {
        switch (evento.getTipo()) {
            case CHEGADA:
                processarChegada(evento);
                break;
                
            case PASSAGEM:
                processarPassagem(evento);
                break;
                
            case SAIDA:
                processarSaida(evento);
                break;
        }
    }
    
    /**
     * Processa um evento de chegada de cliente do exterior
     * @param evento Evento de chegada
     */
    private void processarChegada(Evento evento) {
        int filaDestinoId = evento.getFilaDestinoId();
        Fila filaDestino = filas.get(filaDestinoId);
        
        // Agenda a próxima chegada para esta fila
        double tempoProximaChegada = tempoAtual + filaDestino.gerarTempoChegada(gerador);
        agendarEvento(new Evento(Evento.TipoEvento.CHEGADA, tempoProximaChegada, filaDestinoId));
        
        // Tenta adicionar o cliente à fila
        boolean clienteAceito = filaDestino.adicionarCliente(tempoAtual);
        totalClientesProcessados++;
        
        if (clienteAceito) {
            // Se a fila tem servidores disponíveis, agenda o atendimento
            if (filaDestino.getServidoresOcupados() <= filaDestino.getNumServidores()) {
                agendarAtendimento(filaDestino);
            }
        } else {
            totalClientesRejeitados++;
        }
    }
    
    /**
     * Processa um evento de passagem de cliente entre filas
     * @param evento Evento de passagem
     */
    private void processarPassagem(Evento evento) {
        int filaOrigemId = evento.getFilaOrigemId();
        int filaDestinoId = evento.getFilaDestinoId();
        
        Fila filaOrigem = filas.get(filaOrigemId);
        Fila filaDestino = filas.get(filaDestinoId);
        
        // Remove o cliente da fila de origem
        filaOrigem.removerCliente(tempoAtual);
        
        // Se há clientes em espera na fila de origem, agenda atendimento para o próximo
        if (filaOrigem.getNumClientes() > 0 && 
            filaOrigem.getServidoresOcupados() < filaOrigem.getNumServidores()) {
            agendarAtendimento(filaOrigem);
        }
        
        // Tenta adicionar o cliente à fila de destino
        boolean clienteAceito = filaDestino.adicionarCliente(tempoAtual);
        
        if (clienteAceito) {
            // Se a fila tem servidores disponíveis, agenda o atendimento
            if (filaDestino.getServidoresOcupados() <= filaDestino.getNumServidores()) {
                agendarAtendimento(filaDestino);
            }
        } else {
            // Cliente rejeitado por fila cheia
            // Observe que este cliente já foi contado como processado,
            // mas não foi aceito na fila de destino
        }
    }
    
    /**
     * Processa um evento de saída de cliente do sistema
     * @param evento Evento de saída
     */
    private void processarSaida(Evento evento) {
        int filaOrigemId = evento.getFilaOrigemId();
        Fila filaOrigem = filas.get(filaOrigemId);
        
        // Remove o cliente da fila de origem
        filaOrigem.removerCliente(tempoAtual);
        totalClientesConcluidos++;
        
        // Se há clientes em espera na fila de origem, agenda atendimento para o próximo
        if (filaOrigem.getNumClientes() > 0 && 
            filaOrigem.getServidoresOcupados() < filaOrigem.getNumServidores()) {
            agendarAtendimento(filaOrigem);
        }
    }
    
    /**
     * Agenda um atendimento para a fila especificada
     * @param fila Fila que terá um cliente atendido
     */
    private void agendarAtendimento(Fila fila) {
        // Gera o tempo de serviço
        double tempoServico = fila.gerarTempoServico(gerador);
        double tempoAtendimento = tempoAtual + tempoServico;
        
        // Determina para qual fila o cliente irá depois do atendimento
        int proximaFilaId = fila.escolherProximaRota(gerador);
        
        // Cria o evento adequado (PASSAGEM ou SAIDA)
        Evento eventoAtendimento;
        if (proximaFilaId == -1) {
            // Cliente sai do sistema
            eventoAtendimento = new Evento(Evento.TipoEvento.SAIDA, tempoAtendimento, fila.getId(), -1);
        } else {
            // Cliente vai para outra fila
            eventoAtendimento = new Evento(Evento.TipoEvento.PASSAGEM, tempoAtendimento, fila.getId(), proximaFilaId);
        }
        
        // Agenda o evento
        agendarEvento(eventoAtendimento);
    }
    
    /**
     * Imprime os resultados da simulação
     */
    public void imprimirResultados() {
        System.out.println("\n===== RESULTADOS DA SIMULAÇÃO =====");
        System.out.printf("Tempo total simulado: %.2f%n", tempoAtual);
        System.out.printf("Total de clientes processados: %d%n", totalClientesProcessados);
        System.out.printf("Total de clientes rejeitados: %d (%.2f%%)%n", 
                totalClientesRejeitados, 
                (double) totalClientesRejeitados / Math.max(1, totalClientesProcessados) * 100);
        System.out.printf("Total de clientes que concluíram o atendimento: %d%n", totalClientesConcluidos);
        
        System.out.println("\n----- Estatísticas por Fila -----");
        for (Fila fila : filas) {
            System.out.println(fila);
            System.out.printf("  Clientes atendidos: %d%n", fila.getTotalClientesAtendidos());
            System.out.printf("  Clientes perdidos: %d%n", fila.getTotalClientesPerdidos());
            System.out.printf("  Tempo médio de espera: %.2f%n", fila.getTempoMedioEspera(tempoAtual));
            System.out.printf("  Taxa de utilização de servidores: %.2f%%%n", 
                    fila.getTaxaUtilizacaoServidores(tempoAtual) * 100);
            System.out.println();
        }
        
        System.out.println("\n----- Distribuição de Estados por Fila -----");
        for (Fila fila : filas) {
            System.out.println(fila.gerarDistribuicaoEstados(tempoAtual));
            System.out.println();
        }
    }
    
    /**
     * Retorna uma representação textual das filas do sistema
     * @return String com a descrição das filas
     */
    public String representacaoTextualFilas() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Estado atual das filas:\n");
        for (Fila fila : filas) {
            sb.append(fila.toString()).append("\n");
        }
        
        return sb.toString();
    }
    
    // Getters para estatísticas
    
    public double getTempoAtual() {
        return tempoAtual;
    }
    
    public int getTotalClientesProcessados() {
        return totalClientesProcessados;
    }
    
    public int getTotalClientesRejeitados() {
        return totalClientesRejeitados;
    }
    
    public int getTotalClientesConcluidos() {
        return totalClientesConcluidos;
    }
    
    public List<Fila> getFilas() {
        return filas;
    }
    
    public double getTempoFinalSimulacao() {
        return tempoFinalSimulacao;
    }
    
    /**
     * Salva os resultados da simulação em um formato específico
     * @return String com os resultados formatados
     */
    public String salvarResultados() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Resultados da Simulação\n");
        sb.append("======================\n\n");
        
        sb.append(String.format("Tempo total simulado: %.2f\n", tempoAtual));
        sb.append(String.format("Total de clientes processados: %d\n", totalClientesProcessados));
        sb.append(String.format("Total de clientes rejeitados: %d (%.2f%%)\n", 
                totalClientesRejeitados, 
                (double) totalClientesRejeitados / Math.max(1, totalClientesProcessados) * 100));
        sb.append(String.format("Total de clientes que concluíram: %d\n\n", totalClientesConcluidos));
        
        sb.append("Distribuição de Estados por Fila\n");
        sb.append("===============================\n\n");
        
        for (Fila fila : filas) {
            sb.append(fila.gerarDistribuicaoEstados(tempoAtual));
            sb.append("\n\n");
        }
        
        return sb.toString();
    }
}