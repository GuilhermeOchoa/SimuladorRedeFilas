import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Classe para validar o simulador com o modelo especificado.
 * Versão final que inclui o tempo total da simulação.
 */
public class ValidadorFinal {
    
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando validação final do simulador...");
            
            // Caminho para o arquivo de configuração
            String arquivoConfig = "config-rede-final.txt";
            
            // Executa a simulação
            Escalonador escalonador = executarSimulacao(arquivoConfig);
            
            // Salva a distribuição de estados por fila com tempo total
            salvarDistribuicaoEstados(escalonador, "resultado_final.txt");
            
            System.out.println("Validação concluída. Resultados salvos em 'resultado_final.txt'");
            
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
            
            for (int i = 0; i < filas.size(); i++) {
                Fila fila = filas.get(i);
                
                // Verifica se não é a primeira fila para adicionar espaços em branco entre os resultados
                if (i > 0) {
                    writer.write("\n\n** **\n** **\n\n");
                }
                
                writer.write(fila.gerarDistribuicaoEstados(tempoTotal));
                
                // Não adiciona linhas em branco após a última fila
                if (i < filas.size() - 1) {
                    writer.write("\n");
                }
            }
        }
    }
}