/**
 * Classe que representa um evento na simulação.
 * Um evento tem um tipo, um tempo de ocorrência, e referências para as filas de origem e destino.
 */
public class Evento implements Comparable<Evento> {
    
    // Tipos de eventos possíveis
    public enum TipoEvento {
        CHEGADA,    // Chegada de cliente do exterior para uma fila
        PASSAGEM,   // Passagem de cliente de uma fila para outra
        SAIDA       // Saída de cliente de uma fila para o exterior
    }
    
    private TipoEvento tipo;          // Tipo do evento
    private double tempo;             // Tempo em que o evento ocorre
    private int filaOrigemId;         // ID da fila de origem (-1 se for chegada do exterior)
    private int filaDestinoId;        // ID da fila de destino (-1 se for saída para o exterior)
    
    /**
     * Construtor para eventos de chegada do exterior
     * @param tempo Tempo em que o evento ocorre
     * @param filaDestinoId ID da fila de destino
     */
    public Evento(TipoEvento tipo, double tempo, int filaDestinoId) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.filaOrigemId = -1; // -1 representa o exterior
        this.filaDestinoId = filaDestinoId;
    }
    
    /**
     * Construtor para eventos entre filas ou saída
     * @param tipo Tipo do evento (PASSAGEM ou SAIDA)
     * @param tempo Tempo em que o evento ocorre
     * @param filaOrigemId ID da fila de origem
     * @param filaDestinoId ID da fila de destino (-1 se for saída para o exterior)
     */
    public Evento(TipoEvento tipo, double tempo, int filaOrigemId, int filaDestinoId) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.filaOrigemId = filaOrigemId;
        this.filaDestinoId = filaDestinoId;
    }
    
    // Getters e setters
    
    public TipoEvento getTipo() {
        return tipo;
    }
    
    public double getTempo() {
        return tempo;
    }
    
    public int getFilaOrigemId() {
        return filaOrigemId;
    }
    
    public int getFilaDestinoId() {
        return filaDestinoId;
    }
    
    /**
     * Compara eventos pelo tempo de ocorrência.
     * Usado para ordenar a fila de prioridade do escalonador.
     */
    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
    
    @Override
    public String toString() {
        String origem = (filaOrigemId == -1) ? "Exterior" : "Fila " + filaOrigemId;
        String destino = (filaDestinoId == -1) ? "Exterior" : "Fila " + filaDestinoId;
        
        return String.format("Evento: %s, Tempo: %.2f, De: %s, Para: %s", 
                tipo, tempo, origem, destino);
    }
}