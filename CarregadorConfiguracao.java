import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por carregar a configuração da simulação a partir de um arquivo de texto simples.
 * Esta versão não depende da biblioteca SnakeYAML.
 * 
 * Formato do arquivo:
 * 
 * # Comentário
 * chave = valor
 * fila.id.propriedade = valor
 * fila.id.rota.destino = probabilidade
 */
public class CarregadorConfiguracao {
    
    /**
     * Carrega a configuração da simulação a partir de um arquivo de texto simples.
     * @param caminhoArquivo Caminho para o arquivo de configuração
     * @return Escalonador configurado com as filas e parâmetros
     * @throws IOException Se ocorrer um erro na leitura do arquivo
     */
    public static Escalonador carregarConfiguracao(String caminhoArquivo) throws IOException {
        // Mapas para armazenar as configurações
        Map<String, String> configGeral = new HashMap<>();
        Map<Integer, Map<String, String>> configFilas = new HashMap<>();
        Map<Integer, Map<String, Double>> configRotas = new HashMap<>();
        
        // Lê o arquivo de configuração
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                // Ignora linhas em branco e comentários
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue;
                }
                
                // Separa a chave e o valor
                String[] partes = linha.split("=", 2);
                if (partes.length != 2) {
                    continue;
                }
                
                String chave = partes[0].trim();
                String valor = partes[1].trim();
                
                // Processa a linha conforme o padrão da chave
                if (chave.startsWith("fila.")) {
                    processarConfigFila(chave, valor, configFilas, configRotas);
                } else {
                    configGeral.put(chave, valor);
                }
            }
        }
        
        // Parâmetros gerais da simulação
        double tempoFinalSimulacao = Double.parseDouble(configGeral.getOrDefault("tempo_simulacao", "1000.0"));
        long semente = Long.parseLong(configGeral.getOrDefault("semente", "12345"));
        
        // Cria o escalonador
        Escalonador escalonador = new Escalonador(tempoFinalSimulacao, semente);
        
        // Cria as filas
        for (Map.Entry<Integer, Map<String, String>> entry : configFilas.entrySet()) {
            int idFila = entry.getKey();
            Map<String, String> propFila = entry.getValue();
            
            int capacidade = Integer.parseInt(propFila.getOrDefault("capacidade", "-1"));
            int numServidores = Integer.parseInt(propFila.getOrDefault("servidores", "1"));
            double tempoServicoMin = Double.parseDouble(propFila.getOrDefault("tempo_servico_min", "1.0"));
            double tempoServicoMax = Double.parseDouble(propFila.getOrDefault("tempo_servico_max", "2.0"));
            
            Fila fila;
            
            // Verifica se é uma fila de entrada (com chegadas externas)
            if (propFila.containsKey("chegada_min") && propFila.containsKey("chegada_max")) {
                double chegadaMin = Double.parseDouble(propFila.get("chegada_min"));
                double chegadaMax = Double.parseDouble(propFila.get("chegada_max"));
                fila = new Fila(idFila, capacidade, numServidores, tempoServicoMin, tempoServicoMax, chegadaMin, chegadaMax);
            } else {
                fila = new Fila(idFila, capacidade, numServidores, tempoServicoMin, tempoServicoMax);
            }
            
            // Adiciona as rotas
            Map<String, Double> rotas = configRotas.getOrDefault(idFila, new HashMap<>());
            for (Map.Entry<String, Double> rotaEntry : rotas.entrySet()) {
                String destino = rotaEntry.getKey();
                double probabilidade = rotaEntry.getValue();
                
                int destinoId;
                if (destino.equals("saida")) {
                    destinoId = -1; // -1 representa saída do sistema
                } else {
                    destinoId = Integer.parseInt(destino);
                }
                
                fila.adicionarRota(destinoId, probabilidade);
            }
            
            // Adiciona a fila ao escalonador
            escalonador.adicionarFila(fila);
        }
        
        return escalonador;
    }
    
    /**
     * Processa uma linha de configuração relacionada a uma fila.
     * @param chave Chave da propriedade
     * @param valor Valor da propriedade
     * @param configFilas Mapa de configurações das filas
     * @param configRotas Mapa de configurações das rotas
     */
    private static void processarConfigFila(String chave, String valor, 
                                           Map<Integer, Map<String, String>> configFilas,
                                           Map<Integer, Map<String, Double>> configRotas) {
        // Formato da chave: fila.ID.PROPRIEDADE ou fila.ID.rota.DESTINO
        String[] partes = chave.split("\\.");
        if (partes.length < 3) {
            return;
        }
        
        int idFila = Integer.parseInt(partes[1]);
        
        if (partes.length == 3) {
            // Propriedade da fila
            String propriedade = partes[2];
            
            // Cria o mapa de propriedades da fila se não existir
            configFilas.putIfAbsent(idFila, new HashMap<>());
            configFilas.get(idFila).put(propriedade, valor);
        } else if (partes.length == 4 && partes[2].equals("rota")) {
            // Rota da fila
            String destino = partes[3];
            double probabilidade = Double.parseDouble(valor);
            
            // Cria o mapa de rotas da fila se não existir
            configRotas.putIfAbsent(idFila, new HashMap<>());
            configRotas.get(idFila).put(destino, probabilidade);
        }
    }
}