package excecoes;

/**
 * Lançada quando a operação solicitada não é permitida pelas regras do sistema.
 */
public class OperacaoInvalidaException extends CentralException {

    private static final long serialVersionUID = 1L;

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
