package excecoes;

/**
 * Indica uma falha interna inesperada ao usar uma estrutura de dados.
 * As classes fornecidas pelo professor declaram "throws Exception"; como o
 * sistema verifica as condições antes de cada chamada, esta exceção não deve
 * ocorrer em uso normal. É não verificada (RuntimeException) por indicar defeito
 * de programação, e não erro de uso.
 */
public class EstruturaInconsistenteException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstruturaInconsistenteException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public EstruturaInconsistenteException(String mensagem) {
        super(mensagem);
    }
}
