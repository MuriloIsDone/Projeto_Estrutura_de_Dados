package excecoes;

/**
 * Exceção base de todas as situações de erro tratáveis da Central de Atendimento.
 */
public class CentralException extends Exception {

    private static final long serialVersionUID = 1L;

    public CentralException(String mensagem) {
        super(mensagem);
    }
}
