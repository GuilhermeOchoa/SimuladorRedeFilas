import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe para executar ambas as simulações e comparar os resultados
 */
public class ComparacaoSimulacoes {
    public static void main(String[] args) {
        // Definir o número de aleatórios
        int numAleatorios = 100000;
        
        // Timestamp para identificar os arquivos de relatório
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        System.out.println("=== INICIANDO SIMULAÇÃO DO MODELO ORIGINAL ===");
        
        // Simular o modelo original
        String arquivoOriginal = "config.txt";
        String relatorioOriginal = "relatorio_original_" + timestamp + ".txt";
        
        Simulador simuladorOriginal = new Simulador(arquivoOriginal, numAleatorios);
        simuladorOriginal.executar();
        simuladorOriginal.gerarRelatorioArquivo(relatorioOriginal);
        
        System.out.println("\n=== INICIANDO SIMULAÇÃO DO MODELO MELHORADO ===");
        
        // Simular o modelo melhorado
        String arquivoMelhorado = "config_improved.txt";
        String relatorioMelhorado = "relatorio_melhorado_" + timestamp + ".txt";
        
        Simulador simuladorMelhorado = new Simulador(arquivoMelhorado, numAleatorios);
        simuladorMelhorado.executar();
        simuladorMelhorado.gerarRelatorioArquivo(relatorioMelhorado);
        
        System.out.println("\n=== SIMULAÇÕES CONCLUÍDAS ===");
        System.out.println("Relatório do modelo original: " + relatorioOriginal);
        System.out.println("Relatório do modelo melhorado: " + relatorioMelhorado);
        
        // Aqui poderia ser adicionado código para análise automática dos resultados
        // Por exemplo, cálculo das métricas de desempenho e geração de gráficos comparativos
    }
}