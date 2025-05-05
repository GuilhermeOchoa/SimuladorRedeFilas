import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Classe responsável por coletar e armazenar estatísticas da distribuição de estados do sistema.
 * Registra o tempo que o sistema passa em cada estado (número total de clientes).
 */
public class ColetadorEstatisticas {
    private Map<Integer, Double> tempoEmEstado;       // Mapeia o estado para o tempo acumulado
    private int estadoAtual;                          // Estado atual do sistema
    private double ultimoTempoMudanca;                // Tempo da última mudança de estado
    private double tempoFinalSimulacao;               // Tempo final da simulação
    private int estadoMaximoRegistrado;               // Maior estado encontrado durante a simulação
    
    /**
     * Construtor do ColetadorEstatisticas
     */
    public ColetadorEstatisticas() {
        this.tempoEmEstado = new HashMap<>();
        this.estadoAtual = 0;
        this.ultimoTempoMudanca = 0.0;
        this.estadoMaximoRegistrado = 0;
    }
    
    /**
     * Registra uma mudança no número total de clientes no sistema
     * @param novoEstado Novo número total de clientes
     * @param tempoAtual Tempo atual da simulação
     */
    public void registrarMudancaEstado(int novoEstado, double tempoAtual) {
        // Calcula o tempo no estado atual
        double tempoDuracao = tempoAtual - ultimoTempoMudanca;
        
        // Acumula o tempo no estado atual
        tempoEmEstado.put(estadoAtual, 
                tempoEmEstado.getOrDefault(estadoAtual, 0.0) + tempoDuracao);
        
        // Atualiza o estado atual e o tempo da última mudança
        estadoAtual = novoEstado;
        ultimoTempoMudanca = tempoAtual;
        
        // Atualiza o estado máximo registrado
        if (novoEstado > estadoMaximoRegistrado) {
            estadoMaximoRegistrado = novoEstado;
        }
    }
    
    /**
     * Finaliza a coleta de estatísticas, registrando o tempo final no último estado
     * @param tempoFinal Tempo final da simulação
     */
    public void finalizarColeta(double tempoFinal) {
        this.tempoFinalSimulacao = tempoFinal;
        
        // Acumula o tempo restante no estado atual
        double tempoDuracao = tempoFinal - ultimoTempoMudanca;
        tempoEmEstado.put(estadoAtual, 
                tempoEmEstado.getOrDefault(estadoAtual, 0.0) + tempoDuracao);
    }
    
    /**
     * Retorna uma representação em string das estatísticas coletadas
     * @return String formatada com as estatísticas
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Distribuição de Estados do Sistema\n");
        sb.append("--------------------------------\n\n");
        
        // Ordena os estados para apresentação
        Map<Integer, Double> estadosOrdenados = new TreeMap<>(tempoEmEstado);
        
        for (Map.Entry<Integer, Double> entry : estadosOrdenados.entrySet()) {
            int estado = entry.getKey();
            double tempo = entry.getValue();
            double percentual = (tempo / tempoFinalSimulacao) * 100;
            
            sb.append(String.format("Estado %d: %.2f%% (Tempo: %.2f)\n", 
                    estado, percentual, tempo));
        }
        
        sb.append("\nTempo Total de Simulação: ").append(String.format("%.2f", tempoFinalSimulacao));
        
        return sb.toString();
    }
    
    /**
     * Retorna o tempo que o sistema passou em um determinado estado
     * @param estado Estado
     * @return Tempo acumulado no estado
     */
    public double getTempoEmEstado(int estado) {
        return tempoEmEstado.getOrDefault(estado, 0.0);
    }
    
    /**
     * Retorna o percentual de tempo que o sistema passou em um determinado estado
     * @param estado Estado
     * @return Percentual de tempo no estado
     */
    public double getPercentualEmEstado(int estado) {
        return (getTempoEmEstado(estado) / tempoFinalSimulacao) * 100;
    }
    
    /**
     * Retorna o tempo total da simulação
     * @return Tempo total
     */
    public double getTempoTotalSimulacao() {
        return tempoFinalSimulacao;
    }
    
    /**
     * Retorna o maior estado (número de clientes) registrado durante a simulação
     * @return Estado máximo
     */
    public int getEstadoMaximoRegistrado() {
        return estadoMaximoRegistrado;
    }
}