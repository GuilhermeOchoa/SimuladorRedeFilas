import java.io.IOException;

/**
 * Classe principal que executa a simulação de redes de filas.
 */
public class Simulador {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Simulador <arquivo-configuracao.yml>");
            System.exit(1);
        }
        
        String arquivoConfig = args[0];
        
        try {
            // Carrega a configuração a partir do arquivo YAML
            System.out.println("Carregando configuração do arquivo: " + arquivoConfig);
            Escalonador escalonador = CarregadorConfiguracao.carregarConfiguracao(arquivoConfig);
            
            // Inicializa a simulação
            System.out.println("Inicializando simulação...");
            escalonador.inicializar();
            
            // Executa a simulação
            System.out.println("Executando simulação...");
            escalonador.executarSimulacao();
            
            System.out.println("Simulação concluída com sucesso!");
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de configuração: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro durante a simulação: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Método para executar a simulação a partir de um código Java (não via linha de comando)
     * @param arquivoConfig Caminho para o arquivo de configuração YAML
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
}