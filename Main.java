import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe principal para executar o simulador com geração de relatório em arquivo
 */
public class Main {
    public static void main(String[] args) {
        // Definir o número de aleatórios
        int numAleatorios = 100000;
        
        // Nome do arquivo de saída com timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String arquivoSaida = "relatorio_simulacao_" + timestamp + ".txt";
        
        // Criar o simulador com arquivo de configuração
        Simulador simulador = new Simulador("config.txt", numAleatorios);
        
        // Executar a simulação
        simulador.executar();
        
        // Gerar o relatório em arquivo
        simulador.gerarRelatorioArquivo(arquivoSaida);
        
        System.out.println("Simulação concluída com sucesso!");
    }
}