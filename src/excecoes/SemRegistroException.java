package excecoes;

/**
 * Lançada ao consultar o histórico de operações ou o histórico de atendimentos quando estão vazios.
 */
public class SemRegistroException extends CentralException {

    private static final long serialVersionUID = 1L;

    public SemRegistroException(String mensagem) {
        super(mensagem);
    }
}
