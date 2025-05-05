import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Classe Escalonador - responsável por gerenciar a agenda de eventos
 * e controlar o avanço do tempo de simulação.
 */
public class Escalonador {
    private final PriorityQueue<Evento> eventos;
    private final List<Fila> filas;
    private final GeradorAleatorio gerador;
    private final double[][] matrizRoteamento;
    
    private double tempoAtual;
    private double tempoUltimoEvento;
    private int numFilas;
    
    public Escalonador(List<Fila> filas, double[][] matrizRoteamento, GeradorAleatorio gerador) {
        this.filas = filas;
        this.matrizRoteamento = matrizRoteamento;
        this.gerador = gerador;
        this.eventos = new PriorityQueue<>();
        this.tempoAtual = 0.0;
        this.tempoUltimoEvento = 0.0;
        this.numFilas = filas.size();
    }
    
    /**
     * Inicializa o escalonador, agendando o primeiro evento
     * @param tempoInicial Tempo da primeira chegada
     */
    public void inicializar(double tempoInicial) {
        // Agenda o primeiro evento (chegada do primeiro cliente)
        agendarChegada(0, tempoInicial); // Primeira chegada no tempo especificado
    }
    
    /**
     * Executa a simulação até que não haja mais eventos ou
     * até que o número máximo de aleatórios seja atingido
     * @param maxAleatorios Número máximo de aleatórios a serem usados
     * @return Tempo total da simulação
     */
    public double executar(int maxAleatorios) {
        while (!eventos.isEmpty() && gerador.getContador() < maxAleatorios) {
            // Obtém o próximo evento
            Evento evento = eventos.poll();
            
            // Calcula o tempo decorrido desde o último evento
            double tempoDecorrido = evento.getTempo() - tempoAtual;
            
            // Atualiza os tempos de estado para todas as filas
            for (int i = 0; i < filas.size(); i++) {
                Fila fila = filas.get(i);
                fila.atualizarTempoEstado(tempoDecorrido);
            }
            
            // Atualiza o tempo atual
            tempoUltimoEvento = tempoAtual;
            tempoAtual = evento.getTempo();
            
            // Processa o evento
            processarEvento(evento);
        }
        
        return tempoAtual; // Retorna o tempo total da simulação
    }
    
    /**
     * Processa um evento específico
     * @param evento Evento a ser processado
     */
    private void processarEvento(Evento evento) {
        switch (evento.getTipo()) {
            case Evento.CHEGADA:
                processarChegada(evento);
                break;
            case Evento.SAIDA:
                processarSaida(evento);
                break;
        }
    }
    
    /**
     * Processa um evento de chegada
     * @param evento Evento de chegada
     */
    private void processarChegada(Evento evento) {
        int filaDestino = evento.getFilaDestino();
        
        if (filaDestino <= 0 || filaDestino > numFilas) {
            return; // Fila inválida
        }
        
        Fila fila = filas.get(filaDestino - 1); // Ajuste de índice
        
        if (fila.podeAceitarCliente()) {
            fila.In(); // Coloca o cliente na fila
            
            // Se há servidor disponível, agenda saída imediatamente
            if (fila.Status() <= fila.Servers()) {
                double tempoAtendimento = fila.getMinAtendimento() + 
                      (fila.getMaxAtendimento() - fila.getMinAtendimento()) * gerador.nextRandom();
                agendarSaida(filaDestino, tempoAtual + tempoAtendimento);
            }
        } else {
            fila.Loss(); // Cliente perdido por fila cheia
        }
        
        // Agenda a próxima chegada externa se for do mundo externo para fila 1
        if (evento.getFilaOrigem() == 0 && filaDestino == 1) {
            double tempoProximaChegada = fila.getMinChegada() + 
                  (fila.getMaxChegada() - fila.getMinChegada()) * gerador.nextRandom();
            agendarChegada(0, tempoAtual + tempoProximaChegada);
        }
    }
    
    /**
     * Processa um evento de saída
     * @param evento Evento de saída
     */
    private void processarSaida(Evento evento) {
        int filaOrigem = evento.getFilaOrigem();
        
        if (filaOrigem <= 0 || filaOrigem > numFilas) {
            return; // Fila inválida
        }
        
        Fila fila = filas.get(filaOrigem - 1); // Ajuste de índice
        
        // Cliente sai da fila
        fila.Out();
        
        // Se ainda há clientes além do número de servidores, agenda nova saída
        if (fila.Status() >= fila.Servers()) {
            double tempoAtendimento = fila.getMinAtendimento() + 
                  (fila.getMaxAtendimento() - fila.getMinAtendimento()) * gerador.nextRandom();
            agendarSaida(filaOrigem, tempoAtual + tempoAtendimento);
        }
        
        // Determina para onde o cliente vai após sair da fila
        double probabilidade = gerador.nextRandom();
        double acumulado = 0.0;
        
        int proximaFila = 0; // 0 representa saída do sistema por padrão
        
        for (int i = 0; i <= numFilas; i++) {
            acumulado += matrizRoteamento[filaOrigem][i];
            if (probabilidade <= acumulado) {
                proximaFila = i;
                break;
            }
        }
        
        if (proximaFila > 0) {
            // Cliente vai para outra fila
            agendarChegada(filaOrigem, tempoAtual, proximaFila);
        }
        // Se proximaFila == 0, o cliente sai do sistema (não é necessário agendar evento)
    }
    
    /**
     * Agenda um evento de chegada na primeira fila
     * @param filaOrigem Fila de origem (0 para mundo externo)
     * @param tempo Tempo do evento
     */
    private void agendarChegada(int filaOrigem, double tempo) {
        // Agenda chegada na primeira fila (do mundo externo)
        eventos.add(new Evento(tempo, Evento.CHEGADA, 0, filaOrigem, 1));
    }
    
    /**
     * Agenda um evento de chegada em uma fila específica
     * @param filaOrigem Fila de origem
     * @param tempo Tempo do evento
     * @param filaDestino Fila de destino
     */
    private void agendarChegada(int filaOrigem, double tempo, int filaDestino) {
        // Agenda chegada de um cliente que vem de outra fila
        eventos.add(new Evento(tempo, Evento.CHEGADA, 0, filaOrigem, filaDestino));
    }
    
    /**
     * Agenda um evento de saída
     * @param filaOrigem Fila de origem
     * @param tempo Tempo do evento
     */
    private void agendarSaida(int filaOrigem, double tempo) {
        eventos.add(new Evento(tempo, Evento.SAIDA, 0, filaOrigem, -1));
    }
    
    /**
     * Obtém o tempo atual da simulação
     * @return Tempo atual
     */
    public double getTempoAtual() {
        return tempoAtual;
    }
}