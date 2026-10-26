package excecoes;

/**
 * Lançada quando não há solicitação aguardando atendimento nas filas.
 */
public class SemSolicitacaoException extends CentralException {

    private static final long serialVersionUID = 1L;

    public SemSolicitacaoException(String mensagem) {
        super(mensagem);
    }
}
