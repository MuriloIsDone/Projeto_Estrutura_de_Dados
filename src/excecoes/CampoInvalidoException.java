package excecoes;

/**
 * Lançada quando um campo de texto obrigatório está vazio ou um valor informado não é aceito.
 */
public class CampoInvalidoException extends CentralException {

    private static final long serialVersionUID = 1L;

    public CampoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
