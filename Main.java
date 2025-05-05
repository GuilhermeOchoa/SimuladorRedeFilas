/**
 * Classe principal para executar o simulador
 */
public class Main {
    public static void main(String[] args) {
        // Definir o número de aleatórios
        int numAleatorios = 100000;
        
        // Criar o simulador com arquivo de configuração
        Simulador simulador = new Simulador("config.txt", numAleatorios);
        
        // Executar a simulação
        simulador.executar();
    }
}

/**
 * Formato do arquivo de configuração config.txt:
 * 
 * 3                   # Número de filas
 * 
 * # capacidade numServ minCheg maxCheg minAtend maxAtend
 * 4 1 2.0 4.0 1.0 2.0  # Fila 1: G/G/1 com capacidade 4
 * 5 2 0.0 0.0 4.0 8.0  # Fila 2: G/G/2/5 (tempos de chegada não usados)
 * 10 2 0.0 0.0 5.0 15.0 # Fila 3: G/G/2/10 (tempos de chegada não usados)
 * 
 * # Matriz de roteamento (origem destino probabilidade)
 * # Origem 0 = mundo externo
 * # Destino 0 = saída do sistema
 * 
 * # Da Fila 1 (índice 1)
 * 1 2 0.8  # 80% da Fila 1 vão para Fila 2
 * 1 3 0.2  # 20% da Fila 1 vão para Fila 3
 * 
 * # Da Fila 2 (índice 2)
 * 2 0 0.5  # 50% da Fila 2 saem do sistema
 * 2 1 0.3  # 30% da Fila 2 voltam para Fila 1
 * 
 * # Da Fila 3 (índice 3)
 * 3 2 0.7  # 70% da Fila 3 vão para Fila 2
 * 3 0 0.3  # 30% da Fila 3 saem do sistema
 */