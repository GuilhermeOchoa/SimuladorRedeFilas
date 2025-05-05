import java.util.Random;

/**
 * Classe responsável pela geração de números pseudoaleatórios usando o Método Congruente Linear.
 */
public class GeradorNumerosPseudoAleatorios {
    // Parâmetros para o método congruente linear
    private long a; // multiplicador
    private long c; // incremento
    private long m; // módulo
    private long semente; // valor inicial
    private long ultimoValor; // último valor gerado
    private Random random; // gerador padrão do Java (alternativa)

    /**
     * Construtor que inicializa com parâmetros específicos
     */
    public GeradorNumerosPseudoAleatorios(long a, long c, long m, long semente) {
        this.a = a;
        this.c = c;
        this.m = m;
        this.semente = semente;
        this.ultimoValor = semente;
        this.random = new Random(semente); // inicializa o gerador padrão também
    }

    /**
     * Construtor com valores padrão
     */
    public GeradorNumerosPseudoAleatorios() {
        // Valores padrão comuns para o método congruente linear
        this(25214903917L, 11L, (long) Math.pow(2, 48), System.currentTimeMillis());
    }

    /**
     * Gera o próximo número da sequência usando o método congruente linear
     * @return um número entre 0 e 1
     */
    public double proximoNumero() {
        // Fórmula do método congruente linear: X_{n+1} = (a * X_n + c) mod m
        ultimoValor = (a * ultimoValor + c) % m;
        // Normaliza o valor para ficar entre 0 e 1
        return (double) ultimoValor / m;
    }

    /**
     * Gera um número aleatório entre min e max
     * @param min valor mínimo (inclusive)
     * @param max valor máximo (inclusive)
     * @return número aleatório no intervalo [min, max]
     */
    public double proximoNumeroNoIntervalo(double min, double max) {
        return min + (proximoNumero() * (max - min));
    }

    /**
     * Gera um número inteiro aleatório entre min e max
     * @param min valor mínimo (inclusive)
     * @param max valor máximo (inclusive)
     * @return número inteiro aleatório no intervalo [min, max]
     */
    public int proximoInteiroNoIntervalo(int min, int max) {
        return min + (int) (proximoNumero() * (max - min + 1));
    }

    /**
     * Reinicia o gerador com uma nova semente
     * @param novaSemente nova semente para o gerador
     */
    public void reiniciar(long novaSemente) {
        this.semente = novaSemente;
        this.ultimoValor = novaSemente;
        this.random = new Random(novaSemente);
    }
}