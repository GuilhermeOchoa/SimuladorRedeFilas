import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Classe para validar o simulador com o modelo especificado.
 * Versão atualizada para gerar a distribuição de estados por fila.
 */
public class ValidadorDistribuicao {
    
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando validação do simulador com distribuição de estados por fila...");
            
            // Caminho para o arquivo de configuração
            String arquivoConfig = "config-rede-ajustada.txt";
            
            // Executa a simulação
            Escalonador escalonador = executarSimulacao(arquivoConfig);
            
            // Salva a distribuição de estados por fila
            salvarDistribuicaoEstados(escalonador, "distribuicao_estados_por_fila.txt");
            
            System.out.println("Validação concluída. Resultados salvos em 'distribuicao_estados_por_fila.txt'");
            
        } catch (IOException e) {
            System.err.println("Erro ao executar a validação: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Executa a simulação a partir de um arquivo de configuração
     * @param arquivoConfig Caminho para o arquivo de configuração
     * @return Escalonador após a simulação concluída
     * @throws IOException Se ocorrer erro ao ler o arquivo
     */
    public static Escalonador executarSimulacao(String arquivoConfig) throws IOException {
        // Carrega a configuração
        Escalonador escalonador = CarregadorConfiguracao.carregarConfiguracao(arquivoConfig);
        
        // Inicializa e executa a simulação
        escalonador.inicializar();
        escalonador.executarSimulacao();
        
        return escalonador;
    }
    
    /**
     * Salva a distribuição de estados por fila em um arquivo
     * @param escalonador Escalonador após simulação
     * @param nomeArquivo Nome do arquivo para salvar os resultados
     * @throws IOException Se ocorrer erro ao escrever no arquivo
     */
    private static void salvarDistribuicaoEstados(Escalonador escalonador, String nomeArquivo) throws IOException {
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            List<Fila> filas = escalonador.getFilas();
            double tempoTotal = escalonador.getTempoAtual();
            
            for (Fila fila : filas) {
                writer.write(fila.gerarDistribuicaoEstados(tempoTotal));
                writer.write("\n\n");
            }
            
            writer.write(String.format("Tempo Total de Simulação: %.2f", tempoTotal));
        }
    }
}