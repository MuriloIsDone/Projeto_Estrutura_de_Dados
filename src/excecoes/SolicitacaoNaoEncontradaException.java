package excecoes;

/**
 * Lançada quando o código informado não existe no cadastro geral.
 */
public class SolicitacaoNaoEncontradaException extends CentralException {

    private static final long serialVersionUID = 1L;

    public SolicitacaoNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
