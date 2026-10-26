package excecoes;

/**
 * Lançada quando a prioridade informada não é 1 (alta) nem 2 (baixa).
 */
public class PrioridadeInvalidaException extends CentralException {

    private static final long serialVersionUID = 1L;

    public PrioridadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}
